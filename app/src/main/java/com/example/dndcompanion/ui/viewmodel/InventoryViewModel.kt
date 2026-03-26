package com.example.dndcompanion.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dndcompanion.data.DndCalculations
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.dndcompanion.data.database.AppDatabase

class InventoryViewModel(
    application: Application,
    private val characterVm: CharacterViewModel
) : AndroidViewModel(application) {

    var combatVm: CombatViewModel? = null // set by MainActivity after creation

    private val gson = Gson()

    private val prefs get() = characterVm.prefsManager.prefs

    // --- INVENTAR ---
    val customLoot = mutableStateListOf<InventoryItem>()

    // --- MÜNZEN ---
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

    // --- PFEILE ---
    var totalArrows by mutableIntStateOf(prefs.getInt("totalArrows", 20))
        private set
    var shotArrows by mutableIntStateOf(prefs.getInt("shotArrows", 0))
        private set

    // --- VORRÄTE ---
    var water by mutableFloatStateOf(prefs.getFloat("water", 2.0f))
        private set
    var rations by mutableIntStateOf(prefs.getInt("rations", 10))
        private set
    var goodberries by mutableIntStateOf(prefs.getInt("goodberries", 0))
        private set

    // --- EQUIPMENT-KATALOG ---
    val equipmentCatalog = mutableStateListOf<EquipmentCatalogItem>()

    // --- ABGELEITETE EIGENSCHAFTEN ---
    val maxWeight: Double get() = DndCalculations.maxWeightKg(characterVm.strength)
    val currentWeight: Double
        get() {
            var total = 0.0
            total += water * 2.5
            total += rations * 1.0
            total += totalArrows * 0.02
            total += customLoot.sumOf { it.amount * it.weight }
            return total
        }

    val hasShieldInInventory: Boolean
        get() = customLoot.any { it.name.contains("Schild", ignoreCase = true) }

    val availableWeapons: List<String>
        get() {
            val list = mutableListOf<String>()
            customLoot.forEach { item ->
                if (item.category == "Rüstung & Waffen" &&
                    !item.name.contains("Rüstung", ignoreCase = true) &&
                    !item.name.contains("Schild", ignoreCase = true) &&
                    !item.name.contains("Pfeil", ignoreCase = true) &&
                    !item.name.contains("Köcher", ignoreCase = true) &&
                    !item.name.contains("Munition", ignoreCase = true) &&
                    !item.name.contains("Bolzen", ignoreCase = true) &&
                    !item.name.contains("Peitsche", ignoreCase = true)
                ) {
                    list.add(item.name)
                }
            }
            return list.distinct()
        }

    init {
        viewModelScope.launch {
            characterVm.activeCharacterIdFlow.collect { newId ->
                reloadForCharacter(newId)
            }
        }
        loadLoot()
        viewModelScope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getDatabase(getApplication()).rulebookDao()
            val items = EquipmentCatalogParser.loadFromDb(dao)
            withContext(Dispatchers.Main) {
                if (equipmentCatalog.isEmpty()) equipmentCatalog.addAll(items)
            }
        }
    }

    private fun reloadForCharacter(id: String) {
        coinsKM = prefs.getInt("coinsKM", 0)
        coinsSM = prefs.getInt("coinsSM", 0)
        coinsEM = prefs.getInt("coinsEM", 0)
        coinsGM = prefs.getInt("coinsGM", 0)
        coinsPM = prefs.getInt("coinsPM", 0)
        totalArrows = prefs.getInt("totalArrows", 20)
        shotArrows = prefs.getInt("shotArrows", 0)
        water = prefs.getFloat("water", 2.0f)
        rations = prefs.getInt("rations", 10)
        goodberries = prefs.getInt("goodberries", 0)
        loadLoot()
        cleanupCrossCharacterLoot(id)
    }

    private fun cleanupCrossCharacterLoot(id: String) {
        val cleanupKey = "lootCleanupV3_$id"
        if (prefs.getBoolean(cleanupKey, false)) return
        val otherCharId = if (id == "Athania") "Delat" else "Athania"
        val otherDefaultNames = characterVm.characterRepository.getCharacter(otherCharId).defaultLoot.map { it.name }.toSet()
        val thisDefaultNames = characterVm.characterData.defaultLoot.map { it.name }.toSet()
        val contaminated = customLoot.filter { it.name in otherDefaultNames && it.name !in thisDefaultNames }
        if (contaminated.isNotEmpty()) {
            customLoot.removeAll(contaminated.toSet())
            saveLoot()
        }
        prefs.edit { putBoolean(cleanupKey, true) }
    }

    // --- LOOT SPEICHERN/LADEN ---
    fun saveLoot() {
        val json = gson.toJson(customLoot)
        prefs.edit { putString("customLoot", json) }
    }

    fun loadLoot() {
        val jsonString = prefs.getString("customLoot", "") ?: ""
        if (jsonString.isNotEmpty()) {
            if (jsonString.startsWith("[")) {
                try {
                    val type = object : TypeToken<List<InventoryItem>>() {}.type
                    val items: List<InventoryItem> = gson.fromJson(jsonString, type)
                    customLoot.clear()
                    customLoot.addAll(items)
                } catch (e: Exception) {
                    Log.w("InventoryVM", "Failed to parse custom loot JSON", e)
                    customLoot.clear()
                }
            } else {
                val items = jsonString.split(";").mapNotNull {
                    val parts = it.split("|")
                    if (parts.size == 2) InventoryItem(parts[0], parts[1].toIntOrNull() ?: 1) else null
                }
                customLoot.clear()
                customLoot.addAll(items)
            }
        } else {
            customLoot.clear()
            customLoot.addAll(characterVm.characterData.defaultLoot)
            saveLoot()
        }
    }

    fun addCustomLoot(itemName: String, weight: Double = 0.0, category: String = "Sonstiges") {
        val index = customLoot.indexOfFirst { it.name.equals(itemName, ignoreCase = true) }
        if (index != -1) {
            val existingItem = customLoot[index]
            val newWeight = if (existingItem.weight == 0.0 && weight > 0.0) weight else existingItem.weight
            customLoot[index] = existingItem.copy(amount = existingItem.amount + 1, weight = newWeight)
        } else {
            customLoot.add(InventoryItem(itemName, 1, weight, category))
        }
        saveLoot()
        characterVm.snackbarMessage.value = "$itemName zum Rucksack hinzugefügt"
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

    fun useItemCharge(item: InventoryItem, spellId: String) {
        val cost = item.spellCharges?.get(spellId) ?: 0
        if (item.currentCharges >= cost) {
            item.currentCharges -= cost
            saveLoot()
        }
    }

    // --- MÜNZEN ---
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

    // --- PFEILE ---
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
            val recovered = shotArrows / 2
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

    // --- VORRÄTE ---
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
        if (goodberries > 0) {
            goodberries--
            prefs.edit { putInt("goodberries", goodberries) }
            combatVm?.healManual(1)
        }
    }

}
