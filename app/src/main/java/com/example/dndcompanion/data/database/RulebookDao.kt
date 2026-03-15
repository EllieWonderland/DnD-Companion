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
    fun insertSpecies(species: List<SpeciesEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertClasses(classes: List<ClassEntity>)

    // --- Search Queries ---

    @Query("SELECT * FROM rules WHERE title LIKE '%' || :searchQuery || '%' OR content LIKE '%' || :searchQuery || '%' OR tags LIKE '%' || :searchQuery || '%'")
    fun searchRules(searchQuery: String): Flow<List<RuleEntity>>

    @Query("SELECT * FROM weapons WHERE name LIKE '%' || :searchQuery || '%' OR category LIKE '%' || :searchQuery || '%'")
    fun searchWeapons(searchQuery: String): Flow<List<WeaponEntity>>

    @Query("SELECT * FROM armor WHERE name LIKE '%' || :searchQuery || '%' OR category LIKE '%' || :searchQuery || '%'")
    fun searchArmor(searchQuery: String): Flow<List<ArmorEntity>>

    @Query("SELECT * FROM tools WHERE name LIKE '%' || :searchQuery || '%' OR category LIKE '%' || :searchQuery || '%'")
    fun searchTools(searchQuery: String): Flow<List<ToolEntity>>

    @Query("SELECT * FROM species WHERE name LIKE '%' || :searchQuery || '%'")
    fun searchSpecies(searchQuery: String): Flow<List<SpeciesEntity>>
    
    @Query("SELECT * FROM classes WHERE name LIKE '%' || :searchQuery || '%'")
    fun searchClasses(searchQuery: String): Flow<List<ClassEntity>>

    // --- Fetch All Queries ---

    @Query("SELECT * FROM rules ORDER BY category, title")
    fun getAllRules(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM weapons ORDER BY category, name")
    fun getAllWeapons(): Flow<List<WeaponEntity>>

    @Query("SELECT * FROM armor ORDER BY category, name")
    fun getAllArmor(): Flow<List<ArmorEntity>>

    @Query("SELECT * FROM tools ORDER BY category, name")
    fun getAllTools(): Flow<List<ToolEntity>>

    @Query("SELECT * FROM species ORDER BY name")
    fun getAllSpecies(): Flow<List<SpeciesEntity>>

    @Query("SELECT * FROM classes ORDER BY name")
    fun getAllClasses(): Flow<List<ClassEntity>>
}
