package com.mayra.assistant

/**
 * Canonical Owner career/skill portfolio derived from the Owner-provided CV.
 *
 * This file intentionally excludes personal contact details so phone/email are
 * never hard-coded into the repository. Contact details belong in the local
 * Owner-controlled Career Profile.
 */
object MayraOwnerBiodataPortfolio {
    data class AdvancedSkill(
        val name: String,
        val category: String,
        val evidence: String,
        val mayraCapability: String
    )

    data class WorkOpportunity(
        val title: String,
        val category: String,
        val cvRequired: Boolean,
        val skillNames: List<String>,
        val guidance: String
    )

    private val skills = listOf(
        AdvancedSkill("Data Entry", "Data & Office", "CV professional experience", "Advanced assistance: accurate entry, validation, organization and task guidance"),
        AdvancedSkill("MS Word", "Office", "CV computer & office skills", "Advanced assistance: document creation, formatting and workflow guidance"),
        AdvancedSkill("MS Excel", "Office & Data", "CV computer & office skills", "Advanced assistance: spreadsheet organization, formulas/data workflows and analysis guidance"),
        AdvancedSkill("MS Office", "Office", "CV computer & office skills", "Advanced assistance: office workflow guidance"),
        AdvancedSkill("Document Formatting", "Office", "CV computer & office skills", "Advanced assistance: formatting, cleanup and document preparation"),
        AdvancedSkill("Spreadsheet Management", "Office & Data", "CV professional experience", "Advanced assistance: spreadsheet organization, validation and analysis"),
        AdvancedSkill("Tally", "Accounting & Data", "CV computer & office skills", "Advanced assistance: Tally workflow guidance; never invent accounting credentials"),
        AdvancedSkill("Data Operations", "Data", "CV professional experience", "Advanced assistance: structured data operations and quality checks"),
        AdvancedSkill("Data Organization", "Data", "CV professional experience", "Advanced assistance: classification, cleanup and systematic organization"),
        AdvancedSkill("Basic Data Handling", "Data", "CV computer & office skills", "Advanced assistance: guided data handling and validation"),
        AdvancedSkill("Computer Operations", "Computer", "Two-year computer course", "Advanced assistance: everyday computer workflow guidance"),
        AdvancedSkill("File Management", "Computer", "CV computer skills", "Advanced assistance: file organization and safe document workflows"),
        AdvancedSkill("Internet & Email", "Computer", "CV computer skills", "Advanced assistance: web/email workflow guidance"),
        AdvancedSkill("Web Research", "Research", "CV online work", "Advanced assistance: structured public-web research and source comparison"),
        AdvancedSkill("Google Maps Verification", "Verification", "CV online work", "Advanced assistance: location/listing verification workflows"),
        AdvancedSkill("Gmail Verification", "Verification", "CV online work", "Advanced assistance: authorized email verification workflows"),
        AdvancedSkill("Online Micro-tasks", "Micro-work", "CV online work", "Advanced assistance: discover, compare and guide suitable micro-task work"),
        AdvancedSkill("Task Management", "Operations", "CV online work", "Advanced assistance: task breakdown, tracking and prioritization"),
        AdvancedSkill("Customer Service", "Customer Support", "CV experience", "Advanced assistance: response drafting, issue classification and workflow guidance"),
        AdvancedSkill("Customer Management", "Customer Support", "CV experience", "Advanced assistance: customer workflow and follow-up guidance"),
        AdvancedSkill("Customer Care", "Customer Support", "CV experience", "Advanced assistance: customer communication and service guidance"),
        AdvancedSkill("Service Support", "Customer Support", "CV experience", "Advanced assistance: support workflow guidance"),
        AdvancedSkill("Customer Support Calls", "Communication", "CV communication skills", "Advanced assistance: call preparation, response guidance and summaries"),
        AdvancedSkill("Live Chat Support", "Communication", "CV communication skills", "Advanced assistance: live-chat response drafting and issue handling"),
        AdvancedSkill("Email Support", "Communication", "CV communication skills", "Advanced assistance: professional email drafting and follow-up"),
        AdvancedSkill("Query Handling", "Communication", "CV communication skills", "Advanced assistance: classify and answer customer queries"),
        AdvancedSkill("Customer Issue Handling", "Support Operations", "CV experience", "Advanced assistance: issue triage and resolution guidance"),
        AdvancedSkill("Follow-up Communication", "Support Operations", "CV experience", "Advanced assistance: follow-up planning and message drafting"),
        AdvancedSkill("Service Coordination", "Support Operations", "CV experience", "Advanced assistance: coordination and status tracking"),
        AdvancedSkill("Basic Complaint Handling", "Support Operations", "CV experience", "Advanced assistance: complaint-response guidance"),
        AdvancedSkill("Hindi-to-Bengali Translation", "Language", "CV language skills", "Advanced assistance: translation drafting, comparison and correction"),
        AdvancedSkill("Bengali Communication", "Language", "CV languages", "Advanced assistance: Bengali grammar-aware communication"),
        AdvancedSkill("Hindi Communication", "Language", "CV languages", "Advanced assistance: Hindi grammar-aware communication"),
        AdvancedSkill("English Communication", "Language", "CV languages", "Advanced assistance: English grammar-aware communication"),
        AdvancedSkill("Multilingual Assistance", "Language", "CV language support", "Advanced assistance: multilingual drafting and translation support"),
        AdvancedSkill("Content Writing", "Writing", "CV creative skills", "Advanced assistance: original content drafting and editing"),
        AdvancedSkill("Paragraph Writing", "Writing", "CV creative skills", "Advanced assistance: structured paragraph drafting"),
        AdvancedSkill("Writing Creator", "Writing", "CV creative skills", "Advanced assistance: creative writing workflow support"),
        AdvancedSkill("Short Article/Text Writing", "Writing", "CV creative skills", "Advanced assistance: short-form original writing"),
        AdvancedSkill("Story Writing", "Creative", "CV creative skills", "Advanced assistance: story development and editing"),
        AdvancedSkill("Short Film Creator", "Creative Media", "CV creative skills", "Advanced assistance: concept, story and production planning"),
        AdvancedSkill("Story Creator", "Creative Media", "CV creative skills", "Advanced assistance: story ideation and structure"),
        AdvancedSkill("Creative Concept Development", "Creative Media", "CV creative skills", "Advanced assistance: concept development and refinement"),
        AdvancedSkill("Script/Story Content", "Creative Media", "CV creative skills", "Advanced assistance: script/story drafting and revision"),
        AdvancedSkill("Audio Writing", "Audio/Media", "CV creative skills", "Advanced assistance: audio-related writing and content planning"),
        AdvancedSkill("Video Writing", "Video", "CV creative skills", "Advanced assistance: video scripting/content writing"),
        AdvancedSkill("Video Editing", "Video", "CV creative skills", "Advanced assistance: editing workflow, planning and review"),
        AdvancedSkill("Photo Editing", "Photo", "CV creative skills", "Advanced assistance: editing workflow, review and content preparation"),
        AdvancedSkill("Digital Content Creation", "Digital Media", "CV creative skills", "Advanced assistance: original digital content planning and preparation"),
        AdvancedSkill("AI Training Support", "AI", "CV professional experience", "Advanced assistance: AI task preparation, guideline following and quality review"),
        AdvancedSkill("Prompt & Content Validation", "AI", "CV AI training skills", "Advanced assistance: prompt/content validation and quality checks"),
        AdvancedSkill("Content Review", "Quality", "CV AI training skills", "Advanced assistance: quality review against provided guidelines"),
        AdvancedSkill("Photo Selection", "Quality & Media", "CV professional experience", "Advanced assistance: criteria-based photo selection and review")
    )

