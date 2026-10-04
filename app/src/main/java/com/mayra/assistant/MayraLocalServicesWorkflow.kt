package com.mayra.assistant
object MayraLocalServicesWorkflow {
 enum class Kind { EMERGENCY, NEWS, GOVERNMENT }
 data class Request(val kind:Kind,val query:String,val location:String?)
 fun plan(r:Request)=when(r.kind){Kind.EMERGENCY->"Prioritize official emergency services; never impersonate or place calls without permission";Kind.NEWS->"Cross-check recent reliable sources and show publication time";Kind.GOVERNMENT->"Use official government sources; assist with forms, never bypass identity/OTP/CAPTCHA"}
}