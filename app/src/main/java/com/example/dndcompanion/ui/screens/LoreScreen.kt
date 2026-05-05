package com.example.dndcompanion.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.LoreHouserule
import com.example.dndcompanion.ui.viewmodel.LoreMap
import com.example.dndcompanion.ui.viewmodel.LoreQuest
import com.example.dndcompanion.ui.viewmodel.LoreQuestStatus
import com.example.dndcompanion.ui.viewmodel.LoreStory
import com.example.dndcompanion.ui.viewmodel.LoreViewModel
import com.google.firebase.auth.FirebaseAuth
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.Material3RichText
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val loreTabs = listOf("Quests", "Karten", "Hausregeln", "Geschichten")
private val houseruleCategories = listOf("Allgemein", "Kämpfe", "Magie", "Soziales", "Reisen", "Sonstiges")

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
                    1 -> LoreMapsTab(loreVm)
                    2 -> LoreHouserulesTab(loreVm)
                    3 -> LoreStoriesTab(loreVm)
                }
            }
        }
    }
}

// ─── Quests Tab ─────────────────────────────────────────────────────────────

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

// ─── Maps Tab ────────────────────────────────────────────────────────────────

@Composable
fun LoreMapsTab(loreVm: LoreViewModel) {
    val context = LocalContext.current
    val currentUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMap by remember { mutableStateOf<LoreMap?>(null) }

    var pendingTitle by remember { mutableStateOf("") }
    var pendingDesc by remember { mutableStateOf("") }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            pendingUri = it
            showAddDialog = true
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUri != null) {
            pendingUri = tempCameraUri
            showAddDialog = true
        }
    }

    val cameraPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val file = File(context.cacheDir, "map_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (loreVm.loreMaps.isEmpty() && !loreVm.isUploading) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Noch keine Karten vorhanden",
                    fontFamily = Almendra,
                    fontSize = 16.sp,
                    color = TintenSchwarz.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Füge eine Karte über das + hinzu",
                    fontSize = 13.sp,
                    color = TintenSchwarz.copy(alpha = 0.35f)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(loreVm.loreMaps, key = { it.id }) { map ->
                    LoreMapCard(
                        map = map,
                        currentUid = currentUid,
                        onClick = { selectedMap = map },
                        onDelete = { loreVm.deleteLoreMap(map) }
                    )
                }
            }
        }

        if (loreVm.isUploading) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .background(WaldgruenDunkel.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = PergamentHell,
                        strokeWidth = 2.dp
                    )
                    Text("Karte wird hochgeladen...", color = PergamentHell, fontFamily = Almendra, fontSize = 13.sp)
                }
            }
        }

        FloatingActionButton(
            onClick = { if (!loreVm.isUploading) showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = WaldgruenDunkel,
            contentColor = PergamentHell,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Karte hinzufügen")
        }
    }

    if (showAddDialog) {
        AddMapDialog(
            title = pendingTitle,
            onTitleChange = { pendingTitle = it },
            desc = pendingDesc,
            onDescChange = { pendingDesc = it },
            selectedUri = pendingUri,
            isUploading = loreVm.isUploading,
            onGalleryClick = {
                showAddDialog = false
                galleryLauncher.launch("image/*")
            },
            onCameraClick = {
                showAddDialog = false
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    val file = File(context.cacheDir, "map_${System.currentTimeMillis()}.jpg")
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    tempCameraUri = uri
                    cameraLauncher.launch(uri)
                } else {
                    cameraPermLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onDismiss = {
                showAddDialog = false
                pendingTitle = ""
                pendingDesc = ""
                pendingUri = null
            },
            onUpload = {
                val uri = pendingUri
                if (uri != null && pendingTitle.isNotBlank()) {
                    loreVm.uploadMap(uri, pendingTitle, pendingDesc)
                    pendingTitle = ""
                    pendingDesc = ""
                    pendingUri = null
                    showAddDialog = false
                }
            }
        )
    }

    selectedMap?.let { map ->
        LoreMapFullscreenDialog(map = map, onDismiss = { selectedMap = null })
    }
}

