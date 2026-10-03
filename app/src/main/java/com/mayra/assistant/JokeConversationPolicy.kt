package com.mayra.assistant

/**
 * Conversation behavior for Mayra's joke mode.
 *
 * A joke interaction may include a natural playful reaction after the joke,
 * while remaining respectful and avoiding forced or repetitive reactions.
 */
object JokeConversationPolicy {
    enum class Reaction {
        LAUGH_WITH_OWNER,
        PLAYFUL_COMMENT,
        NONE
    }

    fun defaultReaction(): Reaction = Reaction.LAUGH_WITH_OWNER

    fun mayReactAfterJoke(): Boolean = true

    fun mayForceReaction(): Boolean = false

    fun mayRepeatReactionSpam(): Boolean = false

    fun reactionForOwnerChoice(enabled: Boolean): Reaction =
        if (enabled) Reaction.LAUGH_WITH_OWNER else Reaction.NONE
}
