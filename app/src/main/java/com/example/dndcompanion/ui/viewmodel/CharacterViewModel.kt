package com.example.dndcompanion.ui.viewmodel

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
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

// --- DATENKLASSEN & ENUMS ---
data class InventoryItem(val name: String, val amount: Int, val weight: Double = 0.0)
data class ChatMessage(val text: String, val isUser: Boolean)
data class FaqItem(val question: String, val answer: String)

data class Spell(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val level: Int,
    val castingTime: String,
    val range: String,
    val duration: String,
    val componentsV: Boolean = false,
    val componentsS: Boolean = false,
    val componentsM: Boolean = false,
    val materialCost: String = "",
    val description: String,
    var isPrepared: Boolean = false
)

enum class ActiveWeapon {
    LANGBOGEN,
    KURZSCHWERT_SCHILD,
    SHILLELAGH_SCHILD
}

class CharacterViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("AthaniaSaveGame", Context.MODE_PRIVATE)
    private val gson = Gson()

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

    // --- MÜNZEN (COINS) ---
    var coinsKM by mutableIntStateOf(prefs.getInt("coinsKM", 0))
        private set
    var coinsSM by mutableIntStateOf(prefs.getInt("coinsSM", 0))
        private set
    var coinsEM by mutableIntStateOf(prefs.getInt("coinsEM", 0))
        private set
    var coinsGM by mutableIntStateOf(prefs.getInt("coinsGM", 0))
        private set
    var coinsPM by mutableIntStateOf(prefs.getInt("coinsPM", 0))
        private set

    fun changeCoinsKM(amount: Int) {
        coinsKM = (coinsKM + amount).coerceAtLeast(0)
        prefs.edit { putInt("coinsKM", coinsKM) }
    }
    fun changeCoinsSM(amount: Int) {
        coinsSM = (coinsSM + amount).coerceAtLeast(0)
        prefs.edit { putInt("coinsSM", coinsSM) }
    }
    fun changeCoinsEM(amount: Int) {
        coinsEM = (coinsEM + amount).coerceAtLeast(0)
        prefs.edit { putInt("coinsEM", coinsEM) }
    }
    fun changeCoinsGM(amount: Int) {
        coinsGM = (coinsGM + amount).coerceAtLeast(0)
        prefs.edit { putInt("coinsGM", coinsGM) }
    }
    fun changeCoinsPM(amount: Int) {
        coinsPM = (coinsPM + amount).coerceAtLeast(0)
        prefs.edit { putInt("coinsPM", coinsPM) }
    }

    var totalArrows by mutableIntStateOf(prefs.getInt("totalArrows", 20))
        private set
    var shotArrows by mutableIntStateOf(prefs.getInt("shotArrows", 0))
        private set

    fun shootArrow() {
        if (totalArrows > 0) {
            totalArrows--
            shotArrows++
            prefs.edit {
                putInt("totalArrows", totalArrows)
                putInt("shotArrows", shotArrows)
            }
        }
    }

    fun recoverArrows() {
        if (shotArrows > 0) {
            val recovered = shotArrows / 2 // Integer Division rundet automatisch ab
            totalArrows += recovered
            shotArrows = 0
            prefs.edit {
                putInt("totalArrows", totalArrows)
                putInt("shotArrows", shotArrows)
            }
        }
    }

    fun discardShotArrows() {
        shotArrows = 0
        prefs.edit { putInt("shotArrows", shotArrows) }
    }

    fun changeTotalArrows(amount: Int) {
        totalArrows = (totalArrows + amount).coerceAtLeast(0)
        prefs.edit { putInt("totalArrows", totalArrows) }
    }

    // --- GEWICHTS-BERECHNUNG ---
    val maxWeight: Double = 60.0
    val currentWeight: Double
        get() {
            var total = 0.0
            total += water * 2.0          // 1 Tag Wasser = ca. 2kg
            total += rations * 1.0        // 1 Ration = ca. 1kg
            total += totalArrows * 0.05   // 1 Pfeil = ca. 0.05kg
            total += customLoot.sumOf { it.amount * it.weight }
            return total
        }

    val customLoot = mutableStateListOf<InventoryItem>()
    private fun saveLoot() {
        val json = gson.toJson(customLoot)
        prefs.edit { putString("customLoot", json) }
    }

    private fun loadLoot() {
        val jsonString = prefs.getString("customLoot", "") ?: ""
        if (jsonString.isNotEmpty()) {
            if (jsonString.startsWith("[")) {
                // Es ist sehr wahrscheinlich ein JSON-String
                try {
                    val type = object : TypeToken<List<InventoryItem>>() {}.type
                    val items: List<InventoryItem> = gson.fromJson(jsonString, type)
                    customLoot.clear()
                    customLoot.addAll(items)
                } catch (e: Exception) {
                    customLoot.clear()
                }
            } else {
                // Fallback für alte Speicherstände mit Strichpunkt und Pipe
                val items = jsonString.split(";").mapNotNull {
                    val parts = it.split("|")
                    if (parts.size == 2) InventoryItem(parts[0], parts[1].toIntOrNull() ?: 1) else null
                }
                customLoot.clear()
                customLoot.addAll(items)
            }
        } else {
            // Initiale Gegenstände beim allerersten Start laden
            customLoot.addAll(getAthaniaDefaultLoot())
            saveLoot()
        }
    }

    private fun getAthaniaDefaultLoot(): List<InventoryItem> {
        return listOf(
            InventoryItem("Beschlagene Lederrüstung", 1, 6.0),
            InventoryItem("Langbogen", 1, 1.0),
            InventoryItem("Kurzschwert", 1, 1.0),
            InventoryItem("Kampfstab", 1, 2.0),
            InventoryItem("Peitsche", 1, 1.5),
            InventoryItem("Schild", 1, 3.0),
            InventoryItem("Reisekleidung", 1, 2.0),
            InventoryItem("Rucksack", 1, 2.5),
            InventoryItem("Kleine Onyxstatue (Fokus)", 1, 0.5),
            InventoryItem("Kräuterkundeset", 1, 1.5),
            InventoryItem("Schwarzer Onyxschädel", 1, 1.0),
            InventoryItem("Wasserschlauch (halb)", 2, 1.0),
            InventoryItem("Trank der Rinderhaut", 1, 0.5),
            InventoryItem("Gift (Flasche)", 2, 0.5),
            InventoryItem("Heiltrank", 1, 0.5),
            InventoryItem("Hämatit", 1, 0.1)
        )
    }

    fun addCustomLoot(itemName: String, weight: Double = 0.0) {
        val index = customLoot.indexOfFirst { it.name.equals(itemName, ignoreCase = true) }
        if (index != -1) {
            val existingItem = customLoot[index]
            customLoot[index] = existingItem.copy(amount = existingItem.amount + 1)
        } else {
            customLoot.add(InventoryItem(itemName, 1, weight))
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

    private val model3Flash = GenerativeModel(
        modelName = "gemini-3.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val model25Flash = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private var activeChatSession = model3Flash.startChat()

    fun loadFaqs() {
        val faqString = prefs.getString("savedFaqs", "") ?: ""
        if (faqString.isNotEmpty()) {
            if (faqString.startsWith("[")) {
                // Es ist sehr wahrscheinlich ein JSON-String
                try {
                    val type = object : TypeToken<List<FaqItem>>() {}.type
                    val items: List<FaqItem> = gson.fromJson(faqString, type)
                    faqList.clear()
                    faqList.addAll(items)
                } catch (e: Exception) {
                    faqList.clear()
                }
            } else {
                // Fallback für alte Speicherstände
                val items = faqString.split("||").mapNotNull {
                    val parts = it.split("|:|")
                    if (parts.size == 2) FaqItem(parts[0], parts[1]) else null
                }
                faqList.clear()
                faqList.addAll(items)
            }
        }
    }

    private fun saveFaqs() {
        val json = gson.toJson(faqList)
        prefs.edit { putString("savedFaqs", json) }
    }

    // --- SPELBOOK (ZAUBERBUCH) ---
    val allSpells = mutableStateListOf<Spell>()

    init {
        loadLoot()
        loadFaqs()
        loadSpells()
    }

    private fun saveSpells() {
        val json = gson.toJson(allSpells)
        prefs.edit { putString("savedSpells", json) }
    }

    private fun loadSpells() {
        val spellString = prefs.getString("savedSpells", "") ?: ""
        if (spellString.isNotEmpty()) {
            try {
                val type = object : TypeToken<List<Spell>>() {}.type
                val items: List<Spell> = gson.fromJson(spellString, type)
                allSpells.clear()
                allSpells.addAll(items)
            } catch (e: Exception) {
                // Bei Fehlern nicht abstürzen
            }
        } else {
            // Wenn leer, Standard-Zauber von Athania laden
            allSpells.addAll(getAthaniaDefaultSpells())
            saveSpells()
        }
    }

    fun toggleSpellPrepared(spellId: String) {
        val index = allSpells.indexOfFirst { it.id == spellId }
        if (index != -1) {
            val spell = allSpells[index]
            allSpells[index] = spell.copy(isPrepared = !spell.isPrepared)
            saveSpells()
        }
    }

    fun addNewSpell(spell: Spell) {
        allSpells.add(spell)
        saveSpells()
    }

    fun removeSpell(spellId: String) {
        allSpells.removeAll { it.id == spellId }
        saveSpells()
    }

    private fun getAthaniaDefaultSpells(): List<Spell> {
        return listOf(
            Spell(
                name = "Shillelagh",
                level = 0,
                castingTime = "1 Bonusaktion",
                range = "Berührung",
                duration = "1 Minute",
                componentsV = true, componentsS = true, componentsM = true,
                materialCost = "Ein Knüppel oder Kampfstab",
                description = "Der Knüppel oder Kampfstab, den du hältst, ist von der Macht der Natur erfüllt. Für die Wirkungsdauer kannst du deinen Zauber-Attributsmodifikator anstelle deines Stärke-Modifikators für Angriffs- und Schadenswürfe mit dieser Waffe verwenden. Die Waffe verursacht nun d8 Schaden.",
                isPrepared = true
            ),
            Spell(
                name = "Zeichen des Jägers",
                level = 1,
                castingTime = "1 Bonusaktion",
                range = "27 m",
                duration = "Konzentration, bis zu 1 Std.",
                componentsV = true, componentsS = false, componentsM = false,
                description = "Du wählst eine Kreatur, die du in Reichweite sehen kannst, als deine Beute aus. Bis der Zauber endet, fügst du dem Ziel jedes Mal, wenn du es mit einem Waffenangriff triffst, zusätzlich 1d6 Kraftschaden zu. Vorteil auf Überleben (Fährtenlesen).",
                isPrepared = true
            ),
            Spell(
                name = "Gute Beere",
                level = 1,
                castingTime = "1 Aktion",
                range = "Berührung",
                duration = "Sofort",
                componentsV = true, componentsS = true, componentsM = true,
                materialCost = "Ein Zweig eines Mistelzweigs",
                description = "In deiner Hand erscheinen bis zu zehn Beeren. Eine Kreatur kann eine Aktion ausführen, um eine Beere zu essen. Die Beere heilt 1 Trefferpunkt und spendet genug Nahrung für einen ganzen Tag (PHB 2024). Verliert nach 24 Std. ihre Wirkung.",
                isPrepared = true
            ),
            Spell(
                name = "Wunden heilen",
                level = 1,
                castingTime = "1 Aktion",
                range = "Berührung",
                duration = "Sofort",
                componentsV = true, componentsS = true, componentsM = false,
                description = "Eine Kreatur, die du berührst, erhält Trefferpunkte in Höhe von 2d8 + deinem Zauber-Attributsmodifikator (PHB 2024) zurück.",
                isPrepared = true
            )
        )
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
                        currentUsedModel = "Gemini 3.0 Flash"
                        val response = activeChatSession.sendMessage(finalPrompt)
                        finalizeResponse(loadingIndex, response.text)
                    } catch (e: Exception) {
                        currentUsedModel = "Gemini 2.5 Flash (Fallback)"
                        val fallbackSession = model25Flash.startChat(history = activeChatSession.history)
                        val response = fallbackSession.sendMessage(finalPrompt)
                        activeChatSession = fallbackSession
                        finalizeResponse(loadingIndex, response.text)
                    }
                } else {
                    throw Exception("Limit erreicht")
                }
            } catch (e: Exception) {
                // Fängt den Serialisierungsbug ab, der bei 404 (Modell nicht gefunden) auftritt
                val errorMsg = if (e.localizedMessage?.contains("MissingFieldException") == true) {
                    "Fehler: Das Modell konnte nicht gefunden werden. Prüfe, ob dein API-Key bereits für die 3.0 / 2.5 Modelle freigeschaltet ist."
                } else {
                    "Fehler: ${e.localizedMessage}"
                }
                chatHistory[loadingIndex] = ChatMessage(errorMsg, false)
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