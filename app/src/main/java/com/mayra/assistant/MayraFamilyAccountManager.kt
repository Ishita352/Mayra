package com.mayra.assistant

import android.content.SharedPreferences
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

/**
 * Persistent identity + authentication/session layer for Mayra.
 *
 * Owner controls all family accounts. At most 10 family users may exist.
 * Family credentials are stored as salted SHA-256 hashes, never plaintext.
 * Sessions are short-lived and are invalidated when a user is disabled.
 */
class MayraFamilyAccountManager(
    private val store: Store,
    private val clockMs: () -> Long = { System.currentTimeMillis() },
    private val random: SecureRandom = SecureRandom()
) {
    interface Store {
        fun get(key: String): String?
        fun put(key: String, value: String)
        fun remove(key: String)
        fun keys(prefix: String): Set<String>
    }

    class SharedPreferencesStore(private val prefs: SharedPreferences) : Store {
        override fun get(key: String): String? = prefs.getString(key, null)
        override fun put(key: String, value: String) { prefs.edit().putString(key, value).apply() }
        override fun remove(key: String) { prefs.edit().remove(key).apply() }
        override fun keys(prefix: String): Set<String> =
            prefs.all.keys.filter { it.startsWith(prefix) }.toSet()
    }

    data class AuthenticatedSession(
        val userId: String,
        val role: MayraUserIdentity.Role,
        val issuedAtMs: Long,
        val expiresAtMs: Long
    )

    private data class Credential(val salt: String, val hash: String)

    private val sessions = ConcurrentHashMap<String, AuthenticatedSession>()

    fun createFamilyUser(
        actorRole: MayraUserIdentity.Role,
        displayName: String,
        password: String,
        permissions: Set<MayraFamilyAccessPolicy.Capability> =
            MayraFamilyAccessPolicy.defaultFamilyCapabilities()
    ): MayraFamilyUserProfile {
        require(actorRole == MayraUserIdentity.Role.OWNER) { "Only Owner can create family users." }
        require(displayName.isNotBlank()) { "Display name is required." }
        require(password.length >= 6) { "Family password must contain at least 6 characters." }
        val existing = listFamilyUsers()
        require(existing.size < MayraUserIdentity.MAX_FAMILY_USERS) { "Maximum family-user limit reached." }

        val userId = MayraUserIdentity.familyUserIds().first { id ->
            existing.none { it.userId == id }
        }
        val profile = MayraFamilyUserProfile(
            userId = userId,
            displayName = displayName.trim().take(80),
            enabled = true,
            permissions = MayraFamilyUserProfilePolicy.sanitizePermissions(permissions)
        )
        saveProfile(profile)
        saveCredential(userId, password)
        return profile
    }

    fun updateFamilyUser(
        actorRole: MayraUserIdentity.Role,
        profile: MayraFamilyUserProfile
    ): MayraFamilyUserProfile {
        require(actorRole == MayraUserIdentity.Role.OWNER) { "Only Owner can edit family users." }
        require(MayraUserIdentity.isValidFamilyUserId(profile.userId))
        require(profile.displayName.isNotBlank())
        val current = requireNotNull(loadProfile(profile.userId)) { "Family user does not exist." }
        val updated = profile.copy(
            displayName = profile.displayName.trim().take(80),
            permissions = MayraFamilyUserProfilePolicy.sanitizePermissions(profile.permissions)
        )
        saveProfile(updated)
        if (current.enabled && !updated.enabled) invalidate(updated.userId)
        return updated
    }

    fun changePassword(
        actorRole: MayraUserIdentity.Role,
        userId: String,
        newPassword: String
    ) {
        require(actorRole == MayraUserIdentity.Role.OWNER) { "Only Owner can change family passwords." }
        require(MayraUserIdentity.isValidFamilyUserId(userId))
        require(loadProfile(userId) != null) { "Family user does not exist." }
        require(newPassword.length >= 6) { "Family password must contain at least 6 characters." }
        saveCredential(userId, newPassword)
        invalidate(userId)
    }

    fun deleteFamilyUser(actorRole: MayraUserIdentity.Role, userId: String) {
        require(actorRole == MayraUserIdentity.Role.OWNER) { "Only Owner can delete family users." }
        require(MayraUserIdentity.isValidFamilyUserId(userId))
        store.remove(profileKey(userId))
        store.remove(credentialKey(userId))
        invalidate(userId)
    }

    fun authenticateFamilyUser(
        userId: String,
        password: String,
        sessionTtlMs: Long = DEFAULT_SESSION_TTL_MS
    ): AuthenticatedSession? {
        require(MayraUserIdentity.isValidFamilyUserId(userId))
        require(sessionTtlMs in MIN_SESSION_TTL_MS..MAX_SESSION_TTL_MS)
        val profile = loadProfile(userId) ?: return null
        if (!profile.enabled) return null
        val credential = loadCredential(userId) ?: return null
        if (!constantTimeEquals(credential.hash, hashPassword(password, credential.salt))) return null
        val now = clockMs()
        val session = AuthenticatedSession(userId, MayraUserIdentity.Role.FAMILY_USER, now, now + sessionTtlMs)
        sessions[userId] = session
        return session
    }

    fun authenticateOwnerSession(sessionTtlMs: Long = DEFAULT_SESSION_TTL_MS): AuthenticatedSession {
        require(sessionTtlMs in MIN_SESSION_TTL_MS..MAX_SESSION_TTL_MS)
        val now = clockMs()
        return AuthenticatedSession(
            MayraUserIdentity.OWNER_SLOT,
            MayraUserIdentity.Role.OWNER,
            now,
            now + sessionTtlMs
        )
    }

    fun currentSession(userId: String): AuthenticatedSession? {
        val session = sessions[userId] ?: return null
        if (clockMs() >= session.expiresAtMs) {
            sessions.remove(userId)
            return null
        }
        val profile = loadProfile(userId)
        if (session.role == MayraUserIdentity.Role.FAMILY_USER && profile?.enabled != true) {
            sessions.remove(userId)
            return null
        }
        return session
    }

    fun mayUse(
        session: AuthenticatedSession,
        capability: MayraFamilyAccessPolicy.Capability
    ): Boolean {
        if (clockMs() >= session.expiresAtMs) return false
        return when (session.role) {
            MayraUserIdentity.Role.OWNER -> true
            MayraUserIdentity.Role.FAMILY_USER -> {
                val active = currentSession(session.userId) ?: return false
                val profile = loadProfile(active.userId) ?: return false
                MayraFamilyUserProfilePolicy.mayUse(profile, capability)
            }
        }
    }

    fun listFamilyUsers(): List<MayraFamilyUserProfile> =
        MayraUserIdentity.familyUserIds().mapNotNull { loadProfile(it) }

    fun invalidate(userId: String) {
        sessions.remove(userId)
    }

    private fun saveProfile(profile: MayraFamilyUserProfile) {
        store.put(
            profileKey(profile.userId),
            listOf(
                profile.displayName,
                profile.enabled.toString(),
                profile.permissions.map { it.name }.sorted().joinToString(",")
            ).joinToString("|")
        )
    }

    private fun loadProfile(userId: String): MayraFamilyUserProfile? {
        val raw = store.get(profileKey(userId)) ?: return null
        val parts = raw.split("|", limit = 3)
        if (parts.size != 3) return null
        val permissions = parts[2].split(",")
            .filter { it.isNotBlank() }
            .mapNotNull { value -> runCatching { MayraFamilyAccessPolicy.Capability.valueOf(value) }.getOrNull() }
            .toSet()
        return MayraFamilyUserProfile(userId, parts[0], parts[1].toBoolean(), permissions)
    }

    private fun saveCredential(userId: String, password: String) {
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        val salt = Base64.getEncoder().encodeToString(saltBytes)
        val hash = hashPassword(password, salt)
        store.put(credentialKey(userId), salt + ":" + hash)
    }

    private fun loadCredential(userId: String): Credential? {
        val raw = store.get(credentialKey(userId)) ?: return null
        val parts = raw.split(":", limit = 2)
        if (parts.size != 2) return null
        return Credential(parts[0], parts[1])
    }

    private fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest((salt + password).toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(bytes)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        val left = a.toByteArray(Charsets.UTF_8)
        val right = b.toByteArray(Charsets.UTF_8)
        return MessageDigest.isEqual(left, right)
    }

    private fun profileKey(userId: String) = "family.profile.$userId"
    private fun credentialKey(userId: String) = "family.credential.$userId"

    companion object {
        const val MIN_SESSION_TTL_MS = 5 * 60 * 1000L
        const val DEFAULT_SESSION_TTL_MS = 12 * 60 * 60 * 1000L
        const val MAX_SESSION_TTL_MS = 24 * 60 * 60 * 1000L
    }
}
