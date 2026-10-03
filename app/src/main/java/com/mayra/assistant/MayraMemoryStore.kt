package com.mayra.assistant

import android.content.Context

/**
 * Persistent memory storage kept separate from Master ON/OFF state.
 *
 * Master OFF must never clear this store. Future memory features can use it
 * without coupling memory lifetime to the assistant runtime lifecycle.
 */
object MayraMemoryStore {
    private const val PREFS = "mayra_memory"
    private const val SCHEMA_VERSION = "schema_version"

    fun initialize(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(SCHEMA_VERSION, 1)
            .apply()
    }

    fun read(context: Context, key: String): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(key, null)

    fun write(context: Context, key: String, value: String) {
        require(key.isNotBlank())
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(key, value)
            .apply()
    }
}
