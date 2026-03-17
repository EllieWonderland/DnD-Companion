package com.example.dndcompanion.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

/**
 * Centralises all SharedPreferences access for the app.
 * Call [switchCharacter] whenever the active character changes so that
 * every ViewModel automatically reads from / writes to the correct file.
 */
class PrefsManager(private val application: Application) {

    private var currentCharacterId: String = "Athania"

    var prefs: SharedPreferences = application.getSharedPreferences(
        "${currentCharacterId}SaveGame", Context.MODE_PRIVATE
    )
        private set

    fun switchCharacter(characterId: String) {
        currentCharacterId = characterId
        prefs = application.getSharedPreferences("${characterId}SaveGame", Context.MODE_PRIVATE)
    }
}
