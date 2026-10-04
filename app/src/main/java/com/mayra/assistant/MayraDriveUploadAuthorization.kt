package com.mayra.assistant

/** Final authorization boundary before an actual Google Drive upload adapter is invoked. */
object MayraDriveUploadAuthorization {
    enum class Decision { ALLOW_UPLOAD, QUEUE_FOR_LATER, ASK_OWNER, BLOCK }

    fun decide(item: MayraCentralMemoryVault.Item, config: MayraCentralMemoryVault.Config, online: Boolean, ownerApprovedForItem: Boolean): Decision {
        return when (MayraCentralMemoryVault.decision(item, config, ownerApprovedForItem)) {
            MayraCentralMemoryVault.Decision.ELIGIBLE_FOR_SYNC -> if (online) Decision.ALLOW_UPLOAD else Decision.QUEUE_FOR_LATER
            MayraCentralMemoryVault.Decision.ASK_OWNER_IMPORTANCE,
            MayraCentralMemoryVault.Decision.OWNER_APPROVAL_REQUIRED -> Decision.ASK_OWNER
            MayraCentralMemoryVault.Decision.DO_NOT_SAVE,
            MayraCentralMemoryVault.Decision.BLOCKED -> Decision.BLOCK
        }
    }

    fun scope(): String = MayraCentralMemoryVault.googleDriveScope()
    fun rule(): String = "No Drive upload adapter may proceed unless this boundary returns ALLOW_UPLOAD for the specific item."
}
