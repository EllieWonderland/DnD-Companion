package com.example.dndcompanion.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.dndcompanion.data.CharacterClass
import com.example.dndcompanion.data.CharacterData
import com.example.dndcompanion.data.CharacterDto
import com.example.dndcompanion.ui.viewmodel.InventoryItem
import com.example.dndcompanion.ui.viewmodel.TraitItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val charClass: String,
    val race: String,
    val background: String,
    val alignment: String,
    val subclass: String = "",
    val appearance: String = "",
    val languages: String = "",
    val ideal: String = "",
    val flaw: String = "",
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
    val proficientSkillsJson: String,
    val expertiseSkillsJson: String,
    val defaultLootJson: String,
    val defaultTraitsJson: String
)

fun CharacterEntity.toCharacterData(gson: Gson): CharacterData = CharacterData(
    id = id,
    name = name,
    charClass = CharacterClass.valueOf(charClass),
    race = race,
    background = background,
    alignment = alignment,
    subclass = subclass,
    appearance = appearance,
    languages = languages,
    ideal = ideal,
    flaw = flaw,
    speed = speed,
    passivePerception = passivePerception,
    baseLevel = baseLevel,
    baseEP = baseEP,
    baseStrength = baseStrength,
    baseDexterity = baseDexterity,
    baseConstitution = baseConstitution,
    baseIntelligence = baseIntelligence,
    baseWisdom = baseWisdom,
    baseCharisma = baseCharisma,
    baseMaxHp = baseMaxHp,
    baseHitDice = baseHitDice,
    baseSpellSlotsLevel1 = baseSpellSlotsLevel1,
    baseSpellSlotsLevel2 = baseSpellSlotsLevel2,
    baseSpellSlotsLevel3 = baseSpellSlotsLevel3,
    proficientSkills = gson.fromJson(proficientSkillsJson, object : TypeToken<List<String>>() {}.type),
    expertiseSkills = gson.fromJson(expertiseSkillsJson, object : TypeToken<List<String>>() {}.type),
    defaultLoot = gson.fromJson(defaultLootJson, object : TypeToken<List<InventoryItem>>() {}.type),
    defaultTraits = gson.fromJson(defaultTraitsJson, object : TypeToken<List<TraitItem>>() {}.type)
)

fun CharacterData.toEntity(gson: Gson): CharacterEntity = CharacterEntity(
    id = id,
    name = name,
    charClass = charClass.name,
    race = race,
    background = background,
    alignment = alignment,
    subclass = subclass ?: "",
    appearance = appearance ?: "",
    languages = languages ?: "",
    ideal = ideal ?: "",
    flaw = flaw ?: "",
    speed = speed,
    passivePerception = passivePerception,
    baseLevel = baseLevel,
    baseEP = baseEP,
    baseStrength = baseStrength,
    baseDexterity = baseDexterity,
    baseConstitution = baseConstitution,
    baseIntelligence = baseIntelligence,
    baseWisdom = baseWisdom,
    baseCharisma = baseCharisma,
    baseMaxHp = baseMaxHp,
    baseHitDice = baseHitDice,
    baseSpellSlotsLevel1 = baseSpellSlotsLevel1,
    baseSpellSlotsLevel2 = baseSpellSlotsLevel2,
    baseSpellSlotsLevel3 = baseSpellSlotsLevel3,
    proficientSkillsJson = gson.toJson(proficientSkills),
    expertiseSkillsJson = gson.toJson(expertiseSkills),
    defaultLootJson = gson.toJson(defaultLoot),
    defaultTraitsJson = gson.toJson(defaultTraits)
)

/** Used by AppDatabaseCallback to seed directly from DTO without going through CharacterData. */
fun CharacterDto.toEntity(gson: Gson): CharacterEntity = CharacterEntity(
    id = id,
    name = name,
    charClass = charClass,
    race = race,
    background = background,
    alignment = alignment,
    subclass = subclass,
    appearance = appearance,
    languages = languages,
    ideal = ideal,
    flaw = flaw,
    speed = speed,
    passivePerception = passivePerception,
    baseLevel = baseLevel,
    baseEP = baseEP,
    baseStrength = baseStrength,
    baseDexterity = baseDexterity,
    baseConstitution = baseConstitution,
    baseIntelligence = baseIntelligence,
    baseWisdom = baseWisdom,
    baseCharisma = baseCharisma,
    baseMaxHp = baseMaxHp,
    baseHitDice = baseHitDice,
    baseSpellSlotsLevel1 = baseSpellSlotsLevel1,
    baseSpellSlotsLevel2 = baseSpellSlotsLevel2,
    baseSpellSlotsLevel3 = baseSpellSlotsLevel3,
    proficientSkillsJson = gson.toJson(proficientSkills),
    expertiseSkillsJson = gson.toJson(expertiseSkills),
    defaultLootJson = gson.toJson(defaultLoot),
    defaultTraitsJson = gson.toJson(defaultTraits)
)
