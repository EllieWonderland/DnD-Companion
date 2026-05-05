package com.example.dndcompanion.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

class LoreViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference
    private val currentUid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val loreQuests = mutableStateListOf<LoreQuest>()
    val loreMaps = mutableStateListOf<LoreMap>()
    val loreHouserules = mutableStateListOf<LoreHouserule>()
    val loreStories = mutableStateListOf<LoreStory>()

    var isUploading by mutableStateOf(false)
        private set

    init {
        listenToLoreQuests()
        listenToLoreMaps()
        listenToLoreHouserules()
        listenToLoreStories()
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

    private fun listenToLoreMaps() {
        db.collection("loreMaps")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firebase", "LoreMaps Listener Fehler", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    loreMaps.clear()
                    for (doc in snapshot.documents) {
                        val map = doc.toObject(LoreMap::class.java)
                        if (map != null) loreMaps.add(map)
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

    private fun listenToLoreHouserules() {
        db.collection("loreHouserules")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firebase", "LoreHouserules Listener Fehler", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    loreHouserules.clear()
                    for (doc in snapshot.documents) {
                        val hr = doc.toObject(LoreHouserule::class.java)
                        if (hr != null) loreHouserules.add(hr)
                    }
                }
            }
    }

    fun addLoreHouserule(title: String, ruleText: String, category: String) {
        if (title.isBlank()) return
        val uid = currentUid
        val hr = LoreHouserule(
            title = title.trim(),
            ruleText = ruleText.trim(),
            category = category,
            createdBy = uid
        )
        db.collection("loreHouserules").document(hr.id).set(hr)
    }

    fun updateLoreHouserule(id: String, title: String, ruleText: String, category: String) {
        db.collection("loreHouserules").document(id).update(
            "title", title.trim(),
            "ruleText", ruleText.trim(),
            "category", category
        )
    }

    fun deleteLoreHouserule(id: String) {
        db.collection("loreHouserules").document(id).delete()
    }

    private fun listenToLoreStories() {
        db.collection("loreStories")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firebase", "LoreStories Listener Fehler", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    loreStories.clear()
                    for (doc in snapshot.documents) {
                        val story = doc.toObject(LoreStory::class.java)
                        if (story != null) loreStories.add(story)
                    }
                }
            }
    }

    fun addLoreStory(title: String, text: String, author: String) {
        val uid = currentUid
        if (uid.isEmpty() || title.isBlank()) return
        val story = LoreStory(title = title.trim(), text = text.trim(), author = author.trim(), createdBy = uid)
        db.collection("loreStories").document(story.id).set(story)
    }

    fun updateLoreStory(id: String, title: String, text: String, author: String) {
        db.collection("loreStories").document(id).update(
            "title", title.trim(),
            "text", text.trim(),
            "author", author.trim()
        )
    }

    fun deleteLoreStory(id: String) {
        db.collection("loreStories").document(id).delete()
    }

    fun uploadMap(imageUri: Uri, title: String, description: String) {
        if (isUploading) return
        val uid = currentUid
        if (uid.isEmpty()) return
        isUploading = true
        val timestamp = System.currentTimeMillis()
        val ref = storageRef.child("lore/maps/$uid/$timestamp")
        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        val map = LoreMap(
                            url = downloadUri.toString(),
                            title = title.trim(),
                            description = description.trim(),
                            uploadedBy = uid,
                            timestamp = timestamp
                        )
                        db.collection("loreMaps").document(map.id).set(map)
                            .addOnSuccessListener { isUploading = false }
                            .addOnFailureListener { isUploading = false }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firebase", "Download URL Fehler", e)
                        isUploading = false
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Map Upload Fehler", e)
                isUploading = false
            }
    }

    fun deleteLoreMap(map: LoreMap) {
        val uid = currentUid
        if (map.uploadedBy != uid) return
        db.collection("loreMaps").document(map.id).delete()
        storageRef.child("lore/maps/${map.uploadedBy}/${map.timestamp}").delete()
            .addOnFailureListener { Log.w("Firebase", "Storage-Löschen fehlgeschlagen", it) }
    }
}
