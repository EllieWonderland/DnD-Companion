package com.example.dndcompanion.ui.viewmodel

import com.google.firebase.firestore.PropertyName
import com.example.dndcompanion.data.CharacterClass

// --- DATENKLASSEN & ENUMS ---
data class InventoryItem(
    val name: String,
    val amount: Int,
    val weight: Double = 0.0,
    val category: String = "Sonstiges",
    var maxCharges: Int = 0,
    var currentCharges: Int = 0,
    val spellCharges: Map<String, Int>? = null // Map spellId -> cost
)
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val localText: String? = null,
    val externalText: String? = null,
    val chapterLink: String? = null,
    val chapterSearchTerm: String? = null, // NEU: Für zielgenaues Scrollen
    val faqTitle: String? = null // NEU: Dynamischer Titel für FAQ-Aufnahme
)

data class FaqItem(val question: String, val answer: String)
data class TraitItem(
    val name: String,
    val desc: String,
    var maxUses: Int = 0,
    var currentUses: Int = 0,
    val grantedSpellId: String? = null,
    val resetOnShortRest: Boolean = false
)
data class BookEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isPublic: Boolean = false,
    val author: String = "Athania"
)

data class GroupChatMessage(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = java.util.UUID.randomUUID().toString(),
    @get:PropertyName("text") @set:PropertyName("text") var text: String = "",
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Long = System.currentTimeMillis(),
    @get:PropertyName("author") @set:PropertyName("author") var author: String = "Athania",
    @get:PropertyName("charClass") @set:PropertyName("charClass") var charClass: CharacterClass = CharacterClass.RANGER,
    @get:PropertyName("isOoc") @set:PropertyName("isOoc") var isOoc: Boolean = false
)

data class Quest(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = java.util.UUID.randomUUID().toString(),
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("description") @set:PropertyName("description") var description: String = "",
    @get:PropertyName("isCompleted") @set:PropertyName("isCompleted") var isCompleted: Boolean = false,
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Long = System.currentTimeMillis()
)

data class GroupLootItem(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = java.util.UUID.randomUUID().toString(),
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("amount") @set:PropertyName("amount") var amount: Int = 0,
    @get:PropertyName("weight") @set:PropertyName("weight") var weight: Double = 0.0,
    @get:PropertyName("category") @set:PropertyName("category") var category: String = "Sonstiges"
)

data class SharedCoins(
    @get:PropertyName("km") @set:PropertyName("km") var km: Int = 0,
    @get:PropertyName("sm") @set:PropertyName("sm") var sm: Int = 0,
    @get:PropertyName("em") @set:PropertyName("em") var em: Int = 0,
    @get:PropertyName("gm") @set:PropertyName("gm") var gm: Int = 0,
    @get:PropertyName("pm") @set:PropertyName("pm") var pm: Int = 0
)

data class Spell(
    val id: String = java.util.UUID.randomUUID().toString(),
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
    var isPrepared: Boolean = false,
    var isRitual: Boolean = false
)

enum class ActiveWeapon {
    LANGBOGEN,
    KURZSCHWERT_SCHILD,
    SHILLELAGH_SCHILD,
    KRIEGSHAMMER_PAKT,
    SPEER_PAKT
}

data class EquipmentDefinition(
    val name: String,
    val weight: Double,
    val category: String
)

enum class BeastType {
    LAND,
    SKY,
    SEA
}

data class CompanionDto(
    val name: String,
    val typ_und_gesinnung: String,
    val ruestungsklasse: String,
    val trefferpunkte: String,
    val bewegungsrate: Map<String, String>,
    val attribute: Map<String, AttributeDto>,
    val sinne: String,
    val sprachen: String,
    val herausforderungsgrad: String,
    val merkmale: List<CompanionTraitDto>,
    val aktionen: List<CompanionActionDto>,
    val reaktionen: List<CompanionActionDto>? = null
)

data class AttributeDto(
    val wert: Int,
    val modifikator: Int,
    val rettungswurf: Int
)

data class CompanionTraitDto(
    val name: String,
    val beschreibung: String
)

data class CompanionActionDto(
    val name: String,
    val typ: String? = null,
    val beschreibung: String? = null,
    val ausloeser: String? = null,
    val antwort: String? = null
)
