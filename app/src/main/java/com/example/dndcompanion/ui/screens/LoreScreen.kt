package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.LoreQuest
import com.example.dndcompanion.ui.viewmodel.LoreQuestStatus
import com.example.dndcompanion.ui.viewmodel.LoreViewModel
import kotlinx.coroutines.launch

private val loreTabs = listOf("Quests", "Karten", "Hausregeln", "Geschichten")

@Composable
fun LoreScreen(loreVm: LoreViewModel) {
    val pagerState = rememberPagerState { loreTabs.size }
    val scope = rememberCoroutineScope()

    PergamentBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = WaldgruenDunkel,
                contentColor = PergamentHell,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = PergamentHell
                    )
                }
            ) {
                loreTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(
                                text = title,
                                fontFamily = Almendra,
                                fontSize = 12.sp,
                                fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selectedContentColor = PergamentHell,
                        unselectedContentColor = PergamentHell.copy(alpha = 0.6f)
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> LoreQuestsTab(loreVm)
                    1 -> LoreMapsTab()
                    2 -> LoreHouserulesTab()
                    3 -> LoreStoriesTab()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoreQuestsTab(loreVm: LoreViewModel) {
    var filterStatus by remember { mutableStateOf<LoreQuestStatus?>(null) }
    var newTitle by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }
    var newStatus by remember { mutableStateOf(LoreQuestStatus.OFFEN) }
    var newLocation by remember { mutableStateOf("") }
    var editingQuest by remember { mutableStateOf<LoreQuest?>(null) }

    val displayed = if (filterStatus == null) loreVm.loreQuests.toList()
    else loreVm.loreQuests.filter { it.status == filterStatus!!.name }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WaldgruenDunkel.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            LoreStatusChip(selected = filterStatus == null, label = "Alle") { filterStatus = null }
            LoreQuestStatus.values().forEach { s ->
                LoreStatusChip(selected = filterStatus == s, label = s.label) { filterStatus = s }
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .weight(1f)
        ) {
            if (filterStatus != LoreQuestStatus.ABGESCHLOSSEN) {
                SteinCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Quest-Titel...", color = TintenSchwarz.copy(alpha = 0.6f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Waldgruen,
                                unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f)
                            ),
                            textStyle = TextStyle(color = TintenSchwarz, fontSize = 15.sp, fontWeight = FontWeight.Bold),
                            singleLine = true
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = newLocation,
                            onValueChange = { newLocation = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ort (optional)...", color = TintenSchwarz.copy(alpha = 0.6f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Waldgruen,
                                unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f)
                            ),
                            textStyle = TextStyle(color = TintenSchwarz, fontSize = 14.sp),
                            singleLine = true
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = newDesc,
                            onValueChange = { newDesc = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp),
                            placeholder = { Text("Beschreibung (optional)...", color = TintenSchwarz.copy(alpha = 0.6f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Waldgruen,
                                unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f)
                            ),
                            textStyle = TextStyle(color = TintenSchwarz, fontSize = 14.sp)
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            LoreQuestStatus.values().forEach { s ->
                                LoreStatusChip(selected = newStatus == s, label = s.label) { newStatus = s }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (newTitle.isNotBlank()) {
                                    loreVm.addLoreQuest(newTitle, newDesc, newStatus, newLocation)
                                    newTitle = ""
                                    newDesc = ""
                                    newLocation = ""
                                    newStatus = LoreQuestStatus.OFFEN
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WaldgruenDunkel),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.End).height(44.dp)
                        ) {
                            Text("Quest hinzufügen", fontFamily = Almendra, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PergamentHell)
                        }
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(displayed, key = { it.id }) { quest ->
                    LoreQuestCard(
                        quest = quest,
                        onEdit = { editingQuest = quest },
                        onDelete = { loreVm.deleteLoreQuest(quest.id) }
                    )
                }
            }
        }
    }

    editingQuest?.let { quest ->
        LoreQuestEditDialog(
            quest = quest,
            onDismiss = { editingQuest = null },
            onSave = { title, desc, status, location ->
                loreVm.updateLoreQuest(quest.id, title, desc, status, location)
                editingQuest = null
            }
        )
    }
}

