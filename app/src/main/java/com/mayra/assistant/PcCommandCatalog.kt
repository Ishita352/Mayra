package com.mayra.assistant

/**
 * Owner-approved Windows PC command vocabulary exposed to Mayra Mobile.
 * Natural-language requests must resolve to one of these IDs before execution.
 */
object PcCommandCatalog {
    enum class CommandId {
        PING, OPEN_NOTEPAD, OPEN_CALCULATOR, OPEN_WINDOWS_SETTINGS,
        OPEN_NETWORK_SETTINGS, OPEN_DISPLAY_SETTINGS, OPEN_SOUND_SETTINGS,
        GET_PC_STATUS, GET_SECURITY_STATUS, REVOKE_SESSION
    }

    private val publishedCommands = setOf(
        CommandId.PING,
        CommandId.OPEN_NOTEPAD,
        CommandId.OPEN_CALCULATOR,
        CommandId.OPEN_WINDOWS_SETTINGS,
        CommandId.OPEN_NETWORK_SETTINGS,
        CommandId.OPEN_DISPLAY_SETTINGS,
        CommandId.OPEN_SOUND_SETTINGS,
        CommandId.GET_PC_STATUS,
        CommandId.GET_SECURITY_STATUS,
        CommandId.REVOKE_SESSION
    )

    fun isAllowed(command: CommandId): Boolean = command in publishedCommands

    fun requiresAuthenticatedSession(command: CommandId): Boolean =
        command != CommandId.PING

    fun displayName(command: CommandId): String = when (command) {
        CommandId.PING -> "Check PC connection"
        CommandId.OPEN_NOTEPAD -> "Open Notepad"
        CommandId.OPEN_CALCULATOR -> "Open Calculator"
        CommandId.OPEN_WINDOWS_SETTINGS -> "Open Windows Settings"
        CommandId.OPEN_NETWORK_SETTINGS -> "Open Network Settings"
        CommandId.OPEN_DISPLAY_SETTINGS -> "Open Display Settings"
        CommandId.OPEN_SOUND_SETTINGS -> "Open Sound Settings"
        CommandId.GET_PC_STATUS -> "Read PC status"
        CommandId.GET_SECURITY_STATUS -> "Read Windows security status"
        CommandId.REVOKE_SESSION -> "Revoke PC session"
    }
}
