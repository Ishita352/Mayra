package com.mayra.assistant

/**
 * Structured knowledge map for Mayra's earning/work workflows.
 * Current platform rules must be refreshed before any real action.
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

    val computerWorkTopics = listOf(
        "Web research and information collection",
        "Remote/freelance platform work",
        "Browser-based job applications with Owner permission",
        "Email/document drafting and organization",
        "PDF/document reading, conversion and preparation",
        "Excel/data entry, cleaning, formulas and analysis",
        "Reports, charts and dashboard preparation",
        "Presentation/document preparation",
        "Textile and digital design workflows",
        "File search and organization",
        "Opening approved Windows applications",
        "Computer status and secure phone-to-Windows command workflows",
        "Future approved browser/file actions after authenticated pairing",
        "Interview preparation and authorized interview assistance",
        "Learning software documentation and tutorials"
    )

    fun summary(): String = buildString {
        append("Mayra Income & Computer Work Knowledge Base\n\n")
        append("Income/work skills:\n")
        topics.forEach { topic -> append("• " + topic.name + ": " + topic.skills.joinToString(", ") + "\n") }
        append("\nComputer work knowledge:\n")
        computerWorkTopics.forEach { item -> append("• " + item + "\n") }
        append("\nWorkflow: Discover → Verify → Match → Risk check → Owner permission → Work → Report → Owner handles payment/withdrawal.")
        append("\n₹0 rule: no upfront cost, paid subscription, paid trial or financial transaction by Mayra.")
        append("\nSecurity: computer control stays allowlisted and authorized; no stealth or unauthorized access.")
    }
}
