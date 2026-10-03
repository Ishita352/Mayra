package com.mayra.assistant

/**
 * OneForma project-assistance rules.
 *
 * OneForma is not globally AI-banned. Each project/SOW can impose its own
 * restrictions. Mayra must inspect the current project rules before helping.
 */
object OneFormaWorkPolicy {
    enum class Mode {
        AI_ALLOWED,
        AI_ASSISTANCE_LIMITED,
        AI_PROHIBITED_OWNER_ONLY,
        HUMAN_ONLY_OWNER_ONLY,
        UNKNOWN_RESTRICTED
    }

    fun classify(aiRuleText: String?): Mode {
        val text = aiRuleText?.lowercase()?.trim().orEmpty()
        if (text.isEmpty()) return Mode.UNKNOWN_RESTRICTED
        if (listOf("under no circumstances", "do not use", "no llm", "chatgpt", "ai generated content")
            .any(text::contains)
        ) return Mode.AI_PROHIBITED_OWNER_ONLY
        if (listOf("prior approval", "approved tools", "with permission", "ai assistance")
            .any(text::contains)
        ) return Mode.AI_ASSISTANCE_LIMITED
        if (listOf("ai allowed", "ai assistance allowed", "generative ai allowed")
            .any(text::contains)
        ) return Mode.AI_ALLOWED
        if (text.contains("human only") || text.contains("must be completed by you"))
            return Mode.HUMAN_ONLY_OWNER_ONLY
        return Mode.UNKNOWN_RESTRICTED
    }

    fun mayAssistOwnerWithProhibitedProject(): Boolean = true
    fun mayGenerateOrSubmitWorkForProhibitedProject(): Boolean = false
    fun mayBypassProjectRules(): Boolean = false
    fun mayUseUnauthorizedAutomation(): Boolean = false
    fun mayUseVpnOrProxyToEvadeRules(): Boolean = false
    fun requiresOwnerControlForIdentityVerification(): Boolean = true
    fun requiresOwnerControlForNdaAndLegalAgreement(): Boolean = true
    fun requiresRuleCheckBeforeExecution(): Boolean = true
    fun mayTrackPublicJobListings(): Boolean = true
    fun mayPrepareNon-submissionalNotesOrChecklists(): Boolean = true
}