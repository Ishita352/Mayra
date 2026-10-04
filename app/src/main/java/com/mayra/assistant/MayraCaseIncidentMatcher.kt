package com.mayra.assistant

object MayraCaseIncidentMatcher {
    data class Incident(
        val description:String,
        val place:String? = null,
        val dateLabel:String? = null
    )
    data class Candidate(
        val caseId:String?,
        val policeStation:String?,
        val court:String?,
        val status:String?,
        val sourceUrl:String,
        val matchConfidence:Int,
        val evidence:String
    )
    data class Assessment(
        val candidates:List<Candidate>,
        val likelyPublicCaseExists:Boolean,
        val nextPublicStage:String?
    )

    fun match(incident:Incident, candidates:List<Candidate>):Assessment {
        val valid=candidates.filter{
            it.sourceUrl.isNotBlank() && it.matchConfidence in 0..100
        }.sortedByDescending{it.matchConfidence}
        return Assessment(
            valid,
            valid.any{it.matchConfidence >= 70},
            valid.firstOrNull()?.status?.let{ "Publicly reported status: $it; next procedural stage must be verified from a new official update." }
        )
    }

    fun rule() =
        "When given an incident without a case number, Mayra may compare its description, place and date with publicly available official case/FIR/court information and return ranked possible matches, police station, court, status and source evidence. A match is not proof of identity, guilt or case existence. Case outcome is never predicted as fact."
}