@Composable
private fun LoreMapCard(
    map: LoreMap,
    currentUid: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isOwner = map.uploadedBy == currentUid

    PergamentCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box {
            AsyncImage(
                model = map.url,
                contentDescription = map.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f),
                contentScale = ContentScale.Crop
            )
            // Title overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Text(
                    text = map.title,
                    color = Color.White,
                    fontFamily = Almendra,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = if (isOwner) 28.dp else 0.dp)
                )
            }
            if (isOwner) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Löschen",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddMapDialog(
    title: String,
    onTitleChange: (String) -> Unit,
    desc: String,
    onDescChange: (String) -> Unit,
    selectedUri: Uri?,
    isUploading: Boolean,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onDismiss: () -> Unit,
    onUpload: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        PergamentCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Karte hinzufügen",
                    fontFamily = Almendra,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TintenSchwarz
                )
                Spacer(Modifier.height(12.dp))

                // Image preview or picker buttons
                if (selectedUri != null) {
                    AsyncImage(
                        model = selectedUri,
                        contentDescription = "Vorschau",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onGalleryClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = WaldgruenDunkel)
                        ) {
                            Text("Galerie", fontFamily = Almendra, fontSize = 13.sp)
                        }
                        OutlinedButton(
                            onClick = onCameraClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = WaldgruenDunkel)
                        ) {
                            Text("Kamera", fontFamily = Almendra, fontSize = 13.sp)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(TintenSchwarz.copy(alpha = 0.06f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Bild auswählen", color = TintenSchwarz.copy(alpha = 0.5f), fontFamily = Almendra)
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onGalleryClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = WaldgruenDunkel),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Galerie", fontFamily = Almendra, fontSize = 13.sp, color = PergamentHell)
                                }
                                Button(
                                    onClick = onCameraClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = WaldgruenDunkel),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Kamera", fontFamily = Almendra, fontSize = 13.sp, color = PergamentHell)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Titel *", color = TintenSchwarz.copy(alpha = 0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Waldgruen,
                        unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f)
                    ),
                    textStyle = TextStyle(color = TintenSchwarz, fontWeight = FontWeight.Bold),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = onDescChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp),
                    label = { Text("Beschreibung (optional)", color = TintenSchwarz.copy(alpha = 0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Waldgruen,
                        unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f)
                    ),
                    textStyle = TextStyle(color = TintenSchwarz)
                )
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Abbrechen", fontFamily = Almendra, color = TintenBraun)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onUpload,
                        enabled = selectedUri != null && title.isNotBlank() && !isUploading,
                        colors = ButtonDefaults.buttonColors(containerColor = WaldgruenDunkel),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = PergamentHell,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Hochladen", fontFamily = Almendra, color = PergamentHell)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoreMapFullscreenDialog(map: LoreMap, onDismiss: () -> Unit) {
    var scale by remember { mutableStateOf(1f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = map.url,
                contentDescription = map.title,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                        }
                    }
                    .scale(scale),
                contentScale = ContentScale.Fit
            )

            // Title/description overlay
            if (map.title.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column {
                        Text(
                            text = map.title,
                            color = Color.White,
                            fontFamily = Almendra,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (map.description.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = map.description,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "Schließen",
                    tint = Color.White
                )
            }
        }
    }
}

// ─── Houserules Tab ──────────────────────────────────────────────────────────

