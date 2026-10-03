package com.mayra.assistant

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object BackgroundWorkCoordinator {
    private const val JOB_WATCHER = "mayra_job_watcher"
    private const val INCOME_WATCHER = "mayra_income_watcher"
    private const val PASSIVE_INCOME_ENGINE = "mayra_passive_income_engine"
    private const val LEARNING_REVIEW = "mayra_learning_review"
    private const val INTERVIEW_REVIEW = "mayra_interview_review"
    private const val ONEFORMA_PROJECT_WATCH = "mayra_oneforma_project_watch"
    private const val GOVERNMENT_UPDATE_WATCH = "mayra_government_update_watch"
    private const val LOCAL_CIVIC_WATCH = "mayra_local_civic_watch"
    private const val WEATHER_TRAVEL_WATCH = "mayra_weather_travel_watch"

    fun scheduleDefaults(context: Context) {
        BackgroundSchedulerPolicy.defaultSchedules().forEach { enqueue(context, it) }
    }

    fun cancelAll(context: Context) {
        val wm = WorkManager.getInstance(context)
        listOf(
            JOB_WATCHER, INCOME_WATCHER, PASSIVE_INCOME_ENGINE, LEARNING_REVIEW,
            INTERVIEW_REVIEW, ONEFORMA_PROJECT_WATCH, GOVERNMENT_UPDATE_WATCH, LOCAL_CIVIC_WATCH, WEATHER_TRAVEL_WATCH
        ).forEach(wm::cancelUniqueWork)
    }

    private fun enqueue(context: Context, schedule: BackgroundSchedulerPolicy.Schedule) {
        if (!BackgroundSchedulerPolicy.isValid(schedule)) return
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (schedule.networkRequired) NetworkType.CONNECTED else NetworkType.NOT_REQUIRED
            )
            .build()
        val request = PeriodicWorkRequestBuilder<MayraBackgroundWorker>(
            schedule.intervalHours.toLong(), TimeUnit.HOURS
        ).setConstraints(constraints)
            .setInputData(androidx.work.workDataOf("job_type" to schedule.jobType.name))
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            uniqueName(schedule.jobType), ExistingPeriodicWorkPolicy.UPDATE, request
        )
    }

    private fun uniqueName(jobType: BackgroundSchedulerPolicy.JobType): String = when (jobType) {
        BackgroundSchedulerPolicy.JobType.JOB_WATCHER -> JOB_WATCHER
        BackgroundSchedulerPolicy.JobType.INCOME_WATCHER -> INCOME_WATCHER
        BackgroundSchedulerPolicy.JobType.PASSIVE_INCOME_ENGINE -> PASSIVE_INCOME_ENGINE
        BackgroundSchedulerPolicy.JobType.LEARNING_REVIEW -> LEARNING_REVIEW
        BackgroundSchedulerPolicy.JobType.INTERVIEW_REVIEW -> INTERVIEW_REVIEW
        BackgroundSchedulerPolicy.JobType.ONEFORMA_PROJECT_WATCH -> ONEFORMA_PROJECT_WATCH
        BackgroundSchedulerPolicy.JobType.GOVERNMENT_UPDATE_WATCH -> GOVERNMENT_UPDATE_WATCH
        BackgroundSchedulerPolicy.JobType.LOCAL_CIVIC_WATCH -> LOCAL_CIVIC_WATCH
        BackgroundSchedulerPolicy.JobType.WEATHER_TRAVEL_WATCH -> WEATHER_TRAVEL_WATCH
    }
}