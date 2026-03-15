package com.example.dndcompanion.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weapons")
data class WeaponEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val damage: String,
    val properties: List<String>,
    val mastery: String,
    val weightLb: Double,
    val price: String
)

@Entity(tableName = "armor")
data class ArmorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val baseAC: Int,
    val addDexModifier: Boolean,
    val maxDexModifier: Int?,
    val stealthDisadvantage: Boolean,
    val strengthRequirement: Int,
    val weightLb: Double,
    val price: String
)

@Entity(tableName = "tools")
data class ToolEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val weightLb: Double?,
    val price: String
)
