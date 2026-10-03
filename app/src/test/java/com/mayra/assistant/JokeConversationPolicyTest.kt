package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JokeConversationPolicyTest {
    @Test
    fun jokeModeIncludesNaturalSharedReaction() {
        assertEquals(
            JokeConversationPolicy.Reaction.LAUGH_WITH_OWNER,
            JokeConversationPolicy.defaultReaction()
        )
        assertTrue(JokeConversationPolicy.mayReactAfterJoke())
    }

    @Test
    fun reactionIsNotForcedOrSpammy() {
        assertTrue(!JokeConversationPolicy.mayForceReaction())
        assertTrue(!JokeConversationPolicy.mayRepeatReactionSpam())
        assertEquals(
            JokeConversationPolicy.Reaction.NONE,
            JokeConversationPolicy.reactionForOwnerChoice(false)
        )
    }
}
