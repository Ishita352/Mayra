package com.mayra.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class MayraFirstRunSetupActivity : FragmentActivity() {
    private val prefs by lazy { MayraFeatureCheckManager.prefs(this) }
    private val manager by lazy { MayraFeatureCheckManager(this) }
    private var index = 0
    private lateinit var title: TextView
    private lateinit var detail: TextView
    private lateinit var status: TextView
    private lateinit var action: Button
    private lateinit var next: Button
    private lateinit var progress: TextView
    private val permissionRequestCode = 8101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FeatureToggleRegistry.resetAllToOff(prefs)
        buildUi()
        showCurrent()
    }

    private fun buildUi() {
        val root = ScrollView(this).apply { setBackgroundColor(Color.rgb(7, 10, 22)) }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 36, 28, 36)
        }
        title = TextView(this).apply { textSize = 28f; setTextColor(Color.WHITE) }
        progress = TextView(this).apply { textSize = 14f; setTextColor(Color.rgb(150, 180, 220)) }
        detail = TextView(this).apply { textSize = 16f; setTextColor(Color.rgb(205, 220, 240)); setPadding(0, 18, 0, 18) }
        status = TextView(this).apply { textSize = 15f; setTextColor(Color.rgb(170, 210, 180)) }
        action = Button(this)
        next = Button(this)
        box.addView(TextView(this).apply { text="MAYRA FIRST-TIME SETUP"; textSize=30f; setTextColor(Color.WHITE) })
        box.addView(TextView(this).apply { text="ইন্সটলের পর সব optional feature OFF থাকবে। প্রতিটি feature একবার করে check করে সফল হলে তবেই ON হবে."; textSize=16f; setTextColor(Color.rgb(190,205,235)); setPadding(0,10,0,18) })
        box.addView(progress)
        box.addView(title)
        box.addView(detail)
        box.addView(status)
        box.addView(action)
        box.addView(next)
        root.addView(box)
        setContentView(root)
        action.setOnClickListener { checkCurrent() }
        next.setOnClickListener { moveNext() }
    }

    private fun showCurrent() {
        val specs = MayraFeatureCheckManager.specs()
        if (index >= specs.size) { finishSetup(); return }
        val spec = specs[index]
        progress.text = "Feature " + (index + 1) + " / " + specs.size
        title.text = spec.name
        detail.text = spec.description + if (spec.requiresPermission) "\n\nএই feature-এর প্রয়োজনীয় Android permission এখন চাওয়া হবে।" else ""
        status.text = "OFF • Not checked"
        action.text = "CHECK & ENABLE"
        next.text = "SKIP / KEEP OFF"
        action.isEnabled = true
        next.isEnabled = true
    }

    private fun checkCurrent() {
        val spec = MayraFeatureCheckManager.specs()[index]
        when (spec.id) {
            MayraFeatureCheckManager.OWNER -> authenticateOwner()
            MayraFeatureCheckManager.VOICE -> requestPermissionOrCheck(Manifest.permission.RECORD_AUDIO)
            MayraFeatureCheckManager.CAMERA -> requestPermissionOrCheck(Manifest.permission.CAMERA)
            MayraFeatureCheckManager.CALL -> requestPermissionOrCheck(Manifest.permission.READ_PHONE_STATE)
            else -> completeCheck(spec)
        }
    }

    private fun requestPermissionOrCheck(permission: String) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            completeCheck(MayraFeatureCheckManager.specs()[index])
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), permissionRequestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != permissionRequestCode) return
        val spec = MayraFeatureCheckManager.specs()[index]
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) completeCheck(spec)
        else {
            status.text = "OFF • ✗ Check failed — permission denied."
            manager.markChecked(spec.id, false)
            next.isEnabled = true
        }
    }

    private fun authenticateOwner() {
        val bm = BiometricManager.from(this)
        if (bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
            status.text = "OFF • ✗ Owner biometric unavailable on this phone."
            manager.markChecked(MayraFeatureCheckManager.OWNER, false)
            return
        }
        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                prefs.edit().putBoolean("owner_verified", true).apply()
                MayraFounderIdentity(object : MayraFounderIdentity.Store {
                    override fun get(key: String) = prefs.getString(key, null)
                    override fun put(key: String, value: String) { prefs.edit().putString(key, value).apply() }
                }).recognizeVerifiedOwner(MayraFounderIdentity.VerificationMethod.ANDROID_BIOMETRIC)
                completeCheck(MayraFeatureCheckManager.specs()[index])
            }
        })
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Mayra Owner Verification")
                .setSubtitle("Face অথবা Fingerprint দিয়ে Owner যাচাই করুন")
                .setNegativeButtonText("Cancel")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build()
        )
    }

    private fun completeCheck(spec: MayraFeatureCheckManager.FeatureSpec) {
        val result = manager.check(spec)
        manager.markChecked(spec.id, result.success)
        manager.setEnabled(spec.id, result.success)
        status.text = if (result.success) "ON • ✓ Checked • Working\n" + result.message else "OFF • ✗ Check failed\n" + result.message
        action.isEnabled = false
        next.text = if (index == MayraFeatureCheckManager.specs().lastIndex) "FINISH SETUP" else "NEXT FEATURE"
        next.isEnabled = true
    }

    private fun moveNext() {
        index++
        if (index >= MayraFeatureCheckManager.specs().size) finishSetup() else showCurrent()
    }

    private fun finishSetup() {
        manager.markSetupComplete()
        prefs.edit().putBoolean("first_run_setup_version_1", true).apply()
        Toast.makeText(this, "Mayra Setup Complete", Toast.LENGTH_LONG).show()
        startActivity(Intent(this, ModernMayraHomeActivity::class.java))
        finish()
    }
}
