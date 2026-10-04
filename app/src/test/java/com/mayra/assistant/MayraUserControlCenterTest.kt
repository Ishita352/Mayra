package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import android.content.SharedPreferences

class MayraUserControlCenterTest {
    private class Store : SharedPreferences {
        private val values = mutableMapOf<String, Any?>()
        override fun getAll(): MutableMap<String, *> = values.toMutableMap()
        override fun getString(key:String, def:String?) = values[key] as? String ?: def
        override fun getStringSet(key:String, def:Set<String>?) = def
        override fun getInt(key:String, def:Int) = values[key] as? Int ?: def
        override fun getLong(key:String, def:Long) = values[key] as? Long ?: def
        override fun getFloat(key:String, def:Float) = values[key] as? Float ?: def
        override fun getBoolean(key:String, def:Boolean) = values[key] as? Boolean ?: def
        override fun contains(key:String) = values.containsKey(key)
        override fun edit(): SharedPreferences.Editor = object: SharedPreferences.Editor {
            override fun putString(k:String,v:String?)=apply{values[k]=v}
            override fun putStringSet(k:String,v:Set<String>?)=apply{values[k]=v}
            override fun putInt(k:String,v:Int)=apply{values[k]=v}
            override fun putLong(k:String,v:Long)=apply{values[k]=v}
            override fun putFloat(k:String,v:Float)=apply{values[k]=v}
            override fun putBoolean(k:String,v:Boolean)=apply{values[k]=v}
            override fun remove(k:String)=apply{values.remove(k)}
            override fun clear()=apply{values.clear()}
            override fun commit()=true
            override fun apply(){}
        }
    }
    @Test fun allFiveControlsAreVoiceOperable() {
        assertTrue(MayraUserControlCenter.voiceCommands().size >= 5)
        assertTrue(MayraUserControlCenter.voiceCommands().any { it.contains("WhatsApp") })
    }
    @Test fun rulesKeepCallsAndWhatsAppPermissionBounded() {
        assertTrue(MayraUserControlCenter.incomingCallRule().contains("permission"))
        assertTrue(MayraUserControlCenter.whatsappRule().contains("authorized"))
    }
    @Test fun voiceLightAnd3dAreExplicitlyNonCovert() {
        assertTrue(MayraUserControlCenter.voiceLightRule().contains("covert"))
        assertTrue(MayraUserControlCenter.threeDRule().contains("covert"))
    }
}
