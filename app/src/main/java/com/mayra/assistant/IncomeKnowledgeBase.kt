package com.mayra.assistant

/**
 * Curated, offline-safe knowledge map for Mayra's earning/work workflows.
 * This is a foundation; current platform rules must be refreshed before action.
 */
object IncomeKnowledgeBase {
    data class Topic(val name: String, val skills: List<String>)

    val topics = listOf(
        Topic("Freelancing", listOf("profile setup", "proposal writing", "client communication", "project delivery")),
        Topic("Remote work", listOf("work-from-home roles", "short contracts", "hourly work", "repeatable work")),
        Topic("Microtasks & surveys", listOf("task eligibility", "reward rules", "minimum withdrawal", "availability checks")),
        Topic("Excel & data services", listOf("data entry", "cleaning", "formulas", "reports", "dashboards")),
        Topic("Translation & transcription", listOf("translation", "speech-to-text", "subtitles", "quality checking")),
        Topic("Design & textile services", listOf("digital assets", "saree/textile design", "templates", "licensing")),
        Topic("Digital products", listOf("templates", "spreadsheets", "design assets", "educational materials")),
        Topic("Passive / semi-passive", listOf("licensing", "royalties", "reusable products", "content-based models")),
        Topic("AI-assisted services", listOf("research", "drafting", "analysis", "automation only where permitted"))
    )

    fun summary(): String = buildString {
        append("Mayra Income Knowledge Base\n\n")
        append("জানা/শেখার ক্ষেত্র:\n")
        topics.forEach { topic -> append("• " + topic.name + ": " + topic.skills.joinToString(", ") + "\n") }
        append("\nপ্রতিটি নতুন platform-এর current Terms, payout rules, eligibility এবং AI/automation policy যাচাই করতে হবে.")
        append("\n\nWorkflow: Discover → Verify → Match → Risk check → Owner permission → Work → Report → Owner handles payment/withdrawal.")
        append("\n\n₹0 rule: upfront cost, paid subscription, paid trial বা financial transaction Mayra করবে না.")
    }
}
