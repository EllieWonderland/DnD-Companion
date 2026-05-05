package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel

@Composable
fun CharacterEditDialog(viewModel: CharacterViewModel) {
    val data = viewModel.characterData

    var nameInput by remember(data.id) { mutableStateOf(data.name) }
    var raceInput by remember(data.id) { mutableStateOf(data.race) }
    var subclassInput by remember(data.id) { mutableStateOf(data.subclass ?: "") }
    var alignmentInput by remember(data.id) { mutableStateOf(data.alignment) }
    var backgroundInput by remember(data.id) { mutableStateOf(data.background) }
    var appearanceInput by remember(data.id) { mutableStateOf(data.appearance ?: "") }
    var languagesInput by remember(data.id) { mutableStateOf(data.languages ?: "") }
    var idealInput by remember(data.id) { mutableStateOf(data.ideal ?: "") }
    var flawInput by remember(data.id) { mutableStateOf(data.flaw ?: "") }
    var strInput by remember(data.id) { mutableStateOf(viewModel.strength.toString()) }
    var dexInput by remember(data.id) { mutableStateOf(viewModel.dexterity.toString()) }
    var conInput by remember(data.id) { mutableStateOf(viewModel.constitution.toString()) }
    var intInput by remember(data.id) { mutableStateOf(viewModel.intelligence.toString()) }
    var wisInput by remember(data.id) { mutableStateOf(viewModel.wisdom.toString()) }
    var chaInput by remember(data.id) { mutableStateOf(viewModel.charisma.toString()) }
    var levelInput by remember(data.id) { mutableStateOf(viewModel.level.toString()) }
    var epInput by remember(data.id) { mutableStateOf(viewModel.currentEP.toString()) }
    var maxHpInput by remember(data.id) { mutableStateOf(viewModel.maxHp.toString()) }
    var hitDiceInput by remember(data.id) { mutableStateOf(viewModel.hitDice.toString()) }
    var speedInput by remember(data.id) { mutableStateOf(data.speed.toString()) }
    var slots1Input by remember(data.id) { mutableStateOf(data.baseSpellSlotsLevel1.toString()) }
    var slots2Input by remember(data.id) { mutableStateOf(data.baseSpellSlotsLevel2.toString()) }
    var slots3Input by remember(data.id) { mutableStateOf(data.baseSpellSlotsLevel3.toString()) }

    Dialog(onDismissRequest = { viewModel.closeCharacterEdit() }) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = PergamentHell,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "${data.name} bearbeiten",
                    style = MaterialTheme.typography.titleLarge,
                    color = Waldgruen,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                CharEditSection("Charakter")
                CharEditTextField("Name", nameInput, Modifier.fillMaxWidth()) { nameInput = it }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CharEditTextField("Volk / Rasse", raceInput, Modifier.weight(1f)) { raceInput = it }
                    CharEditTextField("Gesinnung", alignmentInput, Modifier.weight(1f)) { alignmentInput = it }
                }
                Spacer(Modifier.height(8.dp))
                CharEditTextField("Unterklasse", subclassInput, Modifier.fillMaxWidth()) { subclassInput = it }

                Spacer(Modifier.height(12.dp))
                CharEditSection("Hintergrund & Persönlichkeit")
                CharEditTextField("Hintergrund", backgroundInput, Modifier.fillMaxWidth()) { backgroundInput = it }
                Spacer(Modifier.height(8.dp))
                CharEditTextField("Aussehen", appearanceInput, Modifier.fillMaxWidth(), maxLines = 3) { appearanceInput = it }
                Spacer(Modifier.height(8.dp))
                CharEditTextField("Sprachen", languagesInput, Modifier.fillMaxWidth()) { languagesInput = it }
                Spacer(Modifier.height(8.dp))
                CharEditTextField("Ideal", idealInput, Modifier.fillMaxWidth(), maxLines = 2) { idealInput = it }
                Spacer(Modifier.height(8.dp))
                CharEditTextField("Makel", flawInput, Modifier.fillMaxWidth(), maxLines = 2) { flawInput = it }

                Spacer(Modifier.height(12.dp))
                CharEditSection("Stufe & Erfahrung")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CharEditField("Stufe", levelInput, Modifier.weight(1f)) { levelInput = it }
                    CharEditField("EP", epInput, Modifier.weight(2f)) { epInput = it }
                }

                Spacer(Modifier.height(12.dp))

                CharEditSection("Attribute")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CharEditField("STR", strInput, Modifier.weight(1f)) { strInput = it }
                    CharEditField("DEX", dexInput, Modifier.weight(1f)) { dexInput = it }
                    CharEditField("CON", conInput, Modifier.weight(1f)) { conInput = it }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CharEditField("INT", intInput, Modifier.weight(1f)) { intInput = it }
                    CharEditField("WIS", wisInput, Modifier.weight(1f)) { wisInput = it }
                    CharEditField("CHA", chaInput, Modifier.weight(1f)) { chaInput = it }
                }

                Spacer(Modifier.height(12.dp))

                CharEditSection("Trefferpunkte")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CharEditField("Max HP", maxHpInput, Modifier.weight(1f)) { maxHpInput = it }
                    CharEditField("Trefferwürfel", hitDiceInput, Modifier.weight(1f)) { hitDiceInput = it }
                }

                Spacer(Modifier.height(12.dp))

                CharEditSection("Bewegung")
                CharEditField("Geschwindigkeit (m)", speedInput, Modifier.fillMaxWidth()) { speedInput = it }

                Spacer(Modifier.height(12.dp))

                CharEditSection("Zauberplätze (Basis / Tag)")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CharEditField("St. 1", slots1Input, Modifier.weight(1f)) { slots1Input = it }
                    CharEditField("St. 2", slots2Input, Modifier.weight(1f)) { slots2Input = it }
                    CharEditField("St. 3", slots3Input, Modifier.weight(1f)) { slots3Input = it }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { viewModel.closeCharacterEdit() }) {
                        Text("Abbrechen", color = TintenBraun, fontFamily = Almendra)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val updated = data.copy(
                                name = nameInput.trim().ifBlank { data.name },
                                race = raceInput.trim().ifBlank { data.race },
                                subclass = subclassInput.trim(),
                                alignment = alignmentInput.trim().ifBlank { data.alignment },
                                background = backgroundInput.trim().ifBlank { data.background },
                                appearance = appearanceInput.trim(),
                                languages = languagesInput.trim(),
                                ideal = idealInput.trim(),
                                flaw = flawInput.trim(),
                                baseStrength = strInput.toIntOrNull() ?: data.baseStrength,
                                baseDexterity = dexInput.toIntOrNull() ?: data.baseDexterity,
                                baseConstitution = conInput.toIntOrNull() ?: data.baseConstitution,
                                baseIntelligence = intInput.toIntOrNull() ?: data.baseIntelligence,
                                baseWisdom = wisInput.toIntOrNull() ?: data.baseWisdom,
                                baseCharisma = chaInput.toIntOrNull() ?: data.baseCharisma,
                                baseLevel = levelInput.toIntOrNull() ?: data.baseLevel,
                                baseEP = epInput.toIntOrNull() ?: data.baseEP,
                                baseMaxHp = maxHpInput.toIntOrNull() ?: data.baseMaxHp,
                                baseHitDice = hitDiceInput.toIntOrNull() ?: data.baseHitDice,
                                speed = speedInput.toIntOrNull() ?: data.speed,
                                baseSpellSlotsLevel1 = slots1Input.toIntOrNull() ?: data.baseSpellSlotsLevel1,
                                baseSpellSlotsLevel2 = slots2Input.toIntOrNull() ?: data.baseSpellSlotsLevel2,
                                baseSpellSlotsLevel3 = slots3Input.toIntOrNull() ?: data.baseSpellSlotsLevel3,
                            )
                            viewModel.saveCharacterData(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Waldgruen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Speichern", fontFamily = Almendra)
                    }
                }
            }
        }
    }
}

@Composable
private fun CharEditTextField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine = maxLines == 1,
        maxLines = maxLines,
        modifier = if (maxLines > 1) modifier.heightIn(min = 56.dp) else modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Waldgruen,
            unfocusedBorderColor = TintenBraun,
            focusedLabelColor = Waldgruen,
            unfocusedLabelColor = TintenBraun,
            focusedTextColor = TintenSchwarz,
            unfocusedTextColor = TintenSchwarz,
            cursorColor = Waldgruen
        )
    )
}

@Composable
private fun CharEditSection(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.tertiary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun CharEditField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= 6) onValueChange(it) },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Waldgruen,
            unfocusedBorderColor = TintenBraun,
            focusedLabelColor = Waldgruen,
            unfocusedLabelColor = TintenBraun,
            focusedTextColor = TintenSchwarz,
            unfocusedTextColor = TintenSchwarz,
            cursorColor = Waldgruen
        )
    )
}
