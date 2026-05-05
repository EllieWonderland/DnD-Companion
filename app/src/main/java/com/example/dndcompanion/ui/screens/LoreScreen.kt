package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.theme.Almendra
import com.example.dndcompanion.ui.theme.PergamentBackground
import com.example.dndcompanion.ui.theme.PergamentHell
import com.example.dndcompanion.ui.theme.TintenSchwarz
import com.example.dndcompanion.ui.theme.Waldgruen
import com.example.dndcompanion.ui.theme.WaldgruenDunkel
import kotlinx.coroutines.launch

private val loreTabs = listOf("Quests", "Karten", "Hausregeln", "Geschichten")

@Composable
fun LoreScreen() {
    val pagerState = rememberPagerState { loreTabs.size }
    val scope = rememberCoroutineScope()

    PergamentBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = WaldgruenDunkel,
                contentColor = PergamentHell,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = PergamentHell
                    )
                }
            ) {
                loreTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = {
                            Text(
                                text = title,
                                fontFamily = Almendra,
                                fontSize = 12.sp,
                                fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selectedContentColor = PergamentHell,
                        unselectedContentColor = PergamentHell.copy(alpha = 0.6f)
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> LoreQuestsTab()
                    1 -> LoreMapsTab()
                    2 -> LoreHouserulesTab()
                    3 -> LoreStoriesTab()
                }
            }
        }
    }
}

@Composable
fun LoreQuestsTab() {
    LorePlaceholder("Quests", "Gruppenquests – kommt in Task 4.2")
}

@Composable
fun LoreMapsTab() {
    LorePlaceholder("Karten", "Gruppen-Karten – kommt in Task 4.3")
}

@Composable
fun LoreHouserulesTab() {
    LorePlaceholder("Hausregeln", "Hausregeln – kommt in Task 4.4")
}

@Composable
fun LoreStoriesTab() {
    LorePlaceholder("Geschichten", "Gruppen-Geschichten – kommt in Task 4.5")
}

@Composable
private fun LorePlaceholder(title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontFamily = Almendra,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TintenSchwarz
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontFamily = Almendra,
                fontSize = 14.sp,
                color = TintenSchwarz.copy(alpha = 0.6f)
            )
        }
    }
}
