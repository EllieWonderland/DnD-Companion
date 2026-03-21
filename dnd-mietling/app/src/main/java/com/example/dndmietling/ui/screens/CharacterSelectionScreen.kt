package com.example.dndmietling.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndmietling.data.MietlingCharacter
import com.example.dndmietling.ui.theme.*

private const val PREFS_NAME = "mietling_prefs"
private const val KEY_SELECTED_CHAR = "selected_character"

fun getPinKey(character: MietlingCharacter) = "pin_${character.name}"

fun getSavedCharacter(context: Context): MietlingCharacter? {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val name = prefs.getString(KEY_SELECTED_CHAR, null) ?: return null
    return try { MietlingCharacter.valueOf(name) } catch (e: Exception) { null }
}

fun getSavedPin(context: Context, character: MietlingCharacter): String? {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(getPinKey(character), null)
}

fun saveCharacterAndPin(context: Context, character: MietlingCharacter, pin: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit()
        .putString(KEY_SELECTED_CHAR, character.name)
        .putString(getPinKey(character), pin)
        .apply()
}

// Nur die Mietling-Charaktere (nicht Companion-Chars)
private val MIETLING_CHARS = listOf(
    MietlingCharacter.WARLOCK,
    MietlingCharacter.THARION,
    MietlingCharacter.SORA
)

@Composable
fun CharacterSelectionScreen(
    onCharacterSelected: (MietlingCharacter) -> Unit
) {
    val context = LocalContext.current
    val savedChar = remember { getSavedCharacter(context) }

    if (savedChar != null) {
        PinLoginScreen(
            character = savedChar,
            onSuccess = { onCharacterSelected(savedChar) },
            onSwitchCharacter = {
                // Clear saved character
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().remove(KEY_SELECTED_CHAR).apply()
            }
        )
    } else {
        FirstTimeSelectionScreen(onCharacterSelected = onCharacterSelected)
    }
}

@Composable
private fun FirstTimeSelectionScreen(
    onCharacterSelected: (MietlingCharacter) -> Unit
) {
    val context = LocalContext.current
    var selectedChar by remember { mutableStateOf<MietlingCharacter?>(null) }
    var pin by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Pergament),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "DnD Mietling",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Waldgruen
            )
            Text(
                "Wähle deinen Charakter",
                fontSize = 16.sp,
                color = TintenBraun
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Charakter-Auswahl
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MIETLING_CHARS.forEach { char ->
                    CharacterCard(
                        character = char,
                        isSelected = selectedChar == char,
                        onClick = { selectedChar = char }
                    )
                }
            }

            if (selectedChar != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Text("PIN festlegen (4 Zeichen)", color = TintenBraun, fontSize = 14.sp)

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4) pin = it },
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.6f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Waldgruen,
                        focusedLabelColor = Waldgruen
                    )
                )

                OutlinedTextField(
                    value = pinConfirm,
                    onValueChange = { if (it.length <= 4) pinConfirm = it },
                    label = { Text("PIN bestätigen") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.6f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Waldgruen,
                        focusedLabelColor = Waldgruen
                    )
                )

                if (errorMsg.isNotEmpty()) {
                    Text(errorMsg, color = OchsenblutRot, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        when {
                            pin.length < 4 -> errorMsg = "PIN muss 4 Zeichen lang sein"
                            pin != pinConfirm -> errorMsg = "PINs stimmen nicht überein"
                            else -> {
                                val char = selectedChar!!
                                saveCharacterAndPin(context, char, pin)
                                onCharacterSelected(char)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Waldgruen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Weiter", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PinLoginScreen(
    character: MietlingCharacter,
    onSuccess: () -> Unit,
    onSwitchCharacter: () -> Unit
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    var switchRequested by remember { mutableStateOf(false) }

    if (switchRequested) {
        onSwitchCharacter()
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Pergament),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("DnD Mietling", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Waldgruen)

            Text(character.emoji, fontSize = 64.sp)

            Text(
                character.displayName,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = TintenSchwarz
            )

            Text("PIN eingeben", color = TintenBraun, fontSize = 14.sp)

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4) pin = it },
                label = { Text("PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.5f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Waldgruen,
                    focusedLabelColor = Waldgruen
                )
            )

            if (errorMsg.isNotEmpty()) {
                Text(errorMsg, color = OchsenblutRot, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    val savedPin = getSavedPin(context, character)
                    if (pin == savedPin) {
                        onSuccess()
                    } else {
                        errorMsg = "Falscher PIN"
                        pin = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Waldgruen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text("Einloggen", fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = { switchRequested = true }) {
                Text("Charakter wechseln", color = TintenBraun, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun CharacterCard(
    character: MietlingCharacter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Waldgruen else PergamentDunkel,
                shape = RoundedCornerShape(12.dp)
            )
            .background(if (isSelected) Waldgruen.copy(alpha = 0.1f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(character.emoji, fontSize = 36.sp, textAlign = TextAlign.Center)
        Text(
            character.displayName,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Waldgruen else TintenSchwarz,
            textAlign = TextAlign.Center
        )
    }
}
