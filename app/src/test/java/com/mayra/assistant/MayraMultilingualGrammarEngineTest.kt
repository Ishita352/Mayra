package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraMultilingualGrammarEngineTest {
    @Test
    fun primaryLanguagesAreBengaliHindiEnglish() {
        assertEquals(
            listOf("Bengali", "Hindi", "English"),
            MayraMultilingualGrammarEngine.primaryLanguages().map { it.name }
        )
    }

    @Test
    fun primaryLanguagesRequireComprehensiveCompetence() {
        listOf("bn", "hi", "en").forEach {
            assertTrue(MayraMultilingualGrammarEngine.hasComprehensivePrimaryCompetence(it))
        }
        assertTrue(MayraMultilingualGrammarEngine.primaryCompetenceAreas().size >= 10)
    }

    @Test
    fun indianAndInternationalLanguageProfilesExist() {
        assertTrue(MayraMultilingualGrammarEngine.indianLanguages().size >= 10)
        assertTrue(MayraMultilingualGrammarEngine.internationalLanguages().size >= 8)
    }

    @Test
    fun primarySpeakingRuleRequiresGrammarAndVocabulary() {
        val rule = MayraMultilingualGrammarEngine.speakingRule("bn")
        assertTrue(rule.contains("grammar"))
        assertTrue(rule.contains("vocabulary"))
        assertTrue(rule.contains("natural conversational usage"))
    }

    @Test
    fun conversationRuleMakesPrimaryCompetenceMandatory() {
        val rule = MayraMultilingualGrammarEngine.conversationRule()
        assertTrue(rule.contains("comprehensive grammar and vocabulary competence is mandatory"))
    }

    @Test
    fun unknownLanguageDoesNotClaimFluency() {
        assertTrue(
            MayraMultilingualGrammarEngine.speakingRule("xx").contains("do not invent")
        )
    }
}
