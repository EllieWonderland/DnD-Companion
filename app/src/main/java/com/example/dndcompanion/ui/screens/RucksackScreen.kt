package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.theme.BlauDunkel
import com.example.dndcompanion.ui.theme.BlauHell
import com.example.dndcompanion.ui.theme.PinkDunkel
import com.example.dndcompanion.ui.theme.GelbSand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RucksackScreen(viewModel: CharacterViewModel) {
    var newItemName by remember { mutableStateOf("") }
    var newItemWeight by remember { mutableStateOf("") }
    var newItemCategory by remember { mutableStateOf("Sonstiges") }
    var isMoneyBagExpanded by remember { mutableStateOf(false) }
    var showEquipmentPicker by remember { mutableStateOf(false) }

    var showGroupLoot by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GelbSand)
    ) {
        TabRow(
            selectedTabIndex = if (showGroupLoot) 1 else 0,
            containerColor = BlauHell,
            contentColor = Color.White
        ) {
            Tab(
                selected = !showGroupLoot,
                onClick = { showGroupLoot = false },
                text = { Text("Persönlich") },
                selectedContentColor = GelbSand,
                unselectedContentColor = Color.White
            )
            Tab(
                selected = showGroupLoot,
                onClick = { showGroupLoot = true },
                text = { Text("Gruppen-Loot") },
                selectedContentColor = GelbSand,
                unselectedContentColor = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (!showGroupLoot) {
        // --- GEWICHT ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Traglast", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
            val weightColor = if (viewModel.currentWeight > viewModel.maxWeight) Color.Red else BlauDunkel
            Text(String.format(java.util.Locale.US, "%.1f / %.0f Pfd.", viewModel.currentWeight, viewModel.maxWeight), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = weightColor)
        }

        // --- GEWICHTS-BALKEN ---
        val weightRatio = (viewModel.currentWeight / viewModel.maxWeight).toFloat().coerceIn(0f, 1f)
        val barColor = when {
            weightRatio > 0.9f -> Color.Red
            weightRatio > 0.7f -> Color(0xFFFF9800) // Orange
            else -> BlauDunkel
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(BlauHell.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(weightRatio)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // --- FESTER RUCKSACK NACH OBEN ---
        Text("Fester Rucksack", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
        Spacer(modifier = Modifier.height(8.dp))

        InventoryRow(
            name = "Wasserschlauch (Tage)",
            amount = viewModel.water.toString(),
            onMinus = { viewModel.changeWater(-0.5f) },
            onPlus = { viewModel.changeWater(0.5f) }
        )
        InventoryRow(
            name = "Tagesrationen",
            amount = viewModel.rations.toString(),
            onMinus = { viewModel.changeRations(-1) },
            onPlus = { viewModel.changeRations(1) }
        )
        
        // Gute Beeren mit Extra-Button für +10
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = BlauHell),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Gute Beeren", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Button(
                        onClick = { viewModel.castGoodberry() },
                        enabled = viewModel.spellSlotsLevel1 > 0 && viewModel.allSpells.any { it.name == "Gute Beere" && it.isPrepared },
                        colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel),
                        modifier = Modifier.height(36.dp).padding(top = 4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("Zaubern (+10)", fontSize = 14.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.changeGoodberries(-1) }) {
                        Icon(Icons.Default.Remove, contentDescription = "Weniger", tint = PinkDunkel)
                    }
                    Text(text = viewModel.goodberries.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                    IconButton(onClick = { viewModel.changeGoodberries(1) }) {
                        Icon(Icons.Default.Add, contentDescription = "Mehr", tint = BlauDunkel)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- GELDBEUTEL (Einklappbar) ---
        Row(
            modifier = Modifier.fillMaxWidth().clickable { isMoneyBagExpanded = !isMoneyBagExpanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Geldbeutel", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
            if (!isMoneyBagExpanded) {
                Text(
                    "${viewModel.coinsKM}KM | ${viewModel.coinsSM}SM | ${viewModel.coinsEM}EM | ${viewModel.coinsGM}GM | ${viewModel.coinsPM}PM",
                    fontSize = 14.sp,
                    color = PinkDunkel,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text("Einklappen ▲", fontSize = 14.sp, color = BlauDunkel)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (isMoneyBagExpanded) {
            CoinRow("Kupfer (KM)", viewModel.coinsKM.toString(), onMinus = { viewModel.changeCoinsKM(-it) }, onPlus = { viewModel.changeCoinsKM(it) })
            CoinRow("Silber (SM)", viewModel.coinsSM.toString(), onMinus = { viewModel.changeCoinsSM(-it) }, onPlus = { viewModel.changeCoinsSM(it) })
            CoinRow("Elektrum (EM)", viewModel.coinsEM.toString(), onMinus = { viewModel.changeCoinsEM(-it) }, onPlus = { viewModel.changeCoinsEM(it) })
            CoinRow("Gold (GM)", viewModel.coinsGM.toString(), onMinus = { viewModel.changeCoinsGM(-it) }, onPlus = { viewModel.changeCoinsGM(it) })
            CoinRow("Platin (PM)", viewModel.coinsPM.toString(), onMinus = { viewModel.changeCoinsPM(-it) }, onPlus = { viewModel.changeCoinsPM(it) })
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Flexibler Loot", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
        Spacer(modifier = Modifier.height(8.dp))

        // Neues Item hinzufügen
        // Vorschlagsliste (ohne Popup, damit Tastatur offen bleibt)
        var selectedFromSuggestion by remember { mutableStateOf(false) }
        val matchingEquipment = remember(newItemName, viewModel.allEquipment.size, selectedFromSuggestion) {
            if (!selectedFromSuggestion && newItemName.trim().length >= 2) {
                viewModel.allEquipment.filter {
                    it.name.lowercase().contains(newItemName.trim().lowercase())
                }.take(5)
            } else emptyList()
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newItemName,
                onValueChange = { 
                    newItemName = it
                    selectedFromSuggestion = false
                },
                label = { Text("Gegenstand") },
                singleLine = true,
                trailingIcon = {
                    if (newItemName.isNotEmpty()) {
                        IconButton(onClick = { 
                            newItemName = ""
                            newItemWeight = ""
                            selectedFromSuggestion = false
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Löschen", tint = Color.Gray)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkDunkel,
                    focusedLabelColor = PinkDunkel
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = newItemWeight,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        newItemWeight = it 
                    }
                },
                label = { Text("Pfd.") },
                modifier = Modifier.width(70.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkDunkel,
                    focusedLabelColor = PinkDunkel
                )
            )
        }

        // Inline-Vorschläge (kein Popup = kein Fokusverlust)
        if (matchingEquipment.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    matchingEquipment.forEach { equipment ->
                        Surface(
                            onClick = {
                                newItemName = equipment.name
                                newItemWeight = if (equipment.weight > 0.0) equipment.weight.toString() else ""
                                newItemCategory = equipment.category
                                selectedFromSuggestion = true
                            },
                            color = Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(equipment.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BlauDunkel)
                                    Text(equipment.category, fontSize = 11.sp, color = Color.Gray)
                                }
                                if (equipment.weight > 0.0) {
                                    Text("${equipment.weight} Pfd.", fontSize = 12.sp, color = PinkDunkel, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dropdown für Kategorie
            val categories = listOf("Rüstung & Waffen", "Tränke", "Ausrüstung", "Magie", "Werkzeug", "Schätze", "Sonstiges")
            var categoryExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = newItemCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategorie") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PinkDunkel,
                        focusedLabelColor = PinkDunkel
                    )
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                newItemCategory = cat
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newItemName.isNotBlank()) {
                        val w = newItemWeight.toDoubleOrNull() ?: 0.0
                        val cat = if (newItemCategory.isBlank()) "Sonstiges" else newItemCategory.trim()
                        viewModel.addCustomLoot(newItemName.trim(), w, cat)
                        newItemName = ""
                        newItemWeight = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text("Hinzufügen")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- KATALOG-BUTTON ---
        OutlinedButton(
            onClick = { showEquipmentPicker = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = BlauDunkel),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(BlauDunkel)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Aus Katalog auswählen", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Liste des flexiblen Loots gruppiert nach Kategorie
        val groupedLoot = viewModel.customLoot.groupBy { it.category }
        groupedLoot.forEach { (category, itemsInCategory) ->
            var isExpanded by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = category,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlauDunkel
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Einklappen" else "Ausklappen",
                    tint = BlauDunkel
                )
            }

            if (isExpanded) {
                itemsInCategory.forEach { item ->
                    InventoryRow(
                        name = item.name,
                        amount = item.amount.toString(),
                        weight = item.weight,
                        onMinus = { viewModel.removeCustomLoot(item.name) },
                        onPlus = { viewModel.addCustomLoot(item.name, item.weight, item.category) }
                    )
                }
            }
        }
            } else {
                GroupLootView(viewModel = viewModel)
            }
        }
    }

    // --- EQUIPMENT PICKER DIALOG ---
    if (showEquipmentPicker) {
        EquipmentPickerDialog(
            catalog = viewModel.equipmentCatalog,
            onItemSelected = { item ->
                viewModel.addFromCatalog(item)
            },
            onDismiss = { showEquipmentPicker = false }
        )
    }
}

@Composable
fun InventoryRow(name: String, amount: String, weight: Double? = null, onMinus: () -> Unit, onPlus: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = BlauHell),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                if (weight != null && weight > 0.0) {
                    Text(text = "${weight} Pfd.", fontSize = 14.sp, color = GelbSand)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMinus) {
                    Icon(Icons.Default.Remove, contentDescription = "Weniger", tint = PinkDunkel)
                }
                Text(text = amount, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = onPlus) {
                    Icon(Icons.Default.Add, contentDescription = "Mehr", tint = BlauDunkel)
                }
            }
        }
    }
}

@Composable
fun CoinRow(name: String, amount: String, onMinus: (Int) -> Unit, onPlus: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = BlauHell),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { onMinus(10) }, colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel), contentPadding = PaddingValues(0.dp), modifier = Modifier.size(36.dp)) { Text("-10", fontSize = 14.sp) }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = { onMinus(1) }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Remove, contentDescription = "Weniger", tint = PinkDunkel) }
                
                Text(text = amount, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp).widthIn(min = 36.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                
                IconButton(onClick = { onPlus(1) }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Add, contentDescription = "Mehr", tint = BlauDunkel) }
                Spacer(modifier = Modifier.width(4.dp))
                Button(onClick = { onPlus(10) }, colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel), contentPadding = PaddingValues(0.dp), modifier = Modifier.size(32.dp)) { Text("+10", fontSize = 12.sp) }
            }
        }
    }
}

