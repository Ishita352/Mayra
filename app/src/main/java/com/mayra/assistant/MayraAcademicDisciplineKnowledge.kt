package com.mayra.assistant

/**
 * Cross-disciplinary academic knowledge and note-planning layer.
 * Current curricula, regulations and clinical/professional facts require
 * authoritative-source verification before being presented as current.
 */
object MayraAcademicDisciplineKnowledge {
    enum class Faculty {
        MEDICAL_HEALTH, ENGINEERING_TECHNOLOGY, PURE_SCIENCE, LIFE_SCIENCE,
        SOCIAL_SCIENCE, HUMANITIES, COMMERCE_MANAGEMENT, LAW, EDUCATION,
        AGRICULTURE, VETERINARY, PHARMACY, COMPUTER_INFORMATION,
        ARCHITECTURE_DESIGN, FINE_ARTS, MUSIC_PERFORMING_ARTS,
        PHYSICAL_EDUCATION, VOCATIONAL, INTERDISCIPLINARY, OTHER
    }

    data class Department(
        val faculty: Faculty,
        val name: String,
        val commonSubjects: List<String>
    )

    data class NoteRequest(
        val department: String,
        val subject: String,
        val topic: String,
        val academicLevel: MayraEducationIntelligence.AcademicLevel,
        val language: String = "Bengali",
        val depth: String = "standard"
    )

    data class NotePlan(
        val department: String,
        val subject: String,
        val topic: String,
        val sections: List<String>,
        val studyOutputs: List<String>,
        val sourceVerificationRequired: Boolean
    )

    private val departments = listOf(
        Department(Faculty.MEDICAL_HEALTH, "Medicine", listOf("Anatomy", "Physiology", "Biochemistry", "Pathology", "Pharmacology", "Medicine", "Surgery")),
        Department(Faculty.MEDICAL_HEALTH, "Nursing", listOf("Fundamentals of Nursing", "Community Health", "Medical-Surgical Nursing")),
        Department(Faculty.MEDICAL_HEALTH, "Public Health", listOf("Epidemiology", "Biostatistics", "Health Policy")),
        Department(Faculty.PHARMACY, "Pharmacy", listOf("Pharmaceutics", "Pharmacology", "Pharmaceutical Chemistry")),
        Department(Faculty.ENGINEERING_TECHNOLOGY, "Civil Engineering", listOf("Structural", "Geotechnical", "Transportation", "Environmental")),
        Department(Faculty.ENGINEERING_TECHNOLOGY, "Mechanical Engineering", listOf("Thermodynamics", "Fluid Mechanics", "Machine Design", "Manufacturing")),
        Department(Faculty.ENGINEERING_TECHNOLOGY, "Electrical Engineering", listOf("Circuits", "Power Systems", "Electrical Machines", "Control")),
        Department(Faculty.ENGINEERING_TECHNOLOGY, "Electronics & Communication", listOf("Digital Electronics", "Signals", "Communication", "Microprocessors")),
        Department(Faculty.COMPUTER_INFORMATION, "Computer Science", listOf("Programming", "Algorithms", "Databases", "Operating Systems", "Networks", "AI")),
        Department(Faculty.COMPUTER_INFORMATION, "Information Technology", listOf("Web", "Cloud", "Cybersecurity", "Data Management")),
        Department(Faculty.PURE_SCIENCE, "Mathematics", listOf("Algebra", "Calculus", "Geometry", "Statistics", "Analysis")),
        Department(Faculty.PURE_SCIENCE, "Physics", listOf("Mechanics", "Electromagnetism", "Thermodynamics", "Quantum Physics")),
        Department(Faculty.PURE_SCIENCE, "Chemistry", listOf("Organic", "Inorganic", "Physical", "Analytical")),
        Department(Faculty.LIFE_SCIENCE, "Biology", listOf("Cell Biology", "Genetics", "Ecology", "Evolution")),
        Department(Faculty.LIFE_SCIENCE, "Biotechnology", listOf("Molecular Biology", "Genetics", "Bioinformatics")),
        Department(Faculty.SOCIAL_SCIENCE, "Economics", listOf("Microeconomics", "Macroeconomics", "Econometrics")),
        Department(Faculty.SOCIAL_SCIENCE, "Political Science", listOf("Political Theory", "Public Administration", "International Relations")),
        Department(Faculty.SOCIAL_SCIENCE, "Sociology", listOf("Social Theory", "Research Methods", "Social Institutions")),
        Department(Faculty.HUMANITIES, "History", listOf("Ancient", "Medieval", "Modern", "Historiography")),
        Department(Faculty.HUMANITIES, "Philosophy", listOf("Logic", "Ethics", "Epistemology", "Indian Philosophy")),
        Department(Faculty.LANGUAGES, "Languages & Literature", listOf("Bengali", "English", "Hindi", "Sanskrit", "Linguistics")),
        Department(Faculty.COMMERCE_MANAGEMENT, "Commerce", listOf("Accounting", "Finance", "Taxation", "Business Law")),
        Department(Faculty.COMMERCE_MANAGEMENT, "Management", listOf("Marketing", "HR", "Operations", "Strategy")),
        Department(Faculty.LAW, "Law", listOf("Constitutional Law", "Criminal Law", "Civil Law", "Evidence", "Procedure")),
        Department(Faculty.EDUCATION, "Education", listOf("Pedagogy", "Educational Psychology", "Assessment", "Curriculum")),
        Department(Faculty.AGRICULTURE, "Agriculture", listOf("Agronomy", "Soil Science", "Horticulture", "Plant Protection")),
        Department(Faculty.VETERINARY, "Veterinary Science", listOf("Animal Anatomy", "Pathology", "Pharmacology", "Animal Health")),
        Department(Faculty.ARCHITECTURE_DESIGN, "Architecture & Design", listOf("Design", "Building Technology", "Planning", "History")),
        Department(Faculty.FINE_ARTS, "Fine Arts", listOf("Drawing", "Painting", "Sculpture", "Art History")),
        Department(Faculty.MUSIC_PERFORMING_ARTS, "Music & Performing Arts", listOf("Theory", "Vocal", "Instrumental", "Performance")),
        Department(Faculty.PHYSICAL_EDUCATION, "Physical Education", listOf("Exercise Science", "Training", "Sports Psychology")),
        Department(Faculty.VOCATIONAL, "Vocational & Skill Studies", listOf("Technical Skills", "Trade Theory", "Workplace Safety")),
        Department(Faculty.INTERDISCIPLINARY, "Interdisciplinary Studies", listOf("Data Science", "Environmental Studies", "Sustainability", "Research Methods"))
    )

    fun departments(): List<Department> = departments

    fun faculties(): List<Faculty> = Faculty.entries.toList()

    fun planNote(request: NoteRequest): NotePlan {
        val sections = listOf(
            "Concept and definitions",
            "Detailed explanation",
            "Key points and terminology",
            "Examples/applications",
            "Diagrams or step-by-step structure where appropriate",
            "Practice questions",
            "Revision summary"
        )
        val outputs = listOf(
            "Short notes", "Detailed notes", "Easy-language explanation",
            "Question-answer set", "Revision sheet", "Exam-oriented practice"
        )
        return NotePlan(
            request.department.trim(), request.subject.trim(), request.topic.trim(),
            sections, outputs, sourceVerificationRequired = true
        )
    }

    fun rule(): String =
        "Mayra can prepare study notes and explanations across academic disciplines, but must verify current syllabus, professional standards and current facts from authoritative sources. It must not invent qualifications, marks, clinical instructions or examination requirements."
}