    private val opportunities = listOf(
        WorkOpportunity("Data entry micro-task", "Micro-work", false, listOf("Data Entry", "Data Organization", "Internet & Email"), "Mayra can find suitable tasks, explain requirements and guide completion."),
        WorkOpportunity("Web research micro-task", "Research", false, listOf("Web Research", "Task Management"), "Mayra can find public research tasks and guide source-based completion."),
        WorkOpportunity("Map/listing verification task", "Verification", false, listOf("Google Maps Verification", "Data Operations"), "Mayra can identify suitable verification work and guide the manual task."),
        WorkOpportunity("Email/data verification task", "Verification", false, listOf("Gmail Verification", "Data Entry"), "Mayra can find authorized verification tasks and guide the workflow."),
        WorkOpportunity("Customer chat support task", "Customer Support", false, listOf("Live Chat Support", "Customer Service", "Query Handling"), "Mayra can find suitable support work and prepare responses."),
        WorkOpportunity("Translation micro-task", "Language", false, listOf("Hindi-to-Bengali Translation", "Bengali Communication", "Hindi Communication"), "Mayra can find translation opportunities and assist with drafts/review."),
        WorkOpportunity("Content writing micro-task", "Writing", false, listOf("Content Writing", "Paragraph Writing"), "Mayra can find suitable writing tasks and help prepare original work."),
        WorkOpportunity("Photo editing micro-task", "Creative", false, listOf("Photo Editing", "Digital Content Creation"), "Mayra can find suitable editing work and guide preparation."),
        WorkOpportunity("Video editing micro-task", "Creative", false, listOf("Video Editing", "Video Writing"), "Mayra can find suitable editing work and guide delivery preparation."),
        WorkOpportunity("AI training/content review task", "AI", false, listOf("AI Training Support", "Prompt & Content Validation", "Content Review"), "Mayra can find AI-permitted tasks and guide guideline-based completion."),
        WorkOpportunity("Data entry / office role", "Job", true, listOf("Data Entry", "MS Excel", "MS Word"), "Use the Owner CV/Career Profile for CV-first matching and application preparation."),
        WorkOpportunity("Customer support role", "Job", true, listOf("Customer Service", "Live Chat Support", "Email Support"), "Use the Owner CV/Career Profile for CV-first matching."),
        WorkOpportunity("Digital content role", "Job", true, listOf("Content Writing", "Photo Editing", "Video Editing"), "Use the Owner CV/Career Profile for CV-first matching.")
    )

