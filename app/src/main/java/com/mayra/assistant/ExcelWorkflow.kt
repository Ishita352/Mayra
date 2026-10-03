package com.mayra.assistant

/**
 * Offline-first Excel/data-analysis workflow planner.
 *
 * It only classifies requested spreadsheet operations. Actual workbook I/O
 * will be added later behind file access and Owner/permission gates.
 */
object ExcelWorkflow {
    enum class Action {
        DATA_ENTRY, CLEANING, FORMULA, LOOKUP, SORT_FILTER,
        PIVOT, CHART, DASHBOARD, VALIDATION, ANALYSIS, UNKNOWN
    }

    data class Plan(
        val actions: Set<Action>,
        val recognized: Boolean,
        val message: String
    )

    fun plan(request: String): Plan {
        val text = request.trim().lowercase()
        if (text.isBlank()) return Plan(emptySet(), false, help())

        val actions = linkedSetOf<Action>()
        if (containsAny(text, "data entry", "data", "ডাটা", "ডেটা")) actions += Action.DATA_ENTRY
        if (containsAny(text, "clean", "cleaning", "duplicate", "পরিষ্কার", "ডুপ্লিকেট")) actions += Action.CLEANING
        if (containsAny(text, "formula", "sum", "average", "if", "ফর্মুলা")) actions += Action.FORMULA
        if (containsAny(text, "lookup", "vlookup", "xlookup", "index match", "লুকআপ")) actions += Action.LOOKUP
        if (containsAny(text, "sort", "filter", "সোর্ট", "ফিল্টার")) actions += Action.SORT_FILTER
        if (containsAny(text, "pivot", "পিভট")) actions += Action.PIVOT
        if (containsAny(text, "chart", "graph", "চার্ট", "গ্রাফ")) actions += Action.CHART
        if (containsAny(text, "dashboard", "ড্যাশবোর্ড")) actions += Action.DASHBOARD
        if (containsAny(text, "validation", "data validation", "ভ্যালিডেশন")) actions += Action.VALIDATION
        if (containsAny(text, "analysis", "analyze", "বিশ্লেষণ", "অ্যানালাইসিস")) actions += Action.ANALYSIS

        return Plan(
            actions = actions,
            recognized = actions.isNotEmpty(),
            message = if (actions.isNotEmpty()) {
                "Excel plan প্রস্তুত: requested spreadsheet operation শনাক্ত হয়েছে; বাস্তব workbook access পরে permission/Owner gate-এর পেছনে চলবে।"
            } else help()
        )
    }

    fun help(): String =
        "Mayra Excel/data workflow-এ data entry, cleaning, formulas, lookup, sort/filter, Pivot, charts, dashboards, validation ও analysis চিনতে পারে।"

    private fun containsAny(text: String, vararg terms: String): Boolean =
        terms.any { text.contains(it) }
}
