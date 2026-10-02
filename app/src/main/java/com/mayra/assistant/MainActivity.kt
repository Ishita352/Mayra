package com.mayra.assistant

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private val prefs by lazy { getSharedPreferences("mayra_secure", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!prefs.getBoolean("paired", false)) showSetup() else showOwnerLock()
    }

    private fun showSetup() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 72, 48, 48)
        }
        val title = TextView(this).apply {
            text = "মায়রা — প্রথম Setup"
            textSize = 28f
        }
        val info = TextView(this).apply {
            text = "\nএই ডিভাইসটি প্রথমবার pair করার জন্য আজকের installation date ভিত্তিক setup code ব্যবহার করুন।\n\nআজকের code format: MAYRA-02102026"
            textSize = 17f
        }
        val pair = Button(this).apply {
            text = "আমি Owner — Pair করুন"
            setOnClickListener {
                prefs.edit().putBoolean("paired", true).apply()
                showOwnerLock()
            }
        }
        layout.addView(title)
        layout.addView(info)
        layout.addView(pair)
        setContentView(layout)
    }

    private fun showOwnerLock() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 72, 48, 48)
        }
        val title = TextView(this).apply {
            text = "মায়রা"
            textSize = 32f
        }
        val status = TextView(this).apply {
            text = "\nOwner authentication required.\n\nOwner verify না হলে privileged কাজ বন্ধ থাকবে।"
            textSize = 17f
        }
        val unlock = Button(this).apply {
            text = "Owner Verify"
            setOnClickListener { authenticateOwner() }
        }
        layout.addView(title)
        layout.addView(status)
        layout.addView(unlock)
        setContentView(layout)
    }

    private fun authenticateOwner() {
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    showAssistant()
                }
            })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Mayra Owner Verification")
            .setSubtitle("Fingerprint বা device biometric দিয়ে Owner যাচাই করুন")
            .setNegativeButtonText("Cancel")
            .build()
        prompt.authenticate(info)
    }

    private fun showAssistant() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 72, 48, 48)
        }
        layout.addView(TextView(this).apply {
            text = "মায়রা প্রস্তুত ✓"
            textSize = 28f
        })
        layout.addView(TextView(this).apply {
            text = "\nOwner verified.\n\n• Voice Assistant\n• Interview Assist\n• Phone → Computer control\n• Secure pairing\n\nএই MVP-তে privileged action শুধু Owner authentication-এর পরে চালু হবে।"
            textSize = 17f
        })
        val interview = Button(this).apply {
            text = "Interview Assist"
            setOnClickListener {
                // Future module: explicit user-started transcription/answer assistance.
            }
        }
        layout.addView(interview)
        setContentView(layout)
    }
}
