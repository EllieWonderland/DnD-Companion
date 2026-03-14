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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import com.example.dndcompanion.data.CharacterData
import com.example.dndcompanion.data.CharacterRepository
import com.example.dndcompanion.data.CharacterClass

// --- DATENKLASSEN & ENUMS ---
data class InventoryItem(
    val name: String, 
    val amount: Int, 
    val weight: Double = 0.0, 
    val category: String = "Sonstiges",
    var maxCharges: Int = 0,
    var currentCharges: Int = 0,
    val spellCharges: Map<String, Int>? = null // Map spellId -> cost
)
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String, 
    val isUser: Boolean,
    val localText: String? = null,
    val externalText: String? = null,
    val chapterLink: String? = null,
    val chapterSearchTerm: String? = null, // NEU: Für zielgenaues Scrollen
    val faqTitle: String? = null // NEU: Dynamischer Titel für FAQ-Aufnahme
)

data class FaqItem(val question: String, val answer: String)
data class TraitItem(
    val name: String, 
    val desc: String,
    var maxUses: Int = 0,
    var currentUses: Int = 0,
    val grantedSpellId: String? = null,
    val resetOnShortRest: Boolean = false
)
data class BookEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isPublic: Boolean = false,
    val author: String = "Athania"
)

data class GroupChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val author: String = "Athania",
    val charClass: CharacterClass = CharacterClass.RANGER,
    val isOoc: Boolean = false
)

data class Quest(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = java.util.UUID.randomUUID().toString(),
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("description") @set:PropertyName("description") var description: String = "",
    @get:PropertyName("isCompleted") @set:PropertyName("isCompleted") var isCompleted: Boolean = false,
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Long = System.currentTimeMillis()
)

data class GroupLootItem(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = java.util.UUID.randomUUID().toString(),
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("amount") @set:PropertyName("amount") var amount: Int = 0,
    @get:PropertyName("weight") @set:PropertyName("weight") var weight: Double = 0.0,
    @get:PropertyName("category") @set:PropertyName("category") var category: String = "Sonstiges"
)

data class SharedCoins(
    @get:PropertyName("km") @set:PropertyName("km") var km: Int = 0,
    @get:PropertyName("sm") @set:PropertyName("sm") var sm: Int = 0,
    @get:PropertyName("em") @set:PropertyName("em") var em: Int = 0,
    @get:PropertyName("gm") @set:PropertyName("gm") var gm: Int = 0,
    @get:PropertyName("pm") @set:PropertyName("pm") var pm: Int = 0
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
    val school: String = "Unbekannt",
    var isPrepared: Boolean = false,
    var isRitual: Boolean = false
)

enum class ActiveWeapon {
    LANGBOGEN,
    KURZSCHWERT_SCHILD,
    SHILLELAGH_SCHILD,
    KRIEGSHAMMER_PAKT,
    SPEER_PAKT
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

data class CompanionDto(
    val name: String,
    val typ_und_gesinnung: String,
    val ruestungsklasse: String,
    val trefferpunkte: String,
    val bewegungsrate: Map<String, String>,
    val attribute: Map<String, AttributeDto>,
    val sinne: String,
    val sprachen: String,
    val herausforderungsgrad: String,
    val merkmale: List<CompanionTraitDto>,
    val aktionen: List<CompanionActionDto>,
    val reaktionen: List<CompanionActionDto>? = null
)

data class AttributeDto(
    val wert: Int,
    val modifikator: Int,
    val rettungswurf: Int
)

data class CompanionTraitDto(
    val name: String,
    val beschreibung: String
)

data class CompanionActionDto(
    val name: String,
    val typ: String? = null,
    val beschreibung: String? = null,
    val ausloeser: String? = null,
    val antwort: String? = null
)

data class UrtierFileDto(
    val urtiere: List<CompanionDto>
)

class CharacterViewModel(application: Application) : AndroidViewModel(application) {

    var activeCharacterId by mutableStateOf("Athania")
        private set

    var characterData by mutableStateOf(CharacterRepository.getCharacter("Athania"))
        private set

    private var prefs = application.getSharedPreferences("AthaniaSaveGame", Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        loadGlobalSpellbook()
        loadGlobalFeatures()
        loadEquipment()
        listenToQuests()
        listenToSharedLoot()
        listenToPublicNotes()
    }

    // --- BASISWERTE ---
    // EP Table D&D 5e:
    private val epThresholds = listOf(
        0, 300, 900, 2700, 6500, 14000, 23000, 34000, 48000, 64000,
        85000, 100000, 120000, 140000, 165000, 195000, 225000, 265000, 305000, 355000
    )

    var currentEP by mutableIntStateOf(prefs.getInt("currentEP_${characterData.id}", characterData.baseEP))
    var heroicInspiration by mutableStateOf(prefs.getBoolean("heroicInspiration_${activeCharacterId}", false))
        private set

    var level by mutableIntStateOf(prefs.getInt("level", characterData.baseLevel))
        private set

    var strength by mutableIntStateOf(prefs.getInt("strength", characterData.baseStrength))
        private set
    var dexterity by mutableIntStateOf(prefs.getInt("dexterity", characterData.baseDexterity))
        private set
    var constitution by mutableIntStateOf(prefs.getInt("constitution", characterData.baseConstitution))
        private set
    var intelligence by mutableIntStateOf(prefs.getInt("intelligence", characterData.baseIntelligence))
        private set
    var wisdom by mutableIntStateOf(prefs.getInt("wisdom", characterData.baseWisdom))
        private set
    var charisma by mutableIntStateOf(prefs.getInt("charisma", characterData.baseCharisma))
        private set

    var showLevelUpDialog by mutableStateOf(false)
        private set

    var showLevelUpNotification by mutableStateOf(false)
        private set
    var snackbarMessage = mutableStateOf<String?>(null)
    
    var isUsingTwoHanded by mutableStateOf(prefs.getBoolean("isUsingTwoHanded", false))
        private set

    fun toggleTwoHanded(active: Boolean) {
        isUsingTwoHanded = active
        prefs.edit { putBoolean("isUsingTwoHanded", isUsingTwoHanded) }
    }

    // Mage Armor active effect is separate from "daily use" to allow toggling
    var isMageArmorActive by mutableStateOf(prefs.getBoolean("isMageArmorActive", false))
        private set

    fun toggleMageArmor(active: Boolean) {
        isMageArmorActive = active
        prefs.edit { putBoolean("isMageArmorActive", isMageArmorActive) }
    }

    var isShieldEquipped by mutableStateOf(prefs.getBoolean("isShieldEquipped", false))
        private set

    fun toggleShield(active: Boolean) {
        isShieldEquipped = active
        prefs.edit { putBoolean("isShieldEquipped", isShieldEquipped) }
    }

    val hasShieldInInventory: Boolean
        get() = customLoot.any { it.name.contains("Schild", ignoreCase = true) }

    val availableWeapons: List<String>
        get() {
            val list = mutableListOf<String>()
            customLoot.forEach { item ->
                if (item.category == "Rüstung & Waffen" && !item.name.contains("Rüstung", ignoreCase = true) && !item.name.contains("Schild", ignoreCase = true)) {
                    list.add(item.name)
                }
            }
            // Standardwaffen hinzufügen, falls sie im Inventar sind (Migration/Fallback)
            return list.distinct()
        }

    var equippedWeaponName by mutableStateOf(prefs.getString("equippedWeaponName", null) ?: "Keine Waffe")
        private set

    fun equipWeaponByName(name: String) {
        equippedWeaponName = name
        prefs.edit { putString("equippedWeaponName", name) }
        
        // Sync with enum for logic
        val weaponStr = name.lowercase()
        currentWeapon = when {
            weaponStr.contains("bogen") -> ActiveWeapon.LANGBOGEN
            weaponStr.contains("schwert") -> ActiveWeapon.KURZSCHWERT_SCHILD
            weaponStr.contains("hammer") -> ActiveWeapon.KRIEGSHAMMER_PAKT
            weaponStr.contains("speer") -> ActiveWeapon.SPEER_PAKT
            weaponStr.contains("shillelagh") -> ActiveWeapon.SHILLELAGH_SCHILD
            else -> ActiveWeapon.KURZSCHWERT_SCHILD // Default fallback
        }
    }
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
            
