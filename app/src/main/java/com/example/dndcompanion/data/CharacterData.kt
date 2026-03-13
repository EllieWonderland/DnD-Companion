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
    val race: String,
    val background: String,
    val alignment: String,
    val speed: Int,
    val passivePerception: Int,
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
    val proficientSkills: List<String>,
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
                race = "Elf-Drow",
                background = "Wegfinder",
                alignment = "Chaotisch Gut",
                speed = 9,
                passivePerception = 16,
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
                proficientSkills = listOf("Heimlichkeit", "Motiv erkennen", "Naturkunde", "Überleben", "Wahrnehmung"),
                defaultLoot = listOf(
                    InventoryItem("Beschlagene Lederrüstung", 1, 13.0, "Rüstung & Waffen"),
                    InventoryItem("Langbogen", 1, 2.0, "Rüstung & Waffen"),
                    InventoryItem("Kurzschwert", 1, 2.0, "Rüstung & Waffen"),
                    InventoryItem("Knüppel", 1, 2.0, "Rüstung & Waffen"),
                    InventoryItem("Peitsche", 1, 3.0, "Rüstung & Waffen"),
                    InventoryItem("Schild", 1, 6.0, "Rüstung & Waffen"),
                    InventoryItem("Kampfstab", 1, 4.0, "Rüstung & Waffen"),
                    InventoryItem("Köcher mit 28 Pfeilen", 1, 1.4, "Rüstung & Waffen"),
                    InventoryItem("Reisekleidung", 1, 4.0, "Ausrüstung"),
                    InventoryItem("Rucksack", 1, 5.0, "Ausrüstung"),
                    InventoryItem("Kleine Onyxstatue (Fokus)", 1, 1.0, "Magie"),
                    InventoryItem("Kräuterkundeset", 1, 3.0, "Werkzeug"),
                    InventoryItem("Schwarzer Onyxschädel", 1, 1.0, "Sonstiges"),
                    InventoryItem("Wasserschlauch (halb)", 2, 2.5, "Ausrüstung"),
                    InventoryItem("Gute Beeren", 10, 0.0, "Ausrüstung"),
                    InventoryItem("Trank der Rinderhaut", 1, 0.5, "Tränke"),
                    InventoryItem("Gift (Flasche)", 2, 0.5, "Tränke"),
                    InventoryItem("Heiltrank", 1, 0.5, "Tränke"),
                    InventoryItem("Hämatit", 1, 0.1, "Schätze")
                ),
                defaultTraits = listOf(
                    TraitItem("Urbegleiter (Land, Himmel, Meer)", "Bonusaktion: Urtier befehligen\nAktion: Urtier Angriff\nZauberslot: Urtier beleben (volle HP)"),
                    TraitItem("Trance", "Du musst nicht schlafen. Lange Rast dauert 4 Std in Meditation."),
                    TraitItem("Feenfeuer", "Vorteil bei Rettungswürfen gegen Bezauberung. Du kennst Feenfeuer (1/LR ab St. 3) und Dunkelheit (1/LR ab St. 5).", maxUses = 1, currentUses = 1, grantedSpellId = "Feenfeuer"),
                    TraitItem("Dunkelheit", "Du kannst Dunkelheit 1/LR ohne Slot wirken.", maxUses = 1, currentUses = 1, grantedSpellId = "Dunkelheit"),
                    TraitItem("Zeichen des Jägers", "Du kannst Zeichen des Jägers 2/LR ohne Slot wirken.", maxUses = 2, currentUses = 2, grantedSpellId = "Zeichen des Jägers"),
                    TraitItem("Gute Beeren", "Du kannst Gute Beere 1/LR ohne Slot wirken.", maxUses = 1, currentUses = 1, grantedSpellId = "Gute Beere"),
                    TraitItem("Wunden heilen", "Du kannst Wunden heilen 1/LR ohne Slot wirken.", maxUses = 1, currentUses = 1, grantedSpellId = "Wunden heilen"),
                    TraitItem("Messerstecher", "Bei Stichschaden 1x pro Zug 1 Angriffswürfel neu würfeln. Bei Krit 1 zus. Schadenswürfel.")
                )
            )
            "Delat" -> CharacterData(
                id = "Delat",
                name = "Delat",
                charClass = CharacterClass.WARLOCK,
                race = "Zwerg",
                background = "Seefahrer",
                alignment = "Chaotisch Gut",
                speed = 9,
                passivePerception = 10,
                baseLevel = 4,
                baseEP = 4907,
                baseStrength = 8,
                baseDexterity = 14,
                baseConstitution = 15,
                baseIntelligence = 10,
                baseWisdom = 10,
                baseCharisma = 18,
                baseMaxHp = 35,
                baseHitDice = 4,
                baseSpellSlotsLevel1 = 0,
                baseSpellSlotsLevel2 = 2,
                baseSpellSlotsLevel3 = 0,
                proficientSkills = listOf("Arkane Kunde", "Einschüchtern", "Fingerfertigkeit", "Täuschen", "Weisheit-Rettungswurf"),
                defaultLoot = listOf(
                    InventoryItem("Lederrüstung", 1, 10.0, "Rüstung & Waffen"),
                    InventoryItem("Versilberter Speer", 1, 3.0, "Rüstung & Waffen"),
                    InventoryItem("Handschuhe der arkanen Kunde", 1, 0.5, "Magie"),
                    InventoryItem("Kleine magische Truhe", 1, 5.0, "Magie"),
                    InventoryItem("Magischer Würfel mit 1 auf jeder Seite", 1, 0.1, "Magie"),
                    InventoryItem("Rucksack", 1, 5.0, "Ausrüstung"),
                    InventoryItem("Reisekleidung", 1, 4.0, "Ausrüstung"),
                    InventoryItem("Trinkschlauch", 2, 2.5, "Ausrüstung"),
                    InventoryItem("Fackel", 1, 1.0, "Ausrüstung"),
                    InventoryItem("Netz", 1, 3.0, "Ausrüstung"),
                    InventoryItem("Weinflasche", 1, 1.0, "Ausrüstung"),
                    InventoryItem("Glasflasche", 2, 0.5, "Ausrüstung"),
                    InventoryItem("Gute Beeren", 1, 0.0, "Ausrüstung"),
                    InventoryItem("Flöte", 1, 1.0, "Ausrüstung"),
                    InventoryItem("Rationen", 3, 2.0, "Ausrüstung"),
                    InventoryItem("Trank der Tierfreundschaft", 1, 0.5, "Tränke")
                ),
                defaultTraits = listOf(
                    TraitItem("Steingespür", "Bonusaktion. Erschütterungssinn für 10 min innerhalb von 18m, wenn beide auf Gestein.", maxUses = 2, currentUses = 2),
                    TraitItem("Zwergische Unverwüstlichkeit", "Giftresistenz, Vorteil bei Rettungswurf Vergiftet."),
                    TraitItem("Zwergische Zähigkeit", "+1 TP pro Stufe."),
                    TraitItem("Paktmagie", "2 Zaubertricks, 2 Zauberplätze (St. 2), 4 vorbereitete Zauber."),
                    TraitItem("Pakt der Klinge", "Paktwaffe beschwören (BA)."),
                    TraitItem("Unholde Vitalität", "Du kannst Falsches Leben (Level 1) beliebig oft ohne Slot wirken.", maxUses = 999, currentUses = 999, grantedSpellId = "Falsches Leben"),
                    TraitItem("Qualvoller Strahl", "Addiere CHA-Mod zu Schaurigem Strahl."),
                    TraitItem("Magische Rafinesse", "1/LR 1min Ritus, Hälfte der Zauberplätze zurück.", maxUses = 1, currentUses = 1),
                    TraitItem("Erwachter Geist", "Telepathische Verbindung (BA, max 9m)."),
                    TraitItem("Psychische Zauber", "Schadensart in psychisch ändern. Keine V/G an Verzauberung/Illusion."),
                    TraitItem("Macht der Tiefe", "Verbündeten angreifen für Vorteil + Fluch."),
                    TraitItem("Eingeweihter der Magie (Magierrüstung)", "Du kannst Magierrüstung 1/LR ohne Slot wirken.", maxUses = 1, currentUses = 1, grantedSpellId = "Magierrüstung"),
                    TraitItem("Eingeweihter der Magie (Segnen)", "Du kannst Segnen 1/LR ohne Slot wirken.", maxUses = 1, currentUses = 1, grantedSpellId = "Segnen"),
                    TraitItem("Feenberührt (Nebelschritt)", "Du kannst Nebelschritt 1/LR ohne Slot wirken.", maxUses = 1, currentUses = 1, grantedSpellId = "Nebelschritt")
                )
            )
            else -> throw IllegalArgumentException("Unknown character target: $id")
        }
    }
}
