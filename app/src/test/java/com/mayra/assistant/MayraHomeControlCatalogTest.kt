package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraHomeControlCatalogTest {
    @Test fun homeContainsPrimaryControls() {
        val ids = MayraHomeControlCatalog.controls().map { it.id }
        assertEquals(
            listOf(
                MayraHomeControlCatalog.ControlId.VOICE_ASSISTANT,
                MayraHomeControlCatalog.ControlId.CALL_ASSISTANT,
                MayraHomeControlCatalog.ControlId.CAMERA,
                MayraHomeControlCatalog.ControlId.WHATSAPP_ASSISTANT,
                MayraHomeControlCatalog.ControlId.SECURITY,
                MayraHomeControlCatalog.ControlId.PC_CONTROL
            ),
            ids
        )
    }

    @Test fun pcControlIsAnEntryPointNotAToggle() {
        val pc = MayraHomeControlCatalog.controls()
            .first { it.id == MayraHomeControlCatalog.ControlId.PC_CONTROL }
        assertTrue(!pc.toggleable)
    }
}
