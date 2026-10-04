package com.mayra.assistant

/**
 * Job Watcher planning layer.
 * The scheduler may refresh public/authorized sources using OS-supported background work.
 * It never auto-applies, pays, messages employers, or bypasses access controls.
 */
object JobWatcherWorkflow {
    data class ScanPolicy(
        val enabled: Boolean,
        val maxResults: Int = 25,
        val refreshHours: Int = 2
    )

    data class Candidate(
        val title: String,
        val source: String,
        val currency: JobIncomeWorkflow.Currency,
        val verified: Boolean,
        val upfrontCost: Boolean,
        val estimatedMinutes: Int?,
        val repeatable: Boolean,
        val matchedSkills: Set<String>
    )

    fun shouldNotify(candidate: Candidate, policy: ScanPolicy): Boolean =
        policy.enabled &&
            policy.maxResults > 0 &&
            candidate.verified &&
            !candidate.upfrontCost

    fun rank(candidates: List<Candidate>): List<Candidate> =
        candidates
            .filter { it.verified && !it.upfrontCost }
            .sortedByDescending {
                JobIncomeWorkflow.priority(
                    JobIncomeWorkflow.Opportunity(
                        it.title, it.source, it.currency,
                        JobIncomeWorkflow.WorkType.ACTIVE,
                        it.estimatedMinutes, it.repeatable,
                        it.upfrontCost, it.verified
                    )
                )
            }

    fun mayAutoApply(): Boolean = false
    fun maySendEmployerMessage(): Boolean = false
    fun mayMakePayment(): Boolean = false
}
