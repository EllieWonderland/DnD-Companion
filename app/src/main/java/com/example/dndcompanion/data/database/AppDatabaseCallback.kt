package com.example.dndcompanion.data.database

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.dndcompanion.ui.viewmodel.SpellDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppDatabaseCallback(
    private val context: Context,
    private val scope: CoroutineScope
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
            scope.launch(Dispatchers.IO) {
                populateDatabase(database.rulebookDao(), context)
            }
        }
    }

    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
        super.onDestructiveMigration(db)
        INSTANCE?.let { database ->
            scope.launch(Dispatchers.IO) {
                populateDatabase(database.rulebookDao(), context)
            }
        }
    }

    private suspend fun populateDatabase(dao: RulebookDao, context: Context) {
        val gson = Gson()

        // 1. Load Rules
        try {
            val rulesJson = context.assets.open("Rules/Handbuch/Kapitel/rules.json").bufferedReader().use { it.readText() }
            val rulesList = gson.fromJson(rulesJson, Array<RuleEntity>::class.java).toList()
            dao.insertRules(rulesList)
        } catch (e: Exception) {
            Log.e("AppDatabaseCallback", "Failed to load rules.json", e)
        }

        // 2. Load Equipment
        try {
            val equipmentJson = context.assets.open("Rules/Handbuch/Kapitel/equipment.json").bufferedReader().use { it.readText() }
            val equipmentData = gson.fromJson(equipmentJson, EquipmentData::class.java)
            dao.insertWeapons(equipmentData.weapons)
            dao.insertArmor(equipmentData.armor)
            dao.insertTools(equipmentData.tools)
        } catch (e: Exception) {
            Log.e("AppDatabaseCallback", "Failed to load equipment.json", e)
        }

        // 3. Load Character Options
        try {
            val charOptionsJson = context.assets.open("Rules/Handbuch/Kapitel/character_options.json").bufferedReader().use { it.readText() }
            val charOptionsData = gson.fromJson(charOptionsJson, CharacterOptionsData::class.java)
            dao.insertSpecies(charOptionsData.species)
            dao.insertClasses(charOptionsData.classes)
        } catch (e: Exception) {
            Log.e("AppDatabaseCallback", "Failed to load character_options.json", e)
        }

        // 4. Load Features (Merkmale/Talente)
        try {
            val featuresJson = context.assets.open("Rules/merkmale.json").bufferedReader().use { it.readText() }
            val featuresData = gson.fromJson(featuresJson, FeaturesData::class.java)
            dao.insertFeatures(featuresData.features)
        } catch (e: Exception) {
            Log.e("AppDatabaseCallback", "Failed to load merkmale.json", e)
        }

        // 5. Load Spells
        try {
            val spellsJson = context.assets.open("Rules/Zauberbuch/spellbook.json").bufferedReader().use { it.readText() }
            val dtos: List<SpellDto> = gson.fromJson(spellsJson, object : TypeToken<List<SpellDto>>() {}.type)
            val spellEntities = dtos.map { dto ->
                val spell = dto.toSpell()
                SpellEntity(
                    id = spell.id,
                    name = spell.name,
                    level = spell.level,
                    castingTime = spell.castingTime,
                    range = spell.range,
                    duration = spell.duration,
                    componentsV = spell.componentsV,
                    componentsS = spell.componentsS,
                    componentsM = spell.componentsM,
                    materialCost = spell.materialCost,
                    description = spell.description,
                    classes = spell.classes,
                    school = spell.school,
                    isRitual = spell.isRitual
                )
            }
            dao.insertSpells(spellEntities)
        } catch (e: Exception) {
            Log.e("AppDatabaseCallback", "Failed to load spellbook.json", e)
        }
    }

    private data class FeaturesData(
        val features: List<FeatureEntity>
    )

    private data class EquipmentData(
        val weapons: List<WeaponEntity>,
        val armor: List<ArmorEntity>,
        val tools: List<ToolEntity>
    )

    private data class CharacterOptionsData(
        val species: List<SpeciesEntity>,
        val classes: List<ClassEntity>
    )

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun attachDatabase(database: AppDatabase) {
            INSTANCE = database
        }
    }
}
