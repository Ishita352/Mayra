package com.mayra.assistant

import android.content.Context
import java.util.Locale

class MayraInterviewAssistantEngine(private val context: Context) {
    data class Profile(val role: String, val company: String, val jobDescription: String, val resume: String)
    data class Question(val category: String, val text: String, val why: String)
    data class Evaluation(val score: Int, val strengths: List<String>, val improvements: List<String>, val nextStep: String)
    private val prefs = context.getSharedPreferences("mayra_interview", Context.MODE_PRIVATE)

    fun buildQuestionSet(profile: Profile): List<Question> {
        val role = profile.role.ifBlank { "এই role" }
        val company = profile.company.ifBlank { "এই company" }
        val questions = mutableListOf(
            Question("HR", "Tell me about yourself and why you are interested in $role.", "Tests concise career story and motivation."),
            Question("HR", "Why do you want to work with $company?", "Tests company motivation and preparation."),
            Question("Behavioral", "Tell me about a difficult problem you solved. What did you do and what was the result?", "Tests ownership and problem solving."),
            Question("Behavioral", "Describe a time you made a mistake. What did you learn?", "Tests accountability and learning."),
            Question("Role", "Which skills from your experience are most relevant to this $role?", "Tests job-description alignment."),
            Question("Role", "How would you prioritize competing tasks with a tight deadline?", "Tests practical judgment.")
        )
        val jd = profile.jobDescription.lowercase(Locale.ROOT)
        if (listOf("excel", "spreadsheet", "data", "analytics").any { jd.contains(it) })
            questions += Question("Technical", "How would you clean, validate and analyze a spreadsheet before reporting results?", "Targets data/Excel requirements.")
        if (listOf("powerpoint", "presentation", "communication").any { jd.contains(it) })
            questions += Question("Technical", "How would you turn complex information into a clear presentation for a non-technical audience?", "Targets presentation and communication skills.")
        if (listOf("photoshop", "design", "creative").any { jd.contains(it) })
            questions += Question("Technical", "Walk me through your design workflow from brief to final delivery.", "Targets practical creative workflow.")
        if (listOf("coding", "developer", "software", "programming").any { jd.contains(it) })
            questions += Question("Coding", "Explain a technical problem you solved, including your approach, trade-offs and testing.", "Tests technical reasoning.")
        return questions
    }

    fun evaluateAnswer(question: String, answer: String): Evaluation {
        val text = answer.trim()
        if (text.isEmpty()) return Evaluation(0, emptyList(), listOf("Answerটি খালি। অন্তত 30–60 seconds-এর structured answer দিন."), "STAR structure ব্যবহার করে আবার উত্তর দিন.")
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        val lower = text.lowercase(Locale.ROOT)
        val strengths = mutableListOf<String>()
        val improvements = mutableListOf<String>()
        var score = 45
        if (words.size >= 35) { score += 10; strengths += "উত্তরটি যথেষ্ট বিস্তারিত।" } else improvements += "আরও context ও concrete detail যোগ করুন."
        if (listOf("because", "therefore", "so", "কারণ", "তাই", "ফলে").any { lower.contains(it) }) { score += 8; strengths += "কারণ/ফলাফল ব্যাখ্যা করেছেন।" } else improvements += "কেন সিদ্ধান্তটি নিয়েছিলেন তা স্পষ্ট করুন."
        if (listOf("I", "আমি", "আমরা", "my", "আমার").any { lower.contains(it) }) { score += 5; strengths += "নিজের ভূমিকা বোঝা যাচ্ছে।" }
        if (listOf("result", "impact", "improved", "achieved", "ফলাফল", "উন্নতি", "সফল").any { lower.contains(it) }) { score += 12; strengths += "Result/impact উল্লেখ আছে।" } else improvements += "শেষে measurable result বা impact যোগ করুন."
        if (listOf("situation", "task", "action", "result", "পরিস্থিতি", "কাজ", "পদক্ষেপ", "ফলাফল").count { lower.contains(it) } >= 2) { score += 10; strengths += "STAR-style structure-এর কিছু অংশ আছে।" } else improvements += "STAR: Situation → Task → Action → Result ধরে উত্তর সাজান."
        if (words.size > 180) improvements += "উত্তরটি সংক্ষিপ্ত করুন; মূল point আগে বলুন."
        score = score.coerceIn(0, 100)
        return Evaluation(score, strengths.distinct(), improvements.distinct(), if (score >= 75) "ভালো উত্তর। এখন follow-up question practice করুন." else "একবার rewrite করে আবার practice করুন.")
    }

    fun savePractice(question: String, answer: String, evaluation: Evaluation) {
        val count = prefs.getInt("practice_count", 0)
        prefs.edit().putInt("practice_count", count + 1).putInt("score_total", prefs.getInt("score_total", 0) + evaluation.score)
            .putString("last_question", question).putString("last_answer", answer).putInt("last_score", evaluation.score).apply()
    }

    fun progressSummary(): String {
        val count = prefs.getInt("practice_count", 0)
        val avg = if (count == 0) 0 else prefs.getInt("score_total", 0) / count
        return "Interview practice: " + count + " session(s) • Average score: " + avg + "/100 • Last score: " + prefs.getInt("last_score", 0) + "/100"
    }
}
