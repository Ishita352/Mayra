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
            BackgroundSchedulerPolicy.JobType.JOB_WATCHER.name -> {
                MayraNotificationCenter.notifyOwner(
                    applicationContext,
                    NotificationSchedulePolicy.Event(
                        NotificationSchedulePolicy.EventType.JOB_OPPORTUNITY,
                        "Mayra Job Watcher",
                        "Background job check is scheduled and ready for verified opportunities.",
                        important = false
                    ),
                    2101
                )
                Result.success()
            }
            BackgroundSchedulerPolicy.JobType.INCOME_WATCHER.name -> {
                MayraNotificationCenter.notifyOwner(
                    applicationContext,
                    NotificationSchedulePolicy.Event(
                        NotificationSchedulePolicy.EventType.INCOME_UPDATE,
                        "Mayra Income Watcher",
                        "Background income check is scheduled. No financial action is performed automatically.",
                        important = false
                    ),
                    2102
                )
                Result.success()
            }
            BackgroundSchedulerPolicy.JobType.LEARNING_REVIEW.name,
            BackgroundSchedulerPolicy.JobType.INTERVIEW_REVIEW.name -> {
                Result.success()
            }
            else -> Result.failure()
        }
    }
}
