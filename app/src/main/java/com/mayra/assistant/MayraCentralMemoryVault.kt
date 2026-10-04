package com.mayra.assistant

/** Local-first central memory policy with per-item Owner approval. */
object MayraCentralMemoryVault {
    enum class Provider { GOOGLE_DRIVE, LOCAL_ONLY, FUTURE_PROVIDER }
    enum class DataClass {
        CORE_MEMORY, RESUME_STATE, KNOWLEDGE, CAREER, SKILL_EVIDENCE,
        IMPORTANT_PHOTO, IMPORTANT_VIDEO, DOCUMENT, OTHER_MEDIA
    }
    enum class Decision { KEEP_LOCAL, OWNER_APPROVAL_REQUIRED, ELIGIBLE_FOR_SYNC, BLOCKED }

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
        val important: Boolean
    )

    fun decision(
        item: Item,
        config: Config,
        ownerApprovedForThisItem: Boolean = false
    ): Decision {
        if (item.id.isBlank() || item.sizeBytes < 0L) return Decision.BLOCKED
        if (!item.important) return Decision.KEEP_LOCAL
        if (!config.enabled || !config.ownerApprovedForProvider) return Decision.KEEP_LOCAL
        if (!ownerApprovedForThisItem) return Decision.OWNER_APPROVAL_REQUIRED
        if (item.dataClass == DataClass.OTHER_MEDIA) return Decision.KEEP_LOCAL
        return Decision.ELIGIBLE_FOR_SYNC
    }

    fun requiresExplicitOwnerApprovalForEveryUpload(): Boolean = true
    fun shouldFilterBeforeSync(item: Item): Boolean = !item.important
    fun providerCanBeChanged(): Boolean = true
    fun accountCanBeChanged(): Boolean = true

    fun approvalRule(): String =
        "Before every cloud save, show the Owner what will be saved, its data type and approximate size, " +
            "then wait for explicit approval. Without approval, keep it local."

    fun storagePolicy(): String =
        "Local working memory first; filter duplicates and low-value data. No blanket cloud-upload permission. " +
            "Never purchase extra storage automatically."

    fun privacyRule(): String =
        "Do not upload passwords, OTPs, tokens, raw biometric templates, or unrelated private data."

    fun storageLimitRule(): String =
        "When cloud storage is near full, stop non-essential uploads and notify the Owner; never buy storage."

    fun googleDriveScope(): String = "https://www.googleapis.com/auth/drive.file"
}
