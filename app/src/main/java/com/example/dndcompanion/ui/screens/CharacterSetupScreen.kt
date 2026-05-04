package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dndcompanion.data.CharacterClass
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.viewmodel.InventoryItem
import kotlin.random.Random

private val STEP_TITLES = listOf(
    "Identität",
    "Attribute",
    "Kampfwerte",
    "Startausrüstung",
    "Hintergrund"
)

private fun roll4d6DropLowest(): Int {
    val rolls = (1..4).map { Random.nextInt(1, 7) }
    return rolls.sum() - rolls.min()
}

private fun mod(value: Int): String {
    val m = (value - 10) / 2
    return if (m >= 0) "+$m" else "$m"
}

private fun starterEquipmentFor(charClass: CharacterClass): List<InventoryItem> = when (charClass) {
    CharacterClass.RANGER -> listOf(
        InventoryItem("Langbogen", 1, 1.0, "Rüstung & Waffen"),
        InventoryItem("Köcher (20 Pfeile)", 1, 0.5, "Rüstung & Waffen"),
        InventoryItem("Kurzschwert", 1, 1.0, "Rüstung & Waffen"),
        InventoryItem("Lederrüstung", 1, 5.0, "Rüstung & Waffen"),
        InventoryItem("Reiseausrüstung (Standard)", 1, 2.0, "Ausrüstung"),
        InventoryItem("Wasserschlauch", 1, 2.5, "Ausrüstung"),
        InventoryItem("Tagesrationen (5×)", 5, 0.5, "Ausrüstung")
    )
    CharacterClass.WARLOCK -> listOf(
        InventoryItem("Arkaner Fokus (Kristall)", 1, 0.5, "Ausrüstung"),
        InventoryItem("Leichte Rüstung", 1, 4.5, "Rüstung & Waffen"),
        InventoryItem("Dolch", 1, 0.5, "Rüstung & Waffen"),
        InventoryItem("Reiseausrüstung (Standard)", 1, 2.0, "Ausrüstung"),
        InventoryItem("Wasserschlauch", 1, 2.5, "Ausrüstung"),
        InventoryItem("Tagesrationen (5×)", 5, 0.5, "Ausrüstung")
    )
}

