package com.mayra.assistant

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.RecognizerIntent
import android.widget.*
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : FragmentActivity() {
    private val prefs by lazy { getSharedPreferences("mayra_secure", MODE_PRIVATE) }
    private val voiceRequestCode = 7001
    private var responseTts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOwnerLock()
    }

    private fun showOwnerLock() {
        val layout = baseLayout()
        layout.addView(TextView(this).apply { text = "মায়রা"; textSize = 32f })
        layout.addView(TextView(this).apply {
            text = "\nকোনো installation password বা date-based setup code নেই।\nOwner authentication-এর মাধ্যমে Mayra চালু হবে.\nOwner verify না হলে privileged কাজ বন্ধ থাকবে."
            textSize = 17f
        })
        layout.addView(Button(this).apply { text = "Owner Verify"; setOnClickListener { authenticateOwner() } })
        setContentView(layout)
    }

    private fun authenticateOwner() {
        val manager = BiometricManager.from(this)
        if (manager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            ) != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "এই ডিভাইসে biometric/device authentication প্রস্তুত নেই.", Toast.LENGTH_LONG).show()
            return
        }
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                prefs.edit().putBoolean("owner_verified", true).apply()
                startLockedVoiceMode()
                showAssistant()
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Mayra Owner Verification")
            .setSubtitle("Fingerprint/face/device credential দিয়ে Owner যাচাই করুন")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            ).build()
        prompt.authenticate(info)
    }

    private fun startLockedVoiceMode() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), voiceRequestCode)
            return
        }
        ContextCompat.startForegroundService(this, Intent(this, LockedVoiceService::class.java))
    }

    private fun showAssistant() {
        val layout = baseLayout()
        layout.addView(TextView(this).apply { text = "মায়রা প্রস্তুত ✓"; textSize = 30f })
        layout.addView(TextView(this).apply {
            text = "\nOwner: গোপাল বসাক\n\nআমি আপনার ব্যক্তিগত AI assistant-এর Android test build.\nআপনি আমাকে বাংলা, English বা हिन्दी-তে কমান্ড দিতে পারেন."
            textSize = 17f
        })
        layout.addView(Button(this).apply { text = "🎙️ Voice Command"; setOnClickListener { startVoiceCommand() } })
        layout.addView(TextView(this).apply {
            text = "\n🔒 Locked Voice Mode: ON\nলক অবস্থায় শুধু “মায়রা, কাহাঁপে হো?” এবং একই অর্থের বাংলা/English/Hindi নির্দিষ্ট wake phrase-এ উত্তর দেবে। অন্য কোনো কাজ করবে না এবং ফোন unlock করতে পারবে না."
            textSize = 15f
        })
        layout.addView(TextView(this).apply {
            text = "\n🔒 Payment Safety: ON\nMayra কোনো payment/banking/wallet app automate করে টাকা পাঠানো বা লেনদেন শুরু করতে পারবে না."
            textSize = 15f
        })
        layout.addView(TextView(this).apply { text = "\nভাষা"; textSize = 18f })
        val language = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("বাংলা", "English", "हिन्दी"))
        }
        layout.addView(language)

        layout.addView(sectionButton("আমার Biodata / Career Profile") {
            showModule("Biodata & Career Profile", "এখানে আপনার আসল biodata, education, experience, skills, certificates এবং career preferences রাখা হবে.\n\nএখনো আপনার প্রকৃত biodata এখানে যোগ করা হয়নি.")
        })
        layout.addView(sectionButton("Job Watcher") {
            showModule("Job Watcher", "পরবর্তী ধাপে আপনার career profile অনুযায়ী job/freelancing opportunity search, duplicate filtering এবং notification যুক্ত হবে.")
        })
        layout.addView(sectionButton("🧵 Textile Design Studio — OFFLINE") {
            showModule(
                TextileDesignStudio.title,
                TextileDesignStudio.capabilities.joinToString("\n• ", prefix = "• ") +
                    "\n\n" + TextileDesignStudio.note
            )
        })
        layout.addView(sectionButton("Excel / Data Analysis") {
            showModule("Excel / Data Analysis", "পরবর্তী ধাপে Excel formulas, data cleaning, lookup, Pivot Table, charts, dashboards এবং analysis workflow যুক্ত হবে.")
        })
        layout.addView(sectionButton("Self-Learning / Teaching") {
            showModule("Self-Learning", "Mayra নতুন knowledge discover → cross-check → test → আপনাকে জানাবে → আপনার অনুমতি পেলে knowledge base-এ যোগ করবে.")
        })
        layout.addView(sectionButton("Phone → Computer Pair") {
            showModule("Phone → Computer Pair", "Windows agent তৈরি হলে secure pairing-এর মাধ্যমে ফোন থেকে কম্পিউটারে command পাঠানো যাবে.\n\nপ্রধান সংযোগ: Wi-Fi/Internet; Bluetooth optional.")
        })
        layout.addView(sectionButton("🛡️ Cybersecurity / Security Check") {
            showModule("Cybersecurity Mode", "শুধু আপনার নিজের বা স্পষ্ট অনুমতি থাকা ডিভাইস, নেটওয়ার্ক ও ওয়েবসাইটে defensive security check করা যাবে.\n\nযা থাকবে: security configuration review, port/service inventory, authorized vulnerability assessment, log ও suspicious activity analysis, malware/security hygiene checks, এবং CTF/private lab practice.\n\nপ্রতিটি কাজের আগে Owner authorization, target এবং scope যাচাই বাধ্যতামূলক. Password/OTP চুরি, authentication bypass, malware deployment বা অনুমতি ছাড়া access করা যাবে না.")
        })
        setContentView(ScrollView(this).apply { addView(layout) })
    }

    private fun sectionButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        setOnClickListener { action() }
    }

    private fun showModule(title: String, details: String) {
        val layout = baseLayout()
        layout.addView(TextView(this).apply { text = title; textSize = 28f })
        layout.addView(TextView(this).apply { text = "\n$details"; textSize = 17f })
        layout.addView(Button(this).apply { text = "← Mayra Home"; setOnClickListener { showAssistant() } })
        setContentView(layout)
    }

    private fun startVoiceCommand() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), voiceRequestCode)
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "মায়রাকে বলুন...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        try { startActivityForResult(intent, voiceRequestCode) }
        catch (_: Exception) { Toast.makeText(this, "এই ফোনে voice recognition service পাওয়া যাচ্ছে না.", Toast.LENGTH_LONG).show() }
    }

    @Deprecated("Deprecated Android callback retained for broad device compatibility.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == voiceRequestCode && resultCode == RESULT_OK) {
            val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) executeVoiceCommand(spoken)
        }
    }

    private fun executeVoiceCommand(spoken: String) {
        val result = VoiceCommandEngine.parse(spoken)
        when (result.action) {
            VoiceCommandResult.Action.OPEN_SETTINGS -> startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
            VoiceCommandResult.Action.OPEN_BROWSER -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")))
            VoiceCommandResult.Action.OPEN_CAMERA -> startActivity(Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE))
            VoiceCommandResult.Action.SHOW_TIME -> {
                val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Calendar.getInstance().time)
                showVoiceResult(result.response + "\nএখন সময়: " + time)
                speakResponse(result.response + " এখন সময় " + time)
                return
            }
            VoiceCommandResult.Action.SHOW_HELP, VoiceCommandResult.Action.NONE -> {
                showVoiceResult(result.response)
                speakResponse(result.response)
                return
            }
        }
        showVoiceResult(result.response)
        speakResponse(result.response)
    }

    private fun showVoiceResult(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun speakResponse(message: String) {
        responseTts?.shutdown()
        responseTts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = when {
                    message.contains(Regex("[\\u0980-\\u09FF]")) -> Locale("bn", "IN")
                    message.contains(Regex("[\\u0900-\\u097F]")) -> Locale("hi", "IN")
                    else -> Locale.US
                }
                val r = responseTts?.setLanguage(locale)
                if (r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED) {
                    responseTts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "mayra_command_response")
                }
            }
        }
    }

    override fun onDestroy() {
        responseTts?.shutdown()
        responseTts = null
        super.onDestroy()
    }
    private fun baseLayout() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(48, 64, 48, 48)
    }
}
