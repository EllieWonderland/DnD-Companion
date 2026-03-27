package com.example.dndcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.dndcompanion.ui.viewmodel.GroupViewModel
import com.example.dndcompanion.ui.viewmodel.GroupViewModelFactory
import com.example.dndcompanion.ui.viewmodel.InventoryViewModel
import com.example.dndcompanion.ui.viewmodel.InventoryViewModelFactory
import com.example.dndcompanion.ui.viewmodel.SpellViewModel
import com.example.dndcompanion.ui.viewmodel.SpellViewModelFactory
import com.example.dndcompanion.ui.viewmodel.CombatViewModel
import com.example.dndcompanion.ui.viewmodel.CombatViewModelFactory
import com.example.dndcompanion.ui.screens.ZauberScreen
import com.example.dndcompanion.ui.screens.HelpScreen
import com.example.dndcompanion.ui.screens.ProfilScreen
import com.example.dndcompanion.ui.screens.BucherScreen
import com.example.dndcompanion.ui.screens.FeatureSelectionScreen
import com.example.dndcompanion.ui.theme.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
        setTheme(R.style.Theme_DnDCompanion)
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: CharacterViewModel = viewModel()
            val isRanger = viewModel.characterData.charClass == com.example.dndcompanion.data.CharacterClass.RANGER

            DnDCompanionTheme(isRanger = isRanger) {
                val groupViewModel: GroupViewModel = viewModel(
                    factory = GroupViewModelFactory(application, viewModel)
                )
                val inventoryViewModel: InventoryViewModel = viewModel(
                    factory = InventoryViewModelFactory(application, viewModel)
                )
                val spellViewModel: SpellViewModel = viewModel(
                    factory = SpellViewModelFactory(application, viewModel)
                )
                val combatViewModel: CombatViewModel = viewModel(
                    factory = CombatViewModelFactory(application, viewModel, spellViewModel, inventoryViewModel)
                )
                // Wire up cross-references
                inventoryViewModel.combatVm = combatViewModel
                spellViewModel.inventoryVm = inventoryViewModel
                viewModel.connectSiblings(combatViewModel, spellViewModel, inventoryViewModel)

                DnDApp(viewModel, groupViewModel, combatViewModel, spellViewModel, inventoryViewModel)
            }
        }
    }
}

@Composable
fun DnDApp(
    viewModel: CharacterViewModel,
    groupViewModel: GroupViewModel,
    combatViewModel: CombatViewModel,
    spellViewModel: SpellViewModel,
    inventoryViewModel: InventoryViewModel
) {
    // 0 = Athania, 1 = Urtier, 2 = Hilfe, 3 = Bücher
    var currentScreen by rememberSaveable { mutableStateOf(0) }
    var previousScreen by rememberSaveable { mutableStateOf(0) }
    val lastScreen = remember { mutableStateOf(currentScreen) }
    SideEffect {
        if (lastScreen.value != currentScreen) {
            previousScreen = lastScreen.value
            lastScreen.value = currentScreen
        }
    }

    var introOpacity by remember { mutableStateOf(1f) }
    var introFinished by remember { mutableStateOf(false) }

    // Intro Animation Logik für sanftes Ausfaden
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000) // 2 Sekunden voll sichtbar
        introOpacity = 0f              // Startet Fade-Out
        kotlinx.coroutines.delay(1000) // Wartet auf Fade-Dauer (1s)
        introFinished = true
    }

    BackHandler(enabled = currentScreen != 0) {
        currentScreen = previousScreen
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
                    icon = { 
                        val avatarId = if (viewModel.characterData.name == "Athania") R.drawable.athania else R.drawable.delat
                        Image(
                            painter = painterResource(id = avatarId),
                            contentDescription = viewModel.characterData.name,
                            modifier = Modifier.size(38.dp).clip(androidx.compose.foundation.shape.CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    },
                    label = { Text(viewModel.characterData.name, fontFamily = com.example.dndcompanion.ui.theme.Almendra) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WaldGold,
                        selectedTextColor = WaldGold,
                        unselectedIconColor = PergamentDunkel,
                        unselectedTextColor = PergamentDunkel,
                        indicatorColor = Waldgruen
                    )
                )
                if (viewModel.characterData.charClass == com.example.dndcompanion.data.CharacterClass.RANGER || viewModel.activeCharacterId == "Delat") {
                    NavigationBarItem(
                        selected = currentScreen == 1,
                        onClick = { currentScreen = 1 },
                        icon = { 
                            val iconRes = if (viewModel.activeCharacterId == "Delat") R.drawable.vertrauter else R.drawable.icon_capybara
                            Image(
                                painter = painterResource(id = iconRes),
                                contentDescription = "Begleiter",
                                modifier = Modifier.size(38.dp),
                                contentScale = ContentScale.Fit
                            )
                        },
                        label = { Text("Begleiter", fontFamily = com.example.dndcompanion.ui.theme.Almendra) },
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
                    icon = { 
                        Image(
                            painter = painterResource(id = R.drawable.hilfe),
                            contentDescription = "Hilfe",
                            modifier = Modifier.size(38.dp)
                        )
                    },
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
                    icon = { 
                        Image(
                            painter = painterResource(id = R.drawable.bucher),
                            contentDescription = "Bücher",
                            modifier = Modifier.size(38.dp)
                        )
                    },
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
                0 -> AthaniaScreen(viewModel, groupViewModel, combatViewModel, spellViewModel, inventoryViewModel)
                1 -> {
                    if (viewModel.characterData.charClass == com.example.dndcompanion.data.CharacterClass.RANGER || viewModel.activeCharacterId == "Delat") {
                        CompanionScreen(viewModel)
                    } else {
                        ProfilScreen(viewModel, combatViewModel)
                    }
                }
                2 -> HelpScreen(viewModel, onNavigateToRulebook = { chapter, search -> 
                    viewModel.targetRulebookChapter = chapter
                    viewModel.targetRulebookSearch = search
                    currentScreen = 3
                })
                3 -> BucherScreen(viewModel, groupViewModel)
            }
        }

        // --- PREMIUM INTRO OVERLAY ---
        if (!introFinished) {
            val alpha by animateFloatAsState(
                targetValue = introOpacity,
                animationSpec = tween(durationMillis = 1000),
                label = "SplashFade"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F0A05)), // Dunkler Holzhintergrund
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_premium_final),
                    contentDescription = "Premium Intro",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit, // Fit statt Crop, damit keine Ränder abgeschnitten werden
                    alpha = alpha
                )
            }
        }
    }

    if (viewModel.showFeatureSelection) {
        FeatureSelectionScreen(viewModel = viewModel, onDismiss = { viewModel.showFeatureSelection = false })
    }
}

