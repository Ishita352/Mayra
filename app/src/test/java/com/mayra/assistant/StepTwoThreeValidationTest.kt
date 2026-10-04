package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream

class StepTwoThreeValidationTest {
    @Test fun csvIsParsedAndValidated() {
        val data = SpreadsheetDocumentEngine.readDelimited(ByteArrayInputStream("Name,Score\nA,10\nB,20\n".toByteArray()))
        val v = SpreadsheetDocumentEngine.validate(data)
        assertTrue(v.valid); assertEquals(3, v.rows); assertEquals(2, v.columns)
    }
    @Test fun unevenSpreadsheetIsRejected() {
        val data = SpreadsheetDocumentEngine.readDelimited(ByteArrayInputStream("A,B\n1\n".toByteArray()))
        assertFalse(SpreadsheetDocumentEngine.validate(data).valid)
    }
    @Test fun careerProfileRequiresOwnerFacts() {
        val profile = MayraCareerProfile(fullName="Owner", education="Education", skills="Kotlin")
        assertTrue(profile.isReadyForCv()); assertTrue(profile.toCvText().contains("Kotlin"))
    }
    @Test fun careerProfileRoundTrips() {
        val values = mutableMapOf<String,String>()
        val store = object : MayraCareerProfileStore.Store { override fun read(key:String)=values[key]; override fun write(key:String,value:String){values[key]=value} }
        val profile = MayraCareerProfile(fullName="Owner", education="Education", skills="Kotlin", preferredRoles="Android")
        MayraCareerProfileStore.save(store, profile)
        assertEquals(profile, MayraCareerProfileStore.load(store))
    }
    @Test fun careerPlannerRecognizesProfileActions() {
        val plan = CareerProfileWorkflow.plan("create CV and identify skill gap")
        assertTrue(plan.recognized); assertTrue(CareerProfileWorkflow.Action.BUILD_CV in plan.actions); assertTrue(CareerProfileWorkflow.Action.IDENTIFY_SKILL_GAP in plan.actions)
    }
}