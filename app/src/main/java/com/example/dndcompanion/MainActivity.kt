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
import com.example.dndcompanion.ui.screens.LoginScreen
import com.example.dndcompanion.ui.screens.RucksackScreen
import com.example.dndcompanion.ui.viewmodel.AuthViewModel
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
import com.example.dndcompanion.ui.screens.CharacterSetupScreen
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

// Definition der Tabs fÃ¼r bessere Lesbarkeit
enum class AthaniaTab(val title: String) {
    Profil("Profil"),
    Kampf("Kampf"),
    Zauber("Zauber"),
    Rucksack("Rucksack"),
    Hilfe("Chat")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_DnDCompanion)
        super.onCreate(savedInstanceState)
        setContent {
            val authViewModel: AuthViewModel = viewModel()
            val currentUser by authViewModel.currentUser.collectAsState()

            val characterViewModel: CharacterViewModel = viewModel()
            val isRanger = characterViewModel.characterData.charClass == com.example.dndcompanion.data.CharacterClass.RANGER

            LaunchedEffect(currentUser?.uid) {
                val uid = currentUser?.uid ?: return@LaunchedEffect
                characterViewModel.loadUserCharacter(uid)
                characterViewModel.checkSetupComplete(uid)
            }

            DnDCompanionTheme(isRanger = isRanger) {
                if (currentUser == null) {
                    LoginScreen(authViewModel)
                } else {
                    val groupViewModel: GroupViewModel = viewModel(
                        factory = GroupViewModelFactory(application, characterViewModel)
                    )
                    val inventoryViewModel: InventoryViewModel = viewModel(
                        factory = InventoryViewModelFactory(application, characterViewModel)
                    )
                    val spellViewModel: SpellViewModel = viewModel(
                        factory = SpellViewModelFactory(application, characterViewModel)
                    )
                    val combatViewModel: CombatViewModel = viewModel(
                        factory = CombatViewModelFactory(application, characterViewModel, spellViewModel, inventoryViewModel)
                    )
                    // Wire up cross-references
                    inventoryViewModel.combatVm = combatViewModel
                    spellViewModel.inventoryVm = inventoryViewModel
                    characterViewModel.connectSiblings(combatViewModel, spellViewModel, inventoryViewModel)

                    val uid = currentUser!!.uid
                    when (characterViewModel.setupComplete) {
                        null -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = WaldgruenDunkel)
                            }
                        }
                        false -> {
                            CharacterSetupScreen(
                                uid = uid,
                                viewModel = characterViewModel,
                                onSetupComplete = {}
                            )
                        }
                        else -> {
                            DnDApp(
                                characterViewModel, groupViewModel, combatViewModel, spellViewModel, inventoryViewModel,
                                onLogout = { authViewModel.logout() }
                            )
                        }
                    }
                }
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
    inventoryViewModel: InventoryViewModel,
    onLogout: () -> Unit = {}
) {
    // 0 = Athania, 1 = Chat, 2 = Bibliothek
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

    // Intro Animation Logik fÃ¼r sanftes Ausfaden
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
                        selectedIconColor = PergamentHell,
                        selectedTextColor = PergamentHell,
                        unselectedIconColor = PergamentDunkel,
                        unselectedTextColor = PergamentDunkel,
                        indicatorColor = Waldgruen
                    )
                )
                NavigationBarItem(
                    selected = currentScreen == 1, // Chat
                    onClick = { currentScreen = 1 },
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.hilfe),
                            contentDescription = "Chat",
                            modifier = Modifier.size(38.dp)
                        )
                    },
                    label = { Text("Chat", fontFamily = com.example.dndcompanion.ui.theme.Almendra) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PergamentHell,
                        selectedTextColor = PergamentHell,
                        unselectedIconColor = PergamentDunkel,
                        unselectedTextColor = PergamentDunkel,
                        indicatorColor = Waldgruen
                    )
                )
                NavigationBarItem(
                    selected = currentScreen == 2, // Bibliothek
                    onClick = { currentScreen = 2 },
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.bucher),
                            contentDescription = "Bibliothek",
                            modifier = Modifier.size(38.dp)
                        )
                    },
                    label = { Text("Bibliothek", fontFamily = com.example.dndcompanion.ui.theme.Almendra) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PergamentHell,
                        selectedTextColor = PergamentHell,
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
                0 -> AthaniaScreen(
                    viewModel, groupViewModel, combatViewModel, spellViewModel, inventoryViewModel,
                    onNavigateToRulebook = { chapter, search ->
                        viewModel.targetRulebookChapter = chapter
                        viewModel.targetRulebookSearch = search
                        currentScreen = 2
                    },
                    onLogout = onLogout
                )
                1 -> HelpScreen(viewModel, groupViewModel, onNavigateToRulebook = { chapter, search ->
                    viewModel.targetRulebookChapter = chapter
                    viewModel.targetRulebookSearch = search
                    currentScreen = 2
                })
                2 -> BucherScreen(viewModel, spellViewModel, groupViewModel)
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
                    contentScale = ContentScale.Fit, // Fit statt Crop, damit keine RÃ¤nder abgeschnitten werden
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
    inventoryViewModel: InventoryViewModel,
    onNavigateToRulebook: ((String, String) -> Unit)? = null,
    onLogout: () -> Unit = {}
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
                    selectedContentColor = MaterialTheme.colorScheme.tertiary,
                    unselectedContentColor = TintenBraun
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (tabs[page]) {
                AthaniaTab.Profil -> ProfilScreen(viewModel, combatViewModel, onNavigateToRulebook, onLogout = onLogout)
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
                else -> ProfilScreen(viewModel, combatViewModel, onNavigateToRulebook)
            }
        }
    }
}
