package com.example.dndcompanion.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.* 
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.viewmodel.ChatMessage
import com.example.dndcompanion.ui.viewmodel.FaqItem
import com.example.dndcompanion.ui.theme.BlauDunkel
import com.example.dndcompanion.ui.theme.BlauHell
import com.example.dndcompanion.ui.theme.PinkDunkel
import com.example.dndcompanion.ui.theme.PinkHell
import com.example.dndcompanion.ui.theme.GelbSand
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.Material3RichText
import androidx.compose.runtime.CompositionLocalProvider
import kotlinx.coroutines.delay

@Composable
fun HelpScreen(viewModel: CharacterViewModel, onNavigateToRulebook: (String, String?) -> Unit = { _, _ -> }) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Regel-Chat", "Mein FAQ")

    Column(modifier = Modifier.fillMaxSize().background(GelbSand)) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = GelbSand,
            contentColor = BlauDunkel
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold) },
                    selectedContentColor = PinkDunkel,
                    unselectedContentColor = BlauDunkel
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> ChatView(viewModel, onNavigateToRulebook)
                1 -> FaqView(viewModel)
            }
        }
    }
}

@Composable
fun ChatView(viewModel: CharacterViewModel, onNavigateToRulebook: (String, String?) -> Unit) {
    var inputText by remember { mutableStateOf("") }
    // Speichert die Nachricht, die wir gerade ins FAQ aufnehmen wollen
    var messageToFaq by remember { mutableStateOf<ChatMessage?>(null) }
    // NEU: Bestätigungsdialog für Chat-Reset
    var showResetDialog by remember { mutableStateOf(false) }

    // NEU: Auto-Scroll
    val listState = rememberLazyListState()
    LaunchedEffect(viewModel.chatHistory.size) {
        if (viewModel.chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.chatHistory.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).imePadding()) {
        // Status-Leiste für Modell und Limits
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Modell: ${viewModel.currentUsedModel}",
                fontSize = 12.sp,
                color = BlauDunkel,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Gemini Slots: ${viewModel.geminiMax - viewModel.geminiUsesToday} / ${viewModel.geminiMax}",
                fontSize = 12.sp,
                color = if (viewModel.geminiUsesToday >= viewModel.geminiMax) Color.Red else PinkDunkel
            )
        }
        LinearProgressIndicator(
            progress = { (viewModel.geminiMax - viewModel.geminiUsesToday).toFloat() / viewModel.geminiMax.toFloat() },
            modifier = Modifier.fillMaxWidth().height(4.dp).padding(bottom = 12.dp),
            color = PinkDunkel,
            trackColor = BlauHell
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            reverseLayout = false
        ) {
            items(viewModel.chatHistory, key = { it.id }) { message ->
                ChatBubble(
                    message = message,
                    onSaveToFaq = { messageToFaq = message },
                    onNavigateToRulebook = onNavigateToRulebook
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat zurücksetzen Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Chat zurücksetzen", tint = Color.White)
                Spacer(Modifier.width(4.dp))
                Text("Chat zurücksetzen", color = Color.White)
            }
        }

        // Eingabefeld
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                maxLines = 4,
                placeholder = { Text("z.B. Wie funktioniert Zeichen des Jägers?") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PinkDunkel,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessageToBot(inputText.trim())
                        inputText = ""
                    }
                },
                enabled = inputText.isNotBlank(),
                modifier = Modifier.background(
                    if (inputText.isNotBlank()) BlauDunkel else BlauDunkel.copy(alpha = 0.4f),
                    RoundedCornerShape(8.dp)
                )
            ) {
                Icon(Icons.Default.Send, contentDescription = "Senden", tint = Color.White)
            }
        }
    }

    // Popup-Dialog zum Anpassen der FAQ-Antwort
    if (messageToFaq != null) {
        var faqQuestion by remember { mutableStateOf(messageToFaq!!.chapterSearchTerm ?: "Regelklärung") }
        var faqAnswer by remember { mutableStateOf(messageToFaq!!.localText ?: messageToFaq!!.externalText ?: messageToFaq!!.text) }

        AlertDialog(
            onDismissRequest = { messageToFaq = null },
            containerColor = GelbSand,
            title = { Text("Ins FAQ aufnehmen", color = BlauDunkel) },
            text = {
                Column {
                    Text("Passe die Frage und Antwort so an, dass sie kurz und prägnant für dein Regelbuch sind.", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = faqQuestion,
                        onValueChange = { faqQuestion = it },
                        label = { Text("Schlagwort / Frage") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = faqAnswer,
                        onValueChange = { faqAnswer = it },
                        label = { Text("Zusammenfassung (Antwort)") },
                        modifier = Modifier.height(120.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (faqQuestion.isNotBlank() && faqAnswer.isNotBlank()) {
                            viewModel.addChatToFaq(faqQuestion, faqAnswer)
                            messageToFaq = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel)
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToFaq = null }) { Text("Abbrechen", color = BlauDunkel) }
            }
        )
    }

    // NEU: Bestätigungsdialog für Chat-Reset
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = GelbSand,
            title = { Text("Chat zurücksetzen?", color = BlauDunkel, fontWeight = FontWeight.Bold) },
            text = { Text("Alle Nachrichten werden unwiderruflich gelöscht.", color = BlauDunkel) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetChat()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel)
                ) {
                    Text("Ja, löschen", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Abbrechen", color = BlauDunkel)
                }
            }
        )
    }
}