@Composable
fun AthaniaScreen(
    viewModel: CharacterViewModel,
    groupViewModel: GroupViewModel,
    combatViewModel: CombatViewModel,
    spellViewModel: SpellViewModel,
    inventoryViewModel: InventoryViewModel
) {
    val tabs = AthaniaTab.entries.filter { it != AthaniaTab.Hilfe }
    val pagerState = rememberPagerState(initialPage = tabs.indexOf(AthaniaTab.Kampf), pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    var previousPage by remember { mutableIntStateOf(tabs.indexOf(AthaniaTab.Kampf)) }
    LaunchedEffect(Unit) {
        var lastSettledPage = pagerState.settledPage
        snapshotFlow { pagerState.settledPage }.collect { settledPage ->
            if (settledPage != lastSettledPage) {
                previousPage = lastSettledPage
                lastSettledPage = settledPage
            }
        }
    }

    BackHandler(enabled = pagerState.currentPage != tabs.indexOf(AthaniaTab.Kampf)) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(previousPage)
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
                    icon = {
                        val iconRes = when(tab) {
                            AthaniaTab.Profil -> if (viewModel.characterData.name == "Athania") R.drawable.athania else R.drawable.delat
                            AthaniaTab.Kampf -> R.drawable.kampf
                            AthaniaTab.Zauber -> R.drawable.zauberbuch
                            AthaniaTab.Rucksack -> R.drawable.rucksack
                            else -> R.drawable.kampf
                        }
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).clip(if(tab == AthaniaTab.Profil) androidx.compose.foundation.shape.CircleShape else RoundedCornerShape(0.dp)),
                            contentScale = ContentScale.Crop
                        )
                    },
                    text = { Text(tab.title, fontFamily = Almendra, fontSize = 10.sp) },
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
                AthaniaTab.Profil -> ProfilScreen(viewModel, combatViewModel)
                AthaniaTab.Kampf -> CombatScreen(
                    viewModel = viewModel,
                    combatVm = combatViewModel,
                    inventoryVm = inventoryViewModel,
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
                AthaniaTab.Zauber -> ZauberScreen(viewModel, spellViewModel, combatViewModel)
                AthaniaTab.Rucksack -> RucksackScreen(viewModel, inventoryViewModel, groupViewModel)
                else -> ProfilScreen(viewModel, combatViewModel)
            }
        }
    }
}

