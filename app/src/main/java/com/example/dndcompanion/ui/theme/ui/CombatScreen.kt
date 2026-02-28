package com.example.dndcompanion.ui.theme.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.theme.viewmodel.ActiveWeapon
import com.example.dndcompanion.ui.theme.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.theme.BlauDunkel
import com.example.dndcompanion.ui.theme.BlauHell
import com.example.dndcompanion.ui.theme.PinkDunkel
import com.example.dndcompanion.ui.theme.PinkHell
import com.example.dndcompanion.ui.theme.GelbSand

@Composable
fun CombatScreen(viewModel: CharacterViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GelbSand)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Große Anzeige der Rüstungsklasse
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = BlauDunkel),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Rüstungsklasse (RK)", color = Color.White, fontSize = 18.sp)
                Text(
                    text = viewModel.currentArmorClass.toString(),
                    color = PinkHell,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text("Waffe ausrüsten", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
        Spacer(modifier = Modifier.height(8.dp))

        // Waffen-Auswahl (Segmented Control Style)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WeaponButton(
                title = "Langbogen",
                isSelected = viewModel.currentWeapon == ActiveWeapon.LANGBOGEN,
                onClick = { viewModel.equipWeapon(ActiveWeapon.LANGBOGEN) }
            )
            WeaponButton(
                title = "Kurzschwert\n& Schild",
                isSelected = viewModel.currentWeapon == ActiveWeapon.KURZSCHWERT_SCHILD,
                onClick = { viewModel.equipWeapon(ActiveWeapon.KURZSCHWERT_SCHILD) }
            )
            WeaponButton(
                title = "Shillelagh\n& Schild",
                isSelected = viewModel.currentWeapon == ActiveWeapon.SHILLELAGH_SCHILD,
                onClick = { viewModel.equipWeapon(ActiveWeapon.SHILLELAGH_SCHILD) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Anzeige der aktuellen Waffenwerte
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlauHell),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Trefferbonus: ${viewModel.currentAttackBonus}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Schaden: ${viewModel.currentDamage}", fontSize = 18.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Extra: Messer-Talent erlaubt 1x pro Zug Schadenswürfel neu zu werfen (nur Stich!).", fontSize = 14.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = BlauDunkel)
            }
        }
    }
}

@Composable
fun WeaponButton(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) PinkDunkel else BlauHell,
            contentColor = if (isSelected) Color.White else BlauDunkel
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.width(110.dp).height(60.dp)
    ) {
        Text(text = title, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 14.sp)
    }
}