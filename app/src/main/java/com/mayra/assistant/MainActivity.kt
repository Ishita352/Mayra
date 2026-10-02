package com.mayra.assistant

import android.os.Bundle
import android.widget.*
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : FragmentActivity() {

    private val prefs by lazy { getSharedPreferences("mayra_secure", MODE_PRIVATE) }

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
            text = "\nপ্রথমবার এই ডিভাইস pair করতে আজকের installation-date code দিন।\n\nCode format: MAYRA-ddMMyyyy"
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
            text = "\nনোট: date-only code সুবিধাজনক, কিন্তু শক্তিশালী security নয়। পরের ধাপে secure device-key pairing যোগ করা হবে।"
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
            text = "\nOwner authentication required.\nOwner verify না হলে privileged কাজ বন্ধ থাকবে।"
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
            Toast.makeText(this, "এই ডিভাইসে biometric/device authentication প্রস্তুত নেই।", Toast.LENGTH_LONG).show()
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    showAssistant()
                }
            }
        )

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
            textSize = 28f
        })
        layout.addView(TextView(this).apply {
            text = "\nOwner verified. এটি প্রথম Android test build।\n\nমূল modules-এর foundation:\n• বাংলা / हिन्दी / English\n• Biodata & Career Profile\n• Job Watcher foundation\n• Excel / Data Analysis assistant foundation\n• Self-learning / teaching foundation\n• Phone → Computer pairing architecture"
            textSize = 16f
        })
        val language = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item,
                listOf("বাংলা", "English", "हिन्दी"))
        }
        layout.addView(TextView(this).apply { text = "\nভাষা"; textSize = 18f })
        layout.addView(language)

        layout.addView(Button(this).apply {
            text = "আমার Biodata / Career Profile"
            setOnClickListener {
                Toast.makeText(this@MainActivity, "পরের build-এ আপনার আসল biodata যোগ করা হবে।", Toast.LENGTH_LONG).show()
            }
        })
        layout.addView(Button(this).apply {
            text = "Job Watcher"
            setOnClickListener {
                Toast.makeText(this@MainActivity, "Job Watcher-এর automation পরের ধাপে যুক্ত হবে।", Toast.LENGTH_LONG).show()
            }
        })
        layout.addView(Button(this).apply {
            text = "Excel / Data Analysis"
            setOnClickListener {
                Toast.makeText(this@MainActivity, "Excel/Data Analysis module-এর engine পরের ধাপে যুক্ত হবে।", Toast.LENGTH_LONG).show()
            }
        })
        layout.addView(Button(this).apply {
            text = "Phone → Computer Pair"
            setOnClickListener {
                Toast.makeText(this@MainActivity, "Windows agent তৈরি হলে secure pairing এখানে চালু হবে।", Toast.LENGTH_LONG).show()
            }
        })
        setContentView(layout)
    }

    private fun baseLayout() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(48, 64, 48, 48)
    }
}