// NEU: Animierter Typing-Indikator
@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(12.dp)
    ) {
        for (i in 0..2) {
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = LinearEasing, delayMillis = i * 200),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$i"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(alpha)
                    .background(Color.White, CircleShape)
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, onSaveToFaq: () -> Unit, onNavigateToRulebook: (String, String?) -> Unit) {
    val isUser = message.isUser
    val isLoading = !isUser && message.text == "... analysiere Regeln ..." && message.localText == null && message.externalText == null
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            if (isLoading) {
                // NEU: Animierter Typing-Indikator statt statischer Text
                Card(
                    colors = CardDefaults.cardColors(containerColor = BlauHell),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.widthIn(max = 300.dp)
                ) {
                    TypingIndicator()
                }
            } else if (isUser || (message.localText == null && message.externalText == null)) {
                // Standard-Anzeige für User oder Fehler-Rohtext
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isUser) BlauDunkel else BlauHell),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.widthIn(max = 300.dp)
                ) {
                    Text(
                        text = message.text,
                        color = Color.White,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 15.sp
                    )
                }
            } else {
                // RAG-Anzeige (Split-Screen Ansicht)
                Column(modifier = Modifier.widthIn(max = 320.dp)) {
                    
                    // --- LOKALER TEIL (Handbuch) ---
                    if (!message.localText.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = PinkDunkel)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "LOKALES REGELWERK",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = PinkDunkel
                            )
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, PinkDunkel),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                        ) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                // Rendert Markdown (macht Text fett, erstellt Listen etc.)
                                CompositionLocalProvider(LocalContentColor provides BlauDunkel) {
                                    Material3RichText { Markdown(content = message.localText) }
                                }
                            }
                        }

                        // KAPITEL-LINK BADGE
                        if (!message.chapterLink.isNullOrBlank() && message.chapterLink != "null") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(bottom = 12.dp, start = 4.dp)
                                    .background(Color(0xFF2E7D32), RoundedCornerShape(16.dp)) // Dunkelgrün wie im Regelwerk
                                    .clickable { onNavigateToRulebook(message.chapterLink, message.chapterSearchTerm) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("📖", fontSize = 12.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Quelle: ${message.chapterLink}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    // --- EXTERNER TEIL (Gemini) ---
                    if (!message.externalText.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp), tint = BlauDunkel)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "GEMINI WISSEN (D&D 2024)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = BlauDunkel
                            )
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BlauHell),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                // Rendert Markdown in Weiß auf dem blauen Hintergrund
                                CompositionLocalProvider(LocalContentColor provides Color.White) {
                                    Material3RichText { Markdown(content = message.externalText) }
                                }
                            }
                        }
                    }
                }
            }

            // Bot-Nachrichten bekommen den "Ins FAQ"-Button (aber nicht während des Ladens)
            if (!isUser && !isLoading) {
                TextButton(
                    onClick = onSaveToFaq, 
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = PinkDunkel)
                    Spacer(Modifier.width(4.dp))
                    Text("Ins FAQ", color = PinkDunkel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FaqView(viewModel: CharacterViewModel) {
    // NEU: FAQ Edit-Dialog State
    var editingFaq by remember { mutableStateOf<FaqItem?>(null) }

    if (viewModel.faqList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Dein FAQ ist noch leer. Frag den Bot nach Regeln!", color = BlauDunkel)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(viewModel.faqList) { faq ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = faq.question, fontWeight = FontWeight.Bold, color = PinkDunkel, fontSize = 18.sp, modifier = Modifier.weight(1f))
                            Row {
                                IconButton(
                                    onClick = { editingFaq = faq },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Bearbeiten", tint = BlauDunkel)
                                }
                                Spacer(Modifier.width(8.dp))
                                IconButton(
                                    onClick = { viewModel.removeFaq(faq) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Löschen", tint = Color.Gray)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // NEU: Markdown-Rendering statt Plaintext
                        CompositionLocalProvider(LocalContentColor provides BlauDunkel) {
                            Material3RichText { Markdown(content = faq.answer) }
                        }
                    }
                }
            }
        }
    }

    // NEU: Edit-Dialog für FAQ-Einträge
    if (editingFaq != null) {
        var editQuestion by remember(editingFaq) { mutableStateOf(editingFaq!!.question) }
        var editAnswer by remember(editingFaq) { mutableStateOf(editingFaq!!.answer) }

        AlertDialog(
            onDismissRequest = { editingFaq = null },
            containerColor = GelbSand,
            title = { Text("FAQ bearbeiten", color = BlauDunkel, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editQuestion,
                        onValueChange = { editQuestion = it },
                        label = { Text("Frage / Schlagwort") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editAnswer,
                        onValueChange = { editAnswer = it },
                        label = { Text("Antwort") },
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editQuestion.isNotBlank() && editAnswer.isNotBlank()) {
                            viewModel.updateFaq(editingFaq!!, editQuestion, editAnswer)
                            editingFaq = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel)
                ) {
                    Text("Speichern", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingFaq = null }) {
                    Text("Abbrechen", color = BlauDunkel)
                }
            }
        )
    }
}