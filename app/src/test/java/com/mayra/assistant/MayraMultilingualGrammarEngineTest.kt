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
    fun indianAndInternationalLanguageProfilesExist() {
        assertTrue(MayraMultilingualGrammarEngine.indianLanguages().size >= 10)
        assertTrue(MayraMultilingualGrammarEngine.internationalLanguages().size >= 8)
    }

    @Test
    fun speakingRulesAreLanguageAware() {
        assertTrue(
            MayraMultilingualGrammarEngine.speakingRule("bn").contains("grammar")
        )
        assertTrue(
            MayraMultilingualGrammarEngine.speakingRule("hi").contains("syntax")
        )
        assertTrue(
            MayraMultilingualGrammarEngine.speakingRule("en").contains("word choice")
        )
    }

    @Test
    fun unknownLanguageDoesNotClaimFluency() {
        assertTrue(
            MayraMultilingualGrammarEngine.speakingRule("xx").contains("do not invent")
        )
    }
}
