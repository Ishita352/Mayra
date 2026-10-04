package com.mayra.assistant

/**
 * Governs how Mayra may use external study resources and prepare
 * original study products for permitted income channels.
 *
 * Mayra must not scrape, bypass access controls, copy paid/private material,
 * or resell another person's copyrighted notes without permission.
 */
object MayraExternalStudyResourcePolicy {
    enum class Rights { OWNER_CREATED, PUBLIC_DOMAIN, LICENSED, EXPRESS_PERMISSION, UNKNOWN, RESTRICTED }
    enum class Use { STUDY, SUMMARIZE, EXPLAIN, TRANSFORM, SELL }
    enum class Decision { ALLOW, ALLOW_WITH_ATTRIBUTION, MANUAL_REVIEW, BLOCK }

    data class Resource(
        val title: String,
        val subject: String,
        val sourceUrl: String,
        val rights: Rights,
        val platformAllowsUse: Boolean = true
    )

    fun decide(resource: Resource, use: Use): Decision {
        if (resource.title.isBlank() || resource.subject.isBlank() ||
            !resource.sourceUrl.startsWith("https://")) return Decision.BLOCK

        if (resource.rights == Rights.UNKNOWN || resource.rights == Rights.RESTRICTED ||
            !resource.platformAllowsUse) return Decision.BLOCK

        return when (use) {
            Use.STUDY, Use.SUMMARIZE, Use.EXPLAIN ->
                if (resource.rights == Rights.PUBLIC_DOMAIN) Decision.ALLOW
                else Decision.ALLOW_WITH_ATTRIBUTION
            Use.TRANSFORM ->
                if (resource.rights == Rights.OWNER_CREATED ||
                    resource.rights == Rights.LICENSED ||
                    resource.rights == Rights.EXPRESS_PERMISSION) Decision.ALLOW
                else Decision.MANUAL_REVIEW
            Use.SELL ->
                if (resource.rights == Rights.OWNER_CREATED ||
                    resource.rights == Rights.LICENSED ||
                    resource.rights == Rights.EXPRESS_PERMISSION) Decision.ALLOW
                else Decision.MANUAL_REVIEW
        }
    }

    fun incomeRule(): String =
        "For note-selling income, Mayra may help create, improve, format, classify, explain and publish original or properly licensed study material. It must not copy paid/private notes, bypass platform controls, scrape prohibited content, remove attribution, or sell another person's copyrighted material."

    fun coverageRule(): String =
        "The policy applies across subjects and academic disciplines; subject does not change copyright, license, platform terms or permission requirements."
}
