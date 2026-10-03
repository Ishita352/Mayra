package com.mayra.assistant

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Safe execution boundary for scheduled Mayra checks.
 *
 * Concrete network/source scanners are added separately. Until then this worker
 * performs no privileged action and never handles payments or private accounts.
 */
class MayraBackgroundWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val jobType = inputData.getString("job_type") ?: return Result.failure()
        val prefs = applicationContext.getSharedPreferences("mayra_secure", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("owner_verified", false) || !prefs.getBoolean("master_on", true)) {
            return Result.success()
        }

        return when (jobType) {
            BackgroundSchedulerPolicy.JobType.JOB_WATCHER.name,
            BackgroundSchedulerPolicy.JobType.INCOME_WATCHER.name -> runOpportunityWatch()
            BackgroundSchedulerPolicy.JobType.LEARNING_REVIEW.name,
            BackgroundSchedulerPolicy.JobType.INTERVIEW_REVIEW.name -> Result.success()
            else -> Result.failure()
        }
    }

    private fun runOpportunityWatch(): Result {
        // Public-source adapters will be connected separately. Until then,
        // do not invent opportunities or claim that a live scan occurred.
        return Result.success()
    }

    private fun notifyPublicChange(
        title: String,
        message: String,
        important: Boolean,
        notificationId: Int
    ) {
        MayraNotificationCenter.notifyOwner(
            applicationContext,
            NotificationSchedulePolicy.Event(
                type = NotificationSchedulePolicy.EventType.GENERAL,
                title = title,
                message = message,
                important = important
            ),
            notificationId
        )
    }
}