@Composable
fun CompanionScreen(viewModel: CharacterViewModel) {
    val companion = viewModel.companionData
    val isRanger = viewModel.characterData.charClass == com.example.dndcompanion.data.CharacterClass.RANGER
    val isDead = viewModel.companionIsDead
    
    val companionName = companion?.name ?: if(isRanger) "Urtier-Begleiter" else "Sphinx des Wunders"
    val portraitRes = if(isRanger) R.drawable.icon_capybara else R.drawable.vertrauter

    val beastColorLight = if(isRanger) {
        when(viewModel.activeBeastType) {
            com.example.dndcompanion.ui.viewmodel.BeastType.LAND -> WaldgruenDunkel
            com.example.dndcompanion.ui.viewmodel.BeastType.SKY -> HexenLila
            com.example.dndcompanion.ui.viewmodel.BeastType.SEA -> Bronze
        }
    } else HexenLila

    val beastColorDark = if(isRanger) {
        when(viewModel.activeBeastType) {
            com.example.dndcompanion.ui.viewmodel.BeastType.LAND -> Waldgruen
            com.example.dndcompanion.ui.viewmodel.BeastType.SKY -> OchsenblutRot
            com.example.dndcompanion.ui.viewmodel.BeastType.SEA -> WaldGold
        }
    } else WaldGold

    PergamentBackground {
    Column(
        modifier = Modifier.fillMaxSize().padding(8.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = portraitRes),
                contentDescription = "Begleiter Portrait",
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(3.dp, WaldGold, RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Fit,
                alpha = if(isDead) 0.5f else 1f
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(companionName, style = MaterialTheme.typography.titleLarge, color = if(isDead) EisenGrau else Waldgruen, fontFamily = Almendra)
            if (companion != null) {
                Text(companion.typ_und_gesinnung, style = MaterialTheme.typography.labelMedium, color = TintenBraun)
            }
        }

        if (isRanger && !isDead) {
            SegmentedBeastControl(
                activeType = viewModel.activeBeastType,
                onTypeSelected = { viewModel.toggleBeastType(it) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isDead) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = OchsenblutRot.copy(alpha = 0.15f)),
                border = BorderStroke(2.dp, OchsenblutRot),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("BEGLEITER IST GEFALLEN", color = OchsenblutRot, fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = Almendra)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.reviveCompanion() },
                        colors = ButtonDefaults.buttonColors(containerColor = Waldgruen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if(isRanger) "Urtier beleben (1 Zauberslot)" else "Vertrauten neu beschwören", fontFamily = Almendra)
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = beastColorLight),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val capyHpProgress by animateFloatAsState(
                        targetValue = if (viewModel.capyMaxHp > 0) viewModel.capyCurrentHp.toFloat() / viewModel.capyMaxHp.toFloat() else 0f,
                        animationSpec = tween(durationMillis = 500),
                        label = "Companion HP Animation"
                    )

                    Text("HP: ${viewModel.capyCurrentHp} / ${viewModel.capyMaxHp}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = Almendra)

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { capyHpProgress },
                        modifier = Modifier.fillMaxWidth().height(16.dp),
                        color = if (viewModel.capyCurrentHp > (viewModel.capyMaxHp / 4)) beastColorDark else OchsenblutRot,
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
                    
                    if (companion != null) {
                        Text("Sinne: ${companion.sinne}", fontSize = 14.sp, color = TintenBraun)
                        Text("Sprachen: ${companion.sprachen}", fontSize = 14.sp, color = TintenBraun)
                        HorizontalDivider(color = PergamentDunkel, modifier = Modifier.padding(vertical = 8.dp))
                    }

                    Text("Merkmale:", fontWeight = FontWeight.Bold, color = Waldgruen, fontFamily = Almendra, fontSize = 16.sp)
                    Text(viewModel.capySpecial, fontSize = 14.sp, color = TintenSchwarz)

                    HorizontalDivider(color = PergamentDunkel, modifier = Modifier.padding(vertical = 8.dp))

                    Text("Aktionen:", fontWeight = FontWeight.Bold, color = Waldgruen, fontFamily = Almendra, modifier = Modifier.padding(bottom = 4.dp), fontSize = 16.sp)
                    Text("Angriffsbonus: ${viewModel.capyAttackBonus}", fontSize = 18.sp, color = TintenSchwarz)
                    Text("Schaden: ${viewModel.capyDamage}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = beastColorDark)
                    
                    companion?.aktionen?.forEach { aktion ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${aktion.name}:", fontWeight = FontWeight.Bold, color = TintenSchwarz, fontSize = 14.sp)
                        Text(aktion.beschreibung ?: "", fontSize = 14.sp, color = TintenBraun)
                    }
                    
                    companion?.reaktionen?.let { reaktionen ->
                        if (reaktionen.isNotEmpty()) {
                            HorizontalDivider(color = PergamentDunkel, modifier = Modifier.padding(vertical = 8.dp))
                            Text("Reaktionen:", fontWeight = FontWeight.Bold, color = Waldgruen, fontFamily = Almendra, fontSize = 16.sp)
                            reaktionen.forEach { reaktion ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${reaktion.name}:", fontWeight = FontWeight.Bold, color = TintenSchwarz, fontSize = 14.sp)
                                if (reaktion.ausloeser != null) Text("Auslöser: ${reaktion.ausloeser}", fontSize = 13.sp, color = TintenBraun)
                                if (reaktion.antwort != null) Text("Antwort: ${reaktion.antwort}", fontSize = 13.sp, color = TintenBraun)
                            }
                        }
                    }
                }
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