            // --- AUTOMATISIERUNGEN FÜR KLASSEN ---
            for (lvl in (oldLevel + 1)..newLevel) {
                // Zauberplätze automatisch aktualisieren
                spellSlotsLevel1 = getMaxSpellSlots(lvl, 1)
                spellSlotsLevel2 = getMaxSpellSlots(lvl, 2)
                spellSlotsLevel3 = getMaxSpellSlots(lvl, 3)
                spellSlotsLevel4 = getMaxSpellSlots(lvl, 4)
                spellSlotsLevel5 = getMaxSpellSlots(lvl, 5)
                
                prefs.edit { 
                    putInt("spellSlotsLevel1", spellSlotsLevel1)
                    putInt("spellSlotsLevel2", spellSlotsLevel2)
                    putInt("spellSlotsLevel3", spellSlotsLevel3)
                    putInt("spellSlotsLevel4", spellSlotsLevel4)
                    putInt("spellSlotsLevel5", spellSlotsLevel5)
                }

                if (characterData.charClass == CharacterClass.RANGER) {
                    when (lvl) {
                        5 -> {
                            addCustomTrait("Zusätzlicher Angriff (Level 5)", "Du kannst zweimal angreifen, wenn du die Angriffsaktion ausführst.")
                        }
                        6 -> {
                            addCustomTrait("Umherziehen / Roving (Level 6)", "Deine Bewegungsrate erhöht sich um 3m, wenn du keine schwere Rüstung trägst. Du erhältst eine Kletter- und Schwimmrate in Höhe deiner Gehgeschwindigkeit.")
                        }
                        7 -> {
                            addCustomTrait("Außergewöhnliches Training (Level 7)", "Die Bestie kann Spurt, Rückzug, Ausweichen oder Hilfe als Bonusaktion nutzen. Ihre Angriffe können nun Wuchtschaden oder Energieschaden (Force) verursachen.")
                        }
                        9 -> {
                            addCustomTrait("Expertise 2 (Level 9)", "Wähle zwei weitere Fertigkeiten für Expertise aus dem Handbuch.")
                        }
                        10 -> {
                            addCustomTrait("Unermüdlich / Tireless (Level 10)", "Temporäre Trefferpunkte: Als Magie-Aktion erhältst du 1W8 + WIS-Mod TP (Nutzungen = WIS-Mod pro Tag). Erschöpfung: Eine Kurze Rast verringert deine Erschöpfung um 1 Stufe.")
                        }
                    }
                } else if (characterData.charClass == CharacterClass.WARLOCK) {
                    when (lvl) {
                        5 -> {
                            addCustomTrait("Schauerliche Anrufung (Level 5)", "Du erlernst eine neue Anrufung. Empfehlung für Nahkämpfer: 'Dürstende Klinge' (Extra-Angriff) oder 'Schauerliches Niederstrecken'.")
                        }
                        6 -> {
                            addCustomTrait("Hellseherischer Kämpfer / Clairvoyant Combatant (Level 6)", "Du kannst eine mental verbundene Kreatur (Erwachter Geist) zu einem WIS-Rettungswurf zwingen. Fehlschlag: Sie hat Nachteil auf Angriffe gegen dich, du hast Vorteil auf Angriffe gegen sie. (1x pro Rast oder Zauberplatz).")
                        }
                        7 -> {
                            addCustomTrait("Schauerliche Anrufung (Level 7)", "Du erlernst eine neue Anrufung (insgesamt 4).")
                        }
                        8 -> {
                            addCustomTrait("Attributswertverbesserung (Level 8)", "Wähle ein neues Talent oder erhöhe Attribute (z.B. Charisma auf 20).")
                        }
                        9 -> {
                            addCustomTrait("Schutzherrn kontaktieren (Level 9)", "Du hast 'Kontakt zu anderen Ebenen' vorbereitet. Du kannst ihn 1x pro Langer Rast kostenlos wirken und bestehst den Rettungswurf automatisch.")
                        }
                        10 -> {
                            addCustomTrait("Schauerliches Verwünschen (Level 10)", "Der Zauber 'Verwünschen (Hex)' ist immer vorbereitet. Er verursacht zusätzlich Nachteil auf Rettungswürfe des gewählten Attributs.")
                            addCustomTrait("Gedankenschild (Level 10)", "Deine Gedanken können nicht gelesen werden. Resistenz gegen Psychischen Schaden. Wer dir Psychischen Schaden zufügt, erleidet dieselbe Menge an Schaden.")
                        }
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
        var hpIncrease = rolledHp + conModifier
        if (characterData.id == "Delat") {
            hpIncrease += 1
        }
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

    private fun saveBaseStats() {
        prefs.edit {
            putInt("strength", strength)
            putInt("dexterity", dexterity)
            putInt("constitution", constitution)
            putInt("intelligence", intelligence)
            putInt("wisdom", wisdom)
            putInt("charisma", charisma)
            putInt("maxHp", maxHp)
            putInt("level", level)
        }
    }

    fun getMaxSpellSlots(lvl: Int, slotLvl: Int): Int {
        return if (characterData.charClass == CharacterClass.RANGER) {
            when (slotLvl) {
                1 -> if (lvl >= 1) { if (lvl >= 5) 4 else if (lvl >= 3) 3 else 2 } else 0
                2 -> if (lvl >= 5) { if (lvl >= 7) 3 else 2 } else 0
                3 -> if (lvl >= 9) { if (lvl >= 11) 3 else 2 } else 0
                4 -> if (lvl >= 13) { if (lvl >= 15) 3 else 2 } else 0
                5 -> if (lvl >= 17) 2 else 0
                else -> 0
            }
        } else if (characterData.charClass == CharacterClass.WARLOCK) {
            val pactSlots = if (lvl >= 11) 3 else if (lvl >= 2) 2 else 1
            val pactLevel = if (lvl >= 9) 5 else if (lvl >= 7) 4 else if (lvl >= 5) 3 else if (lvl >= 3) 2 else 1
            if (slotLvl == pactLevel) pactSlots else 0
        } else 0
    }

    val proficiencyBonus: Int get() = when(level) {
        in 1..4 -> 2
        in 5..8 -> 3
        in 9..12 -> 4
        in 13..16 -> 5
        else -> 6
    }

    val strMod: Int get() = (strength - 10) / 2
    val dexMod: Int get() {
        val baseMod = (dexterity - 10) / 2
        return if (isMageArmorActive) baseMod + 1 else baseMod
    }
    val conMod: Int get() = (constitution - 10) / 2
    val intMod: Int get() = (intelligence - 10) / 2
    val wisMod: Int get() = (wisdom - 10) / 2
    val chaMod: Int get() = (charisma - 10) / 2

    val speed: Int get() = characterData.speed
    val initiative: Int get() = dexMod
    val passivePerception: Int get() = 10 + wisMod + if (characterData.proficientSkills.contains("Wahrnehmung")) proficiencyBonus else 0

    // Neue Metadata-Properties für die UI
    val characterRace: String get() = characterData.race
    val characterBackground: String get() = characterData.background
    val characterAlignment: String get() = characterData.alignment

    fun getSkillModifier(skillName: String): Int {
        val baseMod = when (skillName) {
            "Athletik" -> strMod
            "Akrobatik", "Fingerfertigkeit", "Heimlichkeit" -> dexMod
            "Arkane Kunde", "Geschichte", "Nachforschung", "Naturkunde", "Religionskunde" -> intMod
            "Mit Tieren umgehen", "Motiv erkennen", "Heilkunde", "Wahrnehmung", "Überlebenskunst" -> wisMod
            "Täuschen", "Einschüchtern", "Auftreten", "Überzeugen" -> chaMod
            else -> 0
        }
        
        var totalBonus = baseMod
        
        // Proficiencies
        if (characterData.id == "Athania") {
            if (skillName in listOf("Heimlichkeit", "Wahrnehmung", "Überlebenskunst", "Naturkunde", "Mit Tieren umgehen")) {
                totalBonus += proficiencyBonus
                // Expertise for Athania
                if (skillName == "Heimlichkeit" || skillName == "Wahrnehmung") {
                    totalBonus += proficiencyBonus
                }
            }
        } else if (characterData.id == "Delat") {
            if (skillName in listOf("Arkane Kunde", "Täuschen", "Einschüchtern", "Motiv erkennen")) {
                totalBonus += proficiencyBonus
            }
            // Gloves of Arcana +2
            if (skillName == "Arkane Kunde" && customLoot.any { it.name.contains("Handschuhe der arkanen Kunde", ignoreCase = true) }) {
                totalBonus += 2
            }
        }
        
        return totalBonus
    }

    val spellAttackBonus: Int get() = if (characterData.charClass == CharacterClass.WARLOCK) proficiencyBonus + chaMod else proficiencyBonus + wisMod
    val spellSaveDc: Int get() = 8 + if (characterData.charClass == CharacterClass.WARLOCK) proficiencyBonus + chaMod else proficiencyBonus + wisMod
    val spellModifier: Int get() = if (characterData.charClass == CharacterClass.WARLOCK) chaMod else wisMod

    var maxHp by mutableIntStateOf(prefs.getInt("maxHp", characterData.baseMaxHp))
        private set
    var currentHp by mutableIntStateOf(prefs.getInt("currentHp", maxHp))
        private set
    var tempHp by mutableIntStateOf(prefs.getInt("tempHp", 0))
        private set
    var hitDice by mutableIntStateOf(prefs.getInt("hitDice", characterData.baseHitDice))
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

    fun toggleHeroicInspiration(active: Boolean) {
        heroicInspiration = active
        prefs.edit { putBoolean("heroicInspiration_${activeCharacterId}", active) }
    }

    fun castGoodberry() {
        if (spellSlotsLevel1 > 0) {
            spellSlotsLevel1--
            goodberries += 10
            prefs.edit { 
                putInt("spellSlotsLevel1", spellSlotsLevel1)
                putInt("goodberries", goodberries)
            }
            snackbarMessage.value = "Gute Beeren gewirkt (+10 Beeren)"
        }
    }

    fun getWeaponName(index: Int): String {
        return prefs.getString("weaponName_$index", null) ?: when {
            characterData.charClass == com.example.dndcompanion.data.CharacterClass.RANGER -> {
                when(index) {
                    0 -> "Langbogen"
                    1 -> "Kurzschwert\n& Schild"
                    else -> "Shillelagh\n& Schild"
                }
            }
            else -> {
                when(index) {
                    0 -> "Kriegshammer\n(Pakt)"
                    else -> "Speer\n(Pakt)"
                }
            }
        }
    }

    fun saveWeaponName(index: Int, name: String) {
        prefs.edit { putString("weaponName_$index", name) }
    }

    fun takeDamage(amount: Int) {
        var remainingDamage = amount
        if (tempHp > 0) {
            if (tempHp >= remainingDamage) {
                tempHp -= remainingDamage
                remainingDamage = 0
            } else {
                remainingDamage -= tempHp
                tempHp = 0
            }
            prefs.edit { putInt("tempHp", tempHp) }
        }

        if (remainingDamage > 0) {
            currentHp = (currentHp - remainingDamage).coerceAtLeast(0)
            if (currentHp > 0) {
                updateDeathSaves(0, 0)
            }
            prefs.edit { putInt("currentHp", currentHp) }
        }
    }

    fun healManual(amount: Int) {
        currentHp = (currentHp + amount).coerceAtMost(maxHp)
        if (currentHp > 0) {
            updateDeathSaves(0, 0)
        }
        prefs.edit { putInt("currentHp", currentHp) }
    }

    fun modifyTempHp(amount: Int) {
        tempHp = (tempHp + amount).coerceAtLeast(0)
        prefs.edit { putInt("tempHp", tempHp) }
    }

    private val savedWeaponName = prefs.getString("currentWeapon", ActiveWeapon.LANGBOGEN.name) ?: ActiveWeapon.LANGBOGEN.name
    var currentWeapon by mutableStateOf(ActiveWeapon.valueOf(savedWeaponName))
        private set

    fun equipWeapon(weapon: ActiveWeapon) {
        currentWeapon = weapon
        prefs.edit { putString("currentWeapon", weapon.name) }
    }
    val currentArmorClass: Int
        get() {
            // Grund-RK
            var baseAc = if (isMageArmorActive) 13 else 10
            
            // Rüstung aus Inventar (die höchste zählt)
            var armorBonus = 0
            if (customLoot.any { it.name.contains("Plattenpanzer", ignoreCase = true) }) {
                baseAc = 18 // Heavy (no Dex)
            } else if (customLoot.any { it.name.contains("Kettenpanzer", ignoreCase = true) }) {
                baseAc = 16 // Heavy (no Dex)
            } else if (customLoot.any { it.name.contains("Brustpanzer", ignoreCase = true) }) {
                baseAc = 14 // Medium (max +2 Dex)
                armorBonus = dexMod.coerceAtMost(2)
            } else if (customLoot.any { it.name.contains("Schuppenpanzer", ignoreCase = true) }) {
                baseAc = 14 // Medium (max +2 Dex)
                armorBonus = dexMod.coerceAtMost(2)
            } else if (customLoot.any { it.name.contains("Beschlagene Lederrüstung", ignoreCase = true) }) {
                baseAc = 12 // Light (full Dex)
                armorBonus = dexMod
            } else if (customLoot.any { it.name.contains("Lederrüstung", ignoreCase = true) }) {
                baseAc = 11 // Light (full Dex)
                armorBonus = dexMod
            } else {
                armorBonus = dexMod
            }

            var ac = baseAc + armorBonus
            
            // Schild-Bonus
            if (isShieldEquipped && !isUsingTwoHanded && hasShieldInInventory) {
                ac += 2
            }
            
            return ac
        }

    val currentAttackBonus: String
        get() = when (currentWeapon) {
            ActiveWeapon.LANGBOGEN -> "+${proficiencyBonus + dexMod + 2}"
            ActiveWeapon.KURZSCHWERT_SCHILD -> "+${proficiencyBonus + dexMod}"
            ActiveWeapon.SHILLELAGH_SCHILD -> "+${proficiencyBonus + wisMod}"
            ActiveWeapon.KRIEGSHAMMER_PAKT -> "+${proficiencyBonus + chaMod}"
            ActiveWeapon.SPEER_PAKT -> "+${proficiencyBonus + chaMod}"
        }

    val currentDamage: String
        get() = when (currentWeapon) {
            ActiveWeapon.LANGBOGEN -> "1W8 + $dexMod Stich (Verlangsamen: -3m Tempo)"
            ActiveWeapon.KURZSCHWERT_SCHILD -> "1W6 + $dexMod Stich (Ärgern: Vorteil auf nächsten Angriff)"
            ActiveWeapon.SHILLELAGH_SCHILD -> {
                val die = if (isUsingTwoHanded) "1W10" else "1W8"
                "$die + $wisMod Wucht (Umwerfen: KON-Save SG 12)"
            }
            ActiveWeapon.KRIEGSHAMMER_PAKT -> {
                val die = if (isUsingTwoHanded) "1W10" else "1W8"
                "$die + $chaMod Wucht (Stoß: bis zu 3m wegstoßen)"
            }
            ActiveWeapon.SPEER_PAKT -> {
                val die = if (isUsingTwoHanded) "1W8" else "1W6"
                "$die + $chaMod Stich (Schwächen: Gegner hat Nachteil auf nächsten Angriff)"
            }
        }

    var spellSlotsLevel1 by mutableIntStateOf(prefs.getInt("spellSlotsLevel1", characterData.baseSpellSlotsLevel1))
        private set
    var spellSlotsLevel2 by mutableIntStateOf(prefs.getInt("spellSlotsLevel2", characterData.baseSpellSlotsLevel2))
        private set
    var spellSlotsLevel3 by mutableIntStateOf(prefs.getInt("spellSlotsLevel3", characterData.baseSpellSlotsLevel3))
        private set
    var spellSlotsLevel4 by mutableIntStateOf(prefs.getInt("spellSlotsLevel4", 0))
        private set
    var spellSlotsLevel5 by mutableIntStateOf(prefs.getInt("spellSlotsLevel5", 0))
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

    fun resetWarlockSlots() {
        spellSlotsLevel2 = characterData.baseSpellSlotsLevel2
        prefs.edit { putInt("spellSlotsLevel2", spellSlotsLevel2) }
    }

    fun applyMagicalCunning() {
        val maxSlots = if (level >= 17) 4 else if (level >= 11) 3 else 2
        val toRegain = kotlin.math.ceil(maxSlots / 2.0).toInt()
        
        if (level >= 9) {
            spellSlotsLevel5 = (spellSlotsLevel5 + toRegain).coerceAtMost(maxSlots)
            prefs.edit { putInt("spellSlotsLevel5", spellSlotsLevel5) }
        } else if (level >= 7) {
            spellSlotsLevel4 = (spellSlotsLevel4 + toRegain).coerceAtMost(maxSlots)
            prefs.edit { putInt("spellSlotsLevel4", spellSlotsLevel4) }
        } else if (level >= 5) {
            spellSlotsLevel3 = (spellSlotsLevel3 + toRegain).coerceAtMost(maxSlots)
            prefs.edit { putInt("spellSlotsLevel3", spellSlotsLevel3) }
        } else {
            spellSlotsLevel2 = (spellSlotsLevel2 + toRegain).coerceAtMost(maxSlots)
            prefs.edit { putInt("spellSlotsLevel2", spellSlotsLevel2) }
        }
    }

    fun useHuntersMarkFree() {
        if (huntersMarkFreeUses > 0) {
            huntersMarkFreeUses--
            prefs.edit { putInt("huntersMarkFreeUses", huntersMarkFreeUses) }
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

    fun eatGoodberry() {
        if (goodberries > 0 && currentHp < maxHp) {
            goodberries--
            currentHp = (currentHp + 1).coerceAtMost(maxHp)
            prefs.edit { 
                putInt("goodberries", goodberries)
                putInt("currentHp", currentHp)
            }
        }
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

    // --- GEWICHTS-BERECHNUNG (in kg) ---
    val maxWeight: Double
        get() = strength * 7.5  // D&D Traglast = STR × 15 Pfd. = STR × 7.5 kg
    val currentWeight: Double
        get() {
            var total = 0.0
            total += water * 2.5          // 1 Trinkschlauch (voll) = 2.5 kg
            total += rations * 1.0        // 1 Ration = 1 kg
            total += totalArrows * 0.02   // 1 Pfeil = ca. 0.02 kg
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
            customLoot.addAll(characterData.defaultLoot)
            saveLoot()
        }
    }

    fun addCustomLoot(itemName: String, weight: Double = 0.0, category: String = "Sonstiges") {
        val index = customLoot.indexOfFirst { it.name.equals(itemName, ignoreCase = true) }
        if (index != -1) {
            val existingItem = customLoot[index]
            // Falls das bestehende Item kein Gewicht hat, aber jetzt eines übergeben wird, übernehmen
            val newWeight = if (existingItem.weight == 0.0 && weight > 0.0) weight else existingItem.weight
            customLoot[index] = existingItem.copy(amount = existingItem.amount + 1, weight = newWeight)
        } else {
            customLoot.add(InventoryItem(itemName, 1, weight, category))
        }
        saveLoot()
        snackbarMessage.value = "$itemName zum Rucksack hinzugefügt"
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

    // --- FREIE MERKMALE (TRAITS) ---    // --- FREE SPELLS LOGIC ---
    fun canCastAsRitual(spell: Spell): Boolean {
        if (!spell.isRitual) return false
        
        // Magier benötigen den Zauber nicht vorbereitet, solange er im globalen Buch (ihrem Zauberbuch) steht
        return if (characterData.charClass == CharacterClass.WARLOCK && characterData.id == "Delat") {
            // Delat ist Warlock, aber hat vielleicht Wizard-Rituale durch Talente/Invocations? 
            // Laut PHB 2024: Wizards only for "unprepared ritual casting".
            spell.isPrepared
        } else if (characterData.charClass == CharacterClass.RANGER) {
            spell.isPrepared
        } else {
            // Default: Muss vorbereitet sein (außer wir implementieren Wizard explizit)
            spell.isPrepared
        }
    }

    fun useTraitSpell(trait: TraitItem) {
        val index = customTraits.indexOf(trait)
        if (index != -1 && trait.currentUses > 0) {
            // Kopie erstellen und verringern, um UI-Update auszulösen
            val updatedTrait = trait.copy(currentUses = trait.currentUses - 1)
            customTraits[index] = updatedTrait
            
            // SPEZIAL-EFFEKTE basierend auf dem Zauber
            when (updatedTrait.grantedSpellId) {
                "Gute Beere" -> {
                    goodberries += 10
                    prefs.edit { putInt("goodberries", goodberries) }
                }
                "Falsches Leben" -> {
                    applyFalseLife()
                }
            }
            saveTraits()
        }
    }

    fun useItemCharge(item: InventoryItem, spellId: String) {
        val cost = item.spellCharges?.get(spellId) ?: 0
        if (item.currentCharges >= cost) {
            item.currentCharges -= cost
            saveLoot()
        }
    }

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
                
                // Sicherstellen, dass neue Standard-Merkmale (Free Spells) auch bei bestehenden Saves erscheinen
                // UND Namen aktualisiert werden (Migration)
                var listChanged = false
                characterData.defaultTraits.forEach { defaultTrait ->
                    val existingIndex = customTraits.indexOfFirst { 
                        it.grantedSpellId != null && it.grantedSpellId == defaultTrait.grantedSpellId 
                    }
                    if (existingIndex != -1) {
                        // Falls der Name abweicht (Migration), Namen aktualisieren
                        if (customTraits[existingIndex].name != defaultTrait.name) {
                            customTraits[existingIndex] = customTraits[existingIndex].copy(name = defaultTrait.name)
                            listChanged = true
                        }
                    } else if (customTraits.none { it.name == defaultTrait.name }) {
                        customTraits.add(defaultTrait)
                        listChanged = true
                    }
                }
                if (listChanged) saveTraits()
            } catch (e: Exception) {
                // Ignore
            }
        } else {
            // Initiale Merkmale beim allerersten Start laden
            customTraits.addAll(characterData.defaultTraits)
            saveTraits()
        }
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

    // --- FEATURE SELECTION UI ---
    var showFeatureSelection by mutableStateOf(false)
    var lastSelectedFeature by mutableStateOf<Feature?>(null)

    fun learnFeature(feature: Feature) {
        if (customTraits.none { it.name == feature.name }) {
            var uses = 0
            var spellId: String? = null
            
            // Heuristik: Bestimme Zauber und Nutzungen anhand des Namens oder der Beschreibung
            if (feature.name.contains("Erzfeind", ignoreCase = true)) {
                spellId = "Zeichen des Jägers"
                uses = 2
            } else if (feature.name.contains("Drow", ignoreCase = true)) {
                // Drow Magie fügen wir explizit separate Traits hinzu, weil es mehrere Zauber sind
                customTraits.add(TraitItem("Feenfeuer", "1x pro Lange Rast kostenlos wirkbar.", grantedSpellId = "Feenfeuer", maxUses = 1, currentUses = 1))
                customTraits.add(TraitItem("Dunkelheit", "1x pro Lange Rast kostenlos wirkbar.", grantedSpellId = "Dunkelheit", maxUses = 1, currentUses = 1))
            } else if (feature.name.contains("Segnen", ignoreCase = true)) {
                spellId = "Segnen"
                uses = 1
            } else if (feature.name.contains("Magierrüstung", ignoreCase = true)) {
                spellId = "Magierrüstung"
                uses = 1
            } else if (feature.name.contains("Nebelschritt", ignoreCase = true)) {
                spellId = "Nebelschritt"
                uses = 1
            }

            customTraits.add(TraitItem(
                name = feature.name,
                desc = feature.description,
                maxUses = uses,
                currentUses = uses,
                grantedSpellId = spellId
            ))
            lastSelectedFeature = feature
            saveTraits()
        }
    }

    fun dismissFeatureSelection() {
        showFeatureSelection = false
        lastSelectedFeature = null
    }

    fun unlearnFeature(featureName: String) {
        customTraits.removeAll { it.name == featureName }
        saveTraits()
    }

    // --- WERTE ZURÜCKSETZEN ---
    fun resetToDefaults() {
        // Alle SharedPreferences löschen und Grundwerte gemäß characterData setzen
        prefs.edit { clear() }

        currentEP = characterData.baseEP
        level = characterData.baseLevel
        strength = characterData.baseStrength
        dexterity = characterData.baseDexterity
        constitution = characterData.baseConstitution
        intelligence = characterData.baseIntelligence
        wisdom = characterData.baseWisdom
        charisma = characterData.baseCharisma
        maxHp = characterData.baseMaxHp
        currentHp = characterData.baseMaxHp
        hitDice = characterData.baseHitDice
        spellSlotsLevel1 = characterData.baseSpellSlotsLevel1
        spellSlotsLevel2 = characterData.baseSpellSlotsLevel2
        spellSlotsLevel3 = characterData.baseSpellSlotsLevel3
        huntersMarkFreeUses = 2
        freeCureWoundsUsed = false
        freeHealingWordUsed = false
        freeFaerieFireUsed = false
        freeDarknessUsed = false
        freeDruidSpellUsed = false
        water = 2.0f
        rations = if (characterData.id == "Athania") 10 else 3
        goodberries = if (characterData.id == "Athania") 10 else 1
        coinsKM = 0
        coinsSM = if (characterData.id == "Athania") 1 else 80
        coinsEM = 0
        coinsGM = if (characterData.id == "Athania") 18 else 19
        coinsPM = 0
        totalArrows = if (characterData.id == "Athania") 28 else 0
        shotArrows = 0
        currentWeapon = if (characterData.id == "Athania") ActiveWeapon.LANGBOGEN else ActiveWeapon.KRIEGSHAMMER_PAKT
        deathSaveSuccesses = 0
        deathSaveFailures = 0
        activeBeastType = BeastType.SKY
        generalBookEntries.clear()
        saveGeneralBookEntries()
        grudgeBookEntries.clear()
        saveGrudgeBookEntries()
        standardTactic = if (characterData.id == "Athania") "1. Zeichen des Jägers wirken (Bonusaktion)\n2. Mit Langbogen angreifen" else ""

        // Loot und Traits zurücksetzen
        customLoot.clear()
        customLoot.addAll(characterData.defaultLoot)
        saveLoot()
        customTraits.clear()
        customTraits.addAll(characterData.defaultTraits)
        saveTraits()
    }

    // --- PROFIL WECHSELN ---
    fun loadProfile(characterId: String) {
        if (activeCharacterId == characterId) return
        activeCharacterId = characterId
        characterData = CharacterRepository.getCharacter(characterId)
        prefs = getApplication<Application>().getSharedPreferences("${characterId}SaveGame", Context.MODE_PRIVATE)

        currentEP = prefs.getInt("currentEP", characterData.baseEP)
        level = prefs.getInt("level", characterData.baseLevel)
        strength = prefs.getInt("strength", characterData.baseStrength)
        dexterity = prefs.getInt("dexterity", characterData.baseDexterity)
        constitution = prefs.getInt("constitution", characterData.baseConstitution)
        intelligence = prefs.getInt("intelligence", characterData.baseIntelligence)
        wisdom = prefs.getInt("wisdom", characterData.baseWisdom)
        charisma = prefs.getInt("charisma", characterData.baseCharisma)
        maxHp = prefs.getInt("maxHp", characterData.baseMaxHp)
        currentHp = prefs.getInt("currentHp", maxHp)
        tempHp = prefs.getInt("${characterId}_tempHp", if (characterId == "Delat") 12 else 0)
        
        // --- ONE-TIME DATA SYNC / REPAIR v2 (Stand 13.03.2026 - Strict Separation) ---
        val syncKeyV2 = "isSyncedWithStatsFiles_2026_03_13_v2"
        val alreadySyncedV2 = prefs.getBoolean(syncKeyV2, false)

        if (!alreadySyncedV2) {
            val defaultLoot = characterData.defaultLoot
            val defaultLootNames = defaultLoot.map { it.name }
            val otherCharId = if (characterId == "Athania") "Delat" else "Athania"
            val otherDefaults = CharacterRepository.getCharacter(otherCharId)
            val otherLootNames = otherDefaults.defaultLoot.map { it.name }.filter { it !in defaultLootNames }
            val otherTraitNames = otherDefaults.defaultTraits.map { it.name }

            var lootChanged = false
            // Add/Update default items
            defaultLoot.forEach { default ->
                val existing = customLoot.find { it.name == default.name }
                if (existing == null) {
                    customLoot.add(default)
                    lootChanged = true
                } else if (existing.weight != default.weight || existing.category != default.category) {
                    val idx = customLoot.indexOf(existing)
                    customLoot[idx] = default
                    lootChanged = true
                }
            }
            
            // REMOVE items from the OTHER character or legacy names
            val itemsToRemove = listOf("Köcher mit 20 Pfeilen", "Kleine Onyxstatue (Fokus)", "Flöte (alt)") + otherLootNames
            val finalLoot = customLoot.filter { it.name !in itemsToRemove || it.name in defaultLootNames }
            if (finalLoot.size != customLoot.size) {
                customLoot.clear()
                customLoot.addAll(finalLoot)
                lootChanged = true
            }
            if (lootChanged) saveLoot()

            var traitsChanged = false
            val defaultTraits = characterData.defaultTraits
            val defaultTraitNames = defaultTraits.map { it.name }
            defaultTraits.forEach { default ->
                val existing = customTraits.find { it.name == default.name }
                if (existing == null) {
                    customTraits.add(default)
                    traitsChanged = true
                } else if (existing.desc != default.desc || existing.maxUses != default.maxUses) {
                    val idx = customTraits.indexOf(existing)
                    customTraits[idx] = default
                    traitsChanged = true
                }
            }
            
            // REMOVE traits from the OTHER character or legacy
            // Wir entfernen alte Bezeichnungen, erlauben aber Feenfeuer, Dunkelheit, Wunden heilen ("good" spells) explizit
            val legacyTraitsToRemove = listOf("Gute Beere", "Pakt der Klinge (alt)") + otherTraitNames
            val finalTraits = customTraits.filter { it.name !in legacyTraitsToRemove || it.name in defaultTraitNames }
            if (finalTraits.size != customTraits.size) {
                customTraits.clear()
                customTraits.addAll(finalTraits)
                traitsChanged = true
            }
            if (traitsChanged) saveTraits()
            
            // Specialized Base Stats Correction (Force stats.md values)
            strength = characterData.baseStrength
            dexterity = characterData.baseDexterity
            constitution = characterData.baseConstitution
            intelligence = characterData.baseIntelligence
            wisdom = characterData.baseWisdom
            charisma = characterData.baseCharisma
            maxHp = characterData.baseMaxHp
            if (currentHp > maxHp) currentHp = maxHp

            // Specialized Fixes
            if (characterId == "Athania") {
                if (totalArrows < 28) totalArrows = 28
                if (coinsSM < 1) coinsSM = 1
                if (coinsGM < 18) coinsGM = 18
                
                // Repariere versehentlich durch den vorherigen Bug gelöschte Traits
                val athaniaMissingTraits = listOf(
                    TraitItem("Feenfeuer", "1x pro Lange Rast kostenlos wirkbar.", grantedSpellId = "Feenfeuer", maxUses = 1, currentUses = 1),
                    TraitItem("Dunkelheit", "1x pro Lange Rast kostenlos wirkbar.", grantedSpellId = "Dunkelheit", maxUses = 1, currentUses = 1),
                    TraitItem("Elfen-Abstammungslinie (Drow)", "Du kennst Tanzende Lichter, Feenfeuer und Dunkelheit."),
                    TraitItem("Eingeweihter der Magie (Segnen)", "Du kannst Segnen 1x pro Lange Rast kostenlos wirken.", grantedSpellId = "Segnen", maxUses = 1, currentUses = 1),
                    TraitItem("Eingeweihter der Magie (Magierrüstung)", "Du kannst Magierrüstung 1x pro Lange Rast kostenlos wirken.", grantedSpellId = "Magierrüstung", maxUses = 1, currentUses = 1),
                    TraitItem("Wunden heilen", "Über Heiligtum amulett 1x pro Lange Rast.", grantedSpellId = "Wunden heilen", maxUses = 1, currentUses = 1)
                )
                
                var repaired = false
                athaniaMissingTraits.forEach { missing ->
                    if (customTraits.none { it.name == missing.name }) {
                        customTraits.add(missing)
                        repaired = true
                    }
                }
                if (repaired) saveTraits()
            }
            if (characterId == "Delat") {
                level = 4
                if (coinsSM < 80) coinsSM = 80
                if (coinsGM < 19) coinsGM = 19
                if (tempHp < 12) tempHp = 12
            }
            
            prefs.edit { putBoolean(syncKeyV2, true) }
            saveBaseStats()
        }
        // --- END SYNC ---
        hitDice = prefs.getInt("hitDice", characterData.baseHitDice)
        
        deathSaveSuccesses = prefs.getInt("deathSaveSuccesses", 0)
        deathSaveFailures = prefs.getInt("deathSaveFailures", 0)
        
        spellSlotsLevel1 = prefs.getInt("spellSlotsLevel1", characterData.baseSpellSlotsLevel1)
        spellSlotsLevel2 = prefs.getInt("spellSlotsLevel2", characterData.baseSpellSlotsLevel2)
        spellSlotsLevel3 = prefs.getInt("spellSlotsLevel3", characterData.baseSpellSlotsLevel3)
        huntersMarkFreeUses = prefs.getInt("huntersMarkFreeUses", 2)
        
        freeCureWoundsUsed = prefs.getBoolean("freeCureWoundsUsed", false)
        freeHealingWordUsed = prefs.getBoolean("freeHealingWordUsed", false)
        freeFaerieFireUsed = prefs.getBoolean("freeFaerieFireUsed", false)
        freeDarknessUsed = prefs.getBoolean("freeDarknessUsed", false)
        freeDruidSpellUsed = prefs.getBoolean("freeDruidSpellUsed", false)
        freeMageArmorUsed = prefs.getBoolean("freeMageArmorUsed", false)
        freeBlessUsed = prefs.getBoolean("freeBlessUsed", false)
        freeMistyStepUsed = prefs.getBoolean("freeMistyStepUsed", false)
        
        water = prefs.getFloat("water", 2.0f)
        rations = prefs.getInt("rations", 10)
        goodberries = prefs.getInt("goodberries", 0)
        
        coinsKM = prefs.getInt("coinsKM", 0)
        coinsSM = prefs.getInt("coinsSM", 0)
        coinsEM = prefs.getInt("coinsEM", 0)
        coinsGM = prefs.getInt("coinsGM", 0)
        coinsPM = prefs.getInt("coinsPM", 0)
        
        totalArrows = prefs.getInt("totalArrows", 20)
        shotArrows = prefs.getInt("shotArrows", 0)
        
        val savedWeaponName = prefs.getString("currentWeapon", ActiveWeapon.LANGBOGEN.name) ?: ActiveWeapon.LANGBOGEN.name
        currentWeapon = ActiveWeapon.valueOf(savedWeaponName)
        
        standardTactic = prefs.getString("standardTactic", if (characterId == "Athania") "1. Zeichen des Jägers wirken (Bonusaktion)\n2. Mit Langbogen angreifen" else "") ?: ""
        
        loadLoot()
        loadTraits()
        loadBooks()
        loadFaqs()
        loadSpells()
        loadCompanion()
        listenToSharedLoot()
        resetChat()
    }

    fun applyFalseLife() {
        tempHp = 12
        prefs.edit { putInt("tempHp", tempHp) }
    }

    // --- BÜCHER & TAKTIK ---
    val generalBookEntries = mutableStateListOf<BookEntry>()
    val grudgeBookEntries = mutableStateListOf<BookEntry>()

    val publicGeneralBookEntries = mutableStateListOf<BookEntry>()
    val publicGrudgeBookEntries = mutableStateListOf<BookEntry>()
    val groupChatMessages = mutableStateListOf<GroupChatMessage>()
    val globalQuests = mutableStateListOf<Quest>()
    
    // Group Loot States
    var sharedCoins by mutableStateOf(SharedCoins())
    val sharedLootItems = mutableStateListOf<GroupLootItem>()

    private val db = FirebaseFirestore.getInstance()

    init {
        listenToPublicNotes()
        listenToGroupChat()
        listenToQuests()
        listenToSharedLoot()
    }

    private fun listenToGroupChat() {
        db.collection("groupChat")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    groupChatMessages.clear()
                    for (doc in snapshot.documents) {
                        val msg = doc.toObject(GroupChatMessage::class.java)
                        if (msg != null) {
                            groupChatMessages.add(msg)
                        }
                    }
                }
            }
    }

    fun sendGroupMessage(text: String, isOoc: Boolean) {
        if (text.isNotBlank()) {
            val msg = GroupChatMessage(
                text = text.trim(),
                author = characterData.name,
                charClass = characterData.charClass,
                isOoc = isOoc
            )
            db.collection("groupChat").document(msg.id).set(msg)
        }
    }

    private fun listenToQuests() {
        db.collection("globalQuests")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    globalQuests.clear()
                    for (doc in snapshot.documents) {
                        val quest = doc.toObject(Quest::class.java)
                        if (quest != null) {
                            globalQuests.add(quest)
                        }
                    }
                }
            }
    }

