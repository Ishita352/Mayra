package com.mayra.assistant

/**
 * Local-first central memory policy for Android + Windows 10.
 *
 * Local device storage remains the primary working memory. Only filtered,
 * important and Owner-approved data is eligible for central sync.
 * Google Drive is a replaceable provider, not a hard dependency.
 */
object MayraCentralMemoryVault {
    enum class Provider { GOOGLE_DRIVE, LOCAL_ONLY, FUTURE_PROVIDER }
    enum class DataClass {
        CORE_MEMORY, RESUME_STATE, KNOWLEDGE, CAREER, SKILL_EVIDENCE,
        IMPORTANT_PHOTO, IMPORTANT_VIDEO, DOCUMENT, OTHER_MEDIA
    }
    enum class Decision { KEEP_LOCAL, ELIGIBLE_FOR_SYNC, OWNER_APPROVAL_REQUIRED, BLOCKED }

    data class Config(
        val provider: Provider = Provider.GOOGLE_DRIVE,
        val accountEmail: String = "basakgopal571@gmail.com",
        val enabled: Boolean = false,
        val ownerApproved: Boolean = false,
        val maxCloudBytes: Long = 15L * 1024L * 1024L * 1024L
    )

    data class Item(
        val id: String,
        val dataClass: DataClass,
        val sizeBytes: Long,
        val important: Boolean,
        val ownerApproved: Boolean = false
    )

    fun decision(item: Item, config: Config): Decision {
        if (item.id.isBlank() || item.sizeBytes < 0L) return Decision.BLOCKED
        if (!item.important) return Decision.KEEP_LOCAL
        if (!config.enabled) return Decision.KEEP_LOCAL
        if (!config.ownerApproved || !item.ownerApproved) return Decision.OWNER_APPROVAL_REQUIRED
        if (item.dataClass == DataClass.OTHER_MEDIA) return Decision.KEEP_LOCAL
        return Decision.ELIGIBLE_FOR_SYNC
    }

    fun shouldFilterBeforeSync(item: Item): Boolean = !item.important

    fun providerCanBeChanged(): Boolean = true

    fun accountCanBeChanged(): Boolean = true

    fun storagePolicy(): String =
        "Android and Windows 10 keep local working memory; Mayra filters duplicates and low-value data before central sync. " +
            "Important data is synced only after Owner approval. Google Drive is replaceable. " +
            "Never purchase extra storage automatically."

    fun privacyRule(): String =
        "Use least-privilege cloud access. Do not upload passwords, OTPs, tokens, raw biometric templates, " +
            "or unrelated private data. Central sync must be Owner-controlled."

    fun storageLimitRule(): String =
        "Google Account free storage is shared across Drive, Gmail and Photos; when the configured quota is near full, " +
            "stop non-essential uploads and notify the Owner instead of buying storage."

    fun googleDriveScope(): String = "https://www.googleapis.com/auth/drive.file"
}
