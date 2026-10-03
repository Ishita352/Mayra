package com.mayra.assistant

/**
 * James Billings / AMT knowledge foundation.
 * Facts marked as verified come from the public General Research
 * open-source AMT-JB repository. Account-specific eligibility, current
 * payout rules and live task availability must be re-checked at runtime.
 */
object JamesBillingsKnowledge {
    data class Topic(val name: String, val knowledge: String, val verified: Boolean)

    val topics = listOf(
        Topic("account_linking", "MTurk worker account linking/migration uses an authenticated link flow; the public repository documents /auth/link-amt/ and magic-link exchange.", true),
        Topic("tasks", "The AMT-JB system handles HIT acceptance, work/preview/report, HIT refill and expired-HIT handling; the public codebase is a panel implementation on top of MTurk.", true),
        Topic("worker_flow", "Worker-facing knowledge should cover login/linking, locating eligible work, opening/previewing a HIT, completing the task, reporting/submitting the assignment, and checking submission status.", true),
        Topic("assignment_events", "MTurk submission notifications are received through an event endpoint and processed into assignment-submitted events.", true),
        Topic("account_migration", "The repository contains an invite/link-AMT flow, worker-id handling, magic-link exchange, and login handling for users transitioning into the panel.", true),
        Topic("cashout_status", "The repository contains cashout request/confirmation/status email flows and transaction-status handling.", true),
        Topic("submission", "Submitted assignments are processed through MTurk notification/event handling; expired HITs are also handled.", true),
        Topic("cashout", "The public codebase contains cashout request, confirmation/status email and redemption flows.", true),
        Topic("paypal", "The public codebase contains PayPal cashout-method creation/lookup and transaction-id handling.", true),
        Topic("tango", "The public codebase contains Tango credentials and redemption-instruction handling for cashout transactions.", true),
        Topic("cashout_delivery", "Cashout status data can include redemption instructions and transaction information; the exact available method depends on the current account and service rules.", true),
        Topic("technical_stack", "The public repository documents FastAPI, React, PostgreSQL, NGINX and background/event processing; this is implementation knowledge, not a requirement for a worker.", true),
        Topic("current_changes", "The public repository is actively changing in 2026, including account-linking and redemption flows, so Mayra must not treat old instructions as permanently current.", true),
        Topic("owner_workflow", "Mayra should teach/account-guide the Owner step-by-step, but must not log in, submit tasks, redeem funds, or perform financial transactions for the Owner.", true),
        Topic("live_rules", "Current eligibility, available HITs, worker qualification, fees, payout timing and country-specific rules must be re-verified before use.", false)
    )

    fun help(): String =
        "James Billings: account/linking → HIT workflow → work/preview/report → submission → cashout → redemption; live rules require re-verification."

    fun mayraMayPerformFinancialAction(): Boolean = false
    fun mayraMaySubmitAsOwner(): Boolean = false
    fun requiresOwnerApproval(): Boolean = true
}