    fun addQuest(title: String, description: String) {
        if (title.isNotBlank()) {
            val quest = Quest(
                title = title.trim(),
                description = description.trim()
            )
            db.collection("globalQuests").document(quest.id).set(quest)
        }
    }

    fun toggleQuestCompletion(quest: Quest) {
        val newState = !quest.isCompleted
        db.collection("globalQuests").document(quest.id).update("isCompleted", newState)
    }

    fun updateQuest(questId: String, title: String, description: String) {
        db.collection("globalQuests").document(questId).update(
            "title", title.trim(),
            "description", description.trim()
        )
    }

    fun deleteQuest(questId: String) {
        db.collection("globalQuests").document(questId).delete()
    }

    // --- SHARED GROUP LOOT ---
    private fun listenToSharedLoot() {
        // Listen to Shared Coins
        db.collection("groupLootCoins").document("shared").addSnapshotListener { snapshot, e ->
            if (e == null && snapshot != null && snapshot.exists()) {
                val coins = snapshot.toObject(SharedCoins::class.java)
                if (coins != null) {
                    sharedCoins = coins
                }
            }
        }
        // Listen to Shared Items
        db.collection("groupLootItems").addSnapshotListener { snapshot, e ->
            if (e == null && snapshot != null) {
                sharedLootItems.clear()
                for (doc in snapshot.documents) {
                    val item = doc.toObject(GroupLootItem::class.java)
                    if (item != null) sharedLootItems.add(item)
                }
            }
        }
    }

