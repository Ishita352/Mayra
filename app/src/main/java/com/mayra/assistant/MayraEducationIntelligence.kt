package com.mayra.assistant

/**
 * Education and academic intelligence layer for Mayra.
 *
 * Covers university exam/syllabus information, study notes, explanations,
 * revision support and broad academic disciplines. Live notices must be
 * refreshed from authoritative sources before being treated as current.
 */
object MayraEducationIntelligence {
    enum class AcademicLevel { SCHOOL, UNDERGRADUATE, POSTGRADUATE, RESEARCH, PROFESSIONAL, COMPETITIVE }
    enum class Discipline {
        ARTS, HUMANITIES, COMMERCE, MANAGEMENT, LAW, EDUCATION, SCIENCE,
        ENGINEERING, COMPUTER_SCIENCE, MEDICINE, HEALTH, AGRICULTURE,
        SOCIAL_SCIENCE, LANGUAGES, FINE_ARTS, MUSIC, PHYSICAL_EDUCATION,
        VOCATIONAL, INTERDISCIPLINARY, OTHER
    }
    enum class NoticeType { EXAM_ROUTINE, SYLLABUS, ADMISSION, FORM_FILLUP, RESULT, REVIEW, CENTRE_LIST, OTHER }

    data class UniversityNotice(
        val university: String,
        val noticeType: NoticeType,
        val title: String,
        val sourceUrl: String,
        val publishedAtMs: Long
    )

    data class StudyNote(
        val subject: String,
        val topic: String,
        val level: AcademicLevel,
        val note: String,
        val keyPoints: List<String>,
        val sourceLabel: String? = null
    )

    data class Explanation(
        val topic: String,
        val simpleExplanation: String,
        val examples: List<String>,
        val practiceQuestions: List<String>
    )

    fun validateNotice(notice: UniversityNotice): Boolean =
        notice.university.isNotBlank() &&
            notice.title.isNotBlank() &&
            notice.sourceUrl.startsWith("https://") &&
            notice.publishedAtMs > 0

    fun createNote(
        subject: String,
        topic: String,
        level: AcademicLevel,
        note: String,
        keyPoints: List<String>,
        sourceLabel: String? = null
    ): StudyNote = StudyNote(
        subject.trim(), topic.trim(), level, note.trim(),
        keyPoints.filter { it.isNotBlank() }, sourceLabel
    )

    fun explain(
        topic: String,
        simpleExplanation: String,
        examples: List<String>,
        practiceQuestions: List<String>
    ): Explanation = Explanation(
        topic.trim(), simpleExplanation.trim(),
        examples.filter { it.isNotBlank() },
        practiceQuestions.filter { it.isNotBlank() }
    )

    fun supportedDisciplines(): List<Discipline> = Discipline.entries.toList()

    fun burdwanOfficialSources(): List<String> = listOf(
        "https://www.buruniv.ac.in/",
        "https://www.buruniv.ac.in/Demo/Template.php?menu=NOT_EXAM&submenu=EXAM_ALL"
    )

    fun currentNoticeRule(): String =
        "Burdwan University exam dates, routines, syllabi, forms, results, review/scrutiny, centre lists and admission notices must be checked against the latest official university notice before being presented as current."

    fun studyRule(): String =
        "Mayra may create notes, summaries, examples, revision plans and explanations for any supported discipline, but must distinguish source-backed facts from explanation or interpretation and must not invent syllabus, marks, exam dates, references or qualifications."
}
