package com.mayra.assistant

import android.content.SharedPreferences

/** Data-driven character selector. Visual 3D assets plug into these stable slots. */
object MayraCharacterSystem {
    data class CharacterProfile(
        val id: String,
        val name: String,
        val style: String,
        val referenceGroup: String,
        val supportsLipSync: Boolean = true,
        val supportsBlink: Boolean = true,
        val supportsEyeHeadMotion: Boolean = true,
        val supportsGestures: Boolean = true,
        val supportsWalking: Boolean = true
    )

    private const val KEY = "mayra_character_id"

    private val profiles = listOf(
        CharacterProfile("character_01_sari", "Mayra Sari", "South Asian sari-inspired 3D", "Approved sari reference"),
        CharacterProfile("character_02_pony", "Mayra Casual Ponytail", "Modern casual 3D", "Approved Pinterest reference 1"),
        CharacterProfile("character_03_burgundy", "Mayra Elegant Burgundy", "Elegant casual 3D", "Approved Pinterest reference 2"),
        CharacterProfile("character_04_modern", "Mayra Modern", "Clean modern 3D", "New style"),
        CharacterProfile("character_05_futuristic", "Mayra Future", "Futuristic 3D", "New style"),
        CharacterProfile("character_06_professional", "Mayra Professional", "Professional 3D", "New style"),
        CharacterProfile("character_07_traditional", "Mayra Traditional", "Traditional South Asian 3D", "New style"),
        CharacterProfile("character_08_sporty", "Mayra Sporty", "Sporty casual 3D", "New style"),
        CharacterProfile("character_09_elegant", "Mayra Elegant", "Elegant evening 3D", "New style"),
        CharacterProfile("character_10_friendly", "Mayra Friendly", "Warm friendly 3D", "New style")
    )

    fun all(): List<CharacterProfile> = profiles
    fun find(id: String): CharacterProfile? = profiles.firstOrNull { it.id == id }

    fun current(prefs: SharedPreferences): CharacterProfile =
        find(prefs.getString(KEY, profiles.first().id) ?: profiles.first().id) ?: profiles.first()

    fun select(prefs: SharedPreferences, id: String): Boolean {
        if (find(id) == null) return false
        prefs.edit().putString(KEY, id).apply()
        return true
    }

    fun behavior(mood: MayraMoodSystem.Mood): String = mood.bodyReaction

    fun integrationRule(): String =
        "Character selection changes presentation only. Lip-sync, blink, eye/head motion, gestures, walking and mood reactions are independent presentation layers."
}
