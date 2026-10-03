package com.mayra.assistant

/**
 * Connects verified learning plans to existing Owner-facing workflows.
 * A learning plan can suggest skill expansion, but never upgrades a skill to
 * "proven" without Owner verification.
 */
object KnowledgeIntegrationWorkflow {
    data class Integration(
        val topic: String,
        val ownerSkillMatches: Set<String>,
        val learningStages: List<SelfLearningWorkflow.Stage>,
        val canImproveJobMatching: Boolean,
        val canImproveInterviewPreparation: Boolean,
        val requiresOwnerVerification: Boolean
    )

    fun prepare(topic: String): Integration {
        val learning = SelfLearningWorkflow.plan(topic)
        val matches = OwnerSkillKnowledge.findMatches(topic).map { it.id }.toSet()
        return Integration(
            topic = topic.trim(),
            ownerSkillMatches = matches,
            learningStages = learning.stages,
            canImproveJobMatching = learning.recognized,
            canImproveInterviewPreparation = learning.recognized,
            requiresOwnerVerification = true
        )
    }

    fun mayPromoteToProvenSkill(): Boolean = false
}
