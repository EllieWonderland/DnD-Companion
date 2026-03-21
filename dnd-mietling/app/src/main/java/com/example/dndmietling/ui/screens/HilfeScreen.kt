package com.example.dndmietling.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.Material3RichText
import com.example.dndmietling.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val HILFE_CHAPTERS = listOf(
    "Gameplay & Grundregeln" to "Rules/Handbuch/Kapitel/kapitel1_gameplay.md",
    "Völker & Herkunft" to "Rules/Handbuch/Kapitel/kapitel2_races.md",
    "Klassen" to "Rules/Handbuch/Kapitel/kapitel3_classes.md",
    "Ursprünge & Hintergründe" to "Rules/Handbuch/Kapitel/kapitel4_origins.md",
    "Talente & Fertigkeiten" to "Rules/Handbuch/Kapitel/kapitel5_talente.md",
    "Ausrüstung" to "Rules/Handbuch/Kapitel/kapitel6_equipment.md",
    "Zauber & Magie" to "Rules/Handbuch/Kapitel/kapitel7_spells.md",
    "Kampf & Zustände" to "Rules/Handbuch/Kapitel/kapitel8_combat_conditions.md"
)

@Composable
fun HilfeScreen() {
    val context = LocalContext.current
    var selectedChapter by remember { mutableStateOf<Pair<String, String>?>(null) }
    var markdownContent by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(selectedChapter) {
        val chapter = selectedChapter ?: return@LaunchedEffect
        isLoading = true
        markdownContent = withContext(Dispatchers.IO) {
            try {
                context.assets.open(chapter.second).bufferedReader().readText()
            } catch (e: Exception) {
                "# ${chapter.first}\n\nDieses Kapitel ist noch nicht verfügbar."
            }
        }
        isLoading = false
    }

    if (selectedChapter != null) {
        // Kapitel-Ansicht
        Column(modifier = Modifier.fillMaxSize().background(Pergament)) {
            // Zurück-Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WaldgruenDunkel)
                    .clickable { selectedChapter = null; markdownContent = null }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = PergamentHell)
                Text(
                    selectedChapter!!.first,
                    color = WaldGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Waldgruen)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    markdownContent?.let { md ->
                        Material3RichText {
                            Markdown(md)
                        }
                    }
                }
            }
        }
    } else {
        // Kapitel-Übersicht
        Column(modifier = Modifier.fillMaxSize().background(Pergament)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WaldgruenDunkel)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    "📚 Regelwerk & Hilfe",
                    color = WaldGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(HILFE_CHAPTERS) { (title, path) ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PergamentHell),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedChapter = title to path }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Book, contentDescription = null, tint = Waldgruen)
                            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = TintenSchwarz)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Waldgruen.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("ℹ️ DnD Mietling", fontWeight = FontWeight.Bold, color = Waldgruen, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Diese App synchronisiert Initiativetracker, Questlog, Loot und Chat in Echtzeit mit dem DnD Companion.",
                                fontSize = 13.sp,
                                color = TintenBraun
                            )
                        }
                    }
                }
            }
        }
    }
}
