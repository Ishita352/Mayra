package com.mayra.assistant

/**
 * Matches a job watcher candidate against the Owner's verified skill knowledge.
 * Matching is descriptive only; it does not invent qualifications.
 */
object JobSkillMatchWorkflow {
    data class Match(
        val candidate: JobWatcherWorkflow.Candidate,
        val matchedSkills: Set<String>,
        val matchCount: Int
    )

    fun match(candidate: JobWatcherWorkflow.Candidate): Match {
        val known = OwnerSkillKnowledge.skills
        val normalized = candidate.matchedSkills.map { it.lowercase() }.toSet()
        val matched = known
            .filter { skill -> skill.keywords.any { normalized.contains(it.lowercase()) } }
            .map { it.id }
            .toSet()
        return Match(candidate, matched, matched.size)
    }

    fun rank(candidates: List<JobWatcherWorkflow.Candidate>): List<Match> =
        candidates
            .filter { it.verified && !it.upfrontCost }
            .map(::match)
            .sortedWith(compareByDescending<Match> { it.matchCount }
                .thenByDescending { JobWatcherWorkflow.rank(listOf(it.candidate)).firstOrNull()?.let { c ->
                    JobIncomeWorkflow.priority(
                        JobIncomeWorkflow.Opportunity(
                            c.title, c.source, c.currency,
                            JobIncomeWorkflow.WorkType.ACTIVE,
                            c.estimatedMinutes, c.repeatable, c.upfrontCost, c.verified
                        )
                    )
                } ?: Int.MIN_VALUE })

    fun requiresOwnerApproval(): Boolean = true
}
