package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.example.dndcompanion.ui.viewmodel.GroupChatMessage
import com.example.dndcompanion.ui.viewmodel.GroupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatDetailView(viewModel: CharacterViewModel, groupVm: GroupViewModel, onBack: () -> Unit) {
    var newMessageText by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val isOoc = selectedTabIndex == 1
    val listState = rememberLazyListState()

    val filteredMessages = groupVm.groupChatMessages.filter { it.isOoc == isOoc }

    LaunchedEffect(filteredMessages.size) {
        if (filteredMessages.isNotEmpty()) {
            listState.animateScrollToItem(filteredMessages.size - 1)
        }
    }

    PergamentBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TintenSchwarz)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = PergamentHell)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gruppen-Chat", fontSize = 24.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = PergamentHell, modifier = Modifier.weight(1f))

                var showDeleteDialog by remember { mutableStateOf(false) }
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Chat löschen", fontFamily = Almendra, color = OchsenblutRot) },
                        text = { Text("Möchtest du wirklich alle ${if (isOoc) "OOC" else "IC"} Nachrichten löschen? Dies kann nicht rückgängig gemacht werden.", color = TintenSchwarz) },
                        confirmButton = {
                            TextButton(onClick = {
                                groupVm.deleteGroupChat(isOoc)
                                showDeleteDialog = false
                            }) {
                                Text("Löschen", color = OchsenblutRot)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Abbrechen", color = TintenSchwarz)
                            }
                        },
                        containerColor = PergamentHell
                    )
                }

                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Chat löschen", tint = OchsenblutRot)
                }
            }

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = PergamentDunkel,
                contentColor = TintenSchwarz
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("In-Character", fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Out-of-Character", fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(filteredMessages) { message ->
                    GroupChatMessageCard(message)
                }
            }

            SteinCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newMessageText,
                            onValueChange = { newMessageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(if (isOoc) "Schreibe etwas OOC..." else "Sprich als dein Charakter...", color = TintenSchwarz.copy(alpha = 0.6f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isOoc) WaldgruenDunkel else OchsenblutRot,
                                unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f),
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 16.sp),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newMessageText.isNotBlank()) {
                                    groupVm.sendGroupMessage(newMessageText, isOoc)
                                    newMessageText = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(if (isOoc) WaldgruenDunkel else OchsenblutRot, RoundedCornerShape(12.dp))
                        ) {
                            Text("➡️", color = PergamentHell, fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupChatMessageCard(message: GroupChatMessage) {
    val dateStr = remember(message.timestamp) {
        val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        sdf.format(java.util.Date(message.timestamp))
    }

    val nameColor = if (message.isOoc) WaldgruenDunkel else OchsenblutRot

    val authorBgColor = when (message.author.trim()) {
        "Athania" -> Pergament
        "Delat" -> PergamentDunkel
        else -> PergamentHell
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = authorBgColor.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
                Text(
                    text = if (message.isOoc) "[OOC] ${message.author}" else message.author,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Almendra,
                    color = nameColor,
                    fontSize = 16.sp
                )
                Text(dateStr, style = GrenzeGotischSmall, color = TintenSchwarz.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.text,
                fontSize = 16.sp,
                color = TintenSchwarz,
                fontStyle = if (message.isOoc) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                lineHeight = 24.sp
            )
        }
    }
}
