package com.mayra.assistant

object MayraWestBengalPublicCaseIntelligence {
    enum class SourceType { POLICE, CYBER_CRIME, COURT, GOVERNMENT_PUBLICATION }
    enum class EventType { NEW_CASE, CASE_UPDATE, ORDER, JUDGMENT, CAUSE_LIST, PUBLIC_NOTICE }

    data class PublicSource(val name:String,val url:String,val type:SourceType,val official:Boolean)
    data class CaseEvent(
        val caseId:String,
        val eventType:EventType,
        val publishedAtMs:Long,
        val summary:String,
        val source:PublicSource
    )

    fun accept(event:CaseEvent):Boolean =
        event.caseId.isNotBlank() &&
        event.summary.isNotBlank() &&
        event.source.official &&
        event.publishedAtMs > 0

    fun track(event:CaseEvent):String =
        if (accept(event)) "PUBLIC_UPDATE_TRACKED" else "REJECTED_UNVERIFIED"

    fun rule() =
        "Track only publicly released information from official West Bengal police/cyber-crime, court/eCourts and government sources. Never infer or obtain non-public investigation, personal, confidential or restricted case information. Preserve source, publication time and uncertainty."
}
