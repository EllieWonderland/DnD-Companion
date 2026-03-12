package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import com.example.dndcompanion.ui.viewmodel.ActiveWeapon
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.data.CharacterClass

@Composable
fun CombatScreen(viewModel: CharacterViewModel, onNavigateToRucksack: () -> Unit, onNavigateToProfile: () -> Unit = {}) {
    val isRanger = viewModel.characterData.charClass == CharacterClass.RANGER
    val accentColor = if (isRanger) WaldGold else HexenLila

    PergamentBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top-Leiste: Passive Stats
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("Initiative: ${if(viewModel.initiative >= 0) "+" else ""}${viewModel.initiative}", style = MaterialTheme.typography.labelLarge, color = Waldgruen)
                Text("Tempo: ${viewModel.speed}", style = MaterialTheme.typography.labelLarge, color = Waldgruen)
                Text("Pass. Wahrnehmung: ${viewModel.passivePerception}", style = MaterialTheme.typography.labelLarge, color = Waldgruen)
            }

            // Lebenspunkte & Trefferwürfel
            PergamentCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                "HP: ${viewModel.currentHp} / ${viewModel.maxHp}",
                                style = GrenzeGotischStyle,
                                color = if (viewModel.currentHp > 10) TintenSchwarz else OchsenblutRot
                            )
                            if (viewModel.tempHp > 0) {
                                Text(
                                    "+${viewModel.tempHp} Temp HP",
                                    style = GrenzeGotischSmall,
                                    color = TempHPBlau
                                )
                            }
                        }
                        Text(
                            "Trefferwürfel: ${viewModel.hitDice}/${viewModel.characterData.baseHitDice}",
                            style = MaterialTheme.typography.labelLarge,
                            color = TintenBraun
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // HP Bar
                    LinearProgressIndicator(
                        progress = { viewModel.currentHp.toFloat() / viewModel.maxHp.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp),
                        color = if (viewModel.currentHp > 10) Waldgruen else OchsenblutRot,
                        trackColor = PergamentDunkel
                    )

                    // Temp HP Bar (wenn vorhanden)
                    if (viewModel.tempHp > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { viewModel.tempHp.toFloat() / 12f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = TempHPBlau,
                            trackColor = PergamentDunkel.copy(alpha = 0.3f)
                        )
                    }

                    if (viewModel.currentHp == 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        DeathSavesRow(viewModel)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // HP-Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(onClick = { viewModel.takeDamage(5) }, colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot), shape = RoundedCornerShape(8.dp), modifier = Modifier.width(60.dp), contentPadding = PaddingValues(0.dp)) { Text("-5", fontFamily = Almendra, fontSize = 14.sp) }
                        Button(onClick = { viewModel.takeDamage(1) }, colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot), shape = RoundedCornerShape(8.dp), modifier = Modifier.width(60.dp), contentPadding = PaddingValues(0.dp)) { Text("-1", fontFamily = Almendra, fontSize = 14.sp) }
                        Button(onClick = { viewModel.healManual(1) }, colors = ButtonDefaults.buttonColors(containerColor = Waldgruen), shape = RoundedCornerShape(8.dp), modifier = Modifier.width(60.dp), contentPadding = PaddingValues(0.dp)) { Text("+1", fontFamily = Almendra, fontSize = 14.sp) }
                        Button(onClick = { viewModel.healManual(5) }, colors = ButtonDefaults.buttonColors(containerColor = Waldgruen), shape = RoundedCornerShape(8.dp), modifier = Modifier.width(60.dp), contentPadding = PaddingValues(0.dp)) { Text("+5", fontFamily = Almendra, fontSize = 14.sp) }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = PergamentDunkel)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Temp HP Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Temp HP:", style = MaterialTheme.typography.labelLarge, color = TempHPBlau)
                        Button(onClick = { viewModel.modifyTempHp(-1) }, colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot.copy(alpha = 0.6f)), shape = RoundedCornerShape(6.dp), modifier = Modifier.width(48.dp), contentPadding = PaddingValues(0.dp)) { Text("-1", fontSize = 14.sp) }
                        Button(onClick = { viewModel.modifyTempHp(1) }, colors = ButtonDefaults.buttonColors(containerColor = Bronze.copy(alpha = 0.7f)), shape = RoundedCornerShape(6.dp), modifier = Modifier.width(48.dp), contentPadding = PaddingValues(0.dp)) { Text("+1", fontSize = 14.sp) }
                        Button(onClick = { viewModel.modifyTempHp(12) }, colors = ButtonDefaults.buttonColors(containerColor = Bronze), shape = RoundedCornerShape(6.dp), modifier = Modifier.width(48.dp), contentPadding = PaddingValues(0.dp)) { Text("+12", fontSize = 14.sp) }
                    }
                }
            }

            // RK und EP nebeneinander
            var showEpDialog by remember { mutableStateOf(false) }
            var epInput by remember { mutableStateOf("") }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rüstungsklasse
                SteinCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Rüstungsklasse", style = MaterialTheme.typography.labelMedium, color = Waldgruen)
                        Text(
                            viewModel.currentArmorClass.toString(),
                            style = GrenzeGotischStyle.copy(fontSize = 48.sp),
                            color = TintenSchwarz
                        )
                    }
                }

                // Erfahrungspunkte
                SteinCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("EP (Stufe ${viewModel.level})", style = MaterialTheme.typography.labelMedium, color = Waldgruen)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                viewModel.currentEP.toString(),
                                style = GrenzeGotischStyle.copy(fontSize = 48.sp),
                                color = TintenSchwarz
                            )
                            IconButton(onClick = { showEpDialog = true }) {
                                Text("+", color = accentColor, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // EP Dialog
            if (showEpDialog) {
                AlertDialog(
                    onDismissRequest = { showEpDialog = false },
                    containerColor = PergamentHell,
                    shape = RoundedCornerShape(12.dp),
                    title = { Text("EP hinzufügen", style = MaterialTheme.typography.titleSmall, color = Waldgruen) },
                    text = {
                        OutlinedTextField(
                            value = epInput,
                            onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) epInput = it },
                            label = { Text("Erfahrungspunkte", fontFamily = Almendra) },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                focusedLabelColor = accentColor
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = if (accentColor == WaldGold) TintenSchwarz else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("Hinzufügen", fontFamily = Almendra) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEpDialog = false }) {
                            Text("Abbrechen", color = Waldgruen, fontFamily = Almendra)
                        }
                    }
                )
            }

            // Level Up Benachrichtigung
            if (viewModel.showLevelUpNotification) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissLevelUpNotification() },
                    containerColor = PergamentHell,
                    shape = RoundedCornerShape(12.dp),
                    title = { Text("Level Up! Stufe ${viewModel.level} erreicht!", style = MaterialTheme.typography.titleSmall, color = accentColor) },
                    text = { Text("Glückwunsch! Deine Erfahrungspunkte reichen für einen Stufenaufstieg. Möchtest du deinen Charakter jetzt verbessern?", style = MaterialTheme.typography.bodyMedium, color = TintenSchwarz) },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.dismissLevelUpNotification()
                                onNavigateToProfile()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = if (accentColor == WaldGold) TintenSchwarz else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("Jetzt anpassen", fontFamily = Almendra) }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissLevelUpNotification() }) {
                            Text("Später erledigen", color = Waldgruen, fontFamily = Almendra)
                        }
                    }
                )
            }

            Text("Waffe ausrüsten", style = MaterialTheme.typography.titleMedium, color = Waldgruen)
            Spacer(modifier = Modifier.height(8.dp))

            // Waffen-Auswahl
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (isRanger) {
                    WeaponButton("Langbogen", viewModel.currentWeapon == ActiveWeapon.LANGBOGEN, WaldGold) { viewModel.equipWeapon(ActiveWeapon.LANGBOGEN) }
                    WeaponButton("Kurzschwert\n& Schild", viewModel.currentWeapon == ActiveWeapon.KURZSCHWERT_SCHILD, WaldGold) { viewModel.equipWeapon(ActiveWeapon.KURZSCHWERT_SCHILD) }
                    WeaponButton("Shillelagh\n& Schild", viewModel.currentWeapon == ActiveWeapon.SHILLELAGH_SCHILD, WaldGold) { viewModel.equipWeapon(ActiveWeapon.SHILLELAGH_SCHILD) }
                } else {
                    WeaponButton("Kriegshammer\n(Pakt)", viewModel.currentWeapon == ActiveWeapon.KRIEGSHAMMER_PAKT, HexenLila) { viewModel.equipWeapon(ActiveWeapon.KRIEGSHAMMER_PAKT) }
                    WeaponButton("Speer\n(Pakt)", viewModel.currentWeapon == ActiveWeapon.SPEER_PAKT, HexenLila) { viewModel.equipWeapon(ActiveWeapon.SPEER_PAKT) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Anzeige der aktuellen Waffenwerte
            PergamentCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Trefferbonus: ${viewModel.currentAttackBonus}", style = MaterialTheme.typography.titleSmall, color = Waldgruen)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Schaden: ${viewModel.currentDamage}", style = MaterialTheme.typography.bodyMedium, color = TintenSchwarz)

                    Spacer(modifier = Modifier.height(8.dp))

                    val extraNote = when (viewModel.currentWeapon) {
                        ActiveWeapon.LANGBOGEN -> "Verlangsamen: Ziel -3 Bewegung.\nMesserstecher: 1x/Zug 1 Angriffswürfel (Stich) neu werfen. Bei Krit +1 Schadenswürfel."
                        ActiveWeapon.KURZSCHWERT_SCHILD -> "Plagen: Nächster Angriff hat Vorteil.\nMesserstecher: 1x/Zug 1 Angriffswürfel (Stich) neu werfen. Bei Krit +1 Schadenswürfel."
                        ActiveWeapon.SHILLELAGH_SCHILD -> "Umstoßen (Mastery): Gegner muss bei Treffer Kon-Save (DC 12) bestehen oder liegt am Boden."
                        ActiveWeapon.KRIEGSHAMMER_PAKT -> "Paktwaffe: Nutzt Charisma. Umstoßen (Mastery): Gegner muss bei Treffer Kon-Save bestehen oder liegt am Boden."
                        ActiveWeapon.SPEER_PAKT -> "Paktwaffe: Nutzt Charisma. Sap (Mastery): Nächster Angriff des Gegners hat Nachteil."
                    }

                    Text(
                        text = extraNote,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = TintenBraun
                    )
                }
            }

            if (viewModel.currentWeapon == ActiveWeapon.LANGBOGEN && isRanger) {
                Spacer(modifier = Modifier.height(16.dp))
                PergamentCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Pfeilköcher", style = MaterialTheme.typography.titleSmall, color = Waldgruen)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Verfügbar: ${viewModel.totalArrows}", style = MaterialTheme.typography.bodyMedium, color = TintenSchwarz)
                                Text("Verschossen: ${viewModel.shotArrows}", style = MaterialTheme.typography.bodyMedium, color = if (viewModel.shotArrows > 0) OchsenblutRot else TintenSchwarz)
                            }
                            Button(
                                onClick = { viewModel.shootArrow() },
                                enabled = viewModel.totalArrows > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot, disabledContainerColor = EisenGrau),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Schießen", fontFamily = Almendra) }
                        }

                        if (viewModel.shotArrows > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = PergamentDunkel)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Nach dem Kampf:", style = MaterialTheme.typography.labelLarge, color = Waldgruen)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(onClick = { viewModel.recoverArrows() }, colors = ButtonDefaults.buttonColors(containerColor = Bronze), modifier = Modifier.weight(1f).padding(end = 4.dp), shape = RoundedCornerShape(8.dp)) { Text("½ Einsammeln", fontSize = 13.sp, fontFamily = Almendra) }
                                Button(onClick = { viewModel.discardShotArrows() }, colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot.copy(alpha = 0.7f)), modifier = Modifier.weight(1f).padding(start = 4.dp), shape = RoundedCornerShape(8.dp)) { Text("Alle verloren", fontSize = 13.sp, fontFamily = Almendra) }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = PergamentDunkel)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Pfeile...", style = MaterialTheme.typography.labelLarge, color = Waldgruen)
                            Row {
                                Button(onClick = { viewModel.changeTotalArrows(-1) }, colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp), modifier = Modifier.height(36.dp), shape = RoundedCornerShape(6.dp)) { Text("- Ablegen", fontSize = 12.sp, fontFamily = Almendra) }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = { viewModel.changeTotalArrows(1) }, colors = ButtonDefaults.buttonColors(containerColor = Bronze), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp), modifier = Modifier.height(36.dp), shape = RoundedCornerShape(6.dp)) { Text("+ Aufnehmen", fontSize = 12.sp, fontFamily = Almendra) }
                            }
                        }
                    }
                }
            }

            // Loot-Button
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateToRucksack,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Bronze),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("💰 Loot eintragen", style = MaterialTheme.typography.titleSmall, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Standard-Taktik
            var isTacticExpanded by remember { mutableStateOf(false) }
            var isTacticEditing by remember { mutableStateOf(false) }
            var editTacticText by remember { mutableStateOf(viewModel.standardTactic) }

            PergamentCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                        .clickable { if (!isTacticEditing) isTacticExpanded = !isTacticExpanded }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Standard-Taktik", style = MaterialTheme.typography.titleSmall, color = Waldgruen)
                        if (isTacticExpanded && !isTacticEditing) {
                            IconButton(onClick = {
                                editTacticText = viewModel.standardTactic
                                isTacticEditing = true
                            }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Bearbeiten", tint = accentColor)
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
                                    focusedBorderColor = accentColor,
                                    unfocusedTextColor = TintenSchwarz,
                                    focusedTextColor = TintenSchwarz
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { isTacticEditing = false }) { Text("Abbrechen", color = TintenBraun, fontFamily = Almendra) }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = { viewModel.updateStandardTactic(editTacticText); isTacticEditing = false }, colors = ButtonDefaults.buttonColors(containerColor = accentColor), shape = RoundedCornerShape(8.dp)) { Text("Speichern", fontFamily = Almendra) }
                            }
                        } else {
                            Text(viewModel.standardTactic, style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeaponButton(title: String, isSelected: Boolean, accentColor: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) accentColor else PergamentDunkel,
            contentColor = if (isSelected) (if (accentColor == WaldGold) TintenSchwarz else Color.White) else TintenSchwarz
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(130.dp)
            .height(60.dp)
    ) {
        Text(text = title, fontFamily = Almendra, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 16.sp)
    }
}

