package com.mayra.assistant

/**
 * Canonical Founder/Owner identity for Mayra.
 *
 * Mayra stores identity metadata and verification state only.
 * Raw biometric templates, camera images, raw voice recordings, and passwords
 * are never stored by this component.
 */
class MayraFounderIdentity(private val store: Store) {
    interface Store {
        fun get(key: String): String?
        fun put(key: String, value: String)
    }

    enum class VerificationMethod { ANDROID_BIOMETRIC, CONSENTED_VOICE, EXPLICIT_OWNER_CONFIRMATION }

    data class FounderProfile(
        val displayName: String = FOUNDER_NAME,
        val role: String = FOUNDER_ROLE,
        val ownerId: String = OWNER_ID
    )

    data class RecognitionResult(
        val recognized: Boolean,
        val profile: FounderProfile,
        val method: VerificationMethod?
    )

    fun profile(): FounderProfile = FounderProfile()

    /**
     * Marks the Founder as recognized only after an independent trusted
     * verification mechanism has succeeded.
     */
    fun recognizeVerifiedOwner(method: VerificationMethod): RecognitionResult {
        store.put(KEY_LAST_METHOD, method.name)
        store.put(KEY_VERIFIED_AT, System.currentTimeMillis().toString())
        store.put(KEY_VERIFIED, "true")
        return RecognitionResult(true, profile(), method)
    }

    fun isCurrentlyRecognized(): Boolean =
        store.get(KEY_VERIFIED) == "true"

    fun lastVerificationMethod(): VerificationMethod? =
        store.get(KEY_LAST_METHOD)?.let { runCatching { VerificationMethod.valueOf(it) }.getOrNull() }

    fun clearRecognition() {
        store.put(KEY_VERIFIED, "false")
        store.put(KEY_LAST_METHOD, "")
        store.put(KEY_VERIFIED_AT, "")
    }

    companion object {
        const val FOUNDER_NAME = "Gopal Basak"
        const val FOUNDER_ROLE = "Founder / Owner"
        const val OWNER_ID = "owner"
        private const val KEY_VERIFIED = "founder.identity.verified"
        private const val KEY_LAST_METHOD = "founder.identity.last_method"
        private const val KEY_VERIFIED_AT = "founder.identity.verified_at"
    }
}
