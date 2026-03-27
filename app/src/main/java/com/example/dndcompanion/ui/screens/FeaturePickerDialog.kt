package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.data.database.FeatureEntity
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.Material3RichText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeaturePickerDialog(viewModel: CharacterViewModel, onDismiss: () -> Unit) {
    val allFeatures by viewModel.searchedFeatures.collectAsState()
    
    // Ensure all features are loaded if the list is empty
    LaunchedEffect(Unit) {
        if (allFeatures.isEmpty()) {
            viewModel.searchRulebook("")
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Alle") }
    val featureTypes = listOf("Alle", "FEAT", "CLASS_FEATURE", "RACIAL_TRAIT", "SUBCLASS_FEATURE")

    val filteredFeatures = allFeatures.filter { feature ->
        val matchesSearch = feature.name.contains(searchQuery, ignoreCase = true) || 
                            feature.description.contains(searchQuery, ignoreCase = true)
        val matchesType = selectedType == "Alle" || feature.type == selectedType
        matchesSearch && matchesType
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PergamentHell,
        modifier = Modifier.fillMaxHeight(0.9f).fillMaxWidth(0.95f),
        title = {
            Text("Merkmal / Talent wählen", fontFamily = Almendra, fontSize = 22.sp, color = Waldgruen)
        },
        text = {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Suchen...", color = TintenBraun) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Suchen", tint = TintenSchwarz) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedBorderColor = TintenBraun,
                        focusedTextColor = TintenSchwarz,
                        unfocusedTextColor = TintenSchwarz,
                        cursorColor = MaterialTheme.colorScheme.tertiary
                    ),
                    singleLine = true
                )

                // Type Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    featureTypes.take(3).forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(if (type == "Alle") "Alle" else type.replace("_", " ")) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Waldgruen,
                                selectedLabelColor = PergamentHell,
                                labelColor = TintenSchwarz
                            )
                        )
                    }
                }

                // Feature List
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredFeatures) { feature ->
                        FeatureListItem(
                            feature = feature,
                            onClick = {
                                viewModel.learnFeature(feature)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen", color = MaterialTheme.colorScheme.tertiary, fontFamily = Almendra, fontSize = 18.sp)
            }
        }
    )
}

@Composable
fun FeatureListItem(feature: FeatureEntity, onClick: () -> Unit) {
    SteinCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            val titleColor = when (feature.type) {
                "FEAT" -> WaldgruenDunkel
                "RACIAL_TRAIT" -> MaterialTheme.colorScheme.tertiary
                "CLASS_FEATURE" -> HexenLila
                "SUBCLASS_FEATURE" -> TintenSchwarz
                else -> WaldgruenDunkel
            }
            Text(feature.name, fontSize = 18.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = titleColor)
            val subText = buildString {
                append(feature.type)
                if (!feature.category.isNullOrBlank()) append(" - ${feature.category}")
                if (feature.levelReq > 1) append(" (Ab Stufe ${feature.levelReq})")
            }.toString()
            Text(subText, fontSize = 13.sp, style = GrenzeGotischSmall, color = TintenSchwarz.copy(alpha = 0.8f))
            
            Spacer(modifier = Modifier.height(4.dp))
            Material3RichText(modifier = Modifier.fillMaxWidth()) {
                Markdown(feature.description)
            }
        }
    }
}
