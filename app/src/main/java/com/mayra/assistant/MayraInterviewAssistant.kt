package com.mayra.assistant

object MayraInterviewAssistant {
    enum class Mode { PREPARE, PRACTICE, REVIEW }
    data class Session(val role: String, val mode: Mode, val questions: List<String>, val checklist: List<String>)

    fun start(role: String, mode: Mode = Mode.PREPARE): Session {
        val safeRole = role.trim().ifBlank { "General Job Interview" }
        return Session(safeRole, mode, listOf(
            "নিজের পরিচয় ও relevant experience কীভাবে সংক্ষেপে বলবেন?",
            "এই role-এর জন্য আপনার strongest skill কোনটি?",
            "কোনো কঠিন problem কীভাবে সমাধান করেছেন?",
            "কেন এই role/company আপনার জন্য উপযুক্ত?",
            "আপনার শেখার বা skill-gap পূরণের পরিকল্পনা কী?"
        ), listOf(
            "CV-এর তথ্যের সঙ্গে উত্তর মিলিয়ে নিন",
            "বাস্তব experience ছাড়া কোনো claim করবেন না",
            "প্রশ্ন বুঝে সংক্ষিপ্ত, পরিষ্কার উত্তর দিন",
            "Interview-এর সময় Mayra-এর impersonation বা hidden assistance ব্যবহার করবেন না"
        ))
    }

    fun evaluateAnswer(question: String, answer: String): String {
        if (answer.isBlank()) return "উত্তর খালি। STAR structure বা একটি বাস্তব উদাহরণ ব্যবহার করুন."
        return when {
            answer.trim().length < 40 -> "উত্তরটি খুব সংক্ষিপ্ত। বাস্তব উদাহরণ, আপনার ভূমিকা এবং ফলাফল যোগ করুন."
            answer.trim().length > 900 -> "উত্তরটি দীর্ঘ। মূল point, action এবং result রেখে সংক্ষিপ্ত করুন."
            else -> "উত্তরটি practice-এর জন্য গ্রহণযোগ্য। বাস্তব তথ্য বজায় রেখে আরও নির্দিষ্ট করুন."
        }
    }
}
