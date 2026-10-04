package com.mayra.assistant

/**
 * India-focused legal guidance layer.
 *
 * This is an informational/decision-support engine, not a court, police authority,
 * or substitute for a qualified advocate. It must use current official legislation
 * and judgments when giving case-specific guidance.
 */
object MayraLegalGuidanceEngine {
    enum class MatterType { CRIMINAL, CIVIL, CYBER, POLICE, COURT, EMPLOYMENT, FAMILY, PROPERTY, OTHER }
    enum class Risk { LOW, MEDIUM, HIGH, URGENT }

    data class LegalCorpus(
        val constitutionOfficialUrl: String,
        val statutesOfficialUrl: String,
        val criminalLaw: List<String>,
        val procedureLaw: List<String>,
        val evidenceLaw: List<String>,
        val legalAidLaw: String
    )

    data class Matter(
        val description: String,
        val type: MatterType,
        val accusedOrPartyStatus: String? = null,
        val sectionOrAct: String? = null,
        val stage: String? = null
    )

    data class Guidance(
        val lawfulStatus: String,
        val possibleRightsOrRemedies: List<String>,
        val lawfulNextSteps: List<String>,
        val documentsToCollect: List<String>,
        val urgency: Risk,
        val advocateRequired: Boolean,
        val warning: String
    )

    fun officialCorpus(): LegalCorpus = LegalCorpus(
        constitutionOfficialUrl = "https://www.legislative.gov.in/constitution-of-india/",
        statutesOfficialUrl = "https://www.indiacode.nic.in/",
        criminalLaw = listOf(
            "Bharatiya Nyaya Sanhita, 2023 (BNS)",
            "Bharatiya Nagarik Suraksha Sanhita, 2023 (BNSS)"
        ),
        procedureLaw = listOf(
            "Bharatiya Nagarik Suraksha Sanhita, 2023 (BNSS)",
            "Constitution of India",
            "Applicable special/state laws and rules"
        ),
        evidenceLaw = listOf("Bharatiya Sakshya Adhiniyam, 2023 (BSA)"),
        legalAidLaw = "Legal Services Authorities Act, 1987"
    )

    fun assess(matter: Matter, verifiedSourcesAvailable: Boolean): Guidance {
        val description = matter.description.trim()
        if (description.isBlank()) {
            return Guidance(
                lawfulStatus = "INSUFFICIENT_FACTS",
                possibleRightsOrRemedies = emptyList(),
                lawfulNextSteps = listOf("Provide the relevant facts, case stage, notice/FIR/order details and applicable jurisdiction."),
                documentsToCollect = emptyList(),
                urgency = Risk.MEDIUM,
                advocateRequired = true,
                warning = "Mayra must not invent facts or legal conclusions."
            )
        }

        val urgent = matter.stage?.contains("arrest", ignoreCase = true) == true ||
            matter.stage?.contains("custody", ignoreCase = true) == true ||
            matter.stage?.contains("bail", ignoreCase = true) == true

        val remedies = mutableListOf(
            "Explain the applicable constitutional/statutory rights and procedural stage.",
            "Identify lawful remedies such as bail, appeal, revision, review, discharge or other relief only when the cited law and facts support them.",
            "Distinguish allegation/FIR, investigation, charge, trial, conviction and final judgment."
        )
        if (matter.type == MatterType.CYBER || matter.type == MatterType.POLICE) {
            remedies += "Identify the appropriate police/cybercrime and court procedure using publicly accessible official sources."
        }
        if (matter.type == MatterType.COURT) {
            remedies += "Check the latest public order/judgment before treating an older legal position as current."
        }

        return Guidance(
            lawfulStatus = if (verifiedSourcesAvailable) "SOURCE-BASED_LEGAL_GUIDANCE" else "OFFICIAL_SOURCE_VERIFICATION_REQUIRED",
            possibleRightsOrRemedies = remedies,
            lawfulNextSteps = listOf(
                "Verify the current Act/section, jurisdiction and latest court order.",
                "Preserve notices, FIR/case number, orders, dates and relevant documents.",
                "Use a qualified advocate for case-specific representation or legal opinion."
            ),
            documentsToCollect = listOf("FIR/complaint if public or lawfully obtained", "Court orders/judgments", "Notices/summons", "Relevant supporting documents"),
            urgency = if (urgent) Risk.URGENT else Risk.MEDIUM,
            advocateRequired = true,
            warning = "Mayra can explain lawful options but cannot guarantee an outcome, declare guilt/innocence, impersonate a lawyer/court, fabricate evidence, evade lawful process, or help bypass police/court requirements."
        )
    }

    fun isPotentiallyIllegal(request: String): Boolean {
        val s = request.lowercase()
        val prohibited = listOf(
            "forge evidence", "fabricate evidence", "destroy evidence",
            "bribe police", "bribe judge", "bypass court order",
            "hide from police", "evade arrest", "hack police", "hack court",
            "fake document", "false identity"
        )
        return prohibited.any { s.contains(it) }
    }

    fun rule(): String =
        "Use the Constitution, current central/state legislation, applicable rules and current authoritative judgments. " +
        "For every legal answer, separate facts from allegations and law from inference; cite the source and date; " +
        "prefer official sources; identify uncertainty and jurisdiction; suggest lawful remedies only; never facilitate evasion, fraud, evidence destruction, bribery, hacking or other unlawful conduct."
}
