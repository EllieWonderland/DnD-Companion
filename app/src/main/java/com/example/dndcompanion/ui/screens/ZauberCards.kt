package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.Spell

@Composable
fun SpellCard(
    spell: Spell,
    isEditMode: Boolean = false,
    isEquipped: Boolean = false,
    customColor: Color = Waldgruen,
    onTogglePrep: () -> Unit = {},
    onDelete: (() -> Unit)? = null,
    onCastAsRitual: (() -> Unit)? = null,
    extraContent: (@Composable () -> Unit)? = null,
    globalSpellbook: List<com.example.dndcompanion.data.database.SpellEntity> = emptyList()
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { if (!isEditMode) expanded = !expanded }
            .border(1.dp, customColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = PergamentDunkel)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(spell.name, color = Waldgruen, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = Almendra)
                    val type = if (spell.level == 0) "Zaubertrick" else "Stufe ${spell.level}"
                    Text(type, color = TintenBraun, fontSize = 14.sp)
                }
                if (isEquipped) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Vorbereitet",
                        tint = Waldgruen,
                        modifier = Modifier.size(32.dp).padding(end = 4.dp)
                    )
                }
                if (isEditMode) {
                    Switch(
                        checked = spell.isPrepared,
                        onCheckedChange = { onTogglePrep() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.tertiary,
                            checkedTrackColor = Waldgruen,
                            uncheckedThumbColor = EisenGrau,
                            uncheckedTrackColor = PergamentDunkel
                        )
                    )
                }
            }
            if (expanded || isEditMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Zeit: ${spell.castingTime} | Dauer: ${spell.duration} | Reichweite: ${spell.range}", color = TintenSchwarz, fontSize = 14.sp, fontWeight = FontWeight.Medium)

                val comps = mutableListOf<String>()
                if (spell.componentsV) comps.add("V")
                if (spell.componentsS) comps.add("S")
                if (spell.componentsM) {
                    val m = if (spell.materialCost.isNotBlank()) "M (${spell.materialCost})" else "M"
                    comps.add(m)
                }
                if (comps.isNotEmpty()) {
                    Text("Komponenten: ${comps.joinToString(", ")}", color = OchsenblutRot, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))
                val fullDescription = globalSpellbook.find { it.name.equals(spell.name, ignoreCase = true) }?.description ?: spell.description
                Text(fullDescription, color = TintenSchwarz, fontSize = 15.sp, lineHeight = 20.sp)

                if (spell.isRitual && onCastAsRitual != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onCastAsRitual,
                        colors = ButtonDefaults.buttonColors(containerColor = Waldgruen, contentColor = PergamentHell),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Als Ritual wirken (+10 Min)", fontSize = 16.sp, color = PergamentHell, fontFamily = Almendra)
                    }
                }
                if (isEditMode && onDelete != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f), contentColor = PergamentHell),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Aus dem Buch löschen", fontSize = 16.sp, color = PergamentHell, fontFamily = Almendra)
                    }
                }
                if (extraContent != null) {
                    extraContent()
                }
            }
        }
    }
}

@Composable
fun TraitCard(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = PergamentDunkel)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = OchsenblutRot, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, color = TintenSchwarz, fontSize = 16.sp)
        }
    }
}

@Composable
fun EditableTraitCard(title: String, desc: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = PergamentDunkel)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(desc, color = TintenSchwarz, fontSize = 16.sp)
                }
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onEdit) {
                        Text("✏️ Bearbeiten", color = MaterialTheme.colorScheme.tertiary, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onDelete) {
                        Text("🗑️ Löschen", color = MaterialTheme.colorScheme.tertiary, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableFreeSpellCard(
    title: String,
    description: String,
    currentUses: Int,
    maxUses: Int,
    accentColor: Color,
    onCast: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = PergamentDunkel)
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = TintenSchwarz, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    if (!expanded) {
                        val subText = if (maxUses >= 999) "Beliebig oft" else ""
                        Text(subText, color = TintenBraun, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }

                if (maxUses < 999) {
                    Text("$currentUses / $maxUses", color = TintenSchwarz, modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onCast,
                    enabled = currentUses > 0,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) { Text("Wirken", fontSize = 16.sp, fontFamily = Almendra) }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Bronze.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(description, color = TintenSchwarz, fontSize = 14.sp, lineHeight = 18.sp)

                val usesInfo = if (maxUses >= 999) "Ein Beliebig oft wirkbarer Zauber." else "Regeneriert alle Nutzungen ($maxUses) nach einer Langen Rast."
                Spacer(modifier = Modifier.height(6.dp))
                Text(usesInfo, color = OchsenblutRot, fontSize = 13.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        }
    }
}
