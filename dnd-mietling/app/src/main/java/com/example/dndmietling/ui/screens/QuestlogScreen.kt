package com.example.dndmietling.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.dndmietling.data.GroupLootItem
import com.example.dndmietling.data.Quest
import com.example.dndmietling.data.SharedCoins
import com.example.dndmietling.ui.theme.*
import com.example.dndmietling.ui.viewmodel.MietlingViewModel
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

@Composable
fun QuestlogScreen(viewModel: MietlingViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().background(Pergament)) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = WaldgruenDunkel,
            contentColor = PergamentHell
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("📜 Questlog") },
                selectedContentColor = WaldGold,
                unselectedContentColor = PergamentDunkel
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("💰 Gruppen-Loot") },
                selectedContentColor = WaldGold,
                unselectedContentColor = PergamentDunkel
            )
        }

        when (selectedTab) {
            0 -> QuestListTab(viewModel)
            1 -> GruppenLootTab(viewModel)
        }
    }
}

// ============================================================
// QUEST-LIST TAB
// ============================================================

@Composable
private fun QuestListTab(viewModel: MietlingViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingQuest by remember { mutableStateOf<Quest?>(null) }
    var expandedQuestId by remember { mutableStateOf<String?>(null) }

    // Offene Quests oben, abgeschlossene unten – in einer gemeinsamen Liste
    val openQuests = viewModel.quests.filter { !it.isCompleted }.sortedByDescending { it.timestamp }
    val doneQuests = viewModel.quests.filter { it.isCompleted }.sortedByDescending { it.timestamp }

    Box(modifier = Modifier.fillMaxSize()) {
        if (viewModel.quests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📜", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Keine Quests", color = TintenBraun, fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Offene Quests
                if (openQuests.isNotEmpty()) {
                    item {
                        Text(
                            "Offene Quests",
                            fontWeight = FontWeight.Bold,
                            color = Waldgruen,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(openQuests, key = { it.id }) { quest ->
                        QuestCard(
                            quest = quest,
                            isExpanded = expandedQuestId == quest.id,
                            onToggleExpand = {
                                expandedQuestId = if (expandedQuestId == quest.id) null else quest.id
                            },
                            onToggleComplete = { viewModel.toggleQuestCompletion(quest) },
                            onEdit = { editingQuest = quest },
                            onDelete = { viewModel.deleteQuest(quest.id) },
                            onAddPhoto = { url -> viewModel.addPhotoToQuest(quest.id, url) }
                        )
                    }
                }

                // Abgeschlossene Quests – direkt darunter, KEIN Untertab
                if (doneQuests.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = PergamentDunkel)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "✅ Abgeschlossen",
                            fontWeight = FontWeight.Bold,
                            color = EisenGrau,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(doneQuests, key = { it.id }) { quest ->
                        QuestCard(
                            quest = quest,
                            isExpanded = expandedQuestId == quest.id,
                            onToggleExpand = {
                                expandedQuestId = if (expandedQuestId == quest.id) null else quest.id
                            },
                            onToggleComplete = { viewModel.toggleQuestCompletion(quest) },
                            onEdit = { editingQuest = quest },
                            onDelete = { viewModel.deleteQuest(quest.id) },
                            onAddPhoto = { url -> viewModel.addPhotoToQuest(quest.id, url) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Waldgruen
        ) {
            Icon(Icons.Default.Add, contentDescription = "Quest hinzufügen", tint = Color.White)
        }
    }

    if (showAddDialog) {
        QuestDialog(
            initialTitle = "",
            initialDescription = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc ->
                viewModel.addQuest(title, desc)
                showAddDialog = false
            }
        )
    }

    editingQuest?.let { quest ->
        QuestDialog(
            initialTitle = quest.title,
            initialDescription = quest.description,
            onDismiss = { editingQuest = null },
            onConfirm = { title, desc ->
                viewModel.updateQuest(quest.id, title, desc)
                editingQuest = null
            }
        )
    }
}

@Composable
private fun QuestCard(
    quest: Quest,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddPhoto: (String) -> Unit
) {
    val context = LocalContext.current
    var uploadingPhoto by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadingPhoto = true
            val storage = FirebaseStorage.getInstance()
            val ref = storage.reference.child("questPhotos/${UUID.randomUUID()}.jpg")
            ref.putFile(it)
                .continueWithTask { task ->
                    if (!task.isSuccessful) throw task.exception!!
                    ref.downloadUrl
                }
                .addOnSuccessListener { downloadUri ->
                    onAddPhoto(downloadUri.toString())
                    uploadingPhoto = false
                }
                .addOnFailureListener { uploadingPhoto = false }
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (quest.isCompleted) PergamentDunkel.copy(alpha = 0.5f) else PergamentHell
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header-Zeile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = quest.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    colors = CheckboxDefaults.colors(checkedColor = Waldgruen)
                )
                Text(
                    quest.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = if (quest.isCompleted) EisenGrau else TintenSchwarz,
                    modifier = Modifier.weight(1f)
                )
                if (quest.photoUrls.isNotEmpty()) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Bronze, modifier = Modifier.size(16.dp))
                    Text("${quest.photoUrls.size}", fontSize = 11.sp, color = Bronze)
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = EisenGrau
                )
            }

            // Aufgeklappter Inhalt
            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                if (quest.description.isNotBlank()) {
                    Text(quest.description, fontSize = 14.sp, color = TintenBraun)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Foto-Galerie
                if (quest.photoUrls.isNotEmpty()) {
                    Text("Karten & Fotos:", fontSize = 13.sp, color = TintenBraun, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(quest.photoUrls) { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = "Quest Foto",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Aktionen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uploadingPhoto) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Waldgruen)
                    } else {
                        OutlinedButton(
                            onClick = { photoLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Foto", fontSize = 13.sp)
                        }
                    }
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bearbeiten", fontSize = 13.sp)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Löschen", tint = OchsenblutRot)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestDialog(
    initialTitle: String,
    initialDescription: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = PergamentHell)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    if (initialTitle.isEmpty()) "Neue Quest" else "Quest bearbeiten",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Waldgruen
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Waldgruen)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Beschreibung") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Waldgruen)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Abbrechen") }
                    Button(
                        onClick = { if (title.isNotBlank()) onConfirm(title, description) },
                        colors = ButtonDefaults.buttonColors(containerColor = Waldgruen),
                        modifier = Modifier.weight(1f)
                    ) { Text("Speichern") }
                }
            }
        }
    }
}

