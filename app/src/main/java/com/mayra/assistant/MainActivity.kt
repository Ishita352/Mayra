package com.mayra.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.*
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : FragmentActivity() {

    private val prefs by lazy { getSharedPreferences("mayra_secure", MODE_PRIVATE) }
    private val voiceRequestCode = 7001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!prefs.getBoolean("paired", false)) showSetup() else showOwnerLock()
    }

    private fun todayCode(): String =
        "MAYRA-" + SimpleDateFormat("ddMMyyyy", Locale.US).format(Date())

    private fun showSetup() {
        val layout = baseLayout()
        layout.addView(TextView(this).apply {
            text = "মায়রা — প্রথম Setup"
            textSize = 28f
        })
        layout.addView(TextView(this).apply {
            text = "\nপ্রথমবার এই ডিভাইস pair করতে আজকের installation-date code দিন.\n\nCode format: MAYRA-ddMMyyyy"
            textSize = 17f
        })
        val input = EditText(this).apply {
            hint = "যেমন: MAYRA-02102026"
            setSingleLine(true)
        }
        layout.addView(input)
        layout.addView(Button(this).apply {
            text = "Owner — Pair করুন"
            setOnClickListener {
                if (input.text.toString().trim().uppercase(Locale.US) == todayCode()) {
                    prefs.edit().putBoolean("paired", true).apply()
                    showOwnerLock()
                } else {
                    Toast.makeText(this@MainActivity, "Setup code সঠিক নয়।", Toast.LENGTH_SHORT).show()
                }
            }
        })
        layout.addView(TextView(this).apply {
            text = "\nনোট: এই test build-এ date-based setup code ব্যবহার করা হয়েছে। production build-এ secure device-key pairing থাকবে."
            textSize = 14f
        })
        setContentView(layout)
    }

    private fun showOwnerLock() {
        val layout = baseLayout()
        layout.addView(TextView(this).apply {
            text = "মায়রা"
            textSize = 32f
        })
        layout.addView(TextView(this).apply {
            text = "\nOwner authentication required.\nOwner verify না হলে privileged কাজ বন্ধ থাকবে."
            textSize = 17f
        })
        layout.addView(Button(this).apply {
            text = "Owner Verify"
            setOnClickListener { authenticateOwner() }
        })
        setContentView(layout)
    }

    private fun authenticateOwner() {
        val manager = BiometricManager.from(this)
        if (manager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            ) != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this@MainActivity, "এই ডিভাইসে biometric/device authentication প্রস্তুত নেই.", Toast.LENGTH_LONG).show()
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    prefs.edit().putBoolean("owner_verified", true).apply()
                    showAssistant()
                }
            })

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Mayra Owner Verification")
            .setSubtitle("Fingerprint/face/device credential দিয়ে Owner যাচাই করুন")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        prompt.authenticate(info)
    }

    private fun showAssistant() {
        val layout = baseLayout()

        layout.addView(TextView(this).apply {
            text = "মায়রা প্রস্তুত ✓"
            textSize = 30f
        })
        layout.addView(TextView(this).apply {
            text = "\nOwner: গোপাল বসাক\n\nআমি আপনার ব্যক্তিগত AI assistant-এর Android test build.\nআপনি আমাকে বাংলা, English বা हिन्दी-তে কমান্ড দিতে পারেন."
            textSize = 17f
        })

        layout.addView(Button(this).apply {
            text = "🎙️ Voice Command"
            setOnClickListener { startVoiceCommand() }
        })

        layout.addView(TextView(this).apply {
            text = "\n🔒 Payment Safety: ON\nMayra কোনো payment/banking/wallet app automate করে টাকা পাঠানো বা লেনদেন শুরু করতে পারবে না."
            textSize = 15f
        })

        layout.addView(TextView(this).apply {
            text = "\nভাষা"
            textSize = 18f
        })
        val language = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item,
                listOf("বাংলা", "English", "हिन्दी"))
        }
        layout.addView(language)

        layout.addView(sectionButton("আমার Biodata / Career Profile") {
            showModule("Biodata & Career Profile",
                "এখানে আপনার আসল biodata, education, experience, skills, certificates এবং career preferences রাখা হবে.\n\nএখনো আপনার প্রকৃত biodata এখানে যোগ করা হয়নি.")
        })
        layout.addView(sectionButton("Job Watcher") {
            showModule("Job Watcher",
                "পরবর্তী ধাপে আপনার career profile অনুযায়ী job/freelancing opportunity search, duplicate filtering এবং notification যুক্ত হবে.")
        })
        layout.addView(sectionButton("Excel / Data Analysis") {
            showModule("Excel / Data Analysis",
                "পরবর্তী ধাপে Excel formulas, data cleaning, lookup, Pivot Table, charts, dashboards এবং analysis workflow যুক্ত হবে.")
        })
        layout.addView(sectionButton("Self-Learning / Teaching") {
            showModule("Self-Learning",
                "Mayra নতুন knowledge discover → cross-check → test → আপনাকে জানাবে → আপনার অনুমতি পেলে knowledge base-এ যোগ করবে.")
        })
        layout.addView(sectionButton("Phone → Computer Pair") {
            showModule("Phone → Computer Pair",
                "Windows agent তৈরি হলে secure pairing-এর মাধ্যমে ফোন থেকে কম্পিউটারে command পাঠানো যাবে.\n\nপ্রধান সংযোগ: Wi-Fi/Internet; Bluetooth optional.")
        })

        setContentView(ScrollView(this).apply { addView(layout) })
    }

    private fun sectionButton(label: String, action: () -> Unit) =
        Button(this).apply {
            text = label
            setOnClickListener { action() }
        }

    private fun showModule(title: String, details: String) {
        val layout = baseLayout()
        layout.addView(TextView(this).apply {
            text = title
            textSize = 28f
        })
        layout.addView(TextView(this).apply {
            text = "\n$details"
            textSize = 17f
        })
        layout.addView(Button(this).apply {
            text = "← Mayra Home"
            setOnClickListener { showAssistant() }
        })
        setContentView(layout)
    }

    private fun startVoiceCommand() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), voiceRequestCode)
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "মায়রাকে বলুন...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        try {
            startActivityForResult(intent, voiceRequestCode)
        } catch (_: Exception) {
            Toast.makeText(this, "এই ফোনে voice recognition service পাওয়া যাচ্ছে না.", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Deprecated Android callback retained for broad device compatibility.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == voiceRequestCode && resultCode == RESULT_OK) {
            val spoken = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                Toast.makeText(this, "আপনি বলেছেন: $spoken", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun baseLayout() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(48, 64, 48, 48)
    }
}
