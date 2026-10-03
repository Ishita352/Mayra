package com.mayra.assistant

object OwnerSkillKnowledge {
    data class Skill(
        val id: String,
        val name: String,
        val keywords: List<String>,
        val workflow: String
    )

    val skills = listOf(
        Skill("word", "MS Word & document work", listOf("word", "document", "ডকুমেন্ট"), "Create → format → review → quality-check → deliver"),
        Skill("excel", "MS Excel & spreadsheet work", listOf("excel", "spreadsheet", "xlsx", "এক্সেল", "স্প্রেডশিট"), "Enter → clean → calculate → analyze → validate → report"),
        Skill("tally", "Tally & accounting-data work", listOf("tally", "accounting", "invoice", "inventory"), "Source check → enter → validate → reconcile → report"),
        Skill("data", "Data entry & organization", listOf("data entry", "data", "ডাটা", "ডেটা"), "Understand fields → enter → validate → deduplicate → QA"),
        Skill("research", "Web research", listOf("web research", "research", "রিসার্চ"), "Define → search → cross-check → source/date → summarize"),
        Skill("maps", "Google Maps verification & exact pin work", listOf("google maps", "maps", "pin", "address", "ম্যাপ", "পিন"), "Address → map match → inspect context → confidence → QA"),
        Skill("customer_service", "Customer service & support", listOf("customer", "support", "live chat", "email support"), "Listen → clarify → solve/escalate → follow-up → record"),
        Skill("translation", "Bengali/Hindi/English & Hindi-to-Bengali translation", listOf("translate", "translation", "অনুবাদ", "hindi", "bengali"), "Understand → translate → preserve meaning → review"),
        Skill("content", "Content & paragraph writing", listOf("content writing", "paragraph", "article", "কনটেন্ট"), "Purpose → outline → draft → fact-check → edit"),
        Skill("practical_projects", "School/college practical project creation", listOf("practical project", "school project", "college project", "প্র্যাকটিক্যাল প্রজেক্ট"), "Topic → structure → material → project content → format → review"),
        Skill("story", "Story/short-film/story-content creation", listOf("story", "short story", "short film", "গল্প"), "Premise → characters → scenes → draft → revise"),
        Skill("audio", "Audio-to-text & audio language conversion", listOf("audio writing", "transcription", "audio to text", "অডিও"), "Listen → transcribe → translate if needed → compare → QA"),
        Skill("video_writing", "Video/story content supply", listOf("video writing", "video story", "ভিডিও রাইটিং"), "Brief → story idea → structure → deliver content"),
        Skill("video_editing", "YouTube/creator video editing", listOf("video editing", "youtube editing", "ভিডিও এডিটিং", "youtube"), "Organize → cut → sequence → audio → graphics → QA → export"),
        Skill("photo", "Photo editing & digital content", listOf("photo editing", "image editing", "ফটো এডিটিং"), "Select → edit → quality-check → export"),
        Skill("ai_training", "AI training & content validation", listOf("ai training", "prompt", "content validation"), "Read rubric → evaluate → label → QA → report"),
        Skill("mturk", "Amazon Mechanical Turk microtask experience (Owner-reported, ~2 years)", listOf("mturk", "mechanical turk", "microtask", "human intelligence task", "hit"), "Apply prior Owner experience → read task rules → complete accurately → QA → submit"),
        Skill("microtask_transfer", "Transferable MTurk-style task skills", listOf("microtask", "data labeling", "data collection", "image labeling", "text classification", "entity matching", "deduplication", "transcription", "ai evaluation", "human-in-the-loop"), "Map proven Owner microtask experience to legitimate platforms → verify task rules → compare pay/time → QA → Owner approval"),

        Skill("content_review", "Content/photo quality review", listOf("content review", "photo selection", "quality review"), "Criteria → inspect → classify → consistency-check"),
        Skill("construction", "Construction-site/work supervision", listOf("construction", "site supervision", "construction supervisor"), "Observe → coordinate → follow work plan → report"),
        Skill("production", "Production supervision", listOf("production supervisor", "production supervision"), "Use only Owner-confirmed duties; ask before expanding scope")
    )

    fun findMatches(request: String): List<Skill> {
        val text = request.trim().lowercase()
        if (text.isBlank()) return emptyList()
        return skills.filter { skill -> skill.keywords.any { text.contains(it.lowercase()) } }
    }

    fun learningModel(): String =
        "Discover → Research → Cross-check → Practice/Sandbox → Verify → Backup → Update Knowledge → Re-test → Notify Owner"

    fun summary(): String =
        "Owner CV Skill Engine: office, data, research, Maps, support, language, writing, media, AI QA, construction and supervision."
}
