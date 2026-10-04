package com.mayra.assistant

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Installation-only bootstrap gate.
 *
 * The password is deterministically derived from the Android package's first
 * install date. It is required only during the initial setup on this phone;
 * completion is persisted locally and no later computer session asks for it.
 */
class MayraInstallationBootstrapManager(private val context: Context) {
    companion object {
        private const val PREFS = "mayra_secure"
        private const val KEY_COMPLETED = "installation_bootstrap_completed"
        private const val PASSWORD_PREFIX = "MAYRA-"
        private const val DATE_PATTERN = "yyyyMMdd"
    }

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isCompleted(): Boolean = prefs.getBoolean(KEY_COMPLETED, false)

    fun installationDateLabel(): String =
        SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).format(Date(installationTimeMs()))

    fun verify(input: String): Boolean =
        input.trim().uppercase(Locale.ROOT) == expectedPassword()

    fun markCompleted() {
        prefs.edit().putBoolean(KEY_COMPLETED, true).apply()
    }

    private fun expectedPassword(): String =
        PASSWORD_PREFIX + SimpleDateFormat(DATE_PATTERN, Locale.ROOT).format(Date(installationTimeMs()))

    private fun installationTimeMs(): Long =
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
        }.getOrDefault(System.currentTimeMillis())
}
