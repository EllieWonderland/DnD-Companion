package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.viewmodel.Feature

@Composable
fun FeatureSelectionScreen(viewModel: CharacterViewModel, onDismiss: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Alle") }
    
    val allAvailable = viewModel.getAvailableFeatures()
    val filteredFeatures = allAvailable.filter { feature ->
        val matchesSearch = feature.name.contains(searchQuery, ignoreCase = true) ||
                           feature.description.contains(searchQuery, ignoreCase = true)
        val matchesCategory = when (selectedCategory) {
            "Volk" -> feature.type == "RACIAL_TRAIT"
            "Klasse" -> feature.type == "CLASS_FEATURE" || feature.type == "SUBCLASS_FEATURE" || feature.type == "ELD_INV"
            "Talent" -> feature.type == "FEAT"
            else -> true
        }
        matchesSearch && matchesCategory
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PergamentBackground(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Merkmale & Talente wählen",
                        style = MaterialTheme.typography.titleLarge,
                        color = Waldgruen,
                        fontFamily = Almendra
                    )
                    IconButton(onClick = { viewModel.dismissFeatureSelection() }) {
                        Icon(Icons.Default.Close, contentDescription = "Schließen", tint = TintenSchwarz)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nach Merkmal suchen...", color = TintenBraun) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TintenSchwarz) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WaldGold,
                        unfocusedBorderColor = PergamentDunkel,
                        cursorColor = WaldGold
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Alle", "Volk", "Klasse", "Talent").forEach { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category, fontFamily = Almendra) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WaldGold,
                                selectedLabelColor = TintenSchwarz,
                                containerColor = PergamentDunkel,
                                labelColor = TintenBraun
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // List
                if (filteredFeatures.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Keine Merkmale gefunden", color = TintenBraun, fontFamily = Almendra)
                            if (allAvailable.isEmpty()) {
                                Text("(Katalog scheint leer zu sein)", style = MaterialTheme.typography.labelSmall, color = OchsenblutRot)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                    items(filteredFeatures) { feature ->
                        FeatureCard(
                            feature = feature,
                            isLearned = viewModel.customTraits.any { it.name == feature.name },
                            onLearn = { viewModel.learnFeature(feature) },
                            onUnlearn = { viewModel.unlearnFeature(feature.name) }
                        )
                    }
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    feature: Feature,
    isLearned: Boolean,
    onLearn: () -> Unit,
    onUnlearn: () -> Unit
) {
    PergamentCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        feature.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Waldgruen,
                        fontFamily = Almendra
                    )
                    
                    val reqs = mutableListOf<String>()
                    if (feature.raceReq.isNotEmpty()) reqs.add(feature.raceReq.joinToString("/"))
                    if (feature.classReq.isNotEmpty()) reqs.add(feature.classReq.joinToString("/"))
                    if (feature.levelReq > 1) reqs.add("Stufe ${feature.levelReq}")
                    
                    if (reqs.isNotEmpty()) {
                        Text(
                            "Voraussetzung: ${reqs.joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = OchsenblutRot,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
                
                if (isLearned) {
                    Button(
                        onClick = onUnlearn,
                        colors = ButtonDefaults.buttonColors(containerColor = EisenGrau),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Vergessen", fontSize = 12.sp, fontFamily = Almendra)
                    }
                } else {
                    Button(
                        onClick = onLearn,
                        colors = MetallButtonColors(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Wählen", fontSize = 12.sp, fontFamily = Almendra)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = TintenSchwarz,
                lineHeight = 16.sp
            )
        }
    }
}
