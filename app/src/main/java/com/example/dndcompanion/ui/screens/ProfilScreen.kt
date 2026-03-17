package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.data.CharacterClass
import com.example.dndcompanion.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete

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
                    val nextLevelEP = if (viewModel.level < viewModel.epThresholds.size) viewModel.epThresholds[viewModel.level] else viewModel.currentEP
                    val epProgress = if (nextLevelEP > 0) viewModel.currentEP.toFloat() / nextLevelEP.toFloat() else 1f
                    val animatedEpProgress by animateFloatAsState(
                        targetValue = epProgress,
                        animationSpec = tween(durationMillis = 800),
                        label = "EP Animation Profile"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val avatarId = if (viewModel.characterData.name == "Athania") R.drawable.athania else R.drawable.delat
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                                CircularProgressIndicator(
                                    progress = { animatedEpProgress },
                                    modifier = Modifier.fillMaxSize(),
                                    color = accentColor,
                                    trackColor = Waldgruen,
                                    strokeWidth = 6.dp,
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                )
                                Image(
                                    painter = painterResource(id = avatarId),
                                    contentDescription = "Charakter Portrait",
                                    modifier = Modifier
                                        .size(144.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, TintenBraun.copy(alpha = 0.3f), CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                viewModel.characterData.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Waldgruen
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val hpProgress by animateFloatAsState(
                                targetValue = if (viewModel.maxHp > 0) viewModel.currentHp.toFloat() / viewModel.maxHp.toFloat() else 0f,
                                animationSpec = tween(durationMillis = 500),
                                label = "HP Animation Profile"
                            )

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
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val remainingEP = (nextLevelEP - viewModel.currentEP).coerceAtLeast(0)
                            Text(
                                "EP: ${viewModel.currentEP} / $nextLevelEP",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 14.sp),
                                color = TintenSchwarz,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Noch $remainingEP EP",
                                style = MaterialTheme.typography.labelSmall,
                                color = TintenBraun
                            )
                        }
                    }

                    val className = if (viewModel.characterData.charClass == CharacterClass.RANGER) "Waldläufer (Herrin der Tiere)" else "Warlock (Pakt der Klinge)"
                    Text(
                        "$className | Stufe ${viewModel.level}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TintenBraun
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val volk = "${viewModel.characterRace} | Hintergrund: ${viewModel.characterBackground}"
                    Text("Volk: $volk", style = MaterialTheme.typography.bodySmall, color = TintenBraun)

                    val gesinnung = "${viewModel.characterAlignment}"
                    Text("Gesinnung: $gesinnung", style = MaterialTheme.typography.bodySmall, color = TintenBraun)

                    Spacer(modifier = Modifier.height(8.dp))
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
            val strProf = viewModel.characterData.proficientSkills.contains("Stärke-Rettungswurf")
            val dexProf = viewModel.characterData.proficientSkills.contains("Geschicklichkeit-Rettungswurf")
            val conProf = viewModel.characterData.proficientSkills.contains("Konstitution-Rettungswurf")
            val intProf = viewModel.characterData.proficientSkills.contains("Intelligenz-Rettungswurf")
            val wisProf = viewModel.characterData.proficientSkills.contains("Weisheit-Rettungswurf")
            val chaProf = viewModel.characterData.proficientSkills.contains("Charisma-Rettungswurf")

            val strSave = viewModel.strMod + if (strProf) viewModel.proficiencyBonus else 0
            val dexSave = viewModel.dexMod + if (dexProf) viewModel.proficiencyBonus else 0
            val conSave = viewModel.conMod + if (conProf) viewModel.proficiencyBonus else 0
            val intSave = viewModel.intMod + if (intProf) viewModel.proficiencyBonus else 0
            val wisSave = viewModel.wisMod + if (wisProf) viewModel.proficiencyBonus else 0
            val chaSave = viewModel.chaMod + if (chaProf) viewModel.proficiencyBonus else 0

            fun formatMod(mod: Int) = if (mod >= 0) "+$mod" else "$mod"
            fun formatRwResult(save: Int, isProf: Boolean) = "RW: ${formatMod(save)}" + if (isProf) " (Geübt)" else ""

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AttributeBox("STR", viewModel.strength.toString(), formatMod(viewModel.strMod), formatRwResult(strSave, strProf), R.drawable.icon_str)
                AttributeBox("DEX", viewModel.dexterity.toString(), formatMod(viewModel.dexMod), formatRwResult(dexSave, dexProf), R.drawable.icon_dex)
                AttributeBox("CON", viewModel.constitution.toString(), formatMod(viewModel.conMod), formatRwResult(conSave, conProf), R.drawable.icon_con)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AttributeBox("INT", viewModel.intelligence.toString(), formatMod(viewModel.intMod), formatRwResult(intSave, intProf), R.drawable.icon_int)
                AttributeBox("WIS", viewModel.wisdom.toString(), formatMod(viewModel.wisMod), formatRwResult(wisSave, wisProf), R.drawable.icon_wis)
                AttributeBox("CHA", viewModel.charisma.toString(), formatMod(viewModel.chaMod), formatRwResult(chaSave, chaProf), R.drawable.icon_cha)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Bronze, thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Fertigkeiten
            Text("Fertigkeiten", style = MaterialTheme.typography.titleMedium, color = Waldgruen)
            Spacer(modifier = Modifier.height(8.dp))
            PergamentCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val skills = listOf(
                        "Akrobatik" to viewModel.dexMod,
                        "Arkane Kunde" to viewModel.intMod,
                        "Athletik" to viewModel.strMod,
                        "Auftreten" to viewModel.chaMod,
                        "Einschüchtern" to viewModel.chaMod,
                        "Fingerfertigkeit" to viewModel.dexMod,
                        "Geschichte" to viewModel.intMod,
                        "Heilkunde" to viewModel.wisMod,
                        "Heimlichkeit" to viewModel.dexMod,
                        "Mit Tieren umg." to viewModel.wisMod,
                        "Motiv erkennen" to viewModel.wisMod,
                        "Nachforschungen" to viewModel.intMod,
                        "Naturkunde" to viewModel.intMod,
                        "Religion" to viewModel.intMod,
                        "Täuschen" to viewModel.chaMod,
                        "Überleben" to viewModel.wisMod,
                        "Überzeugen" to viewModel.chaMod,
                        "Wahrnehmung" to viewModel.wisMod
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            skills.take(9).forEach { (name, _) ->
                                val isProficient = viewModel.characterData.proficientSkills.contains(name)
                                SkillRow("$name (${if(name == "Athletik") "STR" else if(name in listOf("Akrobatik", "Fingerfertigkeit", "Heimlichkeit")) "DEX" else if(name in listOf("Arkane Kunde", "Geschichte", "Nachforschungen", "Naturkunde", "Religionskunde", "Religion")) "INT" else if(name in listOf("Auftreten", "Einschüchtern", "Täuschen", "Überzeugen")) "CHA" else "WIS"})", viewModel.getSkillModifier(if(name == "Mit Tieren umg.") "Mit Tieren umgehen" else if(name == "Überleben") "Überlebenskunst" else if(name == "Nachforschungen") "Nachforschung" else if(name == "Religion") "Religionskunde" else name), isProficient)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            skills.drop(9).forEach { (name, _) ->
                                val isProficient = viewModel.characterData.proficientSkills.contains(name)
                                SkillRow("$name (${if(name == "Athletik") "STR" else if(name in listOf("Akrobatik", "Fingerfertigkeit", "Heimlichkeit")) "DEX" else if(name in listOf("Arkane Kunde", "Geschichte", "Nachforschungen", "Naturkunde", "Religionskunde", "Religion")) "INT" else if(name in listOf("Auftreten", "Einschüchtern", "Täuschen", "Überzeugen")) "CHA" else "WIS"})", viewModel.getSkillModifier(if(name == "Mit Tieren umg.") "Mit Tieren umgehen" else if(name == "Überleben") "Überlebenskunst" else if(name == "Nachforschungen") "Nachforschung" else if(name == "Religion") "Religionskunde" else name), isProficient)
                            }
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
                        Text("Aussehen:", style = MaterialTheme.typography.labelLarge, color = WaldGold)
                        Text("Magisches Tattoo (Blutige Hand eines Kindes)", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sprachen:", style = MaterialTheme.typography.labelLarge, color = WaldGold)
                        Text("Gemeinsprache, Gebärden-Gemeinsprache, Halblingisch, Zwergisch, Elfisch", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                        HorizontalDivider(color = PergamentDunkel, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                        Text("Ideal (Höheres Ziel):", style = MaterialTheme.typography.labelLarge, color = WaldGold)
                        Text("Es ist die Verantwortung jeder Einzelnen, für das Wohl des Stammes zu sorgen.", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Makel (Nachtragend):", style = MaterialTheme.typography.labelLarge, color = WaldGold)
                        Text("Ich erinnere mich an jede einzelne Beleidigung, die mir galt, und hege eine stumme Abneigung gegen all jene, die mich schon einmal falsch behandelt haben.", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                    } else {
                        Text("Aussehen:", style = MaterialTheme.typography.labelLarge, color = WaldGold)
                        Text("Gepflegt, mysteriöses Buch immer in der Hand", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sprachen:", style = MaterialTheme.typography.labelLarge, color = WaldGold)
                        Text("Gemeinsprache, Drakonisch, Abyssal", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                        HorizontalDivider(color = PergamentDunkel, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                        Text("Ideal (Macht):", style = MaterialTheme.typography.labelLarge, color = HexenLila)
                        Text("Wissen ist Macht, und ich werde alles tun, um mehr davon zu erlangen.", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Makel (Arroganz):", style = MaterialTheme.typography.labelLarge, color = HexenLila)
                        Text("Ich unterschätze oft andere, weil ich glaube, dass ich klüger bin als sie.", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Bronze, thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Merkmale & Talente Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Merkmale & Talente", style = MaterialTheme.typography.titleMedium, color = Waldgruen)
                IconButton(onClick = { viewModel.showFeatureSelection = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Merkmal hinzufügen", tint = Waldgruen)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            if (viewModel.customTraits.isEmpty()) {
                Text("Keine Merkmale gewählt.", color = TintenBraun, style = MaterialTheme.typography.bodySmall, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            } else {
                viewModel.customTraits.forEachIndexed { index, trait ->
                    PergamentCard(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(trait.name, fontWeight = FontWeight.Bold, color = Waldgruen, fontFamily = Almendra)
                                IconButton(onClick = { viewModel.removeCustomTrait(index) }, modifier = Modifier.size(40.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Löschen", tint = OchsenblutRot, modifier = Modifier.size(20.dp))
                                }
                            }
                            Text(trait.desc, style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (viewModel.showFeatureSelection) {
        FeatureSelectionScreen(
            viewModel = viewModel,
            onDismiss = { viewModel.showFeatureSelection = false }
        )
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
fun AttributeBox(name: String, value: String, mod: String, rw: String, iconRes: Int? = null) {
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
            if (iconRes != null) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = "$name Icon",
                    modifier = Modifier.size(95.dp).padding(bottom = 2.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Text(name, style = MaterialTheme.typography.labelMedium, color = Waldgruen)
            Text(value, style = GrenzeGotischStyle, color = TintenSchwarz)
            Text(mod, style = GrenzeGotischSmall, color = OchsenblutRot)
            Text(rw, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = TintenBraun, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun SkillRow(name: String, mod: Int, proficient: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val color = if (proficient) WaldGold else TintenSchwarz
        val weight = if (proficient) FontWeight.Bold else FontWeight.Normal
        val modStr = if (mod >= 0) "+$mod" else "$mod"
        Text(name, color = color, style = MaterialTheme.typography.bodySmall, fontWeight = weight)
        Text(modStr, color = color, style = MaterialTheme.typography.bodySmall, fontWeight = weight)
    }
}
