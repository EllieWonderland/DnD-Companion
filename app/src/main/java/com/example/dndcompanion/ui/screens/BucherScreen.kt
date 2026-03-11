package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.Material3RichText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.viewmodel.BookEntry
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.draw.scale

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.aspectRatio
import com.example.dndcompanion.ui.theme.PergamentBackground
import com.example.dndcompanion.ui.theme.PergamentCard
import com.example.dndcompanion.ui.theme.PergamentHell
import com.example.dndcompanion.ui.theme.SteinCard
import com.example.dndcompanion.ui.theme.TintenSchwarz
import com.example.dndcompanion.ui.theme.Almendra
import com.example.dndcompanion.ui.theme.GrenzeGotischSmall
import com.example.dndcompanion.ui.theme.WaldGold
import com.example.dndcompanion.ui.theme.WaldgruenDunkel
import com.example.dndcompanion.ui.theme.OchsenblutRot
import com.example.dndcompanion.R

enum class BookType {
    GENERAL, GRUDGE, SPELLBOOK, RULEBOOK, GROUP_CHAT, QUESTLOG
}

@Composable
fun BucherScreen(viewModel: CharacterViewModel) {
    var activeBook by remember { mutableStateOf<BookType?>(null) }

    LaunchedEffect(viewModel.targetRulebookChapter) {
        if (viewModel.targetRulebookChapter != null) {
            activeBook = BookType.RULEBOOK
        }
    }

    if (activeBook == null) {
        LibraryView(onBookSelected = { activeBook = it })
    } else {
        if (activeBook == BookType.SPELLBOOK) {
            SpellbookDetailView(
                viewModel = viewModel,
                onBack = { activeBook = null }
            )
        } else if (activeBook == BookType.RULEBOOK) {
            RulebookDetailView(
                targetChapter = viewModel.targetRulebookChapter,
                targetSearch = viewModel.targetRulebookSearch,
                onTargetConsumed = { 
                    viewModel.targetRulebookChapter = null 
                    viewModel.targetRulebookSearch = null
                },
                onBack = { 
                    activeBook = null 
                    viewModel.targetRulebookChapter = null
                    viewModel.targetRulebookSearch = null
                }
            )
        } else if (activeBook == BookType.GROUP_CHAT) {
            GroupChatDetailView(
                viewModel = viewModel,
                onBack = { activeBook = null }
            )
        } else if (activeBook == BookType.QUESTLOG) {
            QuestlogDetailView(
                viewModel = viewModel,
                onBack = { activeBook = null }
            )
        } else {
            BookDetailView(
                bookType = activeBook!!,
                viewModel = viewModel,
                onBack = { activeBook = null }
            )
        }
    }
}

