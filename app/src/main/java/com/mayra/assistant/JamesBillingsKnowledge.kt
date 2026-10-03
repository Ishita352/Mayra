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
        Topic("tasks", "The AMT-JB system handles HIT acceptance, work/preview/report and submitted assignments.", true),
        Topic("submission", "Submitted assignments are processed through MTurk notification/event handling; expired HITs are also handled.", true),
        Topic("cashout", "The public codebase contains cashout request, confirmation/status email and redemption flows.", true),
        Topic("paypal", "The public codebase contains PayPal cashout-method creation/lookup and transaction-id handling.", true),
        Topic("tango", "The public codebase contains Tango credentials and redemption-instruction handling for cashout transactions.", true),
        Topic("owner_workflow", "Mayra should teach/account-guide the Owner step-by-step, but must not log in, submit tasks, redeem funds, or perform financial transactions for the Owner.", true),
        Topic("live_rules", "Current eligibility, available HITs, worker qualification, fees, payout timing and country-specific rules must be re-verified before use.", false)
    )

    fun help(): String =
        "James Billings: account/linking → HIT workflow → work/preview/report → submission → cashout → redemption; live rules require re-verification."

    fun mayraMayPerformFinancialAction(): Boolean = false
    fun mayraMaySubmitAsOwner(): Boolean = false
    fun requiresOwnerApproval(): Boolean = true
}
