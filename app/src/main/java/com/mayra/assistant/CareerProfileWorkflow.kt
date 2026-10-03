package com.mayra.assistant

/**
 * Owner-CV-first career profile planner.
 *
 * It stores no new factual credentials and does not invent experience.
 * External applications remain Owner-controlled.
 */
object CareerProfileWorkflow {
    enum class Action {
        VIEW_PROFILE, BUILD_CV, UPDATE_CV, MATCH_JOB, IDENTIFY_SKILL_GAP, UNKNOWN
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
        if (containsAny(text, "cv", "resume", "সিভি", "রিজিউমে")) {
            actions += Action.VIEW_PROFILE
            if (containsAny(text, "make", "create", "বান", "তৈরি")) actions += Action.BUILD_CV
            if (containsAny(text, "update", "edit", "আপডেট", "পরিবর্তন")) actions += Action.UPDATE_CV
        }
        if (containsAny(text, "job", "freelance", "চাকরি", "কাজ")) actions += Action.MATCH_JOB
        if (containsAny(text, "skill gap", "missing skill", "দক্ষতার ঘাটতি", "স্কিল গ্যাপ")) actions += Action.IDENTIFY_SKILL_GAP

        return Plan(
            actions = actions,
            recognized = actions.isNotEmpty(),
            message = if (actions.isNotEmpty()) {
                "Career plan প্রস্তুত: Owner-এর বাস্তব CV তথ্য ব্যবহার করা হবে; নতুন অভিজ্ঞতা/যোগ্যতা বানানো হবে না এবং final application Owner-controlled থাকবে।"
            } else help()
        )
    }

    fun help(): String =
        "Mayra CV/Biodata workflow-এ profile, CV তৈরি/আপডেট, job matching ও skill-gap শনাক্ত করতে পারে।"

    private fun containsAny(text: String, vararg terms: String): Boolean =
        terms.any { text.contains(it) }
}
