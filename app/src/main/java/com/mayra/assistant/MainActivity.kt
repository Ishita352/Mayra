package com.mayra.assistant

import android.Manifest
import android.content.Intent
import android.provider.OpenableColumns
import android.app.KeyguardManager
import android.net.Uri
import android.provider.Settings
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.RecognizerIntent
import android.speech.tts.UtteranceProgressListener
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
    private val windowsPairingSession by lazy { MayraWindowsPairingSession(object : MayraWindowsPairingSession.Store {
        override fun get(key: String): String? = prefs.getString(key, null)
        override fun put(key: String, value: String) { prefs.edit().putString(key, value).apply() }
        override fun remove(key: String) { prefs.edit().remove(key).apply() }
    }) }
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
            speakResponse("Welcome Boss, বলুন কী সাহায্য করতে পারি")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MayraMemoryStore.initialize(this)
        MayraOwnerBiodataPortfolio.seedCareerProfileIfMissing(object : MayraCareerProfileStore.Store {
            override fun read(key: String): String? = prefs.getString(key, null)
            override fun write(key: String, value: String) { prefs.edit().putString(key, value).apply() }
        })
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
            text = "\nPassword-based installation bootstrap সম্পূর্ণভাবে disabled। Owner verification-ই Mayra-র identity gate।\n\nStatus: " +
                manager.installationDateLabel()
            textSize = 16f
        })
        layout.addView(Button(this).apply {
            text = "Continue to Owner Verification"
            setOnClickListener {
                manager.markCompleted()
                showFirstOwnerVerification()
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
                speakResponse("Welcome Boss, বলুন কী সাহায্য করতে পারি")
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
            text = "\nOFF করলে active work, voice execution ও background work থামবে। Memory, knowledge, settings এবং saved session মুছে যাবে না। ON করলে আগের সংরক্ষিত state থেকেই resume হবে."
            textSize = 16f
        })
        addNewOwnerControls(layout)
        setContentView(ScrollView(this).apply { addView(layout) })
    }
    private fun addNewOwnerControls(layout: LinearLayout) {
        val controls = listOf(
            MayraUserControlCenter.CAMERA,
            MayraUserControlCenter.INCOMING_CALLS,
            MayraUserControlCenter.THREE_D_CHARACTER,
            MayraUserControlCenter.VOICE_LIGHT,
            MayraUserControlCenter.WHATSAPP_IMPORTANT,
            MayraUserControlCenter.VOICE_COMMAND_ACCESS,
            MayraUserControlCenter.LOCKED_PHONE_ACTIVE,
            MayraUserControlCenter.SILENT_MODE,
            MayraUserControlCenter.MAYRA_VOLUME
        )
        controls.forEach { control ->
            layout.addView(Button(this).apply {
                text = MayraUserControlCenter.label(control) + if (isControlEnabled(control)) " — ON" else " — OFF"
                textSize = 17f
                setOnClickListener { toggleOwnerControl(control) }
            })
        }
        layout.addView(TextView(this).apply {
            text = "\nএই পাঁচটি control voice command দিয়েও চালু/বন্ধ করা যাবে। গুরুত্বপূর্ণ কাজের জন্য Mayra প্রয়োজনীয় Android/WhatsApp permission চাইবে; কোনো covert access নয়."
            textSize = 15f
        })
    }

    private fun isControlEnabled(control: String): Boolean = when (control) {
        MayraUserControlCenter.CAMERA -> FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.CAMERA)
        MayraUserControlCenter.INCOMING_CALLS -> FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.INCOMING_CALL_ASSISTANT)
        MayraUserControlCenter.THREE_D_CHARACTER -> prefs.getBoolean("mayra_3d_character_enabled", false)
        MayraUserControlCenter.VOICE_LIGHT -> prefs.getBoolean("mayra_voice_light_enabled", true)
        MayraUserControlCenter.WHATSAPP_IMPORTANT -> prefs.getBoolean("mayra_whatsapp_important_enabled", true)
        MayraUserControlCenter.VOICE_COMMAND_ACCESS -> FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.VOICE_COMMAND)
        MayraUserControlCenter.LOCKED_PHONE_ACTIVE -> prefs.getBoolean("mayra_locked_phone_active", false)
        MayraUserControlCenter.SILENT_MODE -> prefs.getBoolean("mayra_silent_mode_behavior", true)
        MayraUserControlCenter.MAYRA_VOLUME -> true
        else -> false
    }

    private fun toggleOwnerControl(control: String) {
        if (!prefs.getBoolean("master_on", true)) {
            showVoiceResult("Mayra Master OFF — এই control পরিবর্তন করা যাবে না।")
            return
        }
        val enabled = !isControlEnabled(control)
        MayraUserControlCenter.set(prefs, control, enabled)
        val message = when (control) {
            MayraUserControlCenter.CAMERA -> if (enabled) "Camera Access ON।" else "Camera Access OFF।"
            MayraUserControlCenter.INCOMING_CALLS -> if (enabled) "Incoming Call Assistant ON। Android-এর অনুমতি ও supported telecom capability অনুযায়ী call receive/answer করা যাবে।" else "Incoming Call Assistant OFF।"
            MayraUserControlCenter.THREE_D_CHARACTER -> if (enabled) "3D Character ON।" else "3D Character OFF।"
            MayraUserControlCenter.VOICE_LIGHT -> if (enabled) "Voice Light ON। Mayra কথা বলার সময় visual light indication দেখাবে।" else "Voice Light OFF।"
            MayraUserControlCenter.WHATSAPP_IMPORTANT -> if (enabled) "Important-information WhatsApp channel ON। Authorized integration না থাকলে Mayra message prepare করবে, সরাসরি send করবে না।" else "Important-information WhatsApp channel OFF।"
            MayraUserControlCenter.VOICE_COMMAND_ACCESS -> if (enabled) "Voice Command Access ON।" else "Voice Command Access OFF।"
            MayraUserControlCenter.LOCKED_PHONE_ACTIVE -> if (enabled) "Locked Phone Activity ON। শুধু অনুমোদিত নিরাপদ কাজ চলবে।" else "Locked Phone Activity OFF।"
            MayraUserControlCenter.SILENT_MODE -> if (enabled) "Silent Mode Behavior ON। ফোন silent থাকলে Mayra শুনবে, কিন্তু কথা বলবে না।" else "Silent Mode Behavior OFF।"
            MayraUserControlCenter.MAYRA_VOLUME -> "Mayra Volume control voice command দিয়ে ব্যবহার করুন: volume বাড়াও / কমাও।"
            else -> "Control updated."
        }
        showVoiceResult(message)
        speakResponse(message)
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
    private fun showDocxEditor(content: String) {
        val editor = EditText(this).apply {
            setText(content)
            setSelection(length())
            minLines = 12
            gravity = android.view.Gravity.TOP
        }
        val layout = baseLayout()
        layout.addView(TextView(this).apply { text = "DOCX Editor"; textSize = 28f })
        layout.addView(editor)
        layout.addView(Button(this).apply {
            text = "Save edited DOCX"
            setOnClickListener {
                pendingDocxEditText = editor.text.toString()
                docxEditPicker.launch("Mayra-edited.docx")
            }
        })
        layout.addView(Button(this).apply {
            text = "← Mayra Home"
            setOnClickListener { showAssistant() }
        })
        setContentView(ScrollView(this).apply { addView(layout) })
    }

    private fun showPdfEditor(content: String) {
        val editor = EditText(this).apply {
            setText(content)
            setSelection(length())
            minLines = 12
            gravity = android.view.Gravity.TOP
        }
        val layout = baseLayout()
        layout.addView(TextView(this).apply { text = "PDF Text Editor"; textSize = 28f })
        layout.addView(editor)
        layout.addView(Button(this).apply {
            text = "Save edited PDF"
            setOnClickListener {
                pendingPdfEditText = editor.text.toString()
                pdfCreatePicker.launch("Mayra-edited.pdf")
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
        Handler(Looper.getMainLooper()).postDelayed(
            { executeVoiceCommandInternal(spoken) },
            MayraCommandTimingPolicy.RESPONSE_DELAY_MS
        )
        showVoiceResult("কমান্ড গ্রহণ করেছি। ৫ সেকেন্ড পরে উত্তর দিয়ে কাজ শুরু করব।")
    }

    private fun executeVoiceCommandInternal(spoken: String) {
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
        val lowerCommand = spoken.lowercase(Locale.ROOT)
        val voiceWhatsAppMarker = listOf(
            "whatsapp voice message", "whatsapp-এ voice message",
            "whatsapp এ voice message", "হোয়াটসঅ্যাপে ভয়েস মেসেজ",
            "হোয়াটসঅ্যাপে ভয়েস মেসেজ", "व्हाट्सऐप पर वॉइस मैसेज"
        ).firstOrNull { lowerCommand.contains(it) }
        if (voiceWhatsAppMarker != null) {
            val raw = spoken.substringAfter(voiceWhatsAppMarker, "").trim()
            val content = raw
                .replace(Regex("^(to|for|কে|কে বল|বল|বলো|পাঠাও|করো|send|भेजो|को)\\s*[:,-]?\\s*", RegexOption.IGNORE_CASE), "")
                .substringAfter("saying", raw)
                .substringAfter("বলবে", raw)
                .substringAfter("এই কথাটা", raw)
                .trim()
            if (content.isBlank()) {
                showVoiceResult("বস, WhatsApp voice message-এ কী কথা বলব সেটি বলুন।")
                speakResponse("বস, voice message-এ কী কথা বলব?")
            } else {
                MayraWhatsAppVoiceMessage.createAndShare(this, content) { message ->
                    runOnUiThread {
                        showVoiceResult(message)
                        speakResponse(message)
                    }
                }
            }
            return
        }
        // Defense-in-depth: route physical lock state through the dedicated Owner-verified gate.
        // When the phone is locked, only explicitly enabled, verified, non-sensitive voice work may proceed.
        val keyguard = getSystemService(KeyguardManager::class.java)
        val locked = keyguard.isKeyguardLocked
        val gate = MayraLockedPhoneVoiceGate.decide(
            prefs = prefs,
            ownerVerified = prefs.getBoolean("owner_verified", false),
            masterOn = prefs.getBoolean("master_on", true),
            locked = locked,
            task = spoken
        )
        when (gate) {
            MayraLockedPhoneVoiceGate.Decision.BLOCK -> {
                showVoiceResult(if (locked) {
                    "ফোন locked — এই voice command এখন চালানো যাবে না।"
                } else {
                    "Mayra এই command এখন চালাতে পারছে না।"
                })
                return
            }
            MayraLockedPhoneVoiceGate.Decision.OWNER_VERIFICATION_REQUIRED -> {
                showVoiceResult("ফোন locked — Owner verification ছাড়া voice command চালানো যাবে না।")
                return
            }
            MayraLockedPhoneVoiceGate.Decision.ALLOW_LIMITED_VOICE -> Unit
        }
        if (!locked && !DeviceSecurityGate.mayExecuteUserCommand(this)) {
            showVoiceResult("Mayra নিরাপত্তা যাচাই সম্পূর্ণ করতে পারেনি।")
            return
        }
        if (isFinishing) return
        if (TemporaryOwnerAccessManager.isActive(this)) {
            TemporaryOwnerAccessManager.record(this, "VOICE_COMMAND", "RECEIVED", spoken.take(300))
        }
        val runtimeVoice = MayraVoiceRuntime.route(spoken)
        when (runtimeVoice.action) {
            MayraVoiceRuntime.Action.STOP_SPEAKING -> {
                responseTts?.stop()
                setVoiceLight(false)
                showVoiceResult(runtimeVoice.response)
                return
            }
            MayraVoiceRuntime.Action.OFFLINE_MODE -> {
                showVoiceResult(runtimeVoice.response)
                speakResponse(runtimeVoice.response)
                return
            }
            MayraVoiceRuntime.Action.RUN_OFFLINE_TASKS -> {
                val allowed = MayraOfflineVoiceWorkflow.Task.entries
                    .filter { it != MayraOfflineVoiceWorkflow.Task.CLOUD_UPLOAD }
                    .joinToString(", ") { it.name }
                val message = runtimeVoice.response + "\\nAvailable local tasks: " + allowed
                showVoiceResult(message)
                speakResponse(message)
                return
            }
            MayraVoiceRuntime.Action.QUEUE_UPLOAD -> {
                val message = runtimeVoice.response
                showVoiceResult(message)
                speakResponse(message)
                return
            }
            MayraVoiceRuntime.Action.NONE -> Unit
        }
        val lowerSpoken = spoken.lowercase(Locale.ROOT)

        // Owner-requested home controls are also available through voice.
        if (lowerSpoken.contains("lock") && (lowerSpoken.contains("active") || lowerSpoken.contains("চালু") || lowerSpoken.contains("বন্ধ") || lowerSpoken.contains("সক্রিয়"))) {
            val off = lowerSpoken.contains("বন্ধ") || lowerSpoken.contains("off") || lowerSpoken.contains("disable") || lowerSpoken.contains("নিষ্ক্রিয়") || lowerSpoken.contains("बंद")
            LockModePolicy.setEnabled(prefs, !off)
            val msg = if (off) "Locked Phone Activity OFF।" else "Locked Phone Activity ON — শুধু নিরাপদ, Owner-verified কাজ চলবে।"
            showVoiceResult(msg); speakResponse(msg); return
        }
        if (lowerSpoken.contains("silent mode") || lowerSpoken.contains("সাইলেন্ট মোড") || lowerSpoken.contains("silent behavior")) {
            val off = lowerSpoken.contains("বন্ধ") || lowerSpoken.contains("off") || lowerSpoken.contains("disable") || lowerSpoken.contains("बंद")
            MayraUserControlCenter.set(prefs, MayraUserControlCenter.SILENT_MODE, !off)
            val msg = if (off) "Silent Mode Behavior OFF।" else "Silent Mode Behavior ON — ফোন silent থাকলে Mayra কথা বলবে না।"
            showVoiceResult(msg); speakResponse(msg); return
        }

        MayraActionPermissionPolicy.automationVoiceCommand(spoken)?.let { (automation, enabled) ->
            MayraActionPermissionPolicy.setAutomation(prefs, automation, enabled)
            val state = if (enabled) "চালু" else "বন্ধ"
            val msg = "Mayra " + automation.key + " background automation " + state + " করা হয়েছে।"
            showVoiceResult(msg)
            speakResponse(msg)
            return
        }
        MayraMoodSystem.commandMood(spoken)?.let { mood ->
            MayraMoodSystem.set(prefs, mood)
            val msg = "Mayra " + mood.label + " Mood চালু হয়েছে। এখন থেকে আমার voice style এই mood অনুযায়ী থাকবে।"
            showVoiceResult(msg)
            speakResponse(msg)
            return
        }
        val wantsOff = lowerSpoken.contains("বন্ধ") || lowerSpoken.contains("off") || lowerSpoken.contains("disable") || lowerSpoken.contains("बंद")
        when {
            lowerSpoken.contains("volume") || lowerSpoken.contains("ভলিউম") || lowerSpoken.contains("সাউন্ড বাড়াও") || lowerSpoken.contains("সাউন্ড কমাও") -> {
                val direction = if (lowerSpoken.contains("কমাও") || lowerSpoken.contains("decrease") || lowerSpoken.contains("down")) -10 else 10
                val current = MayraUserControlCenter.volume(prefs)
                val value = MayraUserControlCenter.setVolume(prefs, current + direction)
                val audio = getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
                val max = audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC).coerceAtLeast(1)
                val target = ((max * value) / 100.0).toInt().coerceIn(0, max)
                audio.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, target, android.media.AudioManager.FLAG_SHOW_UI)
                val msg = "Mayra volume $value% করা হয়েছে।"
                showVoiceResult(msg); speakResponse(msg); return
            }
            lowerSpoken.contains("থ্রিডি") || lowerSpoken.contains("3d") || lowerSpoken.contains("three d") -> {
                MayraUserControlCenter.set(prefs, MayraUserControlCenter.THREE_D_CHARACTER, !wantsOff)
                val msg = if (wantsOff) "3D Character OFF।" else "3D Character ON।"
                showVoiceResult(msg); speakResponse(msg); return
            }
            lowerSpoken.contains("ভয়েস লাইট") || lowerSpoken.contains("voice light") || lowerSpoken.contains("লাইট চালু") || lowerSpoken.contains("লাইট বন্ধ") -> {
                MayraUserControlCenter.set(prefs, MayraUserControlCenter.VOICE_LIGHT, !wantsOff)
                val msg = if (wantsOff) "Voice Light OFF।" else "Voice Light ON।"
                showVoiceResult(msg); speakResponse(msg); return
            }
            lowerSpoken.contains("গুরুত্বপূর্ণ") && lowerSpoken.contains("whatsapp") && (lowerSpoken.contains("পাঠাও") || lowerSpoken.contains("send")) -> {
                if (!prefs.getBoolean("mayra_whatsapp_important_enabled", true)) {
                    showVoiceResult("WhatsApp Important Information OFF।")
                    speakResponse("বস, WhatsApp গুরুত্বপূর্ণ তথ্য পাঠানোর control এখন বন্ধ আছে।")
                    return
                }
                val title = if (sessionState.hasResumeState()) sessionState.title() else "Mayra Important Information"
                val body = if (sessionState.hasResumeState()) sessionState.details() else "Owner-requested important information from Mayra."
                val message = MayraWhatsAppImportantChannel.Message(
                    category = MayraWhatsAppImportantChannel.Category.SYSTEM,
                    priority = MayraWhatsAppImportantChannel.Priority.HIGH,
                    title = title,
                    body = body
                )
                val decision = MayraWhatsAppImportantChannel.deliveryDecision(message, integrationAuthorized = true, ownerApproved = true)
                if (decision != MayraWhatsAppImportantChannel.Delivery.SEND_VIA_AUTHORIZED_INTEGRATION) {
                    showVoiceResult("WhatsApp message validation/authorization ব্যর্থ।")
                    return
                }
                val launched = MayraWhatsAppShare.launch(this, message)
                val response = if (launched) {
                    "বস, গুরুত্বপূর্ণ তথ্য WhatsApp-এ পাঠানোর জন্য WhatsApp chat খুলে দেওয়া হয়েছে। Send button-টি আপনার নিয়ন্ত্রণে থাকবে।"
                } else {
                    "বস, WhatsApp খোলা যায়নি। WhatsApp ইনস্টল আছে কি না এবং availability পরীক্ষা করুন।"
                }
                showVoiceResult(response)
                speakResponse(response)
                return
            }
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
                val paired = windowsPairingSession.pairedDeviceId()
                if (paired != null) {
                    val msg = "Windows computer ইতিমধ্যে paired আছে (device: $paired)।"
                    showVoiceResult(msg); speakResponse(msg); return
                }
                val invite = windowsPairingSession.pendingInvite() ?: run {
                    val created = LocalDeviceLinkCoordinator().createInvite("windows-10")
                    windowsPairingSession.saveInvite(
                        MayraWindowsPairingSession.Invite(
                            created.deviceId,
                            created.code,
                            created.expiresAtMs
                        )
                    )
                    windowsPairingSession.pendingInvite()!!
                }
                val msg = "Windows 10 pairing code: ${invite.code}\nCodeটি শুধু আপনার Windows Mayra companion-এ Owner-approved pairing-এর জন্য ব্যবহার করুন। এটি ৫ মিনিট valid।"
                showVoiceResult(msg); speakResponse(msg)
                return
            }
            VoiceCommandResult.Action.COMPUTER_STATUS -> {
                val paired = windowsPairingSession.pairedDeviceId()
                val pending = windowsPairingSession.pendingInvite()
                val msg = when {
                    paired != null -> "কম্পিউটার: paired এবং Owner-authorized session state সংরক্ষিত আছে।"
                    pending != null -> "কম্পিউটার: pairing code pending আছে; Windows companion থেকে এখনও acceptance আসেনি।"
                    else -> "কম্পিউটার: এখনো paired নয়। 'কম্পিউটার pair করো' বললে নতুন secure pairing code তৈরি হবে।"
                }
                showVoiceResult(msg); speakResponse(msg); return
            }
            VoiceCommandResult.Action.COMPUTER_OPEN_BROWSER,
            VoiceCommandResult.Action.COMPUTER_FIND_FILE -> {
                if (!windowsPairingSession.isPaired()) {
                    val msg = "বস, আগে Windows 10 computer pair করতে হবে।"
                    showVoiceResult(msg); speakResponse(msg); return
                }
                val msg = "বস, Windows session paired আছে; এই command-এর companion-agent execution এখনো allowlist অনুযায়ী unavailable। আমি command নিজে থেকে চালাইনি।"
                showVoiceResult(msg); speakResponse(msg)
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
        val silentBehavior = prefs.getBoolean("mayra_silent_mode_behavior", true)
        val ringerMode = (getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager).ringerMode
        if (silentBehavior && ringerMode == android.media.AudioManager.RINGER_MODE_SILENT) {
            setVoiceLight(false)
            return
        }
        val voiceLightEnabled = prefs.getBoolean("mayra_voice_light_enabled", true) &&
            prefs.getBoolean("master_on", true)
        if (voiceLightEnabled) setVoiceLight(true)
        responseTts?.shutdown()
        responseTts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                responseTts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { if (voiceLightEnabled) setVoiceLight(true) }
                    override fun onDone(utteranceId: String?) { runOnUiThread { setVoiceLight(false) } }
                    override fun onError(utteranceId: String?) { runOnUiThread { setVoiceLight(false) } }
                })
                val locale = when {
                    message.contains(Regex("[\\u0980-\\u09FF]")) -> Locale("bn", "IN")
                    message.contains(Regex("[\\u0900-\\u097F]")) -> Locale("hi", "IN")
                    else -> Locale.US
                }
                val r = responseTts?.setLanguage(locale)
                val mood = MayraMoodSystem.current(prefs)
                responseTts?.setSpeechRate(mood.speechRate)
                responseTts?.setPitch(mood.pitch)
                if (r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED) {
                    responseTts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "mayra_command_response")
                }
            }
        }
    }

    private fun setVoiceLight(active: Boolean) {
        if (!prefs.getBoolean("mayra_voice_light_enabled", true)) return
        val root = window.decorView
        root.setBackgroundColor(
            if (active) android.graphics.Color.rgb(235, 245, 255)
            else android.graphics.Color.BLACK
        )
        window.statusBarColor = if (active) android.graphics.Color.rgb(80, 160, 255) else android.graphics.Color.BLACK
        window.navigationBarColor = if (active) android.graphics.Color.rgb(80, 160, 255) else android.graphics.Color.BLACK
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