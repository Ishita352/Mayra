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

        return when (jobType) {
            BackgroundSchedulerPolicy.JobType.JOB_WATCHER.name,
            BackgroundSchedulerPolicy.JobType.INCOME_WATCHER.name,
            BackgroundSchedulerPolicy.JobType.LEARNING_REVIEW.name,
            BackgroundSchedulerPolicy.JobType.INTERVIEW_REVIEW.name -> {
                // Source-specific scanning/notification work is intentionally
                // added behind its own permission and safety gates.
                Result.success()
            }
            else -> Result.failure()
        }
    }
}
