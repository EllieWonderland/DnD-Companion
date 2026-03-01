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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.dndcompanion.ui.screens.CombatScreen
import com.example.dndcompanion.ui.screens.RucksackScreen
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.screens.ZauberScreen
import com.example.dndcompanion.ui.screens.HelpScreen
import com.example.dndcompanion.ui.screens.ProfilScreen
import com.example.dndcompanion.ui.theme.*

// Definition der Tabs für bessere Lesbarkeit
enum class AthaniaTab(val title: String) {
    Profil("Profil"),
    Kampf("Kampf"),
    Zauber("Zauber"),
    Rucksack("Rucksack"),
    Hilfe("Hilfe")
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
    // 0 = Athania, 1 = Capy, 2 = Hilfe
    var currentScreen by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = BlauHell) {
                NavigationBarItem(
                    selected = currentScreen == 0,
                    onClick = { currentScreen = 0 },
                    icon = { Text("🧝‍♀️") },
                    label = { Text("Athania") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = BlauDunkel)
                )
                NavigationBarItem(
                    selected = currentScreen == 1,
                    onClick = { currentScreen = 1 },
                    icon = { Text("🐾") },
                    label = { Text("Capy") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = BlauDunkel)
                )
                NavigationBarItem(
                    selected = currentScreen == 2, // NEU: Hilfe wieder hier
                    onClick = { currentScreen = 2 },
                    icon = { Text("📚") },
                    label = { Text("Hilfe") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = BlauDunkel)
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (currentScreen) {
                0 -> AthaniaScreen(viewModel)
                1 -> CapyScreen(viewModel)
                2 -> HelpScreen(viewModel)
            }
        }
    }
}

@Composable
fun AthaniaScreen(viewModel: CharacterViewModel) {
    // Hier entfernen wir den Hilfe-Tab, da er jetzt in der Haupt-Navi ist
    var selectedTab by rememberSaveable { mutableStateOf(AthaniaTab.Kampf) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = GelbSand,
            contentColor = BlauDunkel
        ) {
            // Wir filtern den Hilfe-Tab aus der oberen Leiste heraus
            AthaniaTab.entries.filter { it != AthaniaTab.Hilfe }.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.title) },
                    selectedContentColor = PinkDunkel,
                    unselectedContentColor = BlauDunkel
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                AthaniaTab.Profil -> ProfilScreen(viewModel)
                AthaniaTab.Kampf -> CombatScreen(viewModel, onNavigateToRucksack = { selectedTab = AthaniaTab.Rucksack })
                AthaniaTab.Zauber -> ZauberScreen(viewModel)
                AthaniaTab.Rucksack -> RucksackScreen(viewModel)
                else -> ProfilScreen(viewModel) // Fallback
            }
        }
    }
}

@Composable
fun CapyScreen(viewModel: CharacterViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().background(GelbSand).padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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