package com.mayra.assistant

/**
 * Connects interview preparation to the Owner's verified skill knowledge.
 * It produces grounded preparation notes, not fabricated experience.
 */
object InterviewOwnerKnowledgeWorkflow {
    data class GroundedPlan(
        val interview: InterviewWorkflow.Plan,
        val relevantSkillIds: Set<String>,
        val groundingStatus: String
    )

    fun prepare(request: String, mode: InterviewWorkflow.Mode = InterviewWorkflow.Mode.PREPARATION): GroundedPlan {
        val plan = InterviewWorkflow.plan(request, mode)
        val skills = OwnerSkillKnowledge.findMatches(request).map { it.id }.toSet()
        return GroundedPlan(
            interview = plan,
            relevantSkillIds = skills,
            groundingStatus = if (skills.isNotEmpty()) "GROUNDED_IN_OWNER_SKILL_KNOWLEDGE" else "NO_MATCHING_OWNER_SKILL_FOUND"
        )
    }

    fun mayInventExperience(): Boolean = false
    fun mayInventQualification(): Boolean = false
}
