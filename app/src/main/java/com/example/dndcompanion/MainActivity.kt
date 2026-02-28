package com.example.dndcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dndcompanion.ui.theme.ui.CombatScreen
import com.example.dndcompanion.ui.theme.ui.RucksackScreen
import com.example.dndcompanion.ui.theme.viewmodel.CharacterViewModel

// Falls du die Farben noch nicht ausgelagert hast, hier zur Sicherheit nochmal:
val BlauDunkel = Color(0xFF61A0AF)
val BlauHell = Color(0xFF96C9DC)
val PinkDunkel = Color(0xFFF06C9B)
val PinkHell = Color(0xFFF9B9B7)
val GelbSand = Color(0xFFF5D491)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Hier wird das Gehirn der App (ViewModel) erschaffen und an alle Unterseiten weitergegeben
                val viewModel: CharacterViewModel = viewModel()
                DnDApp(viewModel)
            }
        }
    }
}

@Composable
fun DnDApp(viewModel: CharacterViewModel) {
    // Steuert die untere Navigation
    var isAthaniaScreen by remember { mutableStateOf(true) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = BlauHell) {
                NavigationBarItem(
                    selected = isAthaniaScreen,
                    onClick = { isAthaniaScreen = true },
                    icon = { Text("🧝‍♀️") },
                    label = { Text("Athania") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        indicatorColor = BlauDunkel
                    )
                )
                NavigationBarItem(
                    selected = !isAthaniaScreen,
                    onClick = { isAthaniaScreen = false },
                    icon = { Text("🐾") },
                    label = { Text("Capy") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        indicatorColor = BlauDunkel
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (isAthaniaScreen) {
                AthaniaScreen(viewModel)
            } else {
                CapyScreen(viewModel) // Das bauen wir später noch richtig aus
            }
        }
    }
}

@Composable
fun AthaniaScreen(viewModel: CharacterViewModel) {
    // Steuert die oberen Tabs für Athania
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Kampf", "Zauber", "Rucksack")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = GelbSand,
            contentColor = BlauDunkel
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                    selectedContentColor = PinkDunkel,
                    unselectedContentColor = BlauDunkel
                )
            }
        }

        // Zeigt den Screen an, der zum ausgewählten Tab gehört
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> CombatScreen(viewModel)
                1 -> Box(modifier = Modifier.fillMaxSize().background(GelbSand), contentAlignment = Alignment.Center) {
                    Text("Hier bauen wir gleich den Zauber-Screen hin!", color = BlauDunkel)
                }
                2 -> RucksackScreen(viewModel)
            }
        }
    }
}

@Composable
fun CapyScreen(viewModel: CharacterViewModel) {
    var isSkyBeast by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize().background(GelbSand).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Farben getauscht: Land ist jetzt Pink, Himmel ist Blau
            Text("Land", color = PinkDunkel)
            Switch(
                checked = isSkyBeast,
                onCheckedChange = { isSkyBeast = it },
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = SwitchDefaults.colors(
                    // Wenn Himmel aktiv ist (checked) = Blau
                    checkedThumbColor = BlauDunkel,
                    checkedTrackColor = BlauHell,
                    // Wenn Land aktiv ist (unchecked) = Pink
                    uncheckedThumbColor = PinkDunkel,
                    uncheckedTrackColor = PinkHell
                )
            )
            Text("Himmel", color = BlauDunkel)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isSkyBeast) {
            Text("Werte für Urtier des Himmels (Work in Progress)", color = BlauDunkel)
        } else {
            Text("Werte für Urtier des Landes (Work in Progress)", color = PinkDunkel)
        }
    }
}