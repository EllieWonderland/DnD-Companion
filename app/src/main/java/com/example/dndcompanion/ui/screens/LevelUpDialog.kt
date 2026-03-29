package com.example.dndcompanion.ui.screens

import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.dndcompanion.R

@Composable
fun LevelUpDialog(viewModel: CharacterViewModel) {
    var currentStep by remember { mutableIntStateOf(1) }
    
    // Step 2 variables
    var choiceOption by remember { mutableStateOf("A") } // A: +2, B: Feat & +1
    
    // For Option A
    var strBonus by remember { mutableIntStateOf(0) }
    var dexBonus by remember { mutableIntStateOf(0) }
    var conBonus by remember { mutableIntStateOf(0) }
    var intBonus by remember { mutableIntStateOf(0) }
    var wisBonus by remember { mutableIntStateOf(0) }
    var chaBonus by remember { mutableIntStateOf(0) }
    
    // For Option B
    var featName by remember { mutableStateOf("") }
    var featDesc by remember { mutableStateOf("") }
    var selectedAttribute by remember { mutableStateOf("STR") }

    // Sync with catalog selection
    LaunchedEffect(viewModel.lastSelectedFeature) {
        viewModel.lastSelectedFeature?.let {
            featName = it.name
            featDesc = it.description
        }
    }

    val totalBonusA = strBonus + dexBonus + conBonus + intBonus + wisBonus + chaBonus

    // The attributes that are changed in the dialog
    val isAsiLevel = viewModel.level in listOf(4, 8, 12, 16, 19)

    // Fanfare abspielen beim Öffnen des Dialogs
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.fanfare)
        mediaPlayer?.start()
        onDispose { mediaPlayer?.release() }
    }

    AlertDialog(
        onDismissRequest = { /* forced interaction */ },
        containerColor = PergamentHell,
        title = {
            Text("Level Up! Stufe ${viewModel.level}", color = Waldgruen, fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                if (currentStep == 1) {
                    Text("Schritt 1: Trefferpunkte (HP)", fontWeight = FontWeight.Bold, color = TintenSchwarz, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${viewModel.characterName} erhält feste Trefferpunkte (${viewModel.hitDie}) plus den Konstitutions-Modifikator (+${viewModel.conMod}).", color = TintenSchwarz)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Deine Max HP steigen um ${viewModel.hitDie + viewModel.conMod}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary, fontSize = 18.sp)
                    Text("Deine Trefferwürfel steigen um 1.", color = TintenSchwarz)
                } 
                else if (currentStep == 2) {
                    Text("Schritt 2: Attributsverbesserung", fontWeight = FontWeight.Bold, color = TintenSchwarz, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Wähle nach den D&D 2024 Regeln deine Belohnung:", color = TintenSchwarz)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = choiceOption == "A", onClick = { choiceOption = "A" }, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.tertiary, unselectedColor = TintenBraun))
                        Text("Option A: +2 Attribute (ASI)", color = TintenSchwarz, fontWeight = FontWeight.Bold)
                    }
                    if (choiceOption == "A") {
                        Text("Verteile genau 2 Punkte:", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                        Text("Verteilt: $totalBonusA / 2", color = if (totalBonusA == 2) TintenSchwarz else Color.Red)
                        
                        AttributeAdjustRow("STR", viewModel.strength, strBonus) { strBonus += it }
                        AttributeAdjustRow("DEX", viewModel.dexterity, dexBonus) { dexBonus += it }
                        AttributeAdjustRow("CON", viewModel.constitution, conBonus) { conBonus += it }
                        AttributeAdjustRow("INT", viewModel.intelligence, intBonus) { intBonus += it }
                        AttributeAdjustRow("WIS", viewModel.wisdom, wisBonus) { wisBonus += it }
                        AttributeAdjustRow("CHA", viewModel.charisma, chaBonus) { chaBonus += it }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = choiceOption == "B", onClick = { choiceOption = "B" }, colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.tertiary, unselectedColor = TintenBraun))
                        Text("Option B: Neues Talent + 1 Attribut", color = TintenSchwarz, fontWeight = FontWeight.Bold)
                    }
                    if (choiceOption == "B") {
                        OutlinedTextField(
                            value = featName,
                            onValueChange = { featName = it },
                            label = { Text("Name des Talents", color = TintenBraun) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary, focusedLabelColor = MaterialTheme.colorScheme.tertiary, focusedTextColor = TintenSchwarz, unfocusedTextColor = TintenSchwarz),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = featDesc,
                            onValueChange = { featDesc = it },
                            label = { Text("Beschreibung / Effekt", color = TintenBraun) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary, focusedLabelColor = MaterialTheme.colorScheme.tertiary, focusedTextColor = TintenSchwarz, unfocusedTextColor = TintenSchwarz),
                            modifier = Modifier.fillMaxWidth().height(100.dp)
                        )
                        Button(
                            onClick = { viewModel.showFeatureSelection = true },
                            colors = MetallButtonColors(),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Text("Aus Katalog wählen", fontFamily = Almendra)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Wähle ein Attribut (+1):", color = TintenSchwarz, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            listOf("STR", "DEX", "CON").forEach { attr ->
                                FilterChip(
                                    selected = selectedAttribute == attr,
                                    onClick = { selectedAttribute = attr },
                                    label = { Text(attr) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Waldgruen, selectedLabelColor = PergamentHell)
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            listOf("INT", "WIS", "CHA").forEach { attr ->
                                FilterChip(
                                    selected = selectedAttribute == attr,
                                    onClick = { selectedAttribute = attr },
                                    label = { Text(attr) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Waldgruen, selectedLabelColor = PergamentHell)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentStep == 1) {
                        viewModel.applyHpIncrease(viewModel.conMod, viewModel.hitDie)
                        if (isAsiLevel) {
                            currentStep = 2
                        } else {
                            viewModel.dismissLevelUpDialog()
                        }
                    } else if (currentStep == 2) {
                        if (choiceOption == "A") {
                            if (totalBonusA == 2) {
                                viewModel.updateAttributes(strBonus, dexBonus, conBonus, intBonus, wisBonus, chaBonus)
                                viewModel.dismissLevelUpDialog()
                            }
                        } else {
                            if (featName.isNotBlank()) {
                                val strMod = if (selectedAttribute == "STR") 1 else 0
                                val dexMod = if (selectedAttribute == "DEX") 1 else 0
                                val conMod = if (selectedAttribute == "CON") 1 else 0
                                val intMod = if (selectedAttribute == "INT") 1 else 0
                                val wisMod = if (selectedAttribute == "WIS") 1 else 0
                                val chaMod = if (selectedAttribute == "CHA") 1 else 0
                                viewModel.updateAttributes(strMod, dexMod, conMod, intMod, wisMod, chaMod)
                                viewModel.addCustomTrait(featName, featDesc)
                                viewModel.dismissLevelUpDialog()
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = PergamentHell),
                enabled = if (currentStep == 2 && choiceOption == "A") totalBonusA == 2 else if (currentStep == 2 && choiceOption == "B") featName.isNotBlank() else true
            ) {
                Text(if (currentStep == 1 && isAsiLevel) "Weiter" else "Abschließen", color = PergamentHell, fontFamily = Almendra)
            }
        }
    )

    if (viewModel.showFeatureSelection) {
        FeaturePickerDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.dismissFeatureSelection() }
        )
    }
}

@Composable
fun AttributeAdjustRow(name: String, baseVal: Int, bonus: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$name: ${baseVal + bonus}", color = TintenSchwarz, fontWeight = FontWeight.Bold)
        Row {
            Button(onClick = { onChange(-1) }, enabled = bonus > 0, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = PergamentHell), contentPadding = PaddingValues(0.dp), modifier = Modifier.size(48.dp)) { Text("-", color = PergamentHell, fontSize = 20.sp) }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onChange(1) }, colors = ButtonDefaults.buttonColors(containerColor = Waldgruen, contentColor = PergamentHell), contentPadding = PaddingValues(0.dp), modifier = Modifier.size(48.dp)) { Text("+", color = PergamentHell, fontSize = 20.sp) }
        }
    }
}
