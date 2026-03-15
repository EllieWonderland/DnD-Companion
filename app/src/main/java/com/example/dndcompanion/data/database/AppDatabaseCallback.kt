package com.example.dndcompanion.data.database

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
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

    private suspend fun populateDatabase(dao: RulebookDao, context: Context) {
        val gson = Gson()

        // 1. Load Rules
        try {
            val rulesJson = context.assets.open("Rules/Handbuch/Kapitel/rules.json").bufferedReader().use { it.readText() }
            val rulesList = gson.fromJson(rulesJson, Array<RuleEntity>::class.java).toList()
            dao.insertRules(rulesList)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Load Equipment
        try {
            val equipmentJson = context.assets.open("Rules/Handbuch/Kapitel/equipment.json").bufferedReader().use { it.readText() }
            val equipmentData = gson.fromJson(equipmentJson, EquipmentData::class.java)
            dao.insertWeapons(equipmentData.weapons)
            dao.insertArmor(equipmentData.armor)
            dao.insertTools(equipmentData.tools)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Load Character Options
        try {
            val charOptionsJson = context.assets.open("Rules/Handbuch/Kapitel/character_options.json").bufferedReader().use { it.readText() }
            val charOptionsData = gson.fromJson(charOptionsJson, CharacterOptionsData::class.java)
            dao.insertSpecies(charOptionsData.species)
            dao.insertClasses(charOptionsData.classes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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
