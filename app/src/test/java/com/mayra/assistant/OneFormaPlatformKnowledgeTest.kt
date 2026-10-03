package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OneFormaPlatformKnowledgeTest {
    @Test
    fun liveProjectRulesAlwaysOverrideBaseline() {
        assertFalse(OneFormaPlatformKnowledge.mayUseBaselineKnowledgeWhenLiveRulesMissing())
        assertTrue(OneFormaPlatformKnowledge.liveProjectRulesOverrideBaseline())
        assertTrue(OneFormaPlatformKnowledge.requiresProjectSpecificRuleCheckBeforeWork())
    }

    @Test
    fun sensitiveStepsRemainOwnerControlled() {
        assertTrue(OneFormaPlatformKnowledge.requiresTruthfulProfileAndIdentity())
        assertTrue(OneFormaPlatformKnowledge.requiresOwnerControlForNdaAndIdentity())
        assertFalse(OneFormaPlatformKnowledge.vpnOrProxyBypassAllowed())
        assertFalse(OneFormaPlatformKnowledge.unauthorizedAutomationAllowed())
    }
}
