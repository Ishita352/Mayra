package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MayraCapabilityRegistryTest {
    @Test fun containsAllActiveFollowUpCapabilities() {
        assertEquals(60, MayraCapabilityRegistry.all().size)
        assertEquals(38, MayraCapabilityRegistry.count(MayraCapabilityRegistry.Status.COMPLETE))
        assertEquals(21, MayraCapabilityRegistry.count(MayraCapabilityRegistry.Status.IN_PROGRESS))
        assertEquals(1, MayraCapabilityRegistry.count(MayraCapabilityRegistry.Status.PLANNED))
    }

    @Test fun idsAreUniqueAndResolvable() {
        val ids = MayraCapabilityRegistry.all().map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        ids.forEach { assertNotNull(MayraCapabilityRegistry.byId(it)) }
    }

    @Test fun financialTransactionIsNotIntroducedAsACapability() {
        val titles = MayraCapabilityRegistry.all().map { it.title.lowercase() }
        assertEquals(false, titles.any { "financial transaction" in it })
    }
}
