package com.mayra.assistant

/** Owner-controlled workflow for finding and preparing lawful note-selling opportunities. */
object MayraNoteIncomeWorkflow {
    enum class Stage { DISCOVERED, REVIEW_REQUIRED, APPROVED, PREPARING, READY_TO_UPLOAD, MANUAL_ONLY, BLOCKED }

    data class Opportunity(
        val platform: String,
        val sourceUrl: String,
        val subject: String,
        val level: MayraEducationIntelligence.AcademicLevel,
        val noteType: String,
        val aiAllowed: Boolean,
        val platformAllowsSeller: Boolean
    )

    data class ResearchBrief(
        val platform: String,
        val subject: String,
        val requirements: List<String>,
        val titleSuggestion: String,
        val structure: List<String>,
        val uploadChecklist: List<String>
    )

    data class NotePackage(
        val title: String,
        val subject: String,
        val level: MayraEducationIntelligence.AcademicLevel,
        val originalNote: String,
        val keyPoints: List<String>,
        val description: String,
        val tags: List<String>
    )

    fun stage(opportunity: Opportunity, ownerApproved: Boolean): Stage {
        if (opportunity.sourceUrl.isBlank() || !opportunity.sourceUrl.startsWith("https://")) return Stage.BLOCKED
        if (!opportunity.platformAllowsSeller) return Stage.BLOCKED
        if (!opportunity.aiAllowed) return Stage.MANUAL_ONLY
        return if (ownerApproved) Stage.APPROVED else Stage.REVIEW_REQUIRED
    }

    fun buildResearchBrief(opportunity: Opportunity, requirements: List<String>): ResearchBrief {
        val cleanRequirements = requirements.filter { it.isNotBlank() }
        val title = opportunity.subject + " " + opportunity.noteType + " Notes"
        return ResearchBrief(
            platform = opportunity.platform,
            subject = opportunity.subject,
            requirements = cleanRequirements,
            titleSuggestion = title,
            structure = listOf(
                "Overview and learning objectives",
                "Topic/chapter-wise original explanation",
                "Key concepts and definitions",
                "Examples and practice questions",
                "Quick revision summary"
            ),
            uploadChecklist = listOf(
                "Confirm ownership/licence of all source material",
                "Check current platform seller/content rules",
                "Remove copied paid/private material",
                "Proofread and verify academic claims",
                "Owner reviews final file before upload"
            )
        )
    }

    fun preparePackage(
        subject: String,
        level: MayraEducationIntelligence.AcademicLevel,
        title: String,
        originalNote: String,
        keyPoints: List<String>,
        description: String,
        tags: List<String>
    ): NotePackage {
        return NotePackage(
            title = title.trim(),
            subject = subject.trim(),
            level = level,
            originalNote = originalNote.trim(),
            keyPoints = keyPoints.filter { it.isNotBlank() },
            description = description.trim(),
            tags = tags.filter { it.isNotBlank() }
        )
    }

    fun ownerUploadRule(): String =
        "Mayra prepares and checks the material, but the Owner performs the final upload and accepts the platform's current terms. Mayra must not bypass account controls, CAPTCHA, OTP, payment steps or human-only rules."

    fun incomeSafetyRule(): String =
        "Only original, public-domain, properly licensed or expressly permitted material may be prepared for sale. Paid/private third-party notes must not be copied or resold."
}
