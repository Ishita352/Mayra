package com.mayra.assistant

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MayraHinduScriptureKnowledgeTest {
    @Test fun allFourVedasAreRepresented() {
        assertEquals(
            listOf("Rigveda", "Yajurveda", "Samaveda", "Atharvaveda"),
            MayraHinduScriptureKnowledge.divisionsOfVeda()
        )
        assertEquals(4, MayraHinduScriptureKnowledge.vedas().size)
    }

    @Test fun majorTraditionsAndAyurvedaAreRepresented() {
        val traditions = MayraHinduScriptureKnowledge.allCorpus().map { it.tradition }.toSet()
        assertTrue(MayraHinduScriptureKnowledge.Tradition.ITIHASA in traditions)
        assertTrue(MayraHinduScriptureKnowledge.Tradition.GITA in traditions)
        assertTrue(MayraHinduScriptureKnowledge.Tradition.PURANA in traditions)
        assertTrue(MayraHinduScriptureKnowledge.Tradition.DHARMASHASTRA in traditions)
        assertTrue(MayraHinduScriptureKnowledge.Tradition.DARSHANA in traditions)
        assertTrue(MayraHinduScriptureKnowledge.Tradition.AYURVEDA in traditions)
    }

    @Test fun knowledgeRulePreservesContext() {
        val rule = MayraHinduScriptureKnowledge.rule()
        assertTrue(rule.contains("recension"))
        assertTrue(rule.contains("evidence-based medical care"))
    }
}
