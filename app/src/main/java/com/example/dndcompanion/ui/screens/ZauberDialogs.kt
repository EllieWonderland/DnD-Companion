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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.viewmodel.Spell
import com.example.dndcompanion.ui.viewmodel.SpellViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellbookEditDialog(spellVm: SpellViewModel, charVm: CharacterViewModel, onDismiss: () -> Unit) {
    var showAddSpellDialog by remember { mutableStateOf(false) }
    var showHomebrewDialog by remember { mutableStateOf(false) }

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

                // --- ZAUBERPLATZ-MAXIMA BEARBEITEN ---
                val charClass = charVm.characterData.charClass
                val showLevel1 = spellVm.maxSpellSlotsLevel1 > 0 || charVm.characterData.baseSpellSlotsLevel1 > 0
                val showLevel2 = spellVm.maxSpellSlotsLevel2 > 0 || charVm.characterData.baseSpellSlotsLevel2 > 0
                val showLevel3 = spellVm.maxSpellSlotsLevel3 > 0 || charVm.characterData.baseSpellSlotsLevel3 > 0

                if (showLevel1 || showLevel2 || showLevel3) {
                    Text("Zauberschlitze (Maximum)", fontSize = 13.sp, color = Waldgruen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 2, 3).forEach { lvl ->
                            val currentMax = when (lvl) {
                                1 -> spellVm.maxSpellSlotsLevel1
                                2 -> spellVm.maxSpellSlotsLevel2
                                else -> spellVm.maxSpellSlotsLevel3
                            }
                            val show = when (lvl) {
                                1 -> showLevel1
                                2 -> showLevel2
                                else -> showLevel3
                            }
                            if (show) {
                                Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = PergamentDunkel)) {
                                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Grad $lvl", fontSize = 12.sp, color = TintenBraun, fontWeight = FontWeight.Bold)
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                            IconButton(onClick = { spellVm.setMaxSlots(lvl, currentMax - 1) }, modifier = Modifier.size(28.dp)) {
                                                Icon(Icons.Default.Remove, contentDescription = "-", tint = TintenSchwarz, modifier = Modifier.size(16.dp))
                                            }
                                            Text("$currentMax", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.widthIn(min = 24.dp), textAlign = TextAlign.Center)
                                            IconButton(onClick = { spellVm.setMaxSlots(lvl, currentMax + 1) }, modifier = Modifier.size(28.dp)) {
                                                Icon(Icons.Default.Add, contentDescription = "+", tint = TintenSchwarz, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = Bronze.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 8.dp))
                }

                Box(modifier = Modifier.weight(1f).padding(bottom = 8.dp)) {
                    val scrollState = rememberScrollState()
                    val globalSpellbook by spellVm.globalSpellbook.collectAsState()

                    Column(modifier = Modifier.verticalScroll(scrollState).fillMaxWidth()) {
                        val cantrips = spellVm.allSpells.filter { it.level == 0 }
                        val leveled = spellVm.allSpells.filter { it.level > 0 }.sortedBy { it.level }

                        if (cantrips.isNotEmpty()) {
                            Text("Zaubertricks", color = Waldgruen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                            cantrips.forEach { spell ->
                                SpellCard(
                                    spell = spell,
                                    isEditMode = true,
                                    onTogglePrep = { spellVm.toggleSpellPrepared(spell.id) },
                                    onDelete = { spellVm.removeSpell(spell.id) },
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
                                    onTogglePrep = { spellVm.toggleSpellPrepared(spell.id) },
                                    onDelete = { spellVm.removeSpell(spell.id) },
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
                    Column(modifier = Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showAddSpellDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Waldgruen, contentColor = PergamentHell),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("+ Aus Kompendium hinzufügen", color = PergamentHell, fontSize = 15.sp, fontFamily = Almendra)
                        }
                        OutlinedButton(
                            onClick = { showHomebrewDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("+ Eigenen Zauber erstellen", fontSize = 15.sp, fontFamily = Almendra)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))

                if (showAddSpellDialog) {
                    SpellCatalogDialog(
                        spellVm = spellVm,
                        charVm = charVm,
                        onDismiss = { showAddSpellDialog = false }
                    )
                }
                if (showHomebrewDialog) {
                    HomebrewSpellDialog(
                        onDismiss = { showHomebrewDialog = false },
                        onAdd = { spell ->
                            spellVm.addNewSpell(spell)
                            showHomebrewDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomebrewSpellDialog(onDismiss: () -> Unit, onAdd: (Spell) -> Unit) {
    var name by remember { mutableStateOf("") }
    var level by remember { mutableIntStateOf(1) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PergamentHell,
        title = {
            Text("Eigenen Zauber erstellen", color = Waldgruen, fontFamily = Almendra, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        focusedLabelColor = MaterialTheme.colorScheme.tertiary
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Stufe:", color = TintenSchwarz, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { level = (level - 1).coerceAtLeast(0) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = "-", tint = TintenSchwarz)
                    }
                    Text(
                        text = if (level == 0) "Trick" else "$level",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = 36.dp),
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = { level = (level + 1).coerceAtMost(9) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "+", tint = TintenSchwarz)
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Beschreibung") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        focusedLabelColor = MaterialTheme.colorScheme.tertiary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isNotBlank()) {
                        onAdd(
                            Spell(
                                name = name.trim(),
                                level = level,
                                castingTime = "1 Aktion",
                                range = "Selbst",
                                duration = "Sofort",
                                description = description.trim(),
                                isPrepared = true
                            )
                        )
                    }
                },
                enabled = name.trim().isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = PergamentHell)
            ) {
                Text("Hinzufügen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = Waldgruen, fontFamily = Almendra)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellCatalogDialog(spellVm: SpellViewModel, charVm: CharacterViewModel, onDismiss: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableIntStateOf(-1) }
    val globalSpellbook by spellVm.globalSpellbook.collectAsState()

    val localizedClassName = when (charVm.characterData.charClass) {
        com.example.dndcompanion.data.CharacterClass.RANGER -> "Waldläufer"
        com.example.dndcompanion.data.CharacterClass.WARLOCK -> "Hexenmeister"
        else -> "Eigene Klasse"
    }
    var classFilterEnabled by remember { mutableStateOf(true) }

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

                FilterChip(
                    selected = classFilterEnabled,
                    onClick = { classFilterEnabled = !classFilterEnabled },
                    label = { Text("Nur Klassenzauber ($localizedClassName)", color = TintenSchwarz) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                        selectedLabelColor = TintenSchwarz
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
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
                            val classMatch = !classFilterEnabled || spell.classes.contains(localizedClassName)
                            spell.level == lvl && classMatch && (searchQuery.isBlank() || spell.name.contains(searchQuery, ignoreCase = true))
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
                        val classMatch = !classFilterEnabled || spell.classes.contains(localizedClassName)
                        (selectedLevel == -1 || spell.level == selectedLevel) &&
                        classMatch &&
                        (searchQuery.isBlank() || spell.name.contains(searchQuery, ignoreCase = true))
                    }.sortedWith(compareBy({ it.level }, { it.name }))

                    if (filteredSpells.isEmpty()) {
                        Text("Keine Zauber gefunden.", modifier = Modifier.padding(16.dp), color = TintenBraun)
                    } else {
                        val scrollState = rememberScrollState()
                        Column(modifier = Modifier.verticalScroll(scrollState).fillMaxWidth()) {
                            filteredSpells.forEach { catalogSpellEntity ->
                                val catalogSpell = catalogSpellEntity.toSpell()
                                val alreadyInBook = spellVm.allSpells.any { it.name == catalogSpell.name }
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
                                                    spellVm.addNewSpell(catalogSpell.copy(isPrepared = true, id = java.util.UUID.randomUUID().toString()))
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
