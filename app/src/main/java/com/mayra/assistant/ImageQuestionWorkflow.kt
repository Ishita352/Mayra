package com.mayra.assistant

/**
 * Image-question workflow foundation.
 * Actual camera/gallery OCR/vision integration is kept behind permission and
 * interview AI-policy gates.
 */
object ImageQuestionWorkflow {
    enum class Stage { CAPTURE_OR_SELECT, READ_QUESTION, UNDERSTAND, SOLVE, VERIFY, SHOW_ANSWER }
    enum class Status { READY, BLOCKED_AI_POLICY, INVALID_IMAGE }

    data class Plan(
        val stages: List<Stage>,
        val status: Status,
        val requiresOwnerApproval: Boolean
    )

    fun plan(imageAvailable: Boolean, permission: InterviewWorkflow.AiPermission): Plan {
        if (permission == InterviewWorkflow.AiPermission.BANNED) {
            return Plan(Stage.values().toList(), Status.BLOCKED_AI_POLICY, true)
        }
        if (!imageAvailable) {
            return Plan(Stage.values().toList(), Status.INVALID_IMAGE, true)
        }
        return Plan(Stage.values().toList(), Status.READY, true)
    }

    fun mayReadImage(permission: InterviewWorkflow.AiPermission): Boolean =
        InterviewWorkflow.mayUseAi(permission)

    fun mayAnswer(permission: InterviewWorkflow.AiPermission): Boolean =
        InterviewWorkflow.mayUseAi(permission)

    fun mayCovertlyCapture(): Boolean = false
    fun mayBypassInterviewRules(): Boolean = false
}
