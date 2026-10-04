package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraSelfDevelopmentCommandTest {
    private fun prefs(): android.content.SharedPreferences =
        androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
            .getSharedPreferences("mayra_self_development_test", android.content.Context.MODE_PRIVATE)

    @Test
    fun commandRequiresOwnerVerification() {
        val p = prefs()
        p.edit().clear().putBoolean("owner_command_authorized", false).apply()
        val result = MayraSelfDevelopmentCommand.request(p, "Improve voice module")
        assertTrue(result.handled)
        assertEquals(MayraSelfUpdateWorkflow.Stage.OWNER_APPROVAL_REQUIRED, result.stage)
        assertFalse(MayraSelfDevelopmentCommand.hasPendingRequest(p))
    }

    @Test
    fun ownerCommandCreatesPendingReviewableProposal() {
        val p = prefs()
        p.edit().clear().putBoolean("owner_command_authorized", true).apply()
        val result = MayraSelfDevelopmentCommand.request(p, "Improve voice module")
        assertTrue(result.handled)
        assertEquals(MayraSelfUpdateWorkflow.Stage.OWNER_APPROVAL_REQUIRED, result.stage)
        assertTrue(MayraSelfDevelopmentCommand.hasPendingRequest(p))
        assertTrue(result.response.contains("tests/CI"))
        MayraSelfDevelopmentCommand.clearPendingRequest(p)
    }
}
