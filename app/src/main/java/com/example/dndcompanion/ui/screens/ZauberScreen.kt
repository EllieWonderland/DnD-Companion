package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.dndcompanion.ui.theme.*
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

    val isRanger = viewModel.characterData.charClass == com.example.dndcompanion.data.CharacterClass.RANGER
    val accentColor = if (isRanger) WaldGold else HexenLila
    PergamentBackground {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Zauber & Fähigkeiten", style = MaterialTheme.typography.titleLarge, color = Waldgruen)
            Spacer(modifier = Modifier.height(8.dp)) // Etwas weniger Abstand hier

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Zauberwerte anzeigen
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = PergamentDunkel)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Z-Angriff: +${viewModel.spellAttackBonus}", color = TintenSchwarz, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Z-RW DC: ${viewModel.spellSaveDc}", color = TintenSchwarz, fontWeight = FontWeight.Bold)
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
                        colors = ButtonDefaults.buttonColors(containerColor = Bronze),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Kurze Rast", fontSize = 16.sp)
                    }
                    Button(
                        onClick = { 
                            viewModel.attemptLongRest() 
                            if (!viewModel.showRestWarningDialog) showLongRestDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Waldgruen),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Lange Rast", fontSize = 16.sp)
                    }
                }
            }

            // --- KOSTENLOSE ZAUBER (TALENTE) ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = WaldgruenDunkel)
            ) {
                Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                    Text("Kostenlose Zauber (Talente & Gegenstände)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    val freeFeatures = viewModel.customTraits.filter { trait ->
                        val spellId = trait.grantedSpellId
                        if (spellId == null) return@filter false
                        // Nur anzeigen, wenn der Zauber im Buch steht UND vorbereitet ist
                        viewModel.allSpells.any { it.name.equals(spellId, ignoreCase = true) && it.isPrepared }
                    }
                    freeFeatures.forEach { trait ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(trait.name, color = Color.White, fontSize = 14.sp)
                                val subText = if (trait.maxUses >= 999) "Beliebig oft" else "${trait.desc.split("\n").firstOrNull() ?: ""}"
                                Text(subText, color = Color.LightGray, fontSize = 11.sp, maxLines = 1)
                            }
                            if (trait.maxUses < 999) {
                                Text("${trait.currentUses} / ${trait.maxUses}", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                            }
                            Button(
                                onClick = { viewModel.useTraitSpell(trait) },
                                enabled = trait.currentUses > 0,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = if (accentColor == WaldGold) TintenSchwarz else Color.White
                                )
                            ) { Text("Wirken", fontSize = 16.sp, fontFamily = Almendra) }
                        }
                    }

                    if (freeFeatures.isEmpty()) {
                        Text("Keine aktiven kostenlosen Zauber verfügbar.", color = Color.LightGray, fontSize = 12.sp, style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                    }
                }
            }

            // (Standard-Taktik moved to CombatScreen)

            HorizontalDivider(color = Bronze, thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.characterData.charClass == com.example.dndcompanion.data.CharacterClass.RANGER) {
                // --- RANGER ZAUBERPLÄTZE ---
                // Zauberplätze Grad 1
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = WaldgruenDunkel)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Zauberplätze Grad 1", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Vielseitige Waldläufer-Magie", color = Color.White, fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${viewModel.spellSlotsLevel1} / 3", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(end = 16.dp))
                            Button(
                                onClick = { viewModel.useSpellSlotLevel1() },
                                enabled = viewModel.spellSlotsLevel1 > 0,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = if (accentColor == WaldGold) TintenSchwarz else Color.White,
                                    disabledContainerColor = EisenGrau
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
                        colors = CardDefaults.cardColors(containerColor = WaldgruenDunkel)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Zauberplätze Grad 2", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Fortgeschrittene Naturkräfte", color = Color.White, fontSize = 14.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${viewModel.spellSlotsLevel2} / 2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(end = 16.dp))
                                Button(
                                    onClick = { viewModel.useSpellSlotLevel2() },
                                    enabled = viewModel.spellSlotsLevel2 > 0,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentColor,
                                        contentColor = if (accentColor == WaldGold) TintenSchwarz else Color.White,
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
                        colors = CardDefaults.cardColors(containerColor = WaldgruenDunkel)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Zauberplätze Grad 3", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Mächtige Waldläufer-Künste", color = Color.White, fontSize = 14.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${viewModel.spellSlotsLevel3} / 2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(end = 16.dp))
                                Button(
                                    onClick = { viewModel.useSpellSlotLevel3() },
                                    enabled = viewModel.spellSlotsLevel3 > 0,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentColor,
                                        contentColor = if (accentColor == WaldGold) TintenSchwarz else Color.White,
                                        disabledContainerColor = Color.Gray
                                    )
                                ) {
                                    Text("Wirken")
                                }
                            }
                        }
                    }
                }
            } else if (viewModel.characterData.charClass == com.example.dndcompanion.data.CharacterClass.WARLOCK) {
                // --- WARLOCK PAKTMAGIE ---
                val maxSlotsLevel2 = viewModel.characterData.baseSpellSlotsLevel2
                val currentSlotsLevel2 = viewModel.spellSlotsLevel2

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = WaldgruenDunkel)
                ) {
                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Paktmagie (Level 2)", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Slots erholen sich bei kurzer Rast", color = Color.White, fontSize = 14.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("$currentSlotsLevel2 / $maxSlotsLevel2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(end = 16.dp))
                                Button(
                                    onClick = { viewModel.useSpellSlotLevel2() },
                                    enabled = currentSlotsLevel2 > 0,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = accentColor,
                                        contentColor = if (accentColor == WaldGold) TintenSchwarz else Color.White,
                                        disabledContainerColor = Color.Gray
                                    )
                                ) {
                                    Text("Wirken")
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(
                                onClick = { viewModel.resetWarlockSlots() },
                                colors = ButtonDefaults.buttonColors(containerColor = Waldgruen)
                            ) {
                                Text("Kurze Rast Reg.", fontSize = 12.sp)
                            }
                            
                            Button(
                                onClick = { viewModel.applyMagicalCunning() },
                                colors = ButtonDefaults.buttonColors(containerColor = HexenLila)
                            ) {
                                Text("Magische Rafinesse", color = PergamentHell, fontSize = 12.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { viewModel.applyFalseLife() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text("Unholde Vitalität (12 Temp HP)", fontSize = 12.sp)
                        }

                        // --- BESONDERE ZAUBER (TALENTE & ANRUFUNGEN) ---
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Besondere Zauber (Talente)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Segnen
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Segnen", color = Color.White, fontSize = 14.sp)
                                Text("Talent: Eingeweihter", color = Color.LightGray, fontSize = 11.sp)
                            }
                            Text("${if (viewModel.freeBlessUsed) 0 else 1} / 1", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                            Button(
                                onClick = { viewModel.useFreeBless() },
                                enabled = !viewModel.freeBlessUsed,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = if (accentColor == WaldGold) TintenSchwarz else Color.White
                                )
                            ) { Text("Wirken", fontSize = 10.sp) }
                        }

                        // Nebelschritt
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Nebelschritt", color = Color.White, fontSize = 14.sp)
                                Text("Talent: Feenberührt", color = Color.LightGray, fontSize = 11.sp)
                            }
                            Text("${if (viewModel.freeMistyStepUsed) 0 else 1} / 1", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                            Button(
                                onClick = { viewModel.useFreeMistyStep() },
                                enabled = !viewModel.freeMistyStepUsed,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = if (accentColor == WaldGold) TintenSchwarz else Color.White
                                )
                            ) { Text("Wirken", fontSize = 10.sp) }
                        }

                        // Magierrüstung
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Magierrüstung", color = Color.White, fontSize = 14.sp)
                                Text("Talent: Eingeweihter", color = Color.LightGray, fontSize = 11.sp)
                            }
                            Text("${if (viewModel.freeMageArmorUsed) 0 else 1} / 1", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                            Button(
                                onClick = { viewModel.useFreeMageArmor() },
                                enabled = !viewModel.freeMageArmorUsed,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = if (accentColor == WaldGold) TintenSchwarz else Color.White
                                )
                            ) { Text("Wirken", fontSize = 10.sp) }
                        }
                    }
                }
            }

            HorizontalDivider(color = Bronze, thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // --- ZAUBERBUCH (PREPARED SPELLS) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mein Zauberbuch", style = MaterialTheme.typography.titleMedium, color = Waldgruen)
                IconButton(onClick = { showSpellbookEditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Zauber bearbeiten", tint = accentColor)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            val preparedSpells = viewModel.allSpells.filter { it.isPrepared }
            val cantrips = preparedSpells.filter { it.level == 0 }
            val leveledSpells = preparedSpells.filter { it.level > 0 }.sortedBy { it.level }

            if (cantrips.isNotEmpty()) {
                Text("Zaubertricks (0)", color = Waldgruen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                cantrips.forEach { spell ->
                    SpellCard(
                        spell = spell,
                        onCastAsRitual = if (viewModel.canCastAsRitual(spell)) { { /* Visual feedback only for now */ } } else null
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (leveledSpells.isNotEmpty()) {
                Text("Vorbereitete Zauber", color = Waldgruen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                leveledSpells.forEach { spell ->
                    SpellCard(
                        spell = spell,
                        onCastAsRitual = if (viewModel.canCastAsRitual(spell)) { { /* Visual feedback only for now */ } } else null
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (preparedSpells.isEmpty()) {
                Text("Keine Zauber vorbereitet. Klicke auf das Stift-Symbol, um Zauber auszuwählen.", color = TintenBraun, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            HorizontalDivider(color = Bronze, thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Merkmale", style = MaterialTheme.typography.titleMedium, color = Waldgruen)
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
                        colors = CardDefaults.cardColors(containerColor = PergamentDunkel)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            OutlinedTextField(
                                value = editTraitName,
                                onValueChange = { editTraitName = it },
                                label = { Text("Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OchsenblutRot,
                                    focusedLabelColor = OchsenblutRot,
                                    unfocusedTextColor = TintenSchwarz,
                                    focusedTextColor = TintenSchwarz
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = editTraitDesc,
                                onValueChange = { editTraitDesc = it },
                                label = { Text("Beschreibung") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OchsenblutRot,
                                    focusedLabelColor = OchsenblutRot,
                                    unfocusedTextColor = TintenSchwarz,
                                    focusedTextColor = TintenSchwarz
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { editingTraitIndex = -1 }) {
                                    Text("Abbrechen", color = TintenBraun)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.updateCustomTrait(index, editTraitName, editTraitDesc)
                                        editingTraitIndex = -1
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot)
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
                containerColor = PergamentHell,
                title = { Text("Kurze Rast", color = Waldgruen, fontFamily = Almendra, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Wähle aus, wie viele Trefferwürfel (Hit Dice) du ausgeben möchtest, und trage die Summe deiner Würfelergebnisse (W10) ein. Dein Konstitutions-Modifikator (+${viewModel.conMod}) wird pro ausgegebenem Würfel automatisch addiert.", color = TintenSchwarz)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Würfelauswahl
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Auszugebende Würfel:", color = TintenSchwarz, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (hitDiceToSpend > 0) hitDiceToSpend-- }) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Remove, contentDescription = "Weniger", tint = TintenSchwarz)
                                }
                                Text("$hitDiceToSpend / ${viewModel.hitDice}", color = TintenSchwarz, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                IconButton(onClick = { if (hitDiceToSpend < viewModel.hitDice) hitDiceToSpend++ }) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Mehr", tint = TintenSchwarz)
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
                                focusedBorderColor = OchsenblutRot,
                                focusedLabelColor = OchsenblutRot
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
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("Heilen")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showShortRestDialog = false }) {
                        Text("Abbrechen", color = Waldgruen, fontFamily = Almendra)
                    }
                }
            )
        }

        if (showLongRestDialog) {
            AlertDialog(
                onDismissRequest = { showLongRestDialog = false },
                containerColor = PergamentHell,
                title = { Text("Lange Rast beendet", color = Waldgruen, fontFamily = Almendra, fontWeight = FontWeight.Bold) },
                text = { Text("Möchtest du einen vorbereiteten Zauber austauschen?", color = TintenSchwarz) },
                confirmButton = {
                    Button(
                        onClick = {
                            showLongRestDialog = false
                            showSpellbookEditDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("Ja")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLongRestDialog = false }) { Text("Nein", color = Waldgruen, fontFamily = Almendra) }
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
                title = { Text("Unzureichende Rationen", color = OchsenblutRot, fontFamily = Almendra, fontWeight = FontWeight.Bold) },
                text = { Text("Du hast nicht genug Wasserschläuche (0.5 benötigt) oder Tagesrationen (1 benötigt) für eine Lange Rast. Rasten ohne Ressourcen?", color = TintenSchwarz) },
                confirmButton = {
                    Button(
                        onClick = { 
                            viewModel.forceLongRestWithoutResources() 
                            showLongRestDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                    ) {
                        Text("Trotzdem Rasten", fontFamily = Almendra)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissRestWarningDialog() }) {
                        Text("Abbrechen", color = Waldgruen, fontFamily = Almendra)
                    }
                },
                containerColor = PergamentHell
            )
        }
    }
    }
}

@Composable
fun SpellCard(
    spell: Spell,
    isEditMode: Boolean = false,
    isEquipped: Boolean = false,
    customColor: Color = com.example.dndcompanion.ui.theme.Waldgruen,
    onTogglePrep: () -> Unit = {},
    onDelete: (() -> Unit)? = null,
    onCastAsRitual: (() -> Unit)? = null,
    extraContent: (@Composable () -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { if (!isEditMode) expanded = !expanded }
            .border(1.dp, customColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = PergamentDunkel)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(spell.name, color = Waldgruen, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = Almendra)
                    val type = if (spell.level == 0) "Zaubertrick" else "Stufe ${spell.level}"
                    Text(type, color = TintenBraun, fontSize = 14.sp)
                }
                if (isEquipped) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Vorbereitet",
                        tint = Waldgruen,
                        modifier = Modifier.size(24.dp).padding(end = 8.dp)
                    )
                }
                if (isEditMode) {
                    Switch(
                        checked = spell.isPrepared,
                        onCheckedChange = { onTogglePrep() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = WaldGold,
                            checkedTrackColor = Waldgruen,
                            uncheckedThumbColor = EisenGrau,
                            uncheckedTrackColor = PergamentDunkel
                        )
                    )
                }
            }
            if (expanded || isEditMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Zeit: ${spell.castingTime} | Dauer: ${spell.duration} | Reichweite: ${spell.range}", color = TintenSchwarz, fontSize = 14.sp, fontWeight = FontWeight.Medium)

                // Komponenten String bauen
                val comps = mutableListOf<String>()
                if (spell.componentsV) comps.add("V")
                if (spell.componentsS) comps.add("S")
                if (spell.componentsM) {
                    val m = if (spell.materialCost.isNotBlank()) "M (${spell.materialCost})" else "M"
                    comps.add(m)
                }
                if (comps.isNotEmpty()) {
                    Text("Komponenten: ${comps.joinToString(", ")}", color = OchsenblutRot, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(spell.description, color = TintenSchwarz, fontSize = 15.sp, lineHeight = 20.sp)
                
                if (spell.isRitual && onCastAsRitual != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onCastAsRitual,
                        colors = ButtonDefaults.buttonColors(containerColor = Waldgruen),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Als Ritual wirken (+10 Min)", fontSize = 16.sp, color = Color.White, fontFamily = Almendra)
                    }
                }
                if (isEditMode && onDelete != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Aus dem Buch löschen", fontSize = 16.sp, color = Color.White, fontFamily = Almendra)
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
fun TraitCard(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = PergamentDunkel)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = OchsenblutRot, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, color = TintenSchwarz, fontSize = 16.sp)
        }
    }
}

@Composable
fun EditableTraitCard(title: String, desc: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = PergamentDunkel)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = OchsenblutRot, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(desc, color = TintenSchwarz, fontSize = 16.sp)
                }
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onEdit) {
                        Text("✏️ Bearbeiten", color = WaldGold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onDelete) {
                        Text("🗑️ Löschen", color = OchsenblutRot, fontSize = 14.sp)
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
                        Text("Schließen", color = OchsenblutRot, fontWeight = FontWeight.Bold, fontFamily = Almendra)
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
                                    onDelete = { viewModel.removeSpell(spell.id) }
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
                                    onDelete = { viewModel.removeSpell(spell.id) }
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = { showAddSpellDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Waldgruen),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(56.dp)
                ) {
                    Text("+ Zauber aus Kompendium hinzufügen", color = Color.White, fontSize = 16.sp, fontFamily = Almendra)
                }
                Spacer(modifier = Modifier.height(32.dp))
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
        Surface(modifier = Modifier.fillMaxSize(), color = PergamentHell) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Zauberkompendium", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Waldgruen, fontFamily = Almendra)
                    TextButton(onClick = onDismiss) { Text("Schließen", color = OchsenblutRot, fontWeight = FontWeight.Bold, fontFamily = Almendra) }
                }
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Zauber suchen") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OchsenblutRot,
                        focusedLabelColor = OchsenblutRot
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
                        val hasSpells = lvl == -1 || viewModel.globalSpellbook.any { spell ->
                            spell.level == lvl && (searchQuery.isBlank() || spell.name.contains(searchQuery, ignoreCase = true))
                        }
                        Button(
                            onClick = { selectedLevel = lvl },
                            enabled = hasSpells,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedLevel == lvl) OchsenblutRot else PergamentDunkel,
                                contentColor = if (selectedLevel == lvl) Color.White else if (hasSpells) TintenSchwarz else Color.DarkGray,
                                disabledContainerColor = Color.LightGray
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            val label = if (lvl == -1) "Alle" else if (lvl == 0) "Tricks" else "Grad $lvl"
                            Text(
                                text = label, 
                                fontSize = 16.sp,
                                fontFamily = Almendra
                            )
                        }
                    }
                }
                
                Box(modifier = Modifier.weight(1f)) {
                    val filteredSpells = viewModel.globalSpellbook.filter { spell -> 
                        (selectedLevel == -1 || spell.level == selectedLevel) &&
                        (searchQuery.isBlank() || spell.name.contains(searchQuery, ignoreCase = true))
                    }.sortedWith(compareBy({ it.level }, { it.name }))
                    
                    if (filteredSpells.isEmpty()) {
                        Text("Keine Zauber gefunden.", modifier = Modifier.padding(16.dp), color = TintenBraun)
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
                                            colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot, disabledContainerColor = Color.Gray),
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