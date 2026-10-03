package com.mayra.assistant

/**
 * Offline-first intent router for Mayra's core knowledge layer.
 *
 * This module never calls a paid API, performs a network request, or executes
 * a privileged action. It only classifies a user request and returns the
 * next safe workflow description. Real file/network actions are added later
 * behind the existing Owner/Master/permission gates.
 */
object CoreKnowledgeEngine {

    enum class Domain {
        DOCUMENTS,
        EXCEL,
        BIODATA,
        JOBS,
        INCOME,
        INTERVIEW,
        LEARNING,
        TEXTILE,
        SECURITY,
        OWNER_SKILLS,
        GENERAL
    }

    data class Answer(
        val domain: Domain,
        val recognized: Boolean,
        val message: String
    )

    fun answer(request: String): Answer {
        val text = request.trim().lowercase()
        if (text.isBlank()) return Answer(Domain.GENERAL, false, help())

        return when {
            containsAny(text, "pdf", "document", "ডকুমেন্ট", "পিডিএফ", "word", "docx") ->
                Answer(Domain.DOCUMENTS, true, DocumentWorkflow.plan(text).message)

            containsAny(text, "excel", "spreadsheet", "xlsx", "pivot", "এক্সেল", "স্প্রেডশিট") ->
                Answer(Domain.EXCEL, true,
                    "Excel workflow: data cleaning, formulas, lookup, Pivot Table, charts ও analysis-এর ধাপ প্রস্তুত।")

            containsAny(text, "cv", "resume", "biodata", "career", "সিভি", "বায়োডাটা", "রিজিউমে") ->
                Answer(Domain.BIODATA, true,
                    "Career profile workflow: আপনার আসল education, experience, skills ও certificates ব্যবহার করে CV/biodata প্রস্তুত করা হবে।")

            containsAny(text, "job", "freelance", "remote work", "কাজ খুঁজ", "চাকরি", "ফ্রিল্যান্স") ->
                Answer(Domain.JOBS, true,
                    "Job workflow: remote/freelance সুযোগ যাচাই → skill match → risk check → আপনার অনুমতি → application preparation।")

            containsAny(text, "income", "earning", "earn", "টাকা আয়", "আয়", "ইনকাম", "আর্নিং") ->
                Answer(Domain.INCOME, true,
                    "Income workflow: আগে free/legitimate opportunity; USD অগ্রাধিকার, INR পাশাপাশি। Mayra নিজে কোনো টাকা খরচ বা লেনদেন করবে না।")

            containsAny(text, "interview", "ইন্টারভিউ", "সাক্ষাৎকার") ->
                Answer(Domain.INTERVIEW, true,
                    "Interview workflow: Preparation, authorized assistance এবং Human-Only mode আলাদা রেখে CV-ভিত্তিক প্রস্তুতি করা হবে।")

            containsAny(text, "learn", "learning", "শিখ", "শেখ", "training", "training") ->
                Answer(Domain.LEARNING, true,
                    "Learning workflow: Discover → Research → Cross-check → Sandbox Test → Verify → Owner approval → Save Knowledge।")

            containsAny(text, "textile", "saree", "jacquard", "weave", "টেক্সটাইল", "শাড়ি", "জ্যাকার্ড") ->
                Answer(Domain.TEXTILE, true,
                    "Textile workflow: saree, jacquard, weave ও digital-design কাজের offline foundation প্রস্তুত আছে।")

            containsAny(text, "security", "cyber", "নিরাপত্তা", "সাইবার") ->
                Answer(Domain.SECURITY, true,
                    "Security workflow: শুধু authorized target-এ configuration review, inventory, defensive checks ও logs; unauthorized access নয়।")

            OwnerSkillKnowledge.findMatches(text).isNotEmpty() ->
                Answer(Domain.OWNER_SKILLS, true,
                    "Owner Skill workflow: matching CV skill → workflow → quality check → practice → legitimate job matching.")

            else -> Answer(Domain.GENERAL, false, help())
        }
    }

    fun help(): String =
        "মায়রা এখন Documents, Excel, CV/Biodata, Jobs, Income, Interview, Learning, Textile ও Security-এর কাজের ধরন চিনতে পারে।"

    private fun containsAny(text: String, vararg terms: String): Boolean =
        terms.any { text.contains(it) }
}
