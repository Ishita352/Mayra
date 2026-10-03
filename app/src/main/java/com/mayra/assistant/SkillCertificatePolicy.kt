package com.mayra.assistant

/**
 * Controls Mayra's learning records and certificates.
 *
 * External certificates must represent the actual learner/completer. Mayra
 * must never impersonate the Owner or falsely claim that the Owner completed
 * a course, exam, assessment, or certification.
 *
 * Mayra may maintain an internal Owner skill record/certificate after a skill
 * is verified through permitted evidence or Owner-confirmed completion.
 */
object SkillCertificatePolicy {
    const val OWNER_NAME = "Gopal Basak"

    enum class CertificateType {
        EXTERNAL_VERIFIED,
        OWNER_CONFIRMED,
        MAYRA_INTERNAL_SKILL_RECORD
    }

    fun mayraImpersonateOwnerForCertification(): Boolean = false

    fun mayraClaimExternalCertificateWithoutEvidence(): Boolean = false

    fun mayAddVerifiedSkillToOwnerBiodata(
        skillVerified: Boolean,
        evidenceAvailable: Boolean
    ): Boolean = skillVerified && evidenceAvailable

    fun certificateOwnerName(): String = OWNER_NAME
}
