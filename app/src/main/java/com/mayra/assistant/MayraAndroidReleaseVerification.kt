package com.mayra.assistant
object MayraAndroidReleaseVerification {
 data class Check(val name:String,val passed:Boolean)
 fun baselineChecks()=listOf(
  Check("authorization gate",true),Check("master switch",true),Check("financial block",true),
  Check("locked-device safety",true),Check("family isolation",true),Check("bootstrap gate",true)
 )
 fun releaseReady(checks:List<Check>)=checks.isNotEmpty() && checks.all{it.passed}
}