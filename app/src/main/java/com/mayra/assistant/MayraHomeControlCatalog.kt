package com.mayra.assistant

/**
 * Mayra Home Page primary controls.
 *
 * These are intentionally small, high-value controls. Detailed operations
 * remain inside the relevant feature screens or voice-command flow.
 */
object MayraHomeControlCatalog {
    enum class ControlId {
        VOICE_ASSISTANT,
        CALL_ASSISTANT,
        CAMERA,
        WHATSAPP_ASSISTANT,
        SECURITY,
        PC_CONTROL
    }

    data class Control(
        val id: ControlId,
        val title: String,
        val toggleable: Boolean = true
    )

    fun controls(): List<Control> = listOf(
        Control(ControlId.VOICE_ASSISTANT, "Voice Assistant"),
        Control(ControlId.CALL_ASSISTANT, "Call Assistant"),
        Control(ControlId.CAMERA, "Camera"),
        Control(ControlId.WHATSAPP_ASSISTANT, "WhatsApp Read & Voice Reply"),
        Control(ControlId.SECURITY, "Security"),
        Control(ControlId.PC_CONTROL, "PC Control", toggleable = false)
    )

    fun contains(id: ControlId): Boolean = controls().any { it.id == id }
}
