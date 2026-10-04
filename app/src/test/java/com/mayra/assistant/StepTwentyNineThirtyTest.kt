package com.mayra.assistant
import org.junit.Assert.*
import org.junit.Test
class StepTwentyNineThirtyTest {
 @Test fun crossDeviceRequiresSecureState(){assertTrue(MayraCrossDeviceWorkflow.allowed(MayraCrossDeviceWorkflow.Request(true,true,true,"sync")))}
 @Test fun windowsFullBaselinePasses(){assertTrue(MayraWindowsFullTesting.allPassed())}
}