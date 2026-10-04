package com.mayra.assistant

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.KeyguardManager
import android.content.Intent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                }
            })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Mayra Owner Verification")
            .setSubtitle("Face/Fingerprint ব্যবহার করুন; প্রয়োজনে PIN/Pattern/Password")
            .setAllowedAuthenticators(authenticators)
            .build()
        prompt.authenticate(info)
    }

    private fun showHome() {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(7, 10, 22))
            isFillViewport = true
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 30, 28, 34)
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
                    MayraBackgroundVoiceServiceStarter.start(this@ModernMayraHomeActivity)
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
        row2.addView(card("⌁", "COMPUTER", "Windows 10 link") { openAssistant() }, weightParams())
        row2.addView(card("☎", "CALL ASSIST", "Approved calls") { openAssistant() }, weightParams())
        grid.addView(row2)
        val row3 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row3.addView(card("✦", "3D CHARACTER", "Bring Mayra alive") {
            prefs.edit().putBoolean("mayra_3d_character_enabled", true).apply()
            animateOrb()
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
    }

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
        startActivity(Intent(this, MainActivity::class.java))
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
