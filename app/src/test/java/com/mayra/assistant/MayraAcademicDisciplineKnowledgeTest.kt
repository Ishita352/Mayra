package com.mayra.assistant

import org.junit.Assert.assertTrue
import org.junit.Test

class MayraAcademicDisciplineKnowledgeTest {
    @Test fun coversMajorAcademicLines() {
        val names = MayraAcademicDisciplineKnowledge.departments().map { it.name }
        assertTrue(names.contains("Medicine"))
        assertTrue(names.contains("Civil Engineering"))
        assertTrue(names.contains("Computer Science"))
        assertTrue(names.contains("Mathematics"))
        assertTrue(names.contains("Commerce"))
        assertTrue(names.contains("Law"))
        assertTrue(names.contains("Agriculture"))
    }

    @Test fun createsNotePlanForAnyDepartment() {
        val plan = MayraAcademicDisciplineKnowledge.planNote(
            MayraAcademicDisciplineKnowledge.NoteRequest(
                "Medicine", "Anatomy", "Human heart",
                MayraEducationIntelligence.AcademicLevel.UNDERGRADUATE
            )
        )
        assertTrue(plan.sections.isNotEmpty())
        assertTrue(plan.studyOutputs.contains("Short notes"))
        assertTrue(plan.sourceVerificationRequired)
    }

    @Test fun rulePreventsInventedProfessionalOrExamFacts() {
        assertTrue(MayraAcademicDisciplineKnowledge.rule().contains("must not invent"))
    }
}
