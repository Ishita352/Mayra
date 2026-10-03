package com.mayra.assistant

/**
 * Zero-cost earning opportunity pipeline.
 * Discovery is informational; application, work submission and payment remain Owner-controlled.
 */
object JobIncomeWorkflow {
    enum class Stage { FIND, VERIFY, MATCH, COMPARE, RISK_CHECK, NOTIFY_OWNER, OWNER_APPROVAL, PREPARE, TRACK }
    enum class Currency { USD, INR, OTHER }
    enum class WorkType { HOURLY, FIXED_PRICE, COMMISSION, CONTRACT, ACTIVE, SEMI_PASSIVE, PASSIVE }

    data class Opportunity(
        val title: String,
        val source: String,
        val currency: Currency,
        val workType: WorkType,
        val estimatedMinutes: Int?,
        val repeatable: Boolean,
        val upfrontCost: Boolean,
        val verified: Boolean
    )

    fun pipeline(): List<Stage> = Stage.values().toList()

    fun isEligibleForFreeFlow(opportunity: Opportunity): Boolean =
        opportunity.upfrontCost.not() && opportunity.verified

    fun priority(opportunity: Opportunity): Int {
        if (!isEligibleForFreeFlow(opportunity)) return Int.MIN_VALUE
        val currency = when (opportunity.currency) {
            Currency.USD -> "USD"
            Currency.INR -> "INR"
            Currency.OTHER -> "OTHER"
        }
        return IncomeOpportunityPolicy.priority(
            if (opportunity.workType == WorkType.PASSIVE) IncomeOpportunityPolicy.Type.PASSIVE
            else if (opportunity.workType == WorkType.SEMI_PASSIVE) IncomeOpportunityPolicy.Type.SEMI_PASSIVE
            else IncomeOpportunityPolicy.Type.ACTIVE,
            currency,
            opportunity.estimatedMinutes ?: 121,
            opportunity.repeatable
        )
    }

    fun mayraMayExecuteFinancialAction(): Boolean = false
    fun requiresOwnerApproval(): Boolean = true
}
