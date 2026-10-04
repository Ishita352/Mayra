package com.mayra.assistant

object MayraOwnerOpportunityFinder {
    enum class Track { CV_FIRST, NO_CV_MICRO_WORK }

    data class Match(
        val title: String,
        val category: String,
        val track: Track,
        val score: Int,
        val matchedSkills: List<String>,
        val guidance: String
    )

    fun find(track: Track, limit: Int = 10): List<Match> {
        val safeLimit = limit.coerceIn(1, 20)
        val ownerSkills = MayraOwnerBiodataPortfolio.skillNames()
        return MayraOwnerBiodataPortfolio.opportunities()
            .filter { if (track == Track.NO_CV_MICRO_WORK) !it.cvRequired else it.cvRequired }
            .map { opportunity ->
                val hits = opportunity.skillNames.filter(ownerSkills::contains)
                val score = (hits.size * 100 / opportunity.skillNames.size.coerceAtLeast(1)).coerceIn(0, 100)
                Match(opportunity.title, opportunity.category, track, score, hits, opportunity.guidance)
            }
            .sortedByDescending { it.score }
            .take(safeLimit)
    }

    fun rule(): String =
        "CV-first matching uses the Owner Career Profile. No-CV micro-work matching uses evidenced skills from the Owner CV/portfolio. Mayra may research and recommend opportunities; Owner approval remains required before account actions, applications, submissions or paid work."
}
