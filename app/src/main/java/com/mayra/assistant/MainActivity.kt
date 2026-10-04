package com.mayra.assistant

import android.Manifest
import android.content.Intent
import android.provider.OpenableColumns
import android.app.KeyguardManager
import android.net.Uri
import android.provider.Settings
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
    private val sessionState by lazy { MayraSessionState(prefs) }
    private val familyAccountManager by lazy { MayraFamilyAccountManager(MayraFamilyAccountManager.SharedPreferencesStore(prefs)) }
    private val semanticMemory by lazy { MayraSemanticMemory(MayraSemanticMemory.SharedPreferencesStore(this)) }
    private val semanticMemoryBridge by lazy { MayraSemanticMemoryBridge(semanticMemory) }
    private val founderIdentity by lazy { MayraFounderIdentity(object : MayraFounderIdentity.Store { override fun get(key: String) = prefs.getString(key, null); override fun put(key: String, value: String) { prefs.edit().putString(key, value).apply() } }) }
    private var activeFamilySession: MayraFamilyAccountManager.AuthenticatedSession? = null
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
    private var capturingWhatsAppReply = false
    private var welcomePendingAfterLock = false
    private var welcomeShownForCurrentUnlock = false

    private val excelPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) { showVoiceResult("কোনো spreadsheet নির্বাচন করা হয়নি।"); return@registerForActivityResult }
        try {
            val type = contentResolver.getType(uri)
            val data = contentResolver.openInputStream(uri)?.use { input ->
                if (type == "text/csv" || type == "text/comma-separated-values" || uri.toString().lowercase().endsWith(".csv")) {
                    SpreadsheetDocumentEngine.readDelimited(input)
                } else if (type == "text/tab-separated-values" || uri.toString().lowercase().endsWith(".tsv")) {
                    SpreadsheetDocumentEngine.readDelimited(input, '\t')
                } else {
                    SpreadsheetDocumentEngine.readXlsx(input)
                }
            } ?: throw IllegalArgumentException("Spreadsheet file পড়া যায়নি।")
            val validation = SpreadsheetDocumentEngine.validate(data)
            showModule("Excel / Spreadsheet Validation", validation.message + "\n\n--- Preview ---\n" + SpreadsheetDocumentEngine.preview(data))
        } catch (e: Exception) {
            showVoiceResult("Spreadsheet validation ব্যর্থ: " + (e.message ?: "অজানা error"))
        }
    }

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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!prefs.getBoolean("owner_verified", false)) return
        val keyguard = getSystemService(KeyguardManager::class.java)
        if (!hasFocus && keyguard.isKeyguardLocked) {
            welcomePendingAfterLock = true
            welcomeShownForCurrentUnlock = false
        } else if (hasFocus && !keyguard.isKeyguardLocked && welcomePendingAfterLock && !welcomeShownForCurrentUnlock) {
            welcomePendingAfterLock = false
            welcomeShownForCurrentUnlock = true
            speakResponse("Welcome, Boss! বলুন, কী সাহায্য করতে পারি?")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MayraMemoryStore.initialize(this)
        if (!MayraInstallationBootstrapManager(this).isCompleted()) {
            showInstallationBootstrap()
        } else if (prefs.getBoolean("owner_verified", false)) {
            restoreLastSession()
        } else {
            showFirstOwnerVerification()
        }
    }

    private fun showInstallationBootstrap() {
        val manager = MayraInstallationBootstrapManager(this)
        val layout = baseLayout()
        layout.addView(TextView(this).apply { text = "মায়রা — Installation / Pairing Setup"; textSize = 28f })
        layout.addView(TextView(this).apply {
            text = "\nপ্রথম installation/pairing-এর সময় Mayra একটি date-based bootstrap password চাইবে.\n\nInstallation date: " +
                manager.installationDateLabel() +
                "\nPassword format: MAYRA-YYYYMMDD"
            textSize = 16f
        })
        val password = EditText(this).apply {
            hint = "Bootstrap password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        layout.addView(password)
        layout.addView(Button(this).apply {
            text = "Verify & Continue"
            setOnClickListener {
                if (manager.verify(password.text.toString())) {
                    manager.markCompleted()
                    showFirstOwnerVerification()
                } else {
                    showVoiceResult("Bootstrap password সঠিক নয়। Installation date অনুযায়ী password দিন।")
                }
            }
        })
        setContentView(layout)
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
                founderIdentity.recognizeVerifiedOwner(MayraFounderIdentity.VerificationMethod.ANDROID_BIOMETRIC)
                showAssistant()
                speakResponse("Welcome, Boss! বলুন, কী সাহায্য করতে পারি?")
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
    private fun restoreLastSession() {
        if (!prefs.getBoolean("master_on", true)) {
            showAssistant(saveSession = false)
            return
        }
        if (sessionState.hasResumeState()) {
            val semanticResume = semanticMemoryBridge.resumeSummary()
            val restoredTitle = semanticResume?.substringBefore("\n")?.ifBlank { sessionState.title() } ?: sessionState.title()
            val restoredDetails = semanticResume?.substringAfter("\n", "")?.ifBlank { sessionState.details() } ?: sessionState.details()
            showModule(
                restoredTitle,
                restoredDetails + "\n\nMayra restored this saved semantic session context after restart."
            )
        } else {
            showAssistant()
        }
    }

    private fun showAssistant(saveSession: Boolean = true) {
        if (saveSession) { sessionState.saveHome(); semanticMemoryBridge.saveHomeResume() }

        MayraNotificationCenter.ensureChannel(this)
        requestNotificationPermissionIfNeeded()
        if (prefs.getBoolean("master_on", true)) {
            BackgroundWorkCoordinator.scheduleDefaults(this)
        } else {
            BackgroundWorkCoordinator.cancelAll(this)
        }

        // Home is intentionally minimal. Owner will define the only visible
        // feature buttons later; all other operations are intended for voice control.
        val layout = baseLayout()
        layout.addView(TextView(this).apply {
            text = "মায়রা প্রস্তুত ✓"
            textSize = 30f
        })
        layout.addView(TextView(this).apply {
            text = "Founder / Owner: ${founderIdentity.profile().displayName}"
            textSize = 16f
        })
        layout.addView(TextView(this).apply {
            text = "\nHome screen-এর পুরনো ON/OFF switches এবং feature buttons সরিয়ে দেওয়া হয়েছে।\n\nআপনি পরে যে button-এর নাম দেবেন, শুধু সেগুলিই Home screen-এ যোগ করা হবে। অন্য কাজ voice command-এর মাধ্যমে করা হবে।\n\n🔒 Payment Safety: ON — Mayra কোনো payment/banking/wallet transaction শুরু করবে না।"
            textSize = 17f
        })
        layout.addView(Button(this).apply {
            text = if (prefs.getBoolean("master_on", true)) "🟢 মায়রা চালু আছে — বন্ধ করুন" else "🔴 মায়রা বন্ধ আছে — চালু করুন"
            textSize = 18f
            setOnClickListener { toggleMayraMaster() }
        })
        layout.addView(TextView(this).apply {
            text = "\nOFF করলে active work, voice execution ও background work থামবে। Memory, knowledge, settings এবং saved session মুছে যাবে না। ON করলে আগের সংরক্ষিত state থেকেই resume হবে।"
            textSize = 16f
        })
        setContentView(ScrollView(this).apply { addView(layout) })
    }
    private fun toggleMayraMaster() {
        val currentlyOn = prefs.getBoolean("master_on", true)
        if (currentlyOn) {
            sessionState.saveHome()
            semanticMemoryBridge.saveHomeResume()
            prefs.edit().putBoolean("master_on", false).apply()
            BackgroundWorkCoordinator.cancelAll(this)
            showAssistant(saveSession = false)
            showVoiceResult("Mayra বন্ধ করা হয়েছে। Memory, knowledge ও saved state সংরক্ষিত আছে।")
        } else {
            prefs.edit().putBoolean("master_on", true).apply()
            showAssistant(saveSession = false)
            showVoiceResult("Mayra আবার চালু হয়েছে এবং আগের saved state/knowledge থেকেই resume করছে।")
        }
    }
    private fun sectionButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        setOnClickListener { action() }
    }

    private fun showModule(title: String, details: String) {
        sessionState.saveModule(title, details)
        semanticMemoryBridge.saveModuleResume(title, details)
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
            if (!spoken.isNullOrBlank()) {
                if (capturingWhatsAppReply) {
                    capturingWhatsAppReply = false
                    sendWhatsAppReply(spoken)
                } else {
                    executeVoiceCommand(spoken)
                }
            }
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
            VoiceCommandResult.Action.REPLY_WHATSAPP -> {
                capturingWhatsAppReply = true
                showVoiceResult("WhatsApp reply-এর text বলুন।")
                speakResponse("বস, কী reply পাঠাব?")
                startVoiceCommand()
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

    private fun sendWhatsAppReply(text: String) {
        if (!prefs.getBoolean("master_on", true) || !prefs.getBoolean("owner_verified", false)) {
            showVoiceResult("Mayra এখন reply পাঠানোর জন্য প্রস্তুত নয়.")
            return
        }
        if (!FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.SECURITY)) {
            showVoiceResult("Security Control OFF — WhatsApp reply blocked.")
            return
        }
        val sent = MayraNotificationListenerService.replyFromOwnerCommand(this, text)
        val msg = if (sent) "WhatsApp reply পাঠানো হয়েছে।" else "WhatsApp-এর reply action পাওয়া যায়নি। আগে WhatsApp notification access ON করুন এবং একটি reply-capable notification আসতে দিন।"
        showVoiceResult(msg)
        speakResponse(msg)
    }

    private fun showContentCreationIntelligence() {
        val layout = baseLayout()
        layout.addView(TextView(this).apply { text = "🎬 Video / Photo / Social Media Work"; textSize = 28f })
        layout.addView(TextView(this).apply {
            text = "\nMayra-তে video editing, photo editing, YouTube content work এবং social-media account handling-এর কাজের workflow যোগ হয়েছে. আপনার আগের অভিজ্ঞতাও skill-matching-এ ব্যবহার করা যাবে, তবে নতুন experience বানিয়ে বলা হবে না."
            textSize = 17f
        })
        val areas = listOf(
            MayraContentCreationIntelligence.Area.VIDEO_EDITING,
            MayraContentCreationIntelligence.Area.PHOTO_EDITING,
            MayraContentCreationIntelligence.Area.YOUTUBE,
            MayraContentCreationIntelligence.Area.SOCIAL_MEDIA_MANAGEMENT
        )
        areas.forEach { area ->
            val plan = MayraContentCreationIntelligence.plan(area)
            layout.addView(TextView(this).apply {
                text = "\n$area\nSkills: " + plan.skills.joinToString(", ") +
                    "\nDeliverables: " + plan.deliverables.joinToString(", ")
                textSize = 15f
            })
        }
        layout.addView(TextView(this).apply {
            text = "\nআপনার existing experience:\n" +
                MayraContentCreationIntelligence.ownerExperienceProfile().existingSkills.joinToString(" • ") +
                "\n\nClient-work rule:\n" + MayraContentCreationIntelligence.clientWorkRule()
            textSize = 15f
        })
        layout.addView(Button(this).apply { text = "← Mayra Home"; setOnClickListener { showAssistant() } })
        setContentView(ScrollView(this).apply { addView(layout) })
    }

    private fun showPublicInfoLookup() {
        val layout = baseLayout()
        layout.addView(TextView(this).apply { text = "🔎 Public Information Lookup"; textSize = 28f })
        layout.addView(TextView(this).apply {
            text = "\nMayra শুধু publicly available information খুঁজবে। Private identity, private address, live location বা hidden Telegram admin details বের করবে না."
            textSize = 16f
        })
        val number = EditText(this).apply { hint = "WhatsApp number (country code সহ)" }
        layout.addView(number)
        layout.addView(Button(this).apply {
            text = "Search public WhatsApp information"
            setOnClickListener {
                if (number.text.toString().isBlank()) showVoiceResult("WhatsApp number দিন।")
                else MayraPublicInfoLookup.openWhatsAppNumberPublicSearch(this@MainActivity, number.text.toString())
            }
        })
        val channel = EditText(this).apply { hint = "Telegram channel @name বা public name" }
        layout.addView(channel)
        layout.addView(Button(this).apply {
            text = "Search public Telegram channel information"
            setOnClickListener {
                if (channel.text.toString().isBlank()) showVoiceResult("Telegram channel name দিন।")
                else MayraPublicInfoLookup.openTelegramChannelPublicSearch(this@MainActivity, channel.text.toString())
            }
        })
        layout.addView(Button(this).apply { text = "← Mayra Home"; setOnClickListener { showAssistant() } })
        setContentView(ScrollView(this).apply { addView(layout) })
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