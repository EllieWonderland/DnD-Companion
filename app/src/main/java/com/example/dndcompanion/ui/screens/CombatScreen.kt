package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.viewmodel.ActiveWeapon
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.theme.BlauDunkel
import com.example.dndcompanion.ui.theme.BlauHell
import com.example.dndcompanion.ui.theme.PinkDunkel
import com.example.dndcompanion.ui.theme.PinkHell
import com.example.dndcompanion.ui.theme.GelbSand

@Composable
fun CombatScreen(viewModel: CharacterViewModel, onNavigateToRucksack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GelbSand)
            .padding(16.dp)
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
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = BlauHell),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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

        // Große Anzeige der Rüstungsklasse
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = BlauDunkel),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Rüstungsklasse (RK)", color = Color.White, fontSize = 16.sp)
                Text(
                    text = viewModel.currentArmorClass.toString(),
                    color = PinkHell,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // EP Hinzufügen (verschoben aus ProfilScreen)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var epInput by remember { mutableStateOf("") }
            OutlinedTextField(
                value = epInput,
                onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) epInput = it },
                label = { Text("EP hinzufügen (Aktuell: ${viewModel.currentEP})") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkDunkel,
                    focusedLabelColor = PinkDunkel
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val amount = epInput.toIntOrNull()
                    if (amount != null && amount > 0) {
                        viewModel.addExperience(amount)
                        epInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel)
            ) {
                Text("+ EP")
            }
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
            Column(modifier = Modifier.padding(16.dp)) {
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
                    fontSize = 12.sp,
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
                Column(modifier = Modifier.padding(16.dp)) {
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
                                Text("½ Einsammeln", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.discardShotArrows() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f)),
                                modifier = Modifier.weight(1f).padding(start = 4.dp)
                            ) {
                                Text("Alle verloren", fontSize = 12.sp)
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
                        Text("Pfeile kaufen/finden:", fontSize = 14.sp, color = BlauDunkel, fontWeight = FontWeight.Bold)
                        Row {
                            Button(
                                onClick = { viewModel.changeTotalArrows(-1) },
                                colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel),
                                modifier = Modifier.width(48.dp)
                            ) { Text("-1") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.changeTotalArrows(1) },
                                colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel),
                                modifier = Modifier.width(48.dp)
                            ) { Text("+1") }
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
        Text(text = title, fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 14.sp)
    }
}