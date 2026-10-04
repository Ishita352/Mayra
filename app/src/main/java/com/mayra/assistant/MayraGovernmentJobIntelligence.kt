package com.mayra.assistant

/** Government job intelligence and application-preparation workflow for Mayra. */
object MayraGovernmentJobIntelligence {
    enum class Scope { WEST_BENGAL, CENTRAL, BOTH }
    enum class Stage { NOTIFICATION, APPLICATION, ADMIT_CARD, EXAM, ANSWER_KEY, RESULT, INTERVIEW, FINAL_RESULT, ARCHIVE }
    enum class Action { RESEARCH, MATCH, PREPARE_FORM, PREPARE_DOCUMENTS, STUDY, TRACK_RESULT, SUBMIT }

    data class Job(
        val title: String,
        val organization: String,
        val scope: Scope,
        val sourceUrl: String,
        val applicationUrl: String? = null,
        val eligibility: String = "",
        val syllabus: String = "",
        val examPattern: String = "",
        val deadlineMs: Long? = null,
        val stage: Stage = Stage.NOTIFICATION
    )

    data class ApplicationGuide(
        val jobTitle: String,
        val eligibilityCheck: List<String>,
        val documentChecklist: List<String>,
        val formSteps: List<String>,
        val examPreparation: List<String>,
        val resultTracking: List<String>,
        val finalSubmissionOwnerControlled: Boolean
    )

    fun validate(job: Job): Boolean =
        job.title.isNotBlank() &&
            job.organization.isNotBlank() &&
            job.sourceUrl.startsWith("https://")

    fun match(job: Job, education: String, skills: String): Boolean {
        if (!validate(job)) return false
        val text = (job.eligibility + " " + job.syllabus + " " + education + " " + skills).lowercase()
        return education.isNotBlank() && skills.isNotBlank() && text.isNotBlank()
    }

    fun prepareApplicationGuide(job: Job): ApplicationGuide {
        return ApplicationGuide(
            jobTitle = job.title,
            eligibilityCheck = listOf(
                "Verify age, education, category, nationality and other eligibility from the latest official notification",
                "Check application dates, fee, reservation and relaxation rules",
                "Confirm the official recruitment notice has not been superseded"
            ),
            documentChecklist = listOf(
                "Photo and signature in the required format",
                "Educational certificates/marksheets",
                "Identity and address documents when required",
                "Category/EWS/PwD/other certificates when applicable",
                "Experience or other post-specific documents when required"
            ),
            formSteps = listOf(
                "Open the official application portal",
                "Create/login to the candidate account",
                "Enter verified personal, education and eligibility information",
                "Upload documents in the published format and size",
                "Review every field against the original documents",
                "Owner reviews the complete application before final submission"
            ),
            examPreparation = listOf(
                "Extract the latest official syllabus",
                "Build topic-wise notes and revision plan",
                "Create practice questions/mock tests",
                "Track exam date, admit card and official notices"
            ),
            resultTracking = listOf(
                "Track official answer key/response notice when published",
                "Track written result and shortlist/interview notices",
                "Track final result and document-verification notices"
            ),
            finalSubmissionOwnerControlled = true
        )
    }

    fun supportedCoverage(): String =
        "West Bengal and Central Government recruitment: notifications, eligibility, syllabus, exam pattern, application preparation, admit cards, exam notices, answer keys, results, interviews and final results."

    fun safetyRule(): String =
        "Mayra may research, match eligibility, prepare forms/documents/checklists and study plans, but must not invent applicant data, forge documents, bypass CAPTCHA/OTP or submit a legally significant application without Owner confirmation."

    fun currentSourceRule(): String =
        "Current job information must be verified against the latest official recruiting authority or government portal before being presented as current."
}
