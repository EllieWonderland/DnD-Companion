package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.dndcompanion.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.*
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.viewmodel.GroupViewModel
import com.example.dndcompanion.ui.viewmodel.InventoryItem
import com.example.dndcompanion.ui.viewmodel.InventoryViewModel
import com.example.dndcompanion.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RucksackScreen(viewModel: CharacterViewModel, inventoryVm: InventoryViewModel, groupVm: GroupViewModel) {
    var newItemName by remember { mutableStateOf("") }
    var newItemWeight by remember { mutableStateOf("") }
    var newItemCategory by remember { mutableStateOf("Sonstiges") }
    var newItemQuantity by remember { mutableStateOf("1") }
    var newItemNotes by remember { mutableStateOf("") }
    var isMoneyBagExpanded by remember { mutableStateOf(false) }
    var showEquipmentPicker by remember { mutableStateOf(false) }
    var showGroupLoot by remember { mutableStateOf(false) }
    var itemToSell by remember { mutableStateOf<InventoryItem?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(viewModel.snackbarMessage.value) {
        viewModel.snackbarMessage.value?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.snackbarMessage.value = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        PergamentBackground {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(
                        selectedTabIndex = if (showGroupLoot) 1 else 0,
                        containerColor = WaldgruenDunkel,
                        contentColor = PergamentHell,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[if (showGroupLoot) 1 else 0]),
                                color = PergamentHell
                            )
                        }
                    ) {
                        Tab(
                            selected = !showGroupLoot,
                            onClick = { showGroupLoot = false },
                            text = { Text("Persönlich", fontFamily = Almendra, fontWeight = FontWeight.Bold) },
                            selectedContentColor = PergamentHell,
                            unselectedContentColor = PergamentHell.copy(alpha = 0.7f)
                        )
                        Tab(
                            selected = showGroupLoot,
                            onClick = { showGroupLoot = true },
                            text = { Text("Gruppen-Loot", fontFamily = Almendra, fontWeight = FontWeight.Bold) },
                            selectedContentColor = PergamentHell,
                            unselectedContentColor = PergamentHell.copy(alpha = 0.7f)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (!showGroupLoot) {
                            // --- TRAGLAST (Kette/Seil Optik) ---
                            Text(
                                text = "Traglast",
                                style = Typography.titleLarge,
                                color = TintenSchwarz
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val weightRatio = (viewModel.currentWeight / viewModel.maxWeight).toFloat().coerceIn(0f, 1f)
                            val weightColor = when {
                                weightRatio > 0.9f -> MaterialTheme.colorScheme.error
                                weightRatio > 0.7f -> BronzeDunkel
                                else -> Waldgruen
                            }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                ChainProgressBar(progress = weightRatio, color = weightColor)
                                Text(
                                    text = String.format(java.util.Locale.US, "%.1f / %.0f kg", viewModel.currentWeight, viewModel.maxWeight),
                                    modifier = Modifier.align(Alignment.CenterEnd).padding(top = 28.dp),
                                    style = GrenzeGotischSmall,
                                    color = weightColor
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Ausrüstung",
                                style = Typography.headlineSmall,
                                color = TintenSchwarz
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Feste Items
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

                            if (viewModel.allSpells.any { it.name == "Gute Beere" && it.isPrepared } || viewModel.goodberries > 0) {
                                InventoryRow(
                                    name = "Gute Beeren",
                                    amount = viewModel.goodberries.toString(),
                                    onMinus = { viewModel.changeGoodberries(-1) },
                                    onPlus = { viewModel.changeGoodberries(1) }
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // --- GELDBEUTEL ---
                            SteinCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isMoneyBagExpanded = !isMoneyBagExpanded },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Image(
                                                painter = painterResource(id = R.drawable.icon_geld),
                                                contentDescription = "Geldbeutel Icon",
                                                modifier = Modifier.size(38.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Geldbeutel", style = Typography.titleLarge, color = TintenSchwarz)
                                        }
                                        Icon(
                                            imageVector = if (isMoneyBagExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = TintenSchwarz
                                        )
                                    }

                                    if (isMoneyBagExpanded) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        CoinRow("Kupfer (KM)", viewModel.coinsKM.toString(), Color(0xFFCD7F32), onMinus = { viewModel.changeCoinsKM(-it) }, onPlus = { viewModel.changeCoinsKM(it) }, onSetDirect = { viewModel.setCoinsKM(it) })
                                        CoinRow("Silber (SM)", viewModel.coinsSM.toString(), Color(0xFFC0C0C0), onMinus = { viewModel.changeCoinsSM(-it) }, onPlus = { viewModel.changeCoinsSM(it) }, onSetDirect = { viewModel.setCoinsSM(it) })
                                        CoinRow("Elektrum (EM)", viewModel.coinsEM.toString(), Color(0xFFE5E4E2), onMinus = { viewModel.changeCoinsEM(-it) }, onPlus = { viewModel.changeCoinsEM(it) }, onSetDirect = { viewModel.setCoinsEM(it) })
                                        CoinRow("Gold (GM)", viewModel.coinsGM.toString(), Color(0xFFFFD700), onMinus = { viewModel.changeCoinsGM(-it) }, onPlus = { viewModel.changeCoinsGM(it) }, onSetDirect = { viewModel.setCoinsGM(it) })
                                        CoinRow("Platin (PM)", viewModel.coinsPM.toString(), Color(0xFFE5E4E2), onMinus = { viewModel.changeCoinsPM(-it) }, onPlus = { viewModel.changeCoinsPM(it) }, onSetDirect = { viewModel.setCoinsPM(it) })
                                    } else {
                                        Text(
                                            text = "${viewModel.coinsKM}KM | ${viewModel.coinsSM}SM | ${viewModel.coinsEM}EM | ${viewModel.coinsGM}GM | ${viewModel.coinsPM}PM",
                                            style = GrenzeGotischSmall,
                                            color = TintenBraun,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // --- NEUES ITEM / KATALOG ---
                            Text("Neuer Fund", style = Typography.headlineSmall, color = TintenSchwarz)
                            Spacer(modifier = Modifier.height(8.dp))

                            PergamentCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedTextField(
                                            value = newItemName,
                                            onValueChange = { newItemName = it },
                                            label = { Text("Name", fontFamily = Almendra) },
                                            modifier = Modifier.weight(1f),
                                            textStyle = TextStyle(fontFamily = Almendra),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary, focusedLabelColor = MaterialTheme.colorScheme.tertiary)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedTextField(
                                            value = newItemWeight,
                                            onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) newItemWeight = it },
                                            label = { Text("kg", fontFamily = Almendra) },
                                            modifier = Modifier.width(60.dp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary, focusedLabelColor = MaterialTheme.colorScheme.tertiary)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedTextField(
                                            value = newItemQuantity,
                                            onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) newItemQuantity = it },
                                            label = { Text("Anz.", fontFamily = Almendra) },
                                            modifier = Modifier.width(56.dp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary, focusedLabelColor = MaterialTheme.colorScheme.tertiary)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = newItemNotes,
                                        onValueChange = { newItemNotes = it },
                                        label = { Text("Notizen (optional)", fontFamily = Almendra) },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = TextStyle(fontFamily = Almendra),
                                        maxLines = 2,
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary, focusedLabelColor = MaterialTheme.colorScheme.tertiary)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    // Kategorien-Dropdown
                                    val categories = listOf("Ausrüstung", "Rüstung & Waffen", "Tränke", "Magie", "Werkzeug", "Schätze", "Sonstiges")
                                    var categoryExpanded by remember { mutableStateOf(false) }
                                    
                                    ExposedDropdownMenuBox(
                                        expanded = categoryExpanded,
                                        onExpandedChange = { categoryExpanded = !categoryExpanded },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = newItemCategory,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Kategorie", fontFamily = Almendra) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                            textStyle = TextStyle(fontFamily = Almendra),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary, focusedLabelColor = MaterialTheme.colorScheme.tertiary)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = categoryExpanded,
                                            onDismissRequest = { categoryExpanded = false },
                                            modifier = Modifier.background(Pergament)
                                        ) {
                                            categories.forEach { cat ->
                                                DropdownMenuItem(
                                                    text = { Text(cat, fontFamily = Almendra) },
                                                    onClick = {
                                                        newItemCategory = cat
                                                        categoryExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            if (newItemName.isNotBlank()) {
                                                val qty = newItemQuantity.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                                val notes = newItemNotes.trim().ifBlank { null }
                                                viewModel.addCustomLoot(newItemName.trim(), newItemWeight.toDoubleOrNull() ?: 0.0, newItemCategory, notes = notes, quantity = qty)
                                                newItemName = ""
                                                newItemWeight = ""
                                                newItemQuantity = "1"
                                                newItemNotes = ""
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = MetallButtonColors(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Hinzufügen", fontFamily = Almendra, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { showEquipmentPicker = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Waldgruen, contentColor = PergamentHell),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Katalog öffnen", fontFamily = Almendra, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Liste flexibler Loot
                            val groupedLoot = viewModel.customLoot.groupBy { it.category }
                            groupedLoot.forEach { (cat, items) ->
                                var isExpanded by remember { mutableStateOf(true) }
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded }.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(cat, style = Typography.titleMedium, color = Waldgruen)
                                    Icon(if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = Waldgruen)
                                }
                                if (isExpanded) {
                                    items.forEach { item ->
                                        InventoryRow(
                                            name = item.name,
                                            amount = item.amount.toString(),
                                            weight = item.weight,
                                            price = item.price,
                                            notes = item.notes,
                                            onMinus = { viewModel.removeCustomLoot(item.name) },
                                            onPlus = { viewModel.addCustomLoot(item.name, item.weight, cat, item.price) },
                                            onSell = { itemToSell = item }
                                        )
                                    }
                                }
                            }
                        } else {
                            GroupLootView(viewModel, groupVm)
                        }
                    }
                }
            }
        }
    }

    if (itemToSell != null) {
        val item = itemToSell!!
        val sellPrice = if (!item.price.isNullOrBlank() && item.price != "-" && item.price != "—") {
            " (${item.price} / 2)"
        } else ""
        AlertDialog(
            onDismissRequest = { itemToSell = null },
            title = { Text("Verkaufen?", fontFamily = Almendra, fontWeight = FontWeight.Bold) },
            text = { Text("${item.name} verkaufen$sellPrice?", fontFamily = Almendra) },
            confirmButton = {
                TextButton(onClick = {
                    val msg = viewModel.sellItem(item)
                    viewModel.snackbarMessage.value = msg
                    itemToSell = null
                }) { Text("Verkaufen", color = Waldgruen, fontFamily = Almendra) }
            },
            dismissButton = {
                TextButton(onClick = { itemToSell = null }) { Text("Abbrechen", fontFamily = Almendra) }
            },
            containerColor = Pergament
        )
    }

    if (showEquipmentPicker) {
        EquipmentPickerDialog(
            catalog = viewModel.equipmentCatalog,
            onItemSelected = { viewModel.addFromCatalog(it) },
            onItemBought = { viewModel.buyItemFromCatalog(it) },
            onDismiss = { showEquipmentPicker = false }
        )
    }
}

@Composable
fun ChainProgressBar(progress: Float, color: Color) {
    Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
        val width = size.width
        val height = size.height
        val linkWidth = 30.dp.toPx()
        val linkHeight = 12.dp.toPx()
        val spacing = linkWidth * 0.7f
        
        // Hintergrund "Track" (Dunkleres Seil/Kette)
        drawLine(
            color = TintenBraun.copy(alpha = 0.15f),
            start = androidx.compose.ui.geometry.Offset(0f, height/2),
            end = androidx.compose.ui.geometry.Offset(width, height/2),
            strokeWidth = 6.dp.toPx()
        )

        // Leere Kette (der volle Bereich)
        var emptyX = 0f
        while (emptyX < width - linkWidth/2) {
            drawRoundRect(
                color = TintenSchwarz.copy(alpha = 0.1f),
                topLeft = androidx.compose.ui.geometry.Offset(emptyX, height/2 - linkHeight/2),
                size = androidx.compose.ui.geometry.Size(linkWidth, linkHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )
            emptyX += spacing
        }

        // Aktive Kette (Ausschöpfung)
        val activeWidth = width * progress
        var currentX = 0f
        while (currentX < activeWidth - linkWidth/2) {
            // Hintergrund-Füllung für aktive links
            drawRoundRect(
                color = color.copy(alpha = 0.2f),
                topLeft = androidx.compose.ui.geometry.Offset(currentX, height/2 - linkHeight/2),
                size = androidx.compose.ui.geometry.Size(linkWidth, linkHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
            )
            // Umriss der aktiven links
            drawRoundRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(currentX, height/2 - linkHeight/2),
                size = androidx.compose.ui.geometry.Size(linkWidth, linkHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )
            currentX += spacing
        }
    }
}

@Composable
fun InventoryRow(name: String, amount: String, weight: Double? = null, price: String? = null, notes: String? = null, extraAction: @Composable (() -> Unit)? = null, onSell: (() -> Unit)? = null, onMinus: () -> Unit, onPlus: () -> Unit) {
    PergamentCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = Typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (weight != null && weight > 0.0) {
                    Text(text = "${weight} kg", style = Typography.bodySmall)
                }
                if (!price.isNullOrBlank()) {
                    Text(text = price, style = Typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                }
                if (!notes.isNullOrBlank()) {
                    Text(text = notes, style = Typography.bodySmall, color = TintenBraun)
                }
                if (extraAction != null) {
                    Box(modifier = Modifier.padding(top = 4.dp)) {
                        extraAction()
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onSell != null) {
                    IconButton(onClick = onSell, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Sell, contentDescription = "Verkaufen", tint = TintenBraun, modifier = Modifier.size(24.dp))
                    }
                }
                IconButton(onClick = onMinus, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = OchsenblutRot, modifier = Modifier.size(32.dp))
                }
                Text(text = amount, style = GrenzeGotischSmall, modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = onPlus, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Waldgruen, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
fun CoinRow(name: String, amount: String, coinColor: Color, onMinus: (Int) -> Unit, onPlus: (Int) -> Unit, onSetDirect: ((Int) -> Unit)? = null) {
    var showDirectInput by remember { mutableStateOf(false) }
    var directInput by remember { mutableStateOf("") }

    if (showDirectInput) {
        AlertDialog(
            onDismissRequest = { showDirectInput = false; directInput = "" },
            title = { Text(name, fontFamily = Almendra, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = directInput,
                    onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) directInput = it },
                    label = { Text("Menge", fontFamily = Almendra) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary, focusedLabelColor = MaterialTheme.colorScheme.tertiary)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { directInput.toIntOrNull()?.let { onSetDirect?.invoke(it) }; showDirectInput = false; directInput = "" },
                    enabled = directInput.isNotEmpty()
                ) { Text("Übernehmen", color = Waldgruen, fontFamily = Almendra) }
            },
            dismissButton = {
                TextButton(onClick = { showDirectInput = false; directInput = "" }) { Text("Abbrechen", fontFamily = Almendra) }
            },
            containerColor = Pergament
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(6.dp)).background(coinColor))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = name, style = Typography.bodyMedium)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onMinus(10) }, modifier = Modifier.size(width = 48.dp, height = 48.dp)) { Text("-10", fontSize = 13.sp, color = OchsenblutRot) }
            IconButton(onClick = { onMinus(1) }, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Remove, contentDescription = "-1 $name", tint = OchsenblutRot, modifier = Modifier.size(20.dp)) }
            Text(
                text = amount,
                style = GrenzeGotischSmall,
                modifier = Modifier
                    .widthIn(min = 30.dp)
                    .clickable(enabled = onSetDirect != null) { directInput = amount; showDirectInput = true },
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(onClick = { onPlus(1) }, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Add, contentDescription = "+1 $name", tint = Waldgruen, modifier = Modifier.size(20.dp)) }
            TextButton(onClick = { onPlus(10) }, modifier = Modifier.size(width = 48.dp, height = 48.dp)) { Text("+10", fontSize = 13.sp, color = Waldgruen) }
        }
    }
}

