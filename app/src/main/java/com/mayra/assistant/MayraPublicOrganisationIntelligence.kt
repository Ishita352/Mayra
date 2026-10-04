package com.mayra.assistant

/** Public-source organisation intelligence for lawful monitoring of political and social organisations. */
object MayraPublicOrganisationIntelligence {
    enum class OrganisationType { RSS, BJP, HINDU_ORGANISATION, OTHER_PUBLIC_ORGANISATION }
    enum class Level { BOOTH, MANDAL, DISTRICT, REGIONAL, STATE, NATIONAL, OTHER }
    enum class EventType { APPOINTMENT, REMOVAL, TRANSFER, RESIGNATION, MEETING, PUBLIC_NOTICE, OTHER }

    data class Organisation(
        val name: String,
        val type: OrganisationType,
        val state: String? = null,
        val officialSource: String? = null
    )

    data class OfficeHolder(
        val organisation: String,
        val personName: String,
        val role: String,
        val level: Level,
        val area: String?,
        val sourceUrl: String,
        val effectiveFromMs: Long?,
        val effectiveToMs: Long? = null,
        val publicBackground: String? = null
    )

    data class Update(
        val organisation: String,
        val eventType: EventType,
        val description: String,
        val sourceUrl: String,
        val publishedAtMs: Long
    )

    data class SuccessionSignal(
        val personName: String,
        val role: String,
        val basis: List<String>,
        val confidence: Int
    )

    fun acceptHolder(holder: OfficeHolder): Boolean =
        holder.organisation.isNotBlank() &&
            holder.personName.isNotBlank() &&
            holder.role.isNotBlank() &&
            holder.sourceUrl.startsWith("https://") &&
            (holder.publicBackground == null || holder.publicBackground.length <= 5000)

    fun acceptUpdate(update: Update): Boolean =
        update.organisation.isNotBlank() &&
            update.description.isNotBlank() &&
            update.sourceUrl.startsWith("https://") &&
            update.publishedAtMs > 0L

    fun successionRule(): String =
        "Report likely future office-holders only as evidence-based possibilities using public appointment history, official announcements and credible reporting; never present speculation as a confirmed appointment."

    fun privacyRule(): String =
        "Collect only relevant information already made public by authoritative or credible sources; do not collect passwords, private addresses, private contact details, non-public political affiliation data, or covertly obtained personal information."

    fun monitoringRule(): String =
        "Track public organisational changes at booth, mandal, district, regional, state and national levels when reliable public sources exist; preserve source URL and publication time, detect changes, and notify the Owner after verification."
}
