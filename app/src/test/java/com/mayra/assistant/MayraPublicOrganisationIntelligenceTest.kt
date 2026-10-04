package com.mayra.assistant

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MayraPublicOrganisationIntelligenceTest {
    @Test fun acceptsPublicOfficeHolder() {
        val holder = MayraPublicOrganisationIntelligence.OfficeHolder(
            organisation = "Example Organisation",
            personName = "Public Office Holder",
            role = "State Office Bearer",
            level = MayraPublicOrganisationIntelligence.Level.STATE,
            area = "West Bengal",
            sourceUrl = "https://example.org/announcement",
            effectiveFromMs = 1000L,
            publicBackground = "Publicly published professional background."
        )
        assertTrue(MayraPublicOrganisationIntelligence.acceptHolder(holder))
    }

    @Test fun rejectsNonHttpsOrBlankUpdate() {
        val update = MayraPublicOrganisationIntelligence.Update(
            organisation = "Example",
            eventType = MayraPublicOrganisationIntelligence.EventType.APPOINTMENT,
            description = "Appointment announced",
            sourceUrl = "http://example.org",
            publishedAtMs = 1000L
        )
        assertFalse(MayraPublicOrganisationIntelligence.acceptUpdate(update))
    }

    @Test fun successionIsExplicitlyProbabilistic() {
        assertTrue(MayraPublicOrganisationIntelligence.successionRule().contains("possibilities"))
        assertTrue(MayraPublicOrganisationIntelligence.privacyRule().contains("private"))
    }
}
