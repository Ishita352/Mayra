package com.mayra.assistant

/**
 * Rules and workflow for participating in online tests.
 *
 * Mayra must respect the test provider's stated AI/assistance rules.
 * It may assist with preparation or explicitly authorized assistance,
 * but it must not provide hidden live answers when AI assistance is prohibited.
 */
object OnlineTestPolicy {
    enum class Mode {
        PREPARATION,
        AUTHORIZED_ASSISTANCE,
        HUMAN_ONLY
    }

    fun modeForRuleText(ruleText: String): Mode {
        val text = ruleText.lowercase()
        val prohibited = listOf(
            "no ai", "ai not allowed", "ai prohibited",
            "no artificial intelligence", "without ai",
            "no external assistance", "no assistance"
        )
        val allowed = listOf(
            "ai allowed", "ai assistance allowed",
            "artificial intelligence allowed",
            "open book", "external assistance allowed"
        )
        return when {
            prohibited.any { text.contains(it) } -> Mode.HUMAN_ONLY
            allowed.any { text.contains(it) } -> Mode.AUTHORIZED_ASSISTANCE
            else -> Mode.PREPARATION
        }
    }

    fun requiresOwnerPermission(): Boolean = true

    fun reminderMessage(testName: String): String =
        "বস, কাজ পাওয়ার জন্য \"$testName\" টেস্টটি দিতে হবে। আপনার অনুমতি নিয়ে আপনার সঙ্গে থেকে Mayra টেস্টের প্রস্তুতি ও অনুমোদিত অংশে সাহায্য করবে।"

    fun canProvideLiveAnswer(mode: Mode): Boolean =
        mode == Mode.AUTHORIZED_ASSISTANCE

    fun canReadAndExplainScreen(mode: Mode): Boolean =
        mode != Mode.HUMAN_ONLY

    fun description(mode: Mode): String = when (mode) {
        Mode.PREPARATION ->
            "নিয়ম স্পষ্ট নয়। নিরাপদভাবে preparation/mock-test mode ব্যবহার করুন; live answer assistance চালু হবে না।"
        Mode.AUTHORIZED_ASSISTANCE ->
            "Test provider AI assistance অনুমোদন করেছে। Mayra প্রশ্ন বোঝা, ব্যাখ্যা ও উত্তর প্রস্তুতিতে সহায়তা করতে পারবে।"
        Mode.HUMAN_ONLY ->
            "এই পরীক্ষায় AI/external assistance নিষিদ্ধ। Mayra live answer বা test-taking assistance দেবে না।"
    }
}
