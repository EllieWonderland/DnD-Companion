package com.example.dndcompanion.data

import com.example.dndcompanion.ui.viewmodel.InventoryItem
import com.example.dndcompanion.ui.viewmodel.TraitItem

enum class CharacterClass {
    RANGER,
    WARLOCK
}

data class CharacterData(
    val id: String,
    val name: String,
    val charClass: CharacterClass,
    val baseLevel: Int,
    val baseEP: Int,
    val baseStrength: Int,
    val baseDexterity: Int,
    val baseConstitution: Int,
    val baseIntelligence: Int,
    val baseWisdom: Int,
    val baseCharisma: Int,
    val baseMaxHp: Int,
    val baseHitDice: Int,
    val baseSpellSlotsLevel1: Int,
    val baseSpellSlotsLevel2: Int,
    val baseSpellSlotsLevel3: Int,
    val defaultLoot: List<InventoryItem>,
    val defaultTraits: List<TraitItem>
)

object CharacterRepository {
    fun getCharacter(id: String): CharacterData {
        return when (id) {
            "Athania" -> CharacterData(
                id = "Athania",
                name = "Athania",
                charClass = CharacterClass.RANGER,
                baseLevel = 4,
                baseEP = 3606,
                baseStrength = 8,
                baseDexterity = 18,
                baseConstitution = 16,
                baseIntelligence = 10,
                baseWisdom = 14,
                baseCharisma = 8,
                baseMaxHp = 40,
                baseHitDice = 4,
                baseSpellSlotsLevel1 = 3,
                baseSpellSlotsLevel2 = 0,
                baseSpellSlotsLevel3 = 0,
                defaultLoot = listOf(
                    InventoryItem("Beschlagene Lederrüstung", 1, 13.0, "Rüstung & Waffen"),
                    InventoryItem("Langbogen", 1, 2.0, "Rüstung & Waffen"),
                    InventoryItem("Kurzschwert", 1, 2.0, "Rüstung & Waffen"),
                    InventoryItem("Kampfstab", 1, 4.0, "Rüstung & Waffen"),
                    InventoryItem("Peitsche", 1, 3.0, "Rüstung & Waffen"),
                    InventoryItem("Schild", 1, 6.0, "Rüstung & Waffen"),
                    InventoryItem("Reisekleidung", 1, 4.0, "Ausrüstung"),
                    InventoryItem("Rucksack", 1, 5.0, "Ausrüstung"),
                    InventoryItem("Kleine Onyxstatue (Fokus)", 1, 1.0, "Magie"),
                    InventoryItem("Kräuterkundeset", 1, 3.0, "Werkzeug"),
                    InventoryItem("Schwarzer Onyxschädel", 1, 1.0, "Sonstiges"),
                    InventoryItem("Wasserschlauch (halb)", 2, 2.5, "Ausrüstung"),
                    InventoryItem("Trank der Rinderhaut", 1, 0.5, "Tränke"),
                    InventoryItem("Gift (Flasche)", 2, 0.5, "Tränke"),
                    InventoryItem("Heiltrank", 1, 0.5, "Tränke"),
                    InventoryItem("Hämatit", 1, 0.1, "Schätze")
                ),
                defaultTraits = listOf(
                    TraitItem("Urbegleiter (Land, Himmel, Meer)", "Bonusaktion: Urtier befehligen\nAktion: Urtier Angriff\nZauberslot: Urtier beleben (volle HP)"),
                    TraitItem("Trance", "Du musst nicht schlafen. Lange Rast dauert 4 Std in Meditation."),
                    TraitItem("Feenblut", "Vorteil bei Rettungswürfen gegen Bezauberung."),
                    TraitItem("Messerstecher", "Bei Stichschaden 1x pro Zug 1 Angriffswürfel neu würfeln. Bei Krit 1 zus. Schadenswürfel.")
                )
            )
            "Delat" -> CharacterData(
                id = "Delat",
                name = "Delat",
                charClass = CharacterClass.WARLOCK,
                baseLevel = 4,
                baseEP = 3606,
                baseStrength = 8,
                baseDexterity = 14,
                baseConstitution = 14,
                baseIntelligence = 10,
                baseWisdom = 10,
                baseCharisma = 18,
                baseMaxHp = 31,
                baseHitDice = 4,
                baseSpellSlotsLevel1 = 0, // Warlocks use Pact Magic slots mostly, but lets keep it generically 0 here until Phase 3 expands it
                baseSpellSlotsLevel2 = 2, // Warlock Lvl 4 has 2 slot of level 2
                baseSpellSlotsLevel3 = 0,
                defaultLoot = listOf(
                    InventoryItem("Gelederte Rüstung", 1, 10.0, "Rüstung & Waffen"),
                    InventoryItem("Leichte Armbrust", 1, 5.0, "Rüstung & Waffen"),
                    InventoryItem("Dolch", 1, 1.0, "Rüstung & Waffen"),
                    InventoryItem("Arkaner Fokus", 1, 1.0, "Magie"),
                    InventoryItem("Rucksack", 1, 5.0, "Ausrüstung"),
                    InventoryItem("Wasserschlauch", 1, 5.0, "Ausrüstung")
                ),
                defaultTraits = listOf(
                    TraitItem("Pakt der Klinge", "Als Bonusaktion beschwörst du deine Paktwaffe. Charisma für Angriffs- & Schadenswürfe."),
                    TraitItem("Schauerliche Anrufungen", "Agonizing Blast (+CHA Schaden für Eldritch Blast), Armor of Shadows (Mage Armor on Self)")
                )
            )
            else -> throw IllegalArgumentException("Unknown character target: $id")
        }
    }
}
