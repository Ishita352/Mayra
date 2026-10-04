package com.mayra.assistant

object MayraLearningCertificateWorkflow {
    enum class Stage { LEARNING, VERIFIED, CERTIFICATE_PREPARED, OWNER_REVIEW, OWNER_APPROVED, CV_READY, UPLOAD_READY, UPLOADED }

    data class Certificate(
        val skill: String,
        val certificateTitle: String,
        val holderName: String = "Gopal Basak",
        val evidence: String,
        val stage: Stage = Stage.LEARNING
    )

    fun prepare(skill: String, evidence: String): Certificate? {
        if (skill.isBlank() || evidence.isBlank()) return null
        return Certificate(skill, "$skill — Learning & Skill Completion", evidence = evidence, stage = Stage.CERTIFICATE_PREPARED)
    }

    fun approve(certificate: Certificate, ownerApproved: Boolean): Certificate =
        if (ownerApproved && certificate.evidence.isNotBlank())
            certificate.copy(stage = Stage.OWNER_APPROVED)
        else certificate.copy(stage = Stage.OWNER_REVIEW)

    fun readyForCv(certificate: Certificate): Boolean =
        certificate.stage == Stage.OWNER_APPROVED

    fun rule(): String =
        "Mayra may prepare a learning certificate in the Owner name Gopal Basak only from evidenced learning. It must be shown to the Owner; no certificate is represented as an official third-party credential unless actually issued by that provider. CV upload requires explicit Owner approval."
}
