package com.mayra.assistant

/**
 * Security boundary for Mayra self-repair and self-update proposals.
 * Diagnosis and patch preparation are allowed; applying code changes always
 * requires explicit owner approval. Protected security boundaries cannot be
 * self-applied even after approval.
 */
object SelfRepairUpdatePolicy {
    enum class Risk { SAFE, OWNER_APPROVAL_REQUIRED, BLOCKED }
    enum class ChangeArea {
        BUG_FIX, PERFORMANCE, UI, KNOWLEDGE, CONFIGURATION,
        OWNER_IDENTITY, AUTHENTICATION, PERMISSIONS, SECURITY_BOUNDARY,
        AUDIT_LOGGING, SENSITIVE_DATA, PAYMENT
    }

    data class Proposal(
        val id: String,
        val area: ChangeArea,
        val summary: String,
        val risk: Risk,
        val requiresOwnerApproval: Boolean,
        val validationSteps: List<String>
    )

    fun assess(area: ChangeArea, summary: String): Proposal {
        val protectedArea = area in protectedAreas()
        return Proposal(
            id = stableId(area, summary),
            area = area,
            summary = summary.trim(),
            risk = if (protectedArea) Risk.BLOCKED else Risk.OWNER_APPROVAL_REQUIRED,
            requiresOwnerApproval = true,
            validationSteps = listOf(
                "Static policy check",
                "Unit/regression tests",
                "Android debug build",
                "Owner review before installation"
            )
        )
    }

    fun canApply(proposal: Proposal, ownerApproved: Boolean): Boolean =
        ownerApproved &&
            proposal.risk != Risk.BLOCKED &&
            proposal.requiresOwnerApproval

    fun protectedAreas(): Set<ChangeArea> = setOf(
        ChangeArea.OWNER_IDENTITY,
        ChangeArea.AUTHENTICATION,
        ChangeArea.PERMISSIONS,
        ChangeArea.SECURITY_BOUNDARY,
        ChangeArea.AUDIT_LOGGING,
        ChangeArea.SENSITIVE_DATA,
        ChangeArea.PAYMENT
    )

    private fun stableId(area: ChangeArea, summary: String): String =
        area.name.lowercase() + "-" +
            summary.trim().lowercase().hashCode().toUInt().toString(16)
}
