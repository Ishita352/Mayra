package com.mayra.assistant

import android.content.SharedPreferences

class TestSharedPreferences : SharedPreferences {
    private val data = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = data.toMutableMap()
    override fun getString(key: String, defValue: String?) = data[key] as? String ?: defValue
    override fun getStringSet(key: String, defValues: Set<String>?) = data[key] as? Set<String> ?: defValues
    override fun getInt(key: String, defValue: Int) = data[key] as? Int ?: defValue
    override fun getLong(key: String, defValue: Long) = data[key] as? Long ?: defValue
    override fun getFloat(key: String, defValue: Float) = data[key] as? Float ?: defValue
    override fun getBoolean(key: String, defValue: Boolean) = data[key] as? Boolean ?: defValue
    override fun contains(key: String) = data.containsKey(key)

    override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private var clearRequested = false
        override fun putString(k: String, v: String?) = this.also { pending[k] = v }
        override fun putStringSet(k: String, v: Set<String>?) = this.also { pending[k] = v }
        override fun putInt(k: String, v: Int) = this.also { pending[k] = v }
        override fun putLong(k: String, v: Long) = this.also { pending[k] = v }
        override fun putFloat(k: String, v: Float) = this.also { pending[k] = v }
        override fun putBoolean(k: String, v: Boolean) = this.also { pending[k] = v }
        override fun remove(k: String) = this.also { pending[k] = null }
        override fun clear() = this.also { clearRequested = true }
        override fun commit(): Boolean {
            if (clearRequested) data.clear()
            pending.forEach { (key, value) ->
                if (value == null) data.remove(key) else data[key] = value
            }
            return true
        }
        override fun apply() { commit() }
    }

    override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener) {}
    override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener) {}
}
