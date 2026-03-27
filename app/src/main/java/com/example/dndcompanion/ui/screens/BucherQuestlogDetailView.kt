package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.viewmodel.GroupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestlogDetailView(viewModel: CharacterViewModel, groupVm: GroupViewModel, onBack: () -> Unit) {
    var newQuestTitle by remember { mutableStateOf("") }
    var newQuestDesc by remember { mutableStateOf("") }
    var showCompleted by remember { mutableStateOf(false) }

    val currentQuests = groupVm.globalQuests.filter { it.isCompleted == showCompleted }
        .sortedByDescending { it.timestamp }

    PergamentBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OchsenblutRot)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Questlog", fontSize = 24.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = Color.White)
            }

            TabRow(
                selectedTabIndex = if (showCompleted) 1 else 0,
                containerColor = OchsenblutRot,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (showCompleted) 1 else 0]),
                        color = Color.White
                    )
                }
            ) {
                Tab(
                    selected = !showCompleted,
                    onClick = { showCompleted = false },
                    text = { Text("Aktive Quests", fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.height(48.dp)
                )
                Tab(
                    selected = showCompleted,
                    onClick = { showCompleted = true },
                    text = { Text("Abgeschlossen", fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.height(48.dp)
                )
            }

            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                if (!showCompleted) {
                    SteinCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            OutlinedTextField(
                                value = newQuestTitle,
                                onValueChange = { newQuestTitle = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Quest-Titel...", color = TintenSchwarz.copy(alpha = 0.6f)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OchsenblutRot,
                                    unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f)
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newQuestDesc,
                                onValueChange = { newQuestDesc = it },
                                modifier = Modifier.fillMaxWidth().height(80.dp),
                                placeholder = { Text("Beschreibung (optional)...", color = TintenSchwarz.copy(alpha = 0.6f)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OchsenblutRot,
                                    unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f)
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 14.sp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (newQuestTitle.isNotBlank()) {
                                        groupVm.addQuest(newQuestTitle, newQuestDesc)
                                        newQuestTitle = ""
                                        newQuestDesc = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.End).height(48.dp)
                            ) {
                                Text("Quest hinzufügen", fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            }
                        }
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(currentQuests) { quest ->
                        PergamentCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = quest.title,
                                        fontFamily = Almendra,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (quest.isCompleted) TintenSchwarz.copy(alpha = 0.5f) else OchsenblutRot,
                                        modifier = Modifier.weight(1f),
                                        textDecoration = if (quest.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Checkbox(
                                        checked = quest.isCompleted,
                                        onCheckedChange = { groupVm.toggleQuestCompletion(quest) },
                                        colors = CheckboxDefaults.colors(checkedColor = WaldgruenDunkel, uncheckedColor = TintenSchwarz.copy(alpha = 0.5f)),
                                        modifier = Modifier.size(48.dp).padding(8.dp)
                                    )
                                    IconButton(
                                        onClick = { groupVm.deleteQuest(quest.id) },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = "Löschen", tint = if (quest.isCompleted) TintenSchwarz.copy(alpha = 0.5f) else Color.Red)
                                    }
                                }
                                if (quest.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = quest.description,
                                        fontSize = 16.sp,
                                        color = if (quest.isCompleted) TintenSchwarz.copy(alpha = 0.5f) else TintenSchwarz,
                                        lineHeight = 24.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
