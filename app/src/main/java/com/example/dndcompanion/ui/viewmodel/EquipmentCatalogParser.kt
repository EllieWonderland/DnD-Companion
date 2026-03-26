package com.example.dndcompanion.ui.viewmodel

import com.example.dndcompanion.data.database.RulebookDao
import kotlinx.coroutines.flow.first

data class EquipmentCatalogItem(
    val name: String,
    val weight: Double,    // in Pfund (lbs)
    val price: String,
    val category: String
)

object EquipmentCatalogParser {

    @Volatile
    private var cache: List<EquipmentCatalogItem>? = null

    suspend fun loadFromDb(dao: RulebookDao): List<EquipmentCatalogItem> {
        return cache ?: run {
            val items = mutableListOf<EquipmentCatalogItem>()
            dao.getAllWeapons().first().forEach { w ->
                items.add(EquipmentCatalogItem(w.name, w.weightLb, w.price, mapWeaponCategory(w.category)))
            }
            dao.getAllArmor().first().forEach { a ->
                items.add(EquipmentCatalogItem(a.name, a.weightLb, a.price, "Rüstung"))
            }
            dao.getAllTools().first().forEach { t ->
                items.add(EquipmentCatalogItem(t.name, t.weightLb ?: 0.0, t.price, "Werkzeug"))
            }
            dao.getAllGear().first().forEach { g ->
                items.add(EquipmentCatalogItem(g.name, g.weightLb, g.price, "Ausrüstung"))
            }
            items.also { cache = it }
        }
    }

    private fun mapWeaponCategory(category: String): String {
        return when {
            category.contains("Einfache Nahkampf", ignoreCase = true) -> "Waffen (Einfach, Nahkampf)"
            category.contains("Einfache Fernkampf", ignoreCase = true) -> "Waffen (Einfach, Fernkampf)"
            category.contains("Kriegs-Nahkampf", ignoreCase = true) -> "Waffen (Kriegs, Nahkampf)"
            category.contains("Kriegs-Fernkampf", ignoreCase = true) -> "Waffen (Kriegs, Fernkampf)"
            else -> "Waffen"
        }
    }
}
