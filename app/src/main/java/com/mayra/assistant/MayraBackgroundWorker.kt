package com.mayra.assistant

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf

class MayraBackgroundWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): ListenableWorker.Result {
        val jobType = inputData.getString("job_type") ?: return ListenableWorker.Result.failure()
        val prefs = applicationContext.getSharedPreferences("mayra_secure", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("owner_verified", false) ||
            !prefs.getBoolean("master_on", true) ||
            DeviceSecurityGate.isDeviceLocked(applicationContext)
        ) return ListenableWorker.Result.success()

        return when (jobType) {
            BackgroundSchedulerPolicy.JobType.JOB_WATCHER.name,
            BackgroundSchedulerPolicy.JobType.INCOME_WATCHER.name,
            BackgroundSchedulerPolicy.JobType.PASSIVE_INCOME_ENGINE.name -> runIncomeBackgroundCycle()

            BackgroundSchedulerPolicy.JobType.LEARNING_REVIEW.name,
            BackgroundSchedulerPolicy.JobType.INTERVIEW_REVIEW.name -> ListenableWorker.Result.success()

            BackgroundSchedulerPolicy.JobType.GOVERNMENT_UPDATE_WATCH.name,
            BackgroundSchedulerPolicy.JobType.LOCAL_CIVIC_WATCH.name,
            BackgroundSchedulerPolicy.JobType.WEATHER_TRAVEL_WATCH.name ->
                runInformationRefreshCycle(jobType)

            BackgroundTaskPolicy.TaskType.DOCUMENT_PROCESSING.name,
            BackgroundTaskPolicy.TaskType.KNOWLEDGE_REFRESH.name,
            BackgroundTaskPolicy.TaskType.NOTIFICATION_PREPARATION.name,
            BackgroundTaskPolicy.TaskType.GOVERNMENT_UPDATE_WATCH.name -> runApprovedBackgroundTask(jobType)

            else -> ListenableWorker.Result.failure()
        }
    }

    private fun runApprovedBackgroundTask(jobType: String): ListenableWorker.Result {
        val task = runCatching { BackgroundTaskPolicy.TaskType.valueOf(jobType) }.getOrNull()
            ?: return ListenableWorker.Result.failure()
        if (!BackgroundTaskPolicy.isBackgroundAllowed(task)) return ListenableWorker.Result.failure()
        return ListenableWorker.Result.success(
            workDataOf("background_task" to task.name, "status" to "ALLOWED_PENDING_TASK_ADAPTER")
        )
    }

    private fun runInformationRefreshCycle(jobType: String): ListenableWorker.Result =
        ListenableWorker.Result.success(
            workDataOf(
                "information_refresh" to jobType,
                "live_source_adapter" to "PENDING",
                "verified_source_required" to true,
                "invented_results_allowed" to false,
                "owner_notification_allowed" to true
            )
        )

    private fun runIncomeBackgroundCycle(): ListenableWorker.Result =
        ListenableWorker.Result.success(
            workDataOf(
                "income_background" to "ACTIVE",
                "passive_income_objective" to IncomeBackgroundEnginePolicy.PASSIVE_INCOME_PRIMARY_OBJECTIVE,
                "continuous_search_allowed" to IncomeBackgroundEnginePolicy.continuousSearchAllowed(),
                "automation_permission_required" to true,
                "execution_status" to "RULE_CHECK_REQUIRED"
            )
        )
}