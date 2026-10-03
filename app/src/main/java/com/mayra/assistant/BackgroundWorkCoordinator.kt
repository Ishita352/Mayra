package com.mayra.assistant

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Registers OS-supported background checks.
 *
 * This is scheduling infrastructure only: it does not create a stealth service,
 * access private accounts, submit applications, or perform financial actions.
 */
object BackgroundWorkCoordinator {
    private const val JOB_WATCHER = "mayra_job_watcher"
    private const val INCOME_WATCHER = "mayra_income_watcher"
    private const val PASSIVE_INCOME_ENGINE = "mayra_passive_income_engine"
    private const val LEARNING_REVIEW = "mayra_learning_review"
    private const val INTERVIEW_REVIEW = "mayra_interview_review"

    fun scheduleDefaults(context: Context) {
        BackgroundSchedulerPolicy.defaultSchedules().forEach { schedule ->
            enqueue(context, schedule)
        }
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(JOB_WATCHER)
        WorkManager.getInstance(context).cancelUniqueWork(INCOME_WATCHER)
        WorkManager.getInstance(context).cancelUniqueWork(PASSIVE_INCOME_ENGINE)
        WorkManager.getInstance(context).cancelUniqueWork(LEARNING_REVIEW)
        WorkManager.getInstance(context).cancelUniqueWork(INTERVIEW_REVIEW)
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
        )
            .setConstraints(constraints)
            .setInputData(
                androidx.work.workDataOf("job_type" to schedule.jobType.name)
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            uniqueName(schedule.jobType),
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun uniqueName(jobType: BackgroundSchedulerPolicy.JobType): String =
        when (jobType) {
            BackgroundSchedulerPolicy.JobType.JOB_WATCHER -> JOB_WATCHER
            BackgroundSchedulerPolicy.JobType.INCOME_WATCHER -> INCOME_WATCHER
            BackgroundSchedulerPolicy.JobType.PASSIVE_INCOME_ENGINE -> PASSIVE_INCOME_ENGINE
            BackgroundSchedulerPolicy.JobType.LEARNING_REVIEW -> LEARNING_REVIEW
            BackgroundSchedulerPolicy.JobType.INTERVIEW_REVIEW -> INTERVIEW_REVIEW
        }
}
