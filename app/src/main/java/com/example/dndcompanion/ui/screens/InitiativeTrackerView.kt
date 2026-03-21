package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.viewmodel.GroupViewModel
import com.example.dndcompanion.ui.viewmodel.InitiativeEntry
import com.example.dndcompanion.ui.theme.*

@Composable
fun InitiativeTrackerView(
    groupVm: GroupViewModel,
    onBack: () -> Unit
) {
    val sorted = groupVm.initiativeEntries.sortedByDescending { it.initiative }

    PergamentBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WaldgruenDunkel)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = PergamentHell)
                }
                Text(
                    "⚔️ Initiative-Tracker",
                    color = WaldGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Almendra
                )
                Spacer(modifier = Modifier.weight(1f))
                Text("(Nur Ansicht)", color = PergamentDunkel, fontSize = 12.sp)
            }

            if (sorted.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚔️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Kein aktiver Kampf", color = TintenBraun, fontSize = 16.sp, fontFamily = Almendra)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Der DM startet den Tracker im Mietling", color = EisenGrau, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sorted, key = { it.id }) { entry ->
                        CompanionInitiativeCard(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompanionInitiativeCard(entry: InitiativeEntry) {
    val isActive = entry.isActive
    val isMonster = entry.type == "MONSTER"
    val borderColor = when {
        isActive -> Waldgruen
        entry.criticalHint -> OchsenblutRot
        else -> PergamentDunkel
    }

    PergamentCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Initiative Badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isActive) Waldgruen else WaldgruenDunkel),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${entry.initiative}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = Almendra
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isActive) Text("▶", color = Waldgruen, fontSize = 12.sp)
                    Text(
                        entry.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isActive) Waldgruen else TintenSchwarz,
                        fontFamily = Almendra
                    )
                    if (isMonster) {
                        Surface(shape = RoundedCornerShape(4.dp), color = OchsenblutRot.copy(alpha = 0.15f)) {
                            Text("Monster", fontSize = 10.sp, color = OchsenblutRot, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                }
                if (isMonster && entry.damageTaken > 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Schaden: ${entry.damageTaken}", fontSize = 13.sp, color = OchsenblutRot)
                        if (entry.criticalHint) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = OchsenblutRot, modifier = Modifier.size(14.dp))
                            Text("Krit. Hinweis", fontSize = 11.sp, color = OchsenblutRot, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
