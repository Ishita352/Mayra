package com.mayra.assistant

/**
 * Government-service assistance boundary.
 *
 * Mayra can prepare documents/forms and guide the owner through official
 * government portals. Sensitive verification and final submission remain
 * owner-controlled.
 */
object GovernmentServicePolicy {
    enum class Level { CENTRAL_GOVERNMENT, WEST_BENGAL_GOVERNMENT }

    enum class Source { INDIA_PORTAL, MY_SCHEME, WEST_BENGAL_GOVERNMENT, DEPARTMENT_PORTAL }

    fun mayPrepareDocuments(): Boolean = true
    fun mayPrepareFormDraft(): Boolean = true
    fun mayOpenOfficialApplication(): Boolean = true
    fun mayPerformFinalSubmissionWithoutOwnerApproval(): Boolean = false
    fun mayBypassOtpCaptchaOrIdentityVerification(): Boolean = false
    fun mayInventEligibility(): Boolean = false
    fun mayInventSchemeUpdate(): Boolean = false
    fun requiresOfficialSourceForSchemeUpdate(): Boolean = true
    fun mayAutoSubmitWithoutOwnerApproval(): Boolean = false
}