@Composable
fun DeathSavesRow(viewModel: CharacterViewModel) {
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showFailureDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.deathSaveSuccesses) {
        if (viewModel.deathSaveSuccesses >= 3) showSuccessDialog = true
    }

    LaunchedEffect(viewModel.deathSaveFailures) {
        if (viewModel.deathSaveFailures >= 3) showFailureDialog = true
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Stabilisiert!", style = MaterialTheme.typography.titleSmall, color = TodRuneGruen) },
            text = { Text("Du hast 3 erfolgreiche Rettungswürfe geschafft. Du bist stabilisiert.", style = MaterialTheme.typography.bodyMedium, color = TintenSchwarz) },
            confirmButton = { Button(onClick = { showSuccessDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Waldgruen), shape = RoundedCornerShape(8.dp)) { Text("Puh!", fontFamily = Almendra) } },
            containerColor = PergamentHell,
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (showFailureDialog) {
        AlertDialog(
            onDismissRequest = { showFailureDialog = false },
            title = { Text("Gefallen...", style = MaterialTheme.typography.titleSmall, color = TodRuneRot) },
            text = { Text("Du hast 3 fehlgeschlagene Rettungswürfe ereilt. Der Charakter ist gestorben...", style = MaterialTheme.typography.bodyMedium, color = TintenSchwarz) },
            confirmButton = { Button(onClick = { showFailureDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot), shape = RoundedCornerShape(8.dp)) { Text("RiP", fontFamily = Almendra) } },
            containerColor = PergamentHell,
            shape = RoundedCornerShape(12.dp)
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Todesrettungswürfe", style = MaterialTheme.typography.labelLarge, color = TintenSchwarz)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            // Erfolge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Erfolge:", style = MaterialTheme.typography.labelMedium, color = TodRuneGruen)
                Spacer(modifier = Modifier.width(4.dp))
                repeat(3) { index ->
                    val checked = index < viewModel.deathSaveSuccesses
                    Icon(
                        imageVector = if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Erfolg",
                        tint = if (checked) TodRuneGruen else EisenGrau,
                        modifier = Modifier.size(24.dp).clickable {
                            if (checked) viewModel.updateDeathSaves(index, viewModel.deathSaveFailures)
                            else viewModel.updateDeathSaves(index + 1, viewModel.deathSaveFailures)
                        }
                    )
                }
            }
            // Fehlschläge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Fehlschläge:", style = MaterialTheme.typography.labelMedium, color = TodRuneRot)
                Spacer(modifier = Modifier.width(4.dp))
                repeat(3) { index ->
                    val checked = index < viewModel.deathSaveFailures
                    Icon(
                        imageVector = if (checked) Icons.Default.Cancel else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Fehlschlag",
                        tint = if (checked) TodRuneRot else EisenGrau,
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