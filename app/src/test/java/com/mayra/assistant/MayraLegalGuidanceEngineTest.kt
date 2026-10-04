package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraLegalGuidanceEngineTest {
    @Test fun officialCorpusIncludesCurrentCoreCriminalLaws() {
        val c=MayraLegalGuidanceEngine.officialCorpus()
        assertTrue(c.criminalLaw.any{it.startsWith("Bharatiya Nyaya Sanhita")})
        assertTrue(c.criminalLaw.any{it.startsWith("Bharatiya Nagarik Suraksha Sanhita")})
        assertTrue(c.evidenceLaw.any{it.startsWith("Bharatiya Sakshya Adhiniyam")})
    }
    @Test fun emptyFactsAreNotGivenAFalseLegalConclusion() {
        val r=MayraLegalGuidanceEngine.assess(MayraLegalGuidanceEngine.Matter("",MayraLegalGuidanceEngine.MatterType.CRIMINAL),false)
        assertEquals("INSUFFICIENT_FACTS",r.lawfulStatus); assertTrue(r.advocateRequired)
    }
    @Test fun arrestStageIsUrgentAndProvidesLawfulPath() {
        val r=MayraLegalGuidanceEngine.assess(MayraLegalGuidanceEngine.Matter("Police notice received",MayraLegalGuidanceEngine.MatterType.POLICE,stage="arrest/bail"),true)
        assertEquals(MayraLegalGuidanceEngine.Risk.URGENT,r.urgency); assertTrue(r.possibleRightsOrRemedies.any{it.contains("bail",true)})
    }
    @Test fun unlawfulRequestsAreFlagged() {
        assertTrue(MayraLegalGuidanceEngine.isPotentiallyIllegal("forge evidence"))
        assertTrue(MayraLegalGuidanceEngine.isPotentiallyIllegal("hack police"))
        assertFalse(MayraLegalGuidanceEngine.isPotentiallyIllegal("explain bail procedure"))
    }
    @Test fun lawfulDefenseRoutesIncludeIngredientsProcedureJurisdictionEvidenceAndRemedies() {
        val routes=MayraLegalGuidanceEngine.lawfulDefenseRoutes(MayraLegalGuidanceEngine.Matter("A case needs review",MayraLegalGuidanceEngine.MatterType.CRIMINAL))
        assertTrue(routes.size>=5)
        assertTrue(routes.any{it.route.contains("ingredient",true)})
        assertTrue(routes.any{it.route.contains("procedural",true)})
        assertTrue(routes.any{it.route.contains("jurisdiction",true)})
        assertTrue(routes.any{it.route.contains("evidentiary",true)})
        assertTrue(routes.any{it.route.contains("post-order",true)})
    }
}