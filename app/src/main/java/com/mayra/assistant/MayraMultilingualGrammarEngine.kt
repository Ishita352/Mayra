package com.mayra.assistant

/**
 * Mayra's multilingual language, grammar and vocabulary policy.
 *
 * Bengali, Hindi and English are the three PRIMARY languages. For these
 * languages Mayra is required to target comprehensive language competence:
 * grammar, syntax, morphology, vocabulary, semantics, idioms, register,
 * agreement, punctuation, spelling and natural conversational usage.
 *
 * Additional languages have language profiles and verified-resource support.
 * The engine must never invent unsupported fluency.
 */
object MayraMultilingualGrammarEngine {
    enum class Priority { PRIMARY, INDIAN_ADDITIONAL, INTERNATIONAL_ADDITIONAL }

    enum class CompetenceArea {
        GRAMMAR,
        SYNTAX,
        MORPHOLOGY,
        VOCABULARY,
        SEMANTICS,
        WORD_CHOICE,
        SPELLING,
        PUNCTUATION,
        AGREEMENT,
        IDIOMS,
        REGISTER,
        NATURAL_CONVERSATION,
        PRAGMATICS
    }

    data class LanguageProfile(
        val code: String,
        val name: String,
        val priority: Priority,
        val grammarAware: Boolean = true,
        val comprehensivePrimaryCompetence: Boolean = false,
        val competenceAreas: Set<CompetenceArea> = emptySet()
    )

    private val primaryCompetence = setOf(
        CompetenceArea.GRAMMAR,
        CompetenceArea.SYNTAX,
        CompetenceArea.MORPHOLOGY,
        CompetenceArea.VOCABULARY,
        CompetenceArea.SEMANTICS,
        CompetenceArea.WORD_CHOICE,
        CompetenceArea.SPELLING,
        CompetenceArea.PUNCTUATION,
        CompetenceArea.AGREEMENT,
        CompetenceArea.IDIOMS,
        CompetenceArea.REGISTER,
        CompetenceArea.NATURAL_CONVERSATION,
        CompetenceArea.PRAGMATICS
    )

    private val profiles = listOf(
        LanguageProfile("bn", "Bengali", Priority.PRIMARY, comprehensivePrimaryCompetence = true, competenceAreas = primaryCompetence),
        LanguageProfile("hi", "Hindi", Priority.PRIMARY, comprehensivePrimaryCompetence = true, competenceAreas = primaryCompetence),
        LanguageProfile("en", "English", Priority.PRIMARY, comprehensivePrimaryCompetence = true, competenceAreas = primaryCompetence),

        LanguageProfile("as", "Assamese", Priority.INDIAN_ADDITIONAL),
        LanguageProfile("gu", "Gujarati", Priority.INDIAN_ADDITIONAL),
        LanguageProfile("kn", "Kannada", Priority.INDIAN_ADDITIONAL),
        LanguageProfile("ml", "Malayalam", Priority.INDIAN_ADDITIONAL),
        LanguageProfile("mr", "Marathi", Priority.INDIAN_ADDITIONAL),
        LanguageProfile("ne", "Nepali", Priority.INDIAN_ADDITIONAL),
        LanguageProfile("or", "Odia", Priority.INDIAN_ADDITIONAL),
        LanguageProfile("pa", "Punjabi", Priority.INDIAN_ADDITIONAL),
        LanguageProfile("sa", "Sanskrit", Priority.INDIAN_ADDITIONAL),
        LanguageProfile("ta", "Tamil", Priority.INDIAN_ADDITIONAL),
        LanguageProfile("te", "Telugu", Priority.INDIAN_ADDITIONAL),
        LanguageProfile("ur", "Urdu", Priority.INDIAN_ADDITIONAL),

        LanguageProfile("ar", "Arabic", Priority.INTERNATIONAL_ADDITIONAL),
        LanguageProfile("de", "German", Priority.INTERNATIONAL_ADDITIONAL),
        LanguageProfile("es", "Spanish", Priority.INTERNATIONAL_ADDITIONAL),
        LanguageProfile("fr", "French", Priority.INTERNATIONAL_ADDITIONAL),
        LanguageProfile("it", "Italian", Priority.INTERNATIONAL_ADDITIONAL),
        LanguageProfile("ja", "Japanese", Priority.INTERNATIONAL_ADDITIONAL),
        LanguageProfile("ko", "Korean", Priority.INTERNATIONAL_ADDITIONAL),
        LanguageProfile("pt", "Portuguese", Priority.INTERNATIONAL_ADDITIONAL),
        LanguageProfile("ru", "Russian", Priority.INTERNATIONAL_ADDITIONAL),
        LanguageProfile("zh", "Mandarin Chinese", Priority.INTERNATIONAL_ADDITIONAL)
    )

    fun primaryLanguages(): List<LanguageProfile> =
        profiles.filter { it.priority == Priority.PRIMARY }

    fun indianLanguages(): List<LanguageProfile> =
        profiles.filter { it.priority == Priority.INDIAN_ADDITIONAL }

    fun internationalLanguages(): List<LanguageProfile> =
        profiles.filter { it.priority == Priority.INTERNATIONAL_ADDITIONAL }

    fun profile(code: String): LanguageProfile? =
        profiles.firstOrNull { it.code.equals(code.trim(), ignoreCase = true) }

    fun primaryCompetenceAreas(): Set<CompetenceArea> = primaryCompetence

    fun hasComprehensivePrimaryCompetence(code: String): Boolean =
        profile(code)?.let {
            it.priority == Priority.PRIMARY &&
                it.comprehensivePrimaryCompetence &&
                it.competenceAreas.containsAll(primaryCompetence)
        } ?: false

    fun speakingRule(code: String): String =
        when (profile(code)?.priority) {
            Priority.PRIMARY ->
                "MANDATORY: speak and write with comprehensive grammar, syntax, morphology, vocabulary, semantics, correct word choice, spelling, punctuation, agreement, idioms, register, pragmatics and natural conversational usage. Do not knowingly use grammatically incorrect or unnatural construction when a correct form is available."
            Priority.INDIAN_ADDITIONAL ->
                "Use the selected Indian language's verified grammar, vocabulary and natural usage; preserve meaning and context."
            Priority.INTERNATIONAL_ADDITIONAL ->
                "Use the selected international language's verified grammar, vocabulary and natural usage; preserve meaning and context."
            null ->
                "Identify the language first; do not invent grammar rules or claim unsupported fluency."
        }

    fun conversationRule(): String =
        "Bengali, Hindi and English are Mayra's PRIMARY languages. In all three, comprehensive grammar and vocabulary competence is mandatory for speech and text. " +
            "Mayra should select accurate words, sentence structure, agreement, idioms, register and natural conversational forms according to context. " +
            "Additional Indian and international languages use verified language resources and must not be presented as fully mastered without evidence."

    fun correctionRule(): String =
        "Before speaking or producing important text in a primary language, prefer grammatically correct, context-appropriate wording; detect and correct obvious grammar, spelling, agreement and word-choice errors."

    fun rule(): String =
        "Strong primary-language requirement: Bengali, Hindi and English must receive comprehensive grammar and vocabulary treatment across speech and text. " +
            "This includes grammar, syntax, morphology, vocabulary, semantics, word choice, spelling, punctuation, agreement, idioms, register, pragmatics and natural conversation. " +
            "Additional languages are supported through verified language resources; unsupported fluency must never be invented."
}
