package com.mayra.assistant
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
class MayraLockedPhoneVoiceGateTest{
 @Test fun lockedModeNeedsOwnerVerification(){val p=ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("lock-test",Context.MODE_PRIVATE);LockModePolicy.setEnabled(p,true);assertEquals(MayraLockedPhoneVoiceGate.Decision.OWNER_VERIFICATION_REQUIRED,MayraLockedPhoneVoiceGate.decide(p,false,true,true,"status"))}
 @Test fun sensitiveTaskBlocked(){val p=ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("lock-test-2",Context.MODE_PRIVATE);LockModePolicy.setEnabled(p,true);assertEquals(MayraLockedPhoneVoiceGate.Decision.BLOCK,MayraLockedPhoneVoiceGate.decide(p,true,true,true,"payment"))}
}