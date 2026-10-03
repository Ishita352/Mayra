package com.mayra.assistant

/**
 * Foundational rules for Mayra's income-work assistance.
 *
 * These rules are deliberately deny-by-default:
 * - Mayra never bypasses human-only/anti-automation rules.
 * - If AI assistance is prohibited, Mayra stops at explanation/support.
 * - Financial actions remain permanently locked.
 * - Owner approval is required before external work is submitted.
 *
 * This policy is platform-agnostic. A future website adapter must supply the
 * site's current, verifiable rules instead of assuming automation is allowed.
 */
object IncomeWorkRules {
    enum class WorkMode {
        AUTOMATION_ALLOWED,
        HUMAN_ASSISTED,
        BLOCKED_UNVERIFIED
    }

    data class RuleDecision(
        val mode: WorkMode,
        val reason: String
    )

    fun evaluate(
        humanOnly: Boolean,
        aiAssistanceAllowed: Boolean?,
        automationAllowed: Boolean?
    ): RuleDecision {
        if (humanOnly) {
            return if (aiAssistanceAllowed == true) {
                RuleDecision(
                    WorkMode.HUMAN_ASSISTED,
                    "Website requires human completion; Mayra may assist only within its stated AI-assistance rules."
                )
            } else {
                RuleDecision(
                    WorkMode.BLOCKED_UNVERIFIED,
                    "Human-only work: Mayra must not answer, submit, or bypass the platform restriction."
                )
            }
        }

        if (automationAllowed == true) {
            return RuleDecision(
                WorkMode.AUTOMATION_ALLOWED,
                "Automation is explicitly permitted by the current task/platform rules."
            )
        }

        return RuleDecision(
            WorkMode.BLOCKED_UNVERIFIED,
            "Automation permission is not verified; Mayra must not assume it is allowed."
        )
    }

    fun financialActionAllowed(): Boolean = false
    fun bypassAllowed(): Boolean = false
    fun externalSubmissionAllowedWithoutOwnerApproval(): Boolean = false
}
