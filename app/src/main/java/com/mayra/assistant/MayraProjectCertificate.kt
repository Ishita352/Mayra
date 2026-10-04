package com.mayra.assistant
object MayraProjectCertificate {
 data class Certificate(val project:String,val owner:String,val completedRoadmap:Int,val totalRoadmap:Int)
 fun issue(owner:String,completed:Int,total:Int):Certificate? =
  if(owner.isNotBlank() && completed>=total) Certificate("Project Mayra",owner,completed,total) else null
 fun rule()="The final certificate is issued only after every active roadmap item is verified complete."
}