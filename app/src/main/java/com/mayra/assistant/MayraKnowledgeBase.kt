package com.mayra.assistant

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.Locale

/**
 * Offline-first document-aware memory for Mayra.
 * Architecture: ingest -> chunk -> retrieve -> answer.
 */
object MayraKnowledgeBase {
    private const val FILE_NAME = "mayra_knowledge_v1.txt"
    private const val MAX_TOTAL_CHARS = 1_500_000
    private const val CHUNK_SIZE = 1400
    private const val OVERLAP = 180

    data class Hit(val source: String, val text: String, val score: Int)

    fun addText(context: Context, source: String, text: String): Int {
        val clean = text.replace("\u0000", " ").trim()
        if (clean.isBlank()) return 0
        val chunks = chunk(clean)
        val file = file(context)
        val current = if (file.exists()) file.readText() else ""
        val room = (MAX_TOTAL_CHARS - current.length).coerceAtLeast(0)
        if (room == 0) return 0
        val accepted = chunks.joinToString("\n") { encode(source, it) }.take(room)
        file.appendText(if (current.isBlank()) accepted else "\n" + accepted)
        return accepted.length
    }

    fun importPdf(context: Context, uri: Uri): DocumentPdfReader.Result {
        val result = DocumentPdfReader.read(context.contentResolver, uri, context)
        if (result.success) addText(context, "PDF", result.text)
        return result
    }

    fun search(context: Context, query: String, limit: Int = 5): List<Hit> {
        val q = tokens(query)
        if (q.isEmpty()) return emptyList()
        return readRecords(context).mapNotNull { record ->
            val source = record.first
            val text = record.second
            val t = tokens(text)
            val score = q.sumOf { token ->
                if (token in t) 3 else if (t.any { word -> word.startsWith(token) }) 1 else 0
            }
            if (score > 0) Hit(source, text, score) else null
        }.sortedByDescending { it.score }.take(limit)
    }

    fun answer(context: Context, query: String): String? {
        val hits = search(context, query)
        if (hits.isEmpty()) return null
        val joined = hits.joinToString("\n\n") { hit -> "[\${hit.source}]\n\${hit.text}" }
        return "আমার সংরক্ষিত knowledge থেকে প্রাসঙ্গিক তথ্য পেলাম:\n\n$joined"
    }

    fun clear(context: Context) {
        file(context).delete()
    }

    private fun chunk(text: String): List<String> {
        val out = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            val end = (start + CHUNK_SIZE).coerceAtMost(text.length)
            out += text.substring(start, end).trim()
            if (end == text.length) break
            start = (end - OVERLAP).coerceAtLeast(start + 1)
        }
        return out.filter { it.isNotBlank() }
    }

    private fun encode(source: String, text: String): String =
        source.replace("|", "/") + "|" + text.replace("\n", " ").replace("\r", " ").trim()

    private fun readRecords(context: Context): List<Pair<String, String>> =
        if (!file(context).exists()) emptyList()
        else file(context).readLines().mapNotNull { line ->
            val split = line.indexOf('|')
            if (split <= 0) null else line.substring(0, split) to line.substring(split + 1)
        }

    private fun file(context: Context): File = File(context.filesDir, FILE_NAME)

    private fun tokens(value: String): Set<String> =
        value.lowercase(Locale.ROOT)
            .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
            .split(" ")
            .filter { it.length >= 2 }
            .toSet()
}
