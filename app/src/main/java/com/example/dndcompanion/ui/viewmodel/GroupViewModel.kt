package com.example.dndcompanion.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.FirebaseFirestore

class GroupViewModel(
    application: Application,
    private val characterVm: CharacterViewModel
) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()

    val publicGeneralBookEntries = mutableStateListOf<BookEntry>()
    val publicGrudgeBookEntries = mutableStateListOf<BookEntry>()
    val groupChatMessages = mutableStateListOf<GroupChatMessage>()
    val globalQuests = mutableStateListOf<Quest>()

    var sharedCoins by mutableStateOf(SharedCoins())
    val sharedLootItems = mutableStateListOf<GroupLootItem>()

    init {
        listenToPublicNotes()
        listenToGroupChat()
        listenToQuests()
        listenToSharedLoot()
    }

    private fun listenToPublicNotes() {
        db.collection("publicGeneralNotes")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firebase", "Allgemeine Notizen Listener Fehler", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    publicGeneralBookEntries.clear()
                    for (doc in snapshot.documents) {
                        val entry = doc.toObject(BookEntry::class.java)
                        if (entry != null) {
                            publicGeneralBookEntries.add(entry)
                        }
                    }
                }
            }

        db.collection("publicGrudgeNotes")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firebase", "Buch des Grolls Listener Fehler", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    publicGrudgeBookEntries.clear()
                    for (doc in snapshot.documents) {
                        val entry = doc.toObject(BookEntry::class.java)
                        if (entry != null) {
                            publicGrudgeBookEntries.add(entry)
                        }
                    }
                }
            }
    }

    fun addPublicGeneralBookEntry(id: String, text: String) {
        if (text.isNotBlank()) {
            val entry = BookEntry(id = id, text = text.trim(), isPublic = true)
            db.collection("publicGeneralNotes").document(entry.id).set(entry)
        }
    }

    fun addPublicGrudgeBookEntry(id: String, text: String) {
        if (text.isNotBlank()) {
            val entry = BookEntry(id = id, text = text.trim(), isPublic = true)
            db.collection("publicGrudgeNotes").document(entry.id).set(entry)
        }
    }

    fun updatePublicGeneralBookEntry(id: String, newText: String) {
        if (newText.isNotBlank()) {
            db.collection("publicGeneralNotes").document(id).update("text", newText.trim())
        }
    }

    fun updatePublicGrudgeBookEntry(id: String, newText: String) {
        if (newText.isNotBlank()) {
            db.collection("publicGrudgeNotes").document(id).update("text", newText.trim())
        }
    }

    private fun listenToGroupChat() {
        db.collection("groupChat")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firebase", "Gruppen-Chat Listener Fehler", e)
                    characterVm.snackbarMessage.value = "Gruppen-Chat nicht erreichbar"
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    groupChatMessages.clear()
                    for (doc in snapshot.documents) {
                        val msg = doc.toObject(GroupChatMessage::class.java)
                        if (msg != null) {
                            groupChatMessages.add(msg)
                        }
                    }
                }
            }
    }

    fun sendGroupMessage(text: String, isOoc: Boolean) {
        if (text.isNotBlank()) {
            val msg = GroupChatMessage(
                text = text.trim(),
                author = characterVm.characterData.name,
                charClass = characterVm.characterData.charClass,
                isOoc = isOoc
            )
            db.collection("groupChat").document(msg.id).set(msg)
        }
    }

    fun deleteGroupChat(isOoc: Boolean) {
        val messagesToDelete = groupChatMessages.filter { it.isOoc == isOoc }
        val batch = db.batch()
        messagesToDelete.forEach { msg ->
            val docRef = db.collection("groupChat").document(msg.id)
            batch.delete(docRef)
        }
        batch.commit()
    }

    private fun listenToQuests() {
        db.collection("globalQuests")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firebase", "Questlog Listener Fehler", e)
                    characterVm.snackbarMessage.value = "Questlog nicht erreichbar"
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    globalQuests.clear()
                    for (doc in snapshot.documents) {
                        val quest = doc.toObject(Quest::class.java)
                        if (quest != null) {
                            globalQuests.add(quest)
                        }
                    }
                }
            }
    }

    fun addQuest(title: String, description: String) {
        if (title.isNotBlank()) {
            val quest = Quest(
                title = title.trim(),
                description = description.trim()
            )
            db.collection("globalQuests").document(quest.id).set(quest)
        }
    }

    fun toggleQuestCompletion(quest: Quest) {
        val newState = !quest.isCompleted
        db.collection("globalQuests").document(quest.id).update("isCompleted", newState)
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

    fun listenToSharedLoot() {
        db.collection("groupLootCoins").document("shared").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("Firebase", "Gruppen-Münzen Listener Fehler", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val coins = snapshot.toObject(SharedCoins::class.java)
                if (coins != null) {
                    sharedCoins = coins
                }
            }
        }
        db.collection("groupLootItems").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("Firebase", "Gruppen-Beute Listener Fehler", e)
                return@addSnapshotListener
            }
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
        val newCoins = SharedCoins(km, sm, em, gm, pm)
        db.collection("groupLootCoins").document("shared").set(newCoins)
    }

    fun addSharedLootItem(name: String, amount: Int, weight: Double, category: String) {
        val existing = sharedLootItems.find { it.name.trim().equals(name.trim(), ignoreCase = true) }
        if (existing != null) {
            val updatedAmount = existing.amount + amount
            if (updatedAmount <= 0) {
                db.collection("groupLootItems").document(existing.id).delete()
            } else {
                db.collection("groupLootItems").document(existing.id).update("amount", updatedAmount)
            }
        } else if (amount > 0) {
            val item = GroupLootItem(name = name.trim(), amount = amount, weight = weight, category = category)
            db.collection("groupLootItems").document(item.id).set(item)
        }
    }

    fun updateSharedLootItem(id: String, amount: Int) {
        if (amount <= 0) {
            db.collection("groupLootItems").document(id).delete()
        } else {
            db.collection("groupLootItems").document(id).update("amount", amount)
        }
    }

    fun deleteSharedLootItem(id: String) {
        db.collection("groupLootItems").document(id).delete()
    }
}
