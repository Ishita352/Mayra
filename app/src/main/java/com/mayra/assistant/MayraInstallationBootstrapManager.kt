package com.mayra.assistant

import android.content.Context

/** Password-free installation bootstrap; identity is handled by the central Owner verification flow. */
class MayraInstallationBootstrapManager(private val context: Context) {
    companion object {
        private const val PREFS = "mayra_secure"
        private const val KEY_COMPLETED = "installation_bootstrap_completed"
    }

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isCompleted(): Boolean = true
    fun installationDateLabel(): String = "Password disabled"
    /** Legacy compatibility only: installation no longer accepts or validates a password. */
    fun verify(input: String): Boolean = false
    fun markCompleted() { prefs.edit().putBoolean(KEY_COMPLETED, true).apply() }
    fun passwordRemoved(): Boolean = true
}
