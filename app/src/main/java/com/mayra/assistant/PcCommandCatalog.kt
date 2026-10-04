package com.mayra.assistant

/**
 * Owner-approved Windows PC command vocabulary exposed to Mayra Mobile.
 * Natural-language requests must resolve to one of these IDs before execution.
 */
object PcCommandCatalog {
    enum class CommandId {
        PING, OPEN_NOTEPAD, OPEN_CALCULATOR, OPEN_WINDOWS_SETTINGS,
        OPEN_NETWORK_SETTINGS, OPEN_DISPLAY_SETTINGS, OPEN_SOUND_SETTINGS,
        GET_PC_STATUS, GET_SECURITY_STATUS,
        BRIDGE_START_SCREEN, BRIDGE_START_FILE_SHARE,
        LIST_SHARED_FILES, OPEN_SHARED_FILE, SEND_FILE_TO_PC, RECEIVE_FILE_FROM_PC,
        READ_CLIPBOARD, WRITE_CLIPBOARD,
        OPEN_BROWSER, BROWSER_AUTOMATION,
        MEDIA_PLAY_PAUSE, MEDIA_NEXT, MEDIA_PREVIOUS, SET_VOLUME,
        SCREEN_VIEW, SCREEN_CONTROL,
        PHONE_CAMERA_FRONT, PHONE_CAMERA_BACK, PHONE_MICROPHONE, PHONE_SPEAKER,
        PHONE_RECOVERY_STATUS,
        REVOKE_SESSION
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
        CommandId.BRIDGE_START_SCREEN,
        CommandId.BRIDGE_START_FILE_SHARE,
        CommandId.LIST_SHARED_FILES,
        CommandId.OPEN_SHARED_FILE,
        CommandId.SEND_FILE_TO_PC,
        CommandId.RECEIVE_FILE_FROM_PC,
        CommandId.READ_CLIPBOARD,
        CommandId.WRITE_CLIPBOARD,
        CommandId.OPEN_BROWSER,
        CommandId.BROWSER_AUTOMATION,
        CommandId.MEDIA_PLAY_PAUSE,
        CommandId.MEDIA_NEXT,
        CommandId.MEDIA_PREVIOUS,
        CommandId.SET_VOLUME,
        CommandId.SCREEN_VIEW,
        CommandId.SCREEN_CONTROL,
        CommandId.PHONE_CAMERA_FRONT,
        CommandId.PHONE_CAMERA_BACK,
        CommandId.PHONE_MICROPHONE,
        CommandId.PHONE_SPEAKER,
        CommandId.PHONE_RECOVERY_STATUS,
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
        CommandId.BRIDGE_START_SCREEN -> "Start screen-control bridge"
        CommandId.BRIDGE_START_FILE_SHARE -> "Start file-sharing bridge"
        CommandId.LIST_SHARED_FILES -> "List shared files"
        CommandId.OPEN_SHARED_FILE -> "Open shared file"
        CommandId.SEND_FILE_TO_PC -> "Send file to PC"
        CommandId.RECEIVE_FILE_FROM_PC -> "Receive file from PC"
        CommandId.READ_CLIPBOARD -> "Read PC clipboard"
        CommandId.WRITE_CLIPBOARD -> "Write PC clipboard"
        CommandId.OPEN_BROWSER -> "Open browser"
        CommandId.BROWSER_AUTOMATION -> "Approved browser automation"
        CommandId.MEDIA_PLAY_PAUSE -> "Play or pause media"
        CommandId.MEDIA_NEXT -> "Next media"
        CommandId.MEDIA_PREVIOUS -> "Previous media"
        CommandId.SET_VOLUME -> "Set PC volume"
        CommandId.SCREEN_VIEW -> "View PC screen"
        CommandId.SCREEN_CONTROL -> "Control PC screen"
        CommandId.PHONE_CAMERA_FRONT -> "Use phone front camera"
        CommandId.PHONE_CAMERA_BACK -> "Use phone rear camera"
        CommandId.PHONE_MICROPHONE -> "Use phone microphone"
        CommandId.PHONE_SPEAKER -> "Use phone speaker"
        CommandId.PHONE_RECOVERY_STATUS -> "Read phone recovery status"
        CommandId.REVOKE_SESSION -> "Revoke PC session"
    }
}
