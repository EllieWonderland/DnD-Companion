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
import androidx.compose.foundation.Image
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
            DnDCompanionTheme {
                val viewModel: CharacterViewModel = viewModel()
                DnDApp(viewModel)
            }
        }
    }
}

@Composable
fun DnDApp(viewModel: CharacterViewModel) {
    // 0 = Athania, 1 = Urtier, 2 = Hilfe, 3 = Bücher
    var currentScreen by rememberSaveable { mutableStateOf(0) }

    BackHandler(enabled = currentScreen != 0) {
        currentScreen = 0
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = WaldgruenDunkel,
                contentColor = PergamentHell
            ) {
                NavigationBarItem(
                    selected = currentScreen == 0,
                    onClick = { currentScreen = 0 },
                    icon = { Text(if (viewModel.characterData.name == "Athania") "🧝‍♀️" else "🧙‍♂️") },
                    label = { Text(viewModel.characterData.name, fontFamily = com.example.dndcompanion.ui.theme.Almendra) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WaldGold,
                        selectedTextColor = WaldGold,
                        unselectedIconColor = PergamentDunkel,
                        unselectedTextColor = PergamentDunkel,
                        indicatorColor = Waldgruen
                    )
                )
                if (viewModel.characterData.charClass == com.example.dndcompanion.data.CharacterClass.RANGER) {
                    NavigationBarItem(
                        selected = currentScreen == 1,
                        onClick = { currentScreen = 1 },
                        icon = { Text("🐾") },
                        label = { Text("Urtier", fontFamily = com.example.dndcompanion.ui.theme.Almendra) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WaldGold,
                            selectedTextColor = WaldGold,
                            unselectedIconColor = PergamentDunkel,
                            unselectedTextColor = PergamentDunkel,
                            indicatorColor = Waldgruen
                        )
                    )
                }
                NavigationBarItem(
                    selected = currentScreen == 2, // Hilfe
                    onClick = { currentScreen = 2 },
                    icon = { Text("💬") },
                    label = { Text("Hilfe", fontFamily = com.example.dndcompanion.ui.theme.Almendra) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WaldGold,
                        selectedTextColor = WaldGold,
                        unselectedIconColor = PergamentDunkel,
                        unselectedTextColor = PergamentDunkel,
                        indicatorColor = Waldgruen
                    )
                )
                NavigationBarItem(
                    selected = currentScreen == 3, // Bücher
                    onClick = { currentScreen = 3 },
                    icon = { Text("📖") },
                    label = { Text("Bücher", fontFamily = com.example.dndcompanion.ui.theme.Almendra) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WaldGold,
                        selectedTextColor = WaldGold,
                        unselectedIconColor = PergamentDunkel,
                        unselectedTextColor = PergamentDunkel,
                        indicatorColor = Waldgruen
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (currentScreen) {
                0 -> AthaniaScreen(viewModel)
                1 -> {
                    if (viewModel.characterData.charClass == com.example.dndcompanion.data.CharacterClass.RANGER) {
                        CapyScreen(viewModel)
                    } else {
                        // Falls Warlock hier landet (sollte nicht passieren da das Icon weg ist, aber als Fallback Profil anzeigen)
                        ProfilScreen(viewModel)
                    }
                }
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
            containerColor = PergamentDunkel,
            contentColor = TintenSchwarz
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { 
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        } 
                    },
                    text = { Text(tab.title, fontFamily = Almendra) },
                    selectedContentColor = OchsenblutRot,
                    unselectedContentColor = TintenBraun
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
        com.example.dndcompanion.ui.viewmodel.BeastType.LAND -> WaldgruenDunkel
        com.example.dndcompanion.ui.viewmodel.BeastType.SKY -> HexenLila
        com.example.dndcompanion.ui.viewmodel.BeastType.SEA -> Bronze
    }
    val beastColorDark = when(viewModel.activeBeastType) {
        com.example.dndcompanion.ui.viewmodel.BeastType.LAND -> Waldgruen
        com.example.dndcompanion.ui.viewmodel.BeastType.SKY -> OchsenblutRot
        com.example.dndcompanion.ui.viewmodel.BeastType.SEA -> WaldGold
    }

    PergamentBackground {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header mit Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon_capybara),
                contentDescription = "Urtier Icon",
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Urtier-Begleiter", style = MaterialTheme.typography.titleLarge, color = Waldgruen, fontFamily = Almendra)
        }

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
                Text("HP: ${viewModel.capyCurrentHp} / ${viewModel.capyMaxHp}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = Almendra)

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { viewModel.capyCurrentHp.toFloat() / viewModel.capyMaxHp.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(16.dp),
                    color = if (viewModel.capyCurrentHp > 5) beastColorDark else OchsenblutRot,
                    trackColor = PergamentHell
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = { viewModel.takeCapyDamage(5) }, colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot), shape = RoundedCornerShape(8.dp)) { Text("-5", fontFamily = Almendra) }
                    Button(onClick = { viewModel.takeCapyDamage(1) }, colors = ButtonDefaults.buttonColors(containerColor = OchsenblutRot), shape = RoundedCornerShape(8.dp)) { Text("-1", fontFamily = Almendra) }
                    Button(onClick = { viewModel.healCapy(1) }, colors = ButtonDefaults.buttonColors(containerColor = Waldgruen), shape = RoundedCornerShape(8.dp)) { Text("+1", fontFamily = Almendra) }
                    Button(onClick = { viewModel.healCapy(5) }, colors = ButtonDefaults.buttonColors(containerColor = Waldgruen), shape = RoundedCornerShape(8.dp)) { Text("+5", fontFamily = Almendra) }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PergamentCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Rüstungsklasse (RK)", fontWeight = FontWeight.Bold, color = Waldgruen, fontFamily = Almendra)
                    Text("${viewModel.capyAc}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TintenSchwarz)
                }
                HorizontalDivider(color = PergamentDunkel, modifier = Modifier.padding(vertical = 8.dp))

                Text("Tempo: ${viewModel.capySpeed}", fontSize = 16.sp, color = TintenSchwarz)
                Text("Besonderheit: ${viewModel.capySpecial}", fontSize = 16.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = TintenBraun)

                HorizontalDivider(color = PergamentDunkel, modifier = Modifier.padding(vertical = 8.dp))

                Text("Bestienschlag (Kosten: 1 Bonusaktion)", fontWeight = FontWeight.Bold, color = Waldgruen, fontFamily = Almendra, modifier = Modifier.padding(bottom = 4.dp), fontSize = 16.sp)
                Text("Trefferbonus: ${viewModel.capyAttackBonus}", fontSize = 18.sp, color = TintenSchwarz)
                Text("Schaden: ${viewModel.capyDamage}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = beastColorDark)
            }
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
            .background(WaldgruenDunkel),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        options.forEach { (type, label) ->
            val isSelected = activeType == type
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (isSelected) Waldgruen else Color.Transparent)
                    .clickable { onTypeSelected(type) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) PergamentHell else PergamentDunkel,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 16.sp,
                    fontFamily = Almendra
                )
            }
        }
    }
}