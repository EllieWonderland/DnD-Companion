package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellbookDetailView(viewModel: CharacterViewModel, onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableIntStateOf(-1) }
    val globalSpellbook by viewModel.globalSpellbook.collectAsState()

    var selectedClassFilter by remember { mutableStateOf("Alle") }
    val classFilters = remember(globalSpellbook) {
        listOf("Alle") + globalSpellbook.flatMap { it.classes }.map { it.trim() }.distinct().sorted()
    }

    var selectedSchoolFilter by remember { mutableStateOf("Alle") }
    val schoolFilters = remember(globalSpellbook) {
        listOf("Alle") + globalSpellbook.map { it.school.trim() }.distinct().sorted()
    }

    PergamentBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WaldgruenDunkel)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = PergamentHell)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Zauberbuch", fontSize = 24.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = PergamentHell)
            }

            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Zauber suchen...", color = TintenSchwarz.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WaldgruenDunkel) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Suchen löschen", tint = WaldgruenDunkel)
                            }
                        }
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 16.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WaldgruenDunkel,
                        unfocusedBorderColor = WaldgruenDunkel.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                val scrollRowState = rememberScrollState()
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(scrollRowState).padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Stufe:", color = TintenSchwarz, fontFamily = Almendra, fontWeight = FontWeight.Bold)
                    val levels = listOf(-1) + (0..9).toList()
                    levels.forEach { lvl ->
                        val hasSpells = lvl == -1 || globalSpellbook.any { spell ->
                            spell.level == lvl &&
                            (selectedClassFilter == "Alle" || spell.classes.map { it.trim() }.contains(selectedClassFilter)) &&
                            (selectedSchoolFilter == "Alle" || spell.school.trim() == selectedSchoolFilter)
                        }
                        Button(
                            onClick = { selectedLevel = lvl },
                            enabled = hasSpells,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedLevel == lvl) OchsenblutRot else PergamentDunkel,
                                contentColor = if (selectedLevel == lvl) PergamentHell else if (hasSpells) TintenSchwarz else TintenBraun.copy(alpha=0.5f),
                                disabledContainerColor = TintenSchwarz.copy(alpha = 0.1f)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            val label = if (lvl == -1) "Alle" else if (lvl == 0) "Tricks" else "Grad $lvl"
                            Text(label, fontSize = 16.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                val classScrollState = rememberScrollState()
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(classScrollState).padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Klasse:", color = TintenSchwarz, fontFamily = Almendra, fontWeight = FontWeight.Bold)
                    classFilters.forEach { filterClass ->
                        val hasSpells = filterClass == "Alle" || globalSpellbook.any { spell ->
                            spell.classes.map { it.trim() }.contains(filterClass) &&
                            (selectedLevel == -1 || spell.level == selectedLevel) &&
                            (selectedSchoolFilter == "Alle" || spell.school.trim() == selectedSchoolFilter)
                        }
                        Button(
                            onClick = { selectedClassFilter = filterClass },
                            enabled = hasSpells,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedClassFilter == filterClass) OchsenblutRot else PergamentDunkel,
                                contentColor = if (selectedClassFilter == filterClass) PergamentHell else if (hasSpells) TintenSchwarz else TintenBraun.copy(alpha=0.5f),
                                disabledContainerColor = TintenSchwarz.copy(alpha = 0.1f)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(filterClass, fontSize = 16.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val schoolScrollState = rememberScrollState()
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(schoolScrollState).padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Schule:", color = TintenSchwarz, fontFamily = Almendra, fontWeight = FontWeight.Bold)
                    schoolFilters.forEach { filterSchool ->
                        val hasSpells = filterSchool == "Alle" || globalSpellbook.any { spell ->
                            spell.school.trim() == filterSchool &&
                            (selectedLevel == -1 || spell.level == selectedLevel) &&
                            (selectedClassFilter == "Alle" || spell.classes.map { it.trim() }.contains(selectedClassFilter))
                        }
                        Button(
                            onClick = { selectedSchoolFilter = filterSchool },
                            enabled = hasSpells,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedSchoolFilter == filterSchool) OchsenblutRot else PergamentDunkel,
                                contentColor = if (selectedSchoolFilter == filterSchool) PergamentHell else if (hasSpells) TintenSchwarz else TintenBraun.copy(alpha=0.5f),
                                disabledContainerColor = TintenSchwarz.copy(alpha = 0.1f)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(filterSchool, fontSize = 16.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val filteredSpells = globalSpellbook.filter { spell ->
                    val matchesClass = if (selectedClassFilter == "Alle") true else spell.classes.map { it.trim() }.contains(selectedClassFilter)
                    val matchesSchool = if (selectedSchoolFilter == "Alle") true else spell.school.trim() == selectedSchoolFilter

                    matchesClass && matchesSchool &&
                    (selectedLevel == -1 || spell.level == selectedLevel) &&
                    (searchQuery.isBlank() || spell.name.contains(searchQuery, ignoreCase = true))
                }.sortedWith(compareBy({ it.level }, { it.name }))

                if (filteredSpells.isEmpty()) {
                    Text("Keine Zauber gefunden.", modifier = Modifier.padding(16.dp), color = TintenSchwarz, fontWeight = FontWeight.Bold)
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(filteredSpells) { catalogSpellEntity ->
                            val catalogSpell = catalogSpellEntity.toSpell()
                            val alreadyInBook = viewModel.allSpells.any { it.name == catalogSpell.name }
                            val isDruidSpell = catalogSpell.classes.contains("Druide") && !catalogSpell.classes.contains("Waldläufer")
                            val isDruidLevel1 = isDruidSpell && catalogSpell.level == 1
                            val druidLevel1Count = viewModel.allSpells.count { it.classes.contains("Druide") && !it.classes.contains("Waldläufer") && it.level == 1 }

                            SpellCard(
                                spell = catalogSpell,
                                isEditMode = false,
                                isEquipped = alreadyInBook,
                                onTogglePrep = {},
                                onDelete = null,
                                customColor = if (catalogSpell.classes.contains("Waldläufer")) Waldgruen else if (catalogSpell.classes.contains("Hexenmeister")) HexenLila else WaldgruenDunkel,
                                extraContent = {
                                    val canEquip = !alreadyInBook && (!isDruidLevel1 || druidLevel1Count < 1)
                                    Button(
                                        onClick = {
                                            if (canEquip) {
                                                viewModel.addNewSpell(catalogSpell.copy(isPrepared = true, id = java.util.UUID.randomUUID().toString()))
                                            }
                                        },
                                        enabled = canEquip || alreadyInBook,
                                        colors = ButtonDefaults.buttonColors(containerColor = WaldgruenDunkel, disabledContainerColor = TintenSchwarz.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(48.dp)
                                    ) {
                                        val buttonText = if (alreadyInBook) "Bereits ausgerüstet" else if (!canEquip) "Max 1 Druidenzauber" else "+ Ausrüsten"
                                        Text(buttonText, fontSize = 16.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = PergamentHell)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
