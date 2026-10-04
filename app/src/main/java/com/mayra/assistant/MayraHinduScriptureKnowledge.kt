package com.mayra.assistant

/** Structured knowledge taxonomy for Hindu scriptures, traditions and classical Ayurveda. */
object MayraHinduScriptureKnowledge {
    enum class Tradition {
        VEDA, VEDANGA, UPANISHAD, ITIHASA, GITA, PURANA, DHARMASHASTRA,
        DARSHANA, AGAMA_TANTRA, SMRITI, AYURVEDA, OTHER_CLASSICAL_TEXT
    }

    data class Corpus(
        val tradition: Tradition,
        val name: String,
        val divisions: List<String>,
        val description: String
    )

    private val corpus = listOf(
        Corpus(Tradition.VEDA, "Rigveda", listOf("Samhita", "Brahmana", "Aranyaka", "Upanishad"),
            "One of the four Vedas; its textual tradition includes hymns and associated layers."),
        Corpus(Tradition.VEDA, "Yajurveda", listOf("Shukla Yajurveda", "Krishna Yajurveda"),
            "The Veda associated especially with sacrificial formulae and ritual traditions."),
        Corpus(Tradition.VEDA, "Samaveda", listOf("Samhita", "associated Brahmana and ritual traditions"),
            "The Veda especially associated with liturgical melodies and chants."),
        Corpus(Tradition.VEDA, "Atharvaveda", listOf("Samhita", "associated Brahmana and Upanishadic traditions"),
            "The fourth Veda, with hymns, prayers and diverse ritual material."),

        Corpus(Tradition.VEDANGA, "Vedanga", listOf("Shiksha", "Vyakarana", "Chandas", "Nirukta", "Jyotisha", "Kalpa"),
            "The six auxiliary disciplines traditionally used for preserving and interpreting Vedic learning."),

        Corpus(Tradition.UPANISHAD, "Upanishadic corpus", listOf("Principal Upanishads", "other traditional Upanishads"),
            "Philosophical and spiritual texts associated with Vedic traditions; lists vary by tradition."),

        Corpus(Tradition.ITIHASA, "Ramayana", listOf("Kanda traditions"),
            "Itihasa tradition attributed to Valmiki, with multiple recensions and regional traditions."),
        Corpus(Tradition.ITIHASA, "Mahabharata", listOf("Parvas", "Bhagavad Gita"),
            "Major Itihasa with extensive philosophical, ethical, genealogical and narrative material."),

        Corpus(Tradition.GITA, "Bhagavad Gita", listOf("18 chapters"),
            "A dialogue within the Mahabharata; Mayra should distinguish its text, commentaries and later interpretations."),

        Corpus(Tradition.PURANA, "Puranic corpus", listOf("Mahapurana traditions", "Upapurana traditions", "regional Puranic traditions"),
            "A large family of texts with differing traditional classifications and recensions; no single modern list is universally exhaustive."),

        Corpus(Tradition.DHARMASHASTRA, "Dharmashastra", listOf("Dharmasutra traditions", "Smriti texts", "commentarial traditions"),
            "Normative and legal-ethical literature whose historical authority and application vary by period, school and context."),

        Corpus(Tradition.DARSHANA, "Shaddarshana", listOf("Nyaya", "Vaisheshika", "Samkhya", "Yoga", "Mimamsa", "Vedanta"),
            "The six commonly grouped classical philosophical systems, with multiple sub-schools and commentarial traditions."),

        Corpus(Tradition.AGAMA_TANTRA, "Agama and Tantra traditions", listOf("Shaiva", "Vaishnava", "Shakta and other traditions"),
            "Ritual, temple, theology and practice literature with substantial sectarian and regional diversity."),

        Corpus(Tradition.SMRITI, "Smriti traditions", listOf("Manusmriti", "Yajnavalkya Smriti", "Narada Smriti", "other Smriti traditions"),
            "A broad category of remembered/traditional literature; individual texts must be identified by source and historical context."),

        Corpus(Tradition.AYURVEDA, "Classical Ayurveda", listOf(
            "Charaka Samhita", "Sushruta Samhita", "Ashtanga Hridaya", "Ashtanga Sangraha",
            "Kashyapa Samhita traditions", "Madhava Nidana", "Sharngadhara Samhita", "Bhavaprakasha"
        ), "Classical Ayurvedic literature and later compendia. Medical claims must be distinguished from modern evidence-based medical guidance.")
    )

    fun allCorpus(): List<Corpus> = corpus

    fun vedas(): List<Corpus> = corpus.filter { it.tradition == Tradition.VEDA }

    fun divisionsOfVeda(): List<String> =
        listOf("Rigveda", "Yajurveda", "Samaveda", "Atharvaveda")

    fun rule(): String =
        "Represent Hindu scriptures by tradition, text, division, recension and commentary where relevant; preserve source and context; " +
        "do not treat one sectarian or modern list as universally exhaustive. Distinguish scripture, commentary, historical scholarship and interpretation. " +
        "For Ayurveda, present classical knowledge as historical/traditional medical literature and do not replace diagnosis or evidence-based medical care."
}
