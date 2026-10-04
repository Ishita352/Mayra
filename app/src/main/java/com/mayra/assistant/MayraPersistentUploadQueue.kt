package com.mayra.assistant

import android.content.Context
import android.content.SharedPreferences
import java.util.Base64

class MayraPersistentUploadQueue(private val p: SharedPreferences) {
    constructor(context: Context) : this(
        context.getSharedPreferences("mayra_upload_queue", Context.MODE_PRIVATE)
    )

    data class Item(val itemId: String, val queuedAtMillis: Long, val ownerApproved: Boolean)

    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    private fun encodeId(id: String): String =
        encoder.encodeToString(id.toByteArray(Charsets.UTF_8))

    private fun decodeId(value: String): String =
        String(decoder.decode(value), Charsets.UTF_8)

    private fun read(): MutableList<Item> {
        val raw = p.getString("items", "") ?: ""
        if (raw.isBlank()) return mutableListOf()
        return raw.lineSequence().mapNotNull { line ->
            val parts = line.split('|', limit = 3)
            if (parts.size != 3) return@mapNotNull null
            runCatching {
                Item(
                    itemId = decodeId(parts[0]),
                    queuedAtMillis = parts[1].toLong(),
                    ownerApproved = parts[2].toBooleanStrict()
                )
            }.getOrNull()
        }.toMutableList()
    }

    private fun write(xs: List<Item>) {
        val raw = xs.distinctBy { it.itemId }.joinToString("\n") {
            "${encodeId(it.itemId)}|${it.queuedAtMillis}|${it.ownerApproved}"
        }
        p.edit().putString("items", raw).apply()
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
