package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.BookEntry
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.viewmodel.GroupViewModel
import com.example.dndcompanion.ui.viewmodel.Quest
import java.text.SimpleDateFormat
import java.util.*

private val GRUDGE_CHARACTERS = listOf("Delat", "Sora", "Tharion", "Vahlok")
private val GrollHintergrund = Color(0xFFFFCDD2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailView(bookType: BookType, viewModel: CharacterViewModel, groupVm: GroupViewModel, onBack: () -> Unit) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    PergamentBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WaldgruenDunkel)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = PergamentHell)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Notizbuch", fontSize = 24.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = PergamentHell)
            }

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = WaldgruenDunkel,
                contentColor = PergamentHell,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = PergamentHell
                    )
                }
            ) {
                listOf("Persönlich", "Gruppe", "Quests").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontFamily = Almendra, fontWeight = FontWeight.Bold) },
                        selectedContentColor = PergamentHell,
                        unselectedContentColor = PergamentHell.copy(alpha = 0.7f),
                        modifier = Modifier.height(48.dp)
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> PersonalNotesTab(viewModel = viewModel)
                1 -> GroupNotesTab(viewModel = viewModel, groupVm = groupVm)
                2 -> QuestsTab(groupVm = groupVm)
            }
        }
    }
}

@Composable
private fun PersonalNotesTab(viewModel: CharacterViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var grudgeFilter by remember { mutableStateOf<Boolean?>(null) } // null=alle, true=nur Groll, false=kein Groll
    var selectedCharacters by remember { mutableStateOf(emptySet<String>()) }

    var newEntryText by remember { mutableStateOf("") }
    var newIsGrudge by remember { mutableStateOf(false) }
    var newGrudgeTargets by remember { mutableStateOf(emptySet<String>()) }

    var editingEntryId by remember { mutableStateOf<String?>(null) }
    var editText by remember { mutableStateOf("") }
    var editIsGrudge by remember { mutableStateOf(false) }
    var editGrudgeTargets by remember { mutableStateOf(emptySet<String>()) }

    val filteredEntries = viewModel.generalBookEntries.filter { entry ->
        val matchesText = searchQuery.isBlank() || entry.text.contains(searchQuery, ignoreCase = true)
        val matchesGroll = when (grudgeFilter) {
            true -> entry.isGrudge
            false -> !entry.isGrudge
            null -> true
        }
        val matchesCharacter = selectedCharacters.isEmpty() ||
            entry.grudgeTargets.any { it in selectedCharacters }
        matchesText && matchesGroll && matchesCharacter
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Suchfeld
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Notizen durchsuchen...", color = TintenSchwarz.copy(alpha = 0.6f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WaldgruenDunkel) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Suche löschen", tint = WaldgruenDunkel)
                    }
                }
            },
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 16.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WaldgruenDunkel,
                unfocusedBorderColor = WaldgruenDunkel.copy(alpha = 0.5f)
            )
        )

        // Filterzeile
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = grudgeFilter == null && selectedCharacters.isEmpty(),
                onClick = { grudgeFilter = null; selectedCharacters = emptySet() },
                label = { Text("Alle", fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WaldgruenDunkel,
                    selectedLabelColor = PergamentHell
                )
            )
            FilterChip(
                selected = grudgeFilter == true,
                onClick = { grudgeFilter = if (grudgeFilter == true) null else true },
                label = { Text("Groll", fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OchsenblutRot,
                    selectedLabelColor = PergamentHell
                )
            )
            FilterChip(
                selected = grudgeFilter == false,
                onClick = { grudgeFilter = if (grudgeFilter == false) null else false; selectedCharacters = emptySet() },
                label = { Text("Kein Groll", fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WaldgruenDunkel,
                    selectedLabelColor = PergamentHell
                )
            )
            if (grudgeFilter != false) {
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(TintenSchwarz.copy(alpha = 0.3f))
                )
                GRUDGE_CHARACTERS.forEach { char ->
                    FilterChip(
                        selected = char in selectedCharacters,
                        onClick = {
                            selectedCharacters = if (char in selectedCharacters)
                                selectedCharacters - char
                            else
                                selectedCharacters + char
                            if (selectedCharacters.isNotEmpty()) grudgeFilter = true
                        },
                        label = { Text(char, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OchsenblutRot.copy(alpha = 0.75f),
                            selectedLabelColor = PergamentHell
                        )
                    )
                }
            }
        }

        // Eingabe- oder Bearbeitungsformular
        if (editingEntryId == null) {
            SteinCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = newEntryText,
                        onValueChange = { newEntryText = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        placeholder = { Text("Neuer Eintrag...", color = TintenSchwarz.copy(alpha = 0.6f)) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 16.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (newIsGrudge) OchsenblutRot else WaldgruenDunkel,
                            unfocusedBorderColor = if (newIsGrudge) OchsenblutRot.copy(alpha = 0.5f) else WaldgruenDunkel.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = newIsGrudge,
                            onCheckedChange = { newIsGrudge = it; if (!it) newGrudgeTargets = emptySet() },
                            colors = SwitchDefaults.colors(checkedThumbColor = PergamentHell, checkedTrackColor = OchsenblutRot)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Groll markieren",
                            color = if (newIsGrudge) OchsenblutRot else TintenSchwarz,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (newIsGrudge) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Gegen wen?", color = TintenSchwarz.copy(alpha = 0.7f), fontSize = 13.sp)
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GRUDGE_CHARACTERS.forEach { char ->
                                FilterChip(
                                    selected = char in newGrudgeTargets,
                                    onClick = {
                                        newGrudgeTargets = if (char in newGrudgeTargets)
                                            newGrudgeTargets - char
                                        else
                                            newGrudgeTargets + char
                                    },
                                    label = { Text(char) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = OchsenblutRot,
                                        selectedLabelColor = PergamentHell
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (newEntryText.isNotBlank()) {
                                viewModel.addGeneralBookEntry(
                                    text = newEntryText.trim(),
                                    isGrudge = newIsGrudge,
                                    grudgeTargets = newGrudgeTargets.toList()
                                )
                                newEntryText = ""
                                newIsGrudge = false
                                newGrudgeTargets = emptySet()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (newIsGrudge) OchsenblutRot else WaldgruenDunkel
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End).height(48.dp)
                    ) {
                        Text("Speichern", fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        } else {
            SteinCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Eintrag bearbeiten",
                        color = if (editIsGrudge) OchsenblutRot else WaldgruenDunkel,
                        fontFamily = Almendra,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 16.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (editIsGrudge) OchsenblutRot else WaldgruenDunkel,
                            unfocusedBorderColor = if (editIsGrudge) OchsenblutRot.copy(alpha = 0.5f) else WaldgruenDunkel.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = editIsGrudge,
                            onCheckedChange = { editIsGrudge = it; if (!it) editGrudgeTargets = emptySet() },
                            colors = SwitchDefaults.colors(checkedThumbColor = PergamentHell, checkedTrackColor = OchsenblutRot)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Groll markieren",
                            color = if (editIsGrudge) OchsenblutRot else TintenSchwarz,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (editIsGrudge) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Gegen wen?", color = TintenSchwarz.copy(alpha = 0.7f), fontSize = 13.sp)
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GRUDGE_CHARACTERS.forEach { char ->
                                FilterChip(
                                    selected = char in editGrudgeTargets,
                                    onClick = {
                                        editGrudgeTargets = if (char in editGrudgeTargets)
                                            editGrudgeTargets - char
                                        else
                                            editGrudgeTargets + char
                                    },
                                    label = { Text(char) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = OchsenblutRot,
                                        selectedLabelColor = PergamentHell
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { editingEntryId = null }, modifier = Modifier.height(48.dp)) {
                            Text("Abbrechen", color = TintenSchwarz, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (editText.isNotBlank()) {
                                    viewModel.updateGeneralBookEntry(
                                        id = editingEntryId!!,
                                        newText = editText.trim(),
                                        isGrudge = editIsGrudge,
                                        grudgeTargets = editGrudgeTargets.toList()
                                    )
                                    editingEntryId = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (editIsGrudge) OchsenblutRot else WaldgruenDunkel
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Aktualisieren", fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredEntries) { entry ->
                PersonalNoteCard(
                    entry = entry,
                    onEdit = {
                        editingEntryId = entry.id
                        editText = entry.text
                        editIsGrudge = entry.isGrudge
                        editGrudgeTargets = entry.grudgeTargets.toSet()
                    },
                    onDelete = { viewModel.deleteGeneralBookEntry(entry.id) }
                )
            }
        }
    }
}

@Composable
private fun GroupNotesTab(viewModel: CharacterViewModel, groupVm: GroupViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var newEntryText by remember { mutableStateOf("") }
    var editingEntryId by remember { mutableStateOf<String?>(null) }
    var editText by remember { mutableStateOf("") }

    val filteredEntries = groupVm.publicGeneralBookEntries.filter {
        it.text.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Gruppen-Einträge durchsuchen...", color = TintenSchwarz.copy(alpha = 0.6f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WaldgruenDunkel) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Suche löschen", tint = WaldgruenDunkel)
                    }
                }
            },
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 16.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WaldgruenDunkel,
                unfocusedBorderColor = WaldgruenDunkel.copy(alpha = 0.5f)
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (editingEntryId == null) {
            SteinCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = newEntryText,
                        onValueChange = { newEntryText = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        placeholder = { Text("Neue globale Notiz...", color = TintenSchwarz.copy(alpha = 0.6f)) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 16.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WaldgruenDunkel,
                            unfocusedBorderColor = WaldgruenDunkel.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (newEntryText.isNotBlank()) {
                                val tempEntry = BookEntry(text = newEntryText.trim(), isPublic = true)
                                groupVm.addPublicGeneralBookEntry(tempEntry.id, newEntryText)
                                newEntryText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WaldgruenDunkel),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End).height(48.dp)
                    ) {
                        Text("Speichern", fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        } else {
            SteinCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Eintrag bearbeiten", color = WaldgruenDunkel, fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 16.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WaldgruenDunkel,
                            unfocusedBorderColor = WaldgruenDunkel.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { editingEntryId = null }, modifier = Modifier.height(48.dp)) {
                            Text("Abbrechen", color = TintenSchwarz, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (editText.isNotBlank()) {
                                    groupVm.updatePublicGeneralBookEntry(editingEntryId!!, editText)
                                    editingEntryId = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WaldgruenDunkel),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Aktualisieren", fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(filteredEntries) { entry ->
                BookEntryCard(entry = entry, onEdit = {
                    editingEntryId = entry.id
                    editText = entry.text
                })
            }
        }
    }
}

@Composable
private fun QuestsTab(groupVm: GroupViewModel) {
    var newQuestTitle by remember { mutableStateOf("") }
    var newQuestDesc by remember { mutableStateOf("") }

    val activeQuests = groupVm.globalQuests.filter { !it.isCompleted }.sortedByDescending { it.timestamp }
    val completedQuests = groupVm.globalQuests.filter { it.isCompleted }.sortedByDescending { it.timestamp }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SteinCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = newQuestTitle,
                    onValueChange = { newQuestTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Quest-Titel...", color = TintenSchwarz.copy(alpha = 0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WaldgruenDunkel,
                        unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f)
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newQuestDesc,
                    onValueChange = { newQuestDesc = it },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    placeholder = { Text("Beschreibung (optional)...", color = TintenSchwarz.copy(alpha = 0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WaldgruenDunkel,
                        unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f)
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 14.sp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (newQuestTitle.isNotBlank()) {
                            groupVm.addQuest(newQuestTitle, newQuestDesc)
                            newQuestTitle = ""
                            newQuestDesc = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WaldgruenDunkel),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End).height(48.dp)
                ) {
                    Text("Quest hinzufügen", fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PergamentHell)
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            if (activeQuests.isNotEmpty()) {
                item {
                    Text(
                        "Aktive Quests",
                        fontFamily = Almendra,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = WaldgruenDunkel,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(activeQuests) { quest -> QuestCard(quest = quest, groupVm = groupVm) }
            }
            if (completedQuests.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = TintenSchwarz.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Abgeschlossen",
                        fontFamily = Almendra,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TintenSchwarz.copy(alpha = 0.45f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(completedQuests) { quest -> QuestCard(quest = quest, groupVm = groupVm) }
            }
            if (activeQuests.isEmpty() && completedQuests.isEmpty()) {
                item {
                    Text(
                        "Keine Quests vorhanden.",
                        color = TintenSchwarz.copy(alpha = 0.5f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestCard(quest: Quest, groupVm: GroupViewModel) {
    PergamentCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quest.title,
                    fontFamily = Almendra,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (quest.isCompleted) TintenSchwarz.copy(alpha = 0.5f) else Waldgruen,
                    modifier = Modifier.weight(1f),
                    textDecoration = if (quest.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                Checkbox(
                    checked = quest.isCompleted,
                    onCheckedChange = { groupVm.toggleQuestCompletion(quest) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = WaldgruenDunkel,
                        uncheckedColor = TintenSchwarz.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.size(48.dp).padding(8.dp)
                )
                IconButton(onClick = { groupVm.deleteQuest(quest.id) }, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Löschen",
                        tint = if (quest.isCompleted) TintenSchwarz.copy(alpha = 0.4f) else MaterialTheme.colorScheme.error
                    )
                }
            }
            if (quest.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = quest.description,
                    fontSize = 14.sp,
                    color = if (quest.isCompleted) TintenSchwarz.copy(alpha = 0.5f) else TintenSchwarz,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
private fun PersonalNoteCard(entry: BookEntry, onEdit: () -> Unit, onDelete: () -> Unit) {
    val dateStr = remember(entry.timestamp) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(entry.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isGrudge) GrollHintergrund else PergamentHell
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(dateStr, style = GrenzeGotischSmall, color = TintenSchwarz.copy(alpha = 0.7f))
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Bearbeiten",
                            tint = TintenSchwarz.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Löschen",
                            tint = if (entry.isGrudge) OchsenblutRot.copy(alpha = 0.7f) else TintenSchwarz.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(entry.text, fontSize = 16.sp, color = TintenSchwarz, lineHeight = 24.sp)
            if (entry.grudgeTargets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    entry.grudgeTargets.forEach { char ->
                        Surface(
                            color = OchsenblutRot.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                char,
                                fontSize = 12.sp,
                                color = OchsenblutRot,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Wird noch für Gruppen-Tab verwendet
@Composable
fun BookEntryCard(entry: BookEntry, onEdit: () -> Unit) {
    val dateStr = remember(entry.timestamp) {
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(entry.timestamp))
    }

    PergamentCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(dateStr, style = GrenzeGotischSmall, color = TintenSchwarz.copy(alpha = 0.7f))
                IconButton(onClick = onEdit, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Bearbeiten", tint = TintenSchwarz.copy(alpha = 0.6f))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(entry.text, fontSize = 16.sp, color = TintenSchwarz, lineHeight = 24.sp)
        }
    }
}