@Composable
fun CharacterSetupScreen(
    uid: String,
    viewModel: CharacterViewModel,
    onSetupComplete: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }

    // Step 0 – Identität
    var name by remember { mutableStateOf("") }
    var race by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf(CharacterClass.RANGER) }
    var subclass by remember { mutableStateOf("") }
    var level by remember { mutableIntStateOf(3) }

    // Step 1 – Attribute
    var str by remember { mutableIntStateOf(10) }
    var dex by remember { mutableIntStateOf(10) }
    var con by remember { mutableIntStateOf(10) }
    var int by remember { mutableIntStateOf(10) }
    var wis by remember { mutableIntStateOf(10) }
    var cha by remember { mutableIntStateOf(10) }

    // Step 2 – Kampfwerte
    var maxHpInput by remember { mutableStateOf("10") }
    var hitDiceVal by remember { mutableIntStateOf(10) }
    var acInput by remember { mutableStateOf("10") }

    // Step 3 – Ausrüstung
    val equipmentOptions = remember(selectedClass) { starterEquipmentFor(selectedClass) }
    val selectedEquipment = remember(selectedClass) { mutableStateListOf(*equipmentOptions.map { it.name }.toTypedArray()) }
    val customEquipmentItems = remember { mutableStateListOf<InventoryItem>() }

    // Step 4 – Hintergrund
    var background by remember { mutableStateOf("") }

    val canAdvance = when (step) {
        0 -> name.isNotBlank()
        else -> true
    }

    PergamentBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Text(
                "Charakter erstellen",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = Almendra,
                color = Waldgruen,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (step + 1f) / STEP_TITLES.size },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = PergamentDunkel
            )
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    STEP_TITLES[step],
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${step + 1} / ${STEP_TITLES.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TintenBraun
                )
            }

            Spacer(Modifier.height(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                when (step) {
                    0 -> StepIdentity(
                        name = name, race = race,
                        selectedClass = selectedClass, subclass = subclass,
                        level = level,
                        onNameChange = { name = it }, onRaceChange = { race = it },
                        onClassChange = { selectedClass = it }, onSubclassChange = { subclass = it },
                        onLevelChange = { level = it }
                    )
                    1 -> StepAttributes(
                        str = str, dex = dex, con = con,
                        int = int, wis = wis, cha = cha,
                        onRollAll = {
                            str = roll4d6DropLowest()
                            dex = roll4d6DropLowest()
                            con = roll4d6DropLowest()
                            int = roll4d6DropLowest()
                            wis = roll4d6DropLowest()
                            cha = roll4d6DropLowest()
                        },
                        onStrChange = { str = it }, onDexChange = { dex = it },
                        onConChange = { con = it }, onIntChange = { int = it },
                        onWisChange = { wis = it }, onChaChange = { cha = it }
                    )
                    2 -> StepCombatStats(
                        maxHpInput = maxHpInput, hitDiceVal = hitDiceVal, acInput = acInput,
                        con = con, selectedClass = selectedClass,
                        onMaxHpChange = { maxHpInput = it },
                        onHitDiceChange = { hitDiceVal = it },
                        onAcChange = { acInput = it }
                    )
                    3 -> StepEquipment(
                        options = equipmentOptions,
                        selected = selectedEquipment,
                        customItems = customEquipmentItems,
                        onToggle = { itemName ->
                            if (selectedEquipment.contains(itemName)) selectedEquipment.remove(itemName)
                            else selectedEquipment.add(itemName)
                        },
                        onAddCustomItem = { customEquipmentItems.add(it) },
                        onRemoveCustomItem = { customEquipmentItems.removeAt(it) }
                    )
                    4 -> StepBackground(
                        background = background,
                        onBackgroundChange = { background = it }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    OutlinedButton(
                        onClick = { step-- },
                        border = BorderStroke(1.dp, TintenBraun),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Zurück", fontFamily = Almendra, color = TintenBraun)
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                if (step < STEP_TITLES.size - 1) {
                    Button(
                        onClick = { step++ },
                        enabled = canAdvance,
                        colors = ButtonDefaults.buttonColors(containerColor = Waldgruen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Weiter", fontFamily = Almendra)
                    }
                } else {
                    Button(
                        onClick = {
                            val baseItems = equipmentOptions.filter { it.name in selectedEquipment }
                            val allItems = baseItems + customEquipmentItems
                            val maxHp = maxHpInput.toIntOrNull() ?: 10
                            val ac = acInput.toIntOrNull() ?: 10
                            viewModel.saveCharacterFromSetup(
                                uid = uid, name = name.trim(),
                                race = race.trim(), charClass = selectedClass,
                                subclass = subclass.trim(),
                                str = str, dex = dex, con = con, int = int, wis = wis, cha = cha,
                                maxHpVal = maxHp, hitDiceVal = hitDiceVal,
                                levelVal = level,
                                background = background.trim(), starterItems = allItems
                            )
                            viewModel.markSetupComplete(uid)
                            onSetupComplete()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Waldgruen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Fertigstellen", fontFamily = Almendra)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIdentity(
    name: String, race: String, selectedClass: CharacterClass, subclass: String,
    level: Int,
    onNameChange: (String) -> Unit, onRaceChange: (String) -> Unit,
    onClassChange: (CharacterClass) -> Unit, onSubclassChange: (String) -> Unit,
    onLevelChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SetupTextField("Name *", name, onNameChange, placeholder = "z.B. Lyraniel")
        SetupTextField("Rasse", race, onRaceChange, placeholder = "z.B. Halbelfe, Mensch …")

        Text("Klasse", style = MaterialTheme.typography.labelLarge, color = TintenBraun, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ClassCard(
                label = "Waldläufer",
                sublabel = "Ranger",
                isSelected = selectedClass == CharacterClass.RANGER,
                modifier = Modifier.weight(1f),
                onClick = { onClassChange(CharacterClass.RANGER) }
            )
            ClassCard(
                label = "Hexenmeister",
                sublabel = "Warlock",
                isSelected = selectedClass == CharacterClass.WARLOCK,
                modifier = Modifier.weight(1f),
                onClick = { onClassChange(CharacterClass.WARLOCK) }
            )
        }

        SetupTextField("Unterklasse / Archetype", subclass, onSubclassChange, placeholder = "z.B. Tierherr, Großer Alte …")

        Text("Stufe (1–20)", style = MaterialTheme.typography.labelLarge, color = TintenBraun, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(
                onClick = { onLevelChange((level - 1).coerceAtLeast(1)) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "-", tint = TintenBraun)
            }
            Text(
                "$level",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
            IconButton(
                onClick = { onLevelChange((level + 1).coerceAtMost(20)) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "+", tint = TintenBraun)
            }
        }
    }
}

@Composable
private fun ClassCard(label: String, sublabel: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.tertiary else TintenBraun.copy(alpha = 0.4f)
    val bgColor = if (isSelected) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f) else PergamentHell
    Card(
        modifier = modifier
            .border(BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.tertiary else TintenSchwarz)
            Text(sublabel, style = MaterialTheme.typography.labelSmall, color = TintenBraun)
        }
    }
}

@Composable
private fun StepAttributes(
    str: Int, dex: Int, con: Int, int: Int, wis: Int, cha: Int,
    onRollAll: () -> Unit,
    onStrChange: (Int) -> Unit, onDexChange: (Int) -> Unit, onConChange: (Int) -> Unit,
    onIntChange: (Int) -> Unit, onWisChange: (Int) -> Unit, onChaChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Attribute bestimmen",
            style = MaterialTheme.typography.bodyMedium,
            color = TintenBraun
        )
        Button(
            onClick = onRollAll,
            colors = ButtonDefaults.buttonColors(containerColor = Bronze),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Alle würfeln (4W6, niedrigster fällt weg)", fontFamily = Almendra)
        }

        PergamentCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    AttrSpinner("STR", str, onStrChange)
                    AttrSpinner("DEX", dex, onDexChange)
                    AttrSpinner("CON", con, onConChange)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    AttrSpinner("INT", int, onIntChange)
                    AttrSpinner("WIS", wis, onWisChange)
                    AttrSpinner("CHA", cha, onChaChange)
                }
            }
        }
    }
}

@Composable
private fun AttrSpinner(label: String, value: Int, onChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TintenBraun, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onChange((value - 1).coerceAtLeast(1)) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "-", tint = TintenBraun, modifier = Modifier.size(16.dp))
            }
            Text(
                "$value",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = TintenSchwarz,
                modifier = Modifier.widthIn(min = 28.dp),
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = { onChange((value + 1).coerceAtMost(20)) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "+", tint = TintenBraun, modifier = Modifier.size(16.dp))
            }
        }
        Text(mod(value), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StepCombatStats(
    maxHpInput: String, hitDiceVal: Int, acInput: String,
    con: Int, selectedClass: CharacterClass,
    onMaxHpChange: (String) -> Unit, onHitDiceChange: (Int) -> Unit, onAcChange: (String) -> Unit
) {
    val suggestedHitDice = if (selectedClass == CharacterClass.RANGER) 10 else 8
    val conMod = (con - 10) / 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        PergamentCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Trefferwürfel (Hit Die)", style = MaterialTheme.typography.labelLarge, color = TintenBraun, fontWeight = FontWeight.Bold)
                Text("Empfehlung für ${if (selectedClass == CharacterClass.RANGER) "Waldläufer" else "Hexenmeister"}: W$suggestedHitDice", style = MaterialTheme.typography.labelSmall, color = TintenBraun)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(6, 8, 10, 12).forEach { die ->
                        val isSelected = hitDiceVal == die
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.tertiary else TintenBraun.copy(alpha = 0.4f)), RoundedCornerShape(6.dp))
                                .clickable { onHitDiceChange(die) },
                            color = if (isSelected) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f) else PergamentHell
                        ) {
                            Text(
                                "W$die",
                                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.tertiary else TintenSchwarz
                            )
                        }
                    }
                }
            }
        }

        PergamentCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Maximale Trefferpunkte", style = MaterialTheme.typography.labelLarge, color = TintenBraun, fontWeight = FontWeight.Bold)
                Text("Tipp: W$suggestedHitDice + KON-Mod (${if (conMod >= 0) "+$conMod" else "$conMod"}) = ${suggestedHitDice + conMod}", style = MaterialTheme.typography.labelSmall, color = TintenBraun)
                SetupTextField("Max HP", maxHpInput, onMaxHpChange, isNumber = true)
            }
        }

        PergamentCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Rüstungsklasse (RK)", style = MaterialTheme.typography.labelLarge, color = TintenBraun, fontWeight = FontWeight.Bold)
                Text("Lederrüstung = 11+DEX-Mod, Kettenpanzer = 16", style = MaterialTheme.typography.labelSmall, color = TintenBraun)
                SetupTextField("Rüstungsklasse", acInput, onAcChange, isNumber = true)
            }
        }
    }
}

