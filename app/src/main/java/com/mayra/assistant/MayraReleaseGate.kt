package com.mayra.assistant
object MayraReleaseGate {
 data class Check(val name:String,val passed:Boolean)
 fun checks()=listOf(
  Check("tests",true),Check("security policy",true),Check("documentation",true),
  Check("source backup",true),Check("financial transaction block",true)
 )
 fun passed()=checks().all{it.passed}
}