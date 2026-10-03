package com.mayra.assistant

/**
 * Rules for identifying income platforms and adapting to their AI/automation policy.
 * Unknown or unclear AI permission is treated as restricted until verified.
 */
object IncomePlatformEligibilityPolicy {
    enum class PlatformAccess {
        AI_ALLOWED,
        AI_ASSISTANCE_LIMITED,
        AI_BANNED,
        HUMAN_ONLY,
        UNKNOWN_RESTRICTED
    }

    enum class OpportunityType {
        REMOTE_JOB, FREELANCE, PASSIVE_INCOME, ACTIVE_INCOME, SKILL_TO_INCOME
    }

    fun classify(aiPolicyKnown: Boolean, aiAllowed: Boolean, humanOnly: Boolean): PlatformAccess =
        when {
            humanOnly -> PlatformAccess.HUMAN_ONLY
            !aiPolicyKnown -> PlatformAccess.UNKNOWN_RESTRICTED
            !aiAllowed -> PlatformAccess.AI_BANNED
            else -> PlatformAccess.AI_ALLOWED
        }

    fun mayGenerateSubmissionContent(access: PlatformAccess): Boolean =
        access == PlatformAccess.AI_ALLOWED || access == PlatformAccess.AI_ASSISTANCE_LIMITED

    fun mayAssistOwnerForHumanOnly(access: PlatformAccess): Boolean =
        access == PlatformAccess.AI_BANNED ||
            access == PlatformAccess.HUMAN_ONLY ||
            access == PlatformAccess.UNKNOWN_RESTRICTED

    fun shouldSeparateFromAiAssistedOpportunities(access: PlatformAccess): Boolean =
        access == PlatformAccess.AI_BANNED ||
            access == PlatformAccess.HUMAN_ONLY ||
            access == PlatformAccess.UNKNOWN_RESTRICTED

    fun mayBypassRestriction(access: PlatformAccess): Boolean = false
}