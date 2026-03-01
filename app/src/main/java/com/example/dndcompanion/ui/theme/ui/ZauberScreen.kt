package com.example.dndcompanion.ui.theme.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.theme.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.theme.BlauDunkel
import com.example.dndcompanion.ui.theme.BlauHell
import com.example.dndcompanion.ui.theme.PinkDunkel
import com.example.dndcompanion.ui.theme.GelbSand

@Composable
fun ZauberScreen(viewModel: CharacterViewModel) {
    // Diese Variablen steuern, ob das Popup sichtbar ist und was eingetippt wurde
    var showShortRestDialog by remember { mutableStateOf(false) }
    var rolledDiceInput by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(GelbSand)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
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
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Zeichen des Jägers (Gratis)", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Ohne Zauberplatz", color = Color.White, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${viewModel.huntersMarkFreeUses} / 2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(end = 16.dp))
                        Button(
                            onClick = { viewModel.useHuntersMarkFree() },
                            enabled = viewModel.huntersMarkFreeUses > 0,
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

            // Zauberplätze Grad 1
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
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

            HorizontalDivider(color = BlauDunkel, thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Rast-Buttons
            Text("Rasten (Regeneration)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    // Hier öffnen wir das Popup, anstatt direkt zu heilen
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
    }
}