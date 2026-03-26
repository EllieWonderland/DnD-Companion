package com.example.dndcompanion.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RulebookDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRules(rules: List<RuleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWeapons(weapons: List<WeaponEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertArmor(armor: List<ArmorEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTools(tools: List<ToolEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGear(gear: List<GearEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSpecies(species: List<SpeciesEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertClasses(classes: List<ClassEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFeatures(features: List<FeatureEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSpells(spells: List<SpellEntity>)

    // --- Search Queries ---

    @Query("SELECT * FROM rules WHERE title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' OR tags LIKE '%' || :searchQuery || '%'")
    fun searchRules(searchQuery: String): Flow<List<RuleEntity>>

    @Query("SELECT * FROM weapons WHERE name LIKE '%' || :searchQuery || '%' OR category LIKE '%' || :searchQuery || '%' OR properties LIKE '%' || :searchQuery || '%' OR mastery LIKE '%' || :searchQuery || '%'")
    fun searchWeapons(searchQuery: String): Flow<List<WeaponEntity>>

    @Query("SELECT * FROM armor WHERE name LIKE '%' || :searchQuery || '%' OR category LIKE '%' || :searchQuery || '%'")
    fun searchArmor(searchQuery: String): Flow<List<ArmorEntity>>

    @Query("SELECT * FROM tools WHERE name LIKE '%' || :searchQuery || '%' OR category LIKE '%' || :searchQuery || '%'")
    fun searchTools(searchQuery: String): Flow<List<ToolEntity>>

    @Query("SELECT * FROM species WHERE name LIKE '%' || :searchQuery || '%' OR traits LIKE '%' || :searchQuery || '%'")
    fun searchSpecies(searchQuery: String): Flow<List<SpeciesEntity>>
    
    @Query("SELECT * FROM classes WHERE name LIKE '%' || :searchQuery || '%' OR classFeatures LIKE '%' || :searchQuery || '%' OR subclasses LIKE '%' || :searchQuery || '%'")
    fun searchClasses(searchQuery: String): Flow<List<ClassEntity>>

    @Query("SELECT * FROM features WHERE name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%'")
    fun searchFeatures(searchQuery: String): Flow<List<FeatureEntity>>

    @Query("SELECT * FROM spells WHERE name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%'")
    fun searchSpells(searchQuery: String): Flow<List<SpellEntity>>

    // --- Fetch All Queries ---

    @Query("SELECT * FROM rules ORDER BY category, title")
    fun getAllRules(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM weapons ORDER BY category, name")
    fun getAllWeapons(): Flow<List<WeaponEntity>>

    @Query("SELECT * FROM armor ORDER BY category, name")
    fun getAllArmor(): Flow<List<ArmorEntity>>

    @Query("SELECT * FROM tools ORDER BY category, name")
    fun getAllTools(): Flow<List<ToolEntity>>

    @Query("SELECT * FROM gear ORDER BY name")
    fun getAllGear(): Flow<List<GearEntity>>

    @Query("SELECT * FROM gear WHERE name LIKE '%' || :searchQuery || '%'")
    fun searchGear(searchQuery: String): Flow<List<GearEntity>>

    // Suspend queries for one-shot catalog loading
    @Query("SELECT * FROM weapons ORDER BY category, name")
    suspend fun getAllWeaponsList(): List<@JvmSuppressWildcards WeaponEntity>

    @Query("SELECT * FROM armor ORDER BY category, name")
    suspend fun getAllArmorList(): List<@JvmSuppressWildcards ArmorEntity>

    @Query("SELECT * FROM tools ORDER BY category, name")
    suspend fun getAllToolsList(): List<@JvmSuppressWildcards ToolEntity>

    @Query("SELECT * FROM gear ORDER BY name")
    suspend fun getAllGearList(): List<@JvmSuppressWildcards GearEntity>

    @Query("SELECT * FROM species ORDER BY name")
    fun getAllSpecies(): Flow<List<SpeciesEntity>>

    @Query("SELECT * FROM classes ORDER BY name")
    fun getAllClasses(): Flow<List<ClassEntity>>

    @Query("SELECT * FROM features ORDER BY type, name")
    fun getAllFeatures(): Flow<List<FeatureEntity>>

    @Query("SELECT * FROM spells ORDER BY level, name")
    fun getAllSpells(): Flow<List<SpellEntity>>
}
