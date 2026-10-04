package com.mayra.assistant

import android.Manifest
import android.app.Activity
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

class MayraGopalVoiceMatchActivity:Activity(){
 private var recorder:MediaRecorder?=null;private lateinit var status:TextView;private lateinit var record:Button;private var started=0L
 override fun onCreate(b:Bundle?){super.onCreate(b);val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(30,40,30,30);setBackgroundColor(0xff070a16.toInt())};r.addView(TextView(this).apply{text="GOPAL VOICE MATCH";textSize=28f;setTextColor(-1)});r.addView(TextView(this).apply{text="আপনার অনুমতিতে 15–30 সেকেন্ড বাংলা, Hindi ও English বলুন।";textSize=16f;setTextColor(0xffdddddd.toInt());setPadding(0,20,0,20)});status=TextView(this).apply{text="Ready";setTextColor(0xff55ddff.toInt())};r.addView(status);record=Button(this).apply{text="START VOICE SAMPLE"};record.setOnClickListener{if(recorder==null)start()else stop()};r.addView(record);r.addView(Button(this).apply{text="CLOSE";setOnClickListener{finish()}});setContentView(r);permission()}
 private fun permission(){if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.RECORD_AUDIO),9101)}
 private fun start(){if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){permission();return};val f=MayraGopalVoiceMatch.sampleFile(this);recorder=MediaRecorder(this).apply{setAudioSource(MediaRecorder.AudioSource.MIC);setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);setAudioEncoder(MediaRecorder.AudioEncoder.AAC);setAudioSamplingRate(44100);setAudioEncodingBitRate(128000);setOutputFile(f.absolutePath);prepare();start()};started=System.currentTimeMillis();record.text="STOP & ANALYZE";status.text="Recording… target 15–30 seconds."}
 private fun stop(){try{recorder?.stop()}catch(_:RuntimeException){};recorder?.release();recorder=null;val x=MayraGopalVoiceMatch.saveAnalysis(this,System.currentTimeMillis()-started,MayraGopalVoiceMatch.sampleFile(this).length());status.text="Quality: "+x.qualityScore+"%\n"+x.message;record.text="RECORD AGAIN"}
 override fun onDestroy(){try{recorder?.stop()}catch(_:RuntimeException){};recorder?.release();super.onDestroy()}
}