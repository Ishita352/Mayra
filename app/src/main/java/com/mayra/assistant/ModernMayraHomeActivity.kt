package com.mayra.assistant

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.KeyguardManager
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.Locale

/**
 * Mayra's redesigned owner home.
 * This is a visual/interaction layer; existing assistant engines remain in MainActivity.
 */
class ModernMayraHomeActivity : FragmentActivity() {
    private val prefs by lazy { getSharedPreferences("mayra_secure", MODE_PRIVATE) }
    private lateinit var orb: TextView
    private lateinit var status: TextView
    private var welcomeTts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs.edit().putBoolean("owner_command_authorized", false).apply()
        MayraFeatureCheckManager(this).enforceUnavailableFeaturesOff()
        window.decorView.setBackgroundColor(Color.rgb(7, 10, 22))
        if (!MayraFeatureCheckManager.isSetupCompleted(this)) {
            startActivity(Intent(this, MayraFirstRunSetupActivity::class.java))
            finish()
            return
        }
        if (!prefs.getBoolean("owner_verified", false)) {
            verifyOwner()
        } else {
            showHome()
        }
    }

    private fun verifyOwner() {
        val root = rootLayout()
        root.addView(label("MAYRA", 36f, Color.WHITE))
        root.addView(label("Your private AI companion", 16f, Color.rgb(190, 205, 235)))
        root.addView(space(28))
        root.addView(label("Owner verification required", 20f, Color.WHITE))
        root.addView(label("একবার Face / Fingerprint দিয়ে Owner হিসেবে যাচাই করুন। Password ব্যবহার করা হবে না।", 15f, Color.rgb(205, 215, 235)))
        root.addView(space(18))
        val verify = actionButton("VERIFY OWNER  〉")
        verify.setOnClickListener { authenticateOwner() }
        root.addView(verify)
        root.addView(space(12))
        root.addView(label("Founder: Gopal Basak", 13f, Color.rgb(145, 170, 205)))
        setContentView(root)
    }

    private fun authenticateOwner() {
        val manager = BiometricManager.from(this)
        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        if (manager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "Face/Fingerprint বা ফোনের PIN/Pattern/Password সেটআপ নেই।", Toast.LENGTH_LONG).show()
            return
        }
        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    prefs.edit().putBoolean("owner_verified", true).apply()
                    val identity = MayraFounderIdentity(object : MayraFounderIdentity.Store {
                        override fun get(key: String) = prefs.getString(key, null)
                        override fun put(key: String, value: String) { prefs.edit().putString(key, value).apply() }
                    })
                    identity.recognizeVerifiedOwner(MayraFounderIdentity.VerificationMethod.ANDROID_BIOMETRIC)
                    showHome()
                    speakOwnerWelcome()
                }
            })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Mayra Owner Verification")
            .setSubtitle("Face/Fingerprint ব্যবহার করুন; প্রয়োজনে PIN/Pattern/Password")
            .setAllowedAuthenticators(authenticators)
            .build()
        prompt.authenticate(info)
    }

    private fun speakOwnerWelcome() {
        welcomeTts?.shutdown()
        welcomeTts = TextToSpeech(this) { result ->
            if (result == TextToSpeech.SUCCESS) {
                welcomeTts?.let { speech ->
                    speech.language = Locale("bn", "IN")
                    speech.speak(
                        "রাধে রাধে বস, বলুন কী সাহায্য করতে পারি",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "mayra_owner_welcome"
                    )
                }
            }
        }
    }

    private fun showHome() {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(7, 10, 22))
            isFillViewport = true
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 30, 28, 34)
            setBackgroundColor(Color.rgb(7, 10, 22))
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val titleBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
        titleBox.addView(label("MAYRA", 30f, Color.WHITE))
        titleBox.addView(label("Private • Owner controlled • Ready", 13f, Color.rgb(145, 165, 205)))
        header.addView(titleBox)
        val master = Switch(this).apply {
            text = "ON"
            textSize = 13f
            setTextColor(Color.WHITE)
            isChecked = prefs.getBoolean("master_on", false)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("master_on", checked).apply()
                text = if (checked) "ON" else "OFF"
                if (checked && FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.VOICE_COMMAND)) {
                    if (prefs.getBoolean("owner_command_authorized", false)) {
                        MayraBackgroundVoiceServiceStarter.start(this@ModernMayraHomeActivity)
                    } else {
                        prefs.edit().putBoolean("master_on", false).apply()
                        isChecked = false
                        status.text = "Owner verification is required before Mayra can accept commands."
                        authenticateOwnerForSession { }
                    }
                } else if (!checked) {
                    MayraBackgroundVoiceServiceStarter.stop(this@ModernMayraHomeActivity)
                }
                status.text = if (checked) "Mayra is ready for Boss. Background voice is active when Voice Command is ON." else "Mayra paused — memory/state preserved."
            }
        }
        header.addView(master)
        root.addView(header)
        root.addView(space(18))

        val hero = GradientLayout()
        val heroInner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(22, 22, 22, 22)
        }
        orb = label("✦", 62f, Color.WHITE).apply {
            gravity = Gravity.CENTER
            setBackground(roundGradient(120, Color.rgb(31, 44, 92), Color.rgb(13, 150, 170)))
            setPadding(25, 18, 25, 18)
        }
        heroInner.addView(orb, LinearLayout.LayoutParams(-1, 150))
        heroInner.addView(space(8))
        heroInner.addView(label("Welcome Boss", 24f, Color.WHITE))
        status = label("Mayra is ready for your command.", 14f, Color.rgb(205, 220, 240))
        heroInner.addView(status)
        hero.addView(heroInner)
        root.addView(hero)
        root.addView(space(18))

        val grid = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row1.addView(card("🎙", "VOICE", "Talk to Mayra") { openAssistant() }, weightParams())
        row1.addView(card("◉", "CAMERA", "Visual access") {
            if (FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.CAMERA)) {
                startActivity(Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE))
            } else {
                status.text = "Camera is OFF — it must be successfully checked during setup first."
            }
        }, weightParams())
        grid.addView(row1)
        val row2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row2.addView(card("PC", "COMPUTER", "Windows 10 link") {
            if (MayraFeatureCheckManager.isEnabled(this, MayraFeatureCheckManager.COMPUTER)) {
                showComputerLinkDialog()
            } else {
                status.text = "Windows pairing is available. Configure the Windows agent for trusted-LAN mode, then pair with owner approval."
            }
        }, weightParams())
        row2.addView(card("CALL", "CALL ASSIST", "Approved calls") {
            if (MayraFeatureCheckManager.isEnabled(this, MayraFeatureCheckManager.CALL)) {
                openAssistant()
            } else {
                status.text = "Call Assist is OFF until call-handling actions are implemented and verified."
            }
        }, weightParams())
        grid.addView(row2)
        val row3 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row3.addView(card("✦", "3D CHARACTER", "10 character styles") {
            if (!prefs.getBoolean("mayra_3d_character_enabled", false)) {
                prefs.edit().putBoolean("mayra_3d_character_enabled", true).apply()
            }
            val names = MayraCharacterSystem.all().map { it.name }.toTypedArray()
            val current = MayraCharacterSystem.current(prefs)
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Mayra Character")
                .setSingleChoiceItems(names, names.indexOf(current.name)) { dialog, which ->
                    MayraCharacterSystem.select(prefs, MayraCharacterSystem.all()[which].id)
                    status.text = "Character: ${MayraCharacterSystem.all()[which].name}"
                    dialog.dismiss()
                    animateOrb()
                }
                .setNegativeButton("Close", null)
                .show()
        }, weightParams())
        row3.addView(card("☼", "VOICE LIGHT", "Speaking ring") {
            val next = !prefs.getBoolean("mayra_voice_light_enabled", false)
            prefs.edit().putBoolean("mayra_voice_light_enabled", next).apply()
            status.text = if (next) "Voice Light ON." else "Voice Light OFF."
        }, weightParams())
        grid.addView(row3)
        val row4 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row4.addView(card("↗", "WHATSAPP", "Important info") {
            if (FeatureToggleRegistry.isEnabled(prefs, FeatureToggleRegistry.WHATSAPP_ASSISTANT)) showWhatsAppVoiceDialog()
            else status.text = "WhatsApp is OFF — it must be successfully checked during setup first."
        }, weightParams())
        row4.addView(card("◎", "INTERVIEW", "Mock + coaching") {
            if (MayraFeatureCheckManager.isEnabled(this, MayraFeatureCheckManager.INTERVIEW)) {
                startActivity(Intent(this, MayraInterviewAssistantActivity::class.java))
            } else {
                status.text = "Interview Assistant is OFF — enable it during first-time setup."
            }
        }, weightParams())
        grid.addView(row4)
        root.addView(grid)
        root.addView(space(14))

        val lock = Switch(this).apply {
            text = "Mayra active while phone is locked"
            textSize = 15f
            setTextColor(Color.WHITE)
            isChecked = prefs.getBoolean("mayra_active_while_locked", false)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("mayra_active_while_locked", checked).apply()
                status.text = if (checked) "Locked-phone listening mode enabled." else "Locked-phone listening mode disabled."
            }
        }
        root.addView(lock)
        val silent = Switch(this).apply {
            text = "Silent phone → listen, don't speak"
            textSize = 15f
            setTextColor(Color.WHITE)
            isChecked = prefs.getBoolean("mayra_silent_mode_behavior", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("mayra_silent_mode_behavior", checked).apply()
            }
        }
        root.addView(silent)
        root.addView(space(10))
        val full = actionButton("OPEN FULL MAYRA  〉")
        full.setOnClickListener { openAssistant() }
        root.addView(full)
        root.addView(space(8))
        root.addView(label("Password: disabled  •  Owner approval: required for code changes", 12f, Color.rgb(120, 145, 180)))
        scroll.addView(root)
        setContentView(scroll)
        animateOrb()
        restoreWindowsLink()
    }

    /**
     * Persistent pairing is not re-created on every boot. If a trusted session
     * exists, Mayra silently reuses its saved token/endpoint and refreshes the
     * Android endpoint registration when the phone is online.
     */
    private fun restoreWindowsLink() {
        val session = windowsPairingSession()
        val host = session.pairedHost()
        val token = session.sessionToken()
        if (!session.isPaired() || host.isNullOrBlank() || token.isNullOrBlank()) return

        val port = session.pairedPort()
        Thread {
            val phoneHost = LocalDeviceLinkCoordinator.localLanAddress()
            val result = if (phoneHost != null) {
                LocalDeviceLinkCoordinator().registerPhone(
                    LocalDeviceLinkCoordinator.Endpoint(host, port),
                    token,
                    phoneHost,
                    8766
                )
            } else {
                LocalDeviceLinkCoordinator.TransportResult(false, "", "Local network address unavailable")
            }
            runOnUiThread {
                status.text = if (result.ok) {
                    "Windows 10 login restored ✓ — persistent connection ready."
                } else {
                    "Windows 10 remains logged in ✓ — waiting for network/Windows; Android stays standalone."
                }
            }
        }.start()
    }

    private fun showComputerLinkDialog() {
        val session = windowsPairingSession()
        val paired = session.pairedDeviceId()
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Mayra ↔ Windows 10")

        if (paired != null && session.sessionToken() != null && session.pairedHost() != null) {
            val host = session.pairedHost()!!
            val port = session.pairedPort()
            val message = "Windows 10: PAIRED\nDevice: $paired\nEndpoint: $host:$port\n\nAndroid remains fully independent if Windows is offline."
            builder.setMessage(message)
                .setNegativeButton("Close", null)
                .setNeutralButton("Revoke", android.content.DialogInterface.OnClickListener { _, _ ->
                    val token = session.sessionToken()!!
                    Thread {
                        LocalDeviceLinkCoordinator().revoke(LocalDeviceLinkCoordinator.Endpoint(host, port), token)
                        runOnUiThread {
                            session.revoke()
                            status.text = "Windows pairing revoked. Android standalone mode remains active."
                        }
                    }.start()
                })
                .setPositiveButton("Test Connection", android.content.DialogInterface.OnClickListener { _, _ ->
                    val token = session.sessionToken()!!
                    Thread {
                        val result = LocalDeviceLinkCoordinator().status(
                            LocalDeviceLinkCoordinator.Endpoint(host, port), token
                        )
                        runOnUiThread {
                            status.text = if (result.ok) "Windows connection: ONLINE ✓" else "Windows connection unavailable — Android continues standalone."
                            Toast.makeText(this, status.text, Toast.LENGTH_LONG).show()
                        }
                    }.start()
                })
            builder.show()
            return
        }

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 4)
        }
        val hostInput = EditText(this).apply {
            hint = "Windows IP address (e.g. 192.168.1.20)"
            isSingleLine = true
        }
        val portInput = EditText(this).apply {
            hint = "Port (default 8765)"
            setText("8765")
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            isSingleLine = true
        }
        val codeInput = EditText(this).apply {
            hint = "6-digit Windows pairing code"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            isSingleLine = true
        }
        form.addView(hostInput)
        form.addView(portInput)
        val quickCodeInput = EditText(this).apply {
            hint = "8-digit Quick Owner Link code"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            isSingleLine = true
        }
        val ownerId = prefs.getString("mayra_owner_link_id", null) ?: ("gopal-owner-" +
            android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID).takeLast(6)).also {
            prefs.edit().putString("mayra_owner_link_id", it).apply()
        }
        val quickButton = actionButton("QUICK CONNECT  〉")
        quickButton.setOnClickListener {
            val host = hostInput.text.toString().trim()
            val port = portInput.text.toString().toIntOrNull() ?: 8765
            val quickCode = quickCodeInput.text.toString().trim()
            if (host.isBlank() || quickCode.length != 8) {
                status.text = "Windows IP এবং 8-digit Quick Owner Link code দিন।"
                return@setOnClickListener
            }
            Thread {
                val result = LocalDeviceLinkCoordinator().quickPair(
                    LocalDeviceLinkCoordinator.Endpoint(host, port), ownerId, quickCode
                )
                runOnUiThread {
                    if (result.ok) {
                        val token = try { org.json.JSONObject(result.response).optString("session_token") } catch (_: Exception) { "" }
                        if (token.isNotBlank()) {
                            windowsPairingSession().markPaired("windows-10", host, port, token)
                            status.text = "Quick Owner Link সফল ✓ — Windows 10 automatically connected."
                        } else {
                            status.text = "Quick Link response invalid."
                        }
                    } else {
                        status.text = "Quick Owner Link failed: " + (result.error ?: "invalid/expired code")
                    }
                    Toast.makeText(this, status.text, Toast.LENGTH_LONG).show()
                }
            }.start()
        }
        form.addView(quickCodeInput)
        form.addView(quickButton)

        builder.setView(form)
            .setMessage("Windows agent চালু করে তার PAIRING CODE নিন। Android ও Windows একই trusted Wi-Fi/hotspot-এ রাখুন। প্রথমে Send Pair Request, তারপর Windows-এ owner approval, তারপর Complete Pairing করুন।")
            .setNegativeButton("Close", null)
            .setNeutralButton("Send Pair Request", android.content.DialogInterface.OnClickListener { _, _ ->
                val host = hostInput.text.toString().trim()
                val port = portInput.text.toString().toIntOrNull() ?: 8765
                val code = codeInput.text.toString().trim()
                Thread {
                    val result = LocalDeviceLinkCoordinator().requestPair(
                        LocalDeviceLinkCoordinator.Endpoint(host, port), code
                    )
                    runOnUiThread {
                        status.text = if (result.ok) {
                            "Pair request sent ✓ — এখন Windows PC-তে owner approval দিন।"
                        } else {
                            "Pair request failed: " + (result.error ?: "unknown error")
                        }
                        Toast.makeText(this, status.text, Toast.LENGTH_LONG).show()
                    }
                }.start()
            })
            .setPositiveButton("Complete Pairing", android.content.DialogInterface.OnClickListener { _, _ ->
                val host = hostInput.text.toString().trim()
                val port = portInput.text.toString().toIntOrNull() ?: 8765
                val code = codeInput.text.toString().trim()
                Thread {
                    val result = LocalDeviceLinkCoordinator().completePair(
                        LocalDeviceLinkCoordinator.Endpoint(host, port), code
                    )
                    runOnUiThread {
                        if (result.ok) {
                            val token = try {
                                org.json.JSONObject(result.response).optString("session_token")
                            } catch (_: Exception) { "" }
                            if (token.isNotBlank()) {
                                session.markPaired("windows-10", host, port, token)
                                val phoneHost = LocalDeviceLinkCoordinator.localLanAddress()
                                if (phoneHost != null) {
                                    val registration = LocalDeviceLinkCoordinator().registerPhone(
                                        LocalDeviceLinkCoordinator.Endpoint(host, port),
                                        token,
                                        phoneHost,
                                        8766
                                    )
                                    status.text = if (registration.ok) {
                                        "Windows 10 paired successfully ✓ — two-way command channel registered."
                                    } else {
                                        "Windows 10 paired ✓ — Android endpoint registration pending; Android standalone mode remains active."
                                    }
                                } else {
                                    status.text = "Windows 10 paired ✓ — local network address unavailable; Android standalone mode remains active."
                                }
                            } else {
                                status.text = "Pairing did not complete. Windows owner approval may still be pending."
                            }
                        } else {
                            status.text = "Pairing failed: " + (result.error ?: "owner approval required")
                        }
                        Toast.makeText(this, status.text, Toast.LENGTH_LONG).show()
                    }
                }.start()
            })
        builder.show()
    }

    private fun windowsPairingSession(): MayraWindowsPairingSession =
        MayraWindowsPairingSession(object : MayraWindowsPairingSession.Store {
            override fun get(key: String) = prefs.getString(key, null)
            override fun put(key: String, value: String) { prefs.edit().putString(key, value).apply() }
            override fun remove(key: String) { prefs.edit().remove(key).apply() }
        })

    private fun showWhatsAppVoiceDialog() {
        val input = EditText(this).apply {
            hint = "যে কথাটি WhatsApp voice message-এ বলবে"
            minLines = 3
            setPadding(24, 18, 24, 18)
        }
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Mayra → WhatsApp Voice")
            .setMessage("Mayra প্রথমে নিজের পরিচয় দেবে, তারপর আপনার বলা কথাটি voice message হিসেবে প্রস্তুত করবে। শেষ Send আপনার নিয়ন্ত্রণে থাকবে।")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Prepare Voice") { _, _ ->
                val text = input.text.toString()
                MayraWhatsAppVoiceMessage.createAndShare(this, text) { message ->
                    runOnUiThread {
                        status.text = message
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .show()
    }

    private fun openAssistant() {
        if (!prefs.getBoolean("master_on", false)) {
            status.text = "Mayra is OFF. Turn the master switch ON first."
            return
        }
        if (!prefs.getBoolean("owner_command_authorized", false)) {
            authenticateOwnerForSession { startActivity(Intent(this, MainActivity::class.java)) }
            return
        }
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun authenticateOwnerForSession(onAuthorized: () -> Unit) {
        val manager = BiometricManager.from(this)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        if (manager.canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            status.text = "Owner verification unavailable. Configure Face/Fingerprint or the phone's secure credential."
            return
        }
        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    prefs.edit().putBoolean("owner_verified", true)
                        .putBoolean("owner_command_authorized", true).apply()
                    MayraFounderIdentity(object : MayraFounderIdentity.Store {
                        override fun get(key: String) = prefs.getString(key, null)
                        override fun put(key: String, value: String) { prefs.edit().putString(key, value).apply() }
                    }).recognizeVerifiedOwner(MayraFounderIdentity.VerificationMethod.ANDROID_BIOMETRIC)
                    status.text = "Owner verified ✓ — Mayra command access enabled."
                    speakOwnerWelcome()
                    onAuthorized()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    status.text = "Owner verification cancelled. Mayra command access remains locked."
                }
            })
        prompt.authenticate(BiometricPrompt.PromptInfo.Builder()
            .setTitle("Mayra Owner Command Access")
            .setSubtitle("Face / Fingerprint অথবা প্রয়োজনে ফোনের secure PIN/Pattern/Password")
            .setAllowedAuthenticators(authenticators).build())
    }

    private fun animateOrb() {
        if (!::orb.isInitialized) return
        ObjectAnimator.ofFloat(orb, View.ROTATION, 0f, 360f).apply {
            duration = 4200
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        ObjectAnimator.ofFloat(orb, View.SCALE_X, 0.96f, 1.04f).apply {
            duration = 1400
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            start()
        }
        ObjectAnimator.ofFloat(orb, View.SCALE_Y, 0.96f, 1.04f).apply {
            duration = 1400
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            start()
        }
    }

    private fun card(icon: String, title: String, subtitle: String, action: () -> Unit): LinearLayout {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(18, 18, 18, 18)
            background = roundGradient(20, Color.rgb(20, 27, 49), Color.rgb(13, 17, 32))
            elevation = 6f
            setOnClickListener { action() }
        }
        box.addView(label(icon, 26f, Color.rgb(120, 220, 235)))
        box.addView(label(title, 14f, Color.WHITE))
        box.addView(label(subtitle, 11f, Color.rgb(150, 165, 195)))
        return box
    }

    private fun weightParams() = LinearLayout.LayoutParams(0, 125, 1f).apply {
        setMargins(0, 0, 10, 10)
    }

    private fun actionButton(text: String) = Button(this).apply {
        this.text = text
        textSize = 15f
        setTextColor(Color.WHITE)
        background = roundGradient(18, Color.rgb(28, 112, 150), Color.rgb(90, 54, 150))
        setPadding(18, 14, 18, 14)
    }

    private fun label(text: String, size: Float, color: Int) = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(color)
        gravity = Gravity.CENTER_VERTICAL
    }

    private fun space(height: Int) = Space(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, height)
    }

    private fun rootLayout() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(36, 60, 36, 40)
        setBackgroundColor(Color.rgb(7, 10, 22))
    }

    private fun GradientLayout() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = roundGradient(28, Color.rgb(26, 42, 76), Color.rgb(23, 18, 54))
        elevation = 10f
        layoutParams = LinearLayout.LayoutParams(-1, -2)
    }

    private fun roundGradient(radiusDp: Int, start: Int, end: Int): GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(start, end)).apply {
            cornerRadius = radiusDp * resources.displayMetrics.density
            setStroke((1 * resources.displayMetrics.density).toInt().coerceAtLeast(1), Color.rgb(70, 95, 145))
        }

    override fun onDestroy() {
        welcomeTts?.stop()
        welcomeTts?.shutdown()
        welcomeTts = null
        super.onDestroy()
    }
}


object MayraBackgroundVoiceServiceStarter {
    fun start(context: android.content.Context) {
        val intent = android.content.Intent(context, MayraBackgroundVoiceService::class.java)
            .setAction(MayraBackgroundVoiceService.ACTION_START)
        androidx.core.content.ContextCompat.startForegroundService(context, intent)
    }

    fun stop(context: android.content.Context) {
        val intent = android.content.Intent(context, MayraBackgroundVoiceService::class.java)
            .setAction(MayraBackgroundVoiceService.ACTION_STOP)
        context.startService(intent)
    }

}
