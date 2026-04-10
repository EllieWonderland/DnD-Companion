package com.example.dndcompanion.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.Material3RichText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.example.dndcompanion.ui.viewmodel.GroupViewModel
import com.example.dndcompanion.ui.viewmodel.SpellViewModel
import com.example.dndcompanion.ui.viewmodel.BookEntry
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.draw.scale

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.aspectRatio
import com.example.dndcompanion.ui.theme.PergamentBackground
import com.example.dndcompanion.ui.theme.PergamentCard
import com.example.dndcompanion.ui.theme.PergamentHell
import com.example.dndcompanion.ui.theme.SteinCard
import com.example.dndcompanion.ui.theme.TintenSchwarz
import com.example.dndcompanion.ui.theme.Almendra
import com.example.dndcompanion.ui.theme.GrenzeGotischSmall
import com.example.dndcompanion.ui.theme.WaldgruenDunkel
import com.example.dndcompanion.ui.theme.PergamentDunkel
import com.example.dndcompanion.ui.theme.Waldgruen
import com.example.dndcompanion.R
import com.example.dndcompanion.data.database.RuleEntity
import com.example.dndcompanion.data.database.WeaponEntity
import com.example.dndcompanion.data.database.ArmorEntity
import com.example.dndcompanion.data.database.ToolEntity
import com.example.dndcompanion.data.database.SpeciesEntity
import com.example.dndcompanion.data.database.ClassEntity
import com.example.dndcompanion.data.database.FeatureEntity

enum class BookType {
    GENERAL, SPELLBOOK, RULEBOOK
}

@Composable
fun BucherScreen(viewModel: CharacterViewModel, spellVm: SpellViewModel, groupVm: GroupViewModel) {
    var activeBook by remember { mutableStateOf<BookType?>(null) }

    BackHandler(enabled = activeBook != null) {
        activeBook = null
    }

    LaunchedEffect(viewModel.targetRulebookChapter) {
        if (viewModel.targetRulebookChapter != null) {
            activeBook = BookType.RULEBOOK
        }
    }

    if (activeBook == null) {
        LibraryView(viewModel = viewModel, onBookSelected = { activeBook = it })
    } else {
        if (activeBook == BookType.SPELLBOOK) {
            SpellbookDetailView(
                spellVm = spellVm,
                charVm = viewModel,
                onBack = { activeBook = null }
            )
        } else if (activeBook == BookType.RULEBOOK) {
            RulebookDetailView(
                viewModel = viewModel,
                targetChapter = viewModel.targetRulebookChapter,
                targetSearch = viewModel.targetRulebookSearch,
                onTargetConsumed = { 
                    viewModel.targetRulebookChapter = null 
                    viewModel.targetRulebookSearch = null
                },
                onBack = { 
                    activeBook = null 
                    viewModel.targetRulebookChapter = null
                    viewModel.targetRulebookSearch = null
                }
            )
        } else {
            BookDetailView(
                bookType = activeBook!!,
                viewModel = viewModel,
                groupVm = groupVm,
                onBack = { activeBook = null }
            )
        }
    }
}
