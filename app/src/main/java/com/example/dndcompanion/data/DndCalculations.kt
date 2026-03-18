package com.example.dndcompanion.data

object DndCalculations {

    fun spellSlotCount(charClass: CharacterClass, level: Int, slotLevel: Int): Int =
        when (charClass) {
            CharacterClass.RANGER -> when (slotLevel) {
                1 -> if (level >= 5) 4 else if (level >= 3) 3 else if (level >= 1) 2 else 0
                2 -> if (level >= 7) 3 else if (level >= 5) 2 else 0
                3 -> if (level >= 11) 3 else if (level >= 9) 2 else 0
                4 -> if (level >= 15) 3 else if (level >= 13) 2 else 0
                5 -> if (level >= 17) 2 else 0
                else -> 0
            }
            CharacterClass.WARLOCK -> {
                val pactSlots = if (level >= 11) 3 else if (level >= 2) 2 else 1
                val pactLevel = if (level >= 9) 5 else if (level >= 7) 4 else if (level >= 5) 3 else if (level >= 3) 2 else 1
                if (slotLevel == pactLevel) pactSlots else 0
            }
        }

    fun proficiencyBonus(level: Int): Int = when (level) {
        in 1..4 -> 2
        in 5..8 -> 3
        in 9..12 -> 4
        in 13..16 -> 5
        else -> 6
    }

    fun maxWeightKg(strength: Int): Double = strength * 7.5

    fun lbToKg(lbs: Double): String = "%.2f".format(lbs * 0.4536)

    fun abilityMod(score: Int): Int = (score - 10).floorDiv(2)
}
