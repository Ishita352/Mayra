package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProactiveConversationPolicyTest {
    @Test
    fun supportsJokesAndFreshUpdates() {
        assertEquals(
            setOf(
                ProactiveConversationPolicy.PromptType.JOKE,
                ProactiveConversationPolicy.PromptType.FRESH_UPDATE
            ),
            ProactiveConversationPolicy.promptTypes().toSet()
        )
        assertTrue(ProactiveConversationPolicy.mayAskOccasionally(ProactiveConversationPolicy.PromptType.JOKE))
        assertTrue(ProactiveConversationPolicy.mayAskOccasionally(ProactiveConversationPolicy.PromptType.FRESH_UPDATE))
    }

    @Test
    fun ownerStaysInControl() {
        assertTrue(ProactiveConversationPolicy.requiresOwnerChoice())
        assertTrue(!ProactiveConversationPolicy.maySpamRepeatedPrompts())
        assertTrue(!ProactiveConversationPolicy.mayInterruptActiveWork())
        assertTrue(!ProactiveConversationPolicy.mayInventAnUpdate())
    }
}
