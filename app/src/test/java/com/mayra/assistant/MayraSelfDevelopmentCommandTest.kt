package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Owner-gated self-development regression tests. */
class MayraSelfDevelopmentCommandTest {
    private fun prefs(): TestSharedPreferences = TestSharedPreferences()

    @Test
    fun commandRequiresOwnerVerification() {
        val p = prefs()
        p.edit().putBoolean("owner_command_authorized", false).apply()
        val result = MayraSelfDevelopmentCommand.request(p, "Improve voice module")
        assertTrue(result.handled)
        assertEquals(MayraSelfUpdateWorkflow.Stage.OWNER_APPROVAL_REQUIRED, result.stage)
        assertFalse(MayraSelfDevelopmentCommand.hasPendingRequest(p))
    }

    @Test
    fun ownerCommandCreatesPendingReviewableProposal() {
        val p = prefs()
        p.edit().putBoolean("owner_command_authorized", true).apply()
        val result = MayraSelfDevelopmentCommand.request(p, "Improve voice module")
        assertTrue(result.handled)
        assertEquals(MayraSelfUpdateWorkflow.Stage.OWNER_APPROVAL_REQUIRED, result.stage)
        assertTrue(MayraSelfDevelopmentCommand.hasPendingRequest(p))
        assertTrue(result.response.contains("tests/CI"))
        MayraSelfDevelopmentCommand.clearPendingRequest(p)
    }
}
