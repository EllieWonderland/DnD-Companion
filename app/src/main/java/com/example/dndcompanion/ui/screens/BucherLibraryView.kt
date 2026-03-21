package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.R
import com.example.dndcompanion.ui.theme.*

@Composable
fun LibraryView(onBookSelected: (BookType) -> Unit) {
    PergamentBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Bibliothek", fontSize = 32.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = TintenSchwarz)
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BookCard(
                    title = "Notizbuch",
                    subtitle = "Allgemeines & Infos",
                    imageRes = R.drawable.notizbuch,
                    onClick = { onBookSelected(BookType.GENERAL) }
                )
                BookCard(
                    title = "Buch des Grolls",
                    subtitle = "Vergeltung wartet",
                    imageRes = R.drawable.buch_des_grolls,
                    onClick = { onBookSelected(BookType.GRUDGE) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BookCard(
                    title = "Zauberbuch",
                    subtitle = "Alle bekannten Zauber",
                    imageRes = R.drawable.zauberbuch,
                    onClick = { onBookSelected(BookType.SPELLBOOK) }
                )
                BookCard(
                    title = "Regelwerk",
                    subtitle = "Handbuch & D&D Regeln",
                    imageRes = R.drawable.regelwerk,
                    onClick = { onBookSelected(BookType.RULEBOOK) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BookCard(
                    title = "Gruppen-Chat",
                    subtitle = "IC & OOC Nachrichten",
                    imageRes = R.drawable.gruppenchat,
                    onClick = { onBookSelected(BookType.GROUP_CHAT) }
                )
                BookCard(
                    title = "Questlog",
                    subtitle = "Aktive & fertige Aufträge",
                    imageRes = R.drawable.questlog,
                    onClick = { onBookSelected(BookType.QUESTLOG) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BookCard(
                    title = "Initiative",
                    subtitle = "Kampfreihenfolge (Mietling)",
                    imageRes = R.drawable.kampf,
                    onClick = { onBookSelected(BookType.INITIATIVE) }
                )
                Spacer(modifier = Modifier.width(150.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun BookCard(title: String, subtitle: String, imageRes: Int, onClick: () -> Unit) {
    PergamentCard(
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(4.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontFamily = Almendra,
                fontWeight = FontWeight.Bold,
                color = TintenSchwarz,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = GrenzeGotischSmall,
                color = TintenSchwarz.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
