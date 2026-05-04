package com.example.dndcompanion.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.R
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.AuthState
import com.example.dndcompanion.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(authViewModel: AuthViewModel) {
    val authState by authViewModel.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val isLoading = authState is AuthState.Loading

    LaunchedEffect(authState) {
        if (authState is AuthState.PasswordResetSent) {
            showForgotPassword = false
            resetEmail = ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0A05))
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_premium_final),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            alpha = 0.15f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.splash_premium_final),
                contentDescription = "DnD Companion",
                modifier = Modifier
                    .size(180.dp)
                    .padding(bottom = 8.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "D&D Companion",
                fontFamily = Almendra,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PergamentHell,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Willkommen, Abenteurer",
                fontFamily = Almendra,
                fontSize = 14.sp,
                color = PergamentDunkel,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (!showForgotPassword) {
                LoginForm(
                    email = email,
                    onEmailChange = { email = it },
                    password = password,
                    onPasswordChange = { password = it },
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                    isLoading = isLoading,
                    authState = authState,
                    onLogin = {
                        focusManager.clearFocus()
                        authViewModel.login(email, password)
                    },
                    onForgotPassword = {
                        authViewModel.resetState()
                        showForgotPassword = true
                    },
                    focusManager = focusManager
                )
            } else {
                ForgotPasswordForm(
                    resetEmail = resetEmail,
                    onResetEmailChange = { resetEmail = it },
                    isLoading = isLoading,
                    authState = authState,
                    onSendReset = {
                        focusManager.clearFocus()
                        authViewModel.sendPasswordReset(resetEmail)
                    },
                    onBack = {
                        authViewModel.resetState()
                        showForgotPassword = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LoginForm(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityToggle: () -> Unit,
    isLoading: Boolean,
    authState: AuthState,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PergamentDunkel.copy(alpha = 0.92f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("E-Mail", fontFamily = Almendra, color = TintenBraun) },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Waldgruen,
                    unfocusedBorderColor = TintenBraun,
                    focusedTextColor = TintenSchwarz,
                    unfocusedTextColor = TintenSchwarz,
                    cursorColor = Waldgruen
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Passwort", fontFamily = Almendra, color = TintenBraun) },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onLogin() }),
                trailingIcon = {
                    IconButton(onClick = onPasswordVisibilityToggle) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Passwort verbergen" else "Passwort anzeigen",
                            tint = TintenBraun
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Waldgruen,
                    unfocusedBorderColor = TintenBraun,
                    focusedTextColor = TintenSchwarz,
                    unfocusedTextColor = TintenSchwarz,
                    cursorColor = Waldgruen
                ),
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(visible = authState is AuthState.Error) {
                Text(
                    text = (authState as? AuthState.Error)?.message ?: "",
                    color = OchsenblutRot,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp),
                    fontFamily = Almendra
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onLogin,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Waldgruen),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = PergamentHell, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Anmelden", fontFamily = Almendra, fontSize = 16.sp, color = PergamentHell)
                }
            }

            TextButton(
                onClick = onForgotPassword,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
            ) {
                Text("Passwort vergessen?", fontFamily = Almendra, color = TintenBraun, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ForgotPasswordForm(
    resetEmail: String,
    onResetEmailChange: (String) -> Unit,
    isLoading: Boolean,
    authState: AuthState,
    onSendReset: () -> Unit,
    onBack: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PergamentDunkel.copy(alpha = 0.92f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "Passwort zurücksetzen",
                fontFamily = Almendra,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TintenSchwarz,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Gib deine E-Mail-Adresse ein. Du erhältst einen Link zum Zurücksetzen.",
                fontFamily = Almendra,
                fontSize = 13.sp,
                color = TintenBraun,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = resetEmail,
                onValueChange = onResetEmailChange,
                label = { Text("E-Mail", fontFamily = Almendra, color = TintenBraun) },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onSendReset() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Waldgruen,
                    unfocusedBorderColor = TintenBraun,
                    focusedTextColor = TintenSchwarz,
                    unfocusedTextColor = TintenSchwarz,
                    cursorColor = Waldgruen
                ),
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(visible = authState is AuthState.Error) {
                Text(
                    text = (authState as? AuthState.Error)?.message ?: "",
                    color = OchsenblutRot,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp),
                    fontFamily = Almendra
                )
            }

            AnimatedVisibility(visible = authState is AuthState.PasswordResetSent) {
                Text(
                    text = "Reset-E-Mail gesendet! Prüfe deinen Posteingang.",
                    color = Waldgruen,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp),
                    fontFamily = Almendra
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onSendReset,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Waldgruen),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = PergamentHell, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Reset-E-Mail senden", fontFamily = Almendra, fontSize = 16.sp, color = PergamentHell)
                }
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
            ) {
                Text("Zurück zur Anmeldung", fontFamily = Almendra, color = TintenBraun, fontSize = 13.sp)
            }
        }
    }
}
