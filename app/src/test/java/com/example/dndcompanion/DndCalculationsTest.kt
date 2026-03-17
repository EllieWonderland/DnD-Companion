package com.example.dndcompanion

import com.example.dndcompanion.data.CharacterClass
import com.example.dndcompanion.data.DndCalculations
import org.junit.Assert.assertEquals
import org.junit.Test

class DndCalculationsTest {

    // --- Ranger Spell Slots ---

    @Test fun ranger_level1_slot1() = assertEquals(2, DndCalculations.spellSlotCount(CharacterClass.RANGER, 1, 1))
    @Test fun ranger_level3_slot1() = assertEquals(3, DndCalculations.spellSlotCount(CharacterClass.RANGER, 3, 1))
    @Test fun ranger_level5_slot1() = assertEquals(4, DndCalculations.spellSlotCount(CharacterClass.RANGER, 5, 1))
    @Test fun ranger_level5_slot2() = assertEquals(2, DndCalculations.spellSlotCount(CharacterClass.RANGER, 5, 2))
    @Test fun ranger_level7_slot2() = assertEquals(3, DndCalculations.spellSlotCount(CharacterClass.RANGER, 7, 2))
    @Test fun ranger_level9_slot3() = assertEquals(2, DndCalculations.spellSlotCount(CharacterClass.RANGER, 9, 3))
    @Test fun ranger_level11_slot3() = assertEquals(3, DndCalculations.spellSlotCount(CharacterClass.RANGER, 11, 3))
    @Test fun ranger_level13_slot4() = assertEquals(2, DndCalculations.spellSlotCount(CharacterClass.RANGER, 13, 4))
    @Test fun ranger_level15_slot4() = assertEquals(3, DndCalculations.spellSlotCount(CharacterClass.RANGER, 15, 4))
    @Test fun ranger_level17_slot5() = assertEquals(2, DndCalculations.spellSlotCount(CharacterClass.RANGER, 17, 5))
    @Test fun ranger_level4_slot2_zero() = assertEquals(0, DndCalculations.spellSlotCount(CharacterClass.RANGER, 4, 2))
    @Test fun ranger_slot6_zero() = assertEquals(0, DndCalculations.spellSlotCount(CharacterClass.RANGER, 20, 6))

    // --- Warlock Pact Slots ---

    // Level 1: 1 slot at level 1
    @Test fun warlock_level1_slot1() = assertEquals(1, DndCalculations.spellSlotCount(CharacterClass.WARLOCK, 1, 1))
    @Test fun warlock_level1_slot2_zero() = assertEquals(0, DndCalculations.spellSlotCount(CharacterClass.WARLOCK, 1, 2))

    // Level 2+: 2 slots still at pact level 1
    @Test fun warlock_level2_slot1() = assertEquals(2, DndCalculations.spellSlotCount(CharacterClass.WARLOCK, 2, 1))

    // Level 3: pact level 2, 2 slots
    @Test fun warlock_level3_slot2() = assertEquals(2, DndCalculations.spellSlotCount(CharacterClass.WARLOCK, 3, 2))
    @Test fun warlock_level3_slot1_zero() = assertEquals(0, DndCalculations.spellSlotCount(CharacterClass.WARLOCK, 3, 1))

    // Level 5: pact level 3, 2 slots
    @Test fun warlock_level5_slot3() = assertEquals(2, DndCalculations.spellSlotCount(CharacterClass.WARLOCK, 5, 3))

    // Level 7: pact level 4, 2 slots
    @Test fun warlock_level7_slot4() = assertEquals(2, DndCalculations.spellSlotCount(CharacterClass.WARLOCK, 7, 4))

    // Level 9: pact level 5, 2 slots
    @Test fun warlock_level9_slot5() = assertEquals(2, DndCalculations.spellSlotCount(CharacterClass.WARLOCK, 9, 5))

    // Level 11+: 3 slots at pact level 5
    @Test fun warlock_level11_slot5() = assertEquals(3, DndCalculations.spellSlotCount(CharacterClass.WARLOCK, 11, 5))

    // --- Proficiency Bonus ---

    @Test fun profBonus_level1() = assertEquals(2, DndCalculations.proficiencyBonus(1))
    @Test fun profBonus_level4() = assertEquals(2, DndCalculations.proficiencyBonus(4))
    @Test fun profBonus_level5() = assertEquals(3, DndCalculations.proficiencyBonus(5))
    @Test fun profBonus_level8() = assertEquals(3, DndCalculations.proficiencyBonus(8))
    @Test fun profBonus_level9() = assertEquals(4, DndCalculations.proficiencyBonus(9))
    @Test fun profBonus_level13() = assertEquals(5, DndCalculations.proficiencyBonus(13))
    @Test fun profBonus_level17() = assertEquals(6, DndCalculations.proficiencyBonus(17))
    @Test fun profBonus_level20() = assertEquals(6, DndCalculations.proficiencyBonus(20))

    // --- Max Weight (STR × 7.5 kg) ---

    @Test fun maxWeight_str10() = assertEquals(75.0, DndCalculations.maxWeightKg(10), 0.001)
    @Test fun maxWeight_str15() = assertEquals(112.5, DndCalculations.maxWeightKg(15), 0.001)
    @Test fun maxWeight_str20() = assertEquals(150.0, DndCalculations.maxWeightKg(20), 0.001)
    @Test fun maxWeight_str8() = assertEquals(60.0, DndCalculations.maxWeightKg(8), 0.001)

    // --- Ability Modifier ---

    @Test fun abilityMod_10() = assertEquals(0, DndCalculations.abilityMod(10))
    @Test fun abilityMod_11() = assertEquals(0, DndCalculations.abilityMod(11))
    @Test fun abilityMod_12() = assertEquals(1, DndCalculations.abilityMod(12))
    @Test fun abilityMod_15() = assertEquals(2, DndCalculations.abilityMod(15))
    @Test fun abilityMod_20() = assertEquals(5, DndCalculations.abilityMod(20))
    @Test fun abilityMod_8() = assertEquals(-1, DndCalculations.abilityMod(8))
    @Test fun abilityMod_7() = assertEquals(-2, DndCalculations.abilityMod(7))
    @Test fun abilityMod_1() = assertEquals(-5, DndCalculations.abilityMod(1))
}
