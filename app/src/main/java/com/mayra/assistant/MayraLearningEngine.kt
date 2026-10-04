package com.mayra.assistant

object MayraLearningEngine {
    enum class Status { DISCOVERED, CROSS_CHECKED, TESTED, VERIFIED, APPLIED }
    data class KnowledgeItem(
        val id: String, val topic: String, val claim: String, val sources: List<String> = emptyList(),
        val status: Status = Status.DISCOVERED, val skill: String = "", val evidence: String = ""
    )
    data class SkillMastery(val skill: String, val level: Int, val evidence: String)

    fun advance(item: KnowledgeItem, sourceCount: Int = item.sources.size, testPassed: Boolean = false): KnowledgeItem {
        val next = when {
            item.status == Status.DISCOVERED && sourceCount >= 2 -> Status.CROSS_CHECKED
            item.status == Status.CROSS_CHECKED && testPassed -> Status.TESTED
            item.status == Status.TESTED && item.evidence.isNotBlank() -> Status.VERIFIED
            item.status == Status.VERIFIED -> Status.APPLIED
            else -> item.status
        }
        return item.copy(status = next)
    }

    fun mastery(item: KnowledgeItem): SkillMastery? =
        if (item.status >= Status.VERIFIED && item.skill.isNotBlank() && item.evidence.isNotBlank())
            SkillMastery(item.skill, if (item.status == Status.APPLIED) 5 else 4, item.evidence)
        else null

    fun rule(): String =
        "Discover → Cross-check → Test → Verify → Save Knowledge → Apply. Verified skill-এর জন্য evidence বাধ্যতামূলক; নতুন qualification বানানো যাবে না."
}
