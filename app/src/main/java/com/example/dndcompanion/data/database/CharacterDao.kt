package com.example.dndcompanion.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {

    /** Seed-only insert: never overwrites existing in-app edits. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(characters: List<CharacterEntity>)

    /** Write a character update from the app (level-up, new spell, etc.). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(character: CharacterEntity)

    /** Reactive stream — emits whenever this character row changes. */
    @Query("SELECT * FROM characters WHERE id = :id")
    fun getFlow(id: String): Flow<CharacterEntity?>

    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun get(id: String): CharacterEntity?

    @Query("SELECT * FROM characters")
    fun getAllFlow(): Flow<List<CharacterEntity>>
}
