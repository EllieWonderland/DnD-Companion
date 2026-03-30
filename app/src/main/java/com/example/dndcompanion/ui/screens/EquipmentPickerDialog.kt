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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.EquipmentCatalogItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentPickerDialog(
    catalog: List<EquipmentCatalogItem>,
    onItemSelected: (EquipmentCatalogItem) -> Unit,
    onItemBought: (EquipmentCatalogItem) -> Unit,
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
        containerColor = Pergament,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.fillMaxHeight(0.85f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Ausrüstungskatalog",
                    style = Typography.headlineSmall,
                    color = WaldgruenDunkel
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Schließen", tint = WaldgruenDunkel)
                }
            }

            // --- SUCHFELD ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Gegenstand suchen...", fontFamily = Almendra) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Waldgruen) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Löschen", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                textStyle = Typography.bodyLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedBorderColor = PergamentDunkel,
                    cursorColor = MaterialTheme.colorScheme.tertiary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                    label = { Text("Alle", fontFamily = Almendra, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Waldgruen,
                        selectedLabelColor = PergamentHell
                    ),
                    modifier = Modifier.heightIn(min = 40.dp)
                )
                categories.forEach { cat ->
                    val chipLabel = when {
                        cat.contains("Waffen", ignoreCase = true) -> "⚔️ ${cat.substringAfterLast("(").removeSuffix(")")}"
                        cat == "Ausrüstung" -> "🎒 Ausrüstung"
                        cat.contains("Rüstung", ignoreCase = true) -> "🛡️ Rüstung"
                        cat.contains("Werkzeug", ignoreCase = true) -> "🔧 Werkzeug"
                        else -> cat
                    }
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = if (selectedCategory == cat) null else cat
                        },
                        label = { Text(chipLabel, maxLines = 1, overflow = TextOverflow.Ellipsis, fontFamily = Almendra) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Waldgruen,
                            selectedLabelColor = PergamentHell
                        ),
                        modifier = Modifier.heightIn(min = 40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- ERGEBNIS-ZÄHLER ---
            Text(
                "${filteredItems.size} Gegenstände gefunden",
                style = Typography.bodySmall,
                color = TintenBraun,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- ITEM-LISTE ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredItems, key = { "${it.name}_${it.category}" }) { item ->
                    CatalogItemCard(item = item, onAdd = { onItemSelected(item) }, onItemBought = { onItemBought(item) })
                }
            }
        }
    }
}

@Composable
private fun CatalogItemCard(item: EquipmentCatalogItem, onAdd: () -> Unit, onItemBought: () -> Unit) {
    PergamentCard(modifier = Modifier.fillMaxWidth()) {
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
                    style = Typography.titleMedium,
                    color = TintenSchwarz,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    if (item.weight > 0.0) {
                        Text(
                            text = "${item.weight} kg",
                            style = GrenzeGotischSmall.copy(fontSize = 14.sp),
                            color = Waldgruen
                        )
                    }
                    if (item.price.isNotBlank() && item.price != "-") {
                        Text(
                            text = item.price,
                            style = GrenzeGotischSmall.copy(fontSize = 14.sp),
                            color = TintenBraun
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (item.price.isNotBlank() && item.price != "-" && item.price != "—" && !item.price.contains("Variiert", ignoreCase = true)) {
                    IconButton(onClick = onItemBought, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Kaufen",
                            tint = PergamentHell,
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        )
                    }
                }
                IconButton(onClick = onAdd, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Hinzufügen",
                        tint = PergamentHell,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Waldgruen, RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}
