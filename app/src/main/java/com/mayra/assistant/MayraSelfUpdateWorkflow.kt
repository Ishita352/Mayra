package com.mayra.assistant

/**
 * Owner-controlled self-update workflow for Mayra.
 *
 * Mayra may prepare and apply source changes only after explicit Owner approval
 * when the requested change requires elevated permission. Secrets and financial
 * actions are never part of the update workflow.
 */
object MayraSelfUpdateWorkflow {
    enum class Stage { REQUESTED, SUGGESTED, ANALYZING, PERMISSION_REQUIRED, OWNER_APPROVAL_REQUIRED, EDITING, TESTING, VERIFIED, READY_TO_APPLY, APPLIED, BLOCKED }
    enum class Risk { LOW, ELEVATED, HIGH }

    data class Request(
        val description: String,
        val ownerApproved: Boolean,
        val requestedPermissions: Set<String> = emptySet(),
        val risk: Risk = Risk.LOW
    )

    fun suggest(request: Request): String =
        "Mayra suggests: ${request.description}. It will explain the change, required permissions, risks and tests before asking Owner approval."

    fun requiresPermission(request: Request): Boolean =
        request.requestedPermissions.isNotEmpty() || request.risk != Risk.LOW

    fun canEditCode(request: Request): Boolean =
        request.description.isNotBlank() &&
            !request.requestedPermissions.any { it.equals("financial_transaction", true) }

    fun nextStage(request: Request, testsPassed: Boolean): Stage {
        if (!canEditCode(request)) return Stage.BLOCKED
        if (!request.ownerApproved) return Stage.OWNER_APPROVAL_REQUIRED
        if (!testsPassed) return Stage.TESTING
        return Stage.READY_TO_APPLY
    }

    fun approvalRequiredForEveryChange() = true

    fun allowedOperations(): Set<String> = setOf(
        "inspect_source",
        "edit_source",
        "add_tests",
        "run_tests",
        "review_diff",
        "prepare_update"
    )

    fun blockedOperations(): Set<String> = setOf(
        "financial_transaction",
        "secret_exfiltration",
        "bypass_permission",
        "disable_security_controls",
        "covert_remote_access"
    )

    fun rule() =
        "Mayra can improve its code and prepare updates only after explicit Owner approval; every change requires approval, tests must pass, security controls cannot be disabled, and financial/secret/covert operations are blocked."
}
