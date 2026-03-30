package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.viewmodel.BeastType
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.R

@Composable
fun CompanionSelectionDialog(
    viewModel: CharacterViewModel,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = PergamentHell) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text("Tierart wechseln", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Waldgruen, fontFamily = Almendra, modifier = Modifier.padding(bottom = 16.dp))
                
                // Land
                BeastTypeCard(
                    title = "Urtier des Landes",
                    iconRes = R.drawable.icon_capybara,
                    speed = "12m | Klettern 12m",
                    attack = "Zerfleischen (1d8 + 2 + PB Hieb)",
                    isActive = viewModel.activeBeastType == BeastType.LAND,
                    onClick = {
                        viewModel.toggleBeastType(BeastType.LAND)
                        onDismiss()
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Himmel
                BeastTypeCard(
                    title = "Urtier des Himmels",
                    iconRes = R.drawable.icon_capybara,
                    speed = "3m | Fliegen 18m",
                    attack = "Schreddern (1d4 + 3 + PB Hieb) | Vorbeiflug",
                    isActive = viewModel.activeBeastType == BeastType.SKY,
                    onClick = {
                        viewModel.toggleBeastType(BeastType.SKY)
                        onDismiss()
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Meer
                BeastTypeCard(
                    title = "Urtier des Meeres",
                    iconRes = R.drawable.icon_capybara,
                    speed = "9m | Schwimmen 9m",
                    attack = "Fesselnder Schlag (1d6 + PB Hieb) | Packen",
                    isActive = viewModel.activeBeastType == BeastType.SEA,
                    onClick = {
                        viewModel.toggleBeastType(BeastType.SEA)
                        onDismiss()
                    }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Abbrechen", fontFamily = Almendra)
                }
            }
        }
    }
}

@Composable
fun BeastTypeCard(title: String, iconRes: Int, speed: String, attack: String, isActive: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(if (isActive) 2.dp else 0.dp, if (isActive) Waldgruen else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = if (isActive) Waldgruen.copy(alpha=0.1f) else PergamentDunkel),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TintenSchwarz)
                Text("Tempo: $speed", fontSize = 14.sp, color = TintenBraun)
                Text("Angriff: $attack", fontSize = 14.sp, color = TintenBraun)
            }
        }
    }
}
