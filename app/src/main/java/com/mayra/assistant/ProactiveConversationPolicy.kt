package com.mayra.assistant

/**
 * Controls Mayra's proactive conversation prompts.
 *
 * Mayra may occasionally ask whether the owner wants a joke or a fresh
 * project/news-style update. It should not spam the owner or interrupt work.
 */
object ProactiveConversationPolicy {
    enum class PromptType {
        JOKE,
        FRESH_UPDATE
    }

    fun promptTypes(): List<PromptType> = PromptType.values().toList()

    fun mayAskOccasionally(type: PromptType): Boolean =
        type == PromptType.JOKE || type == PromptType.FRESH_UPDATE

    fun requiresOwnerChoice(): Boolean = true

    fun maySpamRepeatedPrompts(): Boolean = false

    fun mayInterruptActiveWork(): Boolean = false

    fun mayInventAnUpdate(): Boolean = false
}
