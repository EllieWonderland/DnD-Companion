package com.example.dndcompanion.data

import android.content.Context
import com.example.dndcompanion.data.database.AppDatabase
import com.example.dndcompanion.data.database.toCharacterData
import com.example.dndcompanion.data.database.toEntity
import com.example.dndcompanion.ui.viewmodel.InventoryItem
import com.example.dndcompanion.ui.viewmodel.TraitItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DTO matching characters.json — plain fields, no hardcoded logic.
 * Gson populates default values (0 / false / null) for omitted optional fields.
 */
data class CharacterDto(
    val id: String,
    val name: String,
    val charClass: String,
    val race: String,
    val background: String,
    val alignment: String,
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
    val defaultLoot: List<InventoryItem>,
    val defaultTraits: List<TraitItem>
) {
    fun toCharacterData() = CharacterData(
        id = id,
        name = name,
        charClass = CharacterClass.valueOf(charClass),
        race = race,
        background = background,
        alignment = alignment,
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
        proficientSkills = proficientSkills,
        defaultLoot = defaultLoot,
        defaultTraits = defaultTraits
    )
}

/**
 * Single access point for character definitions.
 *
 * Reading priority:
 *   1. DB (persistent — survives level-ups, in-app edits)
 *   2. JSON fallback via [getCharacter] (sync, used for fast initial display and
 *      the very first launch before the DB seed coroutine has completed)
 *
 * Writing: [saveCharacter] persists any in-app changes to the DB.
 */
class CharacterRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val gson = Gson()

    // ----- sync (JSON) -----

    /**
     * Loads the requested character directly from [characters.json].
     * Fast and safe to call on the main thread (small file).
     * Use as the initial value and as a fallback only — DB always wins.
     */
    fun getCharacter(id: String): CharacterData {
        val json = context.assets.open("Rules/characters.json")
            .bufferedReader().use { it.readText() }
        val dtos: List<CharacterDto> = gson.fromJson(
            json, object : TypeToken<List<CharacterDto>>() {}.type
        )
        return dtos.first { it.id == id }.toCharacterData()
    }

    // ----- async (DB) -----

    /**
     * Reactive stream. Emits whenever this character's DB row is updated
     * (e.g. after a level-up saved through the app).
     */
    fun getCharacterFlow(id: String): Flow<CharacterData?> =
        database.characterDao().getFlow(id).map { it?.toCharacterData(gson) }

    /**
     * Persists an in-app character update (level-up, new spell learned, etc.).
     * Call from a coroutine scope.
     */
    suspend fun saveCharacter(data: CharacterData) {
        database.characterDao().save(data.toEntity(gson))
    }
}
