package com.mayra.assistant

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Safe execution boundary for scheduled Mayra checks.
 *
 * Live source adapters are connected separately. Until then this worker does
 * not invent opportunities or claim that a live scan occurred.
 */
class MayraBackgroundWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val jobType = inputData.getString("job_type") ?: return Result.failure()
        val prefs = applicationContext.getSharedPreferences("mayra_secure", Context.MODE_PRIVATE)

        if (!prefs.getBoolean("owner_verified", false) ||
            !prefs.getBoolean("master_on", true)
        ) {
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
        // Public-source adapters will be connected in the next integration step.
        // No fabricated result or financial action is allowed.
        return Result.success()
    }
}
