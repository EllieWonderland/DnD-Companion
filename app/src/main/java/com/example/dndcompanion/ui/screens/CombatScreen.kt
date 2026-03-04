package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import com.example.dndcompanion.ui.viewmodel.ActiveWeapon
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.theme.BlauDunkel
import com.example.dndcompanion.ui.theme.BlauHell
import com.example.dndcompanion.ui.theme.PinkDunkel
import com.example.dndcompanion.ui.theme.PinkHell
import com.example.dndcompanion.ui.theme.GelbSand

@Composable
fun CombatScreen(viewModel: CharacterViewModel, onNavigateToRucksack: () -> Unit, onNavigateToProfile: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GelbSand)
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top-Leiste: Passive Stats
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Initiative: +4", color = BlauDunkel, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Tempo: 9", color = BlauDunkel, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Pass. Wahrnehmung: 16", color = BlauDunkel, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        // Lebenspunkte & Trefferwürfel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = BlauHell),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("HP: ${viewModel.currentHp} / ${viewModel.maxHp}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Trefferwürfel: ${viewModel.hitDice}/4", color = BlauDunkel, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { viewModel.currentHp.toFloat() / viewModel.maxHp.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp),
                    color = if (viewModel.currentHp > 10) PinkDunkel else Color.Red,
                    trackColor = BlauDunkel
                )

                if (viewModel.currentHp == 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    DeathSavesRow(viewModel)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Schnell-Buttons für Schaden und Heilung
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = { viewModel.takeDamage(5) }, colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel)) { Text("-5", fontSize = 16.sp) }
                    Button(onClick = { viewModel.takeDamage(1) }, colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel)) { Text("-1", fontSize = 16.sp) }
                    Button(onClick = { viewModel.healManual(1) }, colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel)) { Text("+1", fontSize = 16.sp) }
                    Button(onClick = { viewModel.healManual(5) }, colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel)) { Text("+5", fontSize = 16.sp) }
                }
            }
        }

        // Große Anzeige: RK und EP nebeneinander
        var showEpDialog by remember { mutableStateOf(false) }
        var epInput by remember { mutableStateOf("") }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rüstungsklasse
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = BlauDunkel),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "Rüstungsklasse", color = Color.White, fontSize = 14.sp)
                    Text(
                        text = viewModel.currentArmorClass.toString(),
                        color = PinkHell,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Erfahrungspunkte
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = BlauDunkel),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "EP (Stufe ${viewModel.level})", color = Color.White, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = viewModel.currentEP.toString(),
                            color = PinkHell,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showEpDialog = true }) {
                            Text("+", color = PinkHell, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // EP Dialog
        if (showEpDialog) {
            AlertDialog(
                onDismissRequest = { showEpDialog = false },
                containerColor = GelbSand,
                title = { Text("EP hinzufügen", color = BlauDunkel, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = epInput,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) epInput = it },
                        label = { Text("Erfahrungspunkte") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PinkDunkel,
                            focusedLabelColor = PinkDunkel
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = epInput.toIntOrNull()
                            if (amount != null && amount > 0) {
                                viewModel.addExperience(amount)
                                epInput = ""
                                showEpDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel)
                    ) {
                        Text("Hinzufügen")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEpDialog = false }) {
                        Text("Abbrechen", color = BlauDunkel)
                    }
                }
            )
        }

        // NEU: Level Up Benachrichtigung im Kampfscreen
        if (viewModel.showLevelUpNotification) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissLevelUpNotification() },
                containerColor = GelbSand,
                title = { Text("Level Up! Stufe ${viewModel.level} erreicht!", color = BlauDunkel, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                text = { Text("Glückwunsch! Deine Erfahrungspunkte reichen für einen Stufenaufstieg. Möchtest du deinen Charakter jetzt verbessern?", color = BlauDunkel) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.dismissLevelUpNotification()
                            onNavigateToProfile()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel)
                    ) {
                        Text("Jetzt anpassen")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissLevelUpNotification() }) {
                        Text("Später erledigen", color = BlauDunkel)
                    }
                }
            )
        }

        Text("Waffe ausrüsten", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
        Spacer(modifier = Modifier.height(8.dp))

        // Waffen-Auswahl
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WeaponButton(
                title = "Langbogen",
                isSelected = viewModel.currentWeapon == ActiveWeapon.LANGBOGEN,
                onClick = { viewModel.equipWeapon(ActiveWeapon.LANGBOGEN) }
            )
            WeaponButton(
                title = "Kurzschwert\n& Schild",
                isSelected = viewModel.currentWeapon == ActiveWeapon.KURZSCHWERT_SCHILD,
                onClick = { viewModel.equipWeapon(ActiveWeapon.KURZSCHWERT_SCHILD) }
            )
            WeaponButton(
                title = "Shillelagh\n& Schild",
                isSelected = viewModel.currentWeapon == ActiveWeapon.SHILLELAGH_SCHILD,
                onClick = { viewModel.equipWeapon(ActiveWeapon.SHILLELAGH_SCHILD) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Anzeige der aktuellen Waffenwerte
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlauHell),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Trefferbonus: ${viewModel.currentAttackBonus}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Schaden: ${viewModel.currentDamage}", fontSize = 16.sp, color = Color.White)

                Spacer(modifier = Modifier.height(8.dp))

                // Dynamischer Hinweis je nach Waffentyp
                val extraNote = when (viewModel.currentWeapon) {
                    ActiveWeapon.LANGBOGEN -> "Verlangsamen: Ziel -3 Bewegung.\nMesserstecher: 1x/Zug 1 Angriffswürfel (Stich) neu werfen. Bei Krit +1 Schadenswürfel."
                    ActiveWeapon.KURZSCHWERT_SCHILD -> "Plagen: Nächster Angriff hat Vorteil.\nMesserstecher: 1x/Zug 1 Angriffswürfel (Stich) neu werfen. Bei Krit +1 Schadenswürfel."
                    ActiveWeapon.SHILLELAGH_SCHILD -> "Umstoßen (Mastery): Gegner muss bei Treffer Kon-Save (DC 12) bestehen oder liegt am Boden."
                }

                Text(
                    text = extraNote,
                    fontSize = 14.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = BlauDunkel
                )
            }
        }

        if (viewModel.currentWeapon == ActiveWeapon.LANGBOGEN) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BlauHell),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Pfeilköcher", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Verfügbar: ${viewModel.totalArrows}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Verschossen: ${viewModel.shotArrows}", fontSize = 16.sp, color = if (viewModel.shotArrows > 0) PinkDunkel else Color.White)
                        }

                        Button(
                            onClick = { viewModel.shootArrow() },
                            enabled = viewModel.totalArrows > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PinkDunkel,
                                disabledContainerColor = Color.Gray
                            )
                        ) {
                            Text("Schießen")
                        }
                    }

                    if (viewModel.shotArrows > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = BlauDunkel)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nach dem Kampf:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { viewModel.recoverArrows() },
                                colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel),
                                modifier = Modifier.weight(1f).padding(end = 4.dp)
                            ) {
                                Text("½ Einsammeln", fontSize = 14.sp)
                            }
                            Button(
                                onClick = { viewModel.discardShotArrows() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f)),
                                modifier = Modifier.weight(1f).padding(start = 4.dp)
                            ) {
                                Text("Alle verloren", fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BlauDunkel)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pfeile...", fontSize = 14.sp, color = BlauDunkel, fontWeight = FontWeight.Bold)
                        Row {
                            Button(
                                onClick = { viewModel.changeTotalArrows(-1) },
                                colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier.height(36.dp)
                            ) { Text("- Ablegen", fontSize = 12.sp) }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.changeTotalArrows(1) },
                                colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier.height(36.dp)
                            ) { Text("+ Aufnehmen", fontSize = 12.sp) }
                        }
                    }
                }
            }
        }

        // NEU: Auffälliger Loot-Button zur direkten Navigation in den Rucksack
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onNavigateToRucksack,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("💰 Loot eintragen", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Standard-Taktik (Moved from ZauberScreen)
        var isTacticExpanded by remember { mutableStateOf(false) }
        var isTacticEditing by remember { mutableStateOf(false) }
        var editTacticText by remember { mutableStateOf(viewModel.standardTactic) }

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { 
                if (!isTacticEditing) isTacticExpanded = !isTacticExpanded 
            },
            colors = CardDefaults.cardColors(containerColor = BlauHell)
        ) {
            Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Standard-Taktik", color = GelbSand, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (isTacticExpanded && !isTacticEditing) {
                        IconButton(onClick = { 
                            editTacticText = viewModel.standardTactic
                            isTacticEditing = true 
                        }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Bearbeiten", tint = PinkDunkel)
                        }
                    }
                }
                if (isTacticExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isTacticEditing) {
                        OutlinedTextField(
                            value = editTacticText,
                            onValueChange = { editTacticText = it },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PinkDunkel,
                                unfocusedTextColor = Color.White,
                                focusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { isTacticEditing = false }) { Text("Abbrechen", color = Color.White) }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.updateStandardTactic(editTacticText)
                                    isTacticEditing = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel)
                            ) { Text("Speichern") }
                        }
                    } else {
                        Text(viewModel.standardTactic, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun WeaponButton(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) PinkDunkel else BlauHell,
            contentColor = if (isSelected) Color.White else BlauDunkel
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(110.dp)
            .height(60.dp)
    ) {
        Text(text = title, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 16.sp)
    }
}

