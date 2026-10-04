package com.mayra.assistant
import org.junit.Assert.assertEquals
import org.junit.Test
class MayraDriveUploadAuthorizationTest {
 @Test fun approvedImportantOnlineItemMayReachUploadAdapter(){val i=MayraCentralMemoryVault.Item("d1",MayraCentralMemoryVault.DataClass.DOCUMENT,10,MayraCentralMemoryVault.Importance.IMPORTANT);val c=MayraCentralMemoryVault.Config(enabled=true,ownerApprovedForProvider=true);assertEquals(MayraDriveUploadAuthorization.Decision.ALLOW_UPLOAD,MayraDriveUploadAuthorization.decide(i,c,true,true))}
 @Test fun unapprovedItemCannotUpload(){val i=MayraCentralMemoryVault.Item("d2",MayraCentralMemoryVault.DataClass.DOCUMENT,10,MayraCentralMemoryVault.Importance.IMPORTANT);val c=MayraCentralMemoryVault.Config(enabled=true,ownerApprovedForProvider=true);assertEquals(MayraDriveUploadAuthorization.Decision.ASK_OWNER,MayraDriveUploadAuthorization.decide(i,c,true,false))}
 @Test fun offlineApprovedItemQueues(){val i=MayraCentralMemoryVault.Item("d3",MayraCentralMemoryVault.DataClass.DOCUMENT,10,MayraCentralMemoryVault.Importance.IMPORTANT);val c=MayraCentralMemoryVault.Config(enabled=true,ownerApprovedForProvider=true);assertEquals(MayraDriveUploadAuthorization.Decision.QUEUE_FOR_LATER,MayraDriveUploadAuthorization.decide(i,c,false,true))}
}