package com.example.dndcompanion.data

import android.content.Context
import com.example.dndcompanion.data.database.AppDatabase
import com.example.dndcompanion.data.database.toCharacterData
import com.example.dndcompanion.data.database.toEntity
import com.example.dndcompanion.ui.viewmodel.InventoryItem
import com.example.dndcompanion.ui.viewmodel.TraitItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
    val proficientSkills: List<String>,
    val expertiseSkills: List<String> = emptyList(),
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
        proficientSkills = proficientSkills,
        expertiseSkills = expertiseSkills,
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
    private val firestore = FirebaseFirestore.getInstance()

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

    /**
     * Tries to load a character by [id]; falls back to Athania if not found.
     * Allows loading with a Firebase UID as id (not present in characters.json).
     */
    fun getCharacterOrDefault(id: String): CharacterData {
        return try {
            getCharacter(id)
        } catch (e: Exception) {
            getCharacter("Athania").copy(id = id)
        }
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

    // ----- Firestore (per-user character) -----

    /**
     * Reactive stream from Firestore. Emits null if no document exists for this user yet.
     * The character is stored as Gson JSON under users/{uid}/character/main.
     */
    fun getCharacterFlowFromFirestore(uid: String): Flow<CharacterData?> = callbackFlow {
        val docRef = firestore.collection("users").document(uid)
            .collection("character").document("main")
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val json = snapshot.getString("data")
                if (json != null) {
                    try {
                        trySend(gson.fromJson(json, CharacterData::class.java))
                    } catch (e: Exception) {
                        trySend(null)
                    }
                } else {
                    trySend(null)
                }
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    /**
     * Writes the character to Firestore under users/{uid}/character/main.
     * Call from a coroutine scope.
     */
    suspend fun saveCharacterToFirestore(uid: String, data: CharacterData) {
        val json = gson.toJson(data)
        suspendCancellableCoroutine { cont ->
            firestore.collection("users").document(uid)
                .collection("character").document("main")
                .set(mapOf("data" to json))
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
    }
}
