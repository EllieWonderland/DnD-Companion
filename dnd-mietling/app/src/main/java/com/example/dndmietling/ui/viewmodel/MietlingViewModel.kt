package com.example.dndmietling.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.dndmietling.data.*
import com.google.firebase.firestore.FirebaseFirestore

class MietlingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()

    // ---- Aktuell eingeloggter Charakter ----
    var currentCharacter by mutableStateOf<MietlingCharacter?>(null)

    // ---- Initiative Tracker ----
    val initiativeEntries = mutableStateListOf<InitiativeEntry>()
    var currentRound by mutableStateOf(1)
    var activeEntryId by mutableStateOf<String?>(null)

    // ---- Quests ----
    val quests = mutableStateListOf<Quest>()

    // ---- Gruppen-Loot ----
    var sharedCoins by mutableStateOf(SharedCoins())
    val sharedLootItems = mutableStateListOf<GroupLootItem>()

    // ---- Chat ----
    val chatMessages = mutableStateListOf<GroupChatMessage>()

    // ---- Bücher ----
    val publicGeneralBookEntries = mutableStateListOf<BookEntry>()
    val publicGrudgeBookEntries = mutableStateListOf<BookEntry>()

    init {
        listenToInitiative()
        listenToQuests()
        listenToLoot()
        listenToChat()
        listenToPublicNotes()
    }

    // ============================================================
    // INITIATIVE TRACKER
    // ============================================================

    private fun listenToInitiative() {
        db.collection("initiativeTracker")
            .addSnapshotListener { snapshot, e ->
                if (e != null) { Log.e("Mietling", "Initiative Fehler", e); return@addSnapshotListener }
                if (snapshot != null) {
                    initiativeEntries.clear()
                    for (doc in snapshot.documents) {
                        val entry = doc.toObject(InitiativeEntry::class.java)
                        if (entry != null) initiativeEntries.add(entry)
                    }
                    initiativeEntries.sortByDescending { it.initiative }
                    activeEntryId = initiativeEntries.firstOrNull { it.isActive }?.id
                }
            }
    }

    fun addInitiativeEntry(entry: InitiativeEntry) {
        db.collection("initiativeTracker").document(entry.id).set(entry)
    }

    fun removeInitiativeEntry(id: String) {
        db.collection("initiativeTracker").document(id).delete()
    }

    fun clearInitiativeTracker() {
        val batch = db.batch()
        initiativeEntries.forEach { entry ->
            batch.delete(db.collection("initiativeTracker").document(entry.id))
        }
        batch.commit()
    }

    fun nextTurn() {
        val sorted = initiativeEntries.sortedByDescending { it.initiative }
        if (sorted.isEmpty()) return
        val activeIdx = sorted.indexOfFirst { it.id == activeEntryId }
        val nextIdx = if (activeIdx < 0 || activeIdx >= sorted.size - 1) 0 else activeIdx + 1
        if (nextIdx == 0 && activeIdx >= 0) {
            currentRound++
        }
        val batch = db.batch()
        sorted.forEachIndexed { idx, entry ->
            val isNowActive = idx == nextIdx
            if (entry.isActive != isNowActive) {
                batch.update(db.collection("initiativeTracker").document(entry.id), "isActive", isNowActive)
            }
        }
        batch.commit()
    }

    fun updateMonsterDamage(id: String, damageTaken: Int) {
        db.collection("initiativeTracker").document(id).update("damageTaken", damageTaken)
    }

    fun toggleCriticalHint(id: String, current: Boolean) {
        db.collection("initiativeTracker").document(id).update("criticalHint", !current)
    }

    // ============================================================
    // QUESTS
    // ============================================================

    private fun listenToQuests() {
        db.collection("globalQuests")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { Log.e("Mietling", "Quests Fehler", e); return@addSnapshotListener }
                if (snapshot != null) {
                    quests.clear()
                    for (doc in snapshot.documents) {
                        val q = doc.toObject(Quest::class.java)
                        if (q != null) quests.add(q)
                    }
                }
            }
    }

    fun addQuest(title: String, description: String) {
        if (title.isBlank()) return
        val quest = Quest(title = title.trim(), description = description.trim())
        db.collection("globalQuests").document(quest.id).set(quest)
    }

    fun toggleQuestCompletion(quest: Quest) {
        db.collection("globalQuests").document(quest.id).update("isCompleted", !quest.isCompleted)
    }

    fun updateQuest(questId: String, title: String, description: String) {
        db.collection("globalQuests").document(questId).update(
            "title", title.trim(),
            "description", description.trim()
        )
    }

    fun deleteQuest(questId: String) {
        db.collection("globalQuests").document(questId).delete()
    }

    fun addPhotoToQuest(questId: String, photoUrl: String) {
        val quest = quests.find { it.id == questId } ?: return
        val updated = quest.photoUrls + photoUrl
        db.collection("globalQuests").document(questId).update("photoUrls", updated)
    }

    // ============================================================
    // GRUPPEN-LOOT
    // ============================================================

    private fun listenToLoot() {
        db.collection("groupLootCoins").document("shared").addSnapshotListener { snapshot, e ->
            if (e != null) { Log.e("Mietling", "Coins Fehler", e); return@addSnapshotListener }
            if (snapshot != null && snapshot.exists()) {
                val coins = snapshot.toObject(SharedCoins::class.java)
                if (coins != null) sharedCoins = coins
            }
        }
        db.collection("groupLootItems").addSnapshotListener { snapshot, e ->
            if (e != null) { Log.e("Mietling", "Loot Fehler", e); return@addSnapshotListener }
            if (snapshot != null) {
                sharedLootItems.clear()
                for (doc in snapshot.documents) {
                    val item = doc.toObject(GroupLootItem::class.java)
                    if (item != null) sharedLootItems.add(item)
                }
            }
        }
    }

    fun updateSharedCoins(km: Int, sm: Int, em: Int, gm: Int, pm: Int) {
        db.collection("groupLootCoins").document("shared").set(SharedCoins(km, sm, em, gm, pm))
    }

    fun addSharedLootItem(name: String, amount: Int, weight: Double, category: String) {
        val existing = sharedLootItems.find { it.name.trim().equals(name.trim(), ignoreCase = true) }
        if (existing != null) {
            val updated = existing.amount + amount
            if (updated <= 0) db.collection("groupLootItems").document(existing.id).delete()
            else db.collection("groupLootItems").document(existing.id).update("amount", updated)
        } else if (amount > 0) {
            val item = GroupLootItem(name = name.trim(), amount = amount, weight = weight, category = category)
            db.collection("groupLootItems").document(item.id).set(item)
        }
    }

    fun deleteSharedLootItem(id: String) {
        db.collection("groupLootItems").document(id).delete()
    }

    // ============================================================
    // CHAT
    // ============================================================

    private fun listenToChat() {
        db.collection("groupChat")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { Log.e("Mietling", "Chat Fehler", e); return@addSnapshotListener }
                if (snapshot != null) {
                    chatMessages.clear()
                    for (doc in snapshot.documents) {
                        val msg = doc.toObject(GroupChatMessage::class.java)
                        if (msg != null) chatMessages.add(msg)
                    }
                }
            }
    }

    fun sendChatMessage(text: String, isOoc: Boolean) {
        val author = currentCharacter?.displayName ?: "Mietling"
        if (text.isBlank()) return
        val msg = GroupChatMessage(text = text.trim(), author = author, isOoc = isOoc)
        db.collection("groupChat").document(msg.id).set(msg)
    }

    // ============================================================
    // BÜCHER / NOTIZEN
    // ============================================================

    private fun listenToPublicNotes() {
        db.collection("publicGeneralNotes")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { Log.e("Mietling", "Notizen Fehler", e); return@addSnapshotListener }
                if (snapshot != null) {
                    publicGeneralBookEntries.clear()
                    for (doc in snapshot.documents) {
                        val entry = doc.toObject(BookEntry::class.java)
                        if (entry != null) publicGeneralBookEntries.add(entry)
                    }
                }
            }
        db.collection("publicGrudgeNotes")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { Log.e("Mietling", "Groll Fehler", e); return@addSnapshotListener }
                if (snapshot != null) {
                    publicGrudgeBookEntries.clear()
                    for (doc in snapshot.documents) {
                        val entry = doc.toObject(BookEntry::class.java)
                        if (entry != null) publicGrudgeBookEntries.add(entry)
                    }
                }
            }
    }

    fun addPublicNote(collection: String, text: String) {
        val author = currentCharacter?.displayName ?: "Mietling"
        val entry = BookEntry(text = text.trim(), isPublic = true, author = author)
        db.collection(collection).document(entry.id).set(entry)
    }
}
