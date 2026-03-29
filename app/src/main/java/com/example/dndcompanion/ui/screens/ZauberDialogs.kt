package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellbookEditDialog(viewModel: CharacterViewModel, onDismiss: () -> Unit) {
    var showAddSpellDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = PergamentHell
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Zauberbuch verwalten", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Waldgruen, fontFamily = Almendra)
                    TextButton(onClick = onDismiss) {
                        Text("Schließen", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold, fontFamily = Almendra)
                    }
                }

                Text(
                    text = "Hier kannst du Zauber für den heutigen Kampf vorbereiten (Schalter an) oder nicht mehr benötigte Spells weglegen. Neu gelernte Zauber kannst du ganz unten hinzufügen.",
                    fontSize = 14.sp,
                    color = TintenSchwarz,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Box(modifier = Modifier.weight(1f).padding(bottom = 8.dp)) {
                    val scrollState = rememberScrollState()
                    val globalSpellbook by viewModel.globalSpellbook.collectAsState()

                    Column(modifier = Modifier.verticalScroll(scrollState).fillMaxWidth()) {
                        val cantrips = viewModel.allSpells.filter { it.level == 0 }
                        val leveled = viewModel.allSpells.filter { it.level > 0 }.sortedBy { it.level }

                        if (cantrips.isNotEmpty()) {
                            Text("Zaubertricks", color = Waldgruen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                            cantrips.forEach { spell ->
                                SpellCard(
                                    spell = spell,
                                    isEditMode = true,
                                    onTogglePrep = { viewModel.toggleSpellPrepared(spell.id) },
                                    onDelete = { viewModel.removeSpell(spell.id) },
                                    globalSpellbook = globalSpellbook
                                )
                            }
                        }

                        if (leveled.isNotEmpty()) {
                            Text("Zauber", color = Waldgruen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                            leveled.forEach { spell ->
                                SpellCard(
                                    spell = spell,
                                    isEditMode = true,
                                    onTogglePrep = { viewModel.toggleSpellPrepared(spell.id) },
                                    onDelete = { viewModel.removeSpell(spell.id) },
                                    globalSpellbook = globalSpellbook
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = PergamentHell,
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = { showAddSpellDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Waldgruen, contentColor = PergamentHell),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        Text("+ Zauber aus Kompendium hinzufügen", color = PergamentHell, fontSize = 16.sp, fontFamily = Almendra)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))

                if (showAddSpellDialog) {
                    SpellCatalogDialog(
                        viewModel = viewModel,
                        onDismiss = { showAddSpellDialog = false }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellCatalogDialog(viewModel: CharacterViewModel, onDismiss: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableIntStateOf(-1) }
    val globalSpellbook by viewModel.globalSpellbook.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = PergamentHell) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Zauberkompendium", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Waldgruen, fontFamily = Almendra)
                    TextButton(onClick = onDismiss) { Text("Schließen", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold, fontFamily = Almendra) }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Zauber suchen") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        focusedLabelColor = MaterialTheme.colorScheme.tertiary
                    )
                )

                val scrollRowState = rememberScrollState()
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(scrollRowState).padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Stufe:", color = TintenSchwarz, fontWeight = FontWeight.Bold)
                    val levels = listOf(-1) + (0..9).toList()
                    levels.forEach { lvl ->
                        val hasSpells = lvl == -1 || globalSpellbook.any { spell ->
                            spell.level == lvl && (searchQuery.isBlank() || spell.name.contains(searchQuery, ignoreCase = true))
                        }
                        Button(
                            onClick = { selectedLevel = lvl },
                            enabled = hasSpells,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedLevel == lvl) MaterialTheme.colorScheme.tertiary else PergamentDunkel,
                                contentColor = if (selectedLevel == lvl) PergamentHell else if (hasSpells) TintenSchwarz else Color.DarkGray,
                                disabledContainerColor = PergamentDunkel
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            val label = if (lvl == -1) "Alle" else if (lvl == 0) "Tricks" else "Grad $lvl"
                            Text(text = label, fontSize = 16.sp, fontFamily = Almendra)
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    val filteredSpells = globalSpellbook.filter { spell ->
                        (selectedLevel == -1 || spell.level == selectedLevel) &&
                        (searchQuery.isBlank() || spell.name.contains(searchQuery, ignoreCase = true))
                    }.sortedWith(compareBy({ it.level }, { it.name }))

                    if (filteredSpells.isEmpty()) {
                        Text("Keine Zauber gefunden.", modifier = Modifier.padding(16.dp), color = TintenBraun)
                    } else {
                        val scrollState = rememberScrollState()
                        Column(modifier = Modifier.verticalScroll(scrollState).fillMaxWidth()) {
                            filteredSpells.forEach { catalogSpellEntity ->
                                val catalogSpell = catalogSpellEntity.toSpell()
                                val alreadyInBook = viewModel.allSpells.any { it.name == catalogSpell.name }
                                SpellCard(
                                    spell = catalogSpell,
                                    isEditMode = true,
                                    onTogglePrep = {},
                                    onDelete = null,
                                    globalSpellbook = globalSpellbook,
                                    extraContent = {
                                        Button(
                                            onClick = {
                                                if (!alreadyInBook) {
                                                    viewModel.addNewSpell(catalogSpell.copy(isPrepared = true, id = java.util.UUID.randomUUID().toString()))
                                                }
                                            },
                                            enabled = !alreadyInBook,
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = PergamentHell, disabledContainerColor = EisenGrau),
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(48.dp)
                                        ) {
                                            Text(if (alreadyInBook) "Bereits im Buch" else "+ Hinzufügen", fontSize = 16.sp, fontFamily = Almendra)
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
}
