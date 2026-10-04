package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraCapabilityRegistryTest {
    @Test fun containsAllActiveFollowUpCapabilities() {
        assertEquals(63, MayraCapabilityRegistry.all().size)
        assertEquals(63, MayraCapabilityRegistry.count(MayraCapabilityRegistry.Status.COMPLETE))
        assertEquals(0, MayraCapabilityRegistry.count(MayraCapabilityRegistry.Status.IN_PROGRESS))
        assertEquals(0, MayraCapabilityRegistry.count(MayraCapabilityRegistry.Status.PLANNED))
    }

    @Test fun idsAreUniqueAndResolvable() {
        val ids = MayraCapabilityRegistry.all().map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        ids.forEach { assertNotNull(MayraCapabilityRegistry.byId(it)) }
    }

    @Test fun financialTransactionIsNotIntroducedAsACapability() {
        assertEquals(false, MayraCapabilityRegistry.all().any { "financial transaction" in it.title.lowercase() })
    }

    @Test fun deferredCapability43IsNotInCurrentScope() {
        assertTrue(MayraCapabilityRegistry.byId(43) == null)
    }
}
