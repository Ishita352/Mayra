package com.mayra.assistant

/**
 * Offline-first document workflow foundation.
 *
 * This layer describes safe operations for supported document types without
 * reading, modifying, or exporting user files by itself. File I/O is added
 * later behind Android storage/permission and Owner gates.
 */
object DocumentWorkflow {
    enum class Format { PDF, DOCX, TXT, UNKNOWN }
    enum class Action { READ, CREATE, EDIT, CONVERT, PRESERVE_STRUCTURE, UNKNOWN }

    data class Request(
        val format: Format,
        val actions: Set<Action>,
        val recognized: Boolean,
        val message: String
    )

    fun plan(request: String): Request {
        val text = request.trim().lowercase()
        if (text.isBlank()) return Request(Format.UNKNOWN, emptySet(), false, help())

        val format = when {
            text.contains("pdf") || text.contains("পিডিএফ") -> Format.PDF
            text.contains("docx") || text.contains("word") || text.contains("ডকুমেন্ট") -> Format.DOCX
            text.contains("txt") || text.contains("text file") -> Format.TXT
            else -> Format.UNKNOWN
        }

        val actions = linkedSetOf<Action>()
        if (containsAny(text, "read", "open", "পড়", "দেখ", "read")) actions += Action.READ
        if (containsAny(text, "create", "make", "বান", "তৈরি")) actions += Action.CREATE
        if (containsAny(text, "edit", "modify", "সম্পাদ", "পরিবর্তন")) actions += Action.EDIT
        if (containsAny(text, "convert", "রূপান্তর", "convert")) actions += Action.CONVERT
        if (containsAny(text, "structure", "format", "layout", "গঠন", "ফরম্যাট")) actions += Action.PRESERVE_STRUCTURE

        val recognized = format != Format.UNKNOWN && actions.isNotEmpty()
        val message = when {
            !recognized -> help()
            Action.PRESERVE_STRUCTURE in actions ->
                "Document plan প্রস্তুত, কিন্তু structure-preserving editing/conversion এখনো সক্রিয় নয়; basic text workflow ব্যবহার করতে হবে।"
            else ->
                "Document plan প্রস্তুত: file type ও requested action শনাক্ত হয়েছে; বাস্তব file access permission/Owner gate-এর পেছনে চলবে।"
        }
        return Request(format, actions, recognized, message)
    }

    fun help(): String =
        "Mayra PDF, DOCX ও TXT-এর read/create/edit/convert/structure-preserving workflow প্রস্তুত করতে পারে।"

    private fun containsAny(text: String, vararg terms: String): Boolean =
        terms.any { text.contains(it) }
}
