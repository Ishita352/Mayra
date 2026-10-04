package com.mayra.assistant

/**
 * Keeps the mobile PC-control surface honest: a published command is not
 * considered executable until its companion transport reports readiness.
 */
object PcCommandAvailability {
    fun currentlyImplemented(command: PcCommandCatalog.CommandId): Boolean = when (command) {
        PcCommandCatalog.CommandId.PING,
        PcCommandCatalog.CommandId.OPEN_NOTEPAD,
        PcCommandCatalog.CommandId.OPEN_CALCULATOR,
        PcCommandCatalog.CommandId.OPEN_WINDOWS_SETTINGS,
        PcCommandCatalog.CommandId.OPEN_NETWORK_SETTINGS,
        PcCommandCatalog.CommandId.OPEN_DISPLAY_SETTINGS,
        PcCommandCatalog.CommandId.OPEN_SOUND_SETTINGS,
        PcCommandCatalog.CommandId.GET_PC_STATUS,
        PcCommandCatalog.CommandId.GET_SECURITY_STATUS,
        PcCommandCatalog.CommandId.BRIDGE_START_SCREEN,
        PcCommandCatalog.CommandId.BRIDGE_START_FILE_SHARE,
        PcCommandCatalog.CommandId.REVOKE_SESSION -> true
        else -> false
    }

    fun requiresCompanionModule(command: PcCommandCatalog.CommandId): Boolean =
        PcCommandCatalog.isAllowed(command) && !currentlyImplemented(command)
}
