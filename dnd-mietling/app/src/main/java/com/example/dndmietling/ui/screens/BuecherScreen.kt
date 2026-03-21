package com.example.dndmietling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndmietling.data.BookEntry
import com.example.dndmietling.data.GroupChatMessage
import com.example.dndmietling.ui.theme.*
import com.example.dndmietling.ui.viewmodel.MietlingViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BuecherScreen(viewModel: MietlingViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().background(Pergament)) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = WaldgruenDunkel,
            contentColor = PergamentHell
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("📋 Notizen") },
                selectedContentColor = WaldGold,
                unselectedContentColor = PergamentDunkel
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("😤 Buch des Grolls") },
                selectedContentColor = WaldGold,
                unselectedContentColor = PergamentDunkel
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("💬 Gruppen-Chat") },
                selectedContentColor = WaldGold,
                unselectedContentColor = PergamentDunkel
            )
        }

        when (selectedTab) {
            0 -> NotesTab(
                entries = viewModel.publicGeneralBookEntries,
                authorName = viewModel.currentCharacter?.displayName ?: "Mietling",
                onAdd = { text -> viewModel.addPublicNote("publicGeneralNotes", text) }
            )
            1 -> NotesTab(
                entries = viewModel.publicGrudgeBookEntries,
                authorName = viewModel.currentCharacter?.displayName ?: "Mietling",
                onAdd = { text -> viewModel.addPublicNote("publicGrudgeNotes", text) }
            )
            2 -> ChatTab(viewModel = viewModel)
        }
    }
}

@Composable
private fun NotesTab(
    entries: List<BookEntry>,
    authorName: String,
    onAdd: (String) -> Unit
) {
    var newText by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat("dd.MM. HH:mm", Locale.GERMAN) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Eintrag-Liste
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (entries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Noch keine Einträge", color = EisenGrau, fontSize = 14.sp)
                    }
                }
            } else {
                items(entries, key = { it.id }) { entry ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = PergamentHell)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    entry.author,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Waldgruen
                                )
                                Text(
                                    dateFormat.format(Date(entry.timestamp)),
                                    fontSize = 11.sp,
                                    color = EisenGrau
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(entry.text, fontSize = 14.sp, color = TintenSchwarz)
                        }
                    }
                }
            }
        }

        // Eingabe
        HorizontalDivider(color = PergamentDunkel)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PergamentHell)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newText,
                onValueChange = { newText = it },
                placeholder = { Text("Neuer Eintrag…", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                minLines = 1,
                maxLines = 4,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Waldgruen,
                    unfocusedBorderColor = PergamentDunkel
                )
            )
            IconButton(
                onClick = {
                    if (newText.isNotBlank()) {
                        onAdd(newText)
                        newText = ""
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Eintragen", tint = Waldgruen)
            }
        }
    }
}

@Composable
private fun ChatTab(viewModel: MietlingViewModel) {
    var messageText by remember { mutableStateOf("") }
    var isOoc by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val messages = viewModel.chatMessages
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.GERMAN) }
    val authorName = viewModel.currentCharacter?.displayName ?: "Mietling"

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // OOC Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PergamentDunkel.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("IC", fontSize = 13.sp, color = if (!isOoc) Waldgruen else TintenBraun, fontWeight = if (!isOoc) FontWeight.Bold else FontWeight.Normal)
            Switch(
                checked = isOoc,
                onCheckedChange = { isOoc = it },
                colors = SwitchDefaults.colors(checkedThumbColor = HexenLila, checkedTrackColor = HexenLila.copy(alpha = 0.5f))
            )
            Text("OOC", fontSize = 13.sp, color = if (isOoc) HexenLila else TintenBraun, fontWeight = if (isOoc) FontWeight.Bold else FontWeight.Normal)
            Text("(Out of Character)", fontSize = 11.sp, color = EisenGrau)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Noch keine Nachrichten", color = EisenGrau, fontSize = 14.sp)
                    }
                }
            }
            items(messages, key = { it.id }) { msg ->
                val isOwn = msg.author == authorName
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 12.dp, topEnd = 12.dp,
                            bottomStart = if (isOwn) 12.dp else 2.dp,
                            bottomEnd = if (isOwn) 2.dp else 12.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                msg.isOoc -> HexenLila.copy(alpha = 0.15f)
                                isOwn -> Waldgruen.copy(alpha = 0.15f)
                                else -> PergamentHell
                            }
                        ),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(msg.author, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Waldgruen)
                                if (msg.isOoc) {
                                    Text("[OOC]", fontSize = 10.sp, color = HexenLila)
                                }
                                Text(dateFormat.format(Date(msg.timestamp)), fontSize = 10.sp, color = EisenGrau)
                            }
                            Text(msg.text, fontSize = 14.sp, color = TintenSchwarz)
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = PergamentDunkel)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PergamentHell)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text(if (isOoc) "[OOC] Nachricht…" else "Nachricht…", fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                maxLines = 3,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isOoc) HexenLila else Waldgruen,
                    unfocusedBorderColor = PergamentDunkel
                )
            )
            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendChatMessage(messageText, isOoc)
                        messageText = ""
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Senden", tint = if (isOoc) HexenLila else Waldgruen)
            }
        }
    }
}
