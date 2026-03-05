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
import com.google.ai.client.generativeai.type.generationConfig
import org.json.JSONObject

// --- DATENKLASSEN & ENUMS ---
data class InventoryItem(val name: String, val amount: Int, val weight: Double = 0.0, val category: String = "Sonstiges")
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String, 
    val isUser: Boolean,
    val localText: String? = null,
    val externalText: String? = null,
    val chapterLink: String? = null,
    val chapterSearchTerm: String? = null // NEU: Für zielgenaues Scrollen
)

data class FaqItem(val question: String, val answer: String)
data class TraitItem(val name: String, val desc: String)
data class BookEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

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
    val classes: List<String> = emptyList(),
    var isPrepared: Boolean = false
)

enum class ActiveWeapon {
    LANGBOGEN,
    KURZSCHWERT_SCHILD,
    SHILLELAGH_SCHILD
}

data class EquipmentDefinition(
    val name: String,
    val weight: Double,
    val category: String
)

enum class BeastType {
    LAND,
    SKY,
    SEA
}

class CharacterViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("AthaniaSaveGame", Context.MODE_PRIVATE)
    private val gson = Gson()

    // --- BASISWERTE ---
    // EP Table D&D 5e:
    private val epThresholds = listOf(
        0, 300, 900, 2700, 6500, 14000, 23000, 34000, 48000, 64000,
        85000, 100000, 120000, 140000, 165000, 195000, 225000, 265000, 305000, 355000
    )

    var currentEP by mutableIntStateOf(prefs.getInt("currentEP", 3606))
        private set

    var level by mutableIntStateOf(prefs.getInt("level", 4))
        private set

    var strength by mutableIntStateOf(prefs.getInt("strength", 8))
        private set
    var dexterity by mutableIntStateOf(prefs.getInt("dexterity", 18))
        private set
    var constitution by mutableIntStateOf(prefs.getInt("constitution", 16))
        private set
    var intelligence by mutableIntStateOf(prefs.getInt("intelligence", 10))
        private set
    var wisdom by mutableIntStateOf(prefs.getInt("wisdom", 14))
        private set
    var charisma by mutableIntStateOf(prefs.getInt("charisma", 8))
        private set

    var showLevelUpDialog by mutableStateOf(false)
        private set

    var showLevelUpNotification by mutableStateOf(false)
        private set

    fun addExperience(amount: Int) {
        currentEP += amount
        prefs.edit { putInt("currentEP", currentEP) }
        checkLevelUp()
    }
        
    private fun checkLevelUp() {
        val newLevel = epThresholds.indexOfLast { currentEP >= it } + 1
        if (newLevel > level) {
            val oldLevel = level
            level = newLevel
            prefs.edit { putInt("level", level) }
            showLevelUpDialog = true
            showLevelUpNotification = true
            
            // --- AUTOMATISIERUNGEN FÜR WALDLÄUFER & BEAST MASTER (2024) ---
            for (lvl in (oldLevel + 1)..newLevel) {
                when (lvl) {
                    5 -> {
                        spellSlotsLevel2 = 2
                        prefs.edit { putInt("spellSlotsLevel2", spellSlotsLevel2) }
                        addCustomTrait("Zusätzlicher Angriff (Level 5)", "Du kannst zweimal angreifen, wenn du die Angriffsaktion ausführst.")
                    }
                    6 -> {
                        addCustomTrait("Umherziehen / Roving (Level 6)", "Deine Bewegungsrate erhöht sich um 3m, wenn du keine schwere Rüstung trägst. Du erhältst eine Kletter- und Schwimmrate in Höhe deiner Gehgeschwindigkeit.")
                    }
                    7 -> {
                        addCustomTrait("Außergewöhnliches Training (Level 7)", "Die Bestie kann Spurt, Rückzug, Ausweichen oder Hilfe als Bonusaktion nutzen. Ihre Angriffe können nun Wuchtschaden oder Energieschaden (Force) verursachen.")
                    }
                    9 -> {
                        spellSlotsLevel3 = 2
                        prefs.edit { putInt("spellSlotsLevel3", spellSlotsLevel3) }
                        addCustomTrait("Expertise 2 (Level 9)", "Wähle zwei weitere Fertigkeiten für Expertise aus dem Handbuch.")
                    }
                    10 -> {
                        addCustomTrait("Unermüdlich / Tireless (Level 10)", "Temporäre Trefferpunkte: Als Magie-Aktion erhältst du 1W8 + WIS-Mod TP (Nutzungen = WIS-Mod pro Tag). Erschöpfung: Eine Kurze Rast verringert deine Erschöpfung um 1 Stufe.")
                    }
                }
            }
        }
    }

    fun dismissLevelUpDialog() {
        showLevelUpDialog = false
    }

    fun dismissLevelUpNotification() {
        showLevelUpNotification = false
    }

    var targetRulebookChapter by mutableStateOf<String?>(null)
    var targetRulebookSearch by mutableStateOf<String?>(null)

    fun applyHpIncrease(conModifier: Int, rolledHp: Int = 6) {
        val hpIncrease = rolledHp + conModifier
        maxHp += hpIncrease
        hitDice += 1
        currentHp = (currentHp + hpIncrease).coerceAtMost(maxHp)
        prefs.edit { 
            putInt("maxHp", maxHp)
            putInt("hitDice", hitDice)
            putInt("currentHp", currentHp) 
        }
    }
    
    fun updateAttributes(strMod: Int = 0, dexMod: Int = 0, conMod: Int = 0, intMod: Int = 0, wisMod: Int = 0, chaMod: Int = 0) {
        strength += strMod
        dexterity += dexMod
        constitution += conMod
        intelligence += intMod
        wisdom += wisMod
        charisma += chaMod
        
        prefs.edit {
            putInt("strength", strength)
            putInt("dexterity", dexterity)
            putInt("constitution", constitution)
            putInt("intelligence", intelligence)
            putInt("wisdom", wisdom)
            putInt("charisma", charisma)
        }
    }

    val proficiencyBonus: Int
        get() = when(level) {
            in 1..4 -> 2
            in 5..8 -> 3
            in 9..12 -> 4
            in 13..16 -> 5
            else -> 6
        }

    val strMod: Int get() = (strength - 10) / 2
    val dexMod: Int get() = (dexterity - 10) / 2
    val conMod: Int get() = (constitution - 10) / 2
    val intMod: Int get() = (intelligence - 10) / 2
    val wisMod: Int get() = (wisdom - 10) / 2
    val chaMod: Int get() = (charisma - 10) / 2

    val spellAttackBonus: Int get() = proficiencyBonus + wisMod
    val spellSaveDc: Int get() = 8 + proficiencyBonus + wisMod

    var maxHp by mutableIntStateOf(prefs.getInt("maxHp", 40))
        private set
    var currentHp by mutableIntStateOf(prefs.getInt("currentHp", maxHp))
        private set
    var hitDice by mutableIntStateOf(prefs.getInt("hitDice", 4))
        private set

    var deathSaveSuccesses by mutableIntStateOf(prefs.getInt("deathSaveSuccesses", 0))
        private set
    var deathSaveFailures by mutableIntStateOf(prefs.getInt("deathSaveFailures", 0))
        private set

    fun updateDeathSaves(successes: Int, failures: Int) {
        deathSaveSuccesses = successes.coerceIn(0, 3)
        deathSaveFailures = failures.coerceIn(0, 3)
        prefs.edit {
            putInt("deathSaveSuccesses", deathSaveSuccesses)
            putInt("deathSaveFailures", deathSaveFailures)
        }
    }

    fun takeDamage(amount: Int) {
        currentHp = (currentHp - amount).coerceAtLeast(0)
        if (currentHp > 0) {
            updateDeathSaves(0, 0)
        }
        prefs.edit { putInt("currentHp", currentHp) }
    }

    fun healManual(amount: Int) {
        currentHp = (currentHp + amount).coerceAtMost(maxHp)
        if (currentHp > 0) {
            updateDeathSaves(0, 0)
        }
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
    var spellSlotsLevel2 by mutableIntStateOf(prefs.getInt("spellSlotsLevel2", 0))
        private set
    var spellSlotsLevel3 by mutableIntStateOf(prefs.getInt("spellSlotsLevel3", 0))
        private set

    var huntersMarkFreeUses by mutableIntStateOf(prefs.getInt("huntersMarkFreeUses", 2))
        private set

    fun useSpellSlotLevel1() {
        if (spellSlotsLevel1 > 0) {
            spellSlotsLevel1--
            prefs.edit { putInt("spellSlotsLevel1", spellSlotsLevel1) }
        }
    }
    
    fun useSpellSlotLevel2() {
        if (spellSlotsLevel2 > 0) {
            spellSlotsLevel2--
            prefs.edit { putInt("spellSlotsLevel2", spellSlotsLevel2) }
        }
    }
    
    fun useSpellSlotLevel3() {
        if (spellSlotsLevel3 > 0) {
            spellSlotsLevel3--
            prefs.edit { putInt("spellSlotsLevel3", spellSlotsLevel3) }
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

    // --- EQUIPMENT-KATALOG ---
    val equipmentCatalog: List<EquipmentCatalogItem> by lazy {
        EquipmentCatalogParser.loadFromAssets(getApplication())
    }

    // --- GEWICHTS-BERECHNUNG (in Pfund / lbs) ---
    val maxWeight: Double
        get() = strength * 15.0  // D&D Traglast = STR × 15 Pfd.
    val currentWeight: Double
        get() {
            var total = 0.0
            total += water * 5.0          // 1 Trinkschlauch (voll) = 5 Pfd.
            total += rations * 2.0        // 1 Ration = 2 Pfd.
            total += totalArrows * 0.05   // 1 Pfeil = ca. 0.05 Pfd.
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
            InventoryItem("Beschlagene Lederrüstung", 1, 13.0, "Rüstung & Waffen"),
            InventoryItem("Langbogen", 1, 2.0, "Rüstung & Waffen"),
            InventoryItem("Kurzschwert", 1, 2.0, "Rüstung & Waffen"),
            InventoryItem("Kampfstab", 1, 4.0, "Rüstung & Waffen"),
            InventoryItem("Peitsche", 1, 3.0, "Rüstung & Waffen"),
            InventoryItem("Schild", 1, 6.0, "Rüstung & Waffen"),
            InventoryItem("Reisekleidung", 1, 4.0, "Ausrüstung"),
            InventoryItem("Rucksack", 1, 5.0, "Ausrüstung"),
            InventoryItem("Kleine Onyxstatue (Fokus)", 1, 1.0, "Magie"),
            InventoryItem("Kräuterkundeset", 1, 3.0, "Werkzeug"),
            InventoryItem("Schwarzer Onyxschädel", 1, 1.0, "Sonstiges"),
            InventoryItem("Wasserschlauch (halb)", 2, 2.5, "Ausrüstung"),
            InventoryItem("Trank der Rinderhaut", 1, 0.5, "Tränke"),
            InventoryItem("Gift (Flasche)", 2, 0.5, "Tränke"),
            InventoryItem("Heiltrank", 1, 0.5, "Tränke"),
            InventoryItem("Hämatit", 1, 0.1, "Schätze")
        )
    }

    fun addCustomLoot(itemName: String, weight: Double = 0.0, category: String = "Sonstiges") {
        val index = customLoot.indexOfFirst { it.name.equals(itemName, ignoreCase = true) }
        if (index != -1) {
            val existingItem = customLoot[index]
            customLoot[index] = existingItem.copy(amount = existingItem.amount + 1)
        } else {
            customLoot.add(InventoryItem(itemName, 1, weight, category))
        }
        saveLoot()
    }

    fun addFromCatalog(item: EquipmentCatalogItem) {
        val inventoryCategory = when {
            item.category.startsWith("Waffen") -> "Rüstung & Waffen"
            item.category == "Rüstung" -> "Rüstung & Waffen"
            item.category == "Werkzeug" -> "Werkzeug"
            item.category == "Ausrüstung" -> "Ausrüstung"
            else -> "Sonstiges"
        }
        addCustomLoot(item.name, item.weight, inventoryCategory)
    }

    // --- FREIE MERKMALE (TRAITS) ---
    val customTraits = mutableStateListOf<TraitItem>()
    private fun saveTraits() {
        val json = gson.toJson(customTraits)
        prefs.edit { putString("customTraits", json) }
    }
    
    private fun loadTraits() {
        val jsonString = prefs.getString("customTraits", "") ?: ""
        if (jsonString.isNotEmpty()) {
            try {
                val type = object : TypeToken<List<TraitItem>>() {}.type
                val items: List<TraitItem> = gson.fromJson(jsonString, type)
                customTraits.clear()
                customTraits.addAll(items)
            } catch (e: Exception) {
                // Ignore
            }
        } else {
            // Initiale Merkmale beim allerersten Start laden
            customTraits.addAll(getDefaultTraits())
            saveTraits()
        }
    }

    private fun getDefaultTraits(): List<TraitItem> {
        return listOf(
            TraitItem("Urbegleiter (Land, Himmel, Meer)", "Bonusaktion: Urtier befehligen\nAktion: Urtier Angriff\nZauberslot: Urtier beleben (volle HP)"),
            TraitItem("Trance", "Du musst nicht schlafen. Lange Rast dauert 4 Std in Meditation."),
            TraitItem("Feenblut", "Vorteil bei Rettungswürfen gegen Bezauberung."),
            TraitItem("Messerstecher", "Bei Stichschaden 1x pro Zug 1 Angriffswürfel neu würfeln. Bei Krit 1 zus. Schadenswürfel.")
        )
    }

    fun addCustomTrait(name: String, desc: String) {
        customTraits.add(TraitItem(name, desc))
        saveTraits()
    }

    fun removeCustomTrait(index: Int) {
        if (index in customTraits.indices) {
            customTraits.removeAt(index)
            saveTraits()
        }
    }

    fun updateCustomTrait(index: Int, name: String, desc: String) {
        if (index in customTraits.indices) {
            customTraits[index] = TraitItem(name, desc)
            saveTraits()
        }
    }

    // --- WERTE ZURÜCKSETZEN ---
    fun resetToDefaults() {
        // Alle SharedPreferences löschen und Grundwerte gemäß stats.md setzen
        prefs.edit { clear() }

        currentEP = 3606
        level = 4
        strength = 8
        dexterity = 18
        constitution = 16
        intelligence = 10
        wisdom = 14
        charisma = 8
        maxHp = 40
        currentHp = 40
        hitDice = 4
        spellSlotsLevel1 = 3
        spellSlotsLevel2 = 0
        spellSlotsLevel3 = 0
        huntersMarkFreeUses = 2
        freeCureWoundsUsed = false
        freeHealingWordUsed = false
        freeFaerieFireUsed = false
        freeDarknessUsed = false
        freeDruidSpellUsed = false
        water = 2.0f
        rations = 10
        goodberries = 0
        coinsKM = 0
        coinsSM = 1
        coinsEM = 0
        coinsGM = 18
        coinsPM = 0
        totalArrows = 28
        shotArrows = 0
        currentWeapon = ActiveWeapon.LANGBOGEN
        deathSaveSuccesses = 0
        deathSaveFailures = 0
        activeBeastType = BeastType.SKY
        generalBookEntries.clear()
        saveGeneralBookEntries()
        grudgeBookEntries.clear()
        saveGrudgeBookEntries()
        standardTactic = "1. Zeichen des Jägers wirken (Bonusaktion)\n2. Mit Langbogen angreifen"

        // Loot und Traits zurücksetzen
        customLoot.clear()
        customLoot.addAll(getAthaniaDefaultLoot())
        saveLoot()
        customTraits.clear()
        customTraits.addAll(getDefaultTraits())
        saveTraits()
    }

    // --- BÜCHER & TAKTIK ---
    val generalBookEntries = mutableStateListOf<BookEntry>()
    val grudgeBookEntries = mutableStateListOf<BookEntry>()

    private fun saveGeneralBookEntries() {
        prefs.edit { putString("generalBookEntries", gson.toJson(generalBookEntries)) }
    }

    private fun saveGrudgeBookEntries() {
        prefs.edit { putString("grudgeBookEntries", gson.toJson(grudgeBookEntries)) }
    }

    private fun loadBooks() {
        val generalJson = prefs.getString("generalBookEntries", "[]") ?: "[]"
        val grudgeJson = prefs.getString("grudgeBookEntries", "[]") ?: "[]"
        try {
            val type = object : TypeToken<List<BookEntry>>() {}.type
            generalBookEntries.clear()
            generalBookEntries.addAll(gson.fromJson(generalJson, type))
            
            grudgeBookEntries.clear()
            grudgeBookEntries.addAll(gson.fromJson(grudgeJson, type))
            
            // Migration von alten Einzeleinträgen, falls die neue Liste leer ist
            if (generalBookEntries.isEmpty()) {
                val oldGeneral = prefs.getString("generalNotes", "") ?: ""
                if (oldGeneral.isNotBlank()) {
                    addGeneralBookEntry(oldGeneral)
                    prefs.edit { remove("generalNotes") }
                }
            }
            if (grudgeBookEntries.isEmpty()) {
                val oldGrudge = prefs.getString("grudgeNotes", "") ?: ""
                if (oldGrudge.isNotBlank()) {
                    addGrudgeBookEntry(oldGrudge)
                    prefs.edit { remove("grudgeNotes") }
                }
            }
        } catch (e: Exception) {
            // Ignore parse errors on load
        }
    }

    fun addGeneralBookEntry(text: String) {
        if (text.isNotBlank()) {
            generalBookEntries.add(0, BookEntry(text = text.trim()))
            saveGeneralBookEntries()
        }
    }

    fun updateGeneralBookEntry(id: String, newText: String) {
        val index = generalBookEntries.indexOfFirst { it.id == id }
        if (index != -1 && newText.isNotBlank()) {
            generalBookEntries[index] = generalBookEntries[index].copy(text = newText.trim())
            saveGeneralBookEntries()
        }
    }

    fun addGrudgeBookEntry(text: String) {
        if (text.isNotBlank()) {
            grudgeBookEntries.add(0, BookEntry(text = text.trim()))
            saveGrudgeBookEntries()
        }
    }

    fun updateGrudgeBookEntry(id: String, newText: String) {
        val index = grudgeBookEntries.indexOfFirst { it.id == id }
        if (index != -1 && newText.isNotBlank()) {
            grudgeBookEntries[index] = grudgeBookEntries[index].copy(text = newText.trim())
            saveGrudgeBookEntries()
        }
    }

    var standardTactic by mutableStateOf(prefs.getString("standardTactic", "1. Zeichen des Jägers wirken (Bonusaktion)\n2. Mit Langbogen angreifen") ?: "")
        private set
    fun updateStandardTactic(text: String) {
        standardTactic = text
        prefs.edit { putString("standardTactic", standardTactic) }
    }

    // --- INIT ---
    init {
        loadLoot()
        loadTraits()
        loadBooks()
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

    var freeCureWoundsUsed by mutableStateOf(prefs.getBoolean("freeCureWoundsUsed", false))
        private set

    fun useFreeCureWounds() {
        if (!freeCureWoundsUsed) {
            freeCureWoundsUsed = true
            prefs.edit { putBoolean("freeCureWoundsUsed", true) }
        }
    }

    var freeHealingWordUsed by mutableStateOf(prefs.getBoolean("freeHealingWordUsed", false))
        private set

    fun useFreeHealingWord() {
        if (!freeHealingWordUsed) {
            freeHealingWordUsed = true
            prefs.edit { putBoolean("freeHealingWordUsed", true) }
        }
    }

    var freeFaerieFireUsed by mutableStateOf(prefs.getBoolean("freeFaerieFireUsed", false))
        private set

    fun useFreeFaerieFire() {
        if (!freeFaerieFireUsed) {
            freeFaerieFireUsed = true
            prefs.edit { putBoolean("freeFaerieFireUsed", true) }
        }
    }

    var freeDarknessUsed by mutableStateOf(prefs.getBoolean("freeDarknessUsed", false))
        private set

    fun useFreeDarkness() {
        if (!freeDarknessUsed) {
            freeDarknessUsed = true
            prefs.edit { putBoolean("freeDarknessUsed", true) }
        }
    }

    var freeDruidSpellUsed by mutableStateOf(prefs.getBoolean("freeDruidSpellUsed", false))
        private set

    fun useFreeDruidSpell() {
        if (!freeDruidSpellUsed) {
            freeDruidSpellUsed = true
            prefs.edit { putBoolean("freeDruidSpellUsed", true) }
        }
    }

    fun takeShortRest(hitDiceSpent: Int, rolledValue: Int) {
        if (hitDiceSpent <= hitDice && currentHp < maxHp) {
            hitDice -= hitDiceSpent
            val healAmount = rolledValue + (conMod * hitDiceSpent)
            currentHp = (currentHp + healAmount).coerceAtMost(maxHp)

            prefs.edit {
                putInt("hitDice", hitDice)
                putInt("currentHp", currentHp)
            }
        }
    }

    var showRestWarningDialog by mutableStateOf(false)
        private set

    fun dismissRestWarningDialog() {
        showRestWarningDialog = false
    }

    fun attemptLongRest() {
        if (water < 0.5f || rations < 1) {
            showRestWarningDialog = true
        } else {
            takeLongRest()
        }
    }

    fun forceLongRestWithoutResources() {
        showRestWarningDialog = false
        takeLongRest(consumeResources = false)
    }

    fun takeLongRest(consumeResources: Boolean = true) {
        currentHp = maxHp
        val recoveredHitDice = (level / 2).coerceAtLeast(1)
        hitDice = (hitDice + recoveredHitDice).coerceAtMost(level)

        spellSlotsLevel1 = 3
        
        // --- DYNAMISCHE SLOTS ABHÄNGIG VOM LEVEL ---
        if (level >= 9) {
            spellSlotsLevel2 = 2
            spellSlotsLevel3 = 2
        } else if (level >= 5) {
            spellSlotsLevel2 = 2
            spellSlotsLevel3 = 0
        } else {
            spellSlotsLevel2 = 0
            spellSlotsLevel3 = 0
        }

        huntersMarkFreeUses = 2
        freeCureWoundsUsed = false
        freeHealingWordUsed = false
        freeFaerieFireUsed = false
        freeDarknessUsed = false
        freeDruidSpellUsed = false
        goodberries = 0
        geminiUsesToday = 0
        
        if (consumeResources) {
            changeWater(-0.5f)
            changeRations(-1)
        }

        prefs.edit {
            putInt("currentHp", currentHp)
            putInt("hitDice", hitDice)
            putInt("spellSlotsLevel1", spellSlotsLevel1)
            putInt("spellSlotsLevel2", spellSlotsLevel2)
            putInt("spellSlotsLevel3", spellSlotsLevel3)
            putInt("huntersMarkFreeUses", huntersMarkFreeUses)
            putBoolean("freeCureWoundsUsed", freeCureWoundsUsed)
            putBoolean("freeHealingWordUsed", freeHealingWordUsed)
            putBoolean("freeFaerieFireUsed", freeFaerieFireUsed)
            putBoolean("freeDarknessUsed", freeDarknessUsed)
            putBoolean("freeDruidSpellUsed", freeDruidSpellUsed)
            putInt("goodberries", goodberries)
            putInt("geminiUsesToday", geminiUsesToday)
        }
    }

    var activeBeastType by mutableStateOf(BeastType.valueOf(prefs.getString("activeBeastType", BeastType.SKY.name) ?: BeastType.SKY.name))
        private set

    val capyMaxHp: Int get() = if (activeBeastType == BeastType.SKY || activeBeastType == BeastType.SEA) 4 + (4 * level) else 5 + (5 * level)
    var capyCurrentHp by mutableIntStateOf(prefs.getInt("capyCurrentHp", 20))
        private set

    fun toggleBeastType(type: BeastType) {
        activeBeastType = type
        if (capyCurrentHp > capyMaxHp) capyCurrentHp = capyMaxHp
        prefs.edit {
            putString("activeBeastType", activeBeastType.name)
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
    val capyDamage: String get() = if (activeBeastType == BeastType.SKY) "1W4 + $wisMod Hieb" else if(activeBeastType == BeastType.SEA) "1W6 + $wisMod Stich" else "1W8 + $wisMod Hieb"
    val capySpeed: String get() = if (activeBeastType == BeastType.SKY) "Fliegen 18 m, Laufen 3 m" else if(activeBeastType == BeastType.SEA) "Schwimmen 18 m, Laufen 1.5 m" else "Laufen 12 m, Klettern 12 m"
    val capySpecial: String get() = if (activeBeastType == BeastType.SKY) "Vorbeifliegen" else if(activeBeastType == BeastType.SEA) "Unter Wasser atmen, Amphibisch" else "Ansturm"

    // --- HILFE: CHAT & FAQ ---
    val chatHistory = mutableStateListOf<ChatMessage>()
    val faqList = mutableStateListOf<FaqItem>()

    var currentUsedModel by mutableStateOf("Bereit")
        private set
    var geminiUsesToday by mutableIntStateOf(prefs.getInt("geminiUsesToday", 0))
        private set
    val geminiMax = 20

     private val systemPrompt = """
        Du bist unser D&D 2024 Regel-Assistent. Dein Ziel ist es, Fragen basierend auf unseren Hausregeln (Handbuch/Zauberbuch) und dem Charakterblatt von Athania zu beantworten.
        
        FORMATRICHTLINIE (SEHR WICHTIG):
        Antworte AUSSCHLIESSLICH im JSON-Format. Verwende exakt diese Schlüsselstruktur und erzeuge keinen Text außerhalb der JSON-Klammern:
        {
          "lokale_antwort": "Deine Antwort NUR basierend auf den bereitgestellten Handbüchern/Stats. Wenn nichts gefunden, schreibe 'Keine spezifischen Informationen gefunden.'",
          "externe_antwort": "Deine Antwort basierend auf deinem allgemeinen Wissen über D&D 2024. Gehe auf die Klasse und das Volk des Charakters ein, falls relevant.",
          "kapitel_link": "NUR der exakte Name eines HANDBUCH-Kapitels aus den '--- Quelle: ... ---' Markierungen (z.B. '3. Klassen', '7. Kampf'). Erlaubte Werte: '1. Gameplay', '2. Völker', '3. Klassen', '4. Herkünfte', '5. Talente', '6. Ausrüstung', '7. Kampf', '8. Zauber', 'Zauberbuch Übersicht'. WICHTIG: Wenn die Antwort aus dem CHARAKTERBLATT kommt (Stats, Begleiter/Capys, Inventar, Zauberplätze, Notizbuch etc.) und NICHT aus einem Handbuch-Kapitel, setze den Wert auf null!",
          "suchbegriff": "Ein kurzes Stichwort (1-2 Worte) aus dem Kapitel, das exakt zu deiner Antwort passt, um im UI genau zu dieser Regel zu scrollen (z.B. 'Zaubertricks' oder 'Kampfstile'). Nur setzen wenn kapitel_link gesetzt ist."
        }
    """.trimIndent()

private val model3Flash = GenerativeModel(
        modelName = "gemini-3.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        }
    )

    private val model25Flash = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        }
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
    val globalSpellbook = mutableStateListOf<Spell>()

    init {
        loadLoot()
        loadFaqs()
        loadSpells()
        loadGlobalSpellbook()
        loadEquipment()
    }

    private fun saveSpells() {
        val json = gson.toJson(allSpells)
        prefs.edit { putString("savedSpells", json) }
    }

    private fun loadGlobalSpellbook() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val spellList = mutableListOf<Spell>()
                val type = object : TypeToken<List<SpellDto>>() {}.type

                for (i in 0..9) {
                    val fileName = "Rules/Zauberbuch/zauber_stufe$i.json"
                    try {
                        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
                        val dtos: List<SpellDto> = gson.fromJson(jsonString, type)
                        spellList.addAll(dtos.map { it.toSpell() })
                    } catch (e: Exception) {
                        // ignore missing files
                    }
                }
                globalSpellbook.clear()
                globalSpellbook.addAll(spellList)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    val allEquipment = mutableStateListOf<EquipmentDefinition>()

    private fun loadEquipment() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val text = context.assets.open("Rules/Handbuch/Kapitel/kapitel6_equipment.md").bufferedReader().use { it.readText() }
                
                val equipmentList = mutableListOf<EquipmentDefinition>()
                var currentCategory = "Sonstiges"
                
                val lines = text.lines()
                for (line in lines) {
                    val trimmed = line.trim()
                    
                    // Kategorienerkennung
                    if (trimmed.startsWith("## 2.") || trimmed.startsWith("## 3.")) {
                        currentCategory = "Rüstung & Waffen"
                    } else if (trimmed.startsWith("## 4.")) {
                        currentCategory = "Werkzeug"
                    } else if (trimmed.startsWith("## 5.")) {
                        currentCategory = "Ausrüstung"
                    } else if (trimmed.startsWith("## 6.")) {
                        currentCategory = "Reittiere & Fahrzeuge"
                    } else if (trimmed.startsWith("## 7.")) {
                        currentCategory = "Dienstleistungen"
                    }

                    // Tabellenzeilen parsen (ignorieren von Titel- und Trennzeilen)
                    if (trimmed.startsWith("|") && !trimmed.startsWith("| :---") && !trimmed.contains("Waffe (Name)") && !trimmed.contains("Rüstungstyp") && !trimmed.contains("Werkzeug |") && !trimmed.contains("Gegenstand |") && !trimmed.contains("Tier (Animal)") && !trimmed.contains("Schiffstyp") && !trimmed.contains("Qualität |")) {
                        
                        // Überschrift innerhalb einer Tabelle (z.B. "**Leichte Rüstung**") überspringen
                        if (trimmed.startsWith("| **") && trimmed.indexOf("|", startIndex = 2) < 0) continue

                        val parts = trimmed.split("|").map { it.trim() }
                        // Die Split-Methode erzeugt ein leeres Element am Anfang und Ende, wenn die Zeile mit | beginnt/endet
                        if (parts.size >= 4) { 
                            val rawName = parts[1].replace("**", "") // Entferne Fettmarkierungen
                            var name = rawName
                            
                            // Bereinige den Namen (falls englischer Name in Klammern steht, nimm den deutschen Teil)
                            val bracketIndex = rawName.indexOf("(")
                            if (bracketIndex > 0) {
                                name = rawName.substring(0, bracketIndex).trim()
                            }
                            
                            if (name.isNotEmpty() && !name.startsWith("**")) {
                                // Gewicht extrahieren. Es ist typischerweise in der vorletzten Spalte (bei Waffen/Rüstungen/Ausrüstung)
                                // Wir suchen in allen Spalten nach "Pfd." oder "Tonnen"
                                var weight = 0.0
                                for (part in parts) {
                                    if (part.contains("Pfd.")) {
                                        val weightStr = part.replace(" Pfd.", "").replace(",", ".")
                                        weight = weightStr.toDoubleOrNull() ?: 0.0
                                        break
                                    } else if (part.contains("Tonnen") || part.contains("Tonne")) {
                                        val weightStr = part.replace(" Tonnen", "").replace(" Tonne", "").replace(",", ".")
                                        weight = (weightStr.toDoubleOrNull() ?: 0.0) * 2000.0 // 1 Tonne = 2000 Pfd
                                        break
                                    }
                                }
                                
                                equipmentList.add(EquipmentDefinition(name, weight, currentCategory))
                            }
                        }
                    }
                }
                
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    allEquipment.clear()
                    allEquipment.addAll(equipmentList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadSpells() {
        val spellString = prefs.getString("savedSpells", "") ?: ""
        if (spellString.isNotEmpty()) {
            try {
                val type = object : TypeToken<List<Spell>>() {}.type
                val items: List<Spell> = gson.fromJson(spellString, type)
                allSpells.clear()
                @Suppress("SENSELESS_COMPARISON")
                val safeItems = items.map { spell ->
                    if (spell.classes == null) spell.copy(classes = emptyList()) else spell
                }
                allSpells.addAll(safeItems)

                // Migration: Fehlende Standardzauber (wie "Heilendes Wort" im Update) nachträglich einfügen
                val defaultSpells = getAthaniaDefaultSpells()
                var addedNew = false
                defaultSpells.forEach { defaultSpell ->
                    if (allSpells.none { it.name == defaultSpell.name }) {
                        allSpells.add(defaultSpell)
                        addedNew = true
                    }
                }
                if (addedNew) saveSpells()

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
            ),
            Spell(
                name = "Heilendes Wort",
                level = 1,
                castingTime = "1 Bonusaktion",
                range = "18 m",
                duration = "Sofort",
                componentsV = true, componentsS = false, componentsM = false,
                description = "Eine Kreatur deiner Wahl in Reichweite erhält Trefferpunkte in Höhe von 2d4 + deinem Zauber-Attributsmodifikator zurück. (PHB 2024).",
                isPrepared = true
            )
        )
    }

    private fun getCharacterContext(): String {
        val stModStr = if (strMod >= 0) "+$strMod" else "$strMod"
        val geModStr = if (dexMod >= 0) "+$dexMod" else "$dexMod"
        val koModStr = if (conMod >= 0) "+$conMod" else "$conMod"
        val inModStr = if (intMod >= 0) "+$intMod" else "$intMod"
        val weModStr = if (wisMod >= 0) "+$wisMod" else "$wisMod"
        val chModStr = if (chaMod >= 0) "+$chaMod" else "$chaMod"

        val preparedSpells = allSpells.filter { it.isPrepared }.joinToString(", ") { "${it.name} (Lvl ${it.level})" }
        val allKnownSpells = allSpells.joinToString(", ") { "${it.name} (Lvl ${it.level})" }
        val inventoryStr = customLoot.joinToString(", ") { "${it.amount}x ${it.name}" }
        val notes = generalBookEntries.joinToString(" | ") { it.text }
        val grudges = grudgeBookEntries.joinToString(" | ") { it.text }
        val traitsStr = customTraits.joinToString(" | ") { "${it.name}: ${it.desc.replace("\n", " ")}" }

        val landHp = 5 + 5 * level
        val skyHp = 4 + 4 * level
        val seaHp = 4 + 4 * level
        val beastAc = 13 + proficiencyBonus

        return """
            KONTEXT CHARAKTERBLATT ATHANIA:
            Klasse: Waldläufer (Beast Master)
            Volk: Elf (Waldelf / Feenblut)
            Level: $level, EP: $currentEP
            HP: $currentHp/$maxHp, Trefferwürfel: $hitDice/$level
            Werte: ST $strength ($stModStr), GE $dexterity ($geModStr), KO $constitution ($koModStr), IN $intelligence ($inModStr), WE $wisdom ($weModStr), CH $charisma ($chModStr)
            Rüssi-Klasse: $currentArmorClass, Initiative: $geModStr
            Waffe: ${currentWeapon.name} (Bonus: +$currentAttackBonus, Schaden: $currentDamage)
            Zauberplätze: G1: $spellSlotsLevel1, G2: $spellSlotsLevel2, G3: $spellSlotsLevel3
            Vorbereitete Zauber: $preparedSpells
            Alle bekannten Zauber: $allKnownSpells
            Merkmale/Fähigkeiten: $traitsStr
            Vorrätig: $water L Wasser, $rations Rationen, $goodberries Beeren, $totalArrows Pfeile
            Geld: $coinsGM GM, $coinsSM SM
            Inventar: $inventoryStr
            Notizbuch: $notes
            Buch des Grolls: $grudges
            
            --- BEGLEITER (alle 3 Urtier-Formen) ---
            Aktuell aktiv: ${activeBeastType.name} (HP: $capyCurrentHp/$capyMaxHp)
            Angriffsbonus (alle): +$spellAttackBonus, Rettungswürfe (alle): +$proficiencyBonus
            
            Urtier des Landes: HP: $landHp/$landHp, AC: $beastAc
            Schaden: 1W8 + $wisMod Hieb, Geschwindigkeit: Laufen 12m, Klettern 12m
            Spezial: Ansturm
            
            Urtier des Himmels: HP: $skyHp/$skyHp, AC: $beastAc
            Schaden: 1W4 + $wisMod Hieb, Geschwindigkeit: Fliegen 18m, Laufen 3m
            Spezial: Vorbeifliegen
            
            Urtier des Meeres: HP: $seaHp/$seaHp, AC: $beastAc
            Schaden: 1W6 + $wisMod Stich, Geschwindigkeit: Schwimmen 18m, Laufen 1.5m
            Spezial: Unter Wasser atmen, Amphibisch
        """.trimIndent()
    }

    private suspend fun getManualContext(query: String): String {
        return try {
            val context = getApplication<Application>()
            val assets = context.assets
            val sb = StringBuilder()

            // 1. Satzzeichen entfernen, alles in Kleinbuchstaben
            val cleanQuery = query.lowercase().replace(Regex("[^a-zäöüß0-9 ]"), "")
            
            // 2. Füllwörter ignorieren, auch Wortstämme (vereinfacht) berücksichtigen
            val stopWords = setOf("was", "wie", "ist", "ein", "eine", "der", "die", "das", "und", "oder", "kann", "ich", "mich", "mir", "für", "von", "aus", "mit", "sind", "macht", "darf", "wenn", "dann", "wir", "ihr", "sie", "als", "auf", "bei", "bis", "gibt", "es", "mein", "meine", "welche", "welcher", "habe", "tun", "soll", "werden", "können", "muss")
            // Zuweisung auch von Teilbegriffen (z.b. "Zaubertricks" -> "Tricks" oder "Zauber", aber wir suchen einfach als Substring)
            val keywords = cleanQuery.split(" ").filter { it.length > 3 && !stopWords.contains(it) }.toMutableList()
            
            // Spezifische D&D Begriffs-Helfer: Wenn "zaubertricks" gefragt wird, erweitere das auf "tricks" und "zaubertrick", da es in der markdown evtl "Tricks" heißt.
            if ("zaubertricks" in keywords) keywords.add("tricks")
            if ("zaubertrick" in keywords) keywords.add("trick")
            if ("langbogen" in keywords) keywords.add("bogen")

            if (keywords.isEmpty()) return "Keine spezifischen Handbuch-Einträge gefunden."

            // 3. Durchsuche das Handbuch nach den besten Absätzen
            val chapters = assets.list("Rules/Handbuch/Kapitel") ?: emptyArray()
            val spells = assets.list("Rules/Zauberbuch") ?: emptyArray()
            
            val chapterMapping = mapOf(
                "kapitel1_gameplay.md" to "1. Gameplay",
                "kapitel2_races.md" to "2. Völker",
                "kapitel3_classes.md" to "3. Klassen",
                "kapitel4_origins.md" to "4. Herkünfte",
                "kapitel5_talente.md" to "5. Talente",
                "kapitel6_equipment.md" to "6. Ausrüstung",
                "kapitel8_combat_conditions.md" to "7. Kampf",
                "kapitel7_spells.md" to "8. Zauber"
            )
            
            // Speichert die Absätze zusammen mit ihrer "Relevanz-Punktzahl"
            val bestParagraphs = mutableListOf<Pair<Int, String>>() 
            
            // Verarbeite Handbuch-Kapitel
            for (fileName in chapters) {
                if (!fileName.endsWith(".md")) continue
                val text = assets.open("Rules/Handbuch/Kapitel/$fileName").bufferedReader().use { it.readText() }
                
                // Macht den Dateinamen als Überschrift passend zu den UI-Tabs
                val prettyName = chapterMapping[fileName] ?: fileName.replace(".md", "").replace("_", " ").uppercase()
                
                // Zerlegt das Kapitel in Absätze (getrennt durch doppelte Zeilenumbrüche)
                val paragraphs = text.split("\n\n", "\r\n\r\n")
                
                for (paragraph in paragraphs) {
                    val pLower = paragraph.lowercase()
                    // Zähle, wie oft die Suchwörter vorkommen
                    var score = 0
                    for (kw in keywords) {
                        if (pLower.contains(kw)) {
                            score += 1
                        }
                    }
                    if (score > 0) {
                        // Füge den Absatz plus Quellenangabe hinzu
                        bestParagraphs.add(Pair(score, "--- Quelle: $prettyName ---\n$paragraph"))
                    }
                }
            }

            // Verarbeite Zauberbuch (Sowohl Spellbook.md als auch JSONs)
            for (fileName in spells) {
                if (fileName.endsWith(".md")) {
                    val text = assets.open("Rules/Zauberbuch/$fileName").bufferedReader().use { it.readText() }
                    val paragraphs = text.split("\n\n", "\r\n\r\n")
                    for (paragraph in paragraphs) {
                        val pLower = paragraph.lowercase()
                        var score = 0
                        for (kw in keywords) {
                            if (pLower.contains(kw)) score += 1
                        }
                        if (score > 0) {
                            bestParagraphs.add(Pair(score, "--- Quelle: Zauberbuch Übersicht ---\n$paragraph"))
                        }
                    }
                } else if (fileName.endsWith(".json")) {
                    try {
                        val text = assets.open("Rules/Zauberbuch/$fileName").bufferedReader().use { it.readText() }
                        val jsonArray = org.json.JSONArray(text)
                        for (i in 0 until jsonArray.length()) {
                            val spell = jsonArray.getJSONObject(i)
                            val name = spell.optString("name_de", "")
                            val desc = spell.optString("description", "")
                            val classes = spell.optJSONArray("classes")?.let { arr -> 
                                (0 until arr.length()).map { arr.getString(it) }.joinToString()
                            } ?: ""
                            
                            val spellText = "Zauber: $name\nKlassen: $classes\nBeschreibung: $desc"
                            val pLower = spellText.lowercase()
                            var score = 0
                            for (kw in keywords) {
                                if (pLower.contains(kw)) score += 1
                            }
                            if (score > 0) {
                                bestParagraphs.add(Pair(score, "--- Quelle: Zauberbuch Detail ---\n$spellText"))
                            }
                        }
                    } catch (e: Exception) {
                        // ignoriere leere oder ungültige JSON
                    }
                }
            }
            
            // 4. Sortiere nach den meisten Treffern und nimm die besten 10 Absätze für mehr Kontext
            bestParagraphs.sortByDescending { it.first }
            bestParagraphs.take(10).forEach { 
                sb.append(it.second).append("\n")
            }
            
            // 5. Durchsuche zusätzlich das Zauberbuch
            val searchSpell = globalSpellbook.find { spell -> 
                keywords.any { kw -> spell.name.lowercase().contains(kw) }
            }
            if (searchSpell != null) {
                sb.append("\n--- Quelle: ZAUBERBUCH ---\n")
                sb.append("Zauber: ${searchSpell.name} (Grad ${searchSpell.level})\nBeschreibung: ${searchSpell.description}\n")
            }
            
            // Fallback, falls absolut gar kein Wort aus der Frage im Buch steht
            if (sb.isEmpty()) "Keine spezifischen Handbuch-Einträge gefunden für: ${keywords.joinToString(", ")}" else sb.toString()
            
        } catch (e: Exception) {
            "Fehler beim Laden lokaler Regeln."
        }
    }

    fun sendMessageToBot(message: String) {
        // Fix: Parameter explizit benennen (text = ..., isUser = ...)
        chatHistory.add(ChatMessage(text = message, isUser = true))
        val loadingIndex = chatHistory.size
        chatHistory.add(ChatMessage(text = "... analysiere Regeln ...", isUser = false))

        viewModelScope.launch {
            try {
                if (geminiUsesToday < geminiMax) {
                    val charContext = getCharacterContext()
                    val manualContext = getManualContext(message)
                    val finalPrompt = "$systemPrompt\n\n$charContext\n\nHANDBUCH-AUSZÜGE:\n$manualContext\n\nFRAGE: $message"

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
                    throw Exception("Tageslimit für Gemini erreicht ($geminiMax)")
                }
            } catch (e: Exception) {
                val errorMsg = if (e.localizedMessage?.contains("MissingFieldException") == true) {
                    "Das Modell konnte nicht gefunden werden (API-Key/Quota)."
                } else {
                    e.localizedMessage ?: "Unbekannter Fehler"
                }
                
                // Wir zwingen auch den Fehler in die Split-Ansicht, damit du ihn sehen kannst!
                chatHistory[loadingIndex] = chatHistory[loadingIndex].copy(
                    text = "System",
                    localText = "SYSTEMFEHLER",
                    externalText = "Die Anfrage konnte nicht verarbeitet werden.\nGrund: $errorMsg",
                    chapterLink = null
                )
            }
        }
    }

    private fun finalizeResponse(index: Int, text: String?) {
        geminiUsesToday++
        prefs.edit { putInt("geminiUsesToday", geminiUsesToday) }
        
        val rawText = text ?: "{}"
        
        var parsedLocal: String? = "Keine spezifischen Handbuch-Einträge gefunden."
        var parsedExternal: String? = "Keine allgemeinen Informationen von Gemini."
        var parsedLink: String? = null
        var parsedSearchTerm: String? = null

        try {
            // Sicherer Regex, der alle Varianten von Markdown-JSON-Blöcken entfernt
            val cleanJson = rawText.replace(Regex("```json\n?", RegexOption.IGNORE_CASE), "")
                                   .replace(Regex("```\n?", RegexOption.IGNORE_CASE), "")
                                   .trim()
            
            val json = JSONObject(cleanJson)

            if (json.has("lokale_antwort") && !json.isNull("lokale_antwort")) {
                val l = json.getString("lokale_antwort")
                if (l.isNotBlank()) parsedLocal = l
            }
            if (json.has("externe_antwort") && !json.isNull("externe_antwort")) {
                val e = json.getString("externe_antwort")
                if (e.isNotBlank()) parsedExternal = e
            }
            // Validierung: kapitel_link nur akzeptieren, wenn ein valides Keyword drinsteckt
            // Damit kleine Abweichungen (z.B. "1. gameplay" vs "Gameplay") toleriert werden.
            val validChapterKeywords = listOf(
                "gameplay", "völker", "klassen", "herkünfte", "talente",
                "ausrüstung", "kampf", "zauber", "zauberbuch"
            )
            if (json.has("kapitel_link") && !json.isNull("kapitel_link")) {
                val k = json.getString("kapitel_link")
                if (k.isNotBlank() && k != "null" && validChapterKeywords.any { k.contains(it, ignoreCase = true) }) {
                    parsedLink = k
                }
            }
            if (json.has("suchbegriff") && !json.isNull("suchbegriff") && parsedLink != null) {
                val s = json.getString("suchbegriff")
                if (s.isNotBlank() && s != "null") parsedSearchTerm = s
            }

        } catch (e: Exception) {
            parsedLocal = "Fehler beim Auswerten der Daten."
            parsedExternal = "Konnte das JSON nicht parsen.\nRohtext von Gemini:\n$rawText"
        }

        chatHistory[index] = chatHistory[index].copy(
            text = "System",
            localText = parsedLocal,
            externalText = parsedExternal,
            chapterLink = parsedLink,
            chapterSearchTerm = parsedSearchTerm
        )
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

    fun updateFaq(oldItem: FaqItem, newQuestion: String, newAnswer: String) {
        val index = faqList.indexOf(oldItem)
        if (index != -1) {
            faqList[index] = FaqItem(newQuestion, newAnswer)
            saveFaqs()
        }
    }
}