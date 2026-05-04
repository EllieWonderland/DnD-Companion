package com.example.dndcompanion.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.R
import com.example.dndcompanion.data.CharacterClass
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.viewmodel.CombatViewModel
import com.example.dndcompanion.ui.viewmodel.GroupViewModel
import com.example.dndcompanion.ui.viewmodel.InventoryViewModel
import com.example.dndcompanion.ui.viewmodel.SpellViewModel
import kotlinx.coroutines.launch

enum class CharacterTab(val title: String) {
    Profil("Profil"),
    Kampf("Kampf"),
    Zauber("Zauber"),
    Rucksack("Rucksack"),
    Hilfe("Chat")
}

@Composable
fun CharacterScreen(
    viewModel: CharacterViewModel,
    groupViewModel: GroupViewModel,
    combatViewModel: CombatViewModel,
    spellViewModel: SpellViewModel,
    inventoryViewModel: InventoryViewModel,
    onNavigateToRulebook: ((String, String) -> Unit)? = null,
    onLogout: () -> Unit = {}
) {
    val tabs = CharacterTab.entries.filter { it != CharacterTab.Hilfe }
    val pagerState = rememberPagerState(initialPage = tabs.indexOf(CharacterTab.Kampf), pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    var previousPage by remember { mutableIntStateOf(tabs.indexOf(CharacterTab.Kampf)) }
    LaunchedEffect(Unit) {
        var lastSettledPage = pagerState.settledPage
        snapshotFlow { pagerState.settledPage }.collect { settledPage ->
            if (settledPage != lastSettledPage) {
                previousPage = lastSettledPage
                lastSettledPage = settledPage
            }
        }
    }

    BackHandler(enabled = pagerState.currentPage != tabs.indexOf(CharacterTab.Kampf)) {
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
                        val iconRes = when (tab) {
                            CharacterTab.Profil -> if (viewModel.characterData.charClass == CharacterClass.RANGER) R.drawable.athania else R.drawable.delat
                            CharacterTab.Kampf -> R.drawable.kampf
                            CharacterTab.Zauber -> R.drawable.zauberbuch
                            CharacterTab.Rucksack -> R.drawable.rucksack
                            else -> R.drawable.kampf
                        }
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).clip(
                                if (tab == CharacterTab.Profil) CircleShape else RoundedCornerShape(0.dp)
                            ),
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
                CharacterTab.Profil -> ProfilScreen(viewModel, combatViewModel, onNavigateToRulebook, onLogout = onLogout)
                CharacterTab.Kampf -> CombatScreen(
                    viewModel = viewModel,
                    combatVm = combatViewModel,
                    inventoryVm = inventoryViewModel,
                    onNavigateToRucksack = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(tabs.indexOf(CharacterTab.Rucksack))
                        }
                    },
                    onNavigateToProfile = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(tabs.indexOf(CharacterTab.Profil))
                        }
                    }
                )
                CharacterTab.Zauber -> ZauberScreen(viewModel, spellViewModel, combatViewModel)
                CharacterTab.Rucksack -> RucksackScreen(viewModel, inventoryViewModel, groupViewModel)
                else -> ProfilScreen(viewModel, combatViewModel, onNavigateToRulebook)
            }
        }
    }
}
