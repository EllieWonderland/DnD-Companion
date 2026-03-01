package com.example.dndcompanion.ui.theme.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.* 
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.theme.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.theme.viewmodel.ChatMessage
import com.example.dndcompanion.ui.theme.BlauDunkel
import com.example.dndcompanion.ui.theme.BlauHell
import com.example.dndcompanion.ui.theme.PinkDunkel
import com.example.dndcompanion.ui.theme.PinkHell
import com.example.dndcompanion.ui.theme.GelbSand

@Composable
fun HelpScreen(viewModel: CharacterViewModel) {
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
                0 -> ChatView(viewModel)
                1 -> FaqView(viewModel)
            }
        }
    }
}

@Composable
fun ChatView(viewModel: CharacterViewModel) {
    var inputText by remember { mutableStateOf("") }
    // Speichert die Nachricht, die wir gerade ins FAQ aufnehmen wollen
    var messageToFaq by remember { mutableStateOf<ChatMessage?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
        // Chat-Verlauf
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            reverseLayout = false // Zeigt neueste unten an
        ) {
            items(viewModel.chatHistory) { message ->
                ChatBubble(
                    message = message,
                    onSaveToFaq = { messageToFaq = message }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // NEU: Chat zurücksetzen Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End // Rechts ausgerichtet
        ) {
            Button(
                onClick = { viewModel.resetChat() },
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Frage nach einer Regel...") },
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
                modifier = Modifier.background(BlauDunkel, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Send, contentDescription = "Senden", tint = Color.White)
            }
        }
    }

    // Popup-Dialog zum Anpassen der FAQ-Antwort
    if (messageToFaq != null) {
        var faqQuestion by remember { mutableStateOf("Regelklärung") }
        var faqAnswer by remember { mutableStateOf(messageToFaq!!.text) }

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
}

@Composable
fun ChatBubble(message: ChatMessage, onSaveToFaq: () -> Unit) {
    val isUser = message.isUser
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isUser) BlauDunkel else BlauHell),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.text,
                    color = Color.White,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 16.sp
                )
            }
            // Bot-Nachrichten bekommen den "Ins FAQ"-Button
            if (!isUser) {
                TextButton(onClick = onSaveToFaq, contentPadding = PaddingValues(0.dp)) {
                    Text("+ Ins FAQ", color = PinkDunkel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FaqView(viewModel: CharacterViewModel) {
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
                            IconButton(
                                onClick = { viewModel.removeFaq(faq) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Löschen", tint = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = faq.answer, color = BlauDunkel, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}