package com.mayra.assistant

/**
 * Android scheduler contract.
 * This is intentionally platform-neutral until the final Android integration phase.
 * It describes safe, periodic work rather than a stealth background service.
 */
object BackgroundSchedulerPolicy {
    enum class JobType { JOB_WATCHER, INCOME_WATCHER, PASSIVE_INCOME_ENGINE, LEARNING_REVIEW, INTERVIEW_REVIEW }

    data class Schedule(
        val jobType: JobType,
        val intervalHours: Int,
        val networkRequired: Boolean,
        val ownerVisible: Boolean,
        val backgroundOnly: Boolean = true
    )

    fun isValid(schedule: Schedule): Boolean =
        schedule.intervalHours >= 1 &&
            schedule.intervalHours <= 24 * 7 &&
            schedule.ownerVisible && schedule.backgroundOnly

    fun defaultSchedules(): List<Schedule> = listOf(
        Schedule(JobType.JOB_WATCHER, 12, true, true),
        Schedule(JobType.INCOME_WATCHER, 12, true, true),
        Schedule(JobType.PASSIVE_INCOME_ENGINE, 12, true, true),
        Schedule(JobType.LEARNING_REVIEW, 24, true, true),
        Schedule(JobType.INTERVIEW_REVIEW, 24, false, true)
    )

    fun mayUseStealthService(): Boolean = false
    fun mayExecuteFinancialAction(): Boolean = false
}
