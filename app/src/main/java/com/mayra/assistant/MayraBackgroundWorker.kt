package com.mayra.assistant

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf

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
            !prefs.getBoolean("master_on", true) ||
            DeviceSecurityGate.isDeviceLocked(applicationContext)
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
        // Every future income/work adapter must begin from the same foundational
        // rule gate. Unknown automation permission is never treated as allowed.
        val policy = IncomeWorkRules.evaluate(
            humanOnly = false,
            aiAssistanceAllowed = null,
            automationAllowed = null
        )

        // Keep the worker honest until a live public-source adapter is connected.
        // It may report that no scan was performed, but must never invent a result.
        return Result.success(
            workDataOf(
                "scan_status" to "NO_LIVE_SOURCE_ADAPTER",
                "income_work_mode" to policy.mode.name
            )
        )
    }
}
