package com.mayra.assistant
object MayraReleasePackage {
 data class Artifact(val name:String,val required:Boolean)
 fun manifest()=listOf(
  Artifact("Android APK",true),Artifact("Windows agent",true),Artifact("Project documentation",true),
  Artifact("Security policy",true),Artifact("Backup/source history",true)
 )
 fun ready(available:Set<String>)=manifest().filter{it.required}.all{it.name in available}
 fun rule()="Release package contains source, build artifacts, documentation and security policies; secrets are excluded."
}