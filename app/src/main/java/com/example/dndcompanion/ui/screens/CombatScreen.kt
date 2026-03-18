package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.draw.shadow
import com.example.dndcompanion.ui.viewmodel.ActiveWeapon
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.viewmodel.CombatViewModel
import com.example.dndcompanion.ui.viewmodel.InventoryViewModel
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.data.CharacterClass
import com.example.dndcompanion.R
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombatScreen(
    viewModel: CharacterViewModel,
    combatVm: CombatViewModel,
    inventoryVm: InventoryViewModel,
    onNavigateToRucksack: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
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
            // Lebenspunkte & Trefferwürfel
            PergamentCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Animierte Progress-Werte
                    Column {
                    val hpProgress by animateFloatAsState(
                        targetValue = if (combatVm.maxHp > 0) combatVm.currentHp.toFloat() / combatVm.maxHp.toFloat() else 0f,
                        animationSpec = tween(durationMillis = 500),
                        label = "HP Animation"
                    )

                    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
                        Text("HP", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TintenBraun, fontFamily = Almendra, modifier = Modifier.padding(bottom = 6.dp, end = 6.dp))
                        Text(
                            "${combatVm.currentHp} / ${combatVm.maxHp}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (combatVm.currentHp > (combatVm.maxHp / 4)) Waldgruen else OchsenblutRot,
                            fontFamily = Almendra
                        )
                        if (combatVm.tempHp > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("+${combatVm.tempHp}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TempHPBlau, fontFamily = Almendra)
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            "HD ${combatVm.hitDice}/${viewModel.level}W${viewModel.characterData.baseHitDice}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TintenBraun,
                            fontFamily = Almendra,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { hpProgress },
                        modifier = Modifier.fillMaxWidth().height(16.dp),
                        color = if (combatVm.currentHp > (combatVm.maxHp / 4)) Waldgruen else OchsenblutRot,
                        trackColor = PergamentHell
                    )

                    val tempHpProgress by animateFloatAsState(
                        targetValue = if (combatVm.tempHp > 0) (combatVm.tempHp.toFloat() / 12f).coerceAtMost(1f) else 0f,
                        animationSpec = tween(durationMillis = 500),
                        label = "Temp HP Animation"
                    )

                    // Temp HP Bar (wenn vorhanden)
                    if (combatVm.tempHp > 0 || tempHpProgress > 0f) {
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { tempHpProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = TempHPBlau,
                            trackColor = PergamentDunkel.copy(alpha = 0.3f)
                        )
                    }

                    if (combatVm.currentHp == 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        DeathSavesRow(combatVm)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // HP-Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = { combatVm.takeDamage(5) }, colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(48.dp), contentPadding = PaddingValues(0.dp)) { Text("-5", fontFamily = Almendra, fontSize = 16.sp) }
                        Button(onClick = { combatVm.takeDamage(1) }, colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(48.dp), contentPadding = PaddingValues(0.dp)) { Text("-1", fontFamily = Almendra, fontSize = 16.sp) }
                        Button(onClick = { combatVm.healManual(1) }, colors = ButtonDefaults.buttonColors(containerColor = Waldgruen), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(48.dp), contentPadding = PaddingValues(0.dp)) { Text("+1", fontFamily = Almendra, fontSize = 16.sp) }
                        Button(onClick = { combatVm.healManual(5) }, colors = ButtonDefaults.buttonColors(containerColor = Waldgruen), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f).height(48.dp), contentPadding = PaddingValues(0.dp)) { Text("+5", fontFamily = Almendra, fontSize = 16.sp) }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = PergamentDunkel)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Temp HP Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Temp HP:", style = MaterialTheme.typography.labelLarge, color = TempHPBlau, modifier = Modifier.weight(1.5f))
                        Button(onClick = { combatVm.modifyTempHp(-1) }, colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot.copy(alpha = 0.6f)), shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f).height(48.dp), contentPadding = PaddingValues(0.dp)) { Text("-1", fontSize = 14.sp) }
                        Button(onClick = { combatVm.modifyTempHp(1) }, colors = ButtonDefaults.buttonColors(containerColor = Bronze.copy(alpha = 0.7f)), shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f).height(48.dp), contentPadding = PaddingValues(0.dp)) { Text("+1", fontSize = 14.sp) }
                        Button(onClick = { combatVm.modifyTempHp(12) }, colors = ButtonDefaults.buttonColors(containerColor = Bronze), shape = RoundedCornerShape(6.dp), modifier = Modifier.weight(1f).height(48.dp), contentPadding = PaddingValues(0.dp)) { Text("+12", fontSize = 14.sp) }
                    }

                    if (inventoryVm.goodberries > 0 || isRanger) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { inventoryVm.eatGoodberry() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            enabled = combatVm.currentHp < combatVm.maxHp,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Waldgruen,
                                disabledContainerColor = EisenGrau
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Beere essen (+1 HP)", fontFamily = Almendra, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("(${inventoryVm.goodberries} übrig)", fontSize = 13.sp)
                        }
                    }
                }
            }

            // RK und EP nebeneinander
            }

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
                            combatVm.currentArmorClass.toString(),
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

            // Passive Stats + Heroische Inspiration
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("Init: ${if(combatVm.initiative >= 0) "+" else ""}${combatVm.initiative}", style = MaterialTheme.typography.labelLarge, color = Waldgruen)
                Text("Tempo: ${viewModel.speed}", style = MaterialTheme.typography.labelLarge, color = Waldgruen)
                Text("Wahrnehmung: ${viewModel.passivePerception}", style = MaterialTheme.typography.labelLarge, color = Waldgruen)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { combatVm.toggleHeroicInspiration(!combatVm.heroicInspiration) }.padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (combatVm.heroicInspiration) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Inspiration",
                        tint = if (combatVm.heroicInspiration) WaldGold else EisenGrau,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Heroische Inspiration", style = MaterialTheme.typography.labelLarge, color = if (combatVm.heroicInspiration) WaldGold else Waldgruen)
                }
            }

            // EP Dialog
            if (showEpDialog) {
                AlertDialog(
                    onDismissRequest = { showEpDialog = false },
                    containerColor = PergamentHell,
                    shape = RoundedCornerShape(12.dp),
                    title = { Text("Erfahrungspunkte", style = MaterialTheme.typography.titleMedium, color = Waldgruen) },
                    text = {
                        Column {
                            Text("Wie viele EP hast du erhalten?", style = MaterialTheme.typography.bodyMedium, color = TintenSchwarz, modifier = Modifier.padding(bottom = 8.dp))
                            OutlinedTextField(
                                value = epInput,
                                onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) epInput = it },
                                label = { Text("EP Betrag", fontFamily = Almendra) },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    focusedLabelColor = accentColor,
                                    unfocusedTextColor = TintenSchwarz,
                                    focusedTextColor = TintenSchwarz
                                )
                            )
                        }
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
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("Hinzufügen", fontFamily = Almendra, fontSize = 16.sp) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEpDialog = false }, modifier = Modifier.height(48.dp)) {
                            Text("Abbrechen", color = Waldgruen, fontFamily = Almendra, fontSize = 16.sp)
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
                    title = { Text("Stufenaufstieg!", style = MaterialTheme.typography.titleMedium, color = accentColor) },
                    text = { Text("Glückwunsch! Deine Erfahrung reicht für Stufe ${viewModel.level}. Möchtest du deinen Charakter jetzt verbessern?", style = MaterialTheme.typography.bodyMedium, color = TintenSchwarz) },
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
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("Jetzt anpassen", fontFamily = Almendra, fontSize = 16.sp) }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissLevelUpNotification() }, modifier = Modifier.height(48.dp)) {
                            Text("Später", color = Waldgruen, fontFamily = Almendra, fontSize = 16.sp)
                        }
                    }
                )
            }

            Text("Waffe ausrüsten", style = MaterialTheme.typography.titleMedium, color = Waldgruen)
            Spacer(modifier = Modifier.height(8.dp))

            var showWeaponEditDialog by remember { mutableStateOf<Int?>(null) }
            if (inventoryVm.availableWeapons.isEmpty()) {
                Text("Keine Waffen im Rucksack gefunden. Füge Waffen im Rucksack-Tab hinzu.", style = MaterialTheme.typography.bodySmall, color = TintenBraun, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, modifier = Modifier.padding(8.dp))
            } else {
                var weaponDropdownExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = weaponDropdownExpanded,
                    onExpandedChange = { weaponDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = combatVm.equippedWeaponName ?: "Keine Waffe ausgerüstet",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ausgerüstete Waffe", fontFamily = Almendra) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = weaponDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = TintenBraun,
                            focusedTextColor = TintenSchwarz,
                            unfocusedTextColor = TintenSchwarz,
                            focusedLabelColor = accentColor,
                            unfocusedLabelColor = TintenBraun
                        ),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = weaponDropdownExpanded,
                        onDismissRequest = { weaponDropdownExpanded = false },
                        modifier = Modifier.background(PergamentHell)
                    ) {
                        inventoryVm.availableWeapons.forEach { weaponName ->
                            DropdownMenuItem(
                                text = { Text(weaponName, fontFamily = Almendra, color = TintenSchwarz) },
                                onClick = {
                                    combatVm.equipWeaponByName(weaponName)
                                    weaponDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Schild-Logik Checkbox
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = combatVm.isShieldEquipped,
                    onCheckedChange = { combatVm.toggleShield(it) },
                    enabled = inventoryVm.hasShieldInInventory,
                    colors = CheckboxDefaults.colors(checkedColor = accentColor)
                )
                Text(
                    "Schild anlegen (+2 RK)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (inventoryVm.hasShieldInInventory) TintenSchwarz else EisenGrau
                )
            }
            if (showWeaponEditDialog != null) {
                val index = showWeaponEditDialog!!
                val currentName = combatVm.getWeaponName(index).replace("\n", " ")

                var newName by remember { mutableStateOf(currentName) }

                AlertDialog(
                    onDismissRequest = { showWeaponEditDialog = null },
                    title = { Text("Waffe umbenennen", fontFamily = Almendra) },
                    text = {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Name") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            combatVm.saveWeaponName(index, newName.replace(" & ", "\n& "))
                            showWeaponEditDialog = null
                        }) { Text("Speichern", fontFamily = Almendra) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showWeaponEditDialog = null }) { Text("Abbrechen", fontFamily = Almendra) }
                    }
                )
            }

            val equippedName = combatVm.equippedWeaponName ?: ""
            val isVersatile = equippedName.contains("Kriegshammer", ignoreCase = true) ||
                              equippedName.contains("Speer", ignoreCase = true) ||
                              equippedName.contains("Shillelagh", ignoreCase = true) ||
                              equippedName.contains("Kampfstab", ignoreCase = true) ||
                              equippedName.contains("Langschwert", ignoreCase = true) ||
                              equippedName.contains("Streitaxt", ignoreCase = true) ||
                              equippedName.contains("Dreizack", ignoreCase = true)

            if (isVersatile) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = combatVm.isUsingTwoHanded,
                        onCheckedChange = { combatVm.toggleTwoHanded(it) },
                        colors = CheckboxDefaults.colors(checkedColor = accentColor)
                    )
                    val hint = if (combatVm.isShieldEquipped) " (Deaktiviert Schild-Bonus)" else ""
                    Text("Zweihändig führen$hint", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                }
            }

            // Magierrüstung Toggle (für Delat/Warlock)
            if (!isRanger) {
                Spacer(modifier = Modifier.height(12.dp))
                PergamentCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = if(combatVm.isMageArmorActive) HexenLila else EisenGrau)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Magierrüstung aktiv", style = MaterialTheme.typography.titleSmall, color = Waldgruen)
                        }
                        Switch(
                            checked = combatVm.isMageArmorActive,
                            onCheckedChange = { combatVm.toggleMageArmor(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = HexenLila, checkedTrackColor = HexenLila.copy(alpha = 0.5f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Anzeige der aktuellen Waffenwerte
            PergamentCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Trefferbonus: ${combatVm.currentAttackBonus}", style = MaterialTheme.typography.titleSmall, color = Waldgruen)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Schaden: ${combatVm.currentDamage}", style = MaterialTheme.typography.bodyMedium, color = TintenSchwarz)

                    Spacer(modifier = Modifier.height(8.dp))

                    val extraNote = when (combatVm.currentWeapon) {
                        ActiveWeapon.LANGBOGEN -> "Verlangsamen (Mastery): Tempo -3m.\nMesserstecher: 1x/Zug 1 Angriffswürfel (Stich) neu werfen. Bei Krit +1 Schadenswürfel."
                        ActiveWeapon.KURZSCHWERT_SCHILD -> "Ärgern (Mastery): Nächster Angriff hat Vorteil.\nMesserstecher: 1x/Zug 1 Angriffswürfel (Stich) neu werfen. Bei Krit +1 Schadenswürfel."
                        ActiveWeapon.SHILLELAGH_SCHILD -> "Umwerfen (Mastery): Ziel muss KON-RW (SG 12) bestehen oder liegt am Boden."
                        ActiveWeapon.KRIEGSHAMMER_PAKT -> "Stoß (Mastery): Ziel bis zu 3m wegstoßen (gerade Linie)."
                        ActiveWeapon.SPEER_PAKT -> "Schwächen (Mastery): Nächster Angriff des Gegners hat Nachteil."
                    }

                    Text(
                        text = extraNote,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = TintenBraun
                    )
                }
            }

            if (combatVm.currentWeapon == ActiveWeapon.LANGBOGEN && isRanger) {
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
                                Text("Verfügbar: ${inventoryVm.totalArrows}", style = MaterialTheme.typography.bodyMedium, color = TintenSchwarz)
                                Text("Verschossen: ${inventoryVm.shotArrows}", style = MaterialTheme.typography.bodyMedium, color = if (inventoryVm.shotArrows > 0) OchsenblutRot else TintenSchwarz)
                            }
                            Button(
                                onClick = { inventoryVm.shootArrow() },
                                enabled = inventoryVm.totalArrows > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot, disabledContainerColor = EisenGrau),
                                modifier = Modifier.height(48.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Schießen", fontFamily = Almendra, fontSize = 16.sp) }
                        }

                        if (inventoryVm.shotArrows > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = PergamentDunkel)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Nach dem Kampf:", style = MaterialTheme.typography.labelLarge, color = Waldgruen)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(onClick = { inventoryVm.recoverArrows() }, colors = ButtonDefaults.buttonColors(containerColor = Bronze), modifier = Modifier.weight(1f).padding(end = 4.dp).height(48.dp), shape = RoundedCornerShape(8.dp)) { Text("½ Einsammeln", fontSize = 16.sp, fontFamily = Almendra) }
                                Button(onClick = { inventoryVm.discardShotArrows() }, colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot.copy(alpha = 0.7f)), modifier = Modifier.weight(1f).padding(start = 4.dp).height(48.dp), shape = RoundedCornerShape(8.dp)) { Text("Alle verloren", fontSize = 16.sp, fontFamily = Almendra) }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = PergamentDunkel)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Pfeile...", style = MaterialTheme.typography.labelLarge, color = Waldgruen)
                            Row {
                                Button(onClick = { inventoryVm.changeTotalArrows(-1) }, colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp), modifier = Modifier.height(48.dp), shape = RoundedCornerShape(6.dp)) { Text("- Ablegen", fontSize = 16.sp, fontFamily = Almendra) }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = { inventoryVm.changeTotalArrows(1) }, colors = ButtonDefaults.buttonColors(containerColor = Bronze), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp), modifier = Modifier.height(48.dp), shape = RoundedCornerShape(6.dp)) { Text("+ Aufnehmen", fontSize = 16.sp, fontFamily = Almendra) }
                            }
                        }
                    }
                }
            }

            // Loot-Button (immer sichtbar, unabhängig von ausgerüsteter Waffe)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateToRucksack,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Bronze),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_geld),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Loot eintragen", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
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

            // Merkmale & Talente
            HorizontalDivider(color = Bronze, thickness = 2.dp, modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Merkmale & Talente", style = MaterialTheme.typography.titleMedium, color = Waldgruen)
                IconButton(onClick = { viewModel.showFeatureSelection = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Merkmal hinzufügen", tint = accentColor)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            var editingTraitIndex by remember { mutableIntStateOf(-1) }
            var editTraitName by remember { mutableStateOf("") }
            var editTraitDesc by remember { mutableStateOf("") }

            viewModel.customTraits.forEachIndexed { index, trait ->
                if (editingTraitIndex == index) {
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
                                    focusedBorderColor = accentColor,
                                    focusedLabelColor = accentColor,
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
                                    focusedBorderColor = accentColor,
                                    focusedLabelColor = accentColor,
                                    unfocusedTextColor = TintenSchwarz,
                                    focusedTextColor = TintenSchwarz
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { editingTraitIndex = -1 }) {
                                    Text("Abbrechen", color = TintenBraun, fontFamily = Almendra)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.updateCustomTrait(index, editTraitName, editTraitDesc)
                                        editingTraitIndex = -1
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                    shape = RoundedCornerShape(8.dp)
                                ) { Text("Speichern", fontFamily = Almendra) }
                            }
                        }
                    }
                } else {
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (viewModel.showFeatureSelection) {
        FeatureSelectionScreen(
            viewModel = viewModel,
            onDismiss = { viewModel.showFeatureSelection = false }
        )
    }
}
