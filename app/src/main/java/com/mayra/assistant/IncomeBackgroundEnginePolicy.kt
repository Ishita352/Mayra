package com.mayra.assistant

/**
 * Background income discovery contract.
 *
 * This policy describes what Mayra may continuously monitor. It does not
 * authorize bypassing platform rules or financial transactions.
 */
object IncomeBackgroundEnginePolicy {
    const val PASSIVE_INCOME_PRIMARY_OBJECTIVE =
        "Generate legitimate passive income opportunities and pursue only rule-compliant, owner-authorized execution."

    enum class IncomeMode {
        PASSIVE_INCOME_DISCOVERY,
        ACTIVE_INCOME_DISCOVERY,
        WORK_OPPORTUNITY_DISCOVERY,
        REMOTE_JOB_DISCOVERY,
        FREELANCING_DISCOVERY,
        SKILL_TO_INCOME_DISCOVERY
    }

    enum class Action {
        SEARCH,
        COLLECT_PUBLIC_OPPORTUNITIES,
        CHECK_PLATFORM_RULES,
        DETECT_AI_RESTRICTIONS,
        CHECK_ELIGIBILITY,
        SCORE_RELEVANCE,
        PREPARE_DRAFT,
        NOTIFY_OWNER,
        EXECUTE_ONLY_WHEN_EXPLICITLY_ALLOWED
    }

    fun backgroundModes(): List<IncomeMode> = IncomeMode.values().toList()

    fun allowedBackgroundActions(): List<Action> = listOf(
        Action.SEARCH,
        Action.COLLECT_PUBLIC_OPPORTUNITIES,
        Action.CHECK_PLATFORM_RULES,
        Action.DETECT_AI_RESTRICTIONS,
        Action.CHECK_ELIGIBILITY,
        Action.SCORE_RELEVANCE,
        Action.PREPARE_DRAFT,
        Action.NOTIFY_OWNER,
        Action.EXECUTE_ONLY_WHEN_EXPLICITLY_ALLOWED
    )

    fun continuousSearchAllowed(): Boolean = true
    fun mayInventIncomeOpportunity(): Boolean = false
    fun mayAssumeAutomationPermission(): Boolean = false
    fun mayBypassHumanOnlyWork(): Boolean = false
    fun mayPerformFinancialAction(): Boolean = false
    fun mayUseStealthExecution(): Boolean = false
}
