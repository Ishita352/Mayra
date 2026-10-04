package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraCentralMemoryVaultTest {
    @Test fun importantApprovedMemoryCanBeEligibleForSync() {
        val item = MayraCentralMemoryVault.Item(
            id = "memory-1",
            dataClass = MayraCentralMemoryVault.DataClass.CORE_MEMORY,
            sizeBytes = 100,
            important = true,
            ownerApproved = true
        )
        val config = MayraCentralMemoryVault.Config(
            enabled = true,
            ownerApproved = true
        )
        assertEquals(
            MayraCentralMemoryVault.Decision.ELIGIBLE_FOR_SYNC,
            MayraCentralMemoryVault.decision(item, config)
        )
    }

    @Test fun unimportantMediaStaysLocal() {
        val item = MayraCentralMemoryVault.Item(
            id = "video-1",
            dataClass = MayraCentralMemoryVault.DataClass.IMPORTANT_VIDEO,
            sizeBytes = 1000,
            important = false
        )
        val config = MayraCentralMemoryVault.Config(enabled = true, ownerApproved = true)
        assertEquals(
            MayraCentralMemoryVault.Decision.KEEP_LOCAL,
            MayraCentralMemoryVault.decision(item, config)
        )
    }

    @Test fun providerAndAccountAreReplaceable() {
        assertTrue(MayraCentralMemoryVault.providerCanBeChanged())
        assertTrue(MayraCentralMemoryVault.accountCanBeChanged())
        assertEquals("https://www.googleapis.com/auth/drive.file", MayraCentralMemoryVault.googleDriveScope())
    }

    @Test fun cloudSyncNeverBuysStorage() {
        assertTrue(MayraCentralMemoryVault.storagePolicy().contains("Never purchase"))
        assertTrue(MayraCentralMemoryVault.storageLimitRule().contains("notify"))
    }
}
