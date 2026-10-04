package com.mayra.assistant

object MayraIndiaRegulatoryKnowledge {
    enum class Domain { CONSTITUTION, RBI, BANKING, DIRECT_TAX, GST, CUSTOMS, CORPORATE, CONSUMER_FINANCE }
    data class Update(val domain: Domain, val source: String, val publishedAtMs: Long, val summary: String)
    fun domains(): Set<Domain> = Domain.values().toSet()
    fun accept(update: Update): Boolean = update.source.startsWith("https://") && update.publishedAtMs > 0L && update.summary.isNotBlank()
    fun requiresCurrentVerification(lastVerifiedAtMs: Long?, nowMs: Long): Boolean = lastVerifiedAtMs == null || nowMs - lastVerifiedAtMs >= 86400000L
    fun rule(): String = "Use current official sources for constitutional amendments, RBI directions, banking rules, direct tax, GST, customs and related compliance; record source and effective date."
}