package com.example.dndcompanion.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dndcompanion.data.CharacterClass
import kotlinx.coroutines.launch

class CombatViewModel(
    application: Application,
    private val characterVm: CharacterViewModel,
    private val spellVm: SpellViewModel,
    val inventoryVm: InventoryViewModel
) : AndroidViewModel(application) {

    private val prefs get() = characterVm.prefsManager.prefs

    // --- LEBENSPUNKTE ---
    var maxHp by mutableIntStateOf(characterVm.maxHp)
        private set
    var currentHp by mutableIntStateOf(prefs.getInt("currentHp", characterVm.maxHp))
        private set
    var tempHp by mutableIntStateOf(prefs.getInt("tempHp", 0))
        private set
    var hitDice by mutableIntStateOf(prefs.getInt("hitDice", characterVm.level))
        private set

    // --- TODESRETTUNGSWÜRFE ---
    var deathSaveSuccesses by mutableIntStateOf(prefs.getInt("deathSaveSuccesses", 0))
        private set
    var deathSaveFailures by mutableIntStateOf(prefs.getInt("deathSaveFailures", 0))
        private set

    // --- INSPIRATION ---
    var heroicInspiration by mutableStateOf(prefs.getBoolean("heroicInspiration_${characterVm.activeCharacterIdFlow.value}", false))
        private set

    // --- KAMPF-TOGGLES ---
    var isUsingTwoHanded by mutableStateOf(prefs.getBoolean("isUsingTwoHanded", false))
        private set
    var isMageArmorActive by mutableStateOf(prefs.getBoolean("isMageArmorActive", false))
        private set
    var isShieldEquipped by mutableStateOf(prefs.getBoolean("isShieldEquipped", false))
        private set

    // --- WAFFE ---
    private val savedWeaponName = prefs.getString("currentWeapon", ActiveWeapon.LANGBOGEN.name) ?: ActiveWeapon.LANGBOGEN.name
    var currentWeapon by mutableStateOf(ActiveWeapon.valueOf(savedWeaponName))
        private set

    var equippedWeaponName by mutableStateOf(prefs.getString("equippedWeaponName", null) ?: "Keine Waffe")
        private set

    // --- RAST-WARNDIALOG ---
    var showRestWarningDialog by mutableStateOf(false)
        private set

    // --- BEGLEITER ---
    var activeBeastType by mutableStateOf(BeastType.valueOf(prefs.getString("activeBeastType", BeastType.SKY.name) ?: BeastType.SKY.name))
        private set
    var capyCurrentHp by mutableIntStateOf(
        prefs.getInt(
            "capyCurrentHp_${characterVm.characterData.id}_${prefs.getString("activeBeastType", BeastType.SKY.name) ?: BeastType.SKY.name}",
            prefs.getInt("capyCurrentHp_${characterVm.characterData.id}", 20)
        )
    )
        private set
    var companionIsDead by mutableStateOf(
        prefs.getBoolean(
            "companionIsDead_${characterVm.characterData.id}_${prefs.getString("activeBeastType", BeastType.SKY.name) ?: BeastType.SKY.name}",
            prefs.getBoolean("companionIsDead_${characterVm.characterData.id}", false)
        )
    )

    val companionData: CompanionDto? get() = characterVm.companionData

    init {
        viewModelScope.launch {
            characterVm.activeCharacterIdFlow.collect { newId ->
                reloadForCharacter(newId)
            }
        }
        loadCompanion()
    }

    private fun reloadForCharacter(id: String) {
        maxHp = characterVm.maxHp
        currentHp = prefs.getInt("currentHp", maxHp)
        tempHp = prefs.getInt("${id}_tempHp", if (id == "Delat") 12 else 0)
        hitDice = prefs.getInt("hitDice", characterVm.level).coerceAtMost(characterVm.level)
        deathSaveSuccesses = prefs.getInt("deathSaveSuccesses", 0)
        deathSaveFailures = prefs.getInt("deathSaveFailures", 0)
        heroicInspiration = prefs.getBoolean("heroicInspiration_${id}", false)
        isUsingTwoHanded = prefs.getBoolean("isUsingTwoHanded", false)
        isMageArmorActive = prefs.getBoolean("isMageArmorActive", false)
        isShieldEquipped = prefs.getBoolean("isShieldEquipped", false)
        val defaultWeapon = if (characterVm.characterData.charClass == CharacterClass.RANGER) ActiveWeapon.LANGBOGEN.name else ActiveWeapon.SPEER_PAKT.name
        val wn = prefs.getString("currentWeapon_${id}", defaultWeapon) ?: defaultWeapon
        currentWeapon = ActiveWeapon.valueOf(wn)
        equippedWeaponName = prefs.getString("equippedWeaponName_${id}", null) ?: "Keine Waffe"
        activeBeastType = BeastType.valueOf(prefs.getString("activeBeastType", BeastType.SKY.name) ?: BeastType.SKY.name)
        capyCurrentHp = prefs.getInt("capyCurrentHp_${id}_${activeBeastType.name}", capyMaxHp)
        companionIsDead = prefs.getBoolean("companionIsDead_${id}_${activeBeastType.name}", false)
        loadCompanion()
    }

    // --- ABGELEITETE EIGENSCHAFTEN ---
    val effectiveDexMod: Int get() = characterVm.dexMod + if (isMageArmorActive) 1 else 0

    val currentArmorClass: Int
        get() {
            var baseAc = if (isMageArmorActive) 13 else 10
            var armorBonus = 0
            if (inventoryVm.customLoot.any { it.name.contains("Plattenpanzer", ignoreCase = true) }) {
                baseAc = 18
            } else if (inventoryVm.customLoot.any { it.name.contains("Kettenpanzer", ignoreCase = true) }) {
                baseAc = 16
            } else if (inventoryVm.customLoot.any { it.name.contains("Brustpanzer", ignoreCase = true) }) {
                baseAc = 14
                armorBonus = effectiveDexMod.coerceAtMost(2)
            } else if (inventoryVm.customLoot.any { it.name.contains("Schuppenpanzer", ignoreCase = true) }) {
                baseAc = 14
                armorBonus = effectiveDexMod.coerceAtMost(2)
            } else if (inventoryVm.customLoot.any { it.name.contains("Beschlagene Lederrüstung", ignoreCase = true) }) {
                baseAc = 12
                armorBonus = effectiveDexMod
            } else if (inventoryVm.customLoot.any { it.name.contains("Lederrüstung", ignoreCase = true) }) {
                baseAc = 11
                armorBonus = effectiveDexMod
            } else {
                armorBonus = effectiveDexMod
            }

            var ac = baseAc + armorBonus
            if (isShieldEquipped && !isUsingTwoHanded && inventoryVm.hasShieldInInventory) {
                ac += 2
            }
            return ac
        }

    // Finesse-Waffe: verwende den höheren Wert aus STR und DEX
    private val finesseWeaponMod: Int get() = maxOf(characterVm.strMod, characterVm.dexMod)

    val currentAttackBonus: String
        get() = when (currentWeapon) {
            ActiveWeapon.LANGBOGEN -> "+${characterVm.proficiencyBonus + characterVm.dexMod + 2}"
            ActiveWeapon.KURZSCHWERT_SCHILD -> "+${characterVm.proficiencyBonus + finesseWeaponMod}"
            ActiveWeapon.SHILLELAGH_SCHILD -> "+${characterVm.proficiencyBonus + characterVm.wisMod}"
            ActiveWeapon.KRIEGSHAMMER_PAKT -> "+${characterVm.proficiencyBonus + characterVm.strMod}"
            ActiveWeapon.SPEER_PAKT -> "+${characterVm.proficiencyBonus + characterVm.strMod}"
        }

    val currentDamage: String
        get() = when (currentWeapon) {
            ActiveWeapon.LANGBOGEN -> "1W8 + ${characterVm.dexMod} Stich (Verlangsamen: -3m Tempo)"
            ActiveWeapon.KURZSCHWERT_SCHILD -> "1W6 + $finesseWeaponMod Stich (Ärgern: Vorteil auf nächsten Angriff)"
            ActiveWeapon.SHILLELAGH_SCHILD -> {
                val die = if (isUsingTwoHanded) "1W10" else "1W8"
                "$die + ${characterVm.wisMod} Wucht (Umwerfen: KON-Save SG 12)"
            }
            ActiveWeapon.KRIEGSHAMMER_PAKT -> {
                val die = if (isUsingTwoHanded) "1W10" else "1W8"
                "$die + ${characterVm.strMod} Wucht (Stoß: bis zu 3m wegstoßen)"
            }
            ActiveWeapon.SPEER_PAKT -> {
                val die = if (isUsingTwoHanded) "1W8" else "1W6"
                "$die + ${characterVm.strMod} Stich (Schwächen: Gegner hat Nachteil auf nächsten Angriff)"
            }
        }

    val initiative: Int get() = characterVm.dexMod

    val hasShieldInInventory: Boolean get() = inventoryVm.hasShieldInInventory
    val availableWeapons: List<String> get() = inventoryVm.availableWeapons

    private val isRanger: Boolean get() = characterVm.characterData.charClass == CharacterClass.RANGER

    val capyMaxHp: Int get() {
        if (isRanger) {
            val dto = companionData
            if (dto != null && dto.hpBasis > 0) {
                return dto.hpBasis + dto.hpStufenMult * characterVm.level
            }
            // Fallback: Land=5+5×level, Sky/Sea=4+4×level
            val base = if (activeBeastType == BeastType.LAND) 5 else 4
            val mult = if (activeBeastType == BeastType.LAND) 5 else 4
            return base + (mult * characterVm.level)
        }
        return 24
    }

    val capyAc: Int get() {
        if (isRanger) {
            val dto = companionData
            val base = if (dto != null && dto.rkBasis > 0) dto.rkBasis else 13
            return base + characterVm.wisMod
        }
        return 13
    }
    val capyAttackBonus: String get() = if (isRanger) "+${characterVm.spellAttackBonus}" else "+5"
    val capyDamage: String get() = if (isRanger) {
        if (activeBeastType == BeastType.SKY) "1W4 + 3 + ${characterVm.wisMod} Hieb" else if (activeBeastType == BeastType.SEA) "1W6 + 2 + ${characterVm.wisMod} Stich" else "1W8 + 2 + ${characterVm.wisMod} Hieb"
    } else "1W4+3 Hieb + 2W6 Gleißend"
    val capySpeed: String get() = companionData?.bewegungsrate?.entries?.joinToString(", ") { "${it.key}: ${it.value}" } ?: ""
    val capySpecial: String get() = companionData?.merkmale?.joinToString("\n") { "${it.name}: ${it.beschreibung}" } ?: ""

    // --- SCHADENSFUNKTIONEN ---
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
        prefs.edit { putBoolean("heroicInspiration_${characterVm.activeCharacterId}", active) }
    }

    fun toggleTwoHanded(active: Boolean) {
        isUsingTwoHanded = active
        prefs.edit { putBoolean("isUsingTwoHanded", isUsingTwoHanded) }
    }

    fun toggleMageArmor(active: Boolean) {
        isMageArmorActive = active
        prefs.edit { putBoolean("isMageArmorActive", isMageArmorActive) }
    }

    fun toggleShield(active: Boolean) {
        isShieldEquipped = active
        prefs.edit { putBoolean("isShieldEquipped", isShieldEquipped) }
    }

    fun equipWeaponByName(name: String) {
        equippedWeaponName = name
        val weaponStr = name.lowercase()
        currentWeapon = when {
            weaponStr.contains("bogen") -> ActiveWeapon.LANGBOGEN
            weaponStr.contains("schwert") -> ActiveWeapon.KURZSCHWERT_SCHILD
            weaponStr.contains("hammer") -> ActiveWeapon.KRIEGSHAMMER_PAKT
            weaponStr.contains("speer") -> ActiveWeapon.SPEER_PAKT
            weaponStr.contains("shillelagh") || weaponStr.contains("kampfstab") -> ActiveWeapon.SHILLELAGH_SCHILD
            else -> if (characterVm.characterData.charClass == CharacterClass.RANGER) ActiveWeapon.KURZSCHWERT_SCHILD else ActiveWeapon.SPEER_PAKT
        }
        prefs.edit {
            putString("equippedWeaponName_${characterVm.activeCharacterId}", name)
            putString("currentWeapon_${characterVm.activeCharacterId}", currentWeapon.name)
        }
    }

    fun getWeaponName(index: Int): String {
        return prefs.getString("weaponName_$index", null) ?: when {
            characterVm.characterData.charClass == CharacterClass.RANGER -> {
                when (index) {
                    0 -> "Langbogen"
                    1 -> "Kurzschwert\n& Schild"
                    else -> "Shillelagh\n& Schild"
                }
            }
            else -> {
                when (index) {
                    0 -> "Kriegshammer\n(Pakt)"
                    else -> "Speer\n(Pakt)"
                }
            }
        }
    }

    fun saveWeaponName(index: Int, name: String) {
        prefs.edit { putString("weaponName_$index", name) }
    }

    fun applyHpIncrease(conModifier: Int, rolledHp: Int = 6) {
        var hpIncrease = rolledHp + conModifier
        if (characterVm.characterData.id == "Delat") {
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

    fun applyFalseLife() {
        tempHp = 12
        prefs.edit { putInt("tempHp", tempHp) }
    }

    // --- RASTEN ---
    fun takeShortRest(hitDiceSpent: Int, rolledValue: Int) {
        if (hitDiceSpent <= hitDice && currentHp < maxHp) {
            hitDice -= hitDiceSpent
            val healAmount = rolledValue + (characterVm.conMod * hitDiceSpent)
            currentHp = (currentHp + healAmount).coerceAtMost(maxHp)
            prefs.edit {
                putInt("hitDice", hitDice)
                putInt("currentHp", currentHp)
            }
        }
        spellVm.resetSlotsForShortRest()
        characterVm.resetTraitsForShortRest()
    }

    fun dismissRestWarningDialog() {
        showRestWarningDialog = false
    }

    fun attemptLongRest() {
        if (inventoryVm.water < 0.5f || inventoryVm.rations < 1) {
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
        val recoveredHitDice = (characterVm.level / 2).coerceAtLeast(1)
        hitDice = (hitDice + recoveredHitDice).coerceAtMost(characterVm.level)

        spellVm.resetSlotsForLongRest()
        characterVm.resetTraitsForLongRest()

        // Reset goodberries to 0 on long rest
        if (inventoryVm.goodberries > 0) {
            inventoryVm.changeGoodberries(-inventoryVm.goodberries)
        }
        updateDeathSaves(0, 0)

        if (companionIsDead) {
            reviveCompanion()
        }

        if (consumeResources) {
            inventoryVm.changeWater(-0.5f)
            inventoryVm.changeRations(-1)
        }

        prefs.edit {
            putInt("currentHp", currentHp)
            putInt("hitDice", hitDice)
        }
    }

    // --- BEGLEITER ---
    fun loadCompanion() {
        characterVm.activeBeastType = activeBeastType
        characterVm.loadCompanion()
    }

    fun toggleBeastType(type: BeastType) {
        prefs.edit {
            putInt("capyCurrentHp_${characterVm.characterData.id}_${activeBeastType.name}", capyCurrentHp)
            putBoolean("companionIsDead_${characterVm.characterData.id}_${activeBeastType.name}", companionIsDead)
        }

        activeBeastType = type
        loadCompanion()

        val maxHp = capyMaxHp
        capyCurrentHp = prefs.getInt("capyCurrentHp_${characterVm.characterData.id}_${activeBeastType.name}", maxHp)
        companionIsDead = prefs.getBoolean("companionIsDead_${characterVm.characterData.id}_${activeBeastType.name}", false)

        if (capyCurrentHp > maxHp) capyCurrentHp = maxHp

        prefs.edit {
            putString("activeBeastType", activeBeastType.name)
            putInt("capyCurrentHp_${characterVm.characterData.id}_${activeBeastType.name}", capyCurrentHp)
            putBoolean("companionIsDead_${characterVm.characterData.id}_${activeBeastType.name}", companionIsDead)
        }
    }

    fun takeCapyDamage(amount: Int) {
        capyCurrentHp = (capyCurrentHp - amount).coerceAtLeast(0)
        if (capyCurrentHp == 0) companionIsDead = true
        prefs.edit {
            putInt("capyCurrentHp_${characterVm.characterData.id}_${activeBeastType.name}", capyCurrentHp)
            putBoolean("companionIsDead_${characterVm.characterData.id}_${activeBeastType.name}", companionIsDead)
        }
    }

    fun healCapy(amount: Int) {
        capyCurrentHp = (capyCurrentHp + amount).coerceAtMost(capyMaxHp)
        if (capyCurrentHp > 0) companionIsDead = false
        prefs.edit {
            putInt("capyCurrentHp_${characterVm.characterData.id}_${activeBeastType.name}", capyCurrentHp)
            putBoolean("companionIsDead_${characterVm.characterData.id}_${activeBeastType.name}", companionIsDead)
        }
    }

    fun reviveCompanion() {
        capyCurrentHp = capyMaxHp
        companionIsDead = false
        prefs.edit {
            putInt("capyCurrentHp_${characterVm.characterData.id}_${activeBeastType.name}", capyCurrentHp)
            putBoolean("companionIsDead_${characterVm.characterData.id}_${activeBeastType.name}", companionIsDead)
        }
    }

    fun castGoodberry() {
        if (spellVm.spellSlotsLevel1 > 0) {
            spellVm.useSpellSlotLevel1()
            inventoryVm.changeGoodberries(10)
            characterVm.snackbarMessage.value = "Gute Beeren gewirkt (+10 Beeren)"
        }
    }
}
