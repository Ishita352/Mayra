package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OneFormaProjectAssistantPolicyTest {
    @Test
    fun humanOnlyModeKeepsOwnerInControl() {
        val mode = OneFormaProjectAssistantPolicy.AssistanceMode.HUMAN_ONLY_ASSISTANCE
        val actions = OneFormaProjectAssistantPolicy.allowedActions(mode)
        assertTrue(actions.contains(OneFormaProjectAssistantPolicy.Action.EXPLAIN_INSTRUCTIONS))
        assertTrue(actions.contains(OneFormaProjectAssistantPolicy.Action.PREPARE_NON_SUBMISSION_NOTES))
        assertFalse(actions.contains(OneFormaProjectAssistantPolicy.Action.SUBMIT_WORK))
        assertFalse(OneFormaProjectAssistantPolicy.maySubmitWork(mode))
    }

    @Test
    fun unknownRulesStayRestricted() {
        val mode = OneFormaProjectAssistantPolicy.modeFor(
            IncomePlatformEligibilityPolicy.PlatformAccess.UNKNOWN_RESTRICTED
        )
        assertTrue(mode == OneFormaProjectAssistantPolicy.AssistanceMode.BLOCKED_PENDING_RULES)
        assertFalse(OneFormaProjectAssistantPolicy.maySubmitWork(mode))
    }

    @Test
    fun allowedModeCanPrepareDraft() {
        val mode = OneFormaProjectAssistantPolicy.AssistanceMode.AI_ASSISTED_WORK
        assertTrue(OneFormaProjectAssistantPolicy.allowedActions(mode)
            .contains(OneFormaProjectAssistantPolicy.Action.PREPARE_ALLOWED_DRAFT))
        assertFalse(OneFormaProjectAssistantPolicy.maySubmitWork(mode))
    }
}
