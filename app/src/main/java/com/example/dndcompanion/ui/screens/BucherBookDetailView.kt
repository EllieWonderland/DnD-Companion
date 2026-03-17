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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.BookEntry
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.viewmodel.GroupViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailView(bookType: BookType, viewModel: CharacterViewModel, groupVm: GroupViewModel, onBack: () -> Unit) {
    val privateEntries = if (bookType == BookType.GENERAL) viewModel.generalBookEntries else viewModel.grudgeBookEntries
    val publicEntries = if (bookType == BookType.GENERAL) groupVm.publicGeneralBookEntries else groupVm.publicGrudgeBookEntries

    val title = if (bookType == BookType.GENERAL) "Notizbuch" else "Buch des Grolls"
    val tintColor = if (bookType == BookType.GENERAL) WaldgruenDunkel else OchsenblutRot

    var showPublicTab by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var newEntryText by remember { mutableStateOf("") }
    var editingEntryId by remember { mutableStateOf<String?>(null) }
    var editText by remember { mutableStateOf("") }

    val currentEntries = if (showPublicTab) publicEntries else privateEntries
    val filteredEntries = currentEntries.filter { it.text.contains(searchQuery, ignoreCase = true) }

    PergamentBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tintColor)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = WaldGold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontSize = 24.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = WaldGold)
            }

            TabRow(
                selectedTabIndex = if (showPublicTab) 1 else 0,
                containerColor = tintColor,
                contentColor = WaldGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[if (showPublicTab) 1 else 0]),
                        color = WaldGold
                    )
                }
            ) {
                Tab(
                    selected = !showPublicTab,
                    onClick = { showPublicTab = false },
                    text = { Text("Persönlich", fontFamily = Almendra, fontWeight = FontWeight.Bold) },
                    selectedContentColor = WaldGold,
                    unselectedContentColor = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.height(48.dp)
                )
                Tab(
                    selected = showPublicTab,
                    onClick = { showPublicTab = true },
                    text = { Text("Gruppe", fontFamily = Almendra, fontWeight = FontWeight.Bold) },
                    selectedContentColor = WaldGold,
                    unselectedContentColor = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.height(48.dp)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(if (showPublicTab) "Gruppen-Einträge durchsuchen..." else "Eigene Einträge durchsuchen...", color = TintenSchwarz.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = tintColor) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Suchen löschen", tint = tintColor)
                            }
                        }
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 16.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = tintColor,
                        unfocusedBorderColor = tintColor.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (editingEntryId == null) {
                    SteinCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            OutlinedTextField(
                                value = newEntryText,
                                onValueChange = { newEntryText = it },
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                placeholder = { Text(if (showPublicTab) "Neue globale Notiz..." else "Neuer persönlicher Eintrag...", color = TintenSchwarz.copy(alpha = 0.6f)) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 16.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = tintColor,
                                    unfocusedBorderColor = tintColor.copy(alpha = 0.5f)
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    if (newEntryText.isNotBlank()) {
                                        if (showPublicTab) {
                                            val tempEntry = BookEntry(text = newEntryText.trim(), isPublic = true)
                                            if (bookType == BookType.GENERAL) {
                                                groupVm.addPublicGeneralBookEntry(tempEntry.id, newEntryText)
                                            } else {
                                                groupVm.addPublicGrudgeBookEntry(tempEntry.id, newEntryText)
                                            }
                                        } else {
                                            if (bookType == BookType.GENERAL) {
                                                viewModel.addGeneralBookEntry(newEntryText, false)
                                            } else {
                                                viewModel.addGrudgeBookEntry(newEntryText, false)
                                            }
                                        }
                                        newEntryText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = tintColor),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.End).height(48.dp)
                            ) {
                                Text("Speichern", fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                } else {
                    SteinCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Eintrag bearbeiten", color = tintColor, fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = editText,
                                onValueChange = { editText = it },
                                modifier = Modifier.fillMaxWidth().height(100.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(color = TintenSchwarz, fontSize = 16.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = tintColor,
                                    unfocusedBorderColor = tintColor.copy(alpha = 0.5f)
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { editingEntryId = null }, modifier = Modifier.height(48.dp)) {
                                    Text("Abbrechen", color = TintenSchwarz, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (editText.isNotBlank()) {
                                            if (showPublicTab) {
                                                if (bookType == BookType.GENERAL) {
                                                    groupVm.updatePublicGeneralBookEntry(editingEntryId!!, editText)
                                                } else {
                                                    groupVm.updatePublicGrudgeBookEntry(editingEntryId!!, editText)
                                                }
                                            } else {
                                                if (bookType == BookType.GENERAL) {
                                                    viewModel.updateGeneralBookEntry(editingEntryId!!, editText, false)
                                                } else {
                                                    viewModel.updateGrudgeBookEntry(editingEntryId!!, editText, false)
                                                }
                                            }
                                            editingEntryId = null
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = tintColor),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Text("Aktualisieren", fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredEntries) { entry ->
                        BookEntryCard(
                            entry = entry,
                            bookType = bookType,
                            onEdit = {
                                editingEntryId = entry.id
                                editText = entry.text
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookEntryCard(entry: BookEntry, bookType: BookType, onEdit: () -> Unit) {
    val dateStr = remember(entry.timestamp) {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(entry.timestamp))
    }

    PergamentCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(dateStr, style = GrenzeGotischSmall, color = TintenSchwarz.copy(alpha = 0.7f))
                IconButton(onClick = onEdit, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Bearbeiten", tint = TintenSchwarz.copy(alpha = 0.6f))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(entry.text, fontSize = 16.sp, color = TintenSchwarz, lineHeight = 24.sp)
        }
    }
}
