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
import com.example.dndcompanion.ui.theme.PergamentDunkel
import com.example.dndcompanion.ui.theme.Waldgruen
import com.example.dndcompanion.ui.theme.HexenLila
import com.example.dndcompanion.R
import com.example.dndcompanion.data.database.RuleEntity
import com.example.dndcompanion.data.database.WeaponEntity
import com.example.dndcompanion.data.database.ArmorEntity
import com.example.dndcompanion.data.database.ToolEntity
import com.example.dndcompanion.data.database.SpeciesEntity
import com.example.dndcompanion.data.database.ClassEntity
import com.example.dndcompanion.data.database.FeatureEntity

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
                viewModel = viewModel,
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
                                containerColor = if (selectedLevel == lvl) OchsenblutRot else PergamentDunkel,
                                contentColor = if (selectedLevel == lvl) Color.White else if (hasSpells) TintenSchwarz else Color.DarkGray,
                                disabledContainerColor = TintenSchwarz.copy(alpha = 0.1f)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            val label = if (lvl == -1) "Alle" else if (lvl == 0) "Tricks" else "Grad $lvl"
                            Text(label, fontSize = 16.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold)
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
                                containerColor = if (selectedClassFilter == filterClass) OchsenblutRot else PergamentDunkel,
                                contentColor = if (selectedClassFilter == filterClass) Color.White else if (hasSpells) TintenSchwarz else Color.DarkGray,
                                disabledContainerColor = TintenSchwarz.copy(alpha = 0.1f)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(filterClass, fontSize = 16.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold)
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
                                containerColor = if (selectedSchoolFilter == filterSchool) OchsenblutRot else PergamentDunkel,
                                contentColor = if (selectedSchoolFilter == filterSchool) Color.White else if (hasSpells) TintenSchwarz else Color.DarkGray,
                                disabledContainerColor = TintenSchwarz.copy(alpha = 0.1f)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(filterSchool, fontSize = 16.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold)
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
                                customColor = if (catalogSpell.classes.contains("Waldläufer")) Waldgruen else if (catalogSpell.classes.contains("Hexenmeister")) HexenLila else WaldgruenDunkel,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulebookDetailView(targetChapter: String?, targetSearch: String? = null, viewModel: CharacterViewModel, onTargetConsumed: () -> Unit, onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    
    // Oberkategorien für die Tabs (Manuell definiert, da wir aus verschiedenen Tabellen mischen)
    val tabs = listOf(
        "Global", "Gameplay", "Klassen & Völker", "Ausrüstung", "Kampf & Zustände", "Zauber-Regeln", "Dienstleistungen"
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size })

    // Observe DB Data
    val rules by viewModel.searchedRules.collectAsState()
    val weapons by viewModel.searchedWeapons.collectAsState()
    val armor by viewModel.searchedArmor.collectAsState()
    val tools by viewModel.searchedTools.collectAsState()
    val species by viewModel.searchedSpecies.collectAsState()
    val classes by viewModel.searchedClasses.collectAsState()
    val features by viewModel.searchedFeatures.collectAsState()

    // Filter rules by main category
    val gameplayRules = rules.filter { it.category == "Gameplay" }
    val combatRules = rules.filter { it.category == "Kampf & Zustände" }
    val spellRules = rules.filter { it.category == "Zauber" }
    val serviceRules = rules.filter { it.category == "Ausrüstung & Dienstleistungen" }

    LaunchedEffect(searchQuery) {
        viewModel.searchRulebook(searchQuery)
    }

    LaunchedEffect(targetChapter) {
        if (targetChapter != null) {
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
                tabs.forEachIndexed { index, tabTitle ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = { Text(tabTitle, fontSize = 16.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold) },
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
                        Text("Im Regelwerk suchen...", color = TintenSchwarz.copy(alpha = 0.6f)) 
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(24.dp), tint = WaldgruenDunkel) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { 
                                searchQuery = "" 
                                viewModel.searchRulebook("")
                            }, modifier = Modifier.size(48.dp)) {
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

            // Content Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) { page ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (page) {
                        0 -> { // Global (Combined Search Results)
                            if (searchQuery.isBlank()) {
                                item { 
                                    Text(
                                        text = "Nutze das Suchfeld, um im gesamten Regelwerk, in Klassen, Völkern und der Ausrüstung gleichzeitig zu suchen.",
                                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        color = TintenSchwarz.copy(alpha = 0.6f),
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    ) 
                                }
                            } else {
                                var foundAnything = false
                                if (rules.isNotEmpty()) {
                                    item { SectionHeader("Regeln") }
                                    items(rules) { rule -> RuleCard(rule) }
                                    foundAnything = true
                                }
                                if (species.isNotEmpty()) {
                                    item { SectionHeader("Völker") }
                                    items(species) { spec -> SpeciesCard(spec) }
                                    foundAnything = true
                                }
                                if (classes.isNotEmpty()) {
                                    item { SectionHeader("Klassen") }
                                    items(classes) { cls -> ClassCard(cls) }
                                    foundAnything = true
                                }
                                if (features.isNotEmpty()) {
                                    item { SectionHeader("Merkmale & Talente") }
                                    items(features) { feature -> FeatureCard(feature) }
                                    foundAnything = true
                                }
                                if (weapons.isNotEmpty() || armor.isNotEmpty() || tools.isNotEmpty()) {
                                    item { SectionHeader("Ausrüstung") }
                                    items(weapons) { weapon -> WeaponCard(weapon) }
                                    items(armor) { arm -> ArmorCard(arm) }
                                    items(tools) { tool -> ToolCard(tool) }
                                    foundAnything = true
                                }
                                if (!foundAnything) item { EmptySearchResult() }
                            }
                        }
                        1 -> { // Gameplay
                            items(gameplayRules) { rule -> RuleCard(rule) }
                            if (gameplayRules.isEmpty()) item { EmptySearchResult() }
                        }
                        2 -> { // Klassen & Völker
                            if (species.isNotEmpty()) {
                                item { SectionHeader("Völker (Species)") }
                                items(species) { spec -> SpeciesCard(spec) }
                            }
                            if (classes.isNotEmpty()) {
                                item { SectionHeader("Klassen (Classes)") }
                                items(classes) { cls -> ClassCard(cls) }
                            }
                            if (species.isEmpty() && classes.isEmpty()) item { EmptySearchResult() }
                        }
                        3 -> { // Ausrüstung
                            if (weapons.isNotEmpty()) {
                                item { SectionHeader("Waffen") }
                                items(weapons) { weapon -> WeaponCard(weapon) }
                            }
                            if (armor.isNotEmpty()) {
                                item { SectionHeader("Rüstungen & Schilde") }
                                items(armor) { arm -> ArmorCard(arm) }
                            }
                            if (tools.isNotEmpty()) {
                                item { SectionHeader("Werkzeuge") }
                                items(tools) { tool -> ToolCard(tool) }
                            }
                            if (weapons.isEmpty() && armor.isEmpty() && tools.isEmpty()) item { EmptySearchResult() }
                        }
                        4 -> { // Kampf & Zustände
                            items(combatRules) { rule -> RuleCard(rule) }
                            if (combatRules.isEmpty()) item { EmptySearchResult() }
                        }
                        5 -> { // Zauber-Regeln
                            items(spellRules) { rule -> RuleCard(rule) }
                            if (spellRules.isEmpty()) item { EmptySearchResult() }
                        }
                        6 -> { // Dienstleistungen
                            items(serviceRules) { rule -> RuleCard(rule) }
                            if (serviceRules.isEmpty()) item { EmptySearchResult() }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptySearchResult() {
    Text(
        text = "Keine passenden Einträge gefunden.",
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        color = TintenSchwarz.copy(alpha = 0.6f),
        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
    )
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = Almendra,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = WaldgruenDunkel,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun RuleCard(rule: RuleEntity) {
    SteinCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(rule.title, fontSize = 20.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = OchsenblutRot)
            Spacer(modifier = Modifier.height(8.dp))
            Material3RichText(modifier = Modifier.fillMaxWidth()) {
                Markdown(rule.content)
            }
            if (rule.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    rule.tags.take(3).forEach { tag ->
                        Text("#$tag", fontSize = 12.sp, style = GrenzeGotischSmall, color = WaldgruenDunkel)
                    }
                }
            }
        }
    }
}

@Composable
fun WeaponCard(weapon: WeaponEntity) {
    PergamentCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(weapon.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TintenSchwarz)
                Text(weapon.price, fontWeight = FontWeight.Bold, color = WaldGold, modifier = Modifier.background(WaldgruenDunkel, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
            }
            Text(weapon.category, fontSize = 12.sp, color = TintenSchwarz.copy(alpha = 0.7f), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Schaden: ${weapon.damage}", color = OchsenblutRot, fontWeight = FontWeight.Medium)
                Text("Gewicht: ${weapon.weightLb} lb", color = TintenSchwarz.copy(alpha = 0.8f))
            }
            Text("Meisterschaft: ${weapon.mastery}", color = TintenSchwarz.copy(alpha = 0.9f))
            if (weapon.properties.isNotEmpty()) {
                Text("Eigenschaften: ${weapon.properties.joinToString(", ")}", fontSize = 12.sp, color = TintenSchwarz.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun ArmorCard(armor: ArmorEntity) {
    PergamentCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(armor.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TintenSchwarz)
                Text(armor.price, fontWeight = FontWeight.Bold, color = WaldGold, modifier = Modifier.background(WaldgruenDunkel, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
            }
            Text(armor.category, fontSize = 12.sp, color = TintenSchwarz.copy(alpha = 0.7f), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            Spacer(modifier = Modifier.height(8.dp))
            
            val dexModText = if (armor.addDexModifier) {
                if (armor.maxDexModifier != null) " + GES (max ${armor.maxDexModifier})" else " + GES"
            } else ""
            Text("RK: ${armor.baseAC}$dexModText", color = OchsenblutRot, fontWeight = FontWeight.Medium)
            
            val stealthText = if (armor.stealthDisadvantage) "Nachteil auf Heimlichkeit" else "Normale Heimlichkeit"
            val strengthText = if (armor.strengthRequirement > 0) "STR min. ${armor.strengthRequirement}" else "Keine STR-Anforderung"
            
            Text("$stealthText | $strengthText | ${armor.weightLb} lb", fontSize = 12.sp, color = TintenSchwarz.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun ToolCard(tool: ToolEntity) {
    PergamentCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tool.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TintenSchwarz)
                Text(tool.category, fontSize = 12.sp, color = TintenSchwarz.copy(alpha = 0.7f), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                if (tool.weightLb != null) {
                    Text("Gewicht: ${tool.weightLb} lb", fontSize = 12.sp, color = TintenSchwarz.copy(alpha = 0.8f))
                }
            }
            Text(tool.price, fontWeight = FontWeight.Bold, color = WaldGold, modifier = Modifier.background(WaldgruenDunkel, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
        }
    }
}

@Composable
fun SpeciesCard(species: SpeciesEntity) {
    SteinCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(species.name, fontSize = 20.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = OchsenblutRot)
            Text("Größe: ${species.size} | Tempo: ${species.speed}m", fontSize = 14.sp, color = TintenSchwarz.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(8.dp))
            species.traits.forEach { trait ->
                Text(trait.name, fontWeight = FontWeight.Bold, color = TintenSchwarz, modifier = Modifier.padding(top = 4.dp))
                Text(trait.description, fontSize = 14.sp, color = TintenSchwarz.copy(alpha = 0.9f))
            }
        }
    }
}

@Composable
fun ClassCard(cls: ClassEntity) {
    SteinCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(cls.name, fontSize = 22.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = HexenLila)
            Text("Primär: ${cls.primaryAbility} | Trefferwürfel: ${cls.hitDie}", fontSize = 14.sp, color = TintenSchwarz.copy(alpha = 0.8f))
            Text("Rettungswürfe: ${cls.savingThrows.joinToString(", ")}", fontSize = 14.sp, color = TintenSchwarz.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(12.dp))
            
            Text("Klassenmerkmale", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TintenSchwarz)
            cls.classFeatures.forEach { feature ->
                Text("Lv ${feature.level}: ${feature.name}", fontWeight = FontWeight.Bold, color = TintenSchwarz, modifier = Modifier.padding(top = 8.dp))
                Text(feature.description, fontSize = 14.sp, color = TintenSchwarz.copy(alpha = 0.9f))
            }
            
            if (cls.subclasses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Unterklassen", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TintenSchwarz)
                cls.subclasses.forEach { sub ->
                    Text(sub.name, fontWeight = FontWeight.Bold, color = OchsenblutRot, modifier = Modifier.padding(top = 8.dp))
                    sub.features.forEach { sf ->
                        Text("Lv ${sf.level}: ${sf.name}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TintenSchwarz)
                        Text(sf.description, fontSize = 12.sp, color = TintenSchwarz.copy(alpha = 0.8f))
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
                        Text("In-Character (IC)", color = TintenSchwarz, fontSize = 16.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold)
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
                        Text("Out-Of-Character (OOC)", color = TintenSchwarz, fontSize = 16.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold)
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
                    text = { Text("Aktive Quests", fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    selectedContentColor = WaldGold,
                    unselectedContentColor = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.height(48.dp)
                )
                Tab(
                    selected = showCompleted,
                    onClick = { showCompleted = true },
                    text = { Text("Abgeschlossen", fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
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

@Composable
fun FeatureCard(feature: FeatureEntity) {
    SteinCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            val titleColor = when (feature.type) {
                "FEAT" -> WaldgruenDunkel
                "RACIAL_TRAIT" -> OchsenblutRot
                "CLASS_FEATURE" -> HexenLila
                "SUBCLASS_FEATURE" -> TintenSchwarz
                else -> WaldgruenDunkel
            }
            Text(feature.name, fontSize = 20.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = titleColor)
            val subText = buildString {
                append(feature.type)
                if (!feature.category.isNullOrBlank()) append(" - ${feature.category}")
                if (feature.levelReq > 1) append(" (Ab Stufe ${feature.levelReq})")
            }.toString()
            Text(subText, fontSize = 14.sp, fontFamily = Almendra, color = TintenSchwarz.copy(alpha = 0.8f))
            
            if (!feature.raceReq.isNullOrEmpty() || !feature.classReq.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                val reqText = buildString {
                    if (!feature.raceReq.isNullOrEmpty()) append("Volk: ${feature.raceReq.joinToString()} ")
                    if (!feature.classReq.isNullOrEmpty()) append("Klasse: ${feature.classReq.joinToString()}")
                }.toString()
                Text("Voraussetzung: $reqText", fontSize = 14.sp, style = GrenzeGotischSmall, color = OchsenblutRot)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Material3RichText(modifier = Modifier.fillMaxWidth()) {
                Markdown(feature.description)
            }
        }
    }
}
