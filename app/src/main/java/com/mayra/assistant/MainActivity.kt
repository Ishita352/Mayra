package com.mayra.assistant

import android.Manifest
import android.content.Intent
import android.provider.OpenableColumns
import android.app.KeyguardManager
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
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : FragmentActivity() {
    private val prefs by lazy { getSharedPreferences("mayra_secure", MODE_PRIVATE) }
    private val voiceRequestCode = 7001
    private val notificationRequestCode = 7002
    private var responseTts: TextToSpeech? = null
    private var masterSwitch: Switch? = null
    private var pendingPdfText: String? = null
    private var pendingDocxText: String? = null
    private var pendingDocxEditText: String? = null
    private var pendingPdfEditText: String? = null
    private var pendingDocxPdfText: String? = null

    private val documentPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            showVoiceResult("কোনো document নির্বাচন করা হয়নি।")
            return@registerForActivityResult
        }
        val mimeType = contentResolver.getType(uri)
        val allowed = mimeType == "application/pdf" ||
            mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
            mimeType == "text/plain"
        if (!allowed) {
            showVoiceResult("এই document type Mayra এখনো গ্রহণ করছে না। PDF, DOCX বা TXT নির্বাচন করুন।")
            return@registerForActivityResult
        }
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) { }
        val name = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
            ?: uri.lastPathSegment ?: "Selected document"
        when (mimeType) {
            "text/plain" -> {
                val result = DocumentTextReader.read(contentResolver, uri)
                if (result.success) {
                    showModule("TXT Document Read", "ফাইল: $name\n\n" + result.message + "\n\n--- Preview ---\n" +
                        DocumentTextReader.preview(result.text) +
                        "\n\nএই ধাপে file read-only ছিল; Mayra file-এর কোনো content পরিবর্তন করেনি.")
                } else showVoiceResult(result.message)
            }
            "application/pdf" -> {
                val result = DocumentPdfReader.read(contentResolver, uri, this)
                if (result.success) {
                    showModule("PDF Document Read", "ফাইল: $name\n\n" + result.message + "\n\n--- Preview ---\n" +
                        DocumentPdfReader.preview(result.text) +
                        "\n\nএই ধাপে PDF read-only ছিল; original file পরিবর্তন করা হয়নি.")
                } else showVoiceResult(result.message)
            }
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                val result = DocumentDocxReader.read(contentResolver, uri)
                if (result.success) {
                    showModule("DOCX Document Read", "ফাইল: $name\n\n" + result.message + "\n\n--- Text Preview ---\n" +
                        DocumentTextReader.preview(result.text) +
                        "\n\nএই ধাপে DOCX read-only ছিল; original file পরিবর্তন করা হয়নি। Basic text structure রাখা হয়েছে, কিন্তু advanced formatting/layout preserve করা হয়নি.")
                } else showVoiceResult(result.message)
            }
            else -> showVoiceResult("এই document type Mayra এখনো গ্রহণ করছে না।")
        }
    }

    private val txtConvertPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            showVoiceResult("কোনো TXT source নির্বাচন করা হয়নি।")
            return@registerForActivityResult
        }
        if (contentResolver.getType(uri) != "text/plain") {
            showVoiceResult("Conversion-এর source হিসেবে শুধু TXT document নির্বাচন করুন।")
            return@registerForActivityResult
        }
        val result = DocumentTextReader.read(contentResolver, uri)
        if (!result.success) {
            showVoiceResult(result.message)
            return@registerForActivityResult
        }
        pendingPdfText = result.text
        pdfCreatePicker.launch("Mayra-document.pdf")
    }

    private val docxOpenPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            showVoiceResult("কোনো DOCX নির্বাচন করা হয়নি।")
            return@registerForActivityResult
        }
        if (contentResolver.getType(uri) != "application/vnd.openxmlformats-officedocument.wordprocessingml.document") {
            showVoiceResult("শুধু DOCX document নির্বাচন করুন।")
            return@registerForActivityResult
        }
        val result = DocumentDocxReader.read(contentResolver, uri)
        if (!result.success) {
            showVoiceResult(result.message)
            return@registerForActivityResult
        }
        showDocxEditor(result.text)
    }

    private val docxEditPicker = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    ) { uri ->
        val text = pendingDocxEditText
        pendingDocxEditText = null
        if (uri == null || text == null) {
            showVoiceResult("Edited DOCX save করা হয়নি।")
            return@registerForActivityResult
        }
        showVoiceResult(DocumentDocxWriter.write(contentResolver, uri, text).message)
    }

    private val docxCreatePicker = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    ) { uri ->
        val text = pendingDocxText
        pendingDocxText = null
        if (uri == null || text == null) {
            showVoiceResult("DOCX output তৈরি করা হয়নি।")
            return@registerForActivityResult
        }
        showVoiceResult(DocumentDocxWriter.write(contentResolver, uri, text).message)
    }

    private val docxPdfOpenPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) { showVoiceResult("কোনো DOCX নির্বাচন করা হয়নি।"); return@registerForActivityResult }
        if (contentResolver.getType(uri) != "application/vnd.openxmlformats-officedocument.wordprocessingml.document") {
            showVoiceResult("শুধু DOCX document নির্বাচন করুন।"); return@registerForActivityResult
        }
        val result = DocumentDocxReader.read(contentResolver, uri)
        if (!result.success) { showVoiceResult(result.message); return@registerForActivityResult }
        pendingDocxPdfText = result.text
        pdfCreatePicker.launch("Mayra-docx-converted.pdf")
    }

    private val pdfEditOpenPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) { showVoiceResult("কোনো PDF নির্বাচন করা হয়নি।"); return@registerForActivityResult }
        if (contentResolver.getType(uri) != "application/pdf") {
            showVoiceResult("শুধু PDF document নির্বাচন করুন।"); return@registerForActivityResult
        }
        val result = DocumentPdfReader.read(contentResolver, uri, this)
        if (!result.success) { showVoiceResult(result.message); return@registerForActivityResult }
        showPdfEditor(result.text)
    }

    private val pdfCreatePicker = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val text = pendingDocxPdfText ?: pendingPdfEditText ?: pendingPdfText
        pendingDocxPdfText = null
        pendingPdfEditText = null
        pendingPdfText = null
        if (uri == null || text == null) {
            showVoiceResult("PDF output তৈরি করা হয়নি।")
            return@registerForActivityResult
        }
        val result = DocumentConversionEngine.convertTextToPdf(contentResolver, uri, text)
        showVoiceResult(result.message)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (prefs.getBoolean("owner_verified", false)) showAssistant() else showFirstOwnerVerification()
    }

    private fun showFirstOwnerVerification() {
        val layout = baseLayout()
        layout.addView(TextView(this).apply { text = "মায়রা"; textSize = 32f })
        layout.addView(TextView(this).apply {
            text = "\\nএটি একবারের Owner verification। Face/Fingerprint biometric দিয়ে Owner হিসেবে যাচাই করুন।\\n\\nPattern/PIN/Password fallback থাকবে না। সফল হলে পরবর্তীতে Mayra আর authentication চাইবে না."
            textSize = 17f
        })
        layout.addView(Button(this).apply {
            text = "Face / Fingerprint দিয়ে শুরু করুন"
            setOnClickListener { authenticateOwnerOnce() }
        })
        setContentView(layout)
    }

    private fun authenticateOwnerOnce() {
        val manager = BiometricManager.from(this)
        if (manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "এই ফোনে supported strong biometric (Face/Fingerprint) সেটআপ নেই। ফোনের biometric settings-এ এটি আগে চালু করুন.", Toast.LENGTH_LONG).show()
            return
        }
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                prefs.edit().putBoolean("owner_verified", true).apply()
                showAssistant()
                speakResponse("স্বাগতম বস। মায়রা প্রস্তুত আছে।")
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Mayra Owner Verification")
            .setSubtitle("Face অথবা Fingerprint দিয়ে একবার Owner যাচাই করুন")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        prompt.authenticate(info)
    }

    private fun authenticateTemporaryOwner() {
        if (TemporaryOwnerAccessManager.isActive(this)) {
            showVoiceResult("Temporary Owner Mode ইতিমধ্যে সক্রিয় আছে।")
            return
        }
        val manager = BiometricManager.from(this)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        if (manager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            showVoiceResult("Temporary Owner verification-এর জন্য Face/Fingerprint অথবা ফোনের secure Pattern/PIN/Password দরকার।")
            return
        }
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                TemporaryOwnerAccessManager.start(this@MainActivity)
                showVoiceResult("Temporary Owner Mode চালু হয়েছে। সর্বোচ্চ ২৪ ঘণ্টা।")
                speakResponse("বস, Temporary Owner Mode চালু হয়েছে।")
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Mayra Temporary Owner Verification")
            .setSubtitle("Owner হিসেবে Face/Fingerprint অথবা ফোনের secure credential দিয়ে যাচাই করুন")
            .setAllowedAuthenticators(authenticators)
            .build()
        prompt.authenticate(info)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                notificationRequestCode
            )
        }
    }
    private fun showAssistant() {
        MayraNotificationCenter.ensureChannel(this)
        requestNotificationPermissionIfNeeded()
        if (prefs.getBoolean("master_on", true)) {
            BackgroundWorkCoordinator.scheduleDefaults(this)
        } else {
            BackgroundWorkCoordinator.cancelAll(this)
        }
        val layout = baseLayout()
        layout.addView(TextView(this).apply { text = "মায়রা প্রস্তুত ✓"; textSize = 30f })
        layout.addView(Switch(this).apply {
            text = "🔘 Mayra Master ON/OFF"
            isChecked = prefs.getBoolean("master_on", true)
            masterSwitch = this
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("master_on", checked).apply()
                if (!checked) {
                    BackgroundWorkCoordinator.cancelAll(this@MainActivity)
                    showVoiceResult("Mayra Master OFF — background jobs বন্ধ হয়েছে। কোনো memory বা saved progress মুছবে না।")
                } else {
                    BackgroundWorkCoordinator.scheduleDefaults(this@MainActivity)
                    showVoiceResult("Mayra Master ON — saved state রেখে background jobs আবার চালু হয়েছে।")
                }
            }
        })
        layout.addView(Switch(this).apply {
            text = "🎙️ Voice Command ON/OFF"
            isChecked = FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.VOICE_COMMAND)
            setOnCheckedChangeListener { _, checked ->
                FeatureToggleRegistry.setEnabled(prefs, FeatureToggleRegistry.VOICE_COMMAND, checked)
                showVoiceResult(if (checked) "Voice Command ON — এখন Mayra voice command গ্রহণ করবে।" else "Voice Command OFF — Home Page থেকে আবার ON না করা পর্যন্ত সাধারণ voice command বন্ধ।")
            }
        })
        layout.addView(Switch(this).apply {
            text = "📷 Camera ON/OFF"
            isChecked = FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.CAMERA)
            setOnCheckedChangeListener { _, checked ->
                FeatureToggleRegistry.setEnabled(prefs, FeatureToggleRegistry.CAMERA, checked)
                showVoiceResult(if (checked) "Camera ON — অনুমোদিত camera command চালু।" else "Camera OFF — সাধারণ camera command বন্ধ।")
            }
        })
        layout.addView(Switch(this).apply {
            text = "📞 Incoming Call Assistant ON/OFF"
            isChecked = FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.INCOMING_CALL_ASSISTANT)
            setOnCheckedChangeListener { _, checked ->
                FeatureToggleRegistry.setEnabled(prefs, FeatureToggleRegistry.INCOMING_CALL_ASSISTANT, checked)
                showVoiceResult(if (checked) "Incoming Call Assistant ON — Android-supported call workflow-এর জন্য প্রস্তুত।" else "Incoming Call Assistant OFF।")
            }
        })
        layout.addView(Switch(this).apply {
            text = "🛡️ Safety / Security Control ON/OFF"
            isChecked = FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.SECURITY)
            setOnCheckedChangeListener { _, checked ->
                FeatureToggleRegistry.setEnabled(prefs, FeatureToggleRegistry.SECURITY, checked)
                showVoiceResult(
                    if (checked) "Security Control ON — Mayra-এর নিরাপত্তা ও অনুমতি checks সক্রিয়।"
                    else "Security Control OFF — নিরাপত্তা-সংবেদনশীল action Mayra অনুমোদন করবে না।"
                )
            }
        })
        layout.addView(Switch(this).apply {
            text = "🔐 Locked Phone Mode — ভবিষ্যৎ locked-device execution"
            isChecked = LockModePolicy.isEnabled(prefs)
            setOnCheckedChangeListener { _, checked ->
                LockModePolicy.setEnabled(prefs, checked)
                if (checked) {
                    showVoiceResult("Locked Phone Mode ON — setting সংরক্ষিত। নিরাপদ locked-device execution path এখনো চালু নয়; ফোন locked থাকলে Mayra command চালাবে না।")
                } else {
                    showVoiceResult("Locked Phone Mode OFF — ফোন locked থাকলে Mayra কাজ করবে না।")
                }
            }
        })
        layout.addView(TextView(this).apply {
            text = "\nOwner: গোপাল বসাক\n\nআমি আপনার ব্যক্তিগত AI assistant-এর Android test build.\nআপনি আমাকে বাংলা, English বা हिन्दी-তে কমান্ড দিতে পারেন."
            textSize = 17f
        })
        layout.addView(Button(this).apply { text = "🎙️ Voice Command"; setOnClickListener { if (FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.VOICE_COMMAND)) startVoiceCommand() else showVoiceResult("Voice Command OFF — আগে Home Page থেকে ON করুন।") } })
        layout.addView(Button(this).apply { text = "👑 Temporary Owner Mode (24h)"; setOnClickListener { authenticateTemporaryOwner() } })
        layout.addView(TextView(this).apply {
            text = if (LockModePolicy.isEnabled(prefs)) {
                "\n🔐 Locked Phone Mode: ON\nSetting সংরক্ষিত আছে; ফোন locked থাকলে Mayra এখনো কোনো command চালাবে না।"
            } else {
                "\n🔒 Locked Phone Mode: OFF\nফোন locked থাকলে Mayra কাজ করবে না। কাজের জন্য ফোন unlock করতে হবে।"
            }
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
        layout.addView(sectionButton("💰 Active + Passive Income Watcher") {
            showModule("Income Engine", IncomeOpportunityPolicy.summary())
        })
        layout.addView(sectionButton("Job Watcher") {
            showModule("Job Watcher", "Remote/work-from-home/freelance opportunity scan হবে। USD income প্রথম অগ্রাধিকার; INR পাশাপাশি। কম সময়ে তুলনামূলক বেশি legitimate earning potential এবং hourly/repeatable work আগে দেখা হবে.")
        })
        layout.addView(sectionButton("📝 Online Test Participation") {
            showOnlineTestModule()
        })
        layout.addView(sectionButton("🧵 Textile Design Studio — OFFLINE") {
            showModule(
                TextileDesignStudio.title,
                TextileDesignStudio.capabilities.joinToString("\n• ", prefix = "• ") +
                    "\n\n" + TextileDesignStudio.note
            )
        })
        layout.addView(sectionButton("🧠 Core AI & Knowledge Engine") {
            showModule(
                "Core AI & Knowledge Engine",
                "Offline-first request understanding foundation. Documents, Excel, CV/Biodata, Jobs, Income, Interview, Learning, Textile ও Security domain চিনে নিরাপদ workflow নির্বাচন করে।\n\nএটি কোনো paid API বা network call করে না; বাস্তব file/network action পরে permission gates-এর পেছনে যুক্ত হবে."
            )
        })
        layout.addView(sectionButton("✏️ Edit DOCX") {
            docxOpenPicker.launch(arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        })
        layout.addView(sectionButton("📝 Create DOCX") {
            showDocxCreator()
        })
        layout.addView(sectionButton("🔄 Convert DOCX → PDF") {
            docxPdfOpenPicker.launch(arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        })
        layout.addView(sectionButton("📄 Create PDF (text-based)") {
            showPdfCreator()
        })
        layout.addView(sectionButton("✏️ Edit PDF (text-based)") {
            pdfEditOpenPicker.launch(arrayOf("application/pdf"))
        })
        layout.addView(sectionButton("📄 Documents — PDF / DOCX / TXT") {
            documentPicker.launch(arrayOf(
                "application/pdf",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "text/plain"
            ))
        })
        layout.addView(sectionButton("Excel / Data Analysis") {
            showModule("Excel / Data Analysis", "পরবর্তী ধাপে Excel formulas, data cleaning, lookup, Pivot Table, charts, dashboards এবং analysis workflow যুক্ত হবে.")
        })
        layout.addView(sectionButton("🤖 AI Training & Skill Engine") {
            showModule("AI Training & Skill Development", "English↔Hindi/Bengali voice, video dubbing/subtitles, speech-to-text, translation, text-to-speech, GPS/mapping workflows, remote-work tools, Excel/data analysis, textile/design tools এবং নতুন AI/model/tool শেখার জন্য Discover → Research → Cross-check → Sandbox Test → Verify → Save Knowledge → Apply workflow থাকবে. Free/open-source first.")
        })
        layout.addView(sectionButton("Self-Learning / Teaching") {
            showModule("Self-Learning", "Mayra নতুন knowledge discover → cross-check → test → আপনাকে জানাবে → আপনার অনুমতি পেলে knowledge base-এ যোগ করবে.")
        })
        layout.addView(sectionButton("💻 PC Control") {
            showModule("Mayra PC Control", "Windows 10 PC Companion-এর জন্য authenticated local control interface প্রস্তুত করা হয়েছে।\n\nএখনকার published capabilities allowlisted এবং security-gated; প্রকৃত Phone ↔ PC transport চালু হওয়ার আগে pairing দরকার।\n\nপরবর্তী transport ধাপে: secure pairing → trusted session → capability check → PC command → result ফেরত।\n\nকোনো arbitrary shell/PowerShell command বা Windows security bypass অনুমোদিত নয়.")
        })
        layout.addView(sectionButton("Phone → Computer Pair") {
            showModule("Phone → Computer Pair", "Windows agent তৈরি হলে secure pairing-এর মাধ্যমে ফোন থেকে কম্পিউটারে command পাঠানো যাবে.\n\nপ্রধান সংযোগ: Wi-Fi/Internet; Bluetooth optional.")
        })
        layout.addView(sectionButton("🛡️ Cybersecurity / Security Check") {
            showModule("Cybersecurity Mode", "শুধু আপনার নিজের বা স্পষ্ট অনুমতি থাকা ডিভাইস, নেটওয়ার্ক ও ওয়েবসাইটে defensive security check করা যাবে.\n\nযা থাকবে: security configuration review, port/service inventory, authorized vulnerability assessment, log ও suspicious activity analysis, malware/security hygiene checks, এবং CTF/private lab practice.\n\nপ্রতিটি কাজের আগে Owner authorization, target এবং scope যাচাই বাধ্যতামূলক. Password/OTP চুরি, authentication bypass, malware deployment বা অনুমতি ছাড়া access করা যাবে না.")
        })
        setContentView(ScrollView(this).apply { addView(layout) })
    }

    private fun showPdfCreator() {
        val layout = baseLayout()
        layout.addView(TextView(this).apply { text = "📄 Create PDF"; textSize = 28f })
        layout.addView(TextView(this).apply {
            text = "\nBasic text-based PDF creation. Layout/formatting is not preserved."
            textSize = 16f
        })
        val input = EditText(this).apply { hint = "PDF-এর text লিখুন..."; minLines = 10; gravity = android.view.Gravity.TOP }
        layout.addView(input)
        layout.addView(Button(this).apply {
            text = "💾 Save as PDF"
            setOnClickListener {
                val text = input.text.toString()
                if (text.isBlank()) { showVoiceResult("PDF content খালি রাখা যাবে না।"); return@setOnClickListener }
                pendingPdfText = text
                pdfCreatePicker.launch("Mayra-document.pdf")
            }
        })
        layout.addView(Button(this).apply { text = "← Mayra Home"; setOnClickListener { showAssistant() } })
        setContentView(ScrollView(this).apply { addView(layout) })
    }

    private fun showPdfEditor(initialText: String) {
        val layout = baseLayout()
        layout.addView(TextView(this).apply { text = "✏️ Edit PDF"; textSize = 28f })
        layout.addView(TextView(this).apply {
            text = "\nPDF text extraction করে edit করা হবে। Original layout/images/fonts/formatting preserve হবে না."
            textSize = 16f
        })
        val input = EditText(this).apply { setText(initialText); minLines = 12; gravity = android.view.Gravity.TOP }
        layout.addView(input)
        layout.addView(Button(this).apply {
            text = "💾 Save Edited PDF"
            setOnClickListener {
                val text = input.text.toString()
                if (text.isBlank()) { showVoiceResult("PDF content খালি রাখা যাবে না।"); return@setOnClickListener }
                pendingPdfEditText = text
                pdfCreatePicker.launch("Mayra-edited.pdf")
            }
        })
        layout.addView(Button(this).apply { text = "← Mayra Home"; setOnClickListener { showAssistant() } })
        setContentView(ScrollView(this).apply { addView(layout) })
    }

    private fun showDocxEditor(initialText: String) {
        val layout = baseLayout()
        layout.addView(TextView(this).apply {
            text = "✏️ Edit DOCX"
            textSize = 28f
        })
        layout.addView(TextView(this).apply {
            text = "\nBasic text editing is supported. Formatting/complex Word structure is not preserved."
            textSize = 16f
        })
        val input = EditText(this).apply {
            setText(initialText)
            minLines = 12
            gravity = android.view.Gravity.TOP
        }
        layout.addView(input)
        layout.addView(Button(this).apply {
            text = "💾 Save Edited DOCX"
            setOnClickListener {
                val text = input.text.toString()
                if (text.isBlank()) {
                    showVoiceResult("DOCX content খালি রাখা যাবে না।")
                    return@setOnClickListener
                }
                pendingDocxEditText = text
                docxEditPicker.launch("Mayra-edited.docx")
            }
        })
        layout.addView(Button(this).apply {
            text = "← Mayra Home"
            setOnClickListener { showAssistant() }
        })
        setContentView(ScrollView(this).apply { addView(layout) })
    }

    private fun showDocxCreator() {
        val layout = baseLayout()
        layout.addView(TextView(this).apply {
            text = "📝 Create DOCX"
            textSize = 28f
        })
        layout.addView(TextView(this).apply {
            text = "\nMayra basic text-based DOCX তৈরি করতে পারবে। Formatting/complex Word structure এখনো preserve করা হবে না."
            textSize = 16f
        })
        val input = EditText(this).apply {
            hint = "DOCX-এর text লিখুন..."
            minLines = 10
            gravity = android.view.Gravity.TOP
        }
        layout.addView(input)
        layout.addView(Button(this).apply {
            text = "💾 Save as DOCX"
            setOnClickListener {
                val text = input.text.toString()
                if (text.isBlank()) {
                    showVoiceResult("DOCX content খালি রাখা যাবে না।")
                    return@setOnClickListener
                }
                pendingDocxText = text
                docxCreatePicker.launch("Mayra-document.docx")
            }
        })
        layout.addView(Button(this).apply {
            text = "← Mayra Home"
            setOnClickListener { showAssistant() }
        })
        setContentView(ScrollView(this).apply { addView(layout) })
    }

    private fun showOnlineTestModule() {
        val layout = baseLayout()
        layout.addView(TextView(this).apply {
            text = "📝 Online Test Participation"
            textSize = 28f
        })
        layout.addView(TextView(this).apply {
            text = "\nMayra online test platform খুলতে, test instructions পড়তে, timer/status বুঝতে এবং—শুধু অনুমোদিত হলে—প্রশ্ন বোঝা ও উত্তর প্রস্তুতিতে সাহায্য করতে পারবে.\n\nAI নিষিদ্ধ হলে Mayra live answer দেবে না."
            textSize = 17f
        })
        layout.addView(TextView(this).apply {
            text = "\nTest provider-এর rules এখানে লিখুন/paste করুন:"
            textSize = 16f
        })
        val rulesInput = EditText(this).apply {
            hint = "যেমন: AI assistance allowed / No AI / external assistance..."
            minLines = 3
        }
        layout.addView(rulesInput)
        layout.addView(Button(this).apply {
            text = "🔎 Test Rules Check"
            setOnClickListener {
                val mode = OnlineTestPolicy.modeForRuleText(rulesInput.text.toString())
                showVoiceResult(OnlineTestPolicy.description(mode))
            }
        })
        layout.addView(Button(this).apply {
            text = "🧪 Preparation / Mock Test"
            setOnClickListener {
                showModule("Online Test — Preparation", "Practice questions, explanations, timing strategy, revision এবং mock test-এ Mayra সাহায্য করতে পারবে.")
            }
        })
        layout.addView(Button(this).apply {
            text = "✅ Authorized AI Assistance"
            setOnClickListener {
                showModule("Online Test — Authorized Assistance", "Test provider স্পষ্টভাবে AI assistance অনুমোদন করলে Mayra প্রশ্ন বোঝা, তথ্য/ব্যাখ্যা খোঁজা, উত্তর draft করা এবং test workflow-এ সহায়তা করতে পারবে.\n\nচূড়ান্ত submission-এর আগে user-এর নিয়ন্ত্রণ থাকবে.")
            }
        })
        layout.addView(Button(this).apply {
            text = "👤 Human-Only Test"
            setOnClickListener {
                showModule("Online Test — Human Only", "AI বা external assistance নিষিদ্ধ হলে Mayra live answer, answer selection বা test-taking automation করবে না. শুধু সাধারণ প্রস্তুতি ও প্রযুক্তিগত সহায়তা দিতে পারবে.")
            }
        })
        layout.addView(Button(this).apply {
            text = "← Mayra Home"
            setOnClickListener { showAssistant() }
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
        if (!prefs.getBoolean("master_on", true)) {
            showVoiceResult("Mayra Master OFF — কমান্ড চালানো যাবে না।")
            return
        }
        if (!FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.VOICE_COMMAND)) {
            showVoiceResult("Voice Command OFF — এই voice command চালানো যাবে না।")
            return
        }
        if (!FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.SECURITY)) {
            showVoiceResult("Security Control OFF — নিরাপত্তা checks সক্রিয় না থাকায় এই command চালানো যাবে না।");
            return
        }
        // Defense-in-depth: re-check the physical device lock immediately before execution.
        // Voice results can return after the phone transitions between locked/unlocked states.
        if (!DeviceSecurityGate.mayExecuteUserCommand(this)) {
            showVoiceResult(if (LockModePolicy.isEnabled(prefs)) {
                "ফোন locked — Locked Phone Mode চালু আছে, কিন্তু এই voice command-এর নিরাপদ locked-device execution path এখনো সম্পূর্ণভাবে সক্রিয় নয়।"
            } else {
                "ফোন locked — Mayra কোনো command চালাবে না। আগে ফোন unlock করুন।"
            })
            return
        }
        if (isFinishing) return
        if (TemporaryOwnerAccessManager.isActive(this)) {
            TemporaryOwnerAccessManager.record(this, "VOICE_COMMAND", "RECEIVED", spoken.take(300))
        }
        val result = VoiceCommandEngine.parse(spoken)
        when (result.action) {
            VoiceCommandResult.Action.OPEN_SETTINGS -> startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
            VoiceCommandResult.Action.OPEN_BROWSER -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")))
            VoiceCommandResult.Action.OPEN_CAMERA -> {
                if (FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.CAMERA)) {
                    startActivity(Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE))
                } else {
                    showVoiceResult("Camera OFF — আগে Camera ON করুন।")
                    speakResponse("বস, Camera এখন বন্ধ আছে।")
                }
            }
            VoiceCommandResult.Action.SET_VOICE_COMMAND -> {
                val wantsOff = spoken.lowercase(Locale.ROOT).contains("বন্ধ") ||
                    spoken.lowercase(Locale.ROOT).contains("off") ||
                    spoken.lowercase(Locale.ROOT).contains("disable") ||
                    spoken.lowercase(Locale.ROOT).contains("बंद")
                FeatureToggleRegistry.setEnabled(prefs, FeatureToggleRegistry.VOICE_COMMAND, !wantsOff)
                val msg = if (wantsOff) "Voice Command OFF — সাধারণ voice command বন্ধ করা হয়েছে।" else "Voice Command ON — voice command চালু হয়েছে।"
                showVoiceResult(msg)
                speakResponse(msg)
                return
            }
            VoiceCommandResult.Action.SET_CAMERA -> {
                val lower = spoken.lowercase(Locale.ROOT)
                val wantsOff = lower.contains("বন্ধ") || lower.contains("off") || lower.contains("disable") || lower.contains("बंद")
                FeatureToggleRegistry.setEnabled(prefs, FeatureToggleRegistry.CAMERA, !wantsOff)
                val msg = if (wantsOff) "Camera OFF — সাধারণ camera command বন্ধ।" else "Camera ON — সাধারণ camera command চালু।"
                showVoiceResult(msg)
                speakResponse(msg)
                return
            }
            VoiceCommandResult.Action.SET_INCOMING_CALL_ASSISTANT -> {
                val lower = spoken.lowercase(Locale.ROOT)
                val wantsOff = lower.contains("বন্ধ") || lower.contains("off") || lower.contains("disable") || lower.contains("बंद")
                FeatureToggleRegistry.setEnabled(prefs, FeatureToggleRegistry.INCOMING_CALL_ASSISTANT, !wantsOff)
                val msg = if (wantsOff) "Incoming Call Assistant OFF।" else "Incoming Call Assistant ON — Android-supported call workflow-এর জন্য প্রস্তুত।"
                showVoiceResult(msg)
                speakResponse(msg)
                return
            }
            VoiceCommandResult.Action.PAIR_COMPUTER -> {
                showModule("Phone ↔ Computer Pairing", "এখনো Windows agent ইনস্টল/সংযোগ করা হয়নি।\n\nপরবর্তী ধাপ:\n1. Windows কম্পিউটারে Mayra Windows agent তৈরি ও চালু করতে হবে।\n2. দুই ডিভাইসে অনুমোদিত pairing code দিয়ে সংযোগ করতে হবে।\n3. তারপরেই কম্পিউটারে command পাঠানো যাবে।\n\nএই মুহূর্তে কোনো কম্পিউটার command পাঠানো হয়নি।")
                speakResponse("বস, ফোন-কম্পিউটার pairing-এর জন্য Windows agent দরকার।")
                return
            }
            VoiceCommandResult.Action.COMPUTER_STATUS -> {
                showVoiceResult("কম্পিউটার: এখনো paired নয়। Windows agent ও secure pairing এখনও বাকি।")
                speakResponse("বস, কম্পিউটার এখনও paired নয়।")
                return
            }
            VoiceCommandResult.Action.COMPUTER_OPEN_BROWSER,
            VoiceCommandResult.Action.COMPUTER_FIND_FILE -> {
                showVoiceResult("এই কমান্ডটি বুঝেছি, কিন্তু কম্পিউটারে চালাইনি। Windows agent pairing এখনও বাকি।")
                speakResponse("বস, কম্পিউটার সংযোগ এখনও তৈরি হয়নি।")
                return
            }
            VoiceCommandResult.Action.SHOW_TIME -> {
                val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Calendar.getInstance().time)
                showVoiceResult(result.response + "\nএখন সময়: " + time)
                speakResponse(result.response + " এখন সময় " + time)
                return
            }
            VoiceCommandResult.Action.SHOW_HELP -> {
                showVoiceResult(result.response)
                speakResponse(result.response)
                return
            }
            VoiceCommandResult.Action.NONE -> {
                val core = CoreKnowledgeEngine.answer(spoken)
                val message = if (core.recognized) core.message else result.response
                showVoiceResult(message)
                speakResponse(message)
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