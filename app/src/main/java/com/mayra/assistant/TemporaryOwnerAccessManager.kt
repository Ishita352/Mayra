package com.mayra.assistant

import android.content.Context

data class OwnerAuditEvent(
    val timestamp: Long,
    val action: String,
    val status: String,
    val details: String
)

object TemporaryOwnerAccessManager {
    private const val PREFS = "mayra_secure"
    private const val KEY_ACTIVE = "temporary_owner_active"
    private const val KEY_STARTED = "temporary_owner_started_at"
    private const val KEY_EXPIRES = "temporary_owner_expires_at"
    private const val KEY_LAST_CHECK = "temporary_owner_last_checked_at"
    private const val KEY_AUDIT = "temporary_owner_audit"

    const val MAX_DURATION_MS = 24L * 60L * 60L * 1000L

    fun start(context: Context): Boolean {
        val now = System.currentTimeMillis()
        val expires = now + MAX_DURATION_MS
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_ACTIVE, true)
            .putLong(KEY_STARTED, now)
            .putLong(KEY_EXPIRES, expires)
            .putLong(KEY_LAST_CHECK, now)
            .apply()
        appendAudit(context, "TEMP_OWNER_SESSION", "STARTED", "Temporary Owner Mode started; maximum duration 24 hours.")
        return true
    }

    fun isActive(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val active = prefs.getBoolean(KEY_ACTIVE, false)
        val expires = prefs.getLong(KEY_EXPIRES, 0L)
        val lastCheck = prefs.getLong(KEY_LAST_CHECK, 0L)
        if (!active) return false
        if (System.currentTimeMillis() < lastCheck || expires <= lastCheck || expires - lastCheck > MAX_DURATION_MS) {
            expire(context)
            return false
        }
        if (System.currentTimeMillis() >= expires) {
            expire(context)
            return false
        }
        prefs.edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply()
        return true
    }

    fun remainingMillis(context: Context): Long {
        if (!isActive(context)) return 0L
        val expires = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_EXPIRES, 0L)
            .coerceAtLeast(0L)
        return (expires - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    fun expire(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ACTIVE, false)
            .apply()
        appendAudit(context, "TEMP_OWNER_SESSION", "EXPIRED", "Temporary Owner Mode ended or reached the 24-hour limit.")
    }

    fun end(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ACTIVE, false)
            .apply()
        appendAudit(context, "TEMP_OWNER_SESSION", "ENDED", "Temporary Owner Mode ended by command.")
    }

    fun record(context: Context, action: String, status: String, details: String) {
        appendAudit(context, action, status, details)
    }

    fun audit(context: Context): List<OwnerAuditEvent> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_AUDIT, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.lines().mapNotNull { line ->
            val parts = line.split("|", limit = 4)
            if (parts.size == 4) {
                OwnerAuditEvent(
                    parts[0].toLongOrNull() ?: return@mapNotNull null,
                    parts[1],
                    parts[2],
                    parts[3]
                )
            } else null
        }
    }

    private fun appendAudit(context: Context, action: String, status: String, details: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val old = prefs.getString(KEY_AUDIT, "") ?: ""
        val safeDetails = details.replace("|", "/").replace("\n", " ")
        val line = "${System.currentTimeMillis()}|$action|$status|$safeDetails"
        val lines = (if (old.isBlank()) emptyList() else old.lines()) + line
        prefs.edit().putString(KEY_AUDIT, lines.takeLast(500).joinToString("\n")).apply()
    }
}
