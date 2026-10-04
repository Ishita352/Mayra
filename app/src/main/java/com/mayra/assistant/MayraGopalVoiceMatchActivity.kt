package com.mayra.assistant

import android.Manifest
import android.app.Activity
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

class MayraGopalVoiceMatchActivity : Activity() {
    private var recorder: MediaRecorder? = null
    private lateinit var status: TextView
    private lateinit var timer: TextView
    private lateinit var record: Button
    private var startedAt = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val autoStop = Runnable { if (recorder != null) stopRecording() }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 40, 30, 30)
            setBackgroundColor(0xff070a16.toInt())
        }
        root.addView(TextView(this).apply { text = "GOPAL VOICE MATCH"; textSize = 28f; setTextColor(-1) })
        root.addView(TextView(this).apply {
            text = "Owner consent required. Record one clear 15–30 second sample containing Bengali + Hindi + English. Avoid music and heavy background noise."
            textSize = 16f; setTextColor(0xffdddddd.toInt()); setPadding(0, 20, 0, 20)
        })
        timer = TextView(this).apply { text = "00:00 / 00:30"; textSize = 20f; setTextColor(0xff55ddff.toInt()) }
        root.addView(timer)
        status = TextView(this).apply { text = "Ready — minimum 15 seconds"; setTextColor(0xff55ddff.toInt()) }
        root.addView(status)
        record = Button(this).apply { text = "START VOICE SAMPLE" }
        record.setOnClickListener { if (recorder == null) startRecording() else stopRecording() }
        root.addView(record)
        root.addView(Button(this).apply {
            text = "DELETE SAVED SAMPLE"
            setOnClickListener { MayraGopalVoiceMatch.deleteSample(this@MayraGopalVoiceMatchActivity); status.text = "Saved sample deleted. Ready for a new sample."; timer.text = "00:00 / 00:30" }
        })
        root.addView(Button(this).apply { text = "CLOSE"; setOnClickListener { finish() } })
        setContentView(root)
        requestPermissionIfNeeded()
    }

    private fun requestPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 9101)
    }

    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { requestPermissionIfNeeded(); return }
        val file = MayraGopalVoiceMatch.sampleFile(this)
        runCatching {
            recorder = MediaRecorder(this).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC); setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC); setAudioSamplingRate(44100); setAudioEncodingBitRate(128000)
                setOutputFile(file.absolutePath); prepare(); start()
            }
        }.onFailure {
            recorder?.release(); recorder = null
            status.text = "Recording could not start. Check microphone permission and try again."
            return
        }
        startedAt = System.currentTimeMillis()
        record.text = "STOP & ANALYZE"
        status.text = "Recording… keep speaking until at least 15 seconds."
        handler.postDelayed(autoStop, MayraGopalVoiceMatch.MAX_SAMPLE_MS)
        updateTimer()
    }

    private fun updateTimer() {
        if (recorder == null) return
        val elapsed = System.currentTimeMillis() - startedAt
        val seconds = (elapsed / 1000L).coerceAtMost(30L)
        timer.text = String.format(java.util.Locale.US, "%02d:%02d / 00:30", seconds / 60, seconds % 60)
        handler.postDelayed({ updateTimer() }, 250L)
    }

    private fun stopRecording() {
        handler.removeCallbacks(autoStop)
        val elapsed = System.currentTimeMillis() - startedAt
        val active = recorder; recorder = null
        runCatching { active?.stop() }; active?.release()
        val file = MayraGopalVoiceMatch.sampleFile(this)
        if (elapsed < MayraGopalVoiceMatch.MIN_SAMPLE_MS) {
            file.delete(); status.text = "Rejected: sample must be at least 15 seconds."; record.text = "RECORD AGAIN"; return
        }
        val result = MayraGopalVoiceMatch.saveAnalysis(this, elapsed, file.length())
        status.text = "Quality: " + result.qualityScore + "%\n" + result.message
        record.text = "RECORD AGAIN"
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        val active = recorder; recorder = null
        runCatching { active?.stop() }; active?.release()
        super.onDestroy()
    }
}