@Composable
private fun LoreQuestCard(quest: LoreQuest, onEdit: () -> Unit, onDelete: () -> Unit) {
    val status = runCatching { LoreQuestStatus.valueOf(quest.status) }.getOrDefault(LoreQuestStatus.OFFEN)
    val isCompleted = status == LoreQuestStatus.ABGESCHLOSSEN
    val statusColor = when (status) {
        LoreQuestStatus.OFFEN -> Waldgruen
        LoreQuestStatus.IN_BEARBEITUNG -> Bronze
        LoreQuestStatus.ABGESCHLOSSEN -> EisenGrau
    }

    PergamentCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Column(
            modifier = Modifier
                .clickable { onEdit() }
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quest.title,
                        fontFamily = Almendra,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) TintenSchwarz.copy(alpha = 0.5f) else TintenSchwarz,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    if (quest.location.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "📍 ${quest.location}",
                            fontSize = 12.sp,
                            color = TintenBraun.copy(alpha = if (isCompleted) 0.4f else 0.75f)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = status.label,
                            fontSize = 11.sp,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Löschen",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            if (quest.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = quest.description,
                    fontSize = 14.sp,
                    color = if (isCompleted) TintenSchwarz.copy(alpha = 0.4f) else TintenSchwarz.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoreQuestEditDialog(
    quest: LoreQuest,
    onDismiss: () -> Unit,
    onSave: (String, String, LoreQuestStatus, String) -> Unit
) {
    val initStatus = runCatching { LoreQuestStatus.valueOf(quest.status) }.getOrDefault(LoreQuestStatus.OFFEN)
    var title by remember { mutableStateOf(quest.title) }
    var desc by remember { mutableStateOf(quest.description) }
    var status by remember { mutableStateOf(initStatus) }
    var location by remember { mutableStateOf(quest.location) }

    Dialog(onDismissRequest = onDismiss) {
        PergamentCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Quest bearbeiten", fontFamily = Almendra, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TintenSchwarz)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Titel", color = TintenSchwarz.copy(alpha = 0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Waldgruen,
                        unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f)
                    ),
                    textStyle = TextStyle(color = TintenSchwarz, fontWeight = FontWeight.Bold),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ort", color = TintenSchwarz.copy(alpha = 0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Waldgruen,
                        unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f)
                    ),
                    textStyle = TextStyle(color = TintenSchwarz),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    label = { Text("Beschreibung", color = TintenSchwarz.copy(alpha = 0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Waldgruen,
                        unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f)
                    ),
                    textStyle = TextStyle(color = TintenSchwarz)
                )
                Spacer(Modifier.height(8.dp))
                Text("Status", fontSize = 12.sp, color = TintenSchwarz.copy(alpha = 0.6f), fontFamily = Almendra)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LoreQuestStatus.values().forEach { s ->
                        LoreStatusChip(selected = status == s, label = s.label) { status = s }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Abbrechen", fontFamily = Almendra, color = TintenBraun)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { if (title.isNotBlank()) onSave(title, desc, status, location) },
                        colors = ButtonDefaults.buttonColors(containerColor = WaldgruenDunkel),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Speichern", fontFamily = Almendra, color = PergamentHell)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoreStatusChip(selected: Boolean, label: String, onClick: () -> Unit) {
    val bgColor = if (selected) WaldgruenDunkel else TintenSchwarz.copy(alpha = 0.08f)
    val textColor = if (selected) PergamentHell else TintenSchwarz.copy(alpha = 0.7f)
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = textColor,
            fontFamily = Almendra,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun LoreMapsTab() {
    LorePlaceholder("Karten", "Gruppen-Karten – kommt in Task 4.3")
}

@Composable
fun LoreHouserulesTab() {
    LorePlaceholder("Hausregeln", "Hausregeln – kommt in Task 4.4")
}

@Composable
fun LoreStoriesTab() {
    LorePlaceholder("Geschichten", "Gruppen-Geschichten – kommt in Task 4.5")
}

@Composable
private fun LorePlaceholder(title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontFamily = Almendra,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TintenSchwarz
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontFamily = Almendra,
                fontSize = 14.sp,
                color = TintenSchwarz.copy(alpha = 0.6f)
            )
        }
    }
}