// ============================================================
// GRUPPEN-LOOT TAB
// ============================================================

@Composable
private fun GruppenLootTab(viewModel: MietlingViewModel) {
    var playerCount by remember { mutableStateOf("3") }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showCoinDialog by remember { mutableStateOf(false) }

    val coins = viewModel.sharedCoins
    val playerCountInt = playerCount.toIntOrNull()?.coerceAtLeast(1) ?: 1

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Pergament),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Spieleranzahl
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PergamentHell)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("👥 Spieler heute:", fontWeight = FontWeight.SemiBold, color = TintenSchwarz)
                    OutlinedTextField(
                        value = playerCount,
                        onValueChange = { playerCount = it },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(80.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Waldgruen)
                    )
                }
            }
        }

        // Münzen
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PergamentHell),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💰 Münzen", fontWeight = FontWeight.Bold, color = Waldgruen, fontSize = 16.sp)
                        IconButton(onClick = { showCoinDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Münzen bearbeiten", tint = Bronze)
                        }
                    }

                    // Münz-Anzeige
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(
                            "KM" to coins.km,
                            "SM" to coins.sm,
                            "EM" to coins.em,
                            "GM" to coins.gm,
                            "PM" to coins.pm
                        ).forEach { (label, value) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(label, fontSize = 11.sp, color = TintenBraun)
                                Text("$value", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TintenSchwarz)
                            }
                        }
                    }

                    // Pro-Spieler-Aufteilung
                    if (playerCountInt > 0) {
                        HorizontalDivider(color = PergamentDunkel)
                        Text("Pro Spieler ($playerCountInt Spieler):", fontSize = 13.sp, color = TintenBraun, fontWeight = FontWeight.SemiBold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            listOf(
                                "KM" to coins.km,
                                "SM" to coins.sm,
                                "EM" to coins.em,
                                "GM" to coins.gm,
                                "PM" to coins.pm
                            ).forEach { (label, value) ->
                                val perPlayer = value / playerCountInt
                                val rest = value % playerCountInt
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(label, fontSize = 11.sp, color = TintenBraun)
                                    Text("$perPlayer", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Waldgruen)
                                    if (rest > 0) Text("(+$rest)", fontSize = 10.sp, color = OchsenblutRot)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Loot-Items
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎒 Beute-Gegenstände", fontWeight = FontWeight.Bold, color = Waldgruen, fontSize = 16.sp)
                Button(
                    onClick = { showAddItemDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Waldgruen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hinzufügen", fontSize = 13.sp)
                }
            }
        }

        if (viewModel.sharedLootItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Keine Beute eingetragen", color = EisenGrau, fontSize = 14.sp)
                }
            }
        } else {
            items(viewModel.sharedLootItems, key = { it.id }) { item ->
                LootItemCard(
                    item = item,
                    playerCount = playerCountInt,
                    onDelete = { viewModel.deleteSharedLootItem(item.id) }
                )
            }
        }
    }

    if (showAddItemDialog) {
        AddLootItemDialog(
            onDismiss = { showAddItemDialog = false },
            onAdd = { name, amount, weight, category ->
                viewModel.addSharedLootItem(name, amount, weight, category)
                showAddItemDialog = false
            }
        )
    }

    if (showCoinDialog) {
        CoinEditDialog(
            coins = coins,
            onDismiss = { showCoinDialog = false },
            onSave = { km, sm, em, gm, pm ->
                viewModel.updateSharedCoins(km, sm, em, gm, pm)
                showCoinDialog = false
            }
        )
    }
}

