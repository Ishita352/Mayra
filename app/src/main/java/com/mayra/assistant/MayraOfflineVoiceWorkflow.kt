package com.mayra.assistant

/**
 * Offline-first workflow for tasks that can be completed locally.
 * Network-dependent uploads are queued locally and require connectivity
 * plus the existing Owner approval gate before cloud transfer.
 */
object MayraOfflineVoiceWorkflow {
    enum class Task {
        MEMORY_EDIT, DOCUMENT_EDIT, PDF_PROCESSING, SPREADSHEET_ANALYSIS,
        CV_EDITING, NOTE_CREATION, IMAGE_EDITING, VIDEO_EDITING,
        TEXTILE_DESIGN, KNOWLEDGE_REVIEW, VOICE_COMMAND, FILE_ORGANIZATION,
        CLOUD_UPLOAD
    }

    enum class Decision {
        ALLOW_OFFLINE, QUEUE_FOR_LATER, OWNER_APPROVAL_REQUIRED, NETWORK_REQUIRED, BLOCKED
    }

    fun canWorkOffline(task: Task): Boolean =
        task != Task.CLOUD_UPLOAD

    fun decide(
        task: Task,
        online: Boolean,
        ownerApproved: Boolean = false
    ): Decision {
        if (task == Task.CLOUD_UPLOAD) {
            return if (!online) Decision.QUEUE_FOR_LATER
            else if (!ownerApproved) Decision.OWNER_APPROVAL_REQUIRED
            else Decision.QUEUE_FOR_LATER
        }
        return if (canWorkOffline(task)) Decision.ALLOW_OFFLINE else Decision.NETWORK_REQUIRED
    }

    fun voiceCommand(spoken: String): String? {
        val s = spoken.lowercase()
        return when {
            listOf("অফলাইনে কাজ করো", "offline কাজ", "work offline", "offline mode").any { s.contains(it) } ->
                "OFFLINE_MODE"
            listOf("অফলাইনের কাজ চালাও", "offline task", "অফলাইন কাজ শুরু").any { s.contains(it) } ->
                "RUN_OFFLINE_TASKS"
            listOf("আপলোড পরে করো", "পরে আপলোড করো", "upload later", "queue upload").any { s.contains(it) } ->
                "QUEUE_UPLOAD"
            else -> null
        }
    }

    fun rule(): String =
        "When offline, Mayra may continue only tasks designed for local/offline execution. " +
            "Voice commands can start or control those tasks. Cloud upload requires connectivity and " +
            "the existing per-item Owner approval; if offline, prepare and queue it locally. " +
            "Mayra must not bypass permissions or network requirements."

    fun queueRule(): String =
        "Queued uploads remain local until connectivity is available and the Owner-approved upload gate permits transfer."
}
