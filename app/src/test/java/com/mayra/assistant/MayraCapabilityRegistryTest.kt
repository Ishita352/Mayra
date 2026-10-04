package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MayraCapabilityRegistryTest {
    @Test fun containsAllActiveFollowUpCapabilities() {
        assertEquals(62, MayraCapabilityRegistry.all().size)
        assertEquals(50, MayraCapabilityRegistry.count(MayraCapabilityRegistry.Status.COMPLETE))
        assertEquals(11, MayraCapabilityRegistry.count(MayraCapabilityRegistry.Status.IN_PROGRESS))
        assertEquals(1, MayraCapabilityRegistry.count(MayraCapabilityRegistry.Status.PLANNED))
    }
    @Test fun idsAreUniqueAndResolvable() {
        val ids=MayraCapabilityRegistry.all().map{it.id}
        assertEquals(ids.size,ids.toSet().size); ids.forEach{assertNotNull(MayraCapabilityRegistry.byId(it))}
    }
    @Test fun financialTransactionIsNotIntroducedAsACapability() {
        assertEquals(false,MayraCapabilityRegistry.all().any{"financial transaction" in it.title.lowercase()})
    }
}