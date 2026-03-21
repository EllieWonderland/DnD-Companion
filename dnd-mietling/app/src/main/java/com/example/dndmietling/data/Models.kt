package com.example.dndmietling.data

import com.google.firebase.firestore.PropertyName

// ---- Charaktere ----

enum class MietlingCharacter(val displayName: String, val emoji: String) {
    WARLOCK("Warlock", "🧙"),
    THARION("Tharion", "⚔️"),
    SORA("Sora", "🏹"),
    // Auch Companion-Chars im Initiative-Tracker wählbar
    ATHANIA("Athania", "🔮"),
    DELAT("Delat", "🌿")
}

data class CharacterPin(
    val character: MietlingCharacter,
    val pin: String // 4-stellig
)

// ---- Initiative Tracker ----

enum class ParticipantType { CHARACTER, MONSTER }

data class InitiativeEntry(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = java.util.UUID.randomUUID().toString(),
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("initiative") @set:PropertyName("initiative") var initiative: Int = 0,
    @get:PropertyName("type") @set:PropertyName("type") var type: String = ParticipantType.CHARACTER.name,
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = false,
    @get:PropertyName("damageTaken") @set:PropertyName("damageTaken") var damageTaken: Int = 0,
    @get:PropertyName("criticalHint") @set:PropertyName("criticalHint") var criticalHint: Boolean = false,
    @get:PropertyName("maxHp") @set:PropertyName("maxHp") var maxHp: Int = 0,
    @get:PropertyName("currentRound") @set:PropertyName("currentRound") var currentRound: Int = 1
)

// ---- Quests ----

data class Quest(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = java.util.UUID.randomUUID().toString(),
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("description") @set:PropertyName("description") var description: String = "",
    @get:PropertyName("isCompleted") @set:PropertyName("isCompleted") var isCompleted: Boolean = false,
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Long = System.currentTimeMillis(),
    @get:PropertyName("photoUrls") @set:PropertyName("photoUrls") var photoUrls: List<String> = emptyList()
)

// ---- Gruppen-Loot ----

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

// ---- Chat ----

data class GroupChatMessage(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = java.util.UUID.randomUUID().toString(),
    @get:PropertyName("text") @set:PropertyName("text") var text: String = "",
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Long = System.currentTimeMillis(),
    @get:PropertyName("author") @set:PropertyName("author") var author: String = "",
    @get:PropertyName("isOoc") @set:PropertyName("isOoc") var isOoc: Boolean = false
)

// ---- Bücher ----

data class BookEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isPublic: Boolean = false,
    val author: String = ""
)
