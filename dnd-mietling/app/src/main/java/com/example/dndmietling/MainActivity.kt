package com.example.dndmietling

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dndmietling.data.MietlingCharacter
import com.example.dndmietling.ui.screens.*
import com.example.dndmietling.ui.theme.DnDMietlingTheme
import com.example.dndmietling.ui.theme.PergamentDunkel
import com.example.dndmietling.ui.theme.WaldGold
import com.example.dndmietling.ui.theme.WaldgruenDunkel
import com.example.dndmietling.ui.theme.Waldgruen
import com.example.dndmietling.ui.viewmodel.MietlingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DnDMietlingTheme {
                val viewModel: MietlingViewModel = viewModel()
                MietlingApp(viewModel)
            }
        }
    }
}

private enum class MietlingTab(val label: String, val icon: ImageVector) {
    INITIATIVE("Initiative", Icons.Default.List),
    QUESTLOG("Questlog", Icons.Default.Assignment),
    BUECHER("Bücher", Icons.Default.MenuBook),
    HILFE("Hilfe", Icons.Default.Help)
}

@Composable
fun MietlingApp(viewModel: MietlingViewModel) {
    var loggedInCharacter by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    if (loggedInCharacter == null) {
        CharacterSelectionScreen(
            onCharacterSelected = { character ->
                loggedInCharacter = character.name
                viewModel.currentCharacter = character
            }
        )
        return
    }

    // Restore character in viewmodel if needed (e.g., after process death)
    LaunchedEffect(loggedInCharacter) {
        if (viewModel.currentCharacter == null && loggedInCharacter != null) {
            viewModel.currentCharacter = try {
                MietlingCharacter.valueOf(loggedInCharacter!!)
            } catch (e: Exception) { null }
        }
    }

    val tabs = MietlingTab.entries.toList()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = WaldgruenDunkel,
                contentColor = PergamentDunkel
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
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
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (tabs[selectedTab]) {
                MietlingTab.INITIATIVE -> InitiativeTrackerScreen(viewModel)
                MietlingTab.QUESTLOG -> QuestlogScreen(viewModel)
                MietlingTab.BUECHER -> BuecherScreen(viewModel)
                MietlingTab.HILFE -> HilfeScreen()
            }
        }
    }
}
