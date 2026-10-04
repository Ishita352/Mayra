package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraCentralMemoryVaultTest {
    private val config = MayraCentralMemoryVault.Config(
        enabled = true,
        ownerApprovedForProvider = true
    )

    private fun item() = MayraCentralMemoryVault.Item(
        id = "memory-1",
        dataClass = MayraCentralMemoryVault.DataClass.CORE_MEMORY,
        sizeBytes = 100,
        important = true
    )

    @Test fun everyCloudSaveRequiresSpecificOwnerApproval() {
        assertEquals(
            MayraCentralMemoryVault.Decision.OWNER_APPROVAL_REQUIRED,
            MayraCentralMemoryVault.decision(item(), config)
        )
        assertEquals(
            MayraCentralMemoryVault.Decision.ELIGIBLE_FOR_SYNC,
            MayraCentralMemoryVault.decision(item(), config, ownerApprovedForThisItem = true)
        )
        assertTrue(MayraCentralMemoryVault.requiresExplicitOwnerApprovalForEveryUpload())
    }

    @Test fun unimportantMediaStaysLocal() {
        val item = item().copy(
            id = "video-1",
            dataClass = MayraCentralMemoryVault.DataClass.IMPORTANT_VIDEO,
            important = false
        )
        assertEquals(
            MayraCentralMemoryVault.Decision.KEEP_LOCAL,
            MayraCentralMemoryVault.decision(item, config, true)
        )
    }

    @Test fun providerAndAccountAreReplaceable() {
        assertTrue(MayraCentralMemoryVault.providerCanBeChanged())
        assertTrue(MayraCentralMemoryVault.accountCanBeChanged())
        assertEquals(
            "https://www.googleapis.com/auth/drive.file",
            MayraCentralMemoryVault.googleDriveScope()
        )
    }

    @Test fun cloudSyncNeverBuysStorage() {
        assertTrue(MayraCentralMemoryVault.storagePolicy().contains("Never purchase"))
        assertTrue(MayraCentralMemoryVault.storageLimitRule().contains("notify"))
    }
}