    fun skills(): List<AdvancedSkill> = skills
    fun skillNames(): Set<String> = skills.map { it.name }.toSet()
    fun opportunities(): List<WorkOpportunity> = opportunities


    fun careerProfileFromCv(): MayraCareerProfile = MayraCareerProfile(
        fullName = "Gopal Basak",
        headline = "Freelance Professional • Data Entry • AI Support • Digital & Creative Work",
        location = "Dhatrigram, Kalna, Purba Bardhaman, West Bengal – 713405",
        education = "Bachelor of Arts (B.A.), Bardhaman University, 2017–2020, 75%\nHigher Secondary, Dhatrigram High School, 2015–2016, 72%\nDiploma in Computer Course, Two-Year Computer Course",
        experience = "Freelance Professional — Independent / Remote: Data & Digital Operations; Customer Service & Support; Language, Audio/Video & Content; AI Training & Quality Review.\nProduction Supervisor — Surya Fast Food Limited (Priya Gold).\nSupervisor Engineer — Aparna Construction, Hyderabad.",
        skills = skillNames().joinToString(" • "),
        certificates = "Two-Year Computer Course Diploma",
        languages = "Bengali (Native) • Hindi (Professional) • English (Professional)",
        preferredRoles = "Data Entry • Data/Office Operations • Customer Support • Web Research • Translation • Content Writing • Photo/Video Editing • AI Training/Content Review • Online Micro-tasks",
        preferredWorkMode = "Freelance / Independent / Remote"
    )

    fun seedCareerProfileIfMissing(store: MayraCareerProfileStore.Store) {
        if (MayraCareerProfileStore.load(store) == null) {
            MayraCareerProfileStore.save(store, careerProfileFromCv())
        }
    }

    fun profileSummary(): String = "Owner skill portfolio imported from the provided CV. Mayra may update this profile only with Owner-approved, evidenced skills or completed learning."

    fun rule(): String =
        "Every CV-listed skill is an advanced Mayra assistance capability, but this does not falsely upgrade the Owner's professional credential level. New Owner skills enter the portfolio only with evidence/Owner approval. CV-required jobs use the Career Profile; CV-free micro-work can be discovered from demonstrated skills and experience. No invented qualifications, certificates or experience."
}