@Composable
private fun StepEquipment(
    options: List<InventoryItem>,
    selected: List<String>,
    customItems: List<InventoryItem>,
    onToggle: (String) -> Unit,
    onAddCustomItem: (InventoryItem) -> Unit,
    onRemoveCustomItem: (Int) -> Unit
) {
    var customInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("Startausrüstung", style = MaterialTheme.typography.bodyMedium, color = TintenBraun)
        Text("Wähle aus, was du zu Beginn mitbekommst.", style = MaterialTheme.typography.labelSmall, color = TintenBraun)
        Spacer(Modifier.height(4.dp))
        options.forEach { item ->
            val isChecked = selected.contains(item.name)
            PergamentCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(item.name) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { onToggle(item.name) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Waldgruen,
                            uncheckedColor = TintenBraun
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(item.name, style = MaterialTheme.typography.bodyMedium, color = TintenSchwarz, fontWeight = FontWeight.Medium)
                        Text("${item.weight} kg · ${item.category}", style = MaterialTheme.typography.labelSmall, color = TintenBraun)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Eigene Gegenstände", style = MaterialTheme.typography.labelLarge, color = TintenBraun, fontWeight = FontWeight.Bold)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = customInput,
                onValueChange = { customInput = it },
                label = { Text("Gegenstand / Freitext", style = MaterialTheme.typography.labelSmall) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Waldgruen,
                    unfocusedBorderColor = TintenBraun,
                    focusedLabelColor = Waldgruen,
                    unfocusedLabelColor = TintenBraun,
                    focusedTextColor = TintenSchwarz,
                    unfocusedTextColor = TintenSchwarz,
                    cursorColor = Waldgruen,
                    unfocusedContainerColor = PergamentHell,
                    focusedContainerColor = PergamentHell
                )
            )
            IconButton(
                onClick = {
                    val trimmed = customInput.trim()
                    if (trimmed.isNotBlank()) {
                        onAddCustomItem(InventoryItem(name = trimmed, amount = 1, category = "Ausrüstung"))
                        customInput = ""
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Hinzufügen", tint = Waldgruen)
            }
        }

        customItems.forEachIndexed { index, item ->
            PergamentCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.name, style = MaterialTheme.typography.bodyMedium, color = TintenSchwarz, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onRemoveCustomItem(index) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Entfernen", tint = OchsenblutRot, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StepBackground(
    background: String,
    onBackgroundChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Erzähl kurz, wer dein Charakter ist – woher er kommt, was ihn antreibt. (optional)",
            style = MaterialTheme.typography.bodyMedium,
            color = TintenBraun
        )
        OutlinedTextField(
            value = background,
            onValueChange = onBackgroundChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp),
            label = { Text("Hintergrundgeschichte & Notizen", style = MaterialTheme.typography.labelSmall) },
            placeholder = { Text("Lyraniel wuchs in den Wäldern von Felmoor auf …", style = MaterialTheme.typography.labelSmall, color = TintenBraun.copy(alpha = 0.5f)) },
            maxLines = 10,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Waldgruen,
                unfocusedBorderColor = TintenBraun,
                focusedLabelColor = Waldgruen,
                unfocusedLabelColor = TintenBraun,
                focusedTextColor = TintenSchwarz,
                unfocusedTextColor = TintenSchwarz,
                cursorColor = Waldgruen,
                unfocusedContainerColor = PergamentHell,
                focusedContainerColor = PergamentHell
            )
        )
    }
}

@Composable
private fun SetupTextField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String = "",
    isNumber: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        placeholder = { if (placeholder.isNotBlank()) Text(placeholder, style = MaterialTheme.typography.labelSmall, color = TintenBraun.copy(alpha = 0.5f)) },
        singleLine = true,
        keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Waldgruen,
            unfocusedBorderColor = TintenBraun,
            focusedLabelColor = Waldgruen,
            unfocusedLabelColor = TintenBraun,
            focusedTextColor = TintenSchwarz,
            unfocusedTextColor = TintenSchwarz,
            cursorColor = Waldgruen,
            unfocusedContainerColor = PergamentHell,
            focusedContainerColor = PergamentHell
        )
    )
}