    fun updateSharedCoins(km: Int, sm: Int, em: Int, gm: Int, pm: Int) {
        val newCoins = SharedCoins(km, sm, em, gm, pm)
        db.collection("groupLootCoins").document("shared").set(newCoins)
    }

    fun addSharedLootItem(name: String, amount: Int, weight: Double, category: String) {
        // Check if item with exact name already exists to increment amount
        val existing = sharedLootItems.find { it.name.trim().equals(name.trim(), ignoreCase = true) }
        if (existing != null) {
            val updatedAmount = existing.amount + amount
            if (updatedAmount <= 0) {
                db.collection("groupLootItems").document(existing.id).delete()
            } else {
                db.collection("groupLootItems").document(existing.id).update("amount", updatedAmount)
            }
        } else if (amount > 0) {
            val item = GroupLootItem(name = name.trim(), amount = amount, weight = weight, category = category)
            db.collection("groupLootItems").document(item.id).set(item)
        }
    }

    fun updateSharedLootItem(id: String, amount: Int) {
        if (amount <= 0) {
            db.collection("groupLootItems").document(id).delete()
        } else {
            db.collection("groupLootItems").document(id).update("amount", amount)
        }
    }

    fun deleteSharedLootItem(id: String) {
        db.collection("groupLootItems").document(id).delete()
    }

