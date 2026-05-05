package com.example.dndcompanion.data

import com.example.dndcompanion.ui.viewmodel.InventoryItem
import com.example.dndcompanion.ui.viewmodel.TraitItem

enum class CharacterClass {
    RANGER,
    WARLOCK
}

data class CharacterData(
    val id: String,
    val name: String,
    val charClasses: List<CharacterClass>? = null,
    val race: String,
    val background: String,
    val alignment: String,
    val subclass: String? = null,
    val appearance: String? = null,
    val languages: String? = null,
    val ideal: String? = null,
    val flaw: String? = null,
    val portraitUrl: String? = null,
    val speed: Int,
    val passivePerception: Int,
    val baseLevel: Int,
    val baseEP: Int,
    val baseStrength: Int,
    val baseDexterity: Int,
    val baseConstitution: Int,
    val baseIntelligence: Int,
    val baseWisdom: Int,
    val baseCharisma: Int,
    val baseMaxHp: Int,
    val baseHitDice: Int,
    val baseSpellSlotsLevel1: Int,
    val baseSpellSlotsLevel2: Int,
    val baseSpellSlotsLevel3: Int,
    val proficientSkills: List<String>,
    val expertiseSkills: List<String>,
    val defaultLoot: List<InventoryItem>,
    val defaultTraits: List<TraitItem>
) {
    val charClass: CharacterClass get() = charClasses?.firstOrNull() ?: CharacterClass.RANGER
}

// Character definitions live in assets/Rules/characters.json.
// Load them via CharacterRepository — see CharacterRepository.kt.
