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
import com.example.dndcompanion.ui.viewmodel.SpellViewModel
import com.example.dndcompanion.ui.viewmodel.CombatViewModel
import com.example.dndcompanion.ui.viewmodel.InventoryViewModel
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
fun ZauberScreen(
    viewModel: CharacterViewModel,
    spellVm: SpellViewModel,
    combatVm: CombatViewModel
) {
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
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Zauberwerte anzeigen
                Card(
                    modifier = Modifier.weight(0.5f),
                    colors = CardDefaults.cardColors(containerColor = PergamentDunkel)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("Z-Angriff: +${viewModel.spellAttackBonus}", color = TintenSchwarz, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Z-Mod: +${viewModel.spellModifier}", color = TintenSchwarz, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Z-RW DC: ${viewModel.spellSaveDc}", color = TintenSchwarz, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                // Rasten-Buttons
                Row(
                    modifier = Modifier.weight(0.5f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showShortRestDialog = true },
                        enabled = combatVm.hitDice > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Bronze),
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Kurze\nRast", fontSize = 14.sp, lineHeight = 16.sp, textAlign = TextAlign.Center)
                    }
                    Button(
                        onClick = {
                            combatVm.attemptLongRest()
                            if (!combatVm.showRestWarningDialog) showLongRestDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Waldgruen),
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Lange\nRast", fontSize = 14.sp, lineHeight = 16.sp, textAlign = TextAlign.Center)
                    }
                }
            }

            // --- KOSTENLOSE ZAUBER (TALENTE) ---
            val globalSpellbook by spellVm.globalSpellbook.collectAsState()

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

                        // Ignoriere die alten "Amulett"-Traits, da diese nun unten dynamisch geladen werden
                        if (spellId.equals("Wunden heilen", ignoreCase = true) || spellId.equals("Heilendes Wort", ignoreCase = true)) return@filter false

                        spellVm.allSpells.any { spell ->
                            spell.name.equals(spellId, ignoreCase = true)
                        }
                    }
                    freeFeatures.forEach { trait ->
                        val matchingSpell = globalSpellbook.find { it.name.equals(trait.grantedSpellId, ignoreCase = true) }
                        val fullDescription = if (matchingSpell != null) "${trait.desc}\n\n${matchingSpell.description}" else trait.desc

                        ExpandableFreeSpellCard(
                            title = trait.name,
                            description = fullDescription,
                            currentUses = trait.currentUses,
                            maxUses = trait.maxUses,
                            accentColor = accentColor,
                            onCast = { viewModel.useTraitSpell(trait) }
                        )
                    }

                    // --- GEGENSTANDS-ZAUBER (Fokus / Amulett) ---
                    // "Wunden heilen" oder "Heilendes Wort" sofern vorbereitet
                    val amuletSpell = spellVm.allSpells.find { spell ->
                        (spell.name.equals("Wunden heilen", ignoreCase = true) || spell.name.equals("Heilendes Wort", ignoreCase = true)) && spell.isPrepared
                    }
                    if (amuletSpell != null) {
                        val matchingSpell = globalSpellbook.find { it.name.equals(amuletSpell.name, ignoreCase = true) }
                        val fullDescription = "Kostenlos wirkbar über deinen druidischen Fokus (1x pro Lange Rast).\n\n${matchingSpell?.description ?: amuletSpell.description}"

                        ExpandableFreeSpellCard(
                            title = amuletSpell.name,
                            description = fullDescription,
                            currentUses = if (spellVm.freeAmuletSpellUsed) 0 else 1,
                            maxUses = 1,
                            accentColor = accentColor,
                            onCast = { spellVm.useFreeAmuletSpell() }
                        )
                    }

                    if (freeFeatures.isEmpty() && amuletSpell == null) {
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
                            Text("${spellVm.spellSlotsLevel1} / 3", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(end = 16.dp))
                            Button(
                                onClick = { spellVm.useSpellSlotLevel1() },
                                enabled = spellVm.spellSlotsLevel1 > 0,
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
                    
                    if (spellVm.allSpells.any { it.name.contains("gute beere", ignoreCase = true) && it.isPrepared }) {
                        HorizontalDivider(color = PergamentHell)
                        Button(
                            onClick = { combatVm.castGoodberry() },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            enabled = spellVm.spellSlotsLevel1 > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Waldgruen,
                                disabledContainerColor = EisenGrau
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Gute Beere wirken", fontFamily = Almendra, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("(${combatVm.inventoryVm?.goodberries ?: 0} im Inventar)", fontSize = 12.sp)
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
                                Text("${spellVm.spellSlotsLevel2} / 2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(end = 16.dp))
                                Button(
                                    onClick = { spellVm.useSpellSlotLevel2() },
                                    enabled = spellVm.spellSlotsLevel2 > 0,
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
                                Text("${spellVm.spellSlotsLevel3} / 2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(end = 16.dp))
                                Button(
                                    onClick = { spellVm.useSpellSlotLevel3() },
                                    enabled = spellVm.spellSlotsLevel3 > 0,
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
                val currentSlotsLevel2 = spellVm.spellSlotsLevel2

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
                                    onClick = { spellVm.useSpellSlotLevel2() },
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
                                onClick = { spellVm.resetWarlockSlots() },
                                colors = ButtonDefaults.buttonColors(containerColor = Waldgruen)
                            ) {
                                Text("Kurze Rast Reg.", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { spellVm.applyMagicalCunning() },
                                colors = ButtonDefaults.buttonColors(containerColor = HexenLila)
                            ) {
                                Text("Magische Rafinesse", color = PergamentHell, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { combatVm.applyFalseLife() },
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
                                Text("Talent: Eingeweihter", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                            }
                            Text("${if (spellVm.freeBlessUsed) 0 else 1} / 1", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                            Button(
                                onClick = { spellVm.useFreeBless() },
                                enabled = !spellVm.freeBlessUsed,
                                modifier = Modifier.height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = if (accentColor == WaldGold) TintenSchwarz else Color.White
                                )
                            ) { Text("Wirken", fontSize = 14.sp) }
                        }

                        // Nebelschritt
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Nebelschritt", color = Color.White, fontSize = 14.sp)
                                Text("Talent: Feenberührt", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                            }
                            Text("${if (spellVm.freeMistyStepUsed) 0 else 1} / 1", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                            Button(
                                onClick = { spellVm.useFreeMistyStep() },
                                enabled = !spellVm.freeMistyStepUsed,
                                modifier = Modifier.height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = if (accentColor == WaldGold) TintenSchwarz else Color.White
                                )
                            ) { Text("Wirken", fontSize = 14.sp) }
                        }

                        // Magierrüstung
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Magierrüstung", color = Color.White, fontSize = 14.sp)
                                Text("Talent: Eingeweihter", color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                            }
                            Text("${if (spellVm.freeMageArmorUsed) 0 else 1} / 1", color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                            Button(
                                onClick = {
                                    spellVm.useFreeMageArmor()
                                    combatVm.toggleMageArmor(true) // Activate the AC effect
                                },
                                enabled = !spellVm.freeMageArmorUsed,
                                modifier = Modifier.height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = if (accentColor == WaldGold) TintenSchwarz else Color.White
                                )
                            ) { Text("Wirken", fontSize = 14.sp) }
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

            val preparedSpells = spellVm.allSpells.filter { it.isPrepared }
            val cantrips = preparedSpells.filter { it.level == 0 }
            val leveledSpells = preparedSpells.filter { it.level > 0 }.sortedBy { it.level }

            if (cantrips.isNotEmpty()) {
                Text("Zaubertricks (0)", color = Waldgruen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                cantrips.forEach { spell ->
                    SpellCard(
                        spell = spell,
                        onCastAsRitual = if (spellVm.canCastAsRitual(spell)) { { /* Visual feedback only for now */ } } else null,
                        globalSpellbook = globalSpellbook
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (leveledSpells.isNotEmpty()) {
                Text("Vorbereitete Zauber", color = Waldgruen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                leveledSpells.forEach { spell ->
                    SpellCard(
                        spell = spell,
                        onCastAsRitual = if (spellVm.canCastAsRitual(spell)) { { /* Visual feedback only for now */ } } else null,
                        globalSpellbook = globalSpellbook
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (preparedSpells.isEmpty()) {
                Text("Keine Zauber vorbereitet. Klicke auf das Stift-Symbol, um Zauber auszuwählen.", color = TintenBraun, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
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
                                Text("$hitDiceToSpend / ${combatVm.hitDice}", color = TintenSchwarz, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                IconButton(onClick = { if (hitDiceToSpend < combatVm.hitDice) hitDiceToSpend++ }) {
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
