package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ZauberScreen(viewModel: CharacterViewModel) {
    // Diese Variablen steuern, ob das Popup sichtbar ist und was eingetippt wurde
    var showShortRestDialog by remember { mutableStateOf(false) }
    var showSpellbookEditDialog by remember { mutableStateOf(false) }
    var rolledDiceInput by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(GelbSand)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Zauber & Fähigkeiten", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
            Spacer(modifier = Modifier.height(8.dp)) // Etwas weniger Abstand hier

            // NEU: Zauberwerte anzeigen
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = BlauHell)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Zauber-Angriffsbonus: +${viewModel.spellAttackBonus}", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Zauber-Rettungswurf DC: ${viewModel.spellSaveDc}", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Zeichen des Jägers (Kostenlos)
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = BlauHell)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Zeichen des Jägers (Gratis)", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Ohne Zauberplatz", color = Color.White, fontSize = 12.sp)
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

            // Rasten-Buttons nach oben gezogen
            Text("Regeneration (Rasten)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = { showShortRestDialog = true },
                    enabled = viewModel.hitDice > 0 && viewModel.currentHp < viewModel.maxHp,
                    colors = ButtonDefaults.buttonColors(containerColor = BlauHell)
                ) {
                    Text("Kurze Rast")
                }
                Button(
                    onClick = { viewModel.takeLongRest() },
                    colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel)
                ) {
                    Text("Lange Rast")
                }
            }

            HorizontalDivider(color = BlauDunkel, thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Zauberplätze Grad 1
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = BlauHell)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Zauberplätze Grad 1", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Heilung, Nebel, Beeren...", color = Color.White, fontSize = 12.sp)
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
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Zauberplätze Grad 2", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Pass Without Trace, Spike Growth...", color = Color.White, fontSize = 12.sp)
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
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Zauberplätze Grad 3", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Conjure Animals, Revivify...", color = Color.White, fontSize = 12.sp)
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
                Text("Zauberbuch", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
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

            // Volks- und Klassenmerkmale nach unten gezogen
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
                        Text("Wirf einen W10 und trage das Ergebnis ein. Dein Konstitutions-Modifikator (+${viewModel.conMod}) wird automatisch addiert.", color = BlauDunkel)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = rolledDiceInput,
                            onValueChange = { newValue ->
                                // Wir lassen nur Zahlen zu
                                if (newValue.all { it.isDigit() }) {
                                    rolledDiceInput = newValue
                                }
                            },
                            label = { Text("Gewürfelte Zahl (W10)") },
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
                            val rolledValue = rolledDiceInput.toIntOrNull()
                            if (rolledValue != null && rolledValue > 0) {
                                viewModel.takeShortRest(rolledValue)
                                showShortRestDialog = false
                                rolledDiceInput = "" // Feld für das nächste Mal leeren
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

        if (showSpellbookEditDialog) {
            SpellbookEditDialog(
                viewModel = viewModel,
                onDismiss = { showSpellbookEditDialog = false }
            )
        }
    }
}

@Composable
fun SpellCard(
    spell: Spell,
    isEditMode: Boolean = false,
    onTogglePrep: () -> Unit = {},
    onDelete: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { if (!isEditMode) expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = BlauHell)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(spell.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    val type = if (spell.level == 0) "Zaubertrick" else "Stufe ${spell.level}"
                    Text(type, color = GelbSand, fontSize = 12.sp)
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
                Text("Zeit: ${spell.castingTime} | Dauer: ${spell.duration} | Reichweite: ${spell.range}", color = Color.White, fontSize = 12.sp)

                // Komponenten String bauen
                val comps = mutableListOf<String>()
                if (spell.componentsV) comps.add("V")
                if (spell.componentsS) comps.add("S")
                if (spell.componentsM) {
                    val m = if (spell.materialCost.isNotBlank()) "M (${spell.materialCost})" else "M"
                    comps.add(m)
                }
                if (comps.isNotEmpty()) {
                    Text("Komponenten: ${comps.joinToString(", ")}", color = com.example.dndcompanion.ui.theme.PinkHell, fontSize = 12.sp)
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
                        Text("Aus dem Buch löschen", fontSize = 12.sp)
                    }
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
                        Text("✏️ Bearbeiten", color = GelbSand, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onDelete) {
                        Text("🗑️ Löschen", color = PinkDunkel, fontSize = 12.sp)
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
                    fontSize = 12.sp,
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
                    Text("+ Neuen Zauber eintragen", color = Color.White)
                }
            }
        }
    }

    if (showAddSpellDialog) {
        AddSpellDialog(
            onDismiss = { showAddSpellDialog = false },
            onSave = { newSpell ->
                viewModel.addNewSpell(newSpell)
                showAddSpellDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSpellDialog(onDismiss: () -> Unit, onSave: (Spell) -> Unit) {
    var name by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("1") }
    var castingTime by remember { mutableStateOf("1 Aktion") }
    var range by remember { mutableStateOf("9 m") }
    var duration by remember { mutableStateOf("Sofort") }
    var desc by remember { mutableStateOf("") }
    
    var hasV by remember { mutableStateOf(false) }
    var hasS by remember { mutableStateOf(false) }
    var hasM by remember { mutableStateOf(false) }
    var matCost by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GelbSand,
        title = { Text("Neuer Zauber", color = BlauDunkel, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = level, onValueChange = { level = it }, label = { Text("Level (0 = Trick)") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = castingTime, onValueChange = { castingTime = it }, label = { Text("Zeit") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = range, onValueChange = { range = it }, label = { Text("Reichweite") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Dauer") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                
                Text("Komponenten:", color = BlauDunkel, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hasV, onCheckedChange = { hasV = it })
                    Text("V", modifier = Modifier.padding(end = 8.dp))
                    Checkbox(checked = hasS, onCheckedChange = { hasS = it })
                    Text("S", modifier = Modifier.padding(end = 8.dp))
                    Checkbox(checked = hasM, onCheckedChange = { hasM = it })
                    Text("M")
                }
                if (hasM) {
                    OutlinedTextField(value = matCost, onValueChange = { matCost = it }, label = { Text("Material/Kosten") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }

                OutlinedTextField(
                    value = desc, onValueChange = { desc = it },
                    label = { Text("Beschreibung (Effekt)") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val lvlInt = level.toIntOrNull() ?: 1
                        onSave(
                            Spell(
                                name = name,
                                level = lvlInt,
                                castingTime = castingTime,
                                range = range,
                                duration = duration,
                                componentsV = hasV,
                                componentsS = hasS,
                                componentsM = hasM,
                                materialCost = matCost,
                                description = desc,
                                isPrepared = true // Direkt beim Erstellen aktivieren
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel)
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen", color = BlauDunkel) }
        }
    )
}