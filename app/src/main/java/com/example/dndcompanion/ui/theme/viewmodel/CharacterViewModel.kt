package com.example.dndcompanion.ui.theme.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dndcompanion.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.launch

// --- DATENKLASSEN & ENUMS ---
data class InventoryItem(val name: String, val amount: Int)
data class ChatMessage(val text: String, val isUser: Boolean)
data class FaqItem(val question: String, val answer: String)

enum class ActiveWeapon {
    LANGBOGEN,
    KURZSCHWERT_SCHILD,
    SHILLELAGH_SCHILD
}

class CharacterViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("AthaniaSaveGame", Context.MODE_PRIVATE)

    // --- BASISWERTE ---
    val level: Int = 4
    val dexterity: Int = 18
    val wisdom: Int = 14
    val constitution: Int = 16

    val proficiencyBonus: Int
        get() = when(level) {
            in 1..4 -> 2
            in 5..8 -> 3
            in 9..12 -> 4
            in 13..16 -> 5
            else -> 6
        }

    val dexMod: Int get() = (dexterity - 10) / 2
    val wisMod: Int get() = (wisdom - 10) / 2
    val conMod: Int get() = (constitution - 10) / 2

    val spellAttackBonus: Int get() = proficiencyBonus + wisMod
    val spellSaveDc: Int get() = 8 + proficiencyBonus + wisMod

    val maxHp = 40
    var currentHp by mutableIntStateOf(prefs.getInt("currentHp", maxHp))
        private set
    var hitDice by mutableIntStateOf(prefs.getInt("hitDice", 4))
        private set

    fun takeDamage(amount: Int) {
        currentHp = (currentHp - amount).coerceAtLeast(0)
        prefs.edit { putInt("currentHp", currentHp) }
    }

    fun healManual(amount: Int) {
        currentHp = (currentHp + amount).coerceAtMost(maxHp)
        prefs.edit { putInt("currentHp", currentHp) }
    }

    private val savedWeaponName = prefs.getString("currentWeapon", ActiveWeapon.LANGBOGEN.name) ?: ActiveWeapon.LANGBOGEN.name
    var currentWeapon by mutableStateOf(ActiveWeapon.valueOf(savedWeaponName))
        private set

    fun equipWeapon(weapon: ActiveWeapon) {
        currentWeapon = weapon
        prefs.edit { putString("currentWeapon", weapon.name) }
    }

    val currentArmorClass: Int
        get() = when (currentWeapon) {
            ActiveWeapon.LANGBOGEN -> 12 + dexMod
            ActiveWeapon.KURZSCHWERT_SCHILD -> 12 + dexMod + 2
            ActiveWeapon.SHILLELAGH_SCHILD -> 12 + dexMod + 2
        }

    val currentAttackBonus: String
        get() = when (currentWeapon) {
            ActiveWeapon.LANGBOGEN -> "+${proficiencyBonus + dexMod + 2}"
            ActiveWeapon.KURZSCHWERT_SCHILD -> "+${proficiencyBonus + dexMod}"
            ActiveWeapon.SHILLELAGH_SCHILD -> "+${proficiencyBonus + wisMod}"
        }

    val currentDamage: String
        get() = when (currentWeapon) {
            ActiveWeapon.LANGBOGEN -> "1W8 + $dexMod Stich (Gegner -3m Tempo)"
            ActiveWeapon.KURZSCHWERT_SCHILD -> "1W6 + $dexMod Stich (Vorteil auf nächsten Angriff)"
            ActiveWeapon.SHILLELAGH_SCHILD -> "1W8 + $wisMod Wucht (Umstoßen: ST-Save 12)"
        }

    var spellSlotsLevel1 by mutableIntStateOf(prefs.getInt("spellSlotsLevel1", 3))
        private set
    var huntersMarkFreeUses by mutableIntStateOf(prefs.getInt("huntersMarkFreeUses", 2))
        private set

    fun useSpellSlotLevel1() {
        if (spellSlotsLevel1 > 0) {
            spellSlotsLevel1--
            prefs.edit { putInt("spellSlotsLevel1", spellSlotsLevel1) }
        }
    }

    fun useHuntersMarkFree() {
        if (huntersMarkFreeUses > 0) {
            huntersMarkFreeUses--
            prefs.edit { putInt("huntersMarkFreeUses", huntersMarkFreeUses) }
        }
    }

    fun castGoodberry() {
        if (spellSlotsLevel1 > 0) {
            spellSlotsLevel1--
            goodberries += 10
            prefs.edit {
                putInt("spellSlotsLevel1", spellSlotsLevel1)
                putInt("goodberries", goodberries)
            }
        }
    }

    var water by mutableFloatStateOf(prefs.getFloat("water", 2.0f))
        private set
    var rations by mutableIntStateOf(prefs.getInt("rations", 10))
        private set
    var goodberries by mutableIntStateOf(prefs.getInt("goodberries", 0))
        private set

    fun changeWater(amount: Float) {
        water = (water + amount).coerceAtLeast(0f)
        prefs.edit { putFloat("water", water) }
    }

    fun changeRations(amount: Int) {
        rations = (rations + amount).coerceAtLeast(0)
        prefs.edit { putInt("rations", rations) }
    }

    fun changeGoodberries(amount: Int) {
        goodberries = (goodberries + amount).coerceAtLeast(0)
        prefs.edit { putInt("goodberries", goodberries) }
    }

    val customLoot = mutableStateListOf<InventoryItem>()
    private fun saveLoot() {
        val lootString = customLoot.joinToString(";") { "${it.name}|${it.amount}" }
        prefs.edit { putString("customLoot", lootString) }
    }

    private fun loadLoot() {
        val lootString = prefs.getString("customLoot", "") ?: ""
        if (lootString.isNotEmpty()) {
            val items = lootString.split(";").mapNotNull {
                val parts = it.split("|")
                if (parts.size == 2) InventoryItem(parts[0], parts[1].toIntOrNull() ?: 1) else null
            }
            customLoot.clear()
            customLoot.addAll(items)
        }
    }

    fun addCustomLoot(itemName: String) {
        val index = customLoot.indexOfFirst { it.name.equals(itemName, ignoreCase = true) }
        if (index != -1) {
            val existingItem = customLoot[index]
            customLoot[index] = existingItem.copy(amount = existingItem.amount + 1)
        } else {
            customLoot.add(InventoryItem(itemName, 1))
        }
        saveLoot()
    }

    fun removeCustomLoot(itemName: String) {
        val index = customLoot.indexOfFirst { it.name.equals(itemName, ignoreCase = true) }
        if (index != -1) {
            val existingItem = customLoot[index]
            if (existingItem.amount > 1) {
                customLoot[index] = existingItem.copy(amount = existingItem.amount - 1)
            } else {
                customLoot.removeAt(index)
            }
            saveLoot()
        }
    }

    fun takeShortRest(rolledValue: Int) {
        if (hitDice > 0 && currentHp < maxHp) {
            hitDice--
            val healAmount = rolledValue + conMod
            currentHp = (currentHp + healAmount).coerceAtMost(maxHp)

            prefs.edit {
                putInt("hitDice", hitDice)
                putInt("currentHp", currentHp)
            }
        }
    }

    fun takeLongRest() {
        spellSlotsLevel1 = 3
        huntersMarkFreeUses = 2
        goodberries = 0
        geminiUsesToday = 0
        currentHp = maxHp
        hitDice = 4
        changeWater(-0.5f)
        changeRations(-1)

        prefs.edit {
            putInt("spellSlotsLevel1", spellSlotsLevel1)
            putInt("huntersMarkFreeUses", huntersMarkFreeUses)
            putInt("goodberries", goodberries)
            putInt("currentHp", currentHp)
            putInt("hitDice", hitDice)
            putInt("geminiUsesToday", 0)
        }
    }

    var isSkyBeast by mutableStateOf(prefs.getBoolean("isSkyBeast", true))
        private set

    val capyMaxHp: Int get() = if (isSkyBeast) 4 + (4 * level) else 5 + (5 * level)
    var capyCurrentHp by mutableIntStateOf(prefs.getInt("capyCurrentHp", 20))
        private set

    fun toggleBeastType(isSky: Boolean) {
        isSkyBeast = isSky
        if (capyCurrentHp > capyMaxHp) capyCurrentHp = capyMaxHp
        prefs.edit {
            putBoolean("isSkyBeast", isSkyBeast)
            putInt("capyCurrentHp", capyCurrentHp)
        }
    }

    fun takeCapyDamage(amount: Int) {
        capyCurrentHp = (capyCurrentHp - amount).coerceAtLeast(0)
        prefs.edit { putInt("capyCurrentHp", capyCurrentHp) }
    }

    fun healCapy(amount: Int) {
        capyCurrentHp = (capyCurrentHp + amount).coerceAtMost(capyMaxHp)
        prefs.edit { putInt("capyCurrentHp", capyCurrentHp) }
    }

    val capyAc: Int get() = 13 + proficiencyBonus
    val capyAttackBonus: String get() = "+$spellAttackBonus"
    val capyDamage: String get() = if (isSkyBeast) "1W4 + $wisMod Hieb" else "1W8 + $wisMod Hieb"
    val capySpeed: String get() = if (isSkyBeast) "Fliegen 18 m, Laufen 3 m" else "Laufen 12 m, Klettern 12 m"
    val capySpecial: String get() = if (isSkyBeast) "Vorbeifliegen" else "Ansturm"

    // --- HILFE: CHAT & FAQ ---
    val chatHistory = mutableStateListOf<ChatMessage>()
    val faqList = mutableStateListOf<FaqItem>()

    var currentUsedModel by mutableStateOf("Bereit")
        private set
    var geminiUsesToday by mutableIntStateOf(prefs.getInt("geminiUsesToday", 0))
        private set
    val geminiMax = 20

    private val systemPrompt = "Du bist ein Dungeons and Dragons Regel-Assistent. Beziehe dich ausschließlich auf die Regeln des Player Handbook 2024. Wir spielen nicht abwärtskompatibel. Antworte extrem kurz, präzise und leicht verständlich auf Deutsch."

    // Initialisierung über Firebase.ai.generativeModel
    private val model3Flash = Firebase.ai.generativeModel(
        modelName = "gemini-1.5-flash"
    )

    private val model25Flash = Firebase.ai.generativeModel(
        modelName = "gemini-1.5-pro"
    )

    // Chat-Session startet standardmäßig mit Gemini 3
    private var activeChatSession = model3Flash.startChat()

    init {
        loadLoot()
        loadFaqs()
    }

    fun loadFaqs() {
        val faqString = prefs.getString("savedFaqs", "") ?: ""
        if (faqString.isNotEmpty()) {
            val items = faqString.split("||").mapNotNull {
                val parts = it.split("|:|")
                if (parts.size == 2) FaqItem(parts[0], parts[1]) else null
            }
            faqList.clear()
            faqList.addAll(items)
        }
    }

    private fun saveFaqs() {
        val faqString = faqList.joinToString("||") { "${it.question}|:|${it.answer}" }
        prefs.edit { putString("savedFaqs", faqString) }
    }

    private fun getCharacterContext(): String {
        return """
            KONTEXT ATHANIA (Level $level):
            HP: $currentHp/$maxHp, Trefferwürfel: $hitDice/4.
            Zauberplätze Grad 1: $spellSlotsLevel1/3.
            Kostenloses Zeichen des Jägers: $huntersMarkFreeUses/2.
            Vorräte: $water Liter Wasser, $rations Rationen, $goodberries Gute Beeren.
            Werte: ST 10, GE $dexterity (+4), KO $constitution (+3), IN 10, WE $wisdom (+2), CH 10.
            Ausrüstung: ${currentWeapon.name}.
        """.trimIndent()
    }

    fun sendMessageToBot(message: String) {
        chatHistory.add(ChatMessage(message, true))
        val loadingIndex = chatHistory.size
        chatHistory.add(ChatMessage("... überlegt ...", false))

        val context = getCharacterContext()
        val finalPrompt = "$systemPrompt\n\n$context\n\nFrage: $message"

        viewModelScope.launch {
            try {
                if (geminiUsesToday < geminiMax) {
                    try {
                        // Versuch 1: Gemini 3 Flash
                        currentUsedModel = "Gemini 3 Flash"
                        val response = activeChatSession.sendMessage(finalPrompt)
                        finalizeResponse(loadingIndex, response.text)
                    } catch (_: Exception) {
                        // Fallback: Gemini 2.5 Flash
                        currentUsedModel = "Gemini 2.5 Flash (Fallback)"
                        val fallbackSession = model25Flash.startChat(history = activeChatSession.history)
                        val response = fallbackSession.sendMessage(finalPrompt)
                        activeChatSession = fallbackSession // Bleibe für diesen Chat beim Fallback
                        finalizeResponse(loadingIndex, response.text)
                    }
                } else {
                    throw Exception("Limit erreicht")
                }
            } catch (e: Exception) {
                chatHistory[loadingIndex] = ChatMessage("Fehler: ${e.localizedMessage}", false)
            }
        }
    }

    private fun finalizeResponse(index: Int, text: String?) {
        geminiUsesToday++
        prefs.edit { putInt("geminiUsesToday", geminiUsesToday) }
        chatHistory[index] = ChatMessage(text ?: "Keine Antwort.", false)
    }

    fun resetChat() {
        chatHistory.clear()
        activeChatSession = model3Flash.startChat()
        currentUsedModel = "Bereit"
    }

    fun addChatToFaq(question: String, answer: String) {
        faqList.add(FaqItem(question, answer))
        saveFaqs()
    }

    fun removeFaq(item: FaqItem) {
        faqList.remove(item)
        saveFaqs()
    }
}
