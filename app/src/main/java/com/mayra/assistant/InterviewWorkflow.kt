package com.mayra.assistant

/**
 * Interview preparation foundation.
 * It is CV-first and keeps preparation, authorized assistance and human-only
 * interview modes explicitly separate.
 */
object InterviewWorkflow {
    enum class Mode { PREPARATION, AUTHORIZED_ASSISTANCE, HUMAN_ONLY }
    enum class AiPermission { ALLOWED, NOT_PROHIBITED, BANNED }
    enum class Action { REVIEW_CV, PRACTICE_QUESTION, EXPLAIN_QUESTION, PREPARE_ANSWER, PRONUNCIATION, UNKNOWN }

    data class Plan(
        val mode: Mode,
        val actions: Set<Action>,
        val recognized: Boolean,
        val message: String
    )

    fun plan(request: String, mode: Mode = Mode.PREPARATION): Plan {
        val text = request.trim().lowercase()
        if (text.isBlank()) return Plan(mode, emptySet(), false, help())

        val actions = linkedSetOf<Action>()
        if (containsAny(text, "cv", "resume", "সিভি", "রিজিউমে")) actions += Action.REVIEW_CV
        if (containsAny(text, "practice", "প্র্যাকটিস", "প্রশ্ন")) actions += Action.PRACTICE_QUESTION
        if (containsAny(text, "meaning", "মানে", "অর্থ", "explain", "ব্যাখ্যা")) actions += Action.EXPLAIN_QUESTION
        if (containsAny(text, "answer", "উত্তর", "prepare")) actions += Action.PREPARE_ANSWER
        if (containsAny(text, "pronunciation", "উচ্চারণ", "বাংলা হরফ")) actions += Action.PRONUNCIATION

        return Plan(
            mode = mode,
            actions = actions,
            recognized = actions.isNotEmpty(),
            message = if (actions.isNotEmpty()) {
                "Interview plan প্রস্তুত: CV-ভিত্তিক preparation হবে; interview-এর নিয়মে AI assistance নিষিদ্ধ হলে Human-Only mode অনুসরণ করতে হবে।"
            } else help()
        )
    }

    fun mayUseAi(permission: AiPermission): Boolean = permission != AiPermission.BANNED

    fun mayCovertlyAssist(): Boolean = false
    fun mayBypassInterviewRules(): Boolean = false

    fun help(): String =
        "Mayra interview preparation-এ CV review, practice, question meaning, answer preparation ও pronunciation workflow চিনতে পারে।"

    private fun containsAny(text: String, vararg terms: String): Boolean =
        terms.any { text.contains(it) }
}
