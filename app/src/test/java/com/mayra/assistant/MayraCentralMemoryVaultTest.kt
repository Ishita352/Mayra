package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraCentralMemoryVaultTest {
    private val config = MayraCentralMemoryVault.Config(
        enabled = true,
        ownerApprovedForProvider = true
    )

    private fun item(
        importance: MayraCentralMemoryVault.Importance
    ) = MayraCentralMemoryVault.Item(
        id = "memory-1",
        dataClass = MayraCentralMemoryVault.DataClass.CORE_MEMORY,
        sizeBytes = 100,
        importance = importance
    )

    @Test fun uncertainImportanceAsksOwnerBeforeAnySave() {
        assertEquals(
            MayraCentralMemoryVault.Decision.ASK_OWNER_IMPORTANCE,
            MayraCentralMemoryVault.decision(
                item(MayraCentralMemoryVault.Importance.UNCERTAIN),
                config
            )
        )
        assertTrue(MayraCentralMemoryVault.uncertainImportanceRequiresOwnerQuestion())
    }

    @Test fun notImportantDataIsNotSavedAnywhere() {
        assertEquals(
            MayraCentralMemoryVault.Decision.DO_NOT_SAVE,
            MayraCentralMemoryVault.decision(
                item(MayraCentralMemoryVault.Importance.NOT_IMPORTANT),
                config
            )
        )
        assertTrue(MayraCentralMemoryVault.localSaveAllowedOnlyWhenImportant())
    }

    @Test fun importantCloudSaveNeedsSpecificOwnerApproval() {
        assertEquals(
            MayraCentralMemoryVault.Decision.OWNER_APPROVAL_REQUIRED,
            MayraCentralMemoryVault.decision(
                item(MayraCentralMemoryVault.Importance.IMPORTANT),
                config
            )
        )
        assertEquals(
            MayraCentralMemoryVault.Decision.ELIGIBLE_FOR_SYNC,
            MayraCentralMemoryVault.decision(
                item(MayraCentralMemoryVault.Importance.IMPORTANT),
                config,
                ownerApprovedForThisItem = true
            )
        )
        assertTrue(MayraCentralMemoryVault.requiresExplicitOwnerApprovalForEveryCloudUpload())
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
