package com.mayra.assistant

/**
 * Income, e-commerce and market-intelligence knowledge layer.
 *
 * USD/foreign-currency opportunities are prioritized for research when
 * otherwise comparable and lawful. Market knowledge is advisory/research
 * only: Mayra never executes financial transactions.
 */
object MayraIncomeMarketIntelligence {
    enum class IncomePriority { USD_FOREIGN_CURRENCY_FIRST, INR_SECOND }
    enum class Area {
        ECOMMERCE, MARKETPLACE_SELLING, DIGITAL_PRODUCTS, FREELANCE,
        AFFILIATE, CONTENT, SERVICES, STOCK_MARKET, CRYPTOCURRENCY,
        INVESTING, PERSONAL_FINANCE, BUSINESS
    }
    enum class Action {
        RESEARCH, COMPARE, PLAN, LEARN, ANALYZE, TRACK,
        PREPARE_LISTING, PREPARE_CONTENT, EXECUTE_TRADE, FINANCIAL_TRANSACTION
    }
    enum class Decision { ALLOW_RESEARCH, OWNER_APPROVAL_REQUIRED, BLOCKED }

    data class Opportunity(
        val name: String,
        val area: Area,
        val currency: String,
        val sourceUrl: String,
        val aiAllowed: Boolean,
        val ownerApproved: Boolean = false
    )

    data class MarketPlan(
        val area: Area,
        val focus: String,
        val learningTopics: List<String>,
        val researchTasks: List<String>,
        val riskChecks: List<String>,
        val decision: Decision
    )

    fun priority(): IncomePriority = IncomePriority.USD_FOREIGN_CURRENCY_FIRST

    fun decide(opportunity: Opportunity, action: Action): Decision {
        if (opportunity.name.isBlank() || !opportunity.sourceUrl.startsWith("https://")) {
            return Decision.BLOCKED
        }
        if (action == Action.EXECUTE_TRADE || action == Action.FINANCIAL_TRANSACTION) {
            return Decision.BLOCKED
        }
        if (!opportunity.aiAllowed) return Decision.OWNER_APPROVAL_REQUIRED
        return if (opportunity.ownerApproved) Decision.ALLOW_RESEARCH
        else Decision.OWNER_APPROVAL_REQUIRED
    }

    fun plan(area: Area, ownerApproved: Boolean = false): MarketPlan {
        val topics = when (area) {
            Area.ECOMMERCE, Area.MARKETPLACE_SELLING -> listOf(
                "Product research", "Marketplace rules", "Pricing and margin",
                "Listing quality", "Customer service", "International shipping and taxes"
            )
            Area.DIGITAL_PRODUCTS -> listOf(
                "Original digital products", "Licensing", "Pricing", "Marketplace terms"
            )
            Area.FREELANCE, Area.SERVICES -> listOf(
                "Skill positioning", "Portfolio", "Client requirements", "Platform rules"
            )
            Area.AFFILIATE, Area.CONTENT -> listOf(
                "Audience research", "Content strategy", "Disclosure rules", "Platform policies"
            )
            Area.STOCK_MARKET, Area.INVESTING -> listOf(
                "Market structure", "Fundamental analysis", "Technical analysis",
                "Risk management", "Diversification", "Regulatory basics"
            )
            Area.CRYPTOCURRENCY -> listOf(
                "Blockchain basics", "Market structure", "Token risks",
                "Custody/security", "Volatility", "Applicable regulation and tax"
            )
            Area.PERSONAL_FINANCE -> listOf(
                "Budgeting", "Tax awareness", "Risk", "Savings and investment concepts"
            )
            Area.BUSINESS -> listOf(
                "Business model", "Unit economics", "Market research", "Compliance"
            )
        }
        val tasks = listOf(
            "Find current opportunities from authoritative/platform sources",
            "Compare expected earning potential, eligibility, fees and restrictions",
            "Check whether AI/automation is permitted",
            "Prefer USD/foreign-currency opportunities before comparable INR opportunities",
            "Prepare Owner-controlled action plan and learning materials"
        )
        val risks = listOf(
            "No guaranteed return or income",
            "Verify current platform rules, laws, tax and fees",
            "Never bypass OTP, CAPTCHA, identity checks or platform restrictions",
            "No bank/wallet/payment/withdrawal/purchase/subscription transaction by Mayra"
        )
        return MarketPlan(
            area,
            if (area == Area.STOCK_MARKET || area == Area.CRYPTOCURRENCY)
                "Research and education only; trading decisions remain Owner-controlled"
            else "Income opportunity research with USD/foreign-currency priority",
            topics, tasks, risks,
            if (ownerApproved) Decision.ALLOW_RESEARCH else Decision.OWNER_APPROVAL_REQUIRED
        )
    }

    fun updateRule(): String =
        "Mayra should refresh platform policies, market rules, regulations, taxes, fees and relevant public information over time, record source/date, and re-check stale knowledge before presenting it as current."

    fun safetyRule(): String =
        "Mayra may research, explain, compare and prepare lawful income/market plans. It must not execute financial transactions, place trades, transfer funds, purchase assets, bypass platform rules, or guarantee profits."
}
