package com.mayra.assistant
object MayraOfflineVoiceWorkflow {
 enum class Task{MEMORY_EDIT,DOCUMENT_EDIT,PDF_PROCESSING,SPREADSHEET_ANALYSIS,CV_EDITING,NOTE_CREATION,IMAGE_EDITING,VIDEO_EDITING,TEXTILE_DESIGN,KNOWLEDGE_REVIEW,VOICE_COMMAND,FILE_ORGANIZATION,CLOUD_UPLOAD}
 data class QueuedUpload(val itemId:String,val queuedAtMillis:Long,val approved:Boolean=false)
 enum class Decision{ALLOW_OFFLINE,QUEUE_FOR_LATER,OWNER_APPROVAL_REQUIRED,NETWORK_REQUIRED,BLOCKED}
 fun canWorkOffline(task:Task)=task!=Task.CLOUD_UPLOAD
 fun decide(task:Task,online:Boolean,ownerApproved:Boolean=false)=if(task==Task.CLOUD_UPLOAD){if(!online)Decision.QUEUE_FOR_LATER else if(!ownerApproved)Decision.OWNER_APPROVAL_REQUIRED else Decision.QUEUE_FOR_LATER}else Decision.ALLOW_OFFLINE
 fun voiceCommand(spoken:String):String?{val s=spoken.lowercase();return when{listOf("অফলাইনে কাজ করো","offline কাজ","work offline","offline mode").any{s.contains(it)}->"OFFLINE_MODE";listOf("অফলাইনের কাজ চালাও","offline task","অফলাইন কাজ শুরু").any{s.contains(it)}->"RUN_OFFLINE_TASKS";listOf("আপলোড পরে করো","পরে আপলোড করো","upload later","queue upload").any{s.contains(it)}->"QUEUE_UPLOAD";else->null}}
 fun queueItem(itemId:String,nowMillis:Long=System.currentTimeMillis())=if(itemId.isBlank())null else QueuedUpload(itemId,nowMillis,false)
 fun releaseQueued(item:QueuedUpload,online:Boolean,ownerApproved:Boolean)=if(!online)Decision.QUEUE_FOR_LATER else if(!ownerApproved)Decision.OWNER_APPROVAL_REQUIRED else Decision.QUEUE_FOR_LATER
 fun rule()="Offline work remains local; cloud transfer requires connectivity and per-item Owner approval."
 fun queueRule()="Queued uploads remain local until connectivity and Owner approval permit transfer."
}