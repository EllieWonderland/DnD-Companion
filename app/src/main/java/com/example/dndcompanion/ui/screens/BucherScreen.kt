package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

enum class BookType {
    GENERAL, GRUDGE, SPELLBOOK
}

@Composable
fun BucherScreen(viewModel: CharacterViewModel) {
    var activeBook by remember { mutableStateOf<BookType?>(null) }

    if (activeBook == null) {
        LibraryView(onBookSelected = { activeBook = it })
    } else {
        if (activeBook == BookType.SPELLBOOK) {
            SpellbookDetailView(
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
            horizontalArrangement = Arrangement.Center
        ) {
            BookCard(
                title = "Zauberbuch",
                subtitle = "Alle bekannten Zauber",
                color = BlauDunkel,
                onClick = { onBookSelected(BookType.SPELLBOOK) }
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
    val entries = if (bookType == BookType.GENERAL) viewModel.generalBookEntries else viewModel.grudgeBookEntries
    val title = if (bookType == BookType.GENERAL) "Notizbuch" else "Buch des Grolls"
    val tintColor = if (bookType == BookType.GENERAL) BlauDunkel else PinkDunkel

    var searchQuery by remember { mutableStateOf("") }
    var newEntryText by remember { mutableStateOf("") }
    var editingEntryId by remember { mutableStateOf<String?>(null) }
    var editText by remember { mutableStateOf("") }

    val filteredEntries = entries.filter { it.text.contains(searchQuery, ignoreCase = true) }

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

        Column(modifier = Modifier.padding(16.dp)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Einträge durchsuchen...") },
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
                            placeholder = { Text("Neuer Eintrag...", color = Color.LightGray) },
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
                                    viewModel.addGeneralBookEntry(newEntryText)
                                } else {
                                    viewModel.addGrudgeBookEntry(newEntryText)
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
                                        viewModel.updateGeneralBookEntry(editingEntryId!!, editText)
                                    } else {
                                        viewModel.updateGrudgeBookEntry(editingEntryId!!, editText)
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
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollRowState).padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val levels = listOf(-1) + (0..9).toList()
                levels.forEach { lvl ->
                    Button(
                        onClick = { selectedLevel = lvl },
                        colors = ButtonDefaults.buttonColors(containerColor = if (selectedLevel == lvl) PinkDunkel else BlauHell),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        val label = if (lvl == -1) "Alle" else if (lvl == 0) "Tricks" else "Grad $lvl"
                        Text(label, fontSize = 12.sp, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val filteredSpells = viewModel.globalSpellbook.filter { spell -> 
                (spell.classes.contains("Waldläufer") || spell.classes.contains("Hexenmeister")) &&
                (selectedLevel == -1 || spell.level == selectedLevel) &&
                (searchQuery.isBlank() || spell.name.contains(searchQuery, ignoreCase = true))
            }.sortedWith(compareBy({ it.level }, { it.name }))

            if (filteredSpells.isEmpty()) {
                Text("Keine Zauber gefunden.", modifier = Modifier.padding(16.dp), color = BlauDunkel)
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredSpells) { catalogSpell ->
                        val alreadyInBook = viewModel.allSpells.any { it.name == catalogSpell.name }
                        SpellCard(
                            spell = catalogSpell,
                            isEditMode = false,
                            isEquipped = alreadyInBook,
                            onTogglePrep = {},
                            onDelete = null,
                            extraContent = {
                                Button(
                                    onClick = {
                                        if (!alreadyInBook) {
                                            viewModel.addNewSpell(catalogSpell.copy(isPrepared = true, id = java.util.UUID.randomUUID().toString()))
                                        }
                                    },
                                    enabled = !alreadyInBook,
                                    colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel, disabledContainerColor = Color.Gray),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(36.dp)
                                ) {
                                    Text(if (alreadyInBook) "Bereits ausgerüstet" else "+ Ausrüsten", fontSize = 14.sp)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
