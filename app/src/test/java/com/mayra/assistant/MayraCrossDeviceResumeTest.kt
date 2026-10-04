package com.mayra.assistant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
class MayraCrossDeviceResumeTest {
 @Test fun verifiedPairedDevicesCanResume(){val e=MayraCrossDeviceResume.create("t1","Excel analysis","continue rows","phone",1);assertNotNull(e);assertEquals(MayraCrossDeviceResume.Decision.ALLOW_RESUME,MayraCrossDeviceResume.decide(e,true,true))}
 @Test fun unverifiedOrUnpairedCannotResume(){val e=MayraCrossDeviceResume.create("t2","CV","continue","phone",1);assertEquals(MayraCrossDeviceResume.Decision.OWNER_APPROVAL_REQUIRED,MayraCrossDeviceResume.decide(e,false,true));assertEquals(MayraCrossDeviceResume.Decision.OWNER_APPROVAL_REQUIRED,MayraCrossDeviceResume.decide(e,true,false))}
}