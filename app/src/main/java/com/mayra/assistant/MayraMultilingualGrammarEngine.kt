package com.mayra.assistant

/**
 * Mayra's multilingual language/grammar policy and lightweight rule engine.
 *
 * Bengali, Hindi and English are the primary interaction languages.
 * Indian and major international languages are supported as additional
 * language profiles; this catalog does not claim native-level fluency in
 * every language without verified language resources/models.
 */
object MayraMultilingualGrammarEngine {
    enum class Priority { PRIMARY, INDIAN_ADDITIONAL, INTERNATIONAL_ADDITIONAL }

    data class LanguageProfile(
        val code: String,
        val name: String,
        val priority: Priority,
        val grammarAware: Boolean = true
    )

    private val profiles = listOf(
        LanguageProfile("bn", "Bengali", Priority.PRIMARY),
        LanguageProfile("hi", "Hindi", Priority.PRIMARY),
        LanguageProfile("en", "English", Priority.PRIMARY),
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

    /**
     * Returns a language-aware speech rule. The actual grammar checker/NLP model
     * should be selected according to the requested language and available
     * verified resources.
     */
    fun speakingRule(code: String): String =
        when (profile(code)?.priority) {
            Priority.PRIMARY ->
                "Speak with correct grammar, syntax, word choice, agreement and natural sentence structure."
            Priority.INDIAN_ADDITIONAL ->
                "Use the selected Indian language's verified grammar and natural usage; preserve meaning and context."
            Priority.INTERNATIONAL_ADDITIONAL ->
                "Use the selected international language's verified grammar and natural usage; preserve meaning and context."
            null ->
                "Identify the language first; do not invent grammar rules or claim unsupported fluency."
        }

    fun conversationRule(): String =
        "Detect or use the Owner-selected language, construct grammatically correct sentences, " +
            "keep Bengali/Hindi/English as primary languages, and use verified language resources for additional languages."

    fun languageNames(): List<String> = profiles.map { it.name }

    fun rule(): String =
        "Bengali, Hindi and English are Mayra's primary languages. Grammar is applied to speech and text. " +
            "Indian and major international languages are additional language profiles. " +
            "Mayra must distinguish verified language capability from unsupported claims."
}
