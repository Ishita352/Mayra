package com.mayra.assistant

/**
 * Android scheduler contract for owner-visible periodic checks.
 */
object BackgroundSchedulerPolicy {
    enum class JobType {
        JOB_WATCHER, INCOME_WATCHER, PASSIVE_INCOME_ENGINE, LEARNING_REVIEW,
        INTERVIEW_REVIEW, GOVERNMENT_UPDATE_WATCH, LOCAL_CIVIC_WATCH, WEATHER_TRAVEL_WATCH
    }

    data class Schedule(
        val jobType: JobType,
        val intervalHours: Int,
        val networkRequired: Boolean,
        val ownerVisible: Boolean,
        val backgroundOnly: Boolean = true
    )

    fun isValid(schedule: Schedule): Boolean =
        schedule.intervalHours in 1..(24 * 7) && schedule.ownerVisible && schedule.backgroundOnly

    fun defaultSchedules(): List<Schedule> = listOf(
        Schedule(JobType.JOB_WATCHER, 12, true, true),
        Schedule(JobType.INCOME_WATCHER, 12, true, true),
        Schedule(JobType.PASSIVE_INCOME_ENGINE, 2, true, true),
        Schedule(JobType.LEARNING_REVIEW, 24, true, true),
        Schedule(JobType.INTERVIEW_REVIEW, 24, false, true),
        Schedule(JobType.GOVERNMENT_UPDATE_WATCH, 12, true, true),
        Schedule(JobType.LOCAL_CIVIC_WATCH, 2, true, true),
        Schedule(JobType.WEATHER_TRAVEL_WATCH, 2, true, true)
    )

    fun mayUseStealthService(): Boolean = false
    fun mayExecuteFinancialAction(): Boolean = false
}