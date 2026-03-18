package com.example.dndcompanion.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dndcompanion.data.CharacterClass
import com.example.dndcompanion.data.database.AppDatabase
import com.example.dndcompanion.data.database.SpellEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SpellViewModel(
    application: Application,
    private val characterVm: CharacterViewModel
) : AndroidViewModel(application) {

    var inventoryVm: InventoryViewModel? = null // set later by MainActivity

    private val gson = Gson()

    private val prefs get() = characterVm.prefsManager.prefs

    // --- ZAUBERPLÄTZE ---
    var spellSlotsLevel1 by mutableIntStateOf(prefs.getInt("spellSlotsLevel1", characterVm.characterData.baseSpellSlotsLevel1))
        private set
    var spellSlotsLevel2 by mutableIntStateOf(prefs.getInt("spellSlotsLevel2", characterVm.characterData.baseSpellSlotsLevel2))
        private set
    var spellSlotsLevel3 by mutableIntStateOf(prefs.getInt("spellSlotsLevel3", characterVm.characterData.baseSpellSlotsLevel3))
        private set
    var spellSlotsLevel4 by mutableIntStateOf(prefs.getInt("spellSlotsLevel4", 0))
        private set
    var spellSlotsLevel5 by mutableIntStateOf(prefs.getInt("spellSlotsLevel5", 0))
        private set

    // --- FREIE ZAUBER ---
    var huntersMarkFreeUses by mutableIntStateOf(prefs.getInt("huntersMarkFreeUses", 2))
        private set

    var freeAmuletSpellUsed by mutableStateOf(prefs.getBoolean("freeAmuletSpellUsed", false))
        private set
    var freeFaerieFireUsed by mutableStateOf(prefs.getBoolean("freeFaerieFireUsed", false))
        private set
    var freeDarknessUsed by mutableStateOf(prefs.getBoolean("freeDarknessUsed", false))
        private set
    var freeDruidSpellUsed by mutableStateOf(prefs.getBoolean("freeDruidSpellUsed", false))
        private set
    var freeMageArmorUsed by mutableStateOf(prefs.getBoolean("freeMageArmorUsed", false))
        private set
    var freeBlessUsed by mutableStateOf(prefs.getBoolean("freeBlessUsed", false))
        private set
    var freeMistyStepUsed by mutableStateOf(prefs.getBoolean("freeMistyStepUsed", false))
        private set

    // --- ZAUBERLISTE ---
    val allSpells = mutableStateListOf<Spell>()
    val globalSpellbook = MutableStateFlow<List<SpellEntity>>(emptyList())

    init {
        viewModelScope.launch {
            characterVm.activeCharacterIdFlow.collect { newId ->
                reloadForCharacter(newId)
            }
        }
        loadSpells()

        viewModelScope.launch {
            val db = AppDatabase.getDatabase(getApplication()).rulebookDao()
            db.getAllSpells().collectLatest { spells ->
                globalSpellbook.value = spells
            }
        }
    }

    private fun reloadForCharacter(id: String) {
        spellSlotsLevel1 = prefs.getInt("spellSlotsLevel1", characterVm.characterData.baseSpellSlotsLevel1)
        spellSlotsLevel2 = prefs.getInt("spellSlotsLevel2", characterVm.characterData.baseSpellSlotsLevel2)
        spellSlotsLevel3 = prefs.getInt("spellSlotsLevel3", characterVm.characterData.baseSpellSlotsLevel3)
        spellSlotsLevel4 = prefs.getInt("spellSlotsLevel4", 0)
        spellSlotsLevel5 = prefs.getInt("spellSlotsLevel5", 0)
        huntersMarkFreeUses = prefs.getInt("huntersMarkFreeUses", 2)
        freeAmuletSpellUsed = prefs.getBoolean("freeAmuletSpellUsed", false)
        freeFaerieFireUsed = prefs.getBoolean("freeFaerieFireUsed", false)
        freeDarknessUsed = prefs.getBoolean("freeDarknessUsed", false)
        freeDruidSpellUsed = prefs.getBoolean("freeDruidSpellUsed", false)
        freeMageArmorUsed = prefs.getBoolean("freeMageArmorUsed", false)
        freeBlessUsed = prefs.getBoolean("freeBlessUsed", false)
        freeMistyStepUsed = prefs.getBoolean("freeMistyStepUsed", false)
        loadSpells()
    }

    // --- ZAUBERPLATZ-FUNKTIONEN ---
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

    fun useSpellSlotLevel4() {
        if (spellSlotsLevel4 > 0) {
            spellSlotsLevel4--
            prefs.edit { putInt("spellSlotsLevel4", spellSlotsLevel4) }
        }
    }

    fun useSpellSlotLevel5() {
        if (spellSlotsLevel5 > 0) {
            spellSlotsLevel5--
            prefs.edit { putInt("spellSlotsLevel5", spellSlotsLevel5) }
        }
    }

    fun resetWarlockSlots() {
        val level = characterVm.level
        spellSlotsLevel1 = characterVm.getMaxSpellSlots(level, 1)
        spellSlotsLevel2 = characterVm.getMaxSpellSlots(level, 2)
        spellSlotsLevel3 = characterVm.getMaxSpellSlots(level, 3)
        spellSlotsLevel4 = characterVm.getMaxSpellSlots(level, 4)
        spellSlotsLevel5 = characterVm.getMaxSpellSlots(level, 5)
        prefs.edit {
            putInt("spellSlotsLevel1", spellSlotsLevel1)
            putInt("spellSlotsLevel2", spellSlotsLevel2)
            putInt("spellSlotsLevel3", spellSlotsLevel3)
            putInt("spellSlotsLevel4", spellSlotsLevel4)
            putInt("spellSlotsLevel5", spellSlotsLevel5)
        }
    }

    fun applyMagicalCunning() {
        val level = characterVm.level
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

    fun useFreeAmuletSpell() {
        if (!freeAmuletSpellUsed) {
            freeAmuletSpellUsed = true
            prefs.edit { putBoolean("freeAmuletSpellUsed", true) }
        }
    }

    fun useFreeFaerieFire() {
        if (!freeFaerieFireUsed) {
            freeFaerieFireUsed = true
            prefs.edit { putBoolean("freeFaerieFireUsed", true) }
        }
    }

    fun useFreeDarkness() {
        if (!freeDarknessUsed) {
            freeDarknessUsed = true
            prefs.edit { putBoolean("freeDarknessUsed", true) }
        }
    }

    fun useFreeDruidSpell() {
        if (!freeDruidSpellUsed) {
            freeDruidSpellUsed = true
            prefs.edit { putBoolean("freeDruidSpellUsed", true) }
        }
    }

    fun useFreeMageArmor() {
        if (!freeMageArmorUsed) {
            freeMageArmorUsed = true
            prefs.edit { putBoolean("freeMageArmorUsed", true) }
        }
    }

    fun useFreeBless() {
        if (!freeBlessUsed) {
            freeBlessUsed = true
            prefs.edit { putBoolean("freeBlessUsed", true) }
        }
    }

    fun useFreeMistyStep() {
        if (!freeMistyStepUsed) {
            freeMistyStepUsed = true
            prefs.edit { putBoolean("freeMistyStepUsed", true) }
        }
    }

    // --- ZAUBER VORBEREITEN ---
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

    fun canCastAsRitual(spell: Spell): Boolean {
        if (!spell.isRitual) return false
        return spell.isPrepared
    }

    fun castAsRitual(spell: Spell) {
        characterVm.snackbarMessage.value = "${spell.name} als Ritual gewirkt (+10 Min) — kein Zauberplatz verbraucht."
    }

    fun castGoodberry() {
        if (spellSlotsLevel1 > 0) {
            spellSlotsLevel1--
            prefs.edit { putInt("spellSlotsLevel1", spellSlotsLevel1) }
            inventoryVm?.changeGoodberries(10)
            characterVm.snackbarMessage.value = "Gute Beeren gewirkt (+10 Beeren)"
        }
    }

    // --- RAST-RESET ---
    fun resetSlotsForLongRest() {
        val level = characterVm.level
        spellSlotsLevel1 = characterVm.getMaxSpellSlots(level, 1)
        spellSlotsLevel2 = characterVm.getMaxSpellSlots(level, 2)
        spellSlotsLevel3 = characterVm.getMaxSpellSlots(level, 3)
        spellSlotsLevel4 = characterVm.getMaxSpellSlots(level, 4)
        spellSlotsLevel5 = characterVm.getMaxSpellSlots(level, 5)
        huntersMarkFreeUses = 2
        freeAmuletSpellUsed = false
        freeFaerieFireUsed = false
        freeDarknessUsed = false
        freeDruidSpellUsed = false
        freeMageArmorUsed = false
        freeBlessUsed = false
        freeMistyStepUsed = false
        saveFreeBooleans()
        prefs.edit {
            putInt("spellSlotsLevel1", spellSlotsLevel1)
            putInt("spellSlotsLevel2", spellSlotsLevel2)
            putInt("spellSlotsLevel3", spellSlotsLevel3)
            putInt("spellSlotsLevel4", spellSlotsLevel4)
            putInt("spellSlotsLevel5", spellSlotsLevel5)
            putInt("huntersMarkFreeUses", huntersMarkFreeUses)
        }
    }

    fun resetSlotsForShortRest() {
        if (characterVm.characterData.charClass == CharacterClass.WARLOCK) {
            resetWarlockSlots()
        }
    }

    private fun saveFreeBooleans() {
        prefs.edit {
            putBoolean("freeAmuletSpellUsed", freeAmuletSpellUsed)
            putBoolean("freeFaerieFireUsed", freeFaerieFireUsed)
            putBoolean("freeDarknessUsed", freeDarknessUsed)
            putBoolean("freeDruidSpellUsed", freeDruidSpellUsed)
            putBoolean("freeMageArmorUsed", freeMageArmorUsed)
            putBoolean("freeBlessUsed", freeBlessUsed)
            putBoolean("freeMistyStepUsed", freeMistyStepUsed)
        }
    }

    // --- ZAUBER SPEICHERN/LADEN ---
    fun saveSpells() {
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
                @Suppress("SENSELESS_COMPARISON")
                val safeItems = items.map { spell ->
                    if (spell.classes == null) spell.copy(classes = emptyList()) else spell
                }
                allSpells.addAll(safeItems)

                // ONE-TIME SPELL SYNC v2
                val syncKeyV2 = "isSyncedWithSpells_2026_03_13_v2"
                val alreadySyncedV2 = prefs.getBoolean(syncKeyV2, false)

                if (!alreadySyncedV2) {
                    val defaultSpells = getDefaultSpells()
                    val defaultNames = defaultSpells.map { it.name }
                    val otherCharId = if (characterVm.characterData.id == "Athania") "Delat" else "Athania"
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

            } catch (e: Exception) {
                Log.w("SpellVM", "Fehler beim Spell-Sync", e)
            }
        } else {
            allSpells.addAll(getDefaultSpells())
            saveSpells()
        }
    }

    private fun getDefaultSpells(): List<Spell> {
        return if (characterVm.characterData.id == "Delat") getDelatDefaultSpells() else getAthaniaDefaultSpells()
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
}
