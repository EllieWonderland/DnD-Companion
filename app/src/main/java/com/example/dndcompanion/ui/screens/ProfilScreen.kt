package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import androidx.compose.ui.draw.shadow
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.data.CharacterClass

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown

@Composable
fun ProfilScreen(viewModel: CharacterViewModel) {
    var epInput by remember { mutableStateOf("") }

    PergamentBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Titel + Profil-Wechsler
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.dndcompanion.R.drawable.dndcompanion_logo),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(36.dp)
                            .androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.CircleShape)
                            .border(1.dp, WaldGold, androidx.compose.foundation.shape.CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Charakter Profil",
                        style = MaterialTheme.typography.titleLarge,
                        color = Waldgruen
                    )
                }

                var expanded by remember { mutableStateOf(false) }
                val accentColor = if (viewModel.characterData.charClass == CharacterClass.RANGER) WaldGold else HexenLila
                Box {
                    Button(
                        onClick = { expanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(viewModel.characterData.name, fontFamily = Almendra, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Profil wechseln")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Athania", fontFamily = Almendra) },
                            onClick = {
                                viewModel.loadProfile("Athania")
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delat", fontFamily = Almendra) },
                            onClick = {
                                viewModel.loadProfile("Delat")
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Grunddaten-Karte
            val accentColor = if (viewModel.characterData.charClass == CharacterClass.RANGER) WaldGold else HexenLila
            PergamentCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val avatarId = if (viewModel.characterData.name == "Athania") com.example.dndcompanion.R.drawable.athania else com.example.dndcompanion.R.drawable.delat
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = avatarId),
                                contentDescription = "Charakter Portrait",
                                modifier = Modifier
                                    .size(64.dp)
                                    .androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.CircleShape)
                                    .border(2.dp, accentColor, androidx.compose.foundation.shape.CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                viewModel.characterData.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Waldgruen
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "HP: ${viewModel.currentHp} / ${viewModel.maxHp}",
                                style = GrenzeGotischSmall,
                                color = OchsenblutRot
                            )
                            if (viewModel.tempHp > 0) {
                                Text(
                                    "+${viewModel.tempHp} Temp HP",
                                    style = GrenzeGotischSmall.copy(fontSize = 16.sp),
                                    color = TempHPBlau
                                )
                            }
                        }
                    }

                    val className = if (viewModel.characterData.charClass == CharacterClass.RANGER) "Waldläufer (Herrin der Tiere)" else "Warlock (Pakt der Klinge)"
                    Text(
                        "$className | Stufe ${viewModel.level}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TintenBraun
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val volk = if (viewModel.characterData.name == "Athania") "Elf-Drow | Hintergrund: Wegfinder" else "Mensch | Hintergrund: Gelehrter"
                    Text("Volk: $volk", style = MaterialTheme.typography.bodySmall, color = TintenBraun)

                    val gesinnung = if (viewModel.characterData.name == "Athania") "Chaotisch Gut" else "Rechtschaffen Neutral"
                    Text("Gesinnung: $gesinnung | EP: ${viewModel.currentEP}", style = MaterialTheme.typography.bodySmall, color = TintenBraun)

                    HorizontalDivider(color = PergamentDunkel, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        "Übungsbonus: +${viewModel.proficiencyBonus}",
                        style = MaterialTheme.typography.labelLarge,
                        color = accentColor
                    )
                }
            }

            // Attribute & Rettungswürfe
            Text(
                "Attribute & Rettungswürfe",
                style = MaterialTheme.typography.titleMedium,
                color = Waldgruen
            )
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
            HorizontalDivider(color = Bronze, thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Fertigkeiten
            Text("Fertigkeiten", style = MaterialTheme.typography.titleMedium, color = Waldgruen)
            Spacer(modifier = Modifier.height(8.dp))
            PergamentCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
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

            HorizontalDivider(color = Bronze, thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Hintergrund & Persönlichkeit
            Text("Hintergrund & Besonderheiten", style = MaterialTheme.typography.titleMedium, color = Waldgruen)
            Spacer(modifier = Modifier.height(8.dp))
            PergamentCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (viewModel.characterData.name == "Athania") {
                        Text("Aussehen: Magisches Tattoo (Blutige Hand eines Kindes)", style = MaterialTheme.typography.bodyMedium, color = TintenSchwarz)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sprachen: Gemeinsprache, Gebärden-Gemeinsprache, Halblingisch, Zwergisch, Elfisch", style = MaterialTheme.typography.bodyMedium, color = TintenSchwarz)
                        HorizontalDivider(color = PergamentDunkel, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                        Text("Ideal (Höheres Ziel):", style = MaterialTheme.typography.labelLarge, color = WaldGold)
                        Text("Es ist die Verantwortung jeder Einzelnen, für das Wohl des Stammes zu sorgen.", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Makel (Nachtragend):", style = MaterialTheme.typography.labelLarge, color = WaldGold)
                        Text("Ich erinnere mich an jede einzelne Beleidigung, die mir galt, und hege eine stumme Abneigung gegen all jene, die mich schon einmal falsch behandelt haben.", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                    } else {
                        Text("Aussehen: Gepflegt, mysteriöses Buch immer in der Hand", style = MaterialTheme.typography.bodyMedium, color = TintenSchwarz)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sprachen: Gemeinsprache, Drakonisch, Abyssal", style = MaterialTheme.typography.bodyMedium, color = TintenSchwarz)
                        HorizontalDivider(color = PergamentDunkel, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                        Text("Ideal (Macht):", style = MaterialTheme.typography.labelLarge, color = HexenLila)
                        Text("Wissen ist Macht, und ich werde alles tun, um mehr davon zu erlangen.", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Makel (Arroganz):", style = MaterialTheme.typography.labelLarge, color = HexenLila)
                        Text("Ich unterschätze oft andere, weil ich glaube, dass ich klüger bin als sie.", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                    }
                }
            }
        }
    }

    if (viewModel.showLevelUpDialog) {
        LevelUpDialog(viewModel = viewModel)
    }

    if (viewModel.showRestWarningDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRestWarningDialog() },
            title = { Text("Unzureichende Rationen", style = MaterialTheme.typography.titleSmall, color = OchsenblutRot) },
            text = { Text("Du hast nicht genug Wasserschläuche (0.5 benötigt) oder Tagesrationen (1 benötigt) für eine vollständige Lange Rast. Willst du trotzdem rasten? (Es werden keine Rationen verbraucht, aber du erhältst keine HP oder Zauberslots zurück... oder wir ignorieren die Regeln für jetzt und rasten trotzdem ohne Ressourcen-Abzug?)", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.forceLongRestWithoutResources()
                        viewModel.dismissRestWarningDialog()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Trotzdem Rasten", fontFamily = Almendra)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRestWarningDialog() }) {
                    Text("Abbrechen", color = Waldgruen, fontFamily = Almendra)
                }
            },
            containerColor = PergamentHell,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun AttributeBox(name: String, value: String, mod: String, rw: String) {
    Card(
        modifier = Modifier
            .width(105.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .border(1.5.dp, EisenGrau, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFBEB5A0))
    ) {
        Column(
            modifier = Modifier.padding(6.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(name, style = MaterialTheme.typography.labelMedium, color = Waldgruen)
            Text(value, style = GrenzeGotischStyle, color = TintenSchwarz)
            Text(mod, style = GrenzeGotischSmall, color = OchsenblutRot)
            Text(rw, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TintenBraun, textAlign = TextAlign.Center)
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
        val color = if (proficient) WaldGold else TintenSchwarz
        val weight = if (proficient) FontWeight.Bold else FontWeight.Normal
        Text(name, color = color, style = MaterialTheme.typography.bodySmall, fontWeight = weight)
        Text(mod, color = color, style = MaterialTheme.typography.bodySmall, fontWeight = weight)
    }
}
