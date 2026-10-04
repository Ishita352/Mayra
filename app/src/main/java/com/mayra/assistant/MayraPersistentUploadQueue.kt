package com.mayra.assistant

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class MayraPersistentUploadQueue(context: Context) {
    private val p = context.getSharedPreferences("mayra_upload_queue", Context.MODE_PRIVATE)

    data class Item(val itemId: String, val queuedAtMillis: Long, val ownerApproved: Boolean)

    private fun read(): MutableList<Item> {
        val a = JSONArray(p.getString("items", "[]"))
        return MutableList(a.length()) { i ->
            val o = a.getJSONObject(i)
            Item(o.getString("id"), o.getLong("time"), o.getBoolean("approved"))
        }
    }

    private fun write(xs: List<Item>) {
        val a = JSONArray()
        xs.distinctBy { it.itemId }.forEach {
            a.put(JSONObject().put("id", it.itemId).put("time", it.queuedAtMillis).put("approved", it.ownerApproved))
        }
        p.edit().putString("items", a.toString()).apply()
    }

    fun enqueue(id: String, time: Long = System.currentTimeMillis(), approved: Boolean = false) {
        if (id.isNotBlank()) write(read() + Item(id, time, approved))
    }

    fun all(): List<Item> = read()

    fun approve(id: String) {
        write(read().map { if (it.itemId == id) it.copy(ownerApproved = true) else it })
    }

    fun remove(id: String) {
        write(read().filterNot { it.itemId == id })
    }
}
