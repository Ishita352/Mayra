package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraLegalGuidanceEngineTest {
    @Test
    fun officialCorpusIncludesCurrentCoreCriminalLaws() {
        val corpus = MayraLegalGuidanceEngine.officialCorpus()
        assertTrue(corpus.criminalLaw.any { it.startsWith("Bharatiya Nyaya Sanhita") })
        assertTrue(corpus.criminalLaw.any { it.startsWith("Bharatiya Nagarik Suraksha Sanhita") })
        assertTrue(corpus.evidenceLaw.any { it.startsWith("Bharatiya Sakshya Adhiniyam") })
    }

    @Test
    fun emptyFactsAreNotGivenAFalseLegalConclusion() {
        val result = MayraLegalGuidanceEngine.assess(
            MayraLegalGuidanceEngine.Matter("", MayraLegalGuidanceEngine.MatterType.CRIMINAL),
            verifiedSourcesAvailable = false
        )
        assertEquals("INSUFFICIENT_FACTS", result.lawfulStatus)
        assertTrue(result.advocateRequired)
    }

    @Test
    fun arrestStageIsUrgentAndProvidesLawfulPath() {
        val result = MayraLegalGuidanceEngine.assess(
            MayraLegalGuidanceEngine.Matter(
                description = "Police notice received",
                type = MayraLegalGuidanceEngine.MatterType.POLICE,
                stage = "arrest/bail"
            ),
            verifiedSourcesAvailable = true
        )
        assertEquals(MayraLegalGuidanceEngine.Risk.URGENT, result.urgency)
        assertTrue(result.possibleRightsOrRemedies.any { it.contains("bail", ignoreCase = true) })
    }

    @Test
    fun unlawfulRequestsAreFlagged() {
        assertTrue(MayraLegalGuidanceEngine.isPotentiallyIllegal("forge evidence"))
        assertTrue(MayraLegalGuidanceEngine.isPotentiallyIllegal("hack police"))
        assertFalse(MayraLegalGuidanceEngine.isPotentiallyIllegal("explain bail procedure"))
    }
}

    @Test
    fun lawfulDefenseRoutesIncludeIngredientsProcedureJurisdictionEvidenceAndRemedies() {
        val routes = MayraLegalGuidanceEngine.lawfulDefenseRoutes(
            MayraLegalGuidanceEngine.Matter("A case needs review", MayraLegalGuidanceEngine.MatterType.CRIMINAL)
        )
        assertTrue(routes.size >= 5)
        assertTrue(routes.any { it.route.contains("ingredient", ignoreCase = true) })
        assertTrue(routes.any { it.route.contains("procedural", ignoreCase = true) })
        assertTrue(routes.any { it.route.contains("jurisdiction", ignoreCase = true) })
        assertTrue(routes.any { it.route.contains("evidentiary", ignoreCase = true) })
        assertTrue(routes.any { it.route.contains("post-order", ignoreCase = true) })
    }