@Composable
fun GroupLootView(viewModel: CharacterViewModel, groupVm: GroupViewModel) {
    var newItemName by remember { mutableStateOf("") }
    var isMoneyBagExpanded by remember { mutableStateOf(false) }

    SteinCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { isMoneyBagExpanded = !isMoneyBagExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Gruppenkasse", style = Typography.titleLarge)
                Icon(if (isMoneyBagExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = if (isMoneyBagExpanded) "Einklappen" else "Ausklappen")
            }
            if (isMoneyBagExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                CoinRow("KM", groupVm.sharedCoins.km.toString(), Color(0xFFCD7F32), onMinus = { groupVm.updateSharedCoins((groupVm.sharedCoins.km - it).coerceAtLeast(0), groupVm.sharedCoins.sm, groupVm.sharedCoins.em, groupVm.sharedCoins.gm, groupVm.sharedCoins.pm) }, onPlus = { groupVm.updateSharedCoins(groupVm.sharedCoins.km + it, groupVm.sharedCoins.sm, groupVm.sharedCoins.em, groupVm.sharedCoins.gm, groupVm.sharedCoins.pm) })
                CoinRow("SM", groupVm.sharedCoins.sm.toString(), Color(0xFFC0C0C0), onMinus = { groupVm.updateSharedCoins(groupVm.sharedCoins.km, (groupVm.sharedCoins.sm - it).coerceAtLeast(0), groupVm.sharedCoins.em, groupVm.sharedCoins.gm, groupVm.sharedCoins.pm) }, onPlus = { groupVm.updateSharedCoins(groupVm.sharedCoins.km, groupVm.sharedCoins.sm + it, groupVm.sharedCoins.em, groupVm.sharedCoins.gm, groupVm.sharedCoins.pm) })
                CoinRow("GM", groupVm.sharedCoins.gm.toString(), Color(0xFFFFD700), onMinus = { groupVm.updateSharedCoins(groupVm.sharedCoins.km, groupVm.sharedCoins.sm, groupVm.sharedCoins.em, (groupVm.sharedCoins.gm - it).coerceAtLeast(0), groupVm.sharedCoins.pm) }, onPlus = { groupVm.updateSharedCoins(groupVm.sharedCoins.km, groupVm.sharedCoins.sm, groupVm.sharedCoins.em, groupVm.sharedCoins.gm + it, groupVm.sharedCoins.pm) })
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
    Text("Geteilte Gegenstände", style = Typography.headlineSmall)
    Spacer(modifier = Modifier.height(8.dp))

    PergamentCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newItemName,
                onValueChange = { newItemName = it },
                label = { Text("Gegenstand") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { if (newItemName.isNotBlank()) { groupVm.addSharedLootItem(newItemName.trim(), 1, 0.0, "Sonstiges"); newItemName = "" } }, colors = MetallButtonColors()) {
                Text("Hinzufügen")
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    groupVm.sharedLootItems.groupBy { it.category }.forEach { (cat, items) ->
        Text(cat, style = Typography.titleMedium, color = Waldgruen, modifier = Modifier.padding(vertical = 4.dp))
        items.forEach { item ->
            InventoryRow(
                name = item.name,
                amount = item.amount.toString(),
                weight = item.weight,
                onMinus = { groupVm.updateSharedLootItem(item.id, item.amount - 1) },
                onPlus = { groupVm.updateSharedLootItem(item.id, item.amount + 1) }
            )
        }
    }
}
