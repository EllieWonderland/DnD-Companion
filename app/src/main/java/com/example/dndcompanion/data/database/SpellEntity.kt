package com.example.dndcompanion.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "spells")
data class SpellEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val level: Int,
    val castingTime: String,
    val range: String,
    val duration: String,
    val componentsV: Boolean = false,
    val componentsS: Boolean = false,
    val componentsM: Boolean = false,
    val materialCost: String = "",
    val description: String,
    val classes: List<String> = emptyList(),
    val school: String = "Unbekannt",
    var isRitual: Boolean = false
) {
    fun toSpell(isPrepared: Boolean = false): com.example.dndcompanion.ui.viewmodel.Spell {
        return com.example.dndcompanion.ui.viewmodel.Spell(
            id = id,
            name = name,
            level = level,
            castingTime = castingTime,
            range = range,
            duration = duration,
            componentsV = componentsV,
            componentsS = componentsS,
            componentsM = componentsM,
            materialCost = materialCost,
            description = description,
            classes = classes,
            school = school,
            isPrepared = isPrepared,
            isRitual = isRitual
        )
    }
}