@Composable
fun LoreHouserulesTab(loreVm: LoreViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var filterCategory by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingHouserule by remember { mutableStateOf<LoreHouserule?>(null) }
    var expandedId by remember { mutableStateOf<String?>(null) }

    val filtered = loreVm.loreHouserules.filter { hr ->
        (filterCategory == null || hr.category == filterCategory) &&
        (searchQuery.isBlank() ||
            hr.title.contains(searchQuery, ignoreCase = true) ||
            hr.ruleText.contains(searchQuery, ignoreCase = true))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Hausregeln durchsuchen...", color = TintenSchwarz.copy(alpha = 0.5f)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TintenSchwarz.copy(alpha = 0.6f))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Löschen", tint = TintenSchwarz.copy(alpha = 0.6f))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Waldgruen,
                    unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.4f)
                ),
                textStyle = TextStyle(color = TintenSchwarz, fontSize = 14.sp),
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LoreStatusChip(selected = filterCategory == null, label = "Alle") { filterCategory = null }
                houseruleCategories.forEach { cat ->
                    LoreStatusChip(selected = filterCategory == cat, label = cat) {
                        filterCategory = if (filterCategory == cat) null else cat
                    }
                }
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (searchQuery.isNotBlank() || filterCategory != null)
                                "Keine Ergebnisse gefunden"
                            else
                                "Noch keine Hausregeln vorhanden",
                            fontFamily = Almendra,
                            fontSize = 16.sp,
                            color = TintenSchwarz.copy(alpha = 0.5f)
                        )
                        if (searchQuery.isBlank() && filterCategory == null) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Füge eine Hausregel über das + hinzu",
                                fontSize = 13.sp,
                                color = TintenSchwarz.copy(alpha = 0.35f)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {
                    items(filtered, key = { it.id }) { hr ->
                        LoreHouseruleCard(
                            houserule = hr,
                            isExpanded = expandedId == hr.id,
                            onToggle = { expandedId = if (expandedId == hr.id) null else hr.id },
                            onEdit = { editingHouserule = hr },
                            onDelete = { loreVm.deleteLoreHouserule(hr.id) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = WaldgruenDunkel,
            contentColor = PergamentHell,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Hausregel hinzufügen")
        }
    }

    if (showAddDialog) {
        HouseruleDialog(
            houserule = null,
            onDismiss = { showAddDialog = false },
            onSave = { title, ruleText, category ->
                loreVm.addLoreHouserule(title, ruleText, category)
                showAddDialog = false
            }
        )
    }

    editingHouserule?.let { hr ->
        HouseruleDialog(
            houserule = hr,
            onDismiss = { editingHouserule = null },
            onSave = { title, ruleText, category ->
                loreVm.updateLoreHouserule(hr.id, title, ruleText, category)
                editingHouserule = null
            }
        )
    }
}

@Composable
private fun LoreHouseruleCard(
    houserule: LoreHouserule,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    PergamentCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .clickable { onToggle() }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = houserule.title,
                        fontFamily = Almendra,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TintenSchwarz
                    )
                    Spacer(Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .background(WaldgruenDunkel.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = houserule.category,
                            fontSize = 11.sp,
                            color = WaldgruenDunkel,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Bearbeiten",
                            tint = TintenBraun.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Löschen",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (houserule.ruleText.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                if (isExpanded) {
                    HorizontalDivider(color = TintenSchwarz.copy(alpha = 0.15f))
                    Spacer(Modifier.height(8.dp))
                    Material3RichText(modifier = Modifier.fillMaxWidth()) {
                        Markdown(houserule.ruleText)
                    }
                } else {
                    Text(
                        text = houserule.ruleText,
                        fontSize = 13.sp,
                        color = TintenSchwarz.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun HouseruleDialog(
    houserule: LoreHouserule?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(houserule?.title ?: "") }
    var ruleText by remember { mutableStateOf(houserule?.ruleText ?: "") }
    var category by remember { mutableStateOf(houserule?.category ?: "Allgemein") }

    Dialog(onDismissRequest = onDismiss) {
        PergamentCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (houserule == null) "Hausregel hinzufügen" else "Hausregel bearbeiten",
                    fontFamily = Almendra,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TintenSchwarz
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Titel *", color = TintenSchwarz.copy(alpha = 0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Waldgruen,
                        unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f)
                    ),
                    textStyle = TextStyle(color = TintenSchwarz, fontWeight = FontWeight.Bold),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = ruleText,
                    onValueChange = { ruleText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    label = { Text("Regeltext (Markdown möglich)", color = TintenSchwarz.copy(alpha = 0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Waldgruen,
                        unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f)
                    ),
                    textStyle = TextStyle(color = TintenSchwarz, fontSize = 14.sp)
                )
                Spacer(Modifier.height(8.dp))
                Text("Kategorie", fontSize = 12.sp, color = TintenSchwarz.copy(alpha = 0.6f), fontFamily = Almendra)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    houseruleCategories.forEach { cat ->
                        LoreStatusChip(selected = category == cat, label = cat) { category = cat }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Abbrechen", fontFamily = Almendra, color = TintenBraun)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { if (title.isNotBlank()) onSave(title, ruleText, category) },
                        enabled = title.isNotBlank(),
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

// ─── Placeholder Tabs ────────────────────────────────────────────────────────

// ─── Stories Tab ─────────────────────────────────────────────────────────────

@Composable
fun LoreStoriesTab(loreVm: LoreViewModel) {
    val currentUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val defaultAuthor = remember {
        FirebaseAuth.getInstance().currentUser?.displayName
            ?: FirebaseAuth.getInstance().currentUser?.email?.substringBefore("@")
            ?: ""
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingStory by remember { mutableStateOf<LoreStory?>(null) }
    var selectedStory by remember { mutableStateOf<LoreStory?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (loreVm.loreStories.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Noch keine Geschichten vorhanden",
                    fontFamily = Almendra,
                    fontSize = 16.sp,
                    color = TintenSchwarz.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Füge eine Geschichte über das + hinzu",
                    fontSize = 13.sp,
                    color = TintenSchwarz.copy(alpha = 0.35f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(loreVm.loreStories, key = { it.id }) { story ->
                    LoreStoryCard(
                        story = story,
                        isOwner = story.createdBy == currentUid,
                        onClick = { selectedStory = story },
                        onEdit = { editingStory = story },
                        onDelete = { loreVm.deleteLoreStory(story.id) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = WaldgruenDunkel,
            contentColor = PergamentHell,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Geschichte hinzufügen")
        }
    }

    if (showAddDialog) {
        StoryDialog(
            story = null,
            defaultAuthor = defaultAuthor,
            onDismiss = { showAddDialog = false },
            onSave = { title, text, author ->
                loreVm.addLoreStory(title, text, author)
                showAddDialog = false
            }
        )
    }

    editingStory?.let { story ->
        StoryDialog(
            story = story,
            defaultAuthor = defaultAuthor,
            onDismiss = { editingStory = null },
            onSave = { title, text, author ->
                loreVm.updateLoreStory(story.id, title, text, author)
                editingStory = null
            }
        )
    }

    selectedStory?.let { story ->
        StoryDetailDialog(story = story, onDismiss = { selectedStory = null })
    }
}

@Composable
private fun LoreStoryCard(
    story: LoreStory,
    isOwner: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(story.timestamp) {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(story.timestamp))
    }

    PergamentCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .clickable { onClick() }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = story.title,
                        fontFamily = Almendra,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TintenSchwarz
                    )
                    Spacer(Modifier.height(3.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (story.author.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .background(WaldgruenDunkel.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = story.author,
                                    fontSize = 11.sp,
                                    color = WaldgruenDunkel,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Text(dateStr, fontSize = 11.sp, color = TintenSchwarz.copy(alpha = 0.5f))
                    }
                }
                if (isOwner) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Bearbeiten",
                                tint = TintenBraun.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Löschen",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (story.text.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = story.text,
                    fontSize = 13.sp,
                    color = TintenSchwarz.copy(alpha = 0.65f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
private fun StoryDetailDialog(story: LoreStory, onDismiss: () -> Unit) {
    val dateStr = remember(story.timestamp) {
        SimpleDateFormat("dd.MM.yyyy · HH:mm", Locale.getDefault()).format(Date(story.timestamp))
    }

    Dialog(onDismissRequest = onDismiss) {
        PergamentCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = story.title,
                            fontFamily = Almendra,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TintenSchwarz
                        )
                        Spacer(Modifier.height(4.dp))
                        if (story.author.isNotBlank()) {
                            Text(
                                text = story.author,
                                fontSize = 13.sp,
                                color = WaldgruenDunkel,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(dateStr, fontSize = 11.sp, color = TintenSchwarz.copy(alpha = 0.5f))
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Schließen",
                            tint = TintenSchwarz.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = TintenSchwarz.copy(alpha = 0.15f))
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (story.text.isNotBlank()) {
                        Material3RichText(modifier = Modifier.fillMaxWidth()) {
                            Markdown(story.text)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryDialog(
    story: LoreStory?,
    defaultAuthor: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(story?.title ?: "") }
    var text by remember { mutableStateOf(story?.text ?: "") }
    var author by remember { mutableStateOf(story?.author ?: defaultAuthor) }

    Dialog(onDismissRequest = onDismiss) {
        PergamentCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (story == null) "Geschichte hinzufügen" else "Geschichte bearbeiten",
                    fontFamily = Almendra,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TintenSchwarz
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Titel *", color = TintenSchwarz.copy(alpha = 0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Waldgruen,
                        unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f)
                    ),
                    textStyle = TextStyle(color = TintenSchwarz, fontWeight = FontWeight.Bold),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Autor", color = TintenSchwarz.copy(alpha = 0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Waldgruen,
                        unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f)
                    ),
                    textStyle = TextStyle(color = TintenSchwarz),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    label = { Text("Text (Markdown möglich)", color = TintenSchwarz.copy(alpha = 0.7f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Waldgruen,
                        unfocusedBorderColor = TintenSchwarz.copy(alpha = 0.5f)
                    ),
                    textStyle = TextStyle(color = TintenSchwarz, fontSize = 14.sp)
                )
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Abbrechen", fontFamily = Almendra, color = TintenBraun)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { if (title.isNotBlank()) onSave(title, text, author) },
                        enabled = title.isNotBlank(),
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
