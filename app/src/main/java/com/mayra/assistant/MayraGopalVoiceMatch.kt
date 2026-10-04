package com.mayra.assistant

import android.content.Context
import java.io.File

data class GopalVoiceMatchStatus(val sampleReady:Boolean,val durationMs:Long,val fileSizeBytes:Long,val qualityScore:Int,val message:String)

object MayraGopalVoiceMatch {
 fun sampleFile(c:Context)=File(c.filesDir,"gopal_voice_match.m4a")
 fun saveAnalysis(c:Context,d:Long,s:Long):GopalVoiceMatchStatus{val q=when{d<8000L->35;d<15000L->65;d<30000L->85;else->95}.let{if(s<20000L)(it-20).coerceAtLeast(0)else it};c.getSharedPreferences("mayra_secure",0).edit().putBoolean("gopal_voice_match_ready",q>=65).putInt("gopal_voice_match_quality",q).putLong("gopal_voice_match_duration",d).apply();return status(c)}
 fun status(c:Context):GopalVoiceMatchStatus{val p=c.getSharedPreferences("mayra_secure",0);val f=sampleFile(c);val ok=p.getBoolean("gopal_voice_match_ready",false)&&f.exists();val d=p.getLong("gopal_voice_match_duration",0);val s=if(f.exists())f.length()else 0;val q=p.getInt("gopal_voice_match_quality",0);return GopalVoiceMatchStatus(ok,d,s,q,if(ok)"Sample ready for owner-voice analysis."else"Record a clear 15–30 second sample.")}
 fun deleteSample(c:Context){sampleFile(c).delete();c.getSharedPreferences("mayra_secure",0).edit().remove("gopal_voice_match_ready").remove("gopal_voice_match_quality").remove("gopal_voice_match_duration").apply()}
}