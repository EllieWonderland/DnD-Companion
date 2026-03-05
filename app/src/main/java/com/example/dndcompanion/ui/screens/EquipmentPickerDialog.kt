package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.theme.BlauDunkel
import com.example.dndcompanion.ui.theme.BlauHell
import com.example.dndcompanion.ui.theme.GelbSand
import com.example.dndcompanion.ui.theme.PinkDunkel
import com.example.dndcompanion.ui.viewmodel.EquipmentCatalogItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentPickerDialog(
    catalog: List<EquipmentCatalogItem>,
    onItemSelected: (EquipmentCatalogItem) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val categories = remember(catalog) {
        catalog.map { it.category }.distinct().sorted()
    }

    val filteredItems = remember(catalog, searchQuery, selectedCategory) {
        catalog.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null ||
                item.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GelbSand,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.fillMaxHeight(0.85f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Ausrüstungskatalog",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlauDunkel
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Schließen", tint = BlauDunkel)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- SUCHFELD ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Gegenstand suchen...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BlauDunkel) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Löschen", tint = PinkDunkel)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkDunkel,
                    focusedLabelColor = PinkDunkel,
                    cursorColor = PinkDunkel
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- KATEGORIE-CHIPS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("Alle") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BlauDunkel,
                        selectedLabelColor = Color.White
                    )
                )
                categories.forEach { cat ->
                    val chipLabel = when {
                        cat.startsWith("Waffen") -> "⚔️ ${cat.substringAfter("(").removeSuffix(")")}"
                        cat == "Rüstung" -> "🛡️ Rüstung"
                        cat == "Werkzeug" -> "🔧 Werkzeug"
                        cat == "Ausrüstung" -> "🎒 Ausrüstung"
                        else -> cat
                    }
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = if (selectedCategory == cat) null else cat
                        },
                        label = { Text(chipLabel, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BlauDunkel,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- ERGEBNIS-ZÄHLER ---
            Text(
                "${filteredItems.size} Gegenstände",
                fontSize = 14.sp,
                color = BlauDunkel,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // --- ITEM-LISTE ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredItems, key = { "${it.name}_${it.category}" }) { item ->
                    CatalogItemCard(item = item, onAdd = { onItemSelected(item) })
                }
            }
        }
    }
}

@Composable
private fun CatalogItemCard(item: EquipmentCatalogItem, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BlauHell),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    if (item.weight > 0.0) {
                        Text(
                            text = "${item.weight} Pfd.",
                            fontSize = 13.sp,
                            color = GelbSand
                        )
                    }
                    if (item.price.isNotBlank() && item.price != "-") {
                        Text(
                            text = item.price,
                            fontSize = 13.sp,
                            color = GelbSand.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            IconButton(onClick = onAdd) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Hinzufügen",
                    tint = GelbSand,
                    modifier = Modifier
                        .size(28.dp)
                        .background(BlauDunkel, RoundedCornerShape(6.dp))
                        .padding(4.dp)
                )
            }
        }
    }
}
