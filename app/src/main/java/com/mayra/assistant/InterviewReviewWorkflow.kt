package com.mayra.assistant

/**
 * Background interview review contract.
 * It evaluates saved preparation material, not a live interview screen.
 */
object InterviewReviewWorkflow {
    enum class Result { READY, NEEDS_PRACTICE, HUMAN_ONLY, BLOCKED }

    data class Review(
        val topic: String,
        val result: Result,
        val score: Int,
        val feedback: String,
        val ownerVisible: Boolean
    )

    fun review(topic: String, practiceCompleted: Boolean, rulesAllowAi: Boolean): Review {
        val clean = topic.trim()
        if (clean.isBlank()) return Review("", Result.BLOCKED, 0, "Topic required.", true)
        if (!rulesAllowAi) return Review(clean, Result.HUMAN_ONLY, 0, "Interview rules require Human-Only mode.", true)
        val score = if (practiceCompleted) 100 else 50
        val result = if (practiceCompleted) Result.READY else Result.NEEDS_PRACTICE
        return Review(clean, result, score, if (practiceCompleted) "Preparation reviewed." else "More practice recommended.", true)
    }

    fun mayReviewLiveInterviewScreen(): Boolean = false
    fun mayCovertlyAssist(): Boolean = false
}