@Composable
fun LibraryView(onBookSelected: (BookType) -> Unit) {
    PergamentBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Bibliothek", fontSize = 32.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = TintenSchwarz)
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BookCard(
                    title = "Notizbuch",
                    subtitle = "Allgemeines & Infos",
                    imageRes = R.drawable.notizbuch,
                    onClick = { onBookSelected(BookType.GENERAL) }
                )
                BookCard(
                    title = "Buch des Grolls",
                    subtitle = "Vergeltung wartet",
                    imageRes = R.drawable.buch_des_grolls,
                    onClick = { onBookSelected(BookType.GRUDGE) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BookCard(
                    title = "Zauberbuch",
                    subtitle = "Alle bekannten Zauber",
                    imageRes = R.drawable.zauberbuch,
                    onClick = { onBookSelected(BookType.SPELLBOOK) }
                )
                BookCard(
                    title = "Regelwerk",
                    subtitle = "Handbuch & D&D Regeln",
                    imageRes = R.drawable.regelwerk,
                    onClick = { onBookSelected(BookType.RULEBOOK) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BookCard(
                    title = "Gruppen-Chat",
                    subtitle = "IC & OOC Nachrichten",
                    imageRes = R.drawable.gruppenchat,
                    onClick = { onBookSelected(BookType.GROUP_CHAT) }
                )
                BookCard(
                    title = "Questlog",
                    subtitle = "Aktive & fertige Aufträge",
                    imageRes = R.drawable.questlog,
                    onClick = { onBookSelected(BookType.QUESTLOG) }
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun BookCard(title: String, subtitle: String, imageRes: Int, onClick: () -> Unit) {
    PergamentCard(
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Square image matching 512x512
                    .padding(4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title, 
                fontSize = 18.sp, 
                fontFamily = Almendra, 
                fontWeight = FontWeight.Bold, 
                color = TintenSchwarz, 
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle, 
                style = GrenzeGotischSmall, 
                color = TintenSchwarz.copy(alpha = 0.7f), 
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailView(bookType: BookType, viewModel: CharacterViewModel, onBack: () -> Unit) {
    val privateEntries = if (bookType == BookType.GENERAL) viewModel.generalBookEntries else viewModel.grudgeBookEntries
    val publicEntries = if (bookType == BookType.GENERAL) viewModel.publicGeneralBookEntries else viewModel.publicGrudgeBookEntries
    
    val title = if (bookType == BookType.GENERAL) "Notizbuch" else "Buch des Grolls"
    val tintColor = if (bookType == BookType.GENERAL) WaldgruenDunkel else OchsenblutRot

    var showPublicTab by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var newEntryText by remember { mutableStateOf("") }
    var editingEntryId by remember { mutableStateOf<String?>(null) }
    var editText by remember { mutableStateOf("") }

    val currentEntries = if (showPublicTab) publicEntries else privateEntries
    val filteredEntries = currentEntries.filter { it.text.contains(searchQuery, ignoreCase = true) }

    PergamentBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tintColor)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = WaldGold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontSize = 24.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = WaldGold)
            }

            TabRow(
                selectedTabIndex = if (showPublicTab) 1 else 0,
                containerColor = tintColor,
                contentColor = WaldGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (showPublicTab) 1 else 0]),
                        color = WaldGold
                    )
                }
            ) {
                Tab(
                    selected = !showPublicTab,
                    onClick = { showPublicTab = false },
                    text = { Text("Persönlich", fontFamily = Almendra, fontWeight = FontWeight.Bold) },
                    selectedContentColor = WaldGold,
                    unselectedContentColor = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.height(48.dp)
                )
                Tab(
                    selected = showPublicTab,
                    onClick = { showPublicTab = true },
                    text = { Text("Gruppe", fontFamily = Almendra, fontWeight = FontWeight.Bold) },
                    selectedContentColor = WaldGold,
                    unselectedContentColor = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.height(48.dp)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(if (showPublicTab) "Gruppen-Einträge durchsuchen..." else "Eigene Einträge durchsuchen...", color = TintenSchwarz.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = tintColor) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Suchen löschen", tint = tintColor)
                            }
                        }
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 16.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tintColor,
                        unfocusedBorderColor = tintColor.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // New Entry / Edit Entry
                if (editingEntryId == null) {
                    SteinCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            OutlinedTextField(
                                value = newEntryText,
                                onValueChange = { newEntryText = it },
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                placeholder = { Text(if (showPublicTab) "Neue globale Notiz..." else "Neuer persönlicher Eintrag...", color = TintenSchwarz.copy(alpha = 0.6f)) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 16.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = tintColor,
                                    unfocusedBorderColor = tintColor.copy(alpha = 0.5f)
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (newEntryText.isNotBlank()) {
                                        if (bookType == BookType.GENERAL) {
                                            viewModel.addGeneralBookEntry(newEntryText, showPublicTab)
                                        } else {
                                            viewModel.addGrudgeBookEntry(newEntryText, showPublicTab)
                                        }
                                        newEntryText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = tintColor),
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
                            Text("Eintrag bearbeiten", color = tintColor, fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = editText,
                                onValueChange = { editText = it },
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 16.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = tintColor,
                                    unfocusedBorderColor = tintColor.copy(alpha = 0.5f)
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
                                            if (bookType == BookType.GENERAL) {
                                                viewModel.updateGeneralBookEntry(editingEntryId!!, editText, showPublicTab)
                                            } else {
                                                viewModel.updateGrudgeBookEntry(editingEntryId!!, editText, showPublicTab)
                                            }
                                            editingEntryId = null
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = tintColor),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text("Aktualisieren", fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Entries List
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredEntries) { entry ->
                        BookEntryCard(
                            entry = entry,
                            bookType = bookType,
                            onEdit = {
                                editingEntryId = entry.id
                                editText = entry.text
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookEntryCard(entry: BookEntry, bookType: BookType, onEdit: () -> Unit) {
    val dateStr = remember(entry.timestamp) {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(entry.timestamp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellbookDetailView(viewModel: CharacterViewModel, onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableIntStateOf(-1) }

    var selectedClassFilter by remember { mutableStateOf("Alle") }
    val classFilters = remember(viewModel.globalSpellbook) {
        listOf("Alle") + viewModel.globalSpellbook.flatMap { it.classes }.map { it.trim() }.distinct().sorted()
    }

    var selectedSchoolFilter by remember { mutableStateOf("Alle") }
    val schoolFilters = remember(viewModel.globalSpellbook) {
        listOf("Alle") + viewModel.globalSpellbook.map { it.school.trim() }.distinct().sorted()
    }

    PergamentBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WaldgruenDunkel)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = WaldGold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Zauberbuch", fontSize = 24.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = WaldGold)
            }

            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Zauber suchen...", color = TintenSchwarz.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WaldgruenDunkel) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Suchen löschen", tint = WaldgruenDunkel)
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

                Spacer(modifier = Modifier.height(16.dp))

                val scrollRowState = rememberScrollState()
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(scrollRowState).padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Stufe:", color = TintenSchwarz, fontFamily = Almendra, fontWeight = FontWeight.Bold)
                    val levels = listOf(-1) + (0..9).toList()
                    levels.forEach { lvl ->
                        val hasSpells = lvl == -1 || viewModel.globalSpellbook.any { spell ->
                            spell.level == lvl &&
                            (selectedClassFilter == "Alle" || spell.classes.map { it.trim() }.contains(selectedClassFilter)) &&
                            (selectedSchoolFilter == "Alle" || spell.school.trim() == selectedSchoolFilter)
                        }
                        Button(
                            onClick = { selectedLevel = lvl },
                            enabled = hasSpells,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedLevel == lvl) OchsenblutRot else WaldgruenDunkel,
                                disabledContainerColor = TintenSchwarz.copy(alpha = 0.3f)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            val label = if (lvl == -1) "Alle" else if (lvl == 0) "Tricks" else "Grad $lvl"
                            Text(label, fontSize = 14.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = if (hasSpells) WaldGold else Color.White.copy(alpha = 0.5f))
                        }
                    }
                }

                val classScrollState = rememberScrollState()
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(classScrollState).padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Klasse:", color = TintenSchwarz, fontFamily = Almendra, fontWeight = FontWeight.Bold)
                    classFilters.forEach { filterClass ->
                        val hasSpells = filterClass == "Alle" || viewModel.globalSpellbook.any { spell ->
                            spell.classes.map { it.trim() }.contains(filterClass) &&
                            (selectedLevel == -1 || spell.level == selectedLevel) &&
                            (selectedSchoolFilter == "Alle" || spell.school.trim() == selectedSchoolFilter)
                        }
                        Button(
                            onClick = { selectedClassFilter = filterClass },
                            enabled = hasSpells,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedClassFilter == filterClass) OchsenblutRot else WaldgruenDunkel,
                                disabledContainerColor = TintenSchwarz.copy(alpha = 0.3f)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(filterClass, fontSize = 14.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = if (hasSpells) WaldGold else Color.White.copy(alpha = 0.5f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val schoolScrollState = rememberScrollState()
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(schoolScrollState).padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Schule:", color = TintenSchwarz, fontFamily = Almendra, fontWeight = FontWeight.Bold)
                    schoolFilters.forEach { filterSchool ->
                        val hasSpells = filterSchool == "Alle" || viewModel.globalSpellbook.any { spell ->
                            spell.school.trim() == filterSchool &&
                            (selectedLevel == -1 || spell.level == selectedLevel) &&
                            (selectedClassFilter == "Alle" || spell.classes.map { it.trim() }.contains(selectedClassFilter))
                        }
                        Button(
                            onClick = { selectedSchoolFilter = filterSchool },
                            enabled = hasSpells,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedSchoolFilter == filterSchool) OchsenblutRot else WaldgruenDunkel,
                                disabledContainerColor = TintenSchwarz.copy(alpha = 0.3f)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(filterSchool, fontSize = 14.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = if (hasSpells) WaldGold else Color.White.copy(alpha = 0.5f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val filteredSpells = viewModel.globalSpellbook.filter { spell -> 
                    val matchesClass = if (selectedClassFilter == "Alle") true else spell.classes.map { it.trim() }.contains(selectedClassFilter)
                    val matchesSchool = if (selectedSchoolFilter == "Alle") true else spell.school.trim() == selectedSchoolFilter
                    
                    matchesClass && matchesSchool &&
                    (selectedLevel == -1 || spell.level == selectedLevel) &&
                    (searchQuery.isBlank() || spell.name.contains(searchQuery, ignoreCase = true))
                }.sortedWith(compareBy({ it.level }, { it.name }))

                if (filteredSpells.isEmpty()) {
                    Text("Keine Zauber gefunden.", modifier = Modifier.padding(16.dp), color = TintenSchwarz, fontWeight = FontWeight.Bold)
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(filteredSpells) { catalogSpell ->
                            val alreadyInBook = viewModel.allSpells.any { it.name == catalogSpell.name }
                            val isDruidSpell = catalogSpell.classes.contains("Druide") && !catalogSpell.classes.contains("Waldläufer")
                            val isDruidLevel1 = isDruidSpell && catalogSpell.level == 1
                            val druidLevel1Count = viewModel.allSpells.count { it.classes.contains("Druide") && !it.classes.contains("Waldläufer") && it.level == 1 }
                            
                            SpellCard(
                                spell = catalogSpell,
                                isEditMode = false,
                                isEquipped = alreadyInBook,
                                onTogglePrep = {},
                                onDelete = null,
                                customColor = PergamentHell, // Inherit PergamentCard style
                                extraContent = {
                                    val canEquip = !alreadyInBook && (!isDruidLevel1 || druidLevel1Count < 1)
                                    Button(
                                        onClick = {
                                            if (canEquip) {
                                                viewModel.addNewSpell(catalogSpell.copy(isPrepared = true, id = java.util.UUID.randomUUID().toString()))
                                            }
                                        },
                                        enabled = canEquip || alreadyInBook, // Keep enabled if already in book to show "Bereits ausgerüstet"
                                        colors = ButtonDefaults.buttonColors(containerColor = WaldgruenDunkel, disabledContainerColor = TintenSchwarz.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(48.dp)
                                    ) {
                                        val buttonText = if (alreadyInBook) "Bereits ausgerüstet" else if (!canEquip) "Max 1 Druidenzauber" else "+ Ausrüsten"
                                        Text(buttonText, fontSize = 16.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = WaldGold)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class RulebookChapter(val title: String, val filename: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulebookDetailView(targetChapter: String?, targetSearch: String? = null, onTargetConsumed: () -> Unit, onBack: () -> Unit) {
    val chapters = listOf(
        RulebookChapter("Index", ""), // Fake chapter for Index/Search Tab
        RulebookChapter("1. Gameplay", "Rules/Handbuch/Kapitel/kapitel1_gameplay.md"),
        RulebookChapter("2. Völker", "Rules/Handbuch/Kapitel/kapitel2_races.md"),
        RulebookChapter("3. Klassen", "Rules/Handbuch/Kapitel/kapitel3_classes.md"),
        RulebookChapter("4. Herkünfte", "Rules/Handbuch/Kapitel/kapitel4_origins.md"),
        RulebookChapter("5. Talente", "Rules/Handbuch/Kapitel/kapitel5_talente.md"),
        RulebookChapter("6. Ausrüstung", "Rules/Handbuch/Kapitel/kapitel6_equipment.md"),
        RulebookChapter("7. Kampf", "Rules/Handbuch/Kapitel/kapitel8_combat_conditions.md"),
        RulebookChapter("8. Zauber", "Rules/Handbuch/Kapitel/kapitel7_spells.md"),
        RulebookChapter("9. Spellbook", "Rules/Zauberbuch/Spellbook.md")
    )

    val pagerState = rememberPagerState(pageCount = { chapters.size })
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var isJumpingFromIndex by remember { mutableStateOf(false) }
    var pendingScrollItem by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    
    // Store content and blocks per chapter index
    val chapterContents = remember { mutableStateMapOf<Int, String>() }
    val chapterBlocks = remember { mutableStateMapOf<Int, List<String>>() }
    val scrollStates = chapters.map { androidx.compose.foundation.lazy.rememberLazyListState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Struct for global index
    data class IndexEntry(val chapterIndex: Int, val blockIndex: Int, val title: String, val isH3: Boolean, val chapterName: String)
    var globalIndex by remember { mutableStateOf<List<IndexEntry>>(emptyList()) }

    // Load everything upfront for global search/index
    LaunchedEffect(Unit) {
        val allIndex = mutableListOf<IndexEntry>()
        val regex = Regex("(?=^## |^### )", RegexOption.MULTILINE)
        
        withContext(Dispatchers.IO) {
            for (i in 1 until chapters.size) { // Skip Index chapter
                try {
                    val text = context.assets.open(chapters[i].filename).bufferedReader().use { it.readText() }
                    chapterContents[i] = text
                    val blocks = text.split(regex).filter { it.isNotBlank() }
                    chapterBlocks[i] = blocks
                    
                    blocks.forEachIndexed { blockIndex, blockText ->
                        val firstLine = blockText.trimStart().substringBefore('\n')
                        if (firstLine.startsWith("## ") || firstLine.startsWith("### ")) {
                            val isH3 = firstLine.startsWith("### ")
                            val title = firstLine.removePrefix("### ").removePrefix("## ").trim()
                            allIndex.add(IndexEntry(i, blockIndex, title, isH3, chapters[i].title))
                        }
                    }
                } catch (e: Exception) {
                    chapterContents[i] = "Fehler beim Laden von ${chapters[i].filename}"
                    chapterBlocks[i] = listOf("Fehler")
                }
            }
        }
        globalIndex = allIndex
    }
    
    LaunchedEffect(pagerState.currentPage) {
        if (!isJumpingFromIndex) {
            // Tab has changed manually (swipe or tab click), scroll to top
            try {
                scrollStates[pagerState.currentPage].scrollToItem(0)
            } catch (e: Exception) {
                // Ignore if list is not layouted yet
            }
        }
    }

    LaunchedEffect(targetChapter) {
        if (targetChapter != null) {
            val index = chapters.indexOfFirst { 
                it.title.contains(targetChapter, ignoreCase = true) || targetChapter.contains(it.title, ignoreCase = true) 
            }
            if (index != -1) {
                pagerState.scrollToPage(index)
            }
            if (targetSearch != null) {
                searchQuery = targetSearch
            }
            onTargetConsumed()
        }
    }

    PergamentBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WaldgruenDunkel)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = WaldGold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Regelwerk", fontSize = 24.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = WaldGold)
            }

            // Chapter Selection (Scrollable)
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = WaldgruenDunkel,
                contentColor = WaldGold,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = WaldGold
                    )
                }
            ) {
                chapters.forEachIndexed { index, chapter ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = { Text(chapter.title, fontSize = 16.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold) },
                        selectedContentColor = WaldGold,
                        unselectedContentColor = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.height(48.dp)
                    )
                }
            }

            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    placeholder = { 
                        Text(
                            text = if (pagerState.currentPage == 0) "Im gesamten Regelwerk suchen..." else "Im Kapitel suchen...",
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            color = TintenSchwarz.copy(alpha = 0.6f)
                        ) 
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(24.dp), tint = WaldgruenDunkel) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Suchen löschen", tint = WaldgruenDunkel)
                            }
                        }
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = TintenSchwarz),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WaldgruenDunkel,
                        unfocusedBorderColor = WaldgruenDunkel.copy(alpha = 0.5f)
                    )
                )
            }

            // Markdown Content with HorizontalPager
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val listState = scrollStates[page]

                    if (page == 0) {
                        // --- INDEX & SEARCH VIEW ---
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize().padding(16.dp)
                            ) {
                                if (searchQuery.isBlank()) {
                                    // Group by Chapter
                                    val grouped = globalIndex.groupBy { it.chapterName }
                                    grouped.forEach { (chapterName, entries) ->
                                        item {
                                            Text(chapterName, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = TintenSchwarz, fontSize = 22.sp, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                                        }
                                        items(entries.size) { idx ->
                                            val entry = entries[idx]
                                            Text(
                                                text = (if (entry.isH3) "  • " else "") + entry.title,
                                                color = WaldgruenDunkel,
                                                fontSize = if (entry.isH3) 16.sp else 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        coroutineScope.launch {
                                                            isJumpingFromIndex = true
                                                            searchQuery = ""
                                                            pendingScrollItem = entry.chapterIndex to entry.blockIndex
                                                            pagerState.scrollToPage(entry.chapterIndex)
                                                        }
                                                    }
                                                    .padding(vertical = 8.dp)
                                            )
                                        }
                                    }
                                } else {
                                    // Search Results
                                    val searchResults = globalIndex.filter { it.title.contains(searchQuery, ignoreCase = true) }
                                    
                                    // Also search full text blocks
                                    val contentResults = mutableListOf<IndexEntry>()
                                    chapterBlocks.forEach { (chapterIdx, blocks) ->
                                        blocks.forEachIndexed { blockIdx, blockText ->
                                            if (blockText.contains(searchQuery, ignoreCase = true)) {
                                                // Find the nearest heading above this block for title
                                                val title = globalIndex.lastOrNull { it.chapterIndex == chapterIdx && it.blockIndex <= blockIdx }?.title ?: "Absatz in ${chapters[chapterIdx].title}"
                                                contentResults.add(IndexEntry(chapterIdx, blockIdx, "$title (Texttreffer)", false, chapters[chapterIdx].title))
                                            }
                                        }
                                    }
                                    
                                    val allResults = (searchResults + contentResults).distinctBy { "${it.chapterIndex}-${it.blockIndex}" }

                                    if (allResults.isEmpty()) {
                                        item { Text("Keine Ergebnisse für '$searchQuery'", modifier = Modifier.padding(16.dp), color = TintenSchwarz) }
                                    } else {
                                        item { Text("${allResults.size} Ergebnisse gefunden:", fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TintenSchwarz, modifier = Modifier.padding(bottom = 8.dp)) }
                                        items(allResults.size) { idx ->
                                            val entry = allResults[idx]
                                            PergamentCard(
                                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable {
                                                    coroutineScope.launch {
                                                        isJumpingFromIndex = true
                                                        searchQuery = ""
                                                        pendingScrollItem = entry.chapterIndex to entry.blockIndex
                                                        pagerState.scrollToPage(entry.chapterIndex)
                                                    }
                                                }
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text(entry.chapterName, style = GrenzeGotischSmall, color = TintenSchwarz.copy(alpha = 0.7f))
                                                    Text(entry.title, fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = WaldgruenDunkel)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // --- NORMAL CHAPTER VIEW ---
                        val allBlocks = chapterBlocks[page] ?: listOf("Lade...")
                        val blocks = if (searchQuery.isNotBlank()) {
                            allBlocks.filter { it.contains(searchQuery, ignoreCase = true) }
                        } else {
                            allBlocks
                        }
                        
                        LaunchedEffect(pendingScrollItem) {
                            val pending = pendingScrollItem
                            if (pending != null && pending.first == page) {
                                kotlinx.coroutines.delay(50) // Tiny layout buffer
                                try {
                                    listState.scrollToItem(pending.second)
                                } catch (e: Exception) {}
                                pendingScrollItem = null
                                isJumpingFromIndex = false
                            }
                        }

                        if (blocks.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Keine Ergebnisse für '$searchQuery'", color = TintenSchwarz)
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize()) {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    items(blocks.size) { index ->
                                        val originalText = blocks[index]
                                        val highlightedText = if (searchQuery.isNotBlank() && originalText.contains(searchQuery, ignoreCase = true)) {
                                            // Highlight the search query in bold
                                            originalText.replace(Regex("(?i)(${Regex.escape(searchQuery)})"), "**$1**")
                                        } else {
                                            originalText
                                        }
                                        
                                        // Wir belassen Material3RichText, aber die Theme-Farben (TintenSchwarz) gelten aus dem umgebenden Theme.
                                        Material3RichText(modifier = Modifier.padding(bottom = 8.dp)) {
                                            Markdown(content = highlightedText)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Simple Scrollbar for LazyColumn
                    val isScrollable = listState.layoutInfo.totalItemsCount > 0
                    if (isScrollable) {
                        val firstVisible = listState.firstVisibleItemIndex
                        val totalItems = listState.layoutInfo.totalItemsCount
                        val scrollFraction = if (totalItems > 0) firstVisible.toFloat() / totalItems.toFloat() else 0f
                        
                        BoxWithConstraints(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .padding(vertical = 16.dp, horizontal = 4.dp)
                                .width(6.dp)
                                .background(TintenSchwarz.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                        ) {
                            val viewHeightPx = with(androidx.compose.ui.platform.LocalDensity.current) { this@BoxWithConstraints.maxHeight.toPx() }
                            val thumbHeightPx = viewHeightPx * 0.1f
                            val maxScrollOffsetPx = viewHeightPx - thumbHeightPx
                            val yOffsetPx = (scrollFraction * maxScrollOffsetPx).toInt()
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.1f) // 10% size thumb
                                    .offset { androidx.compose.ui.unit.IntOffset(0, yOffsetPx) }
                                    .background(WaldgruenDunkel, RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatDetailView(viewModel: CharacterViewModel, onBack: () -> Unit) {
    var newMessageText by remember { mutableStateOf("") }
    var isOoc by remember { mutableStateOf(false) }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(viewModel.groupChatMessages.size) {
        if (viewModel.groupChatMessages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.groupChatMessages.size - 1)
        }
    }

    PergamentBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TintenSchwarz)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = WaldGold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gruppen-Chat", fontSize = 24.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = WaldGold)
            }

            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(viewModel.groupChatMessages) { message ->
                    GroupChatMessageCard(message)
                }
            }

            // Message Input
            SteinCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("In-Character (IC)", color = TintenSchwarz, fontSize = 14.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = isOoc,
                            onCheckedChange = { isOoc = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = WaldgruenDunkel,
                                checkedTrackColor = WaldgruenDunkel.copy(alpha = 0.5f),
                                uncheckedThumbColor = OchsenblutRot,
                                uncheckedTrackColor = OchsenblutRot.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text("Out-Of-Character (OOC)", color = TintenSchwarz, fontSize = 14.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newMessageText,
                            onValueChange = { newMessageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(if (isOoc) "Schreibe etwas OOC..." else "Sprich als dein Charakter...", color = TintenSchwarz.copy(alpha = 0.6f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isOoc) WaldgruenDunkel else OchsenblutRot,
                                unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f),
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 16.sp),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newMessageText.isNotBlank()) {
                                    viewModel.sendGroupMessage(newMessageText, isOoc)
                                    newMessageText = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(if (isOoc) WaldgruenDunkel else OchsenblutRot, RoundedCornerShape(12.dp))
                        ) {
                            Text("➡️", color = WaldGold, fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupChatMessageCard(message: com.example.dndcompanion.ui.viewmodel.GroupChatMessage) {
    val dateStr = remember(message.timestamp) {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        sdf.format(java.util.Date(message.timestamp))
    }
    
    val nameColor = if (message.isOoc) WaldgruenDunkel else OchsenblutRot
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = if (message.isOoc) Color(0xFFEFEBE0).copy(alpha=0.8f) else Color.White.copy(alpha=0.9f)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = if (message.isOoc) "[OOC] ${message.author}" else message.author,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Almendra,
                    color = nameColor,
                    fontSize = 16.sp
                )
                Text(dateStr, style = GrenzeGotischSmall, color = TintenSchwarz.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.text, 
                fontSize = 16.sp, 
                color = TintenSchwarz,
                fontStyle = if (message.isOoc) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                lineHeight = 24.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestlogDetailView(viewModel: CharacterViewModel, onBack: () -> Unit) {
    var newQuestTitle by remember { mutableStateOf("") }
    var newQuestDesc by remember { mutableStateOf("") }
    var showCompleted by remember { mutableStateOf(false) }
    
    val currentQuests = viewModel.globalQuests.filter { it.isCompleted == showCompleted }
        .sortedByDescending { it.timestamp }

    PergamentBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OchsenblutRot)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = WaldGold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Questlog", fontSize = 24.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = WaldGold)
            }

            TabRow(
                selectedTabIndex = if (showCompleted) 1 else 0,
                containerColor = OchsenblutRot,
                contentColor = WaldGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (showCompleted) 1 else 0]),
                        color = WaldGold
                    )
                }
            ) {
                Tab(
                    selected = !showCompleted,
                    onClick = { showCompleted = false },
                    text = { Text("Aktive Quests", fontFamily = Almendra, fontWeight = FontWeight.Bold) },
                    selectedContentColor = WaldGold,
                    unselectedContentColor = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.height(48.dp)
                )
                Tab(
                    selected = showCompleted,
                    onClick = { showCompleted = true },
                    text = { Text("Abgeschlossen", fontFamily = Almendra, fontWeight = FontWeight.Bold) },
                    selectedContentColor = WaldGold,
                    unselectedContentColor = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.height(48.dp)
                )
            }

            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                // New Quest Input
                if (!showCompleted) {
                    SteinCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            OutlinedTextField(
                                value = newQuestTitle,
                                onValueChange = { newQuestTitle = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Quest-Titel...", color = TintenSchwarz.copy(alpha = 0.6f)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OchsenblutRot,
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
                                    focusedBorderColor = OchsenblutRot,
                                    unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f)
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 14.sp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (newQuestTitle.isNotBlank()) {
                                        viewModel.addQuest(newQuestTitle, newQuestDesc)
                                        newQuestTitle = ""
                                        newQuestDesc = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.End).height(48.dp)
                            ) {
                                Text("Quest hinzufügen", fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WaldGold)
                            }
                        }
                    }
                }

                // Quest List
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(currentQuests) { quest ->
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
                                        fontSize = 20.sp, 
                                        fontWeight = FontWeight.Bold, 
                                        color = if (quest.isCompleted) TintenSchwarz.copy(alpha = 0.5f) else OchsenblutRot,
                                        modifier = Modifier.weight(1f),
                                        textDecoration = if (quest.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else androidx.compose.ui.text.style.TextDecoration.None
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Checkbox(
                                        checked = quest.isCompleted,
                                        onCheckedChange = { viewModel.toggleQuestCompletion(quest) },
                                        colors = CheckboxDefaults.colors(checkedColor = WaldgruenDunkel, uncheckedColor = TintenSchwarz.copy(alpha = 0.5f)),
                                        modifier = Modifier.size(48.dp).padding(8.dp)
                                    )
                                    IconButton(
                                        onClick = { viewModel.deleteQuest(quest.id) },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Löschen", tint = if (quest.isCompleted) TintenSchwarz.copy(alpha = 0.5f) else Color.Red)
                                    }
                                }
                                if (quest.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = quest.description, 
                                        fontSize = 16.sp, 
                                        color = if (quest.isCompleted) TintenSchwarz.copy(alpha = 0.5f) else TintenSchwarz,
                                        lineHeight = 24.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
