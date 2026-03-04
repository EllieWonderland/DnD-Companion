package com.example.dndcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import com.example.dndcompanion.ui.screens.CombatScreen
import com.example.dndcompanion.ui.screens.RucksackScreen
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.screens.ZauberScreen
import com.example.dndcompanion.ui.screens.HelpScreen
import com.example.dndcompanion.ui.screens.ProfilScreen
import com.example.dndcompanion.ui.screens.BucherScreen
import com.example.dndcompanion.ui.theme.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch

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
    // 0 = Athania, 1 = Capy, 2 = Hilfe, 3 = Bücher
    var currentScreen by rememberSaveable { mutableStateOf(0) }

    BackHandler(enabled = currentScreen != 0) {
        currentScreen = 0
    }

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
                    selected = currentScreen == 2, // Hilfe
                    onClick = { currentScreen = 2 },
                    icon = { Text("💬") },
                    label = { Text("Hilfe") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = BlauDunkel)
                )
                NavigationBarItem(
                    selected = currentScreen == 3, // Bücher
                    onClick = { currentScreen = 3 },
                    icon = { Text("📖") },
                    label = { Text("Bücher") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = BlauDunkel)
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (currentScreen) {
                0 -> AthaniaScreen(viewModel)
                1 -> CapyScreen(viewModel)
                2 -> HelpScreen(viewModel, onNavigateToRulebook = { chapter, search -> 
                    viewModel.targetRulebookChapter = chapter
                    viewModel.targetRulebookSearch = search
                    currentScreen = 3
                })
                3 -> BucherScreen(viewModel)
            }
        }
    }
}

@Composable
fun AthaniaScreen(viewModel: CharacterViewModel) {
    val tabs = AthaniaTab.entries.filter { it != AthaniaTab.Hilfe }
    val pagerState = rememberPagerState(initialPage = tabs.indexOf(AthaniaTab.Kampf), pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = pagerState.currentPage != tabs.indexOf(AthaniaTab.Kampf)) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(tabs.indexOf(AthaniaTab.Kampf))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = GelbSand,
            contentColor = BlauDunkel
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { 
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        } 
                    },
                    text = { Text(tab.title) },
                    selectedContentColor = PinkDunkel,
                    unselectedContentColor = BlauDunkel
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (tabs[page]) {
                AthaniaTab.Profil -> ProfilScreen(viewModel)
                AthaniaTab.Kampf -> CombatScreen(viewModel, 
                    onNavigateToRucksack = { 
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(tabs.indexOf(AthaniaTab.Rucksack))
                        }
                    },
                    onNavigateToProfile = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(tabs.indexOf(AthaniaTab.Profil))
                        }
                    }
                )
                AthaniaTab.Zauber -> ZauberScreen(viewModel)
                AthaniaTab.Rucksack -> RucksackScreen(viewModel)
                else -> ProfilScreen(viewModel)
            }
        }
    }
}

@Composable
fun CapyScreen(viewModel: CharacterViewModel) {
    val beastColorLight = when(viewModel.activeBeastType) {
        com.example.dndcompanion.ui.viewmodel.BeastType.LAND -> Gruen
        com.example.dndcompanion.ui.viewmodel.BeastType.SKY -> PinkHell
        com.example.dndcompanion.ui.viewmodel.BeastType.SEA -> BlauHell
    }
    val beastColorDark = when(viewModel.activeBeastType) {
        com.example.dndcompanion.ui.viewmodel.BeastType.LAND -> PinkDunkel
        com.example.dndcompanion.ui.viewmodel.BeastType.SKY -> BlauDunkel
        com.example.dndcompanion.ui.viewmodel.BeastType.SEA -> Color(0xFF388E3C)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(GelbSand).padding(8.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SegmentedBeastControl(
            activeType = viewModel.activeBeastType,
            onTypeSelected = { viewModel.toggleBeastType(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = beastColorLight),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("HP: ${viewModel.capyCurrentHp} / ${viewModel.capyMaxHp}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { viewModel.capyCurrentHp.toFloat() / viewModel.capyMaxHp.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(16.dp),
                    color = if (viewModel.capyCurrentHp > 5) beastColorDark else Color.Red,
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

                Text("Bestienschlag (Kosten: 1 Bonusaktion)", fontWeight = FontWeight.Bold, color = BlauDunkel, modifier = Modifier.padding(bottom = 4.dp), fontSize = 16.sp)
                Text("Trefferbonus: ${viewModel.capyAttackBonus}", fontSize = 18.sp)
                Text("Schaden: ${viewModel.capyDamage}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = beastColorDark)
            }
        }
    }
}

@Composable
fun SegmentedBeastControl(activeType: com.example.dndcompanion.ui.viewmodel.BeastType, onTypeSelected: (com.example.dndcompanion.ui.viewmodel.BeastType) -> Unit) {
    val options = listOf(
        com.example.dndcompanion.ui.viewmodel.BeastType.LAND to "Land",
        com.example.dndcompanion.ui.viewmodel.BeastType.SKY to "Himmel",
        com.example.dndcompanion.ui.viewmodel.BeastType.SEA to "Meer"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(BlauHell),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        options.forEach { (type, label) ->
            val isSelected = activeType == type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (isSelected) BlauDunkel else Color.Transparent)
                    .clickable { onTypeSelected(type) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 16.sp
                )
            }
        }
    }
}