@Composable
fun GroupLootView(viewModel: CharacterViewModel) {
    var newItemName by remember { mutableStateOf("") }
    var isMoneyBagExpanded by remember { mutableStateOf(false) }

    // Gruppen-Geldbeutel
    Row(
        modifier = Modifier.fillMaxWidth().clickable { isMoneyBagExpanded = !isMoneyBagExpanded },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Gruppenkasse", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
        if (!isMoneyBagExpanded) {
            Text(
                "${viewModel.sharedCoins.km}KM | ${viewModel.sharedCoins.sm}SM | ${viewModel.sharedCoins.em}EM | ${viewModel.sharedCoins.gm}GM | ${viewModel.sharedCoins.pm}PM",
                fontSize = 14.sp,
                color = PinkDunkel,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text("Einklappen ▲", fontSize = 14.sp, color = BlauDunkel)
        }
    }
    Spacer(modifier = Modifier.height(8.dp))

    if (isMoneyBagExpanded) {
        CoinRow("Kupfer (KM)", viewModel.sharedCoins.km.toString(), onMinus = { viewModel.updateSharedCoins((viewModel.sharedCoins.km - it).coerceAtLeast(0), viewModel.sharedCoins.sm, viewModel.sharedCoins.em, viewModel.sharedCoins.gm, viewModel.sharedCoins.pm) }, onPlus = { viewModel.updateSharedCoins(viewModel.sharedCoins.km + it, viewModel.sharedCoins.sm, viewModel.sharedCoins.em, viewModel.sharedCoins.gm, viewModel.sharedCoins.pm) })
        CoinRow("Silber (SM)", viewModel.sharedCoins.sm.toString(), onMinus = { viewModel.updateSharedCoins(viewModel.sharedCoins.km, (viewModel.sharedCoins.sm - it).coerceAtLeast(0), viewModel.sharedCoins.em, viewModel.sharedCoins.gm, viewModel.sharedCoins.pm) }, onPlus = { viewModel.updateSharedCoins(viewModel.sharedCoins.km, viewModel.sharedCoins.sm + it, viewModel.sharedCoins.em, viewModel.sharedCoins.gm, viewModel.sharedCoins.pm) })
        CoinRow("Elektrum (EM)", viewModel.sharedCoins.em.toString(), onMinus = { viewModel.updateSharedCoins(viewModel.sharedCoins.km, viewModel.sharedCoins.sm, (viewModel.sharedCoins.em - it).coerceAtLeast(0), viewModel.sharedCoins.gm, viewModel.sharedCoins.pm) }, onPlus = { viewModel.updateSharedCoins(viewModel.sharedCoins.km, viewModel.sharedCoins.sm, viewModel.sharedCoins.em + it, viewModel.sharedCoins.gm, viewModel.sharedCoins.pm) })
        CoinRow("Gold (GM)", viewModel.sharedCoins.gm.toString(), onMinus = { viewModel.updateSharedCoins(viewModel.sharedCoins.km, viewModel.sharedCoins.sm, viewModel.sharedCoins.em, (viewModel.sharedCoins.gm - it).coerceAtLeast(0), viewModel.sharedCoins.pm) }, onPlus = { viewModel.updateSharedCoins(viewModel.sharedCoins.km, viewModel.sharedCoins.sm, viewModel.sharedCoins.em, viewModel.sharedCoins.gm + it, viewModel.sharedCoins.pm) })
        CoinRow("Platin (PM)", viewModel.sharedCoins.pm.toString(), onMinus = { viewModel.updateSharedCoins(viewModel.sharedCoins.km, viewModel.sharedCoins.sm, viewModel.sharedCoins.em, viewModel.sharedCoins.gm, (viewModel.sharedCoins.pm - it).coerceAtLeast(0)) }, onPlus = { viewModel.updateSharedCoins(viewModel.sharedCoins.km, viewModel.sharedCoins.sm, viewModel.sharedCoins.em, viewModel.sharedCoins.gm, viewModel.sharedCoins.pm + it) })
        Spacer(modifier = Modifier.height(16.dp))
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text("Geteilte Gegenstände", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
    Spacer(modifier = Modifier.height(8.dp))

    // Neues Item hinzufügen
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newItemName,
            onValueChange = { newItemName = it },
            label = { Text("Gegenstand") },
            singleLine = true,
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PinkDunkel, focusedLabelColor = PinkDunkel)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                if (newItemName.isNotBlank()) {
                    viewModel.addSharedLootItem(newItemName.trim(), 1, 0.0, "Sonstiges")
                    newItemName = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel)
        ) {
            Text("Hinzufügen")
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))

    val groupedLoot = viewModel.sharedLootItems.groupBy { it.category }
    groupedLoot.forEach { (category, items) ->
        Text(category, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlauDunkel, modifier = Modifier.padding(vertical = 4.dp))
        items.forEach { item ->
            InventoryRow(
                name = item.name,
                amount = item.amount.toString(),
                weight = item.weight,
                onMinus = { viewModel.updateSharedLootItem(item.id, item.amount - 1) },
                onPlus = { viewModel.updateSharedLootItem(item.id, item.amount + 1) }
            )
        }
    }
}