@Composable
private fun LootItemCard(item: GroupLootItem, playerCount: Int, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = PergamentHell),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.SemiBold, color = TintenSchwarz, fontSize = 15.sp)
                Text("${item.amount}x · ${item.category}", fontSize = 12.sp, color = TintenBraun)
            }
            if (playerCount > 1 && item.amount >= playerCount) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${item.amount / playerCount}x pro Spieler", fontSize = 11.sp, color = Waldgruen)
                    val rest = item.amount % playerCount
                    if (rest > 0) Text("+$rest übrig", fontSize = 10.sp, color = OchsenblutRot)
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Entfernen", tint = OchsenblutRot, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun AddLootItemDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("1") }
    var weight by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Sonstiges") }
    val categories = listOf("Waffe", "Rüstung", "Werkzeug", "Schriftrolle", "Wertgegenstand", "Sonstiges")

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = PergamentHell)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Beute hinzufügen", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Waldgruen)
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Waldgruen)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amount, onValueChange = { amount = it },
                        label = { Text("Anzahl") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Waldgruen)
                    )
                    OutlinedTextField(
                        value = weight, onValueChange = { weight = it },
                        label = { Text("Gewicht (kg)") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Waldgruen)
                    )
                }
                // Kategorie
                Text("Kategorie:", fontSize = 13.sp, color = TintenBraun)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat, onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }, modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Waldgruen, selectedLabelColor = Color.White)
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.drop(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat, onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }, modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Waldgruen, selectedLabelColor = Color.White)
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Abbrechen") }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onAdd(name, amount.toIntOrNull() ?: 1, weight.toDoubleOrNull() ?: 0.0, category)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Waldgruen),
                        modifier = Modifier.weight(1f)
                    ) { Text("Hinzufügen") }
                }
            }
        }
    }
}

@Composable
private fun CoinEditDialog(
    coins: SharedCoins,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int, Int, Int) -> Unit
) {
    var km by remember { mutableStateOf("${coins.km}") }
    var sm by remember { mutableStateOf("${coins.sm}") }
    var em by remember { mutableStateOf("${coins.em}") }
    var gm by remember { mutableStateOf("${coins.gm}") }
    var pm by remember { mutableStateOf("${coins.pm}") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = PergamentHell)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Münzen bearbeiten", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Waldgruen)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf("KM" to km, "SM" to sm, "EM" to em, "GM" to gm, "PM" to pm)
                        .zip(listOf({ v: String -> km = v }, { v: String -> sm = v }, { v: String -> em = v }, { v: String -> gm = v }, { v: String -> pm = v }))
                        .forEach { (labelVal, setter) ->
                            val (label, value) = labelVal
                            OutlinedTextField(
                                value = value,
                                onValueChange = setter,
                                label = { Text(label, fontSize = 11.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(60.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WaldGold)
                            )
                        }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Abbrechen") }
                    Button(
                        onClick = {
                            onSave(
                                km.toIntOrNull() ?: 0,
                                sm.toIntOrNull() ?: 0,
                                em.toIntOrNull() ?: 0,
                                gm.toIntOrNull() ?: 0,
                                pm.toIntOrNull() ?: 0
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Waldgruen),
                        modifier = Modifier.weight(1f)
                    ) { Text("Speichern") }
                }
            }
        }
    }
}
