package com.mayra.assistant

object MayraIncomeWorkflow {
    enum class Type { JOB, FREELANCE, ACTIVE_INCOME, PASSIVE_INCOME }
    enum class Status { WATCHING, APPROVED, WORKING, SUBMITTED, EARNED, MANUAL_ONLY, BLOCKED }

    data class Opportunity(
        val id: String, val title: String, val platform: String, val type: Type,
        val aiAllowed: Boolean, val status: Status = Status.WATCHING, val notes: String = ""
    )

    data class Plan(val opportunities: List<Opportunity>, val requiresOwnerApproval: Boolean, val message: String)

    fun plan(request: String, opportunities: List<Opportunity> = emptyList()): Plan {
        val text = request.trim().lowercase()
        val selected = if (text.isBlank()) emptyList() else opportunities.filter {
            val haystack = "${it.title} ${it.platform} ${it.type.name} ${it.notes}".lowercase()
            text.split(Regex("\\s+")).filter { token -> token.length >= 3 }.any(haystack::contains)
        }
        return Plan(selected, true, if (selected.isEmpty())
            "Mayra opportunity research করবে; account creation, application/submission এবং paid work-এর final approval Owner-এর থাকবে."
        else "Opportunity পাওয়া গেছে। AI/automation policy যাচাই এবং Owner approval ছাড়া application, account action বা submission করা যাবে না.")
    }

    fun register(opportunity: Opportunity): Opportunity =
        opportunity.copy(status = if (opportunity.aiAllowed) Status.APPROVED else Status.MANUAL_ONLY)

    fun canAutomate(opportunity: Opportunity): Boolean =
        opportunity.aiAllowed && opportunity.status != Status.MANUAL_ONLY && opportunity.status != Status.BLOCKED

    fun earningsInstruction(): String =
        "Mayra earning amount শুধু record/track করতে পারে; bank, wallet, payment, withdrawal, purchase বা subscription transaction করবে না."
}
