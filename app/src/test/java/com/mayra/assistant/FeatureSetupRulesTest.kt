package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Test

class FeatureSetupRulesTest {
    @Test fun optional_feature_defaults_are_off() {
        assertFalse(FeatureToggleRegistry.defaultFor(FeatureToggleRegistry.VOICE_COMMAND))
        assertFalse(FeatureToggleRegistry.defaultFor(FeatureToggleRegistry.CAMERA))
        assertFalse(FeatureToggleRegistry.defaultFor(FeatureToggleRegistry.INCOMING_CALL_ASSISTANT))
        assertFalse(FeatureToggleRegistry.defaultFor(FeatureToggleRegistry.WHATSAPP_ASSISTANT))
        assertFalse(FeatureToggleRegistry.defaultFor(FeatureToggleRegistry.SECURITY))
    }

    @Test fun first_run_inventory_contains_expected_core_features() {
        val ids = MayraFeatureCheckManager.specs().map { it.id }
        assertEquals(true, ids.contains(MayraFeatureCheckManager.VOICE))
        assertEquals(true, ids.contains(MayraFeatureCheckManager.CAMERA))
        assertEquals(true, ids.contains(MayraFeatureCheckManager.CALL))
        assertEquals(true, ids.contains(MayraFeatureCheckManager.WHATSAPP))
        assertEquals(true, ids.contains(MayraFeatureCheckManager.COMPUTER))
        assertEquals(true, ids.contains(MayraFeatureCheckManager.DOCUMENTS))
    }
}
