package com.example.dndcompanion.ui.theme.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

enum class ActiveWeapon {
    LANGBOGEN,
    KURZSCHWERT_SCHILD,
    SHILLELAGH_SCHILD
}

// Eine einfache Datenklasse für beliebigen Loot im Ordner "model"
data class InventoryItem(val name: String, val amount: Int)

class CharacterViewModel : ViewModel() {

    // --- LEBENSPUNKTE & TREFFERWÜRFEL ---
    var currentHp by mutableStateOf(40)
        private set
    val maxHp = 40
    var hitDice by mutableStateOf(4) // 4 Würfel (1W10) auf Level 4
        private set

    // --- ZAUBER & FÄHIGKEITEN ---
    var spellSlotsLevel1 by mutableStateOf(3)
        private set
    var huntersMarkFreeUses by mutableStateOf(2) // 2x gratis wegen Weisheit +2
        private set

    // --- FESTER RUCKSACK ---
    var water by mutableStateOf(2.0f) // Wasserschlauch in 0.5er Schritten
        private set
    var rations by mutableStateOf(10)
        private set
    var goodberries by mutableStateOf(0)
        private set

    // --- FLEXIBLER LOOT ---
    val customLoot = mutableStateListOf<InventoryItem>()

    // --- FUNKTIONEN FÜR DEN RUCKSACK ---
    fun changeWater(amount: Float) {
        water = (water + amount).coerceAtLeast(0f)
    }

    // --- ZAUBER ---
    fun useSpellSlotLevel1() {
        if (spellSlotsLevel1 > 0) spellSlotsLevel1--
    }

    fun useHuntersMarkFree() {
        if (huntersMarkFreeUses > 0) huntersMarkFreeUses--
    }

    fun castGoodberry() {
        if (spellSlotsLevel1 > 0) {
            spellSlotsLevel1--
            goodberries += 10
        }
    }

    // --- KAMPF & WAFFEN ---
    var currentWeapon by mutableStateOf(ActiveWeapon.LANGBOGEN)
        private set

    fun equipWeapon(weapon: ActiveWeapon) {
        currentWeapon = weapon
    }

    // Die Rüstungsklasse berechnet sich automatisch!
    val currentArmorClass: Int
        get() = when (currentWeapon) {
            ActiveWeapon.LANGBOGEN -> 16 // Beschlagene Lederrüstung (12) + GE-Mod (+4)
            ActiveWeapon.KURZSCHWERT_SCHILD -> 18 // +2 durch Schild
            ActiveWeapon.SHILLELAGH_SCHILD -> 18 // +2 durch Schild
        }

    // Angriffsbonus (Treffer)
    val currentAttackBonus: String
        get() = when (currentWeapon) {
            ActiveWeapon.LANGBOGEN -> "+8" // Übungsbonus (+2) + GE (+4) + Kampfstil Bogenschießen (+2)
            ActiveWeapon.KURZSCHWERT_SCHILD -> "+6" // Übungsbonus (+2) + GE (+4)
            ActiveWeapon.SHILLELAGH_SCHILD -> "+4" // Übungsbonus (+2) + WE (+2)
        }

    // Schaden & Effekte
    val currentDamage: String
        get() = when (currentWeapon) {
            ActiveWeapon.LANGBOGEN -> "1W8 + 4 Stich (Gegner -3m Tempo)"
            ActiveWeapon.KURZSCHWERT_SCHILD -> "1W6 + 4 Stich (Vorteil auf nächsten Angriff)"
            ActiveWeapon.SHILLELAGH_SCHILD -> "1W8 + 2 Wucht (Umstoßen: ST-Save 12)"
        }

    fun changeRations(amount: Int) {
        rations = (rations + amount).coerceAtLeast(0)
    }

    fun changeGoodberries(amount: Int) {
        goodberries = (goodberries + amount).coerceAtLeast(0)
    }

    fun addCustomLoot(itemName: String) {
        val index = customLoot.indexOfFirst { it.name.equals(itemName, ignoreCase = true) }
        if (index != -1) {
            // Element existiert: Wir kopieren es mit neuem Wert und ersetzen es an der Position
            val existingItem = customLoot[index]
            customLoot[index] = existingItem.copy(amount = existingItem.amount + 1)
        } else {
            customLoot.add(InventoryItem(itemName, 1))
        }
    }

    fun removeCustomLoot(itemName: String) {
        val index = customLoot.indexOfFirst { it.name.equals(itemName, ignoreCase = true) }
        if (index != -1) {
            val existingItem = customLoot[index]
            if (existingItem.amount > 1) {
                // Wert verringern
                customLoot[index] = existingItem.copy(amount = existingItem.amount - 1)
            } else {
                // Bei 0 komplett löschen
                customLoot.removeAt(index)
            }
        }
    }

    // --- RASTEN ---
    fun takeShortRest() {
        // Kurze Rast: Wir geben 1 Trefferwürfel aus und heilen den Durchschnitt (1W10 + 3 = 8 HP)
        if (hitDice > 0 && currentHp < maxHp) {
            hitDice--
            currentHp = (currentHp + 8).coerceAtMost(maxHp)
        }
    }

    fun takeLongRest() {
        // Alles auffüllen
        spellSlotsLevel1 = 3
        huntersMarkFreeUses = 2
        goodberries = 0

        // HP und Trefferwürfel voll heilen
        currentHp = maxHp
        hitDice = 4

        // Verbrauch pro Tag
        changeWater(-0.5f)
        changeRations(-1)
    }
}