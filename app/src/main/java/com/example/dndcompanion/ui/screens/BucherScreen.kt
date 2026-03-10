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
import com.example.dndcompanion.ui.theme.BlauDunkel
import com.example.dndcompanion.ui.theme.BlauHell
import com.example.dndcompanion.ui.theme.PinkDunkel
import com.example.dndcompanion.ui.theme.GelbSand
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.viewmodel.BookEntry
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.draw.scale

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GelbSand)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bibliothek", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BookCard(
                title = "Notizbuch",
                subtitle = "Allgemeines & Infos",
                color = BlauHell,
                onClick = { onBookSelected(BookType.GENERAL) }
            )
            BookCard(
                title = "Buch des Grolls",
                subtitle = "Vergeltung wartet",
                color = PinkDunkel,
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
                color = BlauDunkel,
                onClick = { onBookSelected(BookType.SPELLBOOK) }
            )
            BookCard(
                title = "Regelwerk",
                subtitle = "Handbuch & D&D Regeln",
                color = Color(0xFF2E7D32), // Dark green for Rulebook
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
                color = Color(0xFF7B1FA2), // Purple for Chat
                onClick = { onBookSelected(BookType.GROUP_CHAT) }
            )
            BookCard(
                title = "Questlog",
                subtitle = "Aktive & fertige Aufträge",
                color = Color(0xFFD84315), // Deep Orange for Quests
                onClick = { onBookSelected(BookType.QUESTLOG) }
            )
        }
    }
}

