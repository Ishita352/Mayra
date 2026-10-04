package com.mayra.assistant

import java.util.Locale

/**
 * Local semantic memory and resume engine.
 *
 * This is model-agnostic: it stores compact memory entries and ranks relevant
 * entries using normalized token overlap. A future AI model can consume the
 * ranked context without changing the persistence contract.
 */
class MayraSemanticMemory(private val store: Store) {
    data class MemoryEntry(
        val id: String,
        val text: String,
        val tags: Set<String> = emptySet(),
        val timestampMs: Long
    )

    data class ResumeState(
        val taskId: String,
        val title: String,
        val context: String,
        val timestampMs: Long
    )

    class SharedPreferencesStore(private val context: android.content.Context) : Store {
        override fun read(key: String): String? = MayraMemoryStore.read(context, key)
        override fun write(key: String, value: String) = MayraMemoryStore.write(context, key, value)
    }

    interface Store {
        fun read(key: String): String?
        fun write(key: String, value: String)
    }

    fun remember(id: String, text: String, tags: Set<String> = emptySet(), timestampMs: Long = System.currentTimeMillis()) {
        require(id.isNotBlank())
        require(text.isNotBlank())
        val safeTags = tags.filter { it.isNotBlank() }.map { normalize(it) }.toSet()
        val entry = MemoryEntry(id, text.trim(), safeTags, timestampMs)
        store.write(memoryKey(id), encode(entry))
    }

    fun recall(query: String, limit: Int = 5): List<MemoryEntry> {
        if (query.isBlank() || limit <= 0) return emptyList()
        val queryTokens = tokens(query)
        return listIds().mapNotNull { id -> decode(store.read(memoryKey(id))) }
            .map { it to score(it, queryTokens) }
            .filter { it.second > 0 }
            .sortedWith(compareByDescending<Pair<MemoryEntry, Int>> { it.second }.thenByDescending { it.first.timestampMs })
            .take(limit)
            .map { it.first }
    }

    fun saveResume(taskId: String, title: String, context: String, timestampMs: Long = System.currentTimeMillis()) {
        require(taskId.isNotBlank())
        require(title.isNotBlank())
        require(context.isNotBlank())
        store.write(RESUME_KEY, listOf(taskId, title, context, timestampMs).joinToString(FIELD_SEPARATOR))
    }

    fun resume(): ResumeState? {
        val raw = store.read(RESUME_KEY) ?: return null
        val parts = raw.split(FIELD_SEPARATOR)
        if (parts.size != 4) return null
        return parts.getOrNull(3)?.toLongOrNull()?.let {
            ResumeState(parts[0], parts[1], parts[2], it)
        }
    }

    fun buildContext(query: String, limit: Int = 5): String =
        recall(query, limit).joinToString("\n") { "[${it.id}] ${it.text}" }

    private fun score(entry: MemoryEntry, queryTokens: Set<String>): Int {
        val textScore = tokens(entry.text).count { it in queryTokens } * 2
        val tagScore = entry.tags.count { it in queryTokens } * 3
        return textScore + tagScore
    }

    private fun listIds(): List<String> =
        store.read(INDEX_KEY)?.split(INDEX_SEPARATOR)?.filter { it.isNotBlank() } ?: emptyList()

    private fun encode(entry: MemoryEntry): String {
        val encoded = listOf(entry.id, entry.text, entry.tags.joinToString(TAG_SEPARATOR), entry.timestampMs)
            .joinToString(FIELD_SEPARATOR)
        val ids = listIds().toMutableList()
        if (entry.id !in ids) ids += entry.id
        store.write(INDEX_KEY, ids.joinToString(INDEX_SEPARATOR))
        return encoded
    }

    private fun decode(raw: String?): MemoryEntry? {
        val parts = raw?.split(FIELD_SEPARATOR) ?: return null
        if (parts.size != 4) return null
        val tags = parts[2].split(TAG_SEPARATOR).filter { it.isNotBlank() }.toSet()
        val timestamp = parts[3].toLongOrNull() ?: return null
        return MemoryEntry(parts[0], parts[1], tags, timestamp)
    }

    private fun memoryKey(id: String) = "memory.$id"

    private fun tokens(value: String): Set<String> =
        value.lowercase(Locale.ROOT)
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.length >= 2 }
            .toSet()

    private fun normalize(value: String) = value.trim().lowercase(Locale.ROOT)

    companion object {
        private const val INDEX_KEY = "memory.index"
        private const val RESUME_KEY = "resume.current"
        private const val INDEX_SEPARATOR = "\u001e"
        private const val FIELD_SEPARATOR = "\u001f"
        private const val TAG_SEPARATOR = "\u001d"
    }
}
