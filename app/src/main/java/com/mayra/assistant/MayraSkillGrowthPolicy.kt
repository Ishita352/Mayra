package com.mayra.assistant

/**
 * Controlled continuous-learning boundary for Mayra.
 *
 * Skill improvement can expand knowledge and quality, but can never relax
 * security, human-only, anti-bypass, owner-approval, or financial rules.
 */
object MayraSkillGrowthPolicy {
    enum class LearningState {
        DISCOVER,
        RESEARCH,
        CROSS_CHECK,
        SANDBOX_TEST,
        VERIFY,
        BACKUP,
        UPDATE_KNOWLEDGE,
        RE_TEST,
        NOTIFY_OWNER
    }

    fun learningPipeline(): List<LearningState> = listOf(
        LearningState.DISCOVER,
        LearningState.RESEARCH,
        LearningState.CROSS_CHECK,
        LearningState.SANDBOX_TEST,
        LearningState.VERIFY,
        LearningState.BACKUP,
        LearningState.UPDATE_KNOWLEDGE,
        LearningState.RE_TEST,
        LearningState.NOTIFY_OWNER
    )

    fun mayChangeSecurityOrFinancialRules(): Boolean = false
    fun mayBypassWebsiteRestrictions(): Boolean = false
    fun maySubmitExternalWorkWithoutOwnerApproval(): Boolean = false

    /**
     * Learning records and legitimate evidence may feed the Owner skill profile,
     * but external credentials must never be fabricated.
     */
    fun mayRecordVerifiedOwnerSkill(skillVerified: Boolean, evidenceAvailable: Boolean): Boolean =
        SkillCertificatePolicy.mayAddVerifiedSkillToOwnerBiodata(
            skillVerified = skillVerified,
            evidenceAvailable = evidenceAvailable
        )

    fun mayCreateInternalLearningRecord(): Boolean = true

}