@Composable
fun BookCard(title: String, subtitle: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(200.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📖", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtitle, fontSize = 12.sp, color = GelbSand, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailView(bookType: BookType, viewModel: CharacterViewModel, onBack: () -> Unit) {
    val privateEntries = if (bookType == BookType.GENERAL) viewModel.generalBookEntries else viewModel.grudgeBookEntries
    val publicEntries = if (bookType == BookType.GENERAL) viewModel.publicGeneralBookEntries else viewModel.publicGrudgeBookEntries
    
    val title = if (bookType == BookType.GENERAL) "Notizbuch" else "Buch des Grolls"
    val tintColor = if (bookType == BookType.GENERAL) BlauDunkel else PinkDunkel

    var showPublicTab by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var newEntryText by remember { mutableStateOf("") }
    var editingEntryId by remember { mutableStateOf<String?>(null) }
    var editText by remember { mutableStateOf("") }

    val currentEntries = if (showPublicTab) publicEntries else privateEntries
    val filteredEntries = currentEntries.filter { it.text.contains(searchQuery, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GelbSand)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(tintColor)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        TabRow(
            selectedTabIndex = if (showPublicTab) 1 else 0,
            containerColor = BlauHell,
            contentColor = Color.White
        ) {
            Tab(
                selected = !showPublicTab,
                onClick = { showPublicTab = false },
                text = { Text("Persönlich") },
                selectedContentColor = GelbSand,
                unselectedContentColor = Color.White
            )
            Tab(
                selected = showPublicTab,
                onClick = { showPublicTab = true },
                text = { Text("Gruppe") },
                selectedContentColor = GelbSand,
                unselectedContentColor = Color.White
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(if (showPublicTab) "Gruppen-Einträge durchsuchen..." else "Eigene Einträge durchsuchen...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Suchen löschen")
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = tintColor,
                    focusedLeadingIconColor = tintColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // New Entry / Edit Entry
            if (editingEntryId == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BlauHell)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        OutlinedTextField(
                            value = newEntryText,
                            onValueChange = { newEntryText = it },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            placeholder = { Text(if (showPublicTab) "Neue globale Notiz..." else "Neuer persönlicher Eintrag...", color = Color.LightGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = tintColor,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (bookType == BookType.GENERAL) {
                                    viewModel.addGeneralBookEntry(newEntryText, showPublicTab)
                                } else {
                                    viewModel.addGrudgeBookEntry(newEntryText, showPublicTab)
                                }
                                newEntryText = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = tintColor),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Speichern")
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PinkDunkel)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Eintrag bearbeiten", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editText,
                            onValueChange = { editText = it },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { editingEntryId = null }) {
                                Text("Abbrechen", color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (bookType == BookType.GENERAL) {
                                        viewModel.updateGeneralBookEntry(editingEntryId!!, editText, showPublicTab)
                                    } else {
                                        viewModel.updateGrudgeBookEntry(editingEntryId!!, editText, showPublicTab)
                                    }
                                    editingEntryId = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel)
                            ) {
                                Text("Aktualisieren")
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

@Composable
fun BookEntryCard(entry: BookEntry, bookType: BookType, onEdit: () -> Unit) {
    val dateStr = remember(entry.timestamp) {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(entry.timestamp))
    }
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(dateStr, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Bearbeiten", tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(entry.text, fontSize = 14.sp, color = BlauDunkel)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GelbSand)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BlauDunkel)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Zauberbuch", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Zauber suchen...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Suchen löschen")
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkDunkel,
                    focusedLeadingIconColor = PinkDunkel
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            val scrollRowState = rememberScrollState()
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(scrollRowState).padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Stufe:", color = BlauDunkel, fontWeight = FontWeight.Bold)
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
                            containerColor = if (selectedLevel == lvl) PinkDunkel else BlauHell,
                            disabledContainerColor = Color.LightGray
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        val label = if (lvl == -1) "Alle" else if (lvl == 0) "Tricks" else "Grad $lvl"
                        Text(label, fontSize = 12.sp, color = if (hasSpells) Color.White else Color.DarkGray)
                    }
                }
            }

            val classScrollState = rememberScrollState()
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(classScrollState).padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Klasse:", color = BlauDunkel, fontWeight = FontWeight.Bold)
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
                            containerColor = if (selectedClassFilter == filterClass) PinkDunkel else BlauHell,
                            disabledContainerColor = Color.LightGray
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(filterClass, fontSize = 12.sp, color = if (hasSpells) Color.White else Color.DarkGray)
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
                Text("Schule:", color = BlauDunkel, fontWeight = FontWeight.Bold)
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
                            containerColor = if (selectedSchoolFilter == filterSchool) PinkDunkel else BlauHell,
                            disabledContainerColor = Color.LightGray
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(filterSchool, fontSize = 12.sp, color = if (hasSpells) Color.White else Color.DarkGray)
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
                Text("Keine Zauber gefunden.", modifier = Modifier.padding(16.dp), color = BlauDunkel)
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
                            customColor = BlauDunkel, // Unified background
                            extraContent = {
                                val canEquip = !alreadyInBook && (!isDruidLevel1 || druidLevel1Count < 1)
                                Button(
                                    onClick = {
                                        if (canEquip) {
                                            viewModel.addNewSpell(catalogSpell.copy(isPrepared = true, id = java.util.UUID.randomUUID().toString()))
                                        }
                                    },
                                    enabled = canEquip || alreadyInBook, // Keep enabled if already in book to show "Bereits ausgerüstet"
                                    colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel, disabledContainerColor = Color.Gray),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(36.dp)
                                ) {
                                    val buttonText = if (alreadyInBook) "Bereits ausgerüstet" else if (!canEquip) "Max 1 Druidenzauber" else "+ Ausrüsten"
                                    Text(buttonText, fontSize = 14.sp)
                                }
                            }
                        )
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GelbSand)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2E7D32)) // Dark green
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Regelwerk", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Chapter Selection (Scrollable)
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = BlauHell,
            contentColor = Color.White,
            edgePadding = 8.dp
        ) {
            chapters.forEachIndexed { index, chapter ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = { Text(chapter.title, fontSize = 14.sp) },
                    selectedContentColor = GelbSand,
                    unselectedContentColor = Color.White
                )
            }
        }

        // Search Bar
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { 
                    Text(
                        text = if (pagerState.currentPage == 0) "Im gesamten Regelwerk suchen..." else "Im Kapitel suchen...",
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    ) 
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "Suchen löschen")
                        }
                    }
                },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2E7D32),
                    unfocusedBorderColor = BlauDunkel,
                    focusedLeadingIconColor = Color(0xFF2E7D32)
                )
            )
        }

        // Markdown Content with HorizontalPager
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .background(GelbSand, RoundedCornerShape(8.dp))
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
                                        Text(chapterName, fontWeight = FontWeight.Bold, color = BlauDunkel, fontSize = 20.sp, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                                    }
                                    items(entries.size) { idx ->
                                        val entry = entries[idx]
                                        Text(
                                            text = (if (entry.isH3) "  • " else "") + entry.title,
                                            color = PinkDunkel,
                                            fontSize = if (entry.isH3) 14.sp else 16.sp,
                                            fontWeight = if (entry.isH3) FontWeight.Normal else FontWeight.Bold,
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
                                                .padding(vertical = 4.dp)
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
                                    item { Text("Keine Ergebnisse für '$searchQuery'", modifier = Modifier.padding(16.dp)) }
                                } else {
                                    item { Text("${allResults.size} Ergebnisse gefunden:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp)) }
                                    items(allResults.size) { idx ->
                                        val entry = allResults[idx]
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable {
                                                coroutineScope.launch {
                                                    isJumpingFromIndex = true
                                                    searchQuery = ""
                                                    pendingScrollItem = entry.chapterIndex to entry.blockIndex
                                                    pagerState.scrollToPage(entry.chapterIndex)
                                                }
                                            },
                                            colors = CardDefaults.cardColors(containerColor = BlauHell)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(entry.chapterName, fontSize = 12.sp, color = BlauDunkel)
                                                Text(entry.title, fontWeight = FontWeight.Bold, color = PinkDunkel)
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
                            Text("Keine Ergebnisse für '$searchQuery'", color = PinkDunkel)
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
                            .width(4.dp)
                            .background(Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                    ) {
                        val viewHeightPx = with(androidx.compose.ui.platform.LocalDensity.current) { maxHeight.toPx() }
                        val thumbHeightPx = viewHeightPx * 0.1f
                        val maxScrollOffsetPx = viewHeightPx - thumbHeightPx
                        val yOffsetPx = (scrollFraction * maxScrollOffsetPx).toInt()
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.1f) // 10% size thumb
                                .offset { androidx.compose.ui.unit.IntOffset(0, yOffsetPx) }
                                .background(BlauDunkel, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }
    }
}
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GelbSand)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF7B1FA2))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gruppen-Chat", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = BlauHell),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("In-Character (IC)", color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = isOoc,
                        onCheckedChange = { isOoc = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PinkDunkel,
                            checkedTrackColor = PinkDunkel.copy(alpha = 0.5f),
                            uncheckedThumbColor = BlauDunkel,
                            uncheckedTrackColor = BlauDunkel.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.scale(0.8f).padding(horizontal = 8.dp)
                    )
                    Text("Out-Of-Character (OOC)", color = Color.White, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newMessageText,
                        onValueChange = { newMessageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(if (isOoc) "Schreibe etwas OOC..." else "Sprich als dein Charakter...", color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isOoc) Color.Gray else PinkDunkel,
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White
                        ),
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
                            .size(50.dp)
                            .background(if (isOoc) Color.Gray else PinkDunkel, RoundedCornerShape(25.dp))
                    ) {
                        Text("?", color = Color.White, fontSize = 20.sp)
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
    
    val nameColor = if (message.isOoc) Color.Gray else if (message.charClass == com.example.dndcompanion.data.CharacterClass.RANGER) BlauDunkel else PinkDunkel
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = if (message.isOoc) Color(0xFFF5F5F5) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = if (message.isOoc) "[OOC] ${message.author}" else message.author,
                    fontWeight = FontWeight.Bold,
                    color = nameColor,
                    fontSize = 14.sp
                )
                Text(dateStr, fontSize = 10.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.text, 
                fontSize = 15.sp, 
                color = BlauDunkel,
                fontStyle = if (message.isOoc) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GelbSand)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFD84315))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Questlog", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        TabRow(
            selectedTabIndex = if (showCompleted) 1 else 0,
            containerColor = BlauHell,
            contentColor = Color.White
        ) {
            Tab(
                selected = !showCompleted,
                onClick = { showCompleted = false },
                text = { Text("Aktive Quests") },
                selectedContentColor = GelbSand,
                unselectedContentColor = Color.White
            )
            Tab(
                selected = showCompleted,
                onClick = { showCompleted = true },
                text = { Text("Abgeschlossen") },
                selectedContentColor = GelbSand,
                unselectedContentColor = Color.White
            )
        }

        Column(modifier = Modifier.padding(16.dp).weight(1f)) {
            // New Quest Input
            if (!showCompleted) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = BlauHell)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        OutlinedTextField(
                            value = newQuestTitle,
                            onValueChange = { newQuestTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Quest-Titel...", color = Color.LightGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD84315),
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newQuestDesc,
                            onValueChange = { newQuestDesc = it },
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            placeholder = { Text("Beschreibung (optional)...", color = Color.LightGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFD84315),
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White
                            )
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD84315)),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Quest hinzufügen")
                        }
                    }
                }
            }

            // Quest List
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(currentQuests) { quest ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = if (quest.isCompleted) Color(0xFFE0E0E0) else Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = quest.title, 
                                    fontSize = 18.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    color = if (quest.isCompleted) Color.Gray else BlauDunkel,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Checkbox(
                                    checked = quest.isCompleted,
                                    onCheckedChange = { viewModel.toggleQuestCompletion(quest) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD84315))
                                )
                                IconButton(
                                    onClick = { viewModel.deleteQuest(quest.id) },
                                    modifier = Modifier.size(24.dp).padding(start = 4.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Löschen", tint = Color.Red)
                                }
                            }
                            if (quest.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = quest.description, 
                                    fontSize = 14.sp, 
                                    color = if (quest.isCompleted) Color.Gray else Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}