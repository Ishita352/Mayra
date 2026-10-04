package com.mayra.assistant

/**
 * Copyright-safe originality and licensing workflow.
 *
 * Mayra cannot legally erase another person's copyright merely by editing or
 * reformatting their work. It can help determine rights and create genuinely
 * original or properly licensed outputs.
 */
object MayraCopyrightOriginalityWorkflow {
    enum class SourceStatus {
        OWNER_CREATED, PUBLIC_DOMAIN, LICENSED, EXPRESS_PERMISSION,
        THIRD_PARTY_COPYRIGHTED, UNKNOWN, RESTRICTED
    }

    enum class Action { CHECK_RIGHTS, CREATE_ORIGINAL, TRANSFORM, REWRITE,
        REMOVE_INFRINGING_MATERIAL, PREPARE_FOR_SALE }

    enum class Decision { ALLOW, ALLOW_WITH_ATTRIBUTION, MANUAL_REVIEW, BLOCK }

    data class Source(
        val title: String,
        val sourceUrl: String,
        val status: SourceStatus,
        val licenseTerms: String = ""
    )

    data class OriginalityPlan(
        val sourceDecision: Decision,
        val steps: List<String>,
        val resultStatus: String
    )

    fun decide(source: Source, action: Action): Decision {
        if (source.title.isBlank() || !source.sourceUrl.startsWith("https://")) {
            return Decision.BLOCK
        }
        if (source.status == SourceStatus.UNKNOWN || source.status == SourceStatus.RESTRICTED) {
            return Decision.BLOCK
        }
        return when (action) {
            Action.CHECK_RIGHTS -> Decision.ALLOW
            Action.CREATE_ORIGINAL -> Decision.ALLOW
            Action.REMOVE_INFRINGING_MATERIAL ->
                if (source.status == SourceStatus.OWNER_CREATED ||
                    source.status == SourceStatus.LICENSED ||
                    source.status == SourceStatus.EXPRESS_PERMISSION)
                    Decision.ALLOW
                else Decision.MANUAL_REVIEW
            Action.TRANSFORM, Action.REWRITE, Action.PREPARE_FOR_SALE ->
                when (source.status) {
                    SourceStatus.OWNER_CREATED,
                    SourceStatus.PUBLIC_DOMAIN,
                    SourceStatus.LICENSED,
                    SourceStatus.EXPRESS_PERMISSION -> Decision.ALLOW
                    SourceStatus.THIRD_PARTY_COPYRIGHTED -> Decision.MANUAL_REVIEW
                    SourceStatus.UNKNOWN,
                    SourceStatus.RESTRICTED -> Decision.BLOCK
                }
        }
    }

    fun planForOriginalOutput(source: Source): OriginalityPlan {
        val decision = decide(source, Action.CREATE_ORIGINAL)
        val steps = listOf(
            "Identify facts/ideas that can lawfully be used without copying protected expression",
            "Use the source only within its copyright/license permissions",
            "Write new structure, wording, examples and presentation rather than reproducing protected expression",
            "Replace or remove third-party text, images, music and other assets when rights are not available",
            "Verify licenses, attribution and platform terms before publishing or selling",
            "Keep a record of sources, permissions and the creation process"
        )
        val status = when (source.status) {
            SourceStatus.OWNER_CREATED, SourceStatus.PUBLIC_DOMAIN,
            SourceStatus.LICENSED, SourceStatus.EXPRESS_PERMISSION ->
                "Mayra can prepare an original/licensed output subject to the applicable license."
            else ->
                "Mayra must not claim the source has become copyright-free; create a genuinely original work from lawful inputs or obtain permission."
        }
        return OriginalityPlan(decision, steps, status)
    }

    fun copyrightRule(): String =
        "Copyright cannot be erased from someone else's protected work simply by changing format, wording, file type, resolution or minor edits. Mayra must distinguish facts/ideas from protected expression, respect licenses and permissions, and create genuinely original or lawfully licensed outputs."

    fun publishingRule(): String =
        "Before publishing or selling, Mayra should verify ownership/license, attribution requirements, platform terms and any third-party assets. When rights are uncertain, stop and request Owner/legal review rather than claiming the work is non-copyright."

    fun noMagicNonCopyright(): Boolean = true
}
