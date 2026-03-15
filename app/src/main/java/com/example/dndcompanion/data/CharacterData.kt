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
                baseEP = 4494,
                baseStrength = 8,
                baseDexterity = 18,
                baseConstitution = 16,
                baseIntelligence = 10,
                baseWisdom = 14,
                baseCharisma = 8,
                baseMaxHp = 40,
                baseHitDice = 10,
                baseSpellSlotsLevel1 = 3,
                baseSpellSlotsLevel2 = 0,
                baseSpellSlotsLevel3 = 0,
                proficientSkills = listOf("Heimlichkeit", "Motiv erkennen", "Naturkunde", "Überleben", "Wahrnehmung", "Mit Tieren umgehen"),
                defaultLoot = listOf(
                    InventoryItem("Beschlagene Lederrüstung", 1, 6.5, "Rüstung & Waffen"),
                    InventoryItem("Peitsche", 2, 1.5, "Rüstung & Waffen"),
                    InventoryItem("Knüppel", 1, 1.0, "Rüstung & Waffen"),
                    InventoryItem("Kurzschwert", 1, 1.0, "Rüstung & Waffen"),
                    InventoryItem("Langbogen", 1, 1.0, "Rüstung & Waffen"),
                    InventoryItem("Köcher mit 26 Pfeilen", 1, 0.5, "Rüstung & Waffen"),
                    InventoryItem("Kampfstab (arkaner Fokus)", 1, 2.0, "Rüstung & Waffen"),
                    InventoryItem("Schild", 1, 3.0, "Rüstung & Waffen"),
                    InventoryItem("Reisekleidung", 1, 2.0, "Ausrüstung"),
                    InventoryItem("Rucksack", 1, 2.5, "Ausrüstung"),
                    InventoryItem("Blasrohr", 1, 0.5, "Rüstung & Waffen"),
                    InventoryItem("Seil", 1, 5.0, "Ausrüstung"),
                    InventoryItem("Brosche", 1, 0.1, "Ausrüstung"),
                    InventoryItem("Zange", 1, 1.0, "Werkzeug"),
                    InventoryItem("Kleine Onyxstatue \"Schwarzer Drache\" (Fokus)", 1, 0.5, "Magie"),
                    InventoryItem("Kräuterkundeset", 1, 1.5, "Werkzeug"),
                    InventoryItem("Juwelierwerkzeug", 1, 1.0, "Werkzeug"),
                    InventoryItem("Schwarzer Onyxschädel", 1, 0.5, "Sonstiges"),
                    InventoryItem("Wasserschlauch (halb)", 2, 2.5, "Ausrüstung"),
                    InventoryItem("Gute Beeren", 4, 0.0, "Ausrüstung"),
                    InventoryItem("Trank der Rindenhaut", 1, 0.5, "Tränke"),
                    InventoryItem("Gift (Flasche)", 2, 0.1, "Tränke"),
                    InventoryItem("Heiltrank", 1, 0.5, "Tränke"),
                    InventoryItem("Öl", 1, 0.5, "Ausrüstung"),
                    InventoryItem("Hämatit", 1, 0.1, "Schätze")
                ),
                defaultTraits = listOf(
                    TraitItem("Dunkelsicht (36m)", "Du kannst in Dunkelheit sehen."),
                    TraitItem("Feenblut", "Vorteil gegen Bezaubern."),
                    TraitItem("Scharfe Sinne", "+1 Fertigkeit."),
                    TraitItem("Trance", "Du musst nicht schlafen. Lange Rast dauert 4 Std in Meditation."),
                    TraitItem("Urbegleiter (Land, Himmel, Meer)", "Bonusaktion: Urtier befehligen\nAktion: Urtier Angriff\nZauberslot: Urtier beleben (volle HP)"),
                    TraitItem("Waffenmeisterung (Langbogen, Knüppel)", "Meisterschafts-Eigenschaften nutzbar."),
                    TraitItem("Erzfeind", "Zeichen des Jägers ist stets vorbereitet und kann 2x pro lange Rast gewirkt werden (ohne Spellslot).", maxUses = 2, currentUses = 2, grantedSpellId = "Zeichen des Jägers"),
                    TraitItem("Bogenschießen", "+2 auf Fernkampfangriffe."),
                    TraitItem("Messerstecher", "Bei Stichschaden 1x pro Zug 1 Angriffswürfel neu würfeln. Bei Krit 1 zus. Schadenswürfel."),
                    TraitItem("Eingeweihter der Magie (Druide)", "Nutzt Druiden-Skills.")
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
                baseLevel = 5,
                baseEP = 6500,
                baseStrength = 8,
                baseDexterity = 14,
                baseConstitution = 15,
                baseIntelligence = 10,
                baseWisdom = 10,
                baseCharisma = 18,
                baseMaxHp = 43,
                baseHitDice = 8,
                baseSpellSlotsLevel1 = 0,
                baseSpellSlotsLevel2 = 0,
                baseSpellSlotsLevel3 = 2,
                proficientSkills = listOf("Arkane Kunde", "Einschüchtern", "Fingerfertigkeit", "Täuschen", "Weisheit-Rettungswurf"),
                defaultLoot = listOf(
                    InventoryItem("Flöte", 1, 0.5, "Ausrüstung"),
                    InventoryItem("Reisekleidung", 1, 2.0, "Ausrüstung"),
                    InventoryItem("Versilberter Speer", 1, 1.5, "Rüstung & Waffen"),
                    InventoryItem("Fackel", 1, 0.5, "Ausrüstung"),
                    InventoryItem("Handschuhe der arkanen Kunde (Eingestimmt)", 1, 0.5, "Magie"),
                    InventoryItem("Rucksack", 1, 2.5, "Ausrüstung"),
                    InventoryItem("Netz", 1, 1.5, "Ausrüstung"),
                    InventoryItem("Lederrüstung", 1, 5.0, "Rüstung & Waffen"),
                    InventoryItem("Dolch", 1, 0.5, "Rüstung & Waffen"),
                    InventoryItem("Phiole", 1, 0.1, "Ausrüstung"),
                    InventoryItem("Weinflasche", 3, 1.0, "Ausrüstung"),
                    InventoryItem("Glasflasche", 2, 0.5, "Ausrüstung"),
                    InventoryItem("Trinkschlauch (halb)", 2, 2.5, "Ausrüstung"),
                    InventoryItem("Gute Beeren", 4, 0.0, "Ausrüstung"),
                    InventoryItem("Trank der Tierfreundschaft", 1, 0.5, "Tränke"),
                    InventoryItem("Rationen", 3, 1.5, "Ausrüstung"),
                    InventoryItem("Weihwasser", 1, 0.5, "Magie"),
                    InventoryItem("Kleine magische Truhe", 1, 2.0, "Magie"),
                    InventoryItem("Magischer Würfel mit 1 auf jeder Seite", 1, 0.1, "Magie")
                ),
                defaultTraits = listOf(
                    TraitItem("Dunkelsicht (36m)", "Du kannst in Dunkelheit sehen."),
                    TraitItem("Steingespür", "Bonusaktion. Erschütterungssinn für 10 min innerhalb von 18m, wenn beide auf Gestein.", maxUses = 2, currentUses = 2),
                    TraitItem("Zwergische Unverwüstlichkeit", "Giftresistenz, Vorteil bei Rettungswurf Vergiftet."),
                    TraitItem("Zwergische Zähigkeit", "+1 TP pro Stufe."),
                    TraitItem("Paktmagie", "2 Zaubertricks, 2 Zauberplätze (St. 2), 4 vorbereitete Zauber."),
                    TraitItem("Pakt der Klinge - Paktwaffe", "Unholde Vitalität, Qualvoller Strahl - Schauriger Strahl."),
                    TraitItem("Unholde Vitalität", "Du kannst Falsches Leben (Level 1) beliebig oft ohne Slot wirken.", maxUses = 999, currentUses = 999, grantedSpellId = "Falsches Leben"),
                    TraitItem("Qualvoller Strahl", "Addiere CHA-Mod zu Schaurigem Strahl."),
                    TraitItem("Magische Raffinesse", "1/LR 1min Ritus, Hälfte der Zauberplätze zurück.", maxUses = 1, currentUses = 1),
                    TraitItem("Erwachter Geist", "Telepathische Verbindung (BA, max 9m)."),
                    TraitItem("Psychische Zauber", "Schadensart in psychisch ändern. Keine V/G an Verzauberung/Illusion."),
                    TraitItem("Macht der Tiefe", "Verbündeten angreifen für Vorteil + Fluch."),
                    TraitItem("Eingeweihter der Magie (Magier)", "Druide/Magier Skills."),
                    TraitItem("Feenberührt", "+1 CHA, Nebelschritt & Zauber."),
                    TraitItem("Sprachen", "Gemeinsprache, Elfisch, Zwergisch.")
                )
            )
            else -> throw IllegalArgumentException("Unknown character target: $id")
        }
    }
}
