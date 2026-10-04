package com.mayra.assistant

import kotlin.test.Test
import kotlin.test.assertTrue

class MayraEducationIntelligenceTest {
    @Test
    fun supportsAcademicDisciplines() {
        val items = MayraEducationIntelligence.supportedDisciplines()
        assertTrue(items.contains(MayraEducationIntelligence.Discipline.SCIENCE))
        assertTrue(items.contains(MayraEducationIntelligence.Discipline.COMMERCE))
        assertTrue(items.contains(MayraEducationIntelligence.Discipline.LAW))
    }

    @Test
    fun validatesOfficialNotice() {
        val notice = MayraEducationIntelligence.UniversityNotice(
            "The University of Burdwan",
            MayraEducationIntelligence.NoticeType.EXAM_ROUTINE,
            "UG examination programme",
            "https://www.buruniv.ac.in/",
            1L
        )
        assertTrue(MayraEducationIntelligence.validateNotice(notice))
    }

    @Test
    fun createsStudyContent() {
        val note = MayraEducationIntelligence.createNote(
            "Physics", "Motion", MayraEducationIntelligence.AcademicLevel.UNDERGRADUATE,
            "Short note", listOf("Velocity", "Acceleration")
        )
        assertTrue(note.keyPoints.isNotEmpty())
        val explanation = MayraEducationIntelligence.explain(
            "Velocity", "Rate of change of displacement.",
            listOf("Moving car"), listOf("Define velocity.")
        )
        assertTrue(explanation.examples.isNotEmpty())
    }
}
