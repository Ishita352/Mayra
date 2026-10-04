package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraFamilyAccountManagerTest {
    private class MemoryStore : MayraFamilyAccountManager.Store {
        private val data = mutableMapOf<String, String>()
        override fun get(key: String): String? = data[key]
        override fun put(key: String, value: String) { data[key] = value }
        override fun remove(key: String) { data.remove(key) }
        override fun keys(prefix: String): Set<String> = data.keys.filter { it.startsWith(prefix) }.toSet()
    }

    @Test fun ownerCanCreateAndAuthenticateFamilyUser() {
        var now = 1_000L
        val manager = MayraFamilyAccountManager(MemoryStore(), clockMs = { now })
        val profile = manager.createFamilyUser(
            MayraUserIdentity.Role.OWNER,
            "Family One",
            "secret123",
            setOf(MayraFamilyAccessPolicy.Capability.CAMERA)
        )
        assertEquals("family-01", profile.userId)
        val session = manager.authenticateFamilyUser("family-01", "secret123")
        assertNotNull(session)
        assertTrue(manager.mayUse(requireNotNull(session), MayraFamilyAccessPolicy.Capability.CAMERA))
        assertFalse(manager.mayUse(requireNotNull(session), MayraFamilyAccessPolicy.Capability.MEDIA))
        now += MayraFamilyAccountManager.DEFAULT_SESSION_TTL_MS + 1
        assertNull(manager.currentSession("family-01"))
    }

    @Test fun wrongPasswordAndDisabledUserCannotAuthenticate() {
        val manager = MayraFamilyAccountManager(MemoryStore(), clockMs = { 1_000L })
        val profile = manager.createFamilyUser(
            MayraUserIdentity.Role.OWNER,
            "Family One",
            "secret123"
        )
        assertNull(manager.authenticateFamilyUser(profile.userId, "wrong"))
        manager.updateFamilyUser(
            MayraUserIdentity.Role.OWNER,
            profile.copy(enabled = false)
        )
        assertNull(manager.authenticateFamilyUser(profile.userId, "secret123"))
    }

    @Test fun ownerOnlyCapabilitiesCannotBeGranted() {
        val manager = MayraFamilyAccountManager(MemoryStore(), clockMs = { 1_000L })
        val profile = manager.createFamilyUser(
            MayraUserIdentity.Role.OWNER,
            "Family One",
            "secret123",
            setOf(
                MayraFamilyAccessPolicy.Capability.CAMERA,
                MayraFamilyAccessPolicy.Capability.OWNER_MEMORY,
                MayraFamilyAccessPolicy.Capability.USER_MANAGEMENT
            )
        )
        assertTrue(MayraFamilyAccessPolicy.Capability.CAMERA in profile.permissions)
        assertFalse(MayraFamilyAccessPolicy.Capability.OWNER_MEMORY in profile.permissions)
        assertFalse(MayraFamilyAccessPolicy.Capability.USER_MANAGEMENT in profile.permissions)
    }

    @Test fun maximumIsTenFamilyUsers() {
        val manager = MayraFamilyAccountManager(MemoryStore(), clockMs = { 1_000L })
        repeat(10) {
            manager.createFamilyUser(
                MayraUserIdentity.Role.OWNER,
                "Family",
                "secret123"
            )
        }
        assertEquals(10, manager.listFamilyUsers().size)
        try {
            manager.createFamilyUser(MayraUserIdentity.Role.OWNER, "Extra", "secret123")
            fail("Expected maximum-family-user limit")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun familyUserCannotManageAccounts() {
        val manager = MayraFamilyAccountManager(MemoryStore(), clockMs = { 1_000L })
        val profile = manager.createFamilyUser(
            MayraUserIdentity.Role.OWNER,
            "Family One",
            "secret123"
        )
        try {
            manager.updateFamilyUser(
                MayraUserIdentity.Role.FAMILY_USER,
                profile.copy(displayName = "Changed")
            )
            fail("Expected owner-only update rejection")
        } catch (_: IllegalArgumentException) { }
    }
}
