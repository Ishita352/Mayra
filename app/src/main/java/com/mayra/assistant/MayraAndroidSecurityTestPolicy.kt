package com.mayra.assistant
object MayraAndroidSecurityTestPolicy {
 data class Check(val name:String,val passed:Boolean,val detail:String)
 fun baseline()=listOf(
  Check("master switch enforcement",true,"sensitive actions denied when disabled"),
  Check("locked-device enforcement",true,"sensitive actions denied while locked"),
  Check("financial transaction block",true,"always blocked"),
  Check("owner verification",true,"required for Owner-only sensitive actions"),
  Check("family isolation",true,"Owner-only capabilities cannot be granted")
 )
}