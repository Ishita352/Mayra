package com.mayra.assistant

/**
 * Built-in OneForma platform knowledge.
 *
 * This is a product/workflow model, not a promise that live account data is
 * available. Live project terms always override this baseline knowledge.
 */
object OneFormaPlatformKnowledge {
    const val PLATFORM_NAME = "OneForma"
    const val OFFICIAL_SITE = "https://www.oneforma.com/"
    const val JOBS_HELP = "https://www.oneforma.com/help-center/general-inquiries/how-to-apply-to-projects-in-your-dashboard/"
    const val SOW_HELP = "https://www.oneforma.com/help-center/general-inquiries/understanding-the-statement-of-work-sow-on-oneforma/"
    const val TASKS_HELP = "https://www.oneforma.com/help-center/general-inquiries/what-to-expect-when-working-on-projects-from-start-to-submission/"
    const val CODE_OF_CONDUCT = "https://www.oneforma.com/code-of-conduct/"
    const val TERMS = "https://www.oneforma.com/terms-and-conditions/"

    enum class ProjectStage {
        DISCOVER, MATCH, ELIGIBILITY_CHECK, REQUIREMENTS_REVIEW, NDA_REVIEW,
        CERTIFICATION, APPLICATION, UNDER_REVIEW, ENDORSED, TASK_QUEUE,
        QUALITY_CHECK, SUBMISSION, PAYMENT_TRACKING
    }

    enum class WorkType {
        ANNOTATION, DATA_COLLECTION, JUDGING, TRANSCRIPTION, TRANSLATION, OTHER
    }

    enum class RequiredCheck {
        REGION, LANGUAGE, SKILLS, CERTIFICATION, PHONE_VERIFICATION,
        IDENTITY_VERIFICATION, RESUME, NDA, SOW, EXCLUSIVITY, AI_RULES,
        AUTOMATION_RULES, DEADLINE, PAYMENT_RULES
    }

    fun standardProjectStages(): List<ProjectStage> = ProjectStage.values().toList()
    fun standardRequiredChecks(): List<RequiredCheck> = RequiredCheck.values().toList()
    fun supportedWorkTypes(): List<WorkType> = WorkType.values().toList()
    fun mayUseBaselineKnowledgeWhenLiveRulesMissing(): Boolean = false
    fun liveProjectRulesOverrideBaseline(): Boolean = true
    fun requiresProjectSpecificRuleCheckBeforeWork(): Boolean = true
    fun requiresTruthfulProfileAndIdentity(): Boolean = true
    fun requiresOwnerControlForNdaAndIdentity(): Boolean = true
    fun vpnOrProxyBypassAllowed(): Boolean = false
    fun unauthorizedAutomationAllowed(): Boolean = false
}
