package com.example.dndcompanion.ui.theme.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
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
fun RucksackScreen(viewModel: CharacterViewModel) {
    var newItemName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GelbSand)
            .padding(16.dp)
    ) {
        Text("Fester Rucksack", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
        Spacer(modifier = Modifier.height(8.dp))

        // Feste Items (Wasser, Rationen, Beeren)
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
                        enabled = viewModel.spellSlotsLevel1 > 0, // <-- NEU: Verhindert Zaubern ohne Slots
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
                label = { Text("Neuer Gegenstand") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkDunkel,
                    focusedLabelColor = PinkDunkel
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newItemName.isNotBlank()) {
                        viewModel.addCustomLoot(newItemName.trim())
                        newItemName = "" // Textfeld wieder leeren
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel)
            ) {
                Text("Hinzufügen")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Liste des flexiblen Loots
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(viewModel.customLoot) { item ->
                InventoryRow(
                    name = item.name,
                    amount = item.amount.toString(),
                    onMinus = { viewModel.removeCustomLoot(item.name) },
                    onPlus = { viewModel.addCustomLoot(item.name) }
                )
            }
        }
    }
}

@Composable
fun InventoryRow(name: String, amount: String, onMinus: () -> Unit, onPlus: () -> Unit) {
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
            Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)

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