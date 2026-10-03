package com.mayra.assistant

/**
 * End-to-end informational pipeline from Owner profile to opportunity notification.
 * It prepares a notification only; the Owner remains responsible for any application,
 * communication, work submission and payment-related action.
 */
object OpportunityNotificationWorkflow {
    data class Notification(
        val title: String,
        val matchedSkillIds: Set<String>,
        val priorityScore: Int,
        val riskStatus: String,
        val requiresOwnerApproval: Boolean
    )

    fun prepare(candidate: JobWatcherWorkflow.Candidate): Notification? {
        if (!candidate.verified || candidate.upfrontCost) return null
        val match = JobSkillMatchWorkflow.match(candidate)
        val priority = JobWatcherWorkflow.rank(listOf(candidate)).firstOrNull()?.let {
            JobIncomeWorkflow.priority(
                JobIncomeWorkflow.Opportunity(
                    it.title, it.source, it.currency,
                    JobIncomeWorkflow.WorkType.ACTIVE,
                    it.estimatedMinutes, it.repeatable, it.upfrontCost, it.verified
                )
            )
        } ?: Int.MIN_VALUE

        return Notification(
            title = candidate.title,
            matchedSkillIds = match.matchedSkills,
            priorityScore = priority,
            riskStatus = "VERIFIED + ZERO_UPFRONT_COST",
            requiresOwnerApproval = true
        )
    }

    fun maySubmitApplication(): Boolean = false
    fun mayContactEmployer(): Boolean = false
    fun maySpendMoney(): Boolean = false
}
