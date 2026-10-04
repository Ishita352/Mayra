package com.mayra.assistant

object MayraNotificationScheduler {
 data class Reminder(val id:String,val title:String,val triggerAtMs:Long,val enabled:Boolean=true)
 fun validate(r:Reminder):Boolean=r.id.isNotBlank()&&r.title.isNotBlank()&&r.triggerAtMs>0
 fun nextDue(reminders:List<Reminder>,nowMs:Long):Reminder?=reminders.filter{it.enabled&&it.triggerAtMs>=nowMs}.minByOrNull{it.triggerAtMs}
 fun policy()="Mayra reminders are user-created; background work must respect Android power/notification permissions and never perform financial transactions."
}