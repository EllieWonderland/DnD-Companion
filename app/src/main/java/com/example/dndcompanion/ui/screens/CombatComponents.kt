package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.CombatViewModel

@Composable
fun WeaponButton(title: String, isSelected: Boolean, accentColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) accentColor else PergamentDunkel,
            contentColor = if (isSelected) (if (accentColor == WaldGold) TintenSchwarz else Color.White) else TintenSchwarz
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .defaultMinSize(minHeight = 64.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(text = title, fontFamily = Almendra, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 14.sp)
    }
}

@Composable
fun DeathSavesRow(combatVm: CombatViewModel) {
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showFailureDialog by remember { mutableStateOf(false) }

    LaunchedEffect(combatVm.deathSaveSuccesses) {
        if (combatVm.deathSaveSuccesses >= 3) showSuccessDialog = true
    }

    LaunchedEffect(combatVm.deathSaveFailures) {
        if (combatVm.deathSaveFailures >= 3) showFailureDialog = true
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Stabilisiert!", style = MaterialTheme.typography.titleSmall, color = TodRuneGruen) },
            text = { Text("Du hast 3 erfolgreiche Rettungswürfe geschafft. Du bist stabilisiert.", style = MaterialTheme.typography.bodyMedium, color = TintenSchwarz) },
            confirmButton = { Button(onClick = { showSuccessDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Waldgruen), shape = RoundedCornerShape(8.dp)) { Text("Puh!", fontFamily = Almendra) } },
            containerColor = PergamentHell,
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (showFailureDialog) {
        AlertDialog(
            onDismissRequest = { showFailureDialog = false },
            title = { Text("Gefallen...", style = MaterialTheme.typography.titleSmall, color = TodRuneRot) },
            text = { Text("Du hast 3 fehlgeschlagene Rettungswürfe ereilt. Der Charakter ist gestorben...", style = MaterialTheme.typography.bodyMedium, color = TintenSchwarz) },
            confirmButton = { Button(onClick = { showFailureDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot), shape = RoundedCornerShape(8.dp)) { Text("RiP", fontFamily = Almendra) } },
            containerColor = PergamentHell,
            shape = RoundedCornerShape(12.dp)
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Todesrettungswürfe", style = MaterialTheme.typography.labelLarge, color = TintenSchwarz)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            // Erfolge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Erfolge:", style = MaterialTheme.typography.labelMedium, color = TodRuneGruen)
                Spacer(modifier = Modifier.width(4.dp))
                repeat(3) { index ->
                    val checked = index < combatVm.deathSaveSuccesses
                    Icon(
                        imageVector = if (checked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Erfolg",
                        tint = if (checked) TodRuneGruen else EisenGrau,
                        modifier = Modifier.size(24.dp).clickable {
                            if (checked) combatVm.updateDeathSaves(index, combatVm.deathSaveFailures)
                            else combatVm.updateDeathSaves(index + 1, combatVm.deathSaveFailures)
                        }
                    )
                }
            }
            // Fehlschläge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Fehlschläge:", style = MaterialTheme.typography.labelMedium, color = TodRuneRot)
                Spacer(modifier = Modifier.width(4.dp))
                repeat(3) { index ->
                    val checked = index < combatVm.deathSaveFailures
                    Icon(
                        imageVector = if (checked) Icons.Default.Cancel else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Fehlschlag",
                        tint = if (checked) TodRuneRot else EisenGrau,
                        modifier = Modifier.size(24.dp).clickable {
                            if (checked) combatVm.updateDeathSaves(combatVm.deathSaveSuccesses, index)
                            else combatVm.updateDeathSaves(combatVm.deathSaveSuccesses, index + 1)
                        }
                    )
                }
            }
        }
    }
}
