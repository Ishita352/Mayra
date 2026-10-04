package com.mayra.assistant

data class MayraCareerProfile(
    val fullName: String = "", val headline: String = "", val location: String = "",
    val education: String = "", val experience: String = "", val skills: String = "",
    val certificates: String = "", val languages: String = "", val preferredRoles: String = "",
    val preferredWorkMode: String = ""
) {
    fun missingRequiredFields(): List<String> = buildList {
        if (fullName.isBlank()) add("Full name"); if (skills.isBlank()) add("Skills"); if (education.isBlank()) add("Education")
    }
    fun isReadyForCv(): Boolean = missingRequiredFields().isEmpty()
    fun toCvText(): String = buildString {
        appendLine(fullName); if (headline.isNotBlank()) appendLine(headline); if (location.isNotBlank()) appendLine(location); appendLine()
        appendSection("Education", education); appendSection("Experience", experience); appendSection("Skills", skills)
        appendSection("Certificates", certificates); appendSection("Languages", languages); appendSection("Preferred Roles", preferredRoles); appendSection("Preferred Work Mode", preferredWorkMode)
    }
    private fun StringBuilder.appendSection(title: String, value: String) { if (value.isNotBlank()) { appendLine(title); appendLine(value); appendLine() } }
}

object MayraCareerProfileStore {
    private const val KEY = "owner.career.profile"
    interface Store { fun read(key: String): String?; fun write(key: String, value: String) }
    fun save(store: Store, profile: MayraCareerProfile) {
        val fields = listOf(profile.fullName, profile.headline, profile.location, profile.education, profile.experience, profile.skills, profile.certificates, profile.languages, profile.preferredRoles, profile.preferredWorkMode)
        require(fields.all { !it.contains('|') }); store.write(KEY, fields.joinToString("|"))
    }
    fun load(store: Store): MayraCareerProfile? {
        val fields = (store.read(KEY) ?: return null).split('|'); if (fields.size != 10) return null
        return MayraCareerProfile(fields[0],fields[1],fields[2],fields[3],fields[4],fields[5],fields[6],fields[7],fields[8],fields[9])
    }
}