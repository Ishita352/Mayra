package com.mayra.assistant

import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Test

class MayraActionPermissionPolicyTest {
    @Test fun newTaskNeedsOwnerApproval() {
        assertEquals(
            MayraActionPermissionPolicy.Decision.OWNER_APPROVAL_REQUIRED,
            MayraActionPermissionPolicy.decide(MayraActionPermissionPolicy.ActionType.NEW_TASK, false, false)
        )
    }

    @Test fun approvedAutomationCanRun() {
        assertEquals(
            MayraActionPermissionPolicy.Decision.AUTOMATION_ALLOWED,
            MayraActionPermissionPolicy.decide(MayraActionPermissionPolicy.ActionType.BACKGROUND_AUTOMATION, true, true)
        )
    }

    @Test fun disabledAutomationCannotRunAutomatically() {
        assertEquals(
            MayraActionPermissionPolicy.Decision.OWNER_APPROVAL_REQUIRED,
            MayraActionPermissionPolicy.decide(MayraActionPermissionPolicy.ActionType.BACKGROUND_AUTOMATION, false, false)
        )
    }

    @Test fun voiceCanTurnAutomationOff() {
        val result = MayraActionPermissionPolicy.automationVoiceCommand("মায়রা job automation বন্ধ করো")
        assertNotNull(result)
        assertEquals(MayraActionPermissionPolicy.Automation.JOB_DISCOVERY, result!!.first)
        assertFalse(result.second)
    }

    @Test fun ruleForbidsSelfPermissionAndFinancialActions() {
        val rule = MayraActionPermissionPolicy.rule()
        assertTrue(rule.contains("cannot grant themselves permissions"))
        assertTrue(rule.contains("financial transactions"))
    }
}
