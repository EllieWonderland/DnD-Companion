package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.text.input.KeyboardType
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.theme.BlauDunkel
import com.example.dndcompanion.ui.theme.BlauHell
import com.example.dndcompanion.ui.theme.PinkDunkel
import com.example.dndcompanion.ui.theme.GelbSand

@Composable
fun RucksackScreen(viewModel: CharacterViewModel) {
    var newItemName by remember { mutableStateOf("") }
    var newItemWeight by remember { mutableStateOf("") }
    var isMoneyBagExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GelbSand)
            .padding(16.dp)
    ) {
        item {
            // --- GEWICHT ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Traglast", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
                val weightColor = if (viewModel.currentWeight > viewModel.maxWeight) Color.Red else BlauDunkel
                Text(String.format(java.util.Locale.US, "%.1f / %.1f kg", viewModel.currentWeight, viewModel.maxWeight), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = weightColor)
            }

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
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Gute Beeren", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Button(
                            onClick = { viewModel.castGoodberry() },
                            enabled = viewModel.spellSlotsLevel1 > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel),
                            modifier = Modifier.height(36.dp).padding(top = 4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text("Zaubern (+10)", fontSize = 12.sp)
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
                        "${viewModel.coinsPM} PM | ${viewModel.coinsGM} GM | ${viewModel.coinsEM} EM | ${viewModel.coinsSM} SM | ${viewModel.coinsKM} KM",
                        fontSize = 12.sp,
                        color = PinkDunkel,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text("Einklappen ▲", fontSize = 12.sp, color = BlauDunkel)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (isMoneyBagExpanded) {
                CoinRow("Platin (PM)", viewModel.coinsPM.toString(), onMinus = { viewModel.changeCoinsPM(-it) }, onPlus = { viewModel.changeCoinsPM(it) })
                CoinRow("Gold (GM)", viewModel.coinsGM.toString(), onMinus = { viewModel.changeCoinsGM(-it) }, onPlus = { viewModel.changeCoinsGM(it) })
                CoinRow("Elektrum (EM)", viewModel.coinsEM.toString(), onMinus = { viewModel.changeCoinsEM(-it) }, onPlus = { viewModel.changeCoinsEM(it) })
                CoinRow("Silber (SM)", viewModel.coinsSM.toString(), onMinus = { viewModel.changeCoinsSM(-it) }, onPlus = { viewModel.changeCoinsSM(it) })
                CoinRow("Kupfer (KM)", viewModel.coinsKM.toString(), onMinus = { viewModel.changeCoinsKM(-it) }, onPlus = { viewModel.changeCoinsKM(it) })
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Flexibler Loot", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
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
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PinkDunkel,
                        focusedLabelColor = PinkDunkel
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = newItemWeight,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            newItemWeight = it 
                        }
                    },
                    label = { Text("kg") },
                    modifier = Modifier.width(70.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PinkDunkel,
                        focusedLabelColor = PinkDunkel
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newItemName.isNotBlank()) {
                            val w = newItemWeight.toDoubleOrNull() ?: 0.0
                            viewModel.addCustomLoot(newItemName.trim(), w)
                            newItemName = "" // Textfeld wieder leeren
                            newItemWeight = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Hinzufügen")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Liste des flexiblen Loots
        items(viewModel.customLoot) { item ->
            InventoryRow(
                name = item.name,
                amount = item.amount.toString(),
                weight = item.weight,
                onMinus = { viewModel.removeCustomLoot(item.name) },
                onPlus = { viewModel.addCustomLoot(item.name, item.weight) }
            )
        }
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
                    Text(text = "${weight} kg", fontSize = 12.sp, color = GelbSand)
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
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = BlauHell),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BlauDunkel, modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { onMinus(10) }, colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel), contentPadding = PaddingValues(0.dp), modifier = Modifier.size(32.dp)) { Text("-10", fontSize = 10.sp) }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = { onMinus(1) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Remove, contentDescription = "Weniger", tint = PinkDunkel) }
                
                Text(text = amount, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp).widthIn(min = 28.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                
                IconButton(onClick = { onPlus(1) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Add, contentDescription = "Mehr", tint = BlauDunkel) }
                Spacer(modifier = Modifier.width(4.dp))
                Button(onClick = { onPlus(10) }, colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel), contentPadding = PaddingValues(0.dp), modifier = Modifier.size(32.dp)) { Text("+10", fontSize = 10.sp) }
            }
        }
    }
}