package com.mayra.assistant

/**
 * Central catalog for the 37 active follow-up capabilities requested for Mayra.
 *
 * This is an integration contract, not a claim that every capability is finished.
 * A capability is only marked COMPLETE after its implementation and CI verification.
 */
object MayraCapabilityRegistry {
    enum class Status { COMPLETE, IN_PROGRESS, PLANNED }

    data class Capability(
        val id: Int,
        val title: String,
        val status: Status
    )

    private val capabilities = listOf(
        Capability(4, "Security and permission integration", Status.COMPLETE),
        Capability(5, "Full Documents and Excel integration", Status.COMPLETE),
        Capability(6, "CV/Biodata/Career Profile completion", Status.COMPLETE),
        Capability(7, "Job/freelance and active/passive income workflows", Status.COMPLETE),
        Capability(8, "Interview Assistant completion", Status.COMPLETE),
        Capability(9, "Self-learning, knowledge base and verified skill mastery", Status.COMPLETE),
        Capability(10, "Notifications, reminders and background scheduler", Status.COMPLETE),
        Capability(11, "Online Test workflow completion", Status.COMPLETE),
        Capability(12, "Textile Design workflow completion", Status.COMPLETE),
        Capability(13, "WhatsApp/Telegram/Facebook intelligence", Status.COMPLETE),
        Capability(14, "Media verification, moods and conversation intelligence", Status.COMPLETE),
        Capability(15, "Optional 3D character and Voice Light", Status.COMPLETE),
        Capability(16, "Local Emergency/News/Government Services", Status.COMPLETE),
        Capability(17, "Travel Planner and destination information", Status.COMPLETE),
        Capability(18, "Transport schedules and ticket read-only information", Status.COMPLETE),
        Capability(19, "Accommodation and food price/availability research", Status.COMPLETE),
        Capability(20, "Bengali Hindu religious calendar/Puja knowledge", Status.COMPLETE),
        Capability(21, "Satellite/map visual context", Status.COMPLETE),
        Capability(22, "Maps/app/data research and micro-earning intelligence", Status.COMPLETE),
        Capability(23, "Owner CV skill mastery and CV-first job matching", Status.COMPLETE),
        Capability(24, "Android full integration", Status.COMPLETE),
        Capability(25, "Android real-device and security testing", Status.COMPLETE),
        Capability(26, "Final Android APK build and verification", Status.COMPLETE),
        Capability(27, "Windows 10 secure pairing", Status.COMPLETE),
        Capability(28, "Windows 10 computer-control expansion", Status.PLANNED),
        Capability(29, "Cross-device workflows", Status.PLANNED),
        Capability(30, "Windows 10 full testing", Status.PLANNED),
        Capability(31, "Master documentation, backup and release package", Status.PLANNED),
        Capability(32, "Final project certificate", Status.PLANNED),
        Capability(33, "Public Mayra website and GitHub Pages/custom domain", Status.PLANNED),
        Capability(34, "Website and Mayra integration", Status.PLANNED),
        Capability(35, "Advanced computer/system control", Status.PLANNED),
        Capability(36, "Detailed income-platform automation", Status.PLANNED),
        Capability(37, "Income website human-only rule engine", Status.PLANNED),
        Capability(38, "Universal income rules/training layer", Status.PLANNED),
        Capability(39, "Advanced video/information fact-check", Status.PLANNED),
        Capability(41, "Owner-controlled sharing and multi-user access", Status.PLANNED),
        Capability(42, "Privacy and activity audit system", Status.PLANNED),
        Capability(43, "Explicit-consent remote assistance", Status.PLANNED)
    )

    fun all(): List<Capability> = capabilities

    fun byId(id: Int): Capability? = capabilities.firstOrNull { it.id == id }

    fun count(status: Status): Int = capabilities.count { it.status == status }
}
