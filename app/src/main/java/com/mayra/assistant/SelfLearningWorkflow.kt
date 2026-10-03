package com.mayra.assistant

/**
 * Offline-first self-learning planner.
 * Research itself is informational; saving a new trusted knowledge item requires
 * verification and Owner approval.
 */
object SelfLearningWorkflow {
    enum class Stage { DISCOVER, RESEARCH, CROSS_CHECK, PRACTICE, VERIFY, BACKUP, OWNER_APPROVAL, SAVE_KNOWLEDGE, RETEST, NOTIFY_OWNER }

    data class Plan(
        val topic: String,
        val stages: List<Stage>,
        val recognized: Boolean,
        val requiresOwnerApproval: Boolean
    )

    fun plan(topic: String): Plan {
        val clean = topic.trim()
        if (clean.isBlank()) return Plan("", emptyList(), false, true)
        return Plan(
            topic = clean,
            stages = Stage.values().toList(),
            recognized = true,
            requiresOwnerApproval = true
        )
    }

    fun maySaveUnverifiedKnowledge(): Boolean = false
    fun mayChangeSecurityPolicy(): Boolean = false
    fun mayChangeFinancialLock(): Boolean = false
}