    private fun listenToPublicNotes() {
        // Listener für allgemeine öffentliche Notizen
        db.collection("publicGeneralNotes")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    publicGeneralBookEntries.clear()
                    for (doc in snapshot.documents) {
                        val entry = doc.toObject(BookEntry::class.java)
                        if (entry != null) {
                            publicGeneralBookEntries.add(entry)
                        }
                    }
                }
            }

        // Listener für das öffentliche Buch des Grolls
        db.collection("publicGrudgeNotes")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    publicGrudgeBookEntries.clear()
                    for (doc in snapshot.documents) {
                        val entry = doc.toObject(BookEntry::class.java)
                        if (entry != null) {
                            publicGrudgeBookEntries.add(entry)
                        }
                    }
                }
            }
    }

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

    fun addGeneralBookEntry(text: String, isPublic: Boolean = false) {
        if (text.isNotBlank()) {
            val entry = BookEntry(text = text.trim(), isPublic = isPublic)
            if (isPublic) {
                db.collection("publicGeneralNotes").document(entry.id).set(entry)
            } else {
                generalBookEntries.add(0, entry)
                saveGeneralBookEntries()
            }
        }
    }

    fun updateGeneralBookEntry(id: String, newText: String, isPublic: Boolean = false) {
        if (newText.isNotBlank()) {
            if (isPublic) {
                db.collection("publicGeneralNotes").document(id).update("text", newText.trim())
            } else {
                val index = generalBookEntries.indexOfFirst { it.id == id }
                if (index != -1) {
                    generalBookEntries[index] = generalBookEntries[index].copy(text = newText.trim())
                    saveGeneralBookEntries()
                }
            }
        }
    }

    fun addGrudgeBookEntry(text: String, isPublic: Boolean = false) {
        if (text.isNotBlank()) {
            val entry = BookEntry(text = text.trim(), isPublic = isPublic)
            if (isPublic) {
                db.collection("publicGrudgeNotes").document(entry.id).set(entry)
            } else {
                grudgeBookEntries.add(0, entry)
                saveGrudgeBookEntries()
            }
        }
    }

    fun updateGrudgeBookEntry(id: String, newText: String, isPublic: Boolean = false) {
        if (newText.isNotBlank()) {
            if (isPublic) {
                db.collection("publicGrudgeNotes").document(id).update("text", newText.trim())
            } else {
                val index = grudgeBookEntries.indexOfFirst { it.id == id }
                if (index != -1) {
                    grudgeBookEntries[index] = grudgeBookEntries[index].copy(text = newText.trim())
                    saveGrudgeBookEntries()
                }
            }
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
        listenToPublicNotes()
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

    var freeMageArmorUsed by mutableStateOf(prefs.getBoolean("freeMageArmorUsed", false))
        private set

    fun useFreeMageArmor() {
        if (!freeMageArmorUsed) {
            freeMageArmorUsed = true
            prefs.edit { putBoolean("freeMageArmorUsed", true) }
        }
    }

    var freeBlessUsed by mutableStateOf(prefs.getBoolean("freeBlessUsed", false))
        private set

    fun useFreeBless() {
        if (!freeBlessUsed) {
            freeBlessUsed = true
            prefs.edit { putBoolean("freeBlessUsed", true) }
        }
    }

    var freeMistyStepUsed by mutableStateOf(prefs.getBoolean("freeMistyStepUsed", false))
        private set

    fun useFreeMistyStep() {
        if (!freeMistyStepUsed) {
            freeMistyStepUsed = true
            prefs.edit { putBoolean("freeMistyStepUsed", true) }
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
        
        // Paktmagie-Regeneration für Hexenmeister
        if (characterData.charClass == CharacterClass.WARLOCK) {
            spellSlotsLevel1 = getMaxSpellSlots(level, 1)
            spellSlotsLevel2 = getMaxSpellSlots(level, 2)
            spellSlotsLevel3 = getMaxSpellSlots(level, 3)
            spellSlotsLevel4 = getMaxSpellSlots(level, 4)
            spellSlotsLevel5 = getMaxSpellSlots(level, 5)
            
            prefs.edit { 
                putInt("spellSlotsLevel1", spellSlotsLevel1)
                putInt("spellSlotsLevel2", spellSlotsLevel2)
                putInt("spellSlotsLevel3", spellSlotsLevel3)
                putInt("spellSlotsLevel4", spellSlotsLevel4)
                putInt("spellSlotsLevel5", spellSlotsLevel5)
            }
        }
        
        // Traits mit Reset bei Kurzer Rast zurücksetzen
        var traitsChanged = false
        customTraits.forEachIndexed { index, trait ->
            if (trait.resetOnShortRest && trait.currentUses < trait.maxUses) {
                customTraits[index] = trait.copy(currentUses = trait.maxUses)
                traitsChanged = true
            }
        }
        if (traitsChanged) saveTraits()
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
            forceLongRest(consumeResources = true)
        }
    }

    fun forceLongRestWithoutResources() {
        showRestWarningDialog = false
        forceLongRest(consumeResources = false)
    }

    private fun forceLongRest(consumeResources: Boolean) {
        currentHp = maxHp
        val recoveredHitDice = (level / 2).coerceAtLeast(1)
        hitDice = (hitDice + recoveredHitDice).coerceAtMost(level)

        spellSlotsLevel1 = getMaxSpellSlots(level, 1)
        spellSlotsLevel2 = getMaxSpellSlots(level, 2)
        spellSlotsLevel3 = getMaxSpellSlots(level, 3)
        spellSlotsLevel4 = getMaxSpellSlots(level, 4)
        spellSlotsLevel5 = getMaxSpellSlots(level, 5)

        huntersMarkFreeUses = 2
        freeCureWoundsUsed = false
        freeHealingWordUsed = false
        freeFaerieFireUsed = false
        freeDarknessUsed = false
        freeDruidSpellUsed = false
        freeMageArmorUsed = false
        freeBlessUsed = false
        freeMistyStepUsed = false
        goodberries = 0
        geminiUsesToday = 0
        
        // Alle Traits bei langer Rast zurücksetzen
        customTraits.forEachIndexed { index, trait ->
            if (trait.currentUses < trait.maxUses) {
                customTraits[index] = trait.copy(currentUses = trait.maxUses)
            }
        }
        
        // Begleiter-Wiederbelebung bei langer Rast
        if (companionIsDead) {
            reviveCompanion()
        }
        saveTraits()
        
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
            putInt("spellSlotsLevel4", spellSlotsLevel4)
            putInt("spellSlotsLevel5", spellSlotsLevel5)
            putInt("huntersMarkFreeUses", huntersMarkFreeUses)
            putBoolean("freeCureWoundsUsed", freeCureWoundsUsed)
            putBoolean("freeHealingWordUsed", freeHealingWordUsed)
            putBoolean("freeFaerieFireUsed", freeFaerieFireUsed)
            putBoolean("freeDarknessUsed", freeDarknessUsed)
            putBoolean("freeDruidSpellUsed", freeDruidSpellUsed)
            putBoolean("freeMageArmorUsed", freeMageArmorUsed)
            putBoolean("freeBlessUsed", freeBlessUsed)
            putBoolean("freeMistyStepUsed", freeMistyStepUsed)
            putInt("goodberries", goodberries)
            putInt("geminiUsesToday", geminiUsesToday)
        }
    }

    var companionData by mutableStateOf<CompanionDto?>(null)
        private set

    fun loadCompanion() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                if (characterData.charClass == CharacterClass.RANGER) {
                    val jsonString = context.assets.open("Rules/urtier.json").bufferedReader().use { it.readText() }
                    val fileDto: UrtierFileDto = gson.fromJson(jsonString, UrtierFileDto::class.java)
                    val targetName = when(activeBeastType) {
                        BeastType.LAND -> "Urtier des Landes"
                        BeastType.SKY -> "Urtier des Himmels"
                        BeastType.SEA -> "Urtier des Meeres"
                    }
                    companionData = fileDto.urtiere.find { it.name == targetName }
                } else if (activeCharacterId == "Delat") {
                    val jsonString = context.assets.open("Rules/vertrauter.json").bufferedReader().use { it.readText() }
                    companionData = gson.fromJson(jsonString, CompanionDto::class.java)
                } else {
                    companionData = null
                }
            } catch (e: Exception) {
                println("Error loading companion: ${e.message}")
                companionData = null
            }
        }
    }

    var activeBeastType by mutableStateOf(BeastType.valueOf(prefs.getString("activeBeastType", BeastType.SKY.name) ?: BeastType.SKY.name))
        private set

    val capyMaxHp: Int get() {
        val base = if (activeBeastType == BeastType.SKY || activeBeastType == BeastType.SEA) 4 else 5
        val mult = if (activeBeastType == BeastType.SKY || activeBeastType == BeastType.SEA) 4 else 5
        return if (characterData.charClass == CharacterClass.RANGER) base + (mult * level) else 24 // Sphinx has fixed 24
    }
    var capyCurrentHp by mutableIntStateOf(prefs.getInt("capyCurrentHp_${characterData.id}", 20))
        private set

    var companionIsDead by mutableStateOf(prefs.getBoolean("companionIsDead_${characterData.id}", false))

    fun toggleBeastType(type: BeastType) {
        activeBeastType = type
        loadCompanion()
        if (capyCurrentHp > capyMaxHp) capyCurrentHp = capyMaxHp
        prefs.edit {
            putString("activeBeastType", activeBeastType.name)
            putInt("capyCurrentHp_${characterData.id}", capyCurrentHp)
        }
    }

    fun takeCapyDamage(amount: Int) {
        capyCurrentHp = (capyCurrentHp - amount).coerceAtLeast(0)
        if (capyCurrentHp == 0) companionIsDead = true
        prefs.edit { 
            putInt("capyCurrentHp_${characterData.id}", capyCurrentHp)
            putBoolean("companionIsDead_${characterData.id}", companionIsDead)
        }
    }

    fun healCapy(amount: Int) {
        capyCurrentHp = (capyCurrentHp + amount).coerceAtMost(capyMaxHp)
        if (capyCurrentHp > 0) companionIsDead = false
        prefs.edit { 
            putInt("capyCurrentHp_${characterData.id}", capyCurrentHp)
            putBoolean("companionIsDead_${characterData.id}", companionIsDead)
        }
    }

    fun reviveCompanion() {
        capyCurrentHp = capyMaxHp
        companionIsDead = false
        prefs.edit { 
            putInt("capyCurrentHp_${characterData.id}", capyCurrentHp)
            putBoolean("companionIsDead_${characterData.id}", companionIsDead)
        }
    }

    val capyAc: Int get() = if (characterData.charClass == CharacterClass.RANGER) 13 + wisMod else 13
    val capyAttackBonus: String get() = if (characterData.charClass == CharacterClass.RANGER) "+$spellAttackBonus" else "+5"
    val capyDamage: String get() = if (characterData.charClass == CharacterClass.RANGER) {
        if (activeBeastType == BeastType.SKY) "1W4 + 3 + $wisMod Hieb" else if(activeBeastType == BeastType.SEA) "1W6 + 2 + $wisMod Stich" else "1W8 + 2 + $wisMod Hieb"
    } else "1W4+3 Hieb + 2W6 Gleißend"
    
    val capySpeed: String get() = companionData?.bewegungsrate?.entries?.joinToString(", ") { "${it.key}: ${it.value}" } ?: ""
    val capySpecial: String get() = companionData?.merkmale?.joinToString("\n") { "${it.name}: ${it.beschreibung}" } ?: ""

    // --- HILFE: CHAT & FAQ ---
    val chatHistory = mutableStateListOf<ChatMessage>()
    val faqList = mutableStateListOf<FaqItem>()

    var currentUsedModel by mutableStateOf("Bereit")
        private set
    var geminiUsesToday by mutableIntStateOf(prefs.getInt("geminiUsesToday", 0))
        private set
    val geminiMax = 20

    private val systemPrompt: String get() = """
        Du bist unser D&D 2024 Regel-Assistent. Dein Ziel ist es, Fragen basierend auf unseren Hausregeln (Handbuch/Zauberbuch) und dem Charakterblatt von ${characterData.name} zu beantworten.
        
        FORMATRICHTLINIE (SEHR WICHTIG):
        Antworte AUSSCHLIESSLICH im JSON-Format. Verwende exakt diese Schlüsselstruktur und erzeuge keinen Text außerhalb der JSON-Klammern:
        {
          "lokale_antwort": "Deine Antwort NUR basierend auf den bereitgestellten Handbüchern/Stats. Wenn nichts gefunden, schreibe 'Keine spezifischen Informationen gefunden.'",
          "externe_antwort": "Deine Antwort basierend auf deinem allgemeinen Wissen über D&D 2024. Gehe auf die Klasse und das Volk des Charakters ein, falls relevant.",
          "kapitel_link": "NUR der exakte Name eines HANDBUCH-Kapitels aus den '--- Quelle: ... ---' Markierungen (z.B. '3. Klassen', '7. Kampf'). Erlaubte Werte: '1. Gameplay', '2. Völker', '3. Klassen', '4. Herkünfte', '5. Talente', '6. Ausrüstung', '7. Kampf', '8. Zauber', 'Zauberbuch Übersicht'. WICHTIG: Wenn die Antwort aus dem CHARAKTERBLATT kommt (Stats, Begleiter/Capys, Inventar, Zauberplätze, Notizbuch etc.) und NICHT aus einem Handbuch-Kapitel, setze den Wert auf null!",
          "suchbegriff": "Ein kurzes Stichwort (1-2 Worte) aus dem Kapitel, das exakt zu deiner Antwort passt, um im UI genau zu dieser Regel zu scrollen (z.B. 'Zaubertricks' oder 'Kampfstile'). Nur setzen wenn kapitel_link gesetzt ist.",
          "faq_titel": "Ein knackiges, kurzes Schlagwort (max 3 Worte), das diese Antwort zusammenfasst (z.B. 'Initiative', 'Paktmagie', 'Rüstungsklasse'), falls der User die Info ins FAQ aufnehmen will. IMMER setzen!"
        }
    """.trimIndent()

