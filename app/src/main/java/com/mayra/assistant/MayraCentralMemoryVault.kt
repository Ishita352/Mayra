package com.mayra.assistant

/** Local-first storage policy with importance filtering and per-item Owner approval. */
object MayraCentralMemoryVault {
    enum class Provider { GOOGLE_DRIVE, LOCAL_ONLY, FUTURE_PROVIDER }
    enum class DataClass {
        CORE_MEMORY, RESUME_STATE, KNOWLEDGE, CAREER, SKILL_EVIDENCE,
        IMPORTANT_PHOTO, IMPORTANT_VIDEO, DOCUMENT, OTHER_MEDIA
    }
    enum class Importance { IMPORTANT, NOT_IMPORTANT, UNCERTAIN }
    enum class Decision {
        DO_NOT_SAVE, ASK_OWNER_IMPORTANCE, OWNER_APPROVAL_REQUIRED, ELIGIBLE_FOR_SYNC, BLOCKED
    }

    data class Config(
        val provider: Provider = Provider.GOOGLE_DRIVE,
        val accountEmail: String = "basakgopal571@gmail.com",
        val enabled: Boolean = false,
        val ownerApprovedForProvider: Boolean = false,
        val maxCloudBytes: Long = 15L * 1024L * 1024L * 1024L
    )

    data class Item(
        val id: String,
        val dataClass: DataClass,
        val sizeBytes: Long,
        val importance: Importance
    )

    /**
     * Importance is evaluated before any local or cloud save.
     * Uncertain cases are escalated to the Owner; they are not guessed.
     */
    fun decision(
        item: Item,
        config: Config,
        ownerApprovedForThisItem: Boolean = false
    ): Decision {
        if (item.id.isBlank() || item.sizeBytes < 0L) return Decision.BLOCKED

        when (item.importance) {
            Importance.NOT_IMPORTANT -> return Decision.DO_NOT_SAVE
            Importance.UNCERTAIN -> return Decision.ASK_OWNER_IMPORTANCE
            Importance.IMPORTANT -> Unit
        }

        if (!config.enabled || !config.ownerApprovedForProvider) {
            return Decision.OWNER_APPROVAL_REQUIRED
        }
        if (!ownerApprovedForThisItem) {
            return Decision.OWNER_APPROVAL_REQUIRED
        }
        if (item.dataClass == DataClass.OTHER_MEDIA) return Decision.DO_NOT_SAVE
        return Decision.ELIGIBLE_FOR_SYNC
    }

    fun requiresImportanceCheckBeforeEverySave(): Boolean = true

    fun uncertainImportanceRequiresOwnerQuestion(): Boolean = true

    fun requiresExplicitOwnerApprovalForEveryCloudUpload(): Boolean = true

    fun localSaveAllowedOnlyWhenImportant(): Boolean = true

    fun shouldFilterBeforeSync(item: Item): Boolean =
        item.importance != Importance.IMPORTANT

    fun providerCanBeChanged(): Boolean = true

    fun accountCanBeChanged(): Boolean = true

    fun importanceRule(): String =
        "Before saving anything to Android, Windows 10 or cloud storage, Mayra must decide whether it is important. " +
            "If it is not important, do not save it. If uncertain, ask the Owner and wait for the answer."

    fun approvalRule(): String =
        "After an item is judged important, cloud saving still requires explicit Owner approval for that specific item. " +
            "Provider/account connection approval is never blanket upload permission."

    fun storagePolicy(): String =
        "Important data only: local devices may retain important working data; cloud sync requires Owner approval. " +
            "Do not retain unnecessary or duplicate data. Never purchase extra storage."

    fun privacyRule(): String =
        "Do not upload passwords, OTPs, tokens, raw biometric templates, or unrelated private data."

    fun storageLimitRule(): String =
        "When cloud storage is near full, stop non-essential uploads and notify the Owner; never buy storage."

    fun googleDriveScope(): String = "https://www.googleapis.com/auth/drive.file"
}
