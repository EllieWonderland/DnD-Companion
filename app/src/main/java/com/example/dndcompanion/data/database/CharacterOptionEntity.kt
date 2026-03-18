package com.example.dndcompanion.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
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
    @SerializedName("features") val classFeatures: List<ClassFeature>,
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

@Entity(tableName = "features")
data class FeatureEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // FEAT, RACIAL_TRAIT, CLASS_FEATURE, SUBCLASS_FEATURE
    val category: String?, // e.g. "Origin Feat", "Fighting Style"
    val raceReq: List<String>?, // e.g. ["Elf"]
    val classReq: List<String>?, // e.g. ["Waldläufer"]
    val levelReq: Int,
    val description: String,
    // Spell-grant metadata — replaces heuristic string matching in learnFeature()
    @SerializedName("granted_spell_id")   val grantedSpellId: String? = null,
    @SerializedName("granted_spell_uses") val grantedSpellUses: Int = 0,
    @SerializedName("granted_spell_ids")  val grantedSpellIds: List<String>? = null
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
