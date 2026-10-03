package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OneFormaWorkPolicyTest {
    @Test
    fun prohibitedProjectIsOwnerOnly() {
        assertEquals(
            OneFormaWorkPolicy.Mode.AI_PROHIBITED_OWNER_ONLY,
            OneFormaWorkPolicy.classify("Under no circumstances should ChatGPT or any LLM be used.")
        )
        assertTrue(OneFormaWorkPolicy.mayAssistOwnerWithProhibitedProject())
        assertFalse(OneFormaWorkPolicy.mayGenerateOrSubmitWorkForProhibitedProject())
    }

    @Test
    fun approvalRequiredIsLimited() {
        assertEquals(
            OneFormaWorkPolicy.Mode.AI_ASSISTANCE_LIMITED,
            OneFormaWorkPolicy.classify("Use AI only with prior approval.")
        )
    }

    @Test
    fun unknownRulesFailClosed() {
        assertEquals(OneFormaWorkPolicy.Mode.UNKNOWN_RESTRICTED, OneFormaWorkPolicy.classify(null))
        assertFalse(OneFormaWorkPolicy.mayBypassProjectRules())
        assertFalse(OneFormaWorkPolicy.mayUseUnauthorizedAutomation())
        assertFalse(OneFormaWorkPolicy.mayUseVpnOrProxyToEvadeRules())
    }
}