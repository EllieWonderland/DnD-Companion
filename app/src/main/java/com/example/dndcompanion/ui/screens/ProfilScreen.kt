package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.theme.BlauDunkel
import com.example.dndcompanion.ui.theme.BlauHell
import com.example.dndcompanion.ui.theme.PinkDunkel
import com.example.dndcompanion.ui.theme.GelbSand

@Composable
fun ProfilScreen(viewModel: CharacterViewModel) {
    var epInput by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GelbSand)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Charakter Profil", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
        Spacer(modifier = Modifier.height(16.dp))

        // Grunddaten
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = BlauHell)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Athania", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Waldläufer (Herrin der Tiere) | Stufe ${viewModel.level}", color = GelbSand)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Volk: Elf-Drow | Hintergrund: Wegfinder", color = Color.White)
                Text("Gesinnung: Chaotisch Gut | EP: ${viewModel.currentEP}", color = Color.White)
            }
        }

        // Attribute (STR, DEX, CON, INT, WIS, CHA)
        Text("Attribute & Rettungswürfe", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
        Spacer(modifier = Modifier.height(8.dp))
        val strSave = viewModel.strMod + viewModel.proficiencyBonus
        val dexSave = viewModel.dexMod + viewModel.proficiencyBonus
        
        fun formatMod(mod: Int) = if (mod >= 0) "+$mod" else "$mod"
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            AttributeBox("STR", viewModel.strength.toString(), formatMod(viewModel.strMod), "RW: ${formatMod(strSave)} (Geübt)")
            AttributeBox("DEX", viewModel.dexterity.toString(), formatMod(viewModel.dexMod), "RW: ${formatMod(dexSave)} (Geübt)")
            AttributeBox("CON", viewModel.constitution.toString(), formatMod(viewModel.conMod), "RW: ${formatMod(viewModel.conMod)}")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            AttributeBox("INT", viewModel.intelligence.toString(), formatMod(viewModel.intMod), "RW: ${formatMod(viewModel.intMod)}")
            AttributeBox("WIS", viewModel.wisdom.toString(), formatMod(viewModel.wisMod), "RW: ${formatMod(viewModel.wisMod)}")
            AttributeBox("CHA", viewModel.charisma.toString(), formatMod(viewModel.chaMod), "RW: ${formatMod(viewModel.chaMod)}")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = BlauDunkel, thickness = 2.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // Fertigkeiten
        Text("Fertigkeiten", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = BlauHell)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        SkillRow("Akrobatik (DEX)", "+3")
                        SkillRow("Arkane Kunde (INT)", "+0")
                        SkillRow("Athletik (STR)", "-1")
                        SkillRow("Auftreten (CHA)", "-1")
                        SkillRow("Einschüchtern (CHA)", "-1")
                        SkillRow("Fingerfertigkeit (DEX)", "+3")
                        SkillRow("Geschichte (INT)", "+0")
                        SkillRow("Heilkunde (WIS)", "+2")
                        SkillRow("Heimlichkeit (DEX)", "+5", true)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        SkillRow("Mit Tieren umg. (WIS)", "+4")
                        SkillRow("Motiv erkennen (WIS)", "+4", true)
                        SkillRow("Nachforschungen (INT)", "+0")
                        SkillRow("Naturkunde (INT)", "+2", true)
                        SkillRow("Religion (INT)", "+0")
                        SkillRow("Täuschen (CHA)", "-1")
                        SkillRow("Überleben (WIS)", "+4", true)
                        SkillRow("Überzeugen (CHA)", "-1")
                        SkillRow("Wahrnehmung (WIS)", "+6", true)
                    }
                }
            }
        }

        HorizontalDivider(color = BlauDunkel, thickness = 2.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // Hintergrund & Persönlichkeit
        Text("Hintergrund & Besonderheiten", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BlauDunkel)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BlauHell)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Aussehen: Magisches Tattoo (Blutige Hand eines Kindes)", color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Sprachen: Gemeinsprache, Gebärden-Gemeinsprache, Halblingisch, Zwergisch, Elfisch", color = Color.White)
                HorizontalDivider(color = GelbSand, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                
                Text("Ideal (Höheres Ziel):", fontWeight = FontWeight.Bold, color = GelbSand)
                Text("Es ist die Verantwortung jeder Einzelnen, für das Wohl des Stammes zu sorgen.", color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Makel (Nachtragend):", fontWeight = FontWeight.Bold, color = GelbSand)
                Text("Ich erinnere mich an jede einzelne Beleidigung, die mir galt, und hege eine stumme Abneigung gegen all jene, die mich schon einmal falsch behandelt haben.", color = Color.White)
            }
        }
    }

    if (viewModel.showLevelUpDialog) {
        LevelUpDialog(viewModel = viewModel)
    }
}

@Composable
fun AttributeBox(name: String, value: String, mod: String, rw: String) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(containerColor = BlauHell)
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(name, fontWeight = FontWeight.Bold, color = GelbSand, fontSize = 14.sp)
            Text(value, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 24.sp)
            Text(mod, color = PinkDunkel, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(rw, color = Color.LightGray, fontSize = 10.sp)
        }
    }
}

@Composable
fun SkillRow(name: String, mod: String, proficient: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val color = if (proficient) GelbSand else Color.White
        val weight = if (proficient) FontWeight.Bold else FontWeight.Normal
        Text(name, color = color, fontSize = 12.sp, fontWeight = weight)
        Text(mod, color = color, fontSize = 12.sp, fontWeight = weight)
    }
}
