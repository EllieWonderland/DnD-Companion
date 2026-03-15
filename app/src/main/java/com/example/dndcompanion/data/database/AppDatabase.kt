package com.example.dndcompanion.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        RuleEntity::class, 
        WeaponEntity::class, 
        ArmorEntity::class, 
        ToolEntity::class, 
        SpeciesEntity::class, 
        ClassEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter::class, CharacterOptionConverters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun rulebookDao(): RulebookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dnd_companion_database"
                )
                .addCallback(AppDatabaseCallback(context, kotlinx.coroutines.GlobalScope))
                .build()
                AppDatabaseCallback.attachDatabase(instance)
                INSTANCE = instance
                instance
            }
        }
    }
}
