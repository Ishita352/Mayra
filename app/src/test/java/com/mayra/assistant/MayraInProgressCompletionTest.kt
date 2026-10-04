package com.mayra.assistant

import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Test

class MayraInProgressCompletionTest {

    private class Prefs : SharedPreferences {
        private val data = mutableMapOf<String, Any?>()
        override fun getAll(): MutableMap<String, *> = data.toMutableMap()
        override fun getString(key: String, defValue: String?) = data[key] as? String ?: defValue
        override fun getStringSet(key: String, defValues: Set<String>?) = data[key] as? Set<String> ?: defValues
        override fun getInt(key: String, defValue: Int) = (data[key] as? Int) ?: defValue
        override fun getLong(key: String, defValue: Long) = (data[key] as? Long) ?: defValue
        override fun getFloat(key: String, defValue: Float) = (data[key] as? Float) ?: defValue
        override fun getBoolean(key: String, defValue: Boolean) = (data[key] as? Boolean) ?: defValue
        override fun contains(key: String) = data.containsKey(key)
        override fun edit() = object : SharedPreferences.Editor {
            private val pending = mutableMapOf<String, Any?>()
            private var clear = false
            override fun putString(k:String,v:String?) = this.also { pending[k]=v }
            override fun putStringSet(k:String,v:Set<String>?) = this.also { pending[k]=v }
            override fun putInt(k:String,v:Int) = this.also { pending[k]=v }
            override fun putLong(k:String,v:Long) = this.also { pending[k]=v }
            override fun putFloat(k:String,v:Float) = this.also { pending[k]=v }
            override fun putBoolean(k:String,v:Boolean) = this.also { pending[k]=v }
            override fun remove(k:String) = this.also { pending[k]=null }
            override fun clear() = this.also { clear=true }
            override fun commit():Boolean { if(clear)data.clear(); data.putAll(pending); return true }
            override fun apply() { commit() }
        }
        override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener) {}
        override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener) {}
    }

    @Test fun homeControlsPersistAndExposeAllOwnerControls() {
        val p = Prefs()
        MayraUserControlCenter.set(p, MayraUserControlCenter.CAMERA, true)
        MayraUserControlCenter.set(p, MayraUserControlCenter.THREE_D_CHARACTER, true)
        MayraUserControlCenter.set(p, MayraUserControlCenter.VOICE_LIGHT, true)
        MayraUserControlCenter.set(p, MayraUserControlCenter.WHATSAPP_IMPORTANT, true)
        MayraUserControlCenter.set(p, MayraUserControlCenter.LOCKED_PHONE_ACTIVE, true)
        MayraUserControlCenter.setVolume(p, 85)
        val s = MayraUserControlCenter.state(p)
        assertTrue(s.camera && s.threeDCharacter && s.voiceLight && s.whatsappImportant && s.lockedPhoneActive)
        assertEquals(85, s.volumePercent)
        assertEquals(9, MayraUserControlCenter.voiceCommands().size)
    }

    @Test fun deviceSafetyBlocksSecurityAndFinancialActions() {
        val base = MayraDeviceSafetyAndControl.DeviceState(
            MayraDeviceSafetyAndControl.Device.ANDROID, true, true, true
        )
        assertFalse(MayraDeviceSafetyAndControl.assess(base, MayraDeviceSafetyAndControl.Action.SECURITY_CONTROL).allowed)
        assertFalse(MayraDeviceSafetyAndControl.assess(base, MayraDeviceSafetyAndControl.Action.FINANCIAL_TRANSACTION).allowed)
        assertTrue(MayraDeviceSafetyAndControl.assess(base, MayraDeviceSafetyAndControl.Action.READ_STATUS).allowed)
    }

    @Test fun backgroundAutomationRequiresOwnerEnablement() {
        assertEquals(
            MayraActionPermissionPolicy.Decision.OWNER_APPROVAL_REQUIRED,
            MayraActionPermissionPolicy.decide(
                MayraActionPermissionPolicy.ActionType.BACKGROUND_AUTOMATION, false, false
            )
        )
        assertEquals(
            MayraActionPermissionPolicy.Decision.AUTOMATION_ALLOWED,
            MayraActionPermissionPolicy.decide(
                MayraActionPermissionPolicy.ActionType.BACKGROUND_AUTOMATION, false, true
            )
        )
    }

    @Test fun primaryLanguagesHaveAllRequiredCompetenceAreas() {
        assertEquals(setOf("bn","hi","en"), MayraMultilingualGrammarEngine.primaryLanguages().map { it.code }.toSet())
        assertTrue(MayraMultilingualGrammarEngine.primaryLanguages().all { MayraMultilingualGrammarEngine.hasComprehensivePrimaryCompetence(it.code) })
        assertEquals(13, MayraMultilingualGrammarEngine.primaryCompetenceAreas().size)
    }

    @Test fun certificatesNeedEvidenceAndOwnerApprovalForCv() {
        assertNull(MayraLearningCertificateWorkflow.prepare("Excel", ""))
        val c = MayraLearningCertificateWorkflow.prepare("Excel", "course evidence")!!
        assertFalse(MayraLearningCertificateWorkflow.readyForCv(c))
        val approved = MayraLearningCertificateWorkflow.approve(c, true)
        assertTrue(MayraLearningCertificateWorkflow.readyForCv(approved))
    }

    @Test fun jobProfilePreparationHasOwnerGate() {
        val plan = MayraJobPlatformProfileWorkflow.plan(MayraJobPlatformProfileWorkflow.Platform.LINKEDIN)
        assertTrue(plan.fields.contains("skills"))
        assertTrue(MayraJobPlatformProfileWorkflow.ownerApprovalRequired(MayraJobPlatformProfileWorkflow.Stage.PROFILE_PREPARED))
        assertTrue(MayraJobPlatformProfileWorkflow.ownerApprovalRequired(MayraJobPlatformProfileWorkflow.Stage.READY_TO_CREATE))
    }

    @Test fun multitaskingReducesLoadWhenDeviceIsUnsafe() {
        val c = MayraDeviceAwareMultitasking.capacity(
            MayraDeviceAwareMultitasking.Health.CRITICAL,
            MayraDeviceAwareMultitasking.Load.EXCESSIVE,
            5, 5, 10, false
        )
        assertEquals(1, c.recommendedConcurrentTasks)
        assertEquals(MayraDeviceAwareMultitasking.Load.EXCESSIVE, c.load)
    }

    @Test fun registryContainsEightInProgressCapabilitiesBeforeRelease() {
        val ids = MayraCapabilityRegistry.all()
            .filter { it.status == MayraCapabilityRegistry.Status.IN_PROGRESS }
            .map { it.id }
            .toSet()
        assertEquals(setOf(56,57,58,59,60,61,62,63), ids)
    }
}
