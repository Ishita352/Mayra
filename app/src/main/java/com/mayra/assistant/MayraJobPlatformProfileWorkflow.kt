package com.mayra.assistant

object MayraJobPlatformProfileWorkflow {
    enum class Platform { LINKEDIN, NAUKRI, INDEED, OTHER_JOB_PLATFORM }
    enum class Stage { RESEARCH, PROFILE_PREPARED, OWNER_REVIEW, OWNER_APPROVED, READY_TO_CREATE, CREATED, READY_TO_UPDATE, UPDATED }

    data class ProfilePlan(
        val platform: Platform,
        val fields: List<String>,
        val stage: Stage = Stage.RESEARCH
    )

    fun plan(platform: Platform): ProfilePlan = ProfilePlan(
        platform,
        listOf("name", "headline", "about", "experience", "education", "skills", "certifications", "languages", "projects", "work_preferences")
    )

    fun ownerApprovalRequired(stage: Stage): Boolean =
        stage == Stage.PROFILE_PREPARED || stage == Stage.READY_TO_CREATE || stage == Stage.READY_TO_UPDATE

    fun rule(): String =
        "Mayra can research platform requirements, prepare profile content, match skills/jobs, and guide account/profile creation or updates. Account creation, credential entry, public profile changes and job applications require explicit Owner approval. Never invent experience, certificates or skills; never bypass OTP, CAPTCHA, platform rules or paid services."
}
