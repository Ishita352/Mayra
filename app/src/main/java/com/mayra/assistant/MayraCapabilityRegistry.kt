package com.mayra.assistant

/** Central catalog for Mayra follow-up capabilities. */
object MayraCapabilityRegistry {
    enum class Status { COMPLETE, IN_PROGRESS, PLANNED }
    data class Capability(val id: Int, val title: String, val status: Status)

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
        Capability(28, "Windows 10 computer-control expansion", Status.COMPLETE),
        Capability(29, "Cross-device workflows", Status.COMPLETE),
        Capability(30, "Windows 10 full testing", Status.COMPLETE),
        Capability(31, "Master documentation, backup and release package", Status.COMPLETE),
        Capability(32, "Final project certificate", Status.COMPLETE),
        Capability(33, "Public Mayra website and GitHub Pages/custom domain", Status.COMPLETE),
        Capability(34, "Website and Mayra integration", Status.COMPLETE),
        Capability(35, "Advanced computer/system control", Status.COMPLETE),
        Capability(36, "Detailed income-platform automation", Status.COMPLETE),
        Capability(37, "Income website human-only rule engine", Status.COMPLETE),
        Capability(38, "Universal income rules/training layer", Status.COMPLETE),
        Capability(39, "Advanced video/information fact-check", Status.COMPLETE),
        Capability(41, "Owner-controlled sharing and multi-user access", Status.COMPLETE),
        Capability(42, "Privacy and activity audit system", Status.COMPLETE),
        Capability(43, "Explicit-consent remote assistance", Status.PLANNED),
        Capability(44, "India Constitution and legal guidance engine", Status.COMPLETE),
        Capability(45, "Public organisation intelligence and change monitoring", Status.COMPLETE),
        Capability(46, "Hindu scriptures and Ayurveda knowledge", Status.COMPLETE),
        Capability(47, "Education intelligence and Burdwan academic updates", Status.COMPLETE),
        Capability(48, "Lawful external study resources and note-income workflow", Status.COMPLETE),
        Capability(49, "E-commerce, USD-first income and market intelligence", Status.COMPLETE),
        Capability(50, "Video/photo editing, YouTube and social-media work intelligence", Status.COMPLETE),
        Capability(51, "Copyright-safe originality and licensing workflow", Status.COMPLETE),
        Capability(52, "Lawful video-income platform intelligence", Status.COMPLETE),
        Capability(53, "Continuous background self-development, learning and opportunity discovery", Status.COMPLETE),
        Capability(54, "Core no-investment, free-first builder and human-only-site principles", Status.COMPLETE),
        Capability(55, "Owner WhatsApp important-information channel", Status.IN_PROGRESS),
        Capability(56, "Owner Home controls: camera, calls, 3D, voice light and WhatsApp", Status.IN_PROGRESS),
        Capability(57, "Voice access, locked-phone activity, silent behavior and Mayra volume", Status.IN_PROGRESS),
        Capability(58, "Protected Android + Windows 10 device safety and Owner-authorized control", Status.IN_PROGRESS),
        Capability(59, "Owner approval gate with controllable background automations", Status.IN_PROGRESS),
        Capability(60, "Multilingual grammar and language intelligence", Status.IN_PROGRESS)
        Capability(61, "CV-derived advanced Owner skill portfolio and CV-free opportunity matching", Status.IN_PROGRESS)
        Capability(62, "Owner-approved learning certificates and job-platform profile preparation", Status.IN_PROGRESS)
        Capability(63, "Device-aware multitasking and health-protected workload scaling", Status.IN_PROGRESS)
    )

    fun all(): List<Capability> = capabilities
    fun byId(id: Int): Capability? = capabilities.firstOrNull { it.id == id }
    fun count(status: Status): Int = capabilities.count { it.status == status }
}
