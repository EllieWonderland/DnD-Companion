package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize
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
import com.example.dndcompanion.ui.viewmodel.CombatViewModel
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.data.CharacterClass
import com.example.dndcompanion.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit

@Composable
fun ProfilScreen(viewModel: CharacterViewModel, combatVm: CombatViewModel) {
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.openCharacterEdit() }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Charakter bearbeiten",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                var expanded by remember { mutableStateOf(false) }
                val accentColor = MaterialTheme.colorScheme.tertiary
                Box {
                    Button(
                        onClick = { expanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = PergamentHell),
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
                } // end inner Row (edit button + dropdown)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Grunddaten-Karte
            val accentColor = MaterialTheme.colorScheme.tertiary
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
                                targetValue = if (combatVm.maxHp > 0) combatVm.currentHp.toFloat() / combatVm.maxHp.toFloat() else 0f,
                                animationSpec = tween(durationMillis = 500),
                                label = "HP Animation Profile"
                            )

                            Text(
                                "HP: ${combatVm.currentHp} / ${combatVm.maxHp}",
                                style = GrenzeGotischSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            if (combatVm.tempHp > 0) {
                                Text(
                                    "+${combatVm.tempHp} Temp HP",
                                    style = GrenzeGotischSmall.copy(fontSize = 16.sp),
                                    color = WaldgruenHell
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

            // Attribute, Rettungswürfe & Fertigkeiten
            Text(
                "Attribute, Rettungswürfe & Fertigkeiten",
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
            fun formatRwResult(save: Int, isProf: Boolean) = "RW: ${formatMod(save)}" + if (isProf) " ★" else ""
            fun prof(name: String) = viewModel.characterData.proficientSkills.contains(name)
            fun skillMod(display: String) = viewModel.getSkillModifier(when (display) {
                "Mit Tieren umg." -> "Mit Tieren umgehen"
                "Überleben" -> "Überlebenskunst"
                "Nachforschungen" -> "Nachforschung"
                "Religion" -> "Religionskunde"
                else -> display
            })

            AttributeBox(
                name = "STR", value = viewModel.strength.toString(),
                mod = formatMod(viewModel.strMod), rw = formatRwResult(strSave, strProf),
                iconRes = R.drawable.icon_str,
                skills = listOf(Triple("Athletik", skillMod("Athletik"), prof("Athletik")))
            )
            AttributeBox(
                name = "DEX", value = viewModel.dexterity.toString(),
                mod = formatMod(viewModel.dexMod), rw = formatRwResult(dexSave, dexProf),
                iconRes = R.drawable.icon_dex,
                skills = listOf(
                    Triple("Akrobatik", skillMod("Akrobatik"), prof("Akrobatik")),
                    Triple("Fingerfertigkeit", skillMod("Fingerfertigkeit"), prof("Fingerfertigkeit")),
                    Triple("Heimlichkeit", skillMod("Heimlichkeit"), prof("Heimlichkeit"))
                )
            )
            AttributeBox(
                name = "CON", value = viewModel.constitution.toString(),
                mod = formatMod(viewModel.conMod), rw = formatRwResult(conSave, conProf),
                iconRes = R.drawable.icon_con
            )
            AttributeBox(
                name = "INT", value = viewModel.intelligence.toString(),
                mod = formatMod(viewModel.intMod), rw = formatRwResult(intSave, intProf),
                iconRes = R.drawable.icon_int,
                skills = listOf(
                    Triple("Arkane Kunde", skillMod("Arkane Kunde"), prof("Arkane Kunde")),
                    Triple("Geschichte", skillMod("Geschichte"), prof("Geschichte")),
                    Triple("Nachforschungen", skillMod("Nachforschungen"), prof("Nachforschungen")),
                    Triple("Naturkunde", skillMod("Naturkunde"), prof("Naturkunde")),
                    Triple("Religion", skillMod("Religion"), prof("Religion"))
                )
            )
            AttributeBox(
                name = "WIS", value = viewModel.wisdom.toString(),
                mod = formatMod(viewModel.wisMod), rw = formatRwResult(wisSave, wisProf),
                iconRes = R.drawable.icon_wis,
                skills = listOf(
                    Triple("Heilkunde", skillMod("Heilkunde"), prof("Heilkunde")),
                    Triple("Mit Tieren umg.", skillMod("Mit Tieren umg."), prof("Mit Tieren umg.")),
                    Triple("Motiv erkennen", skillMod("Motiv erkennen"), prof("Motiv erkennen")),
                    Triple("Überleben", skillMod("Überleben"), prof("Überleben")),
                    Triple("Wahrnehmung", skillMod("Wahrnehmung"), prof("Wahrnehmung"))
                )
            )
            AttributeBox(
                name = "CHA", value = viewModel.charisma.toString(),
                mod = formatMod(viewModel.chaMod), rw = formatRwResult(chaSave, chaProf),
                iconRes = R.drawable.icon_cha,
                skills = listOf(
                    Triple("Auftreten", skillMod("Auftreten"), prof("Auftreten")),
                    Triple("Einschüchtern", skillMod("Einschüchtern"), prof("Einschüchtern")),
                    Triple("Täuschen", skillMod("Täuschen"), prof("Täuschen")),
                    Triple("Überzeugen", skillMod("Überzeugen"), prof("Überzeugen"))
                )
            )

            HorizontalDivider(color = Bronze, thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Hintergrund & Persönlichkeit
            Text("Hintergrund & Besonderheiten", style = MaterialTheme.typography.titleMedium, color = Waldgruen)
            Spacer(modifier = Modifier.height(8.dp))
            PergamentCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (viewModel.characterData.name == "Athania") {
                        val accentColorLocal = MaterialTheme.colorScheme.tertiary
                        Text("Aussehen:", style = MaterialTheme.typography.labelLarge, color = accentColorLocal)
                        Text("Magisches Tattoo (Blutige Hand eines Kindes)", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sprachen:", style = MaterialTheme.typography.labelLarge, color = accentColorLocal)
                        Text("Gemeinsprache, Gebärden-Gemeinsprache, Halblingisch, Zwergisch, Elfisch", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                        HorizontalDivider(color = PergamentDunkel, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                        Text("Ideal (Höheres Ziel):", style = MaterialTheme.typography.labelLarge, color = accentColorLocal)
                        Text("Es ist die Verantwortung jeder Einzelnen, für das Wohl des Stammes zu sorgen.", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Makel (Nachtragend):", style = MaterialTheme.typography.labelLarge, color = accentColorLocal)
                        Text("Ich erinnere mich an jede einzelne Beleidigung, die mir galt, und hege eine stumme Abneigung gegen all jene, die mich schon einmal falsch behandelt haben.", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                    } else {
                        val accentColorLocal = MaterialTheme.colorScheme.tertiary
                        Text("Aussehen:", style = MaterialTheme.typography.labelLarge, color = accentColorLocal)
                        Text("Unscheinbar", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sprachen:", style = MaterialTheme.typography.labelLarge, color = accentColorLocal)
                        Text("Gemeinsprache, Elfisch, Zwergisch", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                        HorizontalDivider(color = PergamentDunkel, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                        Text("Ideal (Freiheit):", style = MaterialTheme.typography.labelLarge, color = accentColorLocal)
                        Text("Ketten sind dazu da um sie zu brechen, ebenso wie die, die sie halten.", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Makel:", style = MaterialTheme.typography.labelLarge, color = accentColorLocal)
                        Text("Ich würde alles behaupten, um zusätzlicher Arbeit aus dem Weg zu gehen.", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (viewModel.showCharacterEditDialog) {
        CharacterEditDialog(viewModel = viewModel)
    }

    if (viewModel.showLevelUpDialog) {
        LevelUpDialog(viewModel = viewModel)
    }

    if (viewModel.showRestWarningDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissRestWarningDialog() },
            title = { Text("Unzureichende Rationen", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.tertiary) },
            text = { Text("Du hast nicht genug Wasserschläuche (0.5 benötigt) oder Tagesrationen (1 benötigt) für eine vollständige Lange Rast. Willst du trotzdem rasten? (Es werden keine Rationen verbraucht, aber du erhältst keine HP oder Zauberslots zurück... oder wir ignorieren die Regeln für jetzt und rasten trotzdem ohne Ressourcen-Abzug?)", style = MaterialTheme.typography.bodySmall, color = TintenSchwarz) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.forceLongRestWithoutResources()
                        viewModel.dismissRestWarningDialog()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = PergamentHell),
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

private val AttributeIconSize = 150.dp

@Composable
fun AttributeBox(
    name: String,
    value: String,
    mod: String,
    rw: String,
    iconRes: Int? = null,
    skills: List<Triple<String, Int, Boolean>> = emptyList()
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .border(1.5.dp, EisenGrau, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFBEB5A0))
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            if (iconRes != null) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = "$name Icon",
                    modifier = Modifier.size(AttributeIconSize),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, style = MaterialTheme.typography.labelLarge, color = Waldgruen, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(value, style = GrenzeGotischStyle, color = TintenSchwarz)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(mod, style = GrenzeGotischSmall, color = MaterialTheme.colorScheme.tertiary)
                }
                Text(rw, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = TintenBraun)
                if (skills.isNotEmpty()) {
                    HorizontalDivider(color = TintenBraun.copy(alpha = 0.3f), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                    skills.forEach { (skillName, skillMod, isProficient) ->
                        SkillRow(skillName, skillMod, isProficient)
                    }
                }
            }
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
        val color = if (proficient) MaterialTheme.colorScheme.tertiary else TintenSchwarz
        val weight = if (proficient) FontWeight.Bold else FontWeight.Normal
        val modStr = if (mod >= 0) "+$mod" else "$mod"
        Text(name, color = color, style = MaterialTheme.typography.bodySmall, fontWeight = weight)
        Text(modStr, color = color, style = MaterialTheme.typography.bodySmall, fontWeight = weight)
    }
}
