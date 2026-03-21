package com.example.dndmietling.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.dndmietling.data.InitiativeEntry
import com.example.dndmietling.data.MietlingCharacter
import com.example.dndmietling.data.ParticipantType
import com.example.dndmietling.ui.theme.*
import com.example.dndmietling.ui.viewmodel.MietlingViewModel

// Alle möglichen Standardcharaktere (Mietling + Companion)
private val ALL_STANDARD_CHARS = MietlingCharacter.entries.toList()

@Composable
fun InitiativeTrackerScreen(viewModel: MietlingViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showDamageDialog by remember { mutableStateOf<InitiativeEntry?>(null) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val sorted = viewModel.initiativeEntries.sortedByDescending { it.initiative }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Pergament)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(WaldgruenDunkel)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Initiative – Runde ${viewModel.currentRound}",
                    color = WaldGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Hinzufügen", tint = PergamentHell)
                    }
                    if (sorted.isNotEmpty()) {
                        IconButton(onClick = { viewModel.nextTurn() }) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Nächster", tint = WaldGold)
                        }
                        IconButton(onClick = { showClearConfirm = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Leeren", tint = OchsenblutRot)
                        }
                    }
                }
            }
        }

        if (sorted.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚔️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Keine Teilnehmer", color = TintenBraun, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tippe auf + um zu beginnen", color = EisenGrau, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sorted, key = { it.id }) { entry ->
                    InitiativeEntryCard(
                        entry = entry,
                        isActive = entry.id == viewModel.activeEntryId,
                        onDamageClick = { showDamageDialog = entry },
                        onToggleCritical = { viewModel.toggleCriticalHint(entry.id, entry.criticalHint) },
                        onRemove = { viewModel.removeInitiativeEntry(entry.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddParticipantDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { entry ->
                viewModel.addInitiativeEntry(entry)
                showAddDialog = false
            }
        )
    }

    showDamageDialog?.let { entry ->
        DamageDialog(
            entry = entry,
            onDismiss = { showDamageDialog = null },
            onUpdate = { newDamage ->
                viewModel.updateMonsterDamage(entry.id, newDamage)
                showDamageDialog = null
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Tracker leeren?") },
            text = { Text("Alle Teilnehmer werden entfernt und die Runde zurückgesetzt.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearInitiativeTracker()
                    viewModel.currentRound = 1
                    showClearConfirm = false
                }) { Text("Leeren", color = OchsenblutRot) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Abbrechen") }
            }
        )
    }
}

@Composable
private fun InitiativeEntryCard(
    entry: InitiativeEntry,
    isActive: Boolean,
    onDamageClick: () -> Unit,
    onToggleCritical: () -> Unit,
    onRemove: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isActive -> Waldgruen.copy(alpha = 0.15f)
            entry.criticalHint -> OchsenblutRot.copy(alpha = 0.08f)
            else -> PergamentHell
        },
        label = "bg"
    )
    val borderColor = when {
        isActive -> Waldgruen
        entry.criticalHint -> OchsenblutRot
        else -> PergamentDunkel
    }

    val isMonster = entry.type == ParticipantType.MONSTER.name

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Initiative Badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isActive) Waldgruen else WaldgruenDunkel),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${entry.initiative}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isActive) {
                        Text("▶", color = Waldgruen, fontSize = 12.sp)
                    }
                    Text(
                        entry.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isActive) Waldgruen else TintenSchwarz
                    )
                    if (isMonster) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = OchsenblutRot.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "Monster",
                                fontSize = 10.sp,
                                color = OchsenblutRot,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (isMonster) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Schaden: ${entry.damageTaken}",
                            fontSize = 13.sp,
                            color = if (entry.damageTaken > 0) OchsenblutRot else TintenBraun
                        )
                        if (entry.criticalHint) {
                            Text("⚠ Krit. Hinweis", fontSize = 11.sp, color = OchsenblutRot, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Aktionen
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (isMonster) {
                    IconButton(onClick = onDamageClick, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Shield, contentDescription = "Schaden", tint = OchsenblutRot, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onToggleCritical, modifier = Modifier.size(36.dp)) {
                        Icon(
                            if (entry.criticalHint) Icons.Default.Warning else Icons.Default.WarningAmber,
                            contentDescription = "Krit. Hinweis",
                            tint = if (entry.criticalHint) OchsenblutRot else EisenGrau,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Entfernen", tint = EisenGrau, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun AddParticipantDialog(
    onDismiss: () -> Unit,
    onAdd: (InitiativeEntry) -> Unit
) {
    var tab by remember { mutableStateOf(0) } // 0 = Charakter, 1 = Monster
    var selectedChar by remember { mutableStateOf<MietlingCharacter?>(null) }
    var monsterName by remember { mutableStateOf("") }
    var initiativeText by remember { mutableStateOf("") }
    var maxHpText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PergamentHell)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Teilnehmer hinzufügen", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Waldgruen)

                // Tab-Auswahl
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = tab == 0,
                        onClick = { tab = 0; selectedChar = null },
                        label = { Text("Charakter") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Waldgruen,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = tab == 1,
                        onClick = { tab = 1; selectedChar = null },
                        label = { Text("Monster") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OchsenblutRot,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                if (tab == 0) {
                    // Charakter-Auswahl (alle Standard-Chars)
                    Text("Charakter wählen:", fontSize = 13.sp, color = TintenBraun)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ALL_STANDARD_CHARS.chunked(3).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                row.forEach { char ->
                                    FilterChip(
                                        selected = selectedChar == char,
                                        onClick = { selectedChar = char },
                                        label = { Text("${char.emoji} ${char.displayName}", fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Waldgruen,
                                            selectedLabelColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // Padding für unvollständige Zeilen
                                repeat(3 - row.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    // Monster-Name
                    OutlinedTextField(
                        value = monsterName,
                        onValueChange = { monsterName = it },
                        label = { Text("Monster-Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OchsenblutRot)
                    )
                    OutlinedTextField(
                        value = maxHpText,
                        onValueChange = { maxHpText = it },
                        label = { Text("Max HP (optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OchsenblutRot)
                    )
                }

                // Initiative
                OutlinedTextField(
                    value = initiativeText,
                    onValueChange = { initiativeText = it },
                    label = { Text("Initiative (gewürfelt)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Waldgruen)
                )

                if (errorMsg.isNotEmpty()) {
                    Text(errorMsg, color = OchsenblutRot, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Abbrechen")
                    }
                    Button(
                        onClick = {
                            val initiative = initiativeText.toIntOrNull()
                            if (initiative == null) {
                                errorMsg = "Bitte Initiative eingeben"
                                return@Button
                            }
                            val name = if (tab == 0) {
                                selectedChar?.displayName ?: run {
                                    errorMsg = "Charakter wählen"
                                    return@Button
                                }
                            } else {
                                if (monsterName.isBlank()) {
                                    errorMsg = "Monster-Name eingeben"
                                    return@Button
                                }
                                monsterName.trim()
                            }
                            val entry = InitiativeEntry(
                                name = name,
                                initiative = initiative,
                                type = if (tab == 0) ParticipantType.CHARACTER.name else ParticipantType.MONSTER.name,
                                maxHp = maxHpText.toIntOrNull() ?: 0
                            )
                            onAdd(entry)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Waldgruen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Hinzufügen")
                    }
                }
            }
        }
    }
}

@Composable
private fun DamageDialog(
    entry: InitiativeEntry,
    onDismiss: () -> Unit,
    onUpdate: (Int) -> Unit
) {
    var damageText by remember { mutableStateOf("") }
    var totalDamage by remember { mutableStateOf(entry.damageTaken) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PergamentHell)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(entry.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OchsenblutRot)

                Text("Schaden gesamt: $totalDamage", fontSize = 16.sp, color = TintenSchwarz)

                if (entry.maxHp > 0) {
                    LinearProgressIndicator(
                        progress = { (1f - (totalDamage.toFloat() / entry.maxHp)).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(12.dp),
                        color = Waldgruen,
                        trackColor = OchsenblutRot
                    )
                    Text(
                        "HP: ${(entry.maxHp - totalDamage).coerceAtLeast(0)} / ${entry.maxHp}",
                        fontSize = 13.sp,
                        color = TintenBraun,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = damageText,
                    onValueChange = { damageText = it },
                    label = { Text("Schaden hinzufügen") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OchsenblutRot)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { totalDamage = (totalDamage - (damageText.toIntOrNull() ?: 0)).coerceAtLeast(0); damageText = "" },
                        modifier = Modifier.weight(1f)
                    ) { Text("Heilen", color = Waldgruen) }
                    Button(
                        onClick = { totalDamage += damageText.toIntOrNull() ?: 0; damageText = "" },
                        colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot),
                        modifier = Modifier.weight(1f)
                    ) { Text("Schaden") }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Abbrechen") }
                    Button(
                        onClick = { onUpdate(totalDamage) },
                        colors = ButtonDefaults.buttonColors(containerColor = Waldgruen),
                        modifier = Modifier.weight(1f)
                    ) { Text("Speichern") }
                }
            }
        }
    }
}
