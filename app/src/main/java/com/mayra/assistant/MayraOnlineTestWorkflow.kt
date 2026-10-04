package com.mayra.assistant

object MayraOnlineTestWorkflow {
 enum class Mode { PREPARATION, AUTHORIZED_ASSISTANCE, HUMAN_ONLY }
 data class TestPlan(val title:String,val mode:Mode,val allowedActions:List<String>,val blockedActions:List<String>)
 fun plan(title:String,mode:Mode):TestPlan=TestPlan(title.ifBlank{"Online Test"},mode,
  when(mode){Mode.PREPARATION->listOf("study","practice","explain");Mode.AUTHORIZED_ASSISTANCE->listOf("explain","organize","review");Mode.HUMAN_ONLY->listOf("prepare")}, 
  listOf("impersonate candidate","bypass proctoring","bypass CAPTCHA/OTP","submit without authorization"))
}