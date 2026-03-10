package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel

@Composable
fun WarlockScreen(viewModel: CharacterViewModel) {
    val maxSlots = viewModel.characterData.baseSpellSlotsLevel2
    val currentSlots = viewModel.spellSlotsLevel2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GelbSand)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Warlock Logik & Paktmagie", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
        Spacer(modifier = Modifier.height(16.dp))

        // Paktmagie Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PinkHell),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Paktmagie (Level 2)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("$currentSlots / $maxSlots Slots", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = { viewModel.useSpellSlotLevel2() },
                        enabled = currentSlots > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = BlauDunkel)
                    ) {
                        Text("Slot nutzen (-1)")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = { viewModel.resetWarlockSlots() },
                        colors = ButtonDefaults.buttonColors(containerColor = Gruen)
                    ) {
                        Text("Kurze Rast")
                    }
                    
                    Button(
                        onClick = { viewModel.applyMagicalCunning() },
                        colors = ButtonDefaults.buttonColors(containerColor = PinkDunkel)
                    ) {
                        Text("Magische Rafinesse")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Anrufungen Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Schauerliche Anrufungen (Level 4)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text("• Pakt der Klinge (Paktwaffe)", fontWeight = FontWeight.Bold)
                Text("Als Bonusaktion beschwörst du deine Paktwaffe. Charisma für Angriffs- & Schadenswürfe.", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("• Qualvoller Strahl (Agonizing Blast)", fontWeight = FontWeight.Bold)
                Text("+4 (CHA Modifikator) Schaden für Schauriger Strahl (Eldritch Blast).", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("• Rüstung der Schatten (Armor of Shadows)", fontWeight = FontWeight.Bold)
                Text("Du kannst Magierrüstung beliebig oft auf dich selbst wirken, ohne einen Zauberplatz auszugeben.", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("• Unholde Vitalität", fontWeight = FontWeight.Bold)
                Text("Du kannst Falsches Leben beliebig oft auf dich wirken, ohne Komponenten/Slot. Immer max (12 HP).", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        }
    }
}