@Composable
fun DeathSavesRow(viewModel: CharacterViewModel) {
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showFailureDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.deathSaveSuccesses) {
        if (viewModel.deathSaveSuccesses >= 3) {
            showSuccessDialog = true
        }
    }

    LaunchedEffect(viewModel.deathSaveFailures) {
        if (viewModel.deathSaveFailures >= 3) {
            showFailureDialog = true
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Stabilisiert!", color = GelbSand, fontWeight = FontWeight.Bold) },
            text = { Text("Du hast 3 erfolgreiche Rettungswürfe geschafft. Du bist stabilisiert (HP bleiben 0, aber du stirbst nicht).", color = BlauDunkel) },
            confirmButton = {
                Button(onClick = { showSuccessDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel)) {
                    Text("Puh!")
                }
            },
            containerColor = BlauHell
        )
    }

    if (showFailureDialog) {
        AlertDialog(
            onDismissRequest = { showFailureDialog = false },
            title = { Text("Gefallen...", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text("Du hast 3 fehlgeschlagene Rettungswürfe ereilt. Athania ist gestorben...", color = BlauDunkel) },
            confirmButton = {
                Button(onClick = { showFailureDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel)) {
                    Text("RiP")
                }
            },
            containerColor = Color.LightGray
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Todesrettungswürfe", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Erfolge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Erfolge:", color = GelbSand, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                repeat(3) { index ->
                    val checked = index < viewModel.deathSaveSuccesses
                    Icon(
                        imageVector = if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Erfolg",
                        tint = if (checked) GelbSand else Color.Gray,
                        modifier = Modifier.size(24.dp).clickable { 
                            if (checked) viewModel.updateDeathSaves(index, viewModel.deathSaveFailures)
                            else viewModel.updateDeathSaves(index + 1, viewModel.deathSaveFailures)
                        }
                    )
                }
            }
            // Fehlschläge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Fehlschläge:", color = Color.Red, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                repeat(3) { index ->
                    val checked = index < viewModel.deathSaveFailures
                    Icon(
                        imageVector = if (checked) Icons.Default.Cancel else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Fehlschlag",
                        tint = if (checked) Color.Red else Color.Gray,
                        modifier = Modifier.size(24.dp).clickable {
                            if (checked) viewModel.updateDeathSaves(viewModel.deathSaveSuccesses, index)
                            else viewModel.updateDeathSaves(viewModel.deathSaveSuccesses, index + 1)
                        }
                    )
                }
            }
        }
    }
}