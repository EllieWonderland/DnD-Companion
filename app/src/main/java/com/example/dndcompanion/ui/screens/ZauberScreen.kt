package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.theme.BlauDunkel
import com.example.dndcompanion.ui.theme.BlauHell
import com.example.dndcompanion.ui.theme.PinkDunkel
import com.example.dndcompanion.ui.theme.PinkHell
import com.example.dndcompanion.ui.theme.GelbSand
import com.example.dndcompanion.ui.viewmodel.Spell
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ZauberScreen(viewModel: CharacterViewModel) {
    // Diese Variablen steuern, ob das Popup sichtbar ist und was eingetippt wurde
    var showShortRestDialog by remember { mutableStateOf(false) }
    var showLongRestDialog by remember { mutableStateOf(false) }
    var showSpellbookEditDialog by remember { mutableStateOf(false) }
    var hitDiceToSpend by remember { mutableIntStateOf(1) }
    var rolledDiceInput by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(GelbSand)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Zauber & Fähigkeiten", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
            Spacer(modifier = Modifier.height(8.dp)) // Etwas weniger Abstand hier

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Zauberwerte anzeigen
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = BlauHell)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Z-Angriff: +${viewModel.spellAttackBonus}", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Z-RW DC: ${viewModel.spellSaveDc}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                
                // Rasten-Buttons
                Column(
                    modifier = Modifier.width(IntrinsicSize.Max),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showShortRestDialog = true },
                        enabled = viewModel.hitDice > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = BlauHell),
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Kurze Rast", fontSize = 14.sp)
                    }
                    Button(
                        onClick = { 
                            viewModel.attemptLongRest() 
                            if (!viewModel.showRestWarningDialog) showLongRestDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel),
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Lange Rast", fontSize = 14.sp)
                    }
                }
            }

            val isHuntersMarkPrepared = viewModel.allSpells.any { it.name == "Zeichen des Jägers" && it.isPrepared }
            if (isHuntersMarkPrepared) {
                // Zeichen des Jägers (Kostenlos)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = BlauHell)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Zeichen des Jägers (Gratis)", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Ohne Zauberplatz", color = Color.White, fontSize = 14.sp)
                            }
                            Text("${viewModel.huntersMarkFreeUses} / 2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.useHuntersMarkFree() },
                            enabled = viewModel.huntersMarkFreeUses > 0,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PinkDunkel,
                                disabledContainerColor = Color.Gray
                            )
                        ) {
                            Text("Wirken")
                        }
                    }
                }
            }

            val isCureWoundsPrepared = viewModel.allSpells.any { it.name == "Wunden heilen" && it.isPrepared }
            if (isCureWoundsPrepared) {
                // Wunden heilen (Kostenlos)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = BlauHell)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Wunden heilen (Gratis)", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Ohne Zauberplatz (1x pro Lange Rast)", color = Color.White, fontSize = 14.sp)
                            }
                            Text("${if (viewModel.freeCureWoundsUsed) 0 else 1} / 1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.useFreeCureWounds() },
                            enabled = !viewModel.freeCureWoundsUsed,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PinkDunkel,
                                disabledContainerColor = Color.Gray
                            )
                        ) {
                            Text("Wirken")
                        }
                    }
                }
            }

            val isHealingWordPrepared = viewModel.allSpells.any { it.name == "Heilendes Wort" && it.isPrepared }
            if (isHealingWordPrepared) {
                // Heilendes Wort (Kostenlos)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = BlauHell)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Heilendes Wort (Gratis)", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Ohne Zauberplatz (1x pro Lange Rast)", color = Color.White, fontSize = 14.sp)
                            }
                            Text("${if (viewModel.freeHealingWordUsed) 0 else 1} / 1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.useFreeHealingWord() },
                            enabled = !viewModel.freeHealingWordUsed,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PinkDunkel,
                                disabledContainerColor = Color.Gray
                            )
                        ) {
                            Text("Wirken")
                        }
                    }
                }
            }

            val isFaerieFirePrepared = viewModel.allSpells.any { it.name == "Feenfeuer" && it.isPrepared }
            if (isFaerieFirePrepared) {
                // Feenfeuer (Kostenlos)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = BlauHell)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Feenfeuer (Gratis)", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Ohne Zauberplatz (1x pro Lange Rast)", color = Color.White, fontSize = 14.sp)
                            }
                            Text("${if (viewModel.freeFaerieFireUsed) 0 else 1} / 1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.useFreeFaerieFire() },
                            enabled = !viewModel.freeFaerieFireUsed,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PinkDunkel,
                                disabledContainerColor = Color.Gray
                            )
                        ) {
                            Text("Wirken")
                        }
                    }
                }
            }

            val isDarknessPrepared = viewModel.allSpells.any { it.name == "Dunkelheit" && it.isPrepared }
            if (isDarknessPrepared) {
                // Dunkelheit (Kostenlos)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = BlauHell)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Dunkelheit (Gratis)", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Ohne Zauberplatz (1x pro Lange Rast)", color = Color.White, fontSize = 14.sp)
                            }
                            Text("${if (viewModel.freeDarknessUsed) 0 else 1} / 1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.useFreeDarkness() },
                            enabled = !viewModel.freeDarknessUsed,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PinkDunkel,
                                disabledContainerColor = Color.Gray
                            )
                        ) {
                            Text("Wirken")
                        }
                    }
                }
            }

            // (Standard-Taktik moved to CombatScreen)

            HorizontalDivider(color = BlauDunkel, thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Zauberplätze Grad 1
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = BlauHell)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Zauberplätze Grad 1", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Heilung, Nebel, Beeren...", color = Color.White, fontSize = 14.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${viewModel.spellSlotsLevel1} / 3", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(end = 16.dp))
                        Button(
                            onClick = { viewModel.useSpellSlotLevel1() },
                            enabled = viewModel.spellSlotsLevel1 > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PinkDunkel,
                                disabledContainerColor = Color.Gray
                            )
                        ) {
                            Text("Wirken")
                        }
                    }
                }
            }

            if (viewModel.level >= 5) {
                // Zauberplätze Grad 2
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = BlauHell)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Zauberplätze Grad 2", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Pass Without Trace, Spike Growth...", color = Color.White, fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${viewModel.spellSlotsLevel2} / 2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(end = 16.dp))
                            Button(
                                onClick = { viewModel.useSpellSlotLevel2() },
                                enabled = viewModel.spellSlotsLevel2 > 0,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PinkDunkel,
                                    disabledContainerColor = Color.Gray
                                )
                            ) {
                                Text("Wirken")
                            }
                        }
                    }
                }
            }

            if (viewModel.level >= 9) {
                // Zauberplätze Grad 3
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = BlauHell)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Zauberplätze Grad 3", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Conjure Animals, Revivify...", color = Color.White, fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${viewModel.spellSlotsLevel3} / 2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(end = 16.dp))
                            Button(
                                onClick = { viewModel.useSpellSlotLevel3() },
                                enabled = viewModel.spellSlotsLevel3 > 0,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PinkDunkel,
                                    disabledContainerColor = Color.Gray
                                )
                            ) {
                                Text("Wirken")
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = BlauDunkel, thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // --- ZAUBERBUCH (PREPARED SPELLS) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mein Zauberbuch", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
                IconButton(onClick = { showSpellbookEditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Zauber bearbeiten", tint = PinkDunkel)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            val preparedSpells = viewModel.allSpells.filter { it.isPrepared }
            val cantrips = preparedSpells.filter { it.level == 0 }
            val leveledSpells = preparedSpells.filter { it.level > 0 }.sortedBy { it.level }

            if (cantrips.isNotEmpty()) {
                Text("Zaubertricks (0)", color = BlauDunkel, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                cantrips.forEach { spell ->
                    SpellCard(spell = spell)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (leveledSpells.isNotEmpty()) {
                Text("Vorbereitete Zauber", color = BlauDunkel, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                leveledSpells.forEach { spell ->
                    SpellCard(spell = spell)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (preparedSpells.isEmpty()) {
                Text("Keine Zauber vorbereitet. Klicke auf das Stift-Symbol, um Zauber auszuwählen.", color = Color.DarkGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            HorizontalDivider(color = BlauDunkel, thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Merkmale", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            // Editierbare benutzerdefinierte Merkmale (alle Merkmale)
            var editingTraitIndex by remember { mutableIntStateOf(-1) }
            var editTraitName by remember { mutableStateOf("") }
            var editTraitDesc by remember { mutableStateOf("") }

            viewModel.customTraits.forEachIndexed { index, trait ->
                if (editingTraitIndex == index) {
                    // Inline-Bearbeitungsmodus
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = BlauHell)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            OutlinedTextField(
                                value = editTraitName,
                                onValueChange = { editTraitName = it },
                                label = { Text("Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PinkDunkel,
                                    focusedLabelColor = PinkDunkel,
                                    unfocusedTextColor = Color.White,
                                    focusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = editTraitDesc,
                                onValueChange = { editTraitDesc = it },
                                label = { Text("Beschreibung") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PinkDunkel,
                                    focusedLabelColor = PinkDunkel,
                                    unfocusedTextColor = Color.White,
                                    focusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { editingTraitIndex = -1 }) {
                                    Text("Abbrechen", color = Color.White)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.updateCustomTrait(index, editTraitName, editTraitDesc)
                                        editingTraitIndex = -1
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel)
                                ) {
                                    Text("Speichern")
                                }
                            }
                        }
                    }
                } else {
                    // Normaler Anzeige-Modus mit Bearbeiten/Löschen
                    EditableTraitCard(
                        title = trait.name,
                        desc = trait.desc,
                        onEdit = {
                            editTraitName = trait.name
                            editTraitDesc = trait.desc
                            editingTraitIndex = index
                        },
                        onDelete = { viewModel.removeCustomTrait(index) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Das Popup-Fenster für die Kurze Rast
        if (showShortRestDialog) {
            AlertDialog(
                onDismissRequest = { showShortRestDialog = false },
                containerColor = GelbSand,
                title = { Text("Kurze Rast", color = BlauDunkel, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Wähle aus, wie viele Trefferwürfel (Hit Dice) du ausgeben möchtest, und trage die Summe deiner Würfelergebnisse (W10) ein. Dein Konstitutions-Modifikator (+${viewModel.conMod}) wird pro ausgegebenem Würfel automatisch addiert.", color = BlauDunkel)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Würfelauswahl
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Auszugebende Würfel:", color = BlauDunkel, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (hitDiceToSpend > 0) hitDiceToSpend-- }) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Remove, contentDescription = "Weniger", tint = BlauDunkel)
                                }
                                Text("$hitDiceToSpend / ${viewModel.hitDice}", color = BlauDunkel, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                IconButton(onClick = { if (hitDiceToSpend < viewModel.hitDice) hitDiceToSpend++ }) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Mehr", tint = BlauDunkel)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = rolledDiceInput,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() }) rolledDiceInput = newValue
                            },
                            label = { Text("Summe gewürfelte Augen") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PinkDunkel,
                                focusedLabelColor = PinkDunkel
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val rolledValue = rolledDiceInput.toIntOrNull() ?: 0
                            if (hitDiceToSpend >= 0) {
                                viewModel.takeShortRest(hitDiceToSpend, rolledValue)
                                showShortRestDialog = false
                                rolledDiceInput = "" // Feld für das nächste Mal leeren
                                hitDiceToSpend = 1
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel)
                    ) {
                        Text("Heilen")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showShortRestDialog = false }) {
                        Text("Abbrechen", color = BlauDunkel)
                    }
                }
            )
        }

        if (showLongRestDialog) {
            AlertDialog(
                onDismissRequest = { showLongRestDialog = false },
                containerColor = GelbSand,
                title = { Text("Lange Rast beendet", color = BlauDunkel, fontWeight = FontWeight.Bold) },
                text = { Text("Möchtest du einen vorbereiteten Zauber austauschen?", color = BlauDunkel) },
                confirmButton = {
                    Button(
                        onClick = {
                            showLongRestDialog = false
                            showSpellbookEditDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel)
                    ) {
                        Text("Ja")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLongRestDialog = false }) { Text("Nein", color = BlauDunkel) }
                }
            )
        }

        if (showSpellbookEditDialog) {
            SpellbookEditDialog(
                viewModel = viewModel,
                onDismiss = { showSpellbookEditDialog = false }
            )
        }

        if (viewModel.showRestWarningDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissRestWarningDialog() },
                title = { Text("Unzureichende Rationen", color = BlauDunkel, fontWeight = FontWeight.Bold) },
                text = { Text("Du hast nicht genug Wasserschläuche (0.5 benötigt) oder Tagesrationen (1 benötigt) für eine Lange Rast. Rasten ohne Ressourcen?", color = BlauDunkel) },
                confirmButton = {
                    Button(
                        onClick = { 
                            viewModel.forceLongRestWithoutResources() 
                            showLongRestDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel)
                    ) {
                        Text("Trotzdem Rasten")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissRestWarningDialog() }) {
                        Text("Abbrechen", color = BlauDunkel)
                    }
                },
                containerColor = GelbSand
            )
        }
    }
}

@Composable
fun SpellCard(
    spell: Spell,
    isEditMode: Boolean = false,
    isEquipped: Boolean = false,
    customColor: Color = BlauHell,
    onTogglePrep: () -> Unit = {},
    onDelete: (() -> Unit)? = null,
    extraContent: (@Composable () -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { if (!isEditMode) expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = customColor)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(spell.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    val type = if (spell.level == 0) "Zaubertrick" else "Stufe ${spell.level}"
                    Text(type, color = GelbSand, fontSize = 14.sp)
                }
                if (isEquipped) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                        contentDescription = "Ausrüstet",
                        tint = PinkDunkel,
                        modifier = Modifier.size(24.dp).padding(end = 8.dp)
                    )
                }
                if (isEditMode) {
                    Switch(
                        checked = spell.isPrepared,
                        onCheckedChange = { onTogglePrep() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PinkDunkel,
                            checkedTrackColor = PinkHell,
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = Color.DarkGray
                        )
                    )
                }
            }
            if (expanded || isEditMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Zeit: ${spell.castingTime} | Dauer: ${spell.duration} | Reichweite: ${spell.range}", color = Color.White, fontSize = 14.sp)

                // Komponenten String bauen
                val comps = mutableListOf<String>()
                if (spell.componentsV) comps.add("V")
                if (spell.componentsS) comps.add("S")
                if (spell.componentsM) {
                    val m = if (spell.materialCost.isNotBlank()) "M (${spell.materialCost})" else "M"
                    comps.add(m)
                }
                if (comps.isNotEmpty()) {
                    Text("Komponenten: ${comps.joinToString(", ")}", color = com.example.dndcompanion.ui.theme.PinkHell, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(spell.description, color = Color.White, fontSize = 14.sp)
                if (isEditMode && onDelete != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("Aus dem Buch löschen", fontSize = 14.sp)
                    }
                }
                if (extraContent != null) {
                    extraContent()
                }
            }
        }
    }
}

@Composable
fun TraitCard(title: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = BlauHell)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = GelbSand, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
fun EditableTraitCard(title: String, desc: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = BlauHell)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = GelbSand, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(desc, color = Color.White, fontSize = 14.sp)
                }
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onEdit) {
                        Text("✏️ Bearbeiten", color = GelbSand, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onDelete) {
                        Text("🗑️ Löschen", color = PinkDunkel, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

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
            color = GelbSand
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Zauberbuch verwalten", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
                    TextButton(onClick = onDismiss) {
                        Text("Schließen", color = PinkDunkel, fontWeight = FontWeight.Bold)
                    }
                }
                
                Text(
                    text = "Hier kannst du Zauber für den heutigen Kampf vorbereiten (Schalter an) oder nicht mehr benötigte Spells weglegen. Neu gelernte Zauber kannst du ganz unten hinzufügen.",
                    fontSize = 14.sp,
                    color = BlauDunkel,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                    val scrollState = rememberScrollState()
                    Column(modifier = Modifier.verticalScroll(scrollState).fillMaxWidth()) {
                        val cantrips = viewModel.allSpells.filter { it.level == 0 }
                        val leveled = viewModel.allSpells.filter { it.level > 0 }.sortedBy { it.level }

                        if (cantrips.isNotEmpty()) {
                            Text("Zaubertricks", color = BlauDunkel, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                            cantrips.forEach { spell ->
                                SpellCard(
                                    spell = spell,
                                    isEditMode = true,
                                    onTogglePrep = { viewModel.toggleSpellPrepared(spell.id) },
                                    onDelete = { viewModel.removeSpell(spell.id) }
                                )
                            }
                        }

                        if (leveled.isNotEmpty()) {
                            Text("Zauber", color = BlauDunkel, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                            leveled.forEach { spell ->
                                SpellCard(
                                    spell = spell,
                                    isEditMode = true,
                                    onTogglePrep = { viewModel.toggleSpellPrepared(spell.id) },
                                    onDelete = { viewModel.removeSpell(spell.id) }
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = { showAddSpellDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Text("+ Zauber aus Kompendium hinzufügen", color = Color.White)
                }
            }
        }
    }

    if (showAddSpellDialog) {
        SpellCatalogDialog(
            viewModel = viewModel,
            onDismiss = { showAddSpellDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellCatalogDialog(viewModel: CharacterViewModel, onDismiss: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableIntStateOf(-1) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = GelbSand) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Zauberkompendium", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
                    TextButton(onClick = onDismiss) { Text("Schließen", color = PinkDunkel, fontWeight = FontWeight.Bold) }
                }
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Zauber suchen") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PinkDunkel,
                        focusedLabelColor = PinkDunkel
                    )
                )
                
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
                
                Box(modifier = Modifier.weight(1f)) {
                    val filteredSpells = viewModel.globalSpellbook.filter { spell -> 
                        (selectedLevel == -1 || spell.level == selectedLevel) &&
                        (searchQuery.isBlank() || spell.name.contains(searchQuery, ignoreCase = true))
                    }.sortedWith(compareBy({ it.level }, { it.name }))
                    
                    if (filteredSpells.isEmpty()) {
                        Text("Keine Zauber gefunden.", modifier = Modifier.padding(16.dp), color = BlauDunkel)
                    } else {
                        val scrollState = rememberScrollState()
                        Column(modifier = Modifier.verticalScroll(scrollState).fillMaxWidth()) {
                            filteredSpells.forEach { catalogSpell ->
                                val alreadyInBook = viewModel.allSpells.any { it.name == catalogSpell.name }
                                SpellCard(
                                    spell = catalogSpell,
                                    isEditMode = true, // Wir schalten es ein, damit es immer ausgeklappt/bearbeitbar wirkt
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
                                            Text(if (alreadyInBook) "Bereits im Buch" else "+ Hinzufügen", fontSize = 14.sp)
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