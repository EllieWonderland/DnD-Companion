package com.example.dndcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dndcompanion.ui.theme.ui.CombatScreen
import com.example.dndcompanion.ui.theme.ui.RucksackScreen
import com.example.dndcompanion.ui.theme.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.theme.ui.ZauberScreen

// Farben
val BlauDunkel = Color(0xFF61A0AF)
val BlauHell = Color(0xFF96C9DC)
val PinkDunkel = Color(0xFFF06C9B)
val PinkHell = Color(0xFFF9B9B7)
val GelbSand = Color(0xFFF5D491)

// Definition der Tabs für bessere Lesbarkeit
enum class AthaniaTab(val title: String) {
    Kampf("Kampf"),
    Zauber("Zauber"),
    Rucksack("Rucksack")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val viewModel: CharacterViewModel = viewModel()
                DnDApp(viewModel)
            }
        }
    }
}

@Composable
fun DnDApp(viewModel: CharacterViewModel) {
    // rememberSaveable behält den Zustand auch beim Drehen des Bildschirms
    var isAthaniaScreen by rememberSaveable { mutableStateOf(true) }

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
        // Padding vom Scaffold beachten
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (isAthaniaScreen) {
                AthaniaScreen(viewModel)
            } else {
                CapyScreen(viewModel)
            }
        }
    }
}

@Composable
fun AthaniaScreen(viewModel: CharacterViewModel) {
    // State für die Tabs (jetzt mit Enum statt Index-Zahlen)
    var selectedTab by rememberSaveable { mutableStateOf(AthaniaTab.Kampf) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = GelbSand,
            contentColor = BlauDunkel
        ) {
            AthaniaTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.title) },
                    selectedContentColor = PinkDunkel,
                    unselectedContentColor = BlauDunkel
                )
            }
        }

        // Content-Bereich
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                AthaniaTab.Kampf -> CombatScreen(viewModel)
                AthaniaTab.Zauber -> ZauberScreen(viewModel)
                AthaniaTab.Rucksack -> RucksackScreen(viewModel)
            }
        }
    } // <-- Hier fehlte die schließende Klammer für Column
} // <-- Hier fehlte die schließende Klammer für AthaniaScreen

@Composable
fun CapyScreen(viewModel: CharacterViewModel) {
    var isSkyBeast by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GelbSand)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Land", color = PinkDunkel)
            Switch(
                checked = isSkyBeast,
                onCheckedChange = { isSkyBeast = it },
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BlauDunkel,
                    checkedTrackColor = BlauHell,
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