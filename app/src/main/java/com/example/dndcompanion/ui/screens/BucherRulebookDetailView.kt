package com.example.dndcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.data.database.ArmorEntity
import com.example.dndcompanion.data.database.ClassEntity
import com.example.dndcompanion.data.database.FeatureEntity
import com.example.dndcompanion.data.database.RuleEntity
import com.example.dndcompanion.data.database.SpeciesEntity
import com.example.dndcompanion.data.database.ToolEntity
import com.example.dndcompanion.data.database.WeaponEntity
import com.example.dndcompanion.ui.theme.*
import com.example.dndcompanion.ui.viewmodel.CharacterViewModel
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.Material3RichText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulebookDetailView(targetChapter: String?, targetSearch: String? = null, viewModel: CharacterViewModel, onTargetConsumed: () -> Unit, onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }

    val tabs = listOf(
        "Global", "Gameplay", "Klassen & Völker", "Ausrüstung", "Kampf & Zustände", "Zauber-Regeln", "Dienstleistungen"
    )

    val pagerState = rememberPagerState(pageCount = { tabs.size })

    val rules by viewModel.searchedRules.collectAsState()
    val weapons by viewModel.searchedWeapons.collectAsState()
    val armor by viewModel.searchedArmor.collectAsState()
    val tools by viewModel.searchedTools.collectAsState()
    val species by viewModel.searchedSpecies.collectAsState()
    val classes by viewModel.searchedClasses.collectAsState()
    val features by viewModel.searchedFeatures.collectAsState()
    val spells by viewModel.searchedSpells.collectAsState()

    val gameplayRules = rules.filter { it.category == "Gameplay" }
    val combatRules = rules.filter { it.category == "Kampf & Zustände" }
    val spellRules = rules.filter { it.category == "Zauber" }
    val serviceRules = rules.filter { it.category == "Ausrüstung & Dienstleistungen" }

    LaunchedEffect(searchQuery) {
        viewModel.searchRulebook(searchQuery)
    }

    LaunchedEffect(targetChapter) {
        if (targetChapter != null) {
            if (targetSearch != null) {
                searchQuery = targetSearch
            }
            onTargetConsumed()
        }
    }

    PergamentBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WaldgruenDunkel)
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Zurück", tint = WaldGold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Regelwerk", fontSize = 24.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = WaldGold)
            }

            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = WaldgruenDunkel,
                contentColor = WaldGold,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = WaldGold
                    )
                }
            ) {
                tabs.forEachIndexed { index, tabTitle ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = { Text(tabTitle, fontSize = 16.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold) },
                        selectedContentColor = WaldGold,
                        unselectedContentColor = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.height(48.dp)
                    )
                }
            }

            androidx.compose.foundation.layout.Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    placeholder = { Text("Im Regelwerk suchen...", color = TintenSchwarz.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(24.dp), tint = WaldgruenDunkel) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                viewModel.searchRulebook("")
                            }, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Suchen löschen", tint = WaldgruenDunkel)
                            }
                        }
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = TintenSchwarz),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WaldgruenDunkel,
                        unfocusedBorderColor = WaldgruenDunkel.copy(alpha = 0.5f)
                    )
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) { page ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (page) {
                        0 -> {
                            if (searchQuery.isBlank()) {
                                item {
                                    Text(
                                        text = "Nutze das Suchfeld, um im gesamten Regelwerk, in Klassen, Völkern und der Ausrüstung gleichzeitig zu suchen.",
                                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                                        textAlign = TextAlign.Center,
                                        color = TintenSchwarz.copy(alpha = 0.6f),
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            } else {
                                var foundAnything = false
                                if (rules.isNotEmpty()) {
                                    item { SectionHeader("Regeln") }
                                    items(rules) { rule -> RuleCard(rule) }
                                    foundAnything = true
                                }
                                if (species.isNotEmpty()) {
                                    item { SectionHeader("Völker") }
                                    items(species) { spec -> SpeciesCard(spec) }
                                    foundAnything = true
                                }
                                if (classes.isNotEmpty()) {
                                    item { SectionHeader("Klassen") }
                                    items(classes) { cls -> ClassCard(cls) }
                                    foundAnything = true
                                }
                                if (features.isNotEmpty()) {
                                    item { SectionHeader("Merkmale & Talente") }
                                    items(features) { feature -> FeatureCard(feature) }
                                    foundAnything = true
                                }
                                if (spells.isNotEmpty()) {
                                    item { SectionHeader("Zaubersprüche") }
                                    items(spells) { spellEntity ->
                                        SpellCard(spell = spellEntity.toSpell(), isEditMode = false, isEquipped = false, customColor = HexenLila)
                                    }
                                    foundAnything = true
                                }
                                if (weapons.isNotEmpty() || armor.isNotEmpty() || tools.isNotEmpty()) {
                                    item { SectionHeader("Ausrüstung") }
                                    items(weapons) { weapon -> WeaponCard(weapon) }
                                    items(armor) { arm -> ArmorCard(arm) }
                                    items(tools) { tool -> ToolCard(tool) }
                                    foundAnything = true
                                }
                                if (!foundAnything) item { EmptySearchResult() }
                            }
                        }
                        1 -> {
                            items(gameplayRules) { rule -> RuleCard(rule) }
                            if (gameplayRules.isEmpty()) item { EmptySearchResult() }
                        }
                        2 -> {
                            if (species.isNotEmpty()) {
                                item { SectionHeader("Völker (Species)") }
                                items(species) { spec -> SpeciesCard(spec) }
                            }
                            if (classes.isNotEmpty()) {
                                item { SectionHeader("Klassen (Classes)") }
                                items(classes) { cls -> ClassCard(cls) }
                            }
                            if (species.isEmpty() && classes.isEmpty()) item { EmptySearchResult() }
                        }
                        3 -> {
                            if (weapons.isNotEmpty()) {
                                item { SectionHeader("Waffen") }
                                items(weapons) { weapon -> WeaponCard(weapon) }
                            }
                            if (armor.isNotEmpty()) {
                                item { SectionHeader("Rüstungen & Schilde") }
                                items(armor) { arm -> ArmorCard(arm) }
                            }
                            if (tools.isNotEmpty()) {
                                item { SectionHeader("Werkzeuge") }
                                items(tools) { tool -> ToolCard(tool) }
                            }
                            if (weapons.isEmpty() && armor.isEmpty() && tools.isEmpty()) item { EmptySearchResult() }
                        }
                        4 -> {
                            items(combatRules) { rule -> RuleCard(rule) }
                            if (combatRules.isEmpty()) item { EmptySearchResult() }
                        }
                        5 -> {
                            if (spells.isNotEmpty()) {
                                item { SectionHeader("Zaubersprüche") }
                                items(spells) { spellEntity ->
                                    SpellCard(spell = spellEntity.toSpell(), isEditMode = false, isEquipped = false, customColor = HexenLila)
                                }
                            }
                            items(spellRules) { rule -> RuleCard(rule) }
                            if (spellRules.isEmpty() && spells.isEmpty()) item { EmptySearchResult() }
                        }
                        6 -> {
                            items(serviceRules) { rule -> RuleCard(rule) }
                            if (serviceRules.isEmpty()) item { EmptySearchResult() }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptySearchResult() {
    Text(
        text = "Keine passenden Einträge gefunden.",
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        textAlign = TextAlign.Center,
        color = TintenSchwarz.copy(alpha = 0.6f),
        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
    )
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = Almendra,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = WaldgruenDunkel,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun RuleCard(rule: RuleEntity) {
    SteinCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(rule.title, fontSize = 20.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = OchsenblutRot)
            Spacer(modifier = Modifier.height(8.dp))
            Material3RichText(modifier = Modifier.fillMaxWidth()) {
                Markdown(rule.content)
            }
            if (rule.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    rule.tags.take(3).forEach { tag ->
                        Text("#$tag", fontSize = 13.sp, style = GrenzeGotischSmall, color = WaldgruenDunkel)
                    }
                }
            }
        }
    }
}

@Composable
fun WeaponCard(weapon: WeaponEntity) {
    PergamentCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(weapon.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TintenSchwarz)
                Text(weapon.price, fontWeight = FontWeight.Bold, color = WaldGold, modifier = Modifier.background(WaldgruenDunkel, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
            }
            Text(weapon.category, fontSize = 13.sp, color = TintenSchwarz.copy(alpha = 0.7f), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Schaden: ${weapon.damage}", color = OchsenblutRot, fontWeight = FontWeight.Medium)
                Text("Gewicht: ${weapon.weightLb} lb", color = TintenSchwarz.copy(alpha = 0.8f))
            }
            Text("Meisterschaft: ${weapon.mastery}", color = TintenSchwarz.copy(alpha = 0.9f))
            if (weapon.properties.isNotEmpty()) {
                Text("Eigenschaften: ${weapon.properties.joinToString(", ")}", fontSize = 13.sp, color = TintenSchwarz.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun ArmorCard(armor: ArmorEntity) {
    PergamentCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(armor.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TintenSchwarz)
                Text(armor.price, fontWeight = FontWeight.Bold, color = WaldGold, modifier = Modifier.background(WaldgruenDunkel, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
            }
            Text(armor.category, fontSize = 13.sp, color = TintenSchwarz.copy(alpha = 0.7f), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            Spacer(modifier = Modifier.height(8.dp))

            val dexModText = if (armor.addDexModifier) {
                if (armor.maxDexModifier != null) " + GES (max ${armor.maxDexModifier})" else " + GES"
            } else ""
            Text("RK: ${armor.baseAC}$dexModText", color = OchsenblutRot, fontWeight = FontWeight.Medium)

            val stealthText = if (armor.stealthDisadvantage) "Nachteil auf Heimlichkeit" else "Normale Heimlichkeit"
            val strengthText = if (armor.strengthRequirement > 0) "STR min. ${armor.strengthRequirement}" else "Keine STR-Anforderung"

            Text("$stealthText | $strengthText | ${armor.weightLb} lb", fontSize = 13.sp, color = TintenSchwarz.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun ToolCard(tool: ToolEntity) {
    PergamentCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tool.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TintenSchwarz)
                Text(tool.category, fontSize = 13.sp, color = TintenSchwarz.copy(alpha = 0.7f), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                if (tool.weightLb != null) {
                    Text("Gewicht: ${tool.weightLb} lb", fontSize = 13.sp, color = TintenSchwarz.copy(alpha = 0.8f))
                }
            }
            Text(tool.price, fontWeight = FontWeight.Bold, color = WaldGold, modifier = Modifier.background(WaldgruenDunkel, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
        }
    }
}

@Composable
fun SpeciesCard(species: SpeciesEntity) {
    SteinCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(species.name, fontSize = 20.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = OchsenblutRot)
            Text("Größe: ${species.size} | Tempo: ${species.speed}m", fontSize = 14.sp, color = TintenSchwarz.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(8.dp))
            species.traits.forEach { trait ->
                Text(trait.name, fontWeight = FontWeight.Bold, color = TintenSchwarz, modifier = Modifier.padding(top = 4.dp))
                Text(trait.description, fontSize = 14.sp, color = TintenSchwarz.copy(alpha = 0.9f))
            }
        }
    }
}

@Composable
fun ClassCard(cls: ClassEntity) {
    SteinCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(cls.name, fontSize = 22.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = HexenLila)
            Text("Primär: ${cls.primaryAbility} | Trefferwürfel: ${cls.hitDie}", fontSize = 14.sp, color = TintenSchwarz.copy(alpha = 0.8f))
            Text("Rettungswürfe: ${cls.savingThrows.joinToString(", ")}", fontSize = 14.sp, color = TintenSchwarz.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(12.dp))

            Text("Klassenmerkmale", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TintenSchwarz)
            cls.classFeatures.forEach { feature ->
                Text("Lv ${feature.level}: ${feature.name}", fontWeight = FontWeight.Bold, color = TintenSchwarz, modifier = Modifier.padding(top = 8.dp))
                Text(feature.description, fontSize = 14.sp, color = TintenSchwarz.copy(alpha = 0.9f))
            }

            if (cls.subclasses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Unterklassen", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TintenSchwarz)
                cls.subclasses.forEach { sub ->
                    Text(sub.name, fontWeight = FontWeight.Bold, color = OchsenblutRot, modifier = Modifier.padding(top = 8.dp))
                    sub.features.forEach { sf ->
                        Text("Lv ${sf.level}: ${sf.name}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TintenSchwarz)
                        Text(sf.description, fontSize = 13.sp, color = TintenSchwarz.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureCard(feature: FeatureEntity) {
    SteinCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            val titleColor = when (feature.type) {
                "FEAT" -> WaldgruenDunkel
                "RACIAL_TRAIT" -> OchsenblutRot
                "CLASS_FEATURE" -> HexenLila
                "SUBCLASS_FEATURE" -> TintenSchwarz
                else -> WaldgruenDunkel
            }
            Text(feature.name, fontSize = 20.sp, fontFamily = Almendra, fontWeight = FontWeight.Bold, color = titleColor)
            val subText = buildString {
                append(feature.type)
                if (!feature.category.isNullOrBlank()) append(" - ${feature.category}")
                if (feature.levelReq > 1) append(" (Ab Stufe ${feature.levelReq})")
            }.toString()
            Text(subText, fontSize = 14.sp, fontFamily = Almendra, color = TintenSchwarz.copy(alpha = 0.8f))

            if (!feature.raceReq.isNullOrEmpty() || !feature.classReq.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                val reqText = buildString {
                    if (!feature.raceReq.isNullOrEmpty()) append("Volk: ${feature.raceReq.joinToString()} ")
                    if (!feature.classReq.isNullOrEmpty()) append("Klasse: ${feature.classReq.joinToString()}")
                }.toString()
                Text("Voraussetzung: $reqText", fontSize = 14.sp, style = GrenzeGotischSmall, color = OchsenblutRot)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Material3RichText(modifier = Modifier.fillMaxWidth()) {
                Markdown(feature.description)
            }
        }
    }
}
