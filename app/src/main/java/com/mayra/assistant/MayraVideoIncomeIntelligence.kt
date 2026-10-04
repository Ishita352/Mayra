package com.mayra.assistant

/**
 * Lawful video-income platform intelligence.
 *
 * Mayra researches legitimate creator/contributor routes, eligibility,
 * content requirements, rights, metadata, submission and monetization rules.
 * Account ownership, final submission and financial actions remain Owner-controlled.
 */
object MayraVideoIncomeIntelligence {
    enum class PlatformType { VIDEO_PLATFORM, STOCK_VIDEO, CREATOR_MARKETPLACE, OTHER }
    enum class Action { RESEARCH, COMPARE, PREPARE_VIDEO, PREPARE_METADATA, PREPARE_APPLICATION, SUBMIT, FINANCIAL_TRANSACTION }
    enum class Decision { ALLOW_RESEARCH, OWNER_APPROVAL_REQUIRED, MANUAL_ONLY, BLOCKED }

    data class Platform(
        val name: String,
        val type: PlatformType,
        val officialUrl: String,
        val monetizationModel: String,
        val aiAllowed: Boolean = true,
        val ownerApproved: Boolean = false
    )

    data class IncomePlan(
        val platform: Platform,
        val earningRoutes: List<String>,
        val preparationSteps: List<String>,
        val legalChecks: List<String>,
        val decision: Decision
    )

    fun decide(platform: Platform, action: Action): Decision {
        if (platform.name.isBlank() || !platform.officialUrl.startsWith("https://")) return Decision.BLOCKED
        if (action == Action.FINANCIAL_TRANSACTION) return Decision.BLOCKED
        if (!platform.aiAllowed) return Decision.MANUAL_ONLY
        if (action == Action.SUBMIT && !platform.ownerApproved) return Decision.OWNER_APPROVAL_REQUIRED
        return if (platform.ownerApproved) Decision.ALLOW_RESEARCH else Decision.OWNER_APPROVAL_REQUIRED
    }

    fun knownPlatforms(): List<Platform> = listOf(
        Platform(
            "YouTube",
            PlatformType.VIDEO_PLATFORM,
            "https://www.youtube.com/",
            "YouTube Partner Program: advertising, YouTube Premium, memberships, Super Chat/Super Stickers, Super Thanks and eligible shopping features."
        ),
        Platform(
            "Adobe Stock",
            PlatformType.STOCK_VIDEO,
            "https://stock.adobe.com/",
            "Contributor licensing royalties for accepted video and other creative assets."
        ),
        Platform(
            "Shutterstock Contributor",
            PlatformType.STOCK_VIDEO,
            "https://submit.shutterstock.com/",
            "Contributor earnings from licensed image and video downloads."
        )
    )

    fun plan(platform: Platform, ownerApproved: Boolean = false): IncomePlan {
        val routes = when (platform.type) {
            PlatformType.VIDEO_PLATFORM -> listOf(
                "Check creator-program eligibility and monetization requirements",
                "Build original, policy-compliant videos",
                "Grow a relevant audience and review analytics",
                "Use eligible platform monetization features after acceptance"
            )
            PlatformType.STOCK_VIDEO -> listOf(
                "Create original/licensed footage with commercial-use rights",
                "Meet technical and quality requirements",
                "Add accurate titles, descriptions and metadata",
                "Provide required model/property releases",
                "Submit for moderation and earn when content is licensed"
            )
            PlatformType.CREATOR_MARKETPLACE -> listOf(
                "Check current contributor/creator eligibility",
                "Review accepted content and licensing terms",
                "Prepare original deliverables and metadata",
                "Submit through the platform's approved workflow"
            )
            PlatformType.OTHER -> listOf(
                "Verify the platform's current creator and monetization rules before proceeding"
            )
        }
        val preparation = listOf(
            "Research current official eligibility and monetization rules",
            "Check copyright, music, footage, logo, model and property rights",
            "Prepare video edit, thumbnail/preview and accurate metadata",
            "Check platform-specific technical specifications",
            "Estimate effort, fees, taxes and realistic earning potential",
            "Keep Owner control over account credentials, final submission and publication"
        )
        val legal = listOf(
            "Do not upload content without the necessary rights or permission",
            "Do not bypass copyright checks, moderation, CAPTCHA, OTP or identity verification",
            "Do not promise guaranteed income",
            "Re-check current platform rules, tax obligations and licensing terms before submission"
        )
        return IncomePlan(
            platform, routes, preparation, legal,
            if (ownerApproved) Decision.ALLOW_RESEARCH else Decision.OWNER_APPROVAL_REQUIRED
        )
    }

    fun updateRule(): String =
        "Mayra should periodically refresh official creator-program rules, monetization features, royalty rates, submission requirements, copyright policies, tax information and platform terms, recording source and date before treating them as current."

    fun incomeRule(): String =
        "Mayra may discover, compare and prepare lawful video-income opportunities, editing plans, metadata, portfolios and application checklists. It must not guarantee earnings, impersonate the Owner, bypass platform controls, or execute financial transactions."
}
