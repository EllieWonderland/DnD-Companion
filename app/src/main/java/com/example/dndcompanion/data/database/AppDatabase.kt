package com.example.dndcompanion.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@Database(
    entities = [
        RuleEntity::class, 
        WeaponEntity::class, 
        ArmorEntity::class, 
        ToolEntity::class, 
        SpeciesEntity::class, 
        ClassEntity::class,
        FeatureEntity::class,
        SpellEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(StringListConverter::class, CharacterOptionConverters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun rulebookDao(): RulebookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Application-scoped coroutine scope: lives as long as the process, no leaks
        private val applicationScope = CoroutineScope(SupervisorJob())

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dnd_companion_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(context, applicationScope))
                .build()
                AppDatabaseCallback.attachDatabase(instance)
                INSTANCE = instance
                instance
            }
        }
    }
}
