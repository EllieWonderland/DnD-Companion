package com.example.dndcompanion.ui.theme.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

// --- DATENKLASSEN & ENUMS ---
data class InventoryItem(val name: String, val amount: Int)

enum class ActiveWeapon {
    LANGBOGEN,
    KURZSCHWERT_SCHILD,
    SHILLELAGH_SCHILD
}

// Wir erben nun von AndroidViewModel, um Zugriff auf den Handyspeicher zu bekommen
class CharacterViewModel(application: Application) : AndroidViewModel(application) {

    // Die Verbindung zum unsichtbaren Speicher deines Handys
    private val prefs = application.getSharedPreferences("AthaniaSaveGame", Context.MODE_PRIVATE)

    // --- BASISWERTE & MODIFIKATOREN (Athania) ---
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

    // --- LEBENSPUNKTE & TREFFERWÜRFEL ---
    val maxHp = 40

    // Wir laden den Wert beim Start der App aus dem Speicher (oder nehmen 40, falls er nicht existiert)
    var currentHp by mutableStateOf(prefs.getInt("currentHp", maxHp))
        private set
    var hitDice by mutableStateOf(prefs.getInt("hitDice", 4))
        private set

    fun takeDamage(amount: Int) {
        currentHp = (currentHp - amount).coerceAtLeast(0)
        prefs.edit().putInt("currentHp", currentHp).apply() // Direkt speichern!
    }

    fun healManual(amount: Int) {
        currentHp = (currentHp + amount).coerceAtMost(maxHp)
        prefs.edit().putInt("currentHp", currentHp).apply()
    }

    // --- KAMPF & WAFFEN ---
    // Enum als Text speichern und laden
    private val savedWeaponName = prefs.getString("currentWeapon", ActiveWeapon.LANGBOGEN.name) ?: ActiveWeapon.LANGBOGEN.name
    var currentWeapon by mutableStateOf(ActiveWeapon.valueOf(savedWeaponName))
        private set

    fun equipWeapon(weapon: ActiveWeapon) {
        currentWeapon = weapon
        prefs.edit().putString("currentWeapon", weapon.name).apply()
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

    // --- ZAUBER & FÄHIGKEITEN ---
    var spellSlotsLevel1 by mutableStateOf(prefs.getInt("spellSlotsLevel1", 3))
        private set
    var huntersMarkFreeUses by mutableStateOf(prefs.getInt("huntersMarkFreeUses", 2))
        private set

    fun useSpellSlotLevel1() {
        if (spellSlotsLevel1 > 0) {
            spellSlotsLevel1--
            prefs.edit().putInt("spellSlotsLevel1", spellSlotsLevel1).apply()
        }
    }

    fun useHuntersMarkFree() {
        if (huntersMarkFreeUses > 0) {
            huntersMarkFreeUses--
            prefs.edit().putInt("huntersMarkFreeUses", huntersMarkFreeUses).apply()
        }
    }

    fun castGoodberry() {
        if (spellSlotsLevel1 > 0) {
            spellSlotsLevel1--
            goodberries += 10
            prefs.edit()
                .putInt("spellSlotsLevel1", spellSlotsLevel1)
                .putInt("goodberries", goodberries)
                .apply()
        }
    }

    // --- FESTER RUCKSACK ---
    var water by mutableStateOf(prefs.getFloat("water", 2.0f))
        private set
    var rations by mutableStateOf(prefs.getInt("rations", 10))
        private set
    var goodberries by mutableStateOf(prefs.getInt("goodberries", 0))
        private set

    fun changeWater(amount: Float) {
        water = (water + amount).coerceAtLeast(0f)
        prefs.edit().putFloat("water", water).apply()
    }

    fun changeRations(amount: Int) {
        rations = (rations + amount).coerceAtLeast(0)
        prefs.edit().putInt("rations", rations).apply()
    }

    fun changeGoodberries(amount: Int) {
        goodberries = (goodberries + amount).coerceAtLeast(0)
        prefs.edit().putInt("goodberries", goodberries).apply()
    }

    // --- FLEXIBLER LOOT ---
    val customLoot = mutableStateListOf<InventoryItem>()

    init {
        // Lädt den Loot direkt beim Start der App
        loadLoot()
    }

    private fun saveLoot() {
        val lootString = customLoot.joinToString(";") { "${it.name}|${it.amount}" }
        prefs.edit().putString("customLoot", lootString).apply()
    }

    private fun loadLoot() {
        val lootString = prefs.getString("customLoot", "") ?: ""
        if (lootString.isNotEmpty()) {
            val items = lootString.split(";").mapNotNull {
                val parts = it.split("|")
                if (parts.size == 2) InventoryItem(parts[0], parts[1].toIntOrNull() ?: 1) else null
            }
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
        saveLoot() // Liste nach jeder Änderung speichern
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

    // --- RASTEN ---
    fun takeShortRest(rolledValue: Int) {
        if (hitDice > 0 && currentHp < maxHp) {
            hitDice--
            val healAmount = rolledValue + conMod
            currentHp = (currentHp + healAmount).coerceAtMost(maxHp)

            prefs.edit()
                .putInt("hitDice", hitDice)
                .putInt("currentHp", currentHp)
                .apply()
        }
    }

    fun takeLongRest() {
        spellSlotsLevel1 = 3
        huntersMarkFreeUses = 2
        goodberries = 0

        currentHp = maxHp
        hitDice = 4

        changeWater(-0.5f)
        changeRations(-1)

        prefs.edit()
            .putInt("spellSlotsLevel1", spellSlotsLevel1)
            .putInt("huntersMarkFreeUses", huntersMarkFreeUses)
            .putInt("goodberries", goodberries)
            .putInt("currentHp", currentHp)
            .putInt("hitDice", hitDice)
            .apply()
    }

    // --- CAPY (URTIER) ---
    var isSkyBeast by mutableStateOf(prefs.getBoolean("isSkyBeast", true))
        private set

    val capyMaxHp: Int get() = if (isSkyBeast) 4 + (4 * level) else 5 + (5 * level)

    var capyCurrentHp by mutableStateOf(prefs.getInt("capyCurrentHp", 20))
        private set

    fun toggleBeastType(isSky: Boolean) {
        isSkyBeast = isSky
        if (capyCurrentHp > capyMaxHp) capyCurrentHp = capyMaxHp

        prefs.edit()
            .putBoolean("isSkyBeast", isSkyBeast)
            .putInt("capyCurrentHp", capyCurrentHp)
            .apply()
    }

    fun takeCapyDamage(amount: Int) {
        capyCurrentHp = (capyCurrentHp - amount).coerceAtLeast(0)
        prefs.edit().putInt("capyCurrentHp", capyCurrentHp).apply()
    }

    fun healCapy(amount: Int) {
        capyCurrentHp = (capyCurrentHp + amount).coerceAtMost(capyMaxHp)
        prefs.edit().putInt("capyCurrentHp", capyCurrentHp).apply()
    }

    val capyAc: Int get() = 13 + proficiencyBonus
    val capyAttackBonus: String get() = "+$spellAttackBonus"
    val capyDamage: String get() = if (isSkyBeast) "1W4 + $wisMod Hieb" else "1W8 + $wisMod Hieb"
    val capySpeed: String get() = if (isSkyBeast) "Fliegen 18 m, Laufen 3 m" else "Laufen 12 m, Klettern 12 m"
    val capySpecial: String get() = if (isSkyBeast) "Vorbeifliegen (keine Gelegenheitsangriffe)" else "Ansturm (Gegner umstoßen, ST-Save)"
}