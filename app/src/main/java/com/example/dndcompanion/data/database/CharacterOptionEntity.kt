package com.example.dndcompanion.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "species")
data class SpeciesEntity(
    @PrimaryKey val id: String,
    val name: String,
    val size: String,
    val speed: Double,
    val traits: List<Trait>
)

data class Trait(
    val name: String,
    val description: String
)

@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey val id: String,
    val name: String,
    val primaryAbility: String,
    val hitDie: String,
    val savingThrows: List<String>,
    val classFeatures: List<ClassFeature>,
    val subclasses: List<Subclass>
)

data class ClassFeature(
    val level: Int,
    val name: String,
    val description: String
)

data class Subclass(
    val id: String,
    val name: String,
    val features: List<ClassFeature>
)

class CharacterOptionConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromTraitList(list: List<Trait>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toTraitList(data: String?): List<Trait>? {
        if (data == null) return null
        val type = object : TypeToken<List<Trait>>() {}.type
        return gson.fromJson(data, type)
    }

    @TypeConverter
    fun fromClassFeatureList(list: List<ClassFeature>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toClassFeatureList(data: String?): List<ClassFeature>? {
        if (data == null) return null
        val type = object : TypeToken<List<ClassFeature>>() {}.type
        return gson.fromJson(data, type)
    }

    @TypeConverter
    fun fromSubclassList(list: List<Subclass>?): String? {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toSubclassList(data: String?): List<Subclass>? {
        if (data == null) return null
        val type = object : TypeToken<List<Subclass>>() {}.type
        return gson.fromJson(data, type)
    }
}
