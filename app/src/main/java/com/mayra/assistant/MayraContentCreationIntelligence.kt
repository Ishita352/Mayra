package com.mayra.assistant

/**
 * Video/photo editing and social-media work intelligence for Owner-controlled income workflows.
 *
 * Mayra can plan, explain, review and prepare creative work, while the Owner remains
 * responsible for account ownership, platform terms, final publishing and client commitments.
 */
object MayraContentCreationIntelligence {
    enum class Area {
        VIDEO_EDITING, PHOTO_EDITING, YOUTUBE, SOCIAL_MEDIA_MANAGEMENT,
        SHORT_FORM_CONTENT, THUMBNAILS, CONTENT_REPURPOSING, CLIENT_WORK
    }

    enum class Action {
        RESEARCH, LEARN, PLAN, EDIT, REVIEW, PREPARE_DELIVERABLE,
        PREPARE_POST, SCHEDULE_POST, PUBLISH
    }

    enum class Decision { ALLOW_PREPARATION, OWNER_APPROVAL_REQUIRED, MANUAL_ONLY, BLOCKED }

    data class WorkOpportunity(
        val platform: String,
        val sourceUrl: String,
        val area: Area,
        val aiAllowed: Boolean,
        val ownerApproved: Boolean = false
    )

    data class WorkPlan(
        val area: Area,
        val skills: List<String>,
        val workflow: List<String>,
        val deliverables: List<String>,
        val decision: Decision
    )

    data class ExperienceProfile(
        val existingSkills: List<String>,
        val verifiedSkills: List<String>,
        val trainingSkills: List<String>,
        val learningSkills: List<String>
    )

    fun decide(opportunity: WorkOpportunity, action: Action): Decision {
        if (opportunity.platform.isBlank() || !opportunity.sourceUrl.startsWith("https://")) {
            return Decision.BLOCKED
        }
        if (action == Action.PUBLISH && !opportunity.ownerApproved) {
            return Decision.OWNER_APPROVAL_REQUIRED
        }
        if (!opportunity.aiAllowed) return Decision.MANUAL_ONLY
        return if (opportunity.ownerApproved) Decision.ALLOW_PREPARATION
        else Decision.OWNER_APPROVAL_REQUIRED
    }

    fun plan(area: Area, ownerApproved: Boolean = false): WorkPlan {
        val skills = when (area) {
            Area.VIDEO_EDITING -> listOf(
                "Cutting and sequencing", "Transitions and pacing", "Audio cleanup and sync",
                "Captions/subtitles", "Color correction basics", "Aspect ratios and export",
                "Short-form and long-form editing", "Client revision workflow"
            )
            Area.PHOTO_EDITING -> listOf(
                "Crop and composition", "Exposure and color correction", "Retouching basics",
                "Background/object cleanup", "Resizing and export", "Thumbnail preparation"
            )
            Area.YOUTUBE -> listOf(
                "Video editing workflow", "Thumbnail planning", "Titles and descriptions",
                "Audience/content research", "Upload checklist", "Copyright/platform-policy awareness"
            )
            Area.SOCIAL_MEDIA_MANAGEMENT -> listOf(
                "Content calendar", "Platform-specific formatting", "Caption and hashtag planning",
                "Community-response workflow", "Analytics interpretation", "Brand consistency"
            )
            Area.SHORT_FORM_CONTENT -> listOf(
                "Hook and pacing", "Vertical framing", "Captions", "B-roll planning",
                "Platform-specific export", "Repurposing long-form content"
            )
            Area.THUMBNAILS -> listOf(
                "Visual hierarchy", "Readable typography", "Image selection",
                "Brand consistency", "Platform dimensions", "A/B concept planning"
            )
            Area.CONTENT_REPURPOSING -> listOf(
                "Extract key moments", "Short-form cuts", "Quote cards",
                "Captions", "Platform adaptation", "Source attribution"
            )
            Area.CLIENT_WORK -> listOf(
                "Brief analysis", "Scope and deliverables", "Editing workflow",
                "Revision tracking", "File handoff", "Client communication"
            )
        }
        val workflow = listOf(
            "Understand the brief and target platform",
            "Check source rights, client instructions and platform rules",
            "Prepare an edit/content plan",
            "Create or revise the deliverable",
            "Quality-check audio, visuals, captions, format and metadata",
            "Prepare the final handoff; Owner controls final account action/publishing"
        )
        val deliverables = when (area) {
            Area.VIDEO_EDITING, Area.SHORT_FORM_CONTENT -> listOf(
                "Edited video", "Captions/subtitles", "Platform-ready export", "Revision notes"
            )
            Area.PHOTO_EDITING, Area.THUMBNAILS -> listOf(
                "Edited image", "Thumbnail variants", "Platform-ready export", "Revision notes"
            )
            Area.YOUTUBE -> listOf(
                "Edited video", "Thumbnail plan", "Title/description draft", "Upload checklist"
            )
            Area.SOCIAL_MEDIA_MANAGEMENT -> listOf(
                "Content calendar", "Caption drafts", "Creative checklist", "Analytics review"
            )
            Area.CONTENT_REPURPOSING -> listOf(
                "Short clips", "Caption assets", "Post drafts", "Source/attribution notes"
            )
            Area.CLIENT_WORK -> listOf(
                "Brief summary", "Production checklist", "Final deliverable", "Handoff notes"
            )
        }
        return WorkPlan(
            area, skills, workflow, deliverables,
            if (ownerApproved) Decision.ALLOW_PREPARATION else Decision.OWNER_APPROVAL_REQUIRED
        )
    }

    fun ownerExperienceProfile(): ExperienceProfile = ExperienceProfile(
        existingSkills = listOf(
            "YouTube video editing", "Photo editing/digital content work",
            "Word", "Excel/data analysis", "Web research", "Bengali/Hindi/English communication",
            "Writing", "Transcription/audio conversion", "Story/short-film content",
            "AI training/content validation", "Photo/content review"
        ),
        verifiedSkills = listOf(),
        trainingSkills = listOf(),
        learningSkills = listOf()
    )

    fun clientWorkRule(): String =
        "Mayra may research legitimate video/photo editing and social-media work opportunities, "
            + "prepare proposals, briefs, editing plans, checklists and deliverables, and help the Owner "
            + "use existing experience. It must not falsely claim skills, impersonate the Owner, or publish "
            + "to a client/social account without the required Owner authorization."

    fun rightsRule(): String =
        "Only use footage, images, music, fonts, logos and other assets when the Owner/client has the "
            + "necessary rights or permission. Do not remove attribution, bypass platform controls, or "
            + "reuse restricted material without permission."

    fun updateRule(): String =
        "Refresh editing-platform features, social-media policies, copyright rules, client-work practices "
            + "and marketplace opportunities over time; record source/date and re-check stale information."
}
