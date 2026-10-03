package com.mayra.assistant

/**
 * Central policy for work that Mayra may run while the app UI is not visible.
 *
 * Background execution is permission-gated; it is never a mechanism to bypass
 * website rules, device security, owner approval, or financial restrictions.
 */
object BackgroundTaskPolicy {
    enum class TaskType {
        JOB_WATCH,
        INCOME_WATCH,
        LEARNING_REVIEW,
        INTERVIEW_REVIEW,
        DOCUMENT_PROCESSING,
        KNOWLEDGE_REFRESH,
        NOTIFICATION_PREPARATION,
        GOVERNMENT_UPDATE_WATCH
    }

    fun isBackgroundAllowed(task: TaskType): Boolean = when (task) {
        TaskType.JOB_WATCH,
        TaskType.INCOME_WATCH,
        TaskType.LEARNING_REVIEW,
        TaskType.INTERVIEW_REVIEW,
        TaskType.DOCUMENT_PROCESSING,
        TaskType.KNOWLEDGE_REFRESH,
        TaskType.NOTIFICATION_PREPARATION,
        TaskType.GOVERNMENT_UPDATE_WATCH -> true
    }

    fun requiresOwnerApproval(task: TaskType): Boolean = when (task) {
        TaskType.JOB_WATCH,
        TaskType.INCOME_WATCH,
        TaskType.LEARNING_REVIEW,
        TaskType.INTERVIEW_REVIEW,
        TaskType.DOCUMENT_PROCESSING,
        TaskType.KNOWLEDGE_REFRESH,
        TaskType.NOTIFICATION_PREPARATION,
        TaskType.GOVERNMENT_UPDATE_WATCH -> false
    }

    fun mayBypassPlatformRules(): Boolean = false
    fun mayRunWhileDeviceLocked(): Boolean = false
    fun mayPerformFinancialAction(): Boolean = false
    fun maySubmitExternalWorkWithoutOwnerApproval(): Boolean = false
    fun mayUseStealthExecution(): Boolean = false
}
