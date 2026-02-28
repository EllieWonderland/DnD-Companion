package com.example.dndcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    }
}

@Composable
fun CapyScreen(viewModel: CharacterViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().background(GelbSand).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Der Toggle-Switch (Himmel / Land)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Land", color = PinkDunkel, fontWeight = FontWeight.Bold)
            Switch(
                checked = viewModel.isSkyBeast,
                onCheckedChange = { viewModel.toggleBeastType(it) },
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BlauDunkel,
                    checkedTrackColor = BlauHell,
                    uncheckedThumbColor = PinkDunkel,
                    uncheckedTrackColor = PinkHell
                )
            )
            Text("Himmel", color = BlauDunkel, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lebenspunkte Balken für Capy
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if(viewModel.isSkyBeast) BlauHell else PinkHell),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("HP: ${viewModel.capyCurrentHp} / ${viewModel.capyMaxHp}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { viewModel.capyCurrentHp.toFloat() / viewModel.capyMaxHp.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(12.dp),
                    color = if (viewModel.capyCurrentHp > 5) (if(viewModel.isSkyBeast) BlauDunkel else PinkDunkel) else Color.Red,
                    trackColor = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = { viewModel.takeCapyDamage(5) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))) { Text("-5") }
                    Button(onClick = { viewModel.takeCapyDamage(1) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))) { Text("-1") }
                    Button(onClick = { viewModel.healCapy(1) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) { Text("+1") }
                    Button(onClick = { viewModel.healCapy(5) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) { Text("+5") }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Basiswerte & Angriff
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Rüstungsklasse (RK)", fontWeight = FontWeight.Bold, color = BlauDunkel)
                    Text("${viewModel.capyAc}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = BlauDunkel)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Tempo: ${viewModel.capySpeed}", fontSize = 16.sp)
                Text("Besonderheit: ${viewModel.capySpecial}", fontSize = 16.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text("Bestienschlag (Kosten: 1 Bonusaktion)", fontWeight = FontWeight.Bold, color = BlauDunkel, modifier = Modifier.padding(bottom = 4.dp))
                Text("Trefferbonus: ${viewModel.capyAttackBonus}", fontSize = 18.sp)
                Text("Schaden: ${viewModel.capyDamage}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PinkDunkel)
            }
        }
    }
}