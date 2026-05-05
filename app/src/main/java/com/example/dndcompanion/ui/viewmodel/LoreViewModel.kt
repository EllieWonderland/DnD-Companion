package com.example.dndcompanion.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LoreViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    val loreQuests = mutableStateListOf<LoreQuest>()

    init {
        listenToLoreQuests()
    }

    private fun listenToLoreQuests() {
        db.collection("loreQuests")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firebase", "LoreQuests Listener Fehler", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    loreQuests.clear()
                    for (doc in snapshot.documents) {
                        val quest = doc.toObject(LoreQuest::class.java)
                        if (quest != null) loreQuests.add(quest)
                    }
                }
            }
    }

    fun addLoreQuest(title: String, description: String, status: LoreQuestStatus, location: String) {
        if (title.isNotBlank()) {
            val quest = LoreQuest(
                title = title.trim(),
                description = description.trim(),
                status = status.name,
                location = location.trim()
            )
            db.collection("loreQuests").document(quest.id).set(quest)
        }
    }

    fun updateLoreQuest(id: String, title: String, description: String, status: LoreQuestStatus, location: String) {
        db.collection("loreQuests").document(id).update(
            "title", title.trim(),
            "description", description.trim(),
            "status", status.name,
            "location", location.trim()
        )
    }

    fun deleteLoreQuest(id: String) {
        db.collection("loreQuests").document(id).delete()
    }
}