private val model25Flash = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val model25FlashLite = GenerativeModel(
        modelName = "gemini-2.5-flash-lite-preview",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private var activeChatSession = model25Flash.startChat()

    fun loadFaqs() {
        val faqKey = "savedFaqs_${characterData.name}"
        val faqString = prefs.getString(faqKey, "") ?: ""
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
        } else {
            faqList.clear()
        }
    }

    private fun saveFaqs() {
        val faqKey = "savedFaqs_${characterData.name}"
        val json = gson.toJson(faqList)
        prefs.edit { putString(faqKey, json) }
    }

    // --- SPELBOOK (ZAUBERBUCH) ---
    val allSpells = mutableStateListOf<Spell>()
    val globalSpellbook = mutableStateListOf<Spell>()
    val globalFeatures = mutableStateListOf<Feature>()

    init {
        loadLoot()
        loadFaqs()
        loadSpells()
        loadGlobalSpellbook()
        loadGlobalFeatures()
        loadEquipment()
        loadTraits()
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

                val fileName = "Rules/Zauberbuch/spellbook.json"
                try {
                    val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
                    val dtos: List<SpellDto> = gson.fromJson(jsonString, type)
                    spellList.addAll(dtos.map { it.toSpell() })
                } catch (e: Exception) {
                    println("Error loading spellbook: ${e.message}")
                }
                globalSpellbook.clear()
                globalSpellbook.addAll(spellList)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun loadGlobalFeatures() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val fileName = "Rules/merkmale.json"
                val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
                val featureListDto: FeatureListDto = gson.fromJson(jsonString, FeatureListDto::class.java)
                
                globalFeatures.clear()
                globalFeatures.addAll(featureListDto.features.map { it.toFeature() })
            } catch (e: Exception) {
                println("Error loading features: ${e.message}")
            }
        }
    }

    fun getAvailableFeatures(): List<Feature> {
        val charClassEn = characterData.charClass.name // e.g. "RANGER"
        val charClassDe = when(charClassEn) {
            "RANGER" -> "Waldläufer"
            "WARLOCK" -> "Hexenmeister"
            else -> charClassEn
        }
        val charRace = characterData.race.lowercase()
        // Simplify race (e.g. "Elf-Drow" or "Elf (Drow)" -> "elf")
        val simpleRace = charRace.split("-", "(", " ").first()

        return globalFeatures.filter { feature ->
            val classMatch = feature.classReq.isEmpty() || feature.classReq.any { it.equals(charClassDe, ignoreCase = true) || it.equals(charClassEn, ignoreCase = true) }
            val raceMatch = feature.raceReq.isEmpty() || feature.raceReq.any { req ->
                val reqLow = req.lowercase()
                charRace == reqLow || simpleRace == reqLow || charRace.startsWith(reqLow)
            }
            val levelMatch = level >= feature.levelReq
            
            classMatch && raceMatch && levelMatch
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
                                // Wir suchen in allen Spalten nach
                                var weight = 0.0
                                for (part in parts) {
                                    val cleanPart = part.lowercase().trim()
                                    if (cleanPart.endsWith("kg")) {
                                        val weightStr = cleanPart.replace("kg", "").trim().replace(",", ".")
                                        weight = weightStr.toDoubleOrNull() ?: 0.0
                                        break
                                    } else if (cleanPart.contains("tonne")) {
                                        val weightStr = cleanPart.replace("tonnen", "").replace("tonne", "").trim().replace(",", ".")
                                        weight = (weightStr.toDoubleOrNull() ?: 0.0) * 1000.0 // 1 Tonne = 1000 kg (in Metric context)
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

                // --- ONE-TIME SPELL SYNC v2 (Stand 13.03.2026 - Strict Separation) ---
                val syncKeyV2 = "isSyncedWithSpells_2026_03_13_v2"
                val alreadySyncedV2 = prefs.getBoolean(syncKeyV2, false)

                if (!alreadySyncedV2) {
                    val defaultSpells = getDefaultSpells()
                    val defaultNames = defaultSpells.map { it.name }
                    val otherCharId = if (characterData.id == "Athania") "Delat" else "Athania"
                    val otherSpells = if (otherCharId == "Delat") getDelatDefaultSpells() else getAthaniaDefaultSpells()
                    val otherSpellNames = otherSpells.map { it.name }
                    
                    var changed = false
                    defaultSpells.forEach { default ->
                        val existing = allSpells.find { it.name == default.name }
                        if (existing == null) {
                            allSpells.add(default)
                            changed = true
                        } else if (existing.description != default.description || existing.level != default.level) {
                            val idx = allSpells.indexOf(existing)
                            allSpells[idx] = default
                            changed = true
                        }
                    }
                    
                    // REMOVE spells from the OTHER character or legacy
                    val legacyNames = listOf("Heilendes Wort", "Gute Beere")
                    val filtered = allSpells.filter { (it.name !in legacyNames && it.name !in otherSpellNames) || it.name in defaultNames }
                    if (filtered.size != allSpells.size) {
                        allSpells.clear()
                        allSpells.addAll(filtered)
                        changed = true
                    }

                    if (changed) saveSpells()
                    prefs.edit { putBoolean(syncKeyV2, true) }
                }
                // --- END SYNC ---

            } catch (e: Exception) {
                // Bei Fehlern nicht abstürzen
            }
        } else {
            // Wenn leer, Standard-Zauber laden
            allSpells.addAll(getDefaultSpells())
            saveSpells()
        }
    }

    private fun getDefaultSpells(): List<Spell> {
        return if (characterData.id == "Delat") getDelatDefaultSpells() else getAthaniaDefaultSpells()
    }

    private fun getDelatDefaultSpells(): List<Spell> {
        return listOf(
            Spell(name = "Schauerlicher Strahl", level = 0, castingTime = "1 Aktion", range = "36 m", duration = "Sofort", componentsV = true, componentsS = true, description = "1d10 Kraftschaden.", isPrepared = true),
            Spell(name = "Totenläuten", level = 0, castingTime = "1 Aktion", range = "18 m", duration = "Sofort", componentsV = true, componentsS = true, description = "1W8/1W12 Nekrotisch (WIS RW).", isPrepared = true),
            Spell(name = "Einfache Illusion", level = 0, castingTime = "1 Aktion", range = "9 m", duration = "1 Min.", componentsV = true, componentsS = true, componentsM = true, description = "Erzeugt ein Bild oder Geräusch.", isPrepared = true),
            Spell(name = "Donnerschlag", level = 0, castingTime = "1 Aktion", range = "Selbst", duration = "Sofort", componentsV = true, description = "1W6 Donnerschaden (KON RW).", isPrepared = true),
            Spell(name = "Paktwaffe", level = 0, castingTime = "1 Bonusaktion", range = "Selbst", duration = "Bis zur Entlassung", description = "Beschwört eine Paktwaffe.", isPrepared = true),
            Spell(name = "Magierhand", level = 0, castingTime = "1 Aktion", range = "9 m", duration = "1 Min.", componentsV = true, componentsS = true, description = "Eine schwebende Spektralhand.", isPrepared = true),
            
            Spell(name = "Verwünschen", level = 1, castingTime = "1 Bonusaktion", range = "27 m", duration = "1 Std.", componentsV = true, componentsS = true, componentsM = true, description = "Extra 1W6 Schaden bei Treffern.", isPrepared = true),
            Spell(name = "Magie Entdecken", level = 1, castingTime = "1 Aktion (Ritual)", range = "Selbst", duration = "Konzentration, 10 Min.", componentsV = true, componentsS = true, description = "Spürt Magie in 9m.", isPrepared = true),
            Spell(name = "Falsches Leben", level = 1, castingTime = "1 Aktion", range = "Selbst", duration = "1 Std.", componentsV = true, componentsS = true, componentsM = true, description = "2W4 + 4 temporäre HP.", isPrepared = true),
            Spell(name = "Magierrüstung", level = 1, castingTime = "1 Aktion", range = "Berührung", duration = "8 Std.", componentsV = true, componentsS = true, componentsM = true, description = "RK wird 13 + GES.", isPrepared = true),
            Spell(name = "Dissonantes Flüstern", level = 1, castingTime = "1 Aktion", range = "18 m", duration = "Sofort", componentsV = true, description = "3W6 Psychisch + Flucht (WIS RW).", isPrepared = true),
            Spell(name = "Tashas fürchterlicher Lachanfall", level = 1, castingTime = "1 Aktion", range = "9 m", duration = "Konzentration, 1 Min.", componentsV = true, componentsS = true, componentsM = true, description = "Ziel bricht in Lachen aus (WEI RW).", isPrepared = true),
            Spell(name = "Segnen", level = 1, castingTime = "1 Aktion", range = "9 m", duration = "Konzentration, 1 Min.", componentsV = true, componentsS = true, componentsM = true, description = "+1W4 auf Angriffs/Rettungswürfe.", isPrepared = true),

            Spell(name = "Gedanken Wahrnehmen", level = 2, castingTime = "1 Aktion", range = "Selbst", duration = "Konzentration, 1 Min.", componentsV = true, componentsS = true, componentsM = true, description = "Liest Oberflächengedanken.", isPrepared = true),
            Spell(name = "Macht der Vorstellungskraft", level = 2, castingTime = "1 Aktion", range = "18 m", duration = "Konzentration, 1 Min.", componentsV = true, componentsS = true, componentsM = true, description = "Erschafft eine mentale Illusion (INT RW).", isPrepared = true),
            Spell(name = "Nebelschritt", level = 2, castingTime = "1 Bonusaktion", range = "9 m", duration = "Sofort", componentsV = false, componentsS = true, description = "Teleportation.", isPrepared = true),
            Spell(name = "Unsichtbarkeit", level = 2, castingTime = "1 Aktion", range = "Berührung", duration = "Konzentration, 1 Std.", componentsV = true, componentsS = true, componentsM = true, description = "Ziel wird unsichtbar.", isPrepared = true),
            Spell(name = "Einflüsterung", level = 2, castingTime = "1 Aktion", range = "9 m", duration = "Konzentration, 8 Std.", componentsV = true, componentsM = true, description = "Gibt einer Kreatur einen Befehl (WEI RW).", isPrepared = true),
            Spell(name = "Spiegelbilder", level = 2, castingTime = "1 Aktion", range = "Selbst", duration = "1 Std.", componentsV = true, componentsS = true, description = "Erschafft 3 Abbilder.", isPrepared = true)
        )
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
            Spell(name = "Tanzende Lichter", level = 0, castingTime = "1 Aktion", range = "36 m", duration = "Konzentration, 1 Min.", description = "Erschafft 4 Lichter.", isPrepared = true),
            Spell(name = "Göttliche Führung", level = 0, castingTime = "1 Aktion", range = "Berührung", duration = "Konzentration, 1 Min.", description = "+1W4 auf Attributswurf.", isPrepared = true),
            Spell(name = "Shillelagh", level = 0, castingTime = "1 Bonusaktion", range = "Berührung", duration = "1 Min.", description = "Waffe nutzt WEI für Angriff/Schaden (1W8).", isPrepared = true),
            Spell(name = "Kalte Hand", level = 0, castingTime = "1 Aktion", range = "Berührung", duration = "Sofort", description = "Nekrotischer Angriff.", isPrepared = true),
            
            Spell(name = "Verstricken", level = 1, castingTime = "1 Aktion", range = "27 m", duration = "Konzentration, 1 Min.", description = "Pflanzen halten Gegner fest (ST RW).", isPrepared = true),
            Spell(name = "Wunden heilen", level = 1, castingTime = "1 Aktion", range = "Berührung", duration = "Sofort", description = "2W8 + WEI Heilung.", isPrepared = true),
            Spell(name = "Gute Beeren", level = 1, castingTime = "1 Aktion", range = "Berührung", duration = "Sofort", description = "10 Beeren, heilen 1 TP.", isPrepared = true),
            Spell(name = "Mit Tieren sprechen", level = 1, castingTime = "1 Aktion (Ritual)", range = "Selbst", duration = "10 Min.", description = "Kommunikation mit Tieren.", isPrepared = true),
            Spell(name = "Zeichen des Jägers", level = 1, castingTime = "1 Bonusaktion", range = "27 m", duration = "Konzentration, 1 Std.", description = "+1W6 Kraftschaden.", isPrepared = true),
            Spell(name = "Nebelwolke", level = 1, castingTime = "1 Aktion", range = "36 m", duration = "Konzentration, 1 Std.", description = "Erschafft Nebel.", isPrepared = true),
            Spell(name = "Feenfeuer", level = 1, castingTime = "1 Aktion", range = "18 m", duration = "Konzentration, 1 Min.", description = "Vorteil auf Angriffe gegen Ziele (GES RW).", isPrepared = true),
            Spell(name = "Lange Schritte", level = 1, castingTime = "1 Aktion", range = "Berührung", duration = "1 Std.", description = "+3m Bewegungsrate.", isPrepared = true)
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

        val className = if (characterData.charClass == com.example.dndcompanion.data.CharacterClass.RANGER) "Waldläufer (Beast Master)" else "Warlock (Pakt der Klinge)"
        val race = if (characterData.name == "Athania") "Elf (Waldelf / Feenblut)" else "Mensch"
        val primaryCastingInfo = if (characterData.charClass == com.example.dndcompanion.data.CharacterClass.RANGER) {
            "Zauberplätze: G1: $spellSlotsLevel1, G2: $spellSlotsLevel2, G3: $spellSlotsLevel3"
        } else {
            val maxSlots = characterData.baseSpellSlotsLevel2
            "Paktmagie: $spellSlotsLevel2/$maxSlots (Level 2 Slots)"
        }

        val baseContext = """
            KONTEXT CHARAKTERBLATT ${characterData.name.uppercase()}:
            Klasse: $className
            Volk: $race
            Level: $level, EP: $currentEP
            HP: $currentHp/$maxHp (Temp HP: $tempHp), Trefferwürfel: $hitDice/$level
            Werte: ST $strength ($stModStr), GE $dexterity ($geModStr), KO $constitution ($koModStr), IN $intelligence ($inModStr), WE $wisdom ($weModStr), CH $charisma ($chModStr)
            Rüssi-Klasse: $currentArmorClass, Initiative: $geModStr
            Waffe: ${currentWeapon.name} (Bonus: +$currentAttackBonus, Schaden: $currentDamage)
            $primaryCastingInfo
            Vorbereitete Zauber: $preparedSpells
            Alle bekannten Zauber: $allKnownSpells
            Merkmale/Fähigkeiten: $traitsStr
            Vorrätig: $water L Wasser, $rations Rationen, $goodberries Beeren, $totalArrows Pfeile
            Geld: $coinsGM GM, $coinsSM SM
            Inventar: $inventoryStr
            Notizbuch: $notes
            Buch des Grolls: $grudges
        """.trimIndent()

        if (characterData.charClass == com.example.dndcompanion.data.CharacterClass.RANGER) {
            val landHp = 5 + 5 * level
            val skyHp = 4 + 4 * level
            val seaHp = 4 + 4 * level
            val beastAc = 13 + proficiencyBonus

            return baseContext + "\n\n" + """
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

        return baseContext
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
                        currentUsedModel = "Gemini 2.5 Flash"
                        val response = activeChatSession.sendMessage(finalPrompt)
                        finalizeResponse(loadingIndex, response.text)
                    } catch (e: Exception) {
                        currentUsedModel = "Gemini 2.5 Flash-Lite (Fallback)"
                        val fallbackSession = model25FlashLite.startChat(history = activeChatSession.history)
                        val response = fallbackSession.sendMessage(finalPrompt)
                        activeChatSession = fallbackSession
                        finalizeResponse(loadingIndex, response.text)
                    }
                } else {
                    throw Exception("Tageslimit für Gemini erreicht ($geminiMax)")
                }
            } catch (e: Exception) {
                android.util.Log.e("GeminiError", "Error during Gemini API call", e)
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
        var parsedFaqTitle: String? = "Regelerklärung"

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
            if (json.has("faq_titel") && !json.isNull("faq_titel")) {
                val f = json.getString("faq_titel")
                if (f.isNotBlank() && f != "null") parsedFaqTitle = f
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
            chapterSearchTerm = parsedSearchTerm,
            faqTitle = parsedFaqTitle
        )
    }

    fun resetChat() {
        chatHistory.clear()
        activeChatSession = model25Flash.startChat()
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