package com.example.dndcompanion.ui.viewmodel

import android.content.Context

data class EquipmentCatalogItem(
    val name: String,
    val weight: Double,    // in Pfund (lbs)
    val price: String,
    val category: String
)

object EquipmentCatalogParser {

    fun loadFromAssets(context: Context): List<EquipmentCatalogItem> {
        val content = context.assets.open("Rules/Handbuch/Kapitel/kapitel6_equipment.md")
            .bufferedReader().use { it.readText() }
        return parseEquipmentMarkdown(content)
    }

    fun parseEquipmentMarkdown(content: String): List<EquipmentCatalogItem> {
        val items = mutableListOf<EquipmentCatalogItem>()
        val lines = content.lines()

        var currentSection = ""       // e.g. "Waffen", "Rüstung", "Werkzeug", "Ausrüstung"
        var currentSubSection = ""    // e.g. "Einfache Nahkampfwaffen"
        var tableType = ""            // "weapons", "armor", "tools", "gear", "vehicles"
        var inTable = false

        for (i in lines.indices) {
            val line = lines[i].trim()

            // Section headers
            if (line.startsWith("## ")) {
                inTable = false
                when {
                    line.contains("Waffen") -> { currentSection = "Waffen"; tableType = "weapons" }
                    line.contains("Rüstungen") || line.contains("Armor") -> { currentSection = "Rüstung"; tableType = "armor" }
                    line.contains("Werkzeuge") || line.contains("Tools") -> { currentSection = "Werkzeug"; tableType = "tools" }
                    line.contains("Abenteuerausrüstung") || line.contains("Adventuring Gear") -> { currentSection = "Ausrüstung"; tableType = "gear" }
                    line.contains("Reittiere") || line.contains("Fahrzeuge") -> { currentSection = "Fahrzeuge"; tableType = "vehicles" }
                    line.contains("Dienstleistungen") -> { currentSection = ""; tableType = "" }
                }
                continue
            }

            // Sub-section headers
            if (line.startsWith("### ")) {
                currentSubSection = line.removePrefix("### ").trim()
                continue
            }

            // Skip non-table and separator lines
            if (!line.startsWith("|") || line.contains(":---") || line.contains("---:") || tableType.isEmpty()) continue

            // Parse table rows
            val columns = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
            if (columns.isEmpty()) continue

            // Detect bold sub-headers inside tables (e.g. "**Einfache Nahkampfwaffen ...**")
            if (columns[0].startsWith("**") && columns[0].endsWith("**")) {
                currentSubSection = columns[0].removeSurrounding("**").trim()
                continue
            }

            // Skip header rows
            if (columns[0] == "Waffe (Name)" || columns[0] == "Rüstungstyp" ||
                columns[0] == "Werkzeug" || columns[0] == "Gegenstand" ||
                columns[0] == "Tier (Animal)" || columns[0] == "Schiffstyp" ||
                columns[0] == "Münze") continue

            try {
                when (tableType) {
                    "weapons" -> {
                        // columns: Name | Schaden | Eigenschaften | Meisterschaft | Gewicht | Preis
                        if (columns.size >= 6) {
                            val name = columns[0]
                            val weight = parseWeight(columns[4])
                            val price = columns[5]
                            val category = mapWeaponCategory(currentSubSection)
                            items.add(EquipmentCatalogItem(name, weight, price, category))
                        }
                    }
                    "armor" -> {
                        // columns: Rüstungstyp | RK | Stärke | Heimlichkeit | Gewicht | Preis
                        if (columns.size >= 6) {
                            val name = columns[0]
                            val weight = parseWeight(columns[4])
                            val price = columns[5]
                            items.add(EquipmentCatalogItem(name, weight, price, "Rüstung"))
                        }
                    }
                    "tools" -> {
                        // columns: Werkzeug | Preis | Gewicht
                        if (columns.size >= 3) {
                            val name = columns[0]
                            val weight = parseWeight(columns[2])
                            val price = columns[1]
                            items.add(EquipmentCatalogItem(name, weight, price, "Werkzeug"))
                        }
                    }
                    "gear" -> {
                        // columns: Gegenstand | Preis | Gewicht
                        if (columns.size >= 3) {
                            val name = columns[0]
                            val weight = parseWeight(columns[2])
                            val price = columns[1]
                            items.add(EquipmentCatalogItem(name, weight, price, "Ausrüstung"))
                        }
                    }
                    "vehicles" -> {
                        // Various table formats, skip vehicle tables for inventory purposes
                    }
                }
            } catch (_: Exception) {
                // Skip malformed rows
            }
        }
        return items
    }

    private fun parseWeight(raw: String): Double {
        val cleaned = raw.trim()
            .replace("Pfd.", "")
            .replace("lb.", "")
            .replace(",", ".")
            .replace(" ", "")
            .trim()
        if (cleaned == "-" || cleaned.isEmpty() || cleaned == "Variiert") return 0.0
        return cleaned.toDoubleOrNull() ?: 0.0
    }

    private fun mapWeaponCategory(subSection: String): String {
        return when {
            subSection.contains("Einfache Nahkampf", ignoreCase = true) -> "Waffen (Einfach, Nahkampf)"
            subSection.contains("Einfache Fernkampf", ignoreCase = true) -> "Waffen (Einfach, Fernkampf)"
            subSection.contains("Kriegs-Nahkampf", ignoreCase = true) -> "Waffen (Kriegs, Nahkampf)"
            subSection.contains("Kriegs-Fernkampf", ignoreCase = true) -> "Waffen (Kriegs, Fernkampf)"
            else -> "Waffen"
        }
    }
}
