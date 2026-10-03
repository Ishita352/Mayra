package com.mayra.assistant

object IncomeOpportunityPolicy {
    enum class Type { ACTIVE, PASSIVE, SEMI_PASSIVE }

    fun priority(type: Type, currency: String, estimatedMinutes: Int, repeatable: Boolean): Int {
        var score = 0
        if (currency.uppercase().contains("USD") || currency.uppercase().contains("DOLLAR")) score += 40
        if (currency.uppercase().contains("INR") || currency.contains("₹")) score += 20
        if (estimatedMinutes in 1..120) score += 25
        if (repeatable) score += 15
        if (type == Type.PASSIVE || type == Type.SEMI_PASSIVE) score += 5
        return score
    }

    fun requiresOwnerPermission(): Boolean = true

    fun summary(): String = """
        Active + Passive Income Engine

        • USD/dollar opportunities: first priority
        • INR opportunities: searched in parallel
        • Short-duration/high earning-potential work: prioritized
        • Hourly, repeatable and remote/freelance work: prioritized
        • Passive/semi-passive opportunities: continuously researched
        • Every opportunity: legality, scam/risk, zero-upfront-cost and skill-match checks
        • Mayra informs the Owner when an opportunity is found
        • Owner permission is required before applying, starting work, publishing or operating an income workflow
        • Mayra cannot spend money, subscribe, withdraw money or execute financial transactions
        • Background operation means normal OS-supported background processing, not stealth or unauthorized access
    """.trimIndent()
}