package com.example.dndcompanion.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dndcompanion.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.ai.client.generativeai.type.generationConfig
import org.json.JSONObject
import com.example.dndcompanion.data.CharacterData
import com.example.dndcompanion.data.CharacterRepository
import com.example.dndcompanion.data.CharacterClass
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.filterNotNull
import com.example.dndcompanion.data.DndCalculations
import com.example.dndcompanion.data.PrefsManager
import com.example.dndcompanion.data.database.AppDatabase
import com.example.dndcompanion.data.database.RuleEntity
import com.example.dndcompanion.data.database.WeaponEntity
import com.example.dndcompanion.data.database.ArmorEntity
import com.example.dndcompanion.data.database.ToolEntity
import com.example.dndcompanion.data.database.SpeciesEntity
import com.example.dndcompanion.data.database.ClassEntity
import com.example.dndcompanion.data.database.FeatureEntity
import com.example.dndcompanion.data.database.SpellEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import com.google.firebase.firestore.FirebaseFirestore

data class UrtierFileDto(
    val urtiere: List<CompanionDto>
)

class CharacterViewModel(application: Application) : AndroidViewModel(application) {

    var activeCharacterId by mutableStateOf("Athania")
        private set

    private val _activeCharacterIdFlow = MutableStateFlow("Athania")
    val activeCharacterIdFlow: StateFlow<String> = _activeCharacterIdFlow.asStateFlow()

    /**
     * Called once after login with the Firebase UID.
     * Loads the user's character from Firestore (with Room cache and JSON fallback),
     * and starts a live listener that keeps characterData in sync with Firestore.
     */
    fun loadUserCharacter(uid: String) {
        loadProfile(uid)
        loadInventoryFromFirestore(uid)
        viewModelScope.launch {
            characterRepository.getCharacterFlowFromFirestore(uid)
                .catch { e -> android.util.Log.e("CharacterVM", "Firestore stream error (check security rules)", e) }
                .collect { firestoreData ->
                    if (firestoreData != null) {
                        val data = firestoreData.copy(id = uid)
                        characterRepository.saveCharacter(data)
                        // Room flow in init{} will pick up the update → characterData refreshed
                    } else {
                        // No Firestore document yet → seed from current characterData
                        try {
                            characterRepository.saveCharacterToFirestore(uid, characterData.copy(id = uid))
                        } catch (e: Exception) {
                            android.util.Log.e("CharacterVM", "Failed to seed Firestore", e)
                        }
                    }
                }
        }
    }

    val prefsManager = PrefsManager(application)
    private val prefs get() = prefsManager.prefs
    private val gson = Gson()
    private val firestore = FirebaseFirestore.getInstance()
    private var suppressInventoryFirestoreSync = false

    private val database = AppDatabase.getDatabase(application)
    private val rulebookDao = database.rulebookDao()
    internal val characterRepository = CharacterRepository(application, database)

    var characterData by mutableStateOf(characterRepository.getCharacter("Athania"))
        private set

    private val _searchedRules = MutableStateFlow<List<RuleEntity>>(emptyList())
    val searchedRules: StateFlow<List<RuleEntity>> = _searchedRules.asStateFlow()

    private val _searchedWeapons = MutableStateFlow<List<WeaponEntity>>(emptyList())
    val searchedWeapons: StateFlow<List<WeaponEntity>> = _searchedWeapons.asStateFlow()

    private val _searchedArmor = MutableStateFlow<List<ArmorEntity>>(emptyList())
    val searchedArmor: StateFlow<List<ArmorEntity>> = _searchedArmor.asStateFlow()

    private val _searchedTools = MutableStateFlow<List<ToolEntity>>(emptyList())
    val searchedTools: StateFlow<List<ToolEntity>> = _searchedTools.asStateFlow()

    private val _searchedSpecies = MutableStateFlow<List<SpeciesEntity>>(emptyList())
    val searchedSpecies: StateFlow<List<SpeciesEntity>> = _searchedSpecies.asStateFlow()

    private val _searchedClasses = MutableStateFlow<List<ClassEntity>>(emptyList())
    val searchedClasses: StateFlow<List<ClassEntity>> = _searchedClasses.asStateFlow()

    private val _searchedFeatures = MutableStateFlow<List<FeatureEntity>>(emptyList())
    val searchedFeatures: StateFlow<List<FeatureEntity>> = _searchedFeatures.asStateFlow()

    private val _searchedSpells = MutableStateFlow<List<SpellEntity>>(emptyList())
    val searchedSpells: StateFlow<List<SpellEntity>> = _searchedSpells.asStateFlow()

    // These must be declared before init{} to avoid NullPointerException during initialization
    val customLoot = mutableStateListOf<InventoryItem>()
    val customTraits = mutableStateListOf<TraitItem>()
    val generalBookEntries = mutableStateListOf<BookEntry>()
    val grudgeBookEntries = mutableStateListOf<BookEntry>()
    val chatHistory = mutableStateListOf<ChatMessage>()
    val faqList = mutableStateListOf<FaqItem>()
    val allSpells = mutableStateListOf<Spell>()
    val globalFeatures = mutableStateListOf<Feature>()
    val globalSpellbook = MutableStateFlow<List<SpellEntity>>(emptyList())
    // companionData and activeBeastType also accessed/set by loadCompanion() via Main.immediate coroutine
    var companionData by mutableStateOf<CompanionDto?>(null)
        private set
    var activeBeastType by mutableStateOf(BeastType.valueOf(prefs.getString("activeBeastType", BeastType.SKY.name) ?: BeastType.SKY.name))
        internal set

    init {
        // Keep characterData in sync with DB — picks up in-app edits (level-ups, etc.)
        viewModelScope.launch {
            _activeCharacterIdFlow.flatMapLatest { id ->
                characterRepository.getCharacterFlow(id)
            }.filterNotNull().collect { dbChar ->
                characterData = dbChar
            }
        }

        // Load initial data (all content)
        searchRulebook("")
        suppressInventoryFirestoreSync = true
        loadLoot()
        suppressInventoryFirestoreSync = false
        loadFaqs()
        loadSpells()
        loadGlobalFeatures()
        loadTraits()
        loadBooks()
        loadCompanion()

        viewModelScope.launch {
            val db = com.example.dndcompanion.data.database.AppDatabase.getDatabase(getApplication()).rulebookDao()
            db.getAllSpells().collectLatest { spells ->
                globalSpellbook.value = spells
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val items = EquipmentCatalogParser.loadFromDb(database.rulebookDao())
            withContext(Dispatchers.Main) {
                if (equipmentCatalog.isEmpty()) equipmentCatalog.addAll(items)
            }
        }

    }

    fun searchRulebook(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (query.isBlank()) {
                rulebookDao.getAllRules().collectLatest { _searchedRules.value = it }
            } else {
                rulebookDao.searchRules(query).collectLatest { _searchedRules.value = it }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (query.isBlank()) {
                rulebookDao.getAllWeapons().collectLatest { _searchedWeapons.value = it }
            } else {
                rulebookDao.searchWeapons(query).collectLatest { _searchedWeapons.value = it }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (query.isBlank()) {
                rulebookDao.getAllArmor().collectLatest { _searchedArmor.value = it }
            } else {
                rulebookDao.searchArmor(query).collectLatest { _searchedArmor.value = it }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (query.isBlank()) {
                rulebookDao.getAllTools().collectLatest { _searchedTools.value = it }
            } else {
                rulebookDao.searchTools(query).collectLatest { _searchedTools.value = it }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (query.isBlank()) {
                rulebookDao.getAllSpecies().collectLatest { _searchedSpecies.value = it }
            } else {
                rulebookDao.searchSpecies(query).collectLatest { _searchedSpecies.value = it }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (query.isBlank()) {
                rulebookDao.getAllClasses().collectLatest { _searchedClasses.value = it }
            } else {
                rulebookDao.searchClasses(query).collectLatest { _searchedClasses.value = it }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (query.isBlank()) {
                rulebookDao.getAllFeatures().collectLatest { _searchedFeatures.value = it }
            } else {
                rulebookDao.searchFeatures(query).collectLatest { _searchedFeatures.value = it }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (query.isBlank()) {
                rulebookDao.getAllSpells().collectLatest { _searchedSpells.value = it }
            } else {
                rulebookDao.searchSpells(query).collectLatest { _searchedSpells.value = it }
            }
        }
    }

    // --- BASISWERTE ---
    // EP Table D&D 5e:
    val epThresholds = listOf(
        0, 300, 900, 2700, 6500, 14000, 23000, 34000, 48000, 64000,
        85000, 100000, 120000, 140000, 165000, 195000, 225000, 265000, 305000, 355000
    )

    var currentEP by mutableIntStateOf(prefs.getInt("currentEP_${characterData.id}", characterData.baseEP))
    var heroicInspiration by mutableStateOf(prefs.getBoolean("heroicInspiration_${activeCharacterId}", false))
        private set

    var level by mutableIntStateOf(prefs.getInt("level", characterData.baseLevel))
        private set

    var strength by mutableIntStateOf(prefs.getInt("strength", characterData.baseStrength))
        private set
    var dexterity by mutableIntStateOf(prefs.getInt("dexterity", characterData.baseDexterity))
        private set
    var constitution by mutableIntStateOf(prefs.getInt("constitution", characterData.baseConstitution))
        private set
    var intelligence by mutableIntStateOf(prefs.getInt("intelligence", characterData.baseIntelligence))
        private set
    var wisdom by mutableIntStateOf(prefs.getInt("wisdom", characterData.baseWisdom))
        private set
    var charisma by mutableIntStateOf(prefs.getInt("charisma", characterData.baseCharisma))
        private set

    var showLevelUpDialog by mutableStateOf(false)
        private set

    var showLevelUpNotification by mutableStateOf(false)
        private set

    var showCharacterEditDialog by mutableStateOf(false)
        private set

    var snackbarMessage = mutableStateOf<String?>(null)
    
    var isUsingTwoHanded by mutableStateOf(prefs.getBoolean("isUsingTwoHanded", false))
        private set

    fun toggleTwoHanded(active: Boolean) {
        isUsingTwoHanded = active
        prefs.edit { putBoolean("isUsingTwoHanded", isUsingTwoHanded) }
    }

    // Mage Armor active effect is separate from "daily use" to allow toggling
    var isMageArmorActive by mutableStateOf(prefs.getBoolean("isMageArmorActive", false))
        private set

    fun toggleMageArmor(active: Boolean) {
        isMageArmorActive = active
        prefs.edit { putBoolean("isMageArmorActive", isMageArmorActive) }
    }

    var isShieldEquipped by mutableStateOf(prefs.getBoolean("isShieldEquipped", false))
        private set

    fun toggleShield(active: Boolean) {
        isShieldEquipped = active
        prefs.edit { putBoolean("isShieldEquipped", isShieldEquipped) }
    }

    val hasShieldInInventory: Boolean
        get() = customLoot.any { it.name.contains("Schild", ignoreCase = true) }

    val availableWeapons: List<String>
        get() {
            val list = mutableListOf<String>()
            customLoot.forEach { item ->
                if (item.category == "Rüstung & Waffen" && !item.name.contains("Rüstung", ignoreCase = true) && !item.name.contains("Schild", ignoreCase = true)) {
                    list.add(item.name)
                }
            }
            // Standardwaffen hinzufügen, falls sie im Inventar sind (Migration/Fallback)
            return list.distinct()
        }

    fun addExperience(amount: Int) {
        currentEP += amount
        prefs.edit { putInt("currentEP", currentEP) }
        checkLevelUp()
    }
        
    private fun checkLevelUp() {
        val newLevel = epThresholds.indexOfLast { currentEP >= it } + 1
        if (newLevel > level) {
            val oldLevel = level
            level = newLevel
            prefs.edit { putInt("level", level) }
            showLevelUpDialog = true
            showLevelUpNotification = true
            
            // --- AUTOMATISIERUNGEN FÜR KLASSEN ---
            for (lvl in (oldLevel + 1)..newLevel) {
                // Zauberplätze automatisch aktualisieren
                spellSlotsLevel1 = getMaxSpellSlots(lvl, 1)
                spellSlotsLevel2 = getMaxSpellSlots(lvl, 2)
                spellSlotsLevel3 = getMaxSpellSlots(lvl, 3)
                spellSlotsLevel4 = getMaxSpellSlots(lvl, 4)
                spellSlotsLevel5 = getMaxSpellSlots(lvl, 5)
                
                prefs.edit { 
                    putInt("spellSlotsLevel1", spellSlotsLevel1)
                    putInt("spellSlotsLevel2", spellSlotsLevel2)
                    putInt("spellSlotsLevel3", spellSlotsLevel3)
                    putInt("spellSlotsLevel4", spellSlotsLevel4)
                    putInt("spellSlotsLevel5", spellSlotsLevel5)
                }

                if (characterData.charClass == CharacterClass.RANGER) {
                    when (lvl) {
                        5 -> {
                            addCustomTrait("Zusätzlicher Angriff (Level 5)", "Du kannst zweimal angreifen, wenn du die Angriffsaktion ausführst.")
                        }
                        6 -> {
                            addCustomTrait("Umherziehen / Roving (Level 6)", "Deine Bewegungsrate erhöht sich um 3m, wenn du keine schwere Rüstung trägst. Du erhältst eine Kletter- und Schwimmrate in Höhe deiner Gehgeschwindigkeit.")
                        }
                        7 -> {
                            addCustomTrait("Außergewöhnliches Training (Level 7)", "Die Bestie kann Spurt, Rückzug, Ausweichen oder Hilfe als Bonusaktion nutzen. Ihre Angriffe können nun Wuchtschaden oder Energieschaden (Force) verursachen.")
                        }
                        9 -> {
                            addCustomTrait("Expertise 2 (Level 9)", "Wähle zwei weitere Fertigkeiten für Expertise aus dem Handbuch.")
                        }
                        10 -> {
                            addCustomTrait("Unermüdlich / Tireless (Level 10)", "Temporäre Trefferpunkte: Als Magie-Aktion erhältst du 1W8 + WIS-Mod TP (Nutzungen = WIS-Mod pro Tag). Erschöpfung: Eine Kurze Rast verringert deine Erschöpfung um 1 Stufe.")
                        }
                    }
                } else if (characterData.charClass == CharacterClass.WARLOCK) {
                    when (lvl) {
                        5 -> {
                            addCustomTrait("Schauerliche Anrufung (Level 5)", "Du erlernst eine neue Anrufung. Empfehlung für Nahkämpfer: 'Dürstende Klinge' (Extra-Angriff) oder 'Schauerliches Niederstrecken'.")
                        }
                        6 -> {
                            addCustomTrait("Hellseherischer Kämpfer / Clairvoyant Combatant (Level 6)", "Du kannst eine mental verbundene Kreatur (Erwachter Geist) zu einem WIS-Rettungswurf zwingen. Fehlschlag: Sie hat Nachteil auf Angriffe gegen dich, du hast Vorteil auf Angriffe gegen sie. (1x pro Rast oder Zauberplatz).")
                        }
                        7 -> {
                            addCustomTrait("Schauerliche Anrufung (Level 7)", "Du erlernst eine neue Anrufung (insgesamt 4).")
                        }
                        8 -> {
                            addCustomTrait("Attributswertverbesserung (Level 8)", "Wähle ein neues Talent oder erhöhe Attribute (z.B. Charisma auf 20).")
                        }
                        9 -> {
                            addCustomTrait("Schutzherrn kontaktieren (Level 9)", "Du hast 'Kontakt zu anderen Ebenen' vorbereitet. Du kannst ihn 1x pro Langer Rast kostenlos wirken und bestehst den Rettungswurf automatisch.")
                        }
                        10 -> {
                            addCustomTrait("Schauerliches Verwünschen (Level 10)", "Der Zauber 'Verwünschen (Hex)' ist immer vorbereitet. Er verursacht zusätzlich Nachteil auf Rettungswürfe des gewählten Attributs.")
                            addCustomTrait("Gedankenschild (Level 10)", "Deine Gedanken können nicht gelesen werden. Resistenz gegen Psychischen Schaden. Wer dir Psychischen Schaden zufügt, erleidet dieselbe Menge an Schaden.")
                        }
                    }
                }
            }
        }
    }

    fun dismissLevelUpDialog() {
        showLevelUpDialog = false
    }

    fun dismissLevelUpNotification() {
        showLevelUpNotification = false
    }

    fun openCharacterEdit() { showCharacterEditDialog = true }
    fun closeCharacterEdit() { showCharacterEditDialog = false }

    // --- SETUP WIZARD ---
    var setupComplete by mutableStateOf<Boolean?>(null)
        private set

    fun checkSetupComplete(uid: String) {
        viewModelScope.launch {
            try {
                setupComplete = characterRepository.isSetupComplete(uid)
            } catch (e: Exception) {
                Log.e("CharacterVM", "Setup check failed, assuming incomplete", e)
                setupComplete = false
            }
        }
    }

    fun markSetupComplete(uid: String) {
        setupComplete = true
        viewModelScope.launch {
            try {
                characterRepository.markSetupComplete(uid)
            } catch (e: Exception) {
                Log.e("CharacterVM", "Failed to persist setup flag", e)
            }
        }
    }

    fun saveCharacterFromSetup(
        uid: String,
        name: String,
        race: String,
        charClasses: List<CharacterClass>,
        subclass: String,
        str: Int, dex: Int, con: Int, int: Int, wis: Int, cha: Int,
        maxHpVal: Int,
        hitDiceVal: Int,
        levelVal: Int = 1,
        background: String,
        starterItems: List<InventoryItem>
    ) {
        val wisMod = (wis - 10) / 2
        val newData = characterData.copy(
            id = uid,
            name = name,
            race = race,
            charClasses = charClasses,
            subclass = subclass,
            background = background,
            passivePerception = 10 + wisMod,
            baseStrength = str,
            baseDexterity = dex,
            baseConstitution = con,
            baseIntelligence = int,
            baseWisdom = wis,
            baseCharisma = cha,
            baseMaxHp = maxHpVal,
            baseHitDice = hitDiceVal,
            baseLevel = levelVal
        )
        strength = str
        dexterity = dex
        constitution = con
        intelligence = int
        wisdom = wis
        charisma = cha
        maxHp = maxHpVal
        currentHp = maxHpVal
        hitDice = hitDiceVal
        level = levelVal
        prefs.edit {
            putInt("strength", str)
            putInt("dexterity", dex)
            putInt("constitution", con)
            putInt("intelligence", int)
            putInt("wisdom", wis)
            putInt("charisma", cha)
            putInt("maxHp", maxHpVal)
            putInt("currentHp", maxHpVal)
            putInt("hitDice", hitDiceVal)
            putInt("level", levelVal)
        }
        if (starterItems.isNotEmpty()) {
            val newItems = starterItems.filter { item -> customLoot.none { it.name == item.name } }
            customLoot.addAll(newItems)
            saveLoot()
        }
        viewModelScope.launch {
            characterRepository.saveCharacterToFirestore(uid, newData)
            characterRepository.saveCharacter(newData)
        }
    }

    fun saveCharacterData(updated: CharacterData) {
        strength = updated.baseStrength
        dexterity = updated.baseDexterity
        constitution = updated.baseConstitution
        intelligence = updated.baseIntelligence
        wisdom = updated.baseWisdom
        charisma = updated.baseCharisma
        maxHp = updated.baseMaxHp
        currentHp = currentHp.coerceAtMost(maxHp)
        hitDice = updated.baseHitDice
        level = updated.baseLevel
        currentEP = updated.baseEP
        prefs.edit {
            putInt("strength", strength)
            putInt("dexterity", dexterity)
            putInt("constitution", constitution)
            putInt("intelligence", intelligence)
            putInt("wisdom", wisdom)
            putInt("charisma", charisma)
            putInt("maxHp", maxHp)
            putInt("currentHp", currentHp)
            putInt("hitDice", hitDice)
            putInt("level", level)
            putInt("currentEP_${updated.id}", currentEP)
            putInt("currentEP", currentEP)
        }
        val wisMod = (updated.baseWisdom - 10) / 2
        val withPerception = updated.copy(passivePerception = 10 + wisMod)
        viewModelScope.launch {
            characterRepository.saveCharacter(withPerception)
            try {
                characterRepository.saveCharacterToFirestore(activeCharacterId, withPerception)
            } catch (e: Exception) {
                Log.e("CharacterVM", "Firestore save failed in saveCharacterData", e)
            }
        }
        closeCharacterEdit()
    }

    private fun saveCurrentStateToFirestore() {
        val wisMod = (wisdom - 10) / 2
        val snapshot = characterData.copy(
            baseStrength = strength,
            baseDexterity = dexterity,
            baseConstitution = constitution,
            baseIntelligence = intelligence,
            baseWisdom = wisdom,
            baseCharisma = charisma,
            baseMaxHp = maxHp,
            baseHitDice = hitDice,
            baseLevel = level,
            baseEP = currentEP,
            passivePerception = 10 + wisMod
        )
        viewModelScope.launch {
            try {
                characterRepository.saveCharacter(snapshot)
                characterRepository.saveCharacterToFirestore(activeCharacterId, snapshot)
            } catch (e: Exception) {
                Log.e("CharacterVM", "Firestore sync failed", e)
            }
        }
    }

    fun uploadPortrait(uid: String, imageUri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val url = characterRepository.uploadPortrait(uid, imageUri)
                val updated = characterData.copy(portraitUrl = url)
                characterRepository.saveCharacter(updated)
                characterRepository.saveCharacterToFirestore(uid, updated)
            } catch (e: Exception) {
                Log.e("CharacterVM", "Portrait upload failed", e)
            }
        }
    }

    var targetRulebookChapter by mutableStateOf<String?>(null)
    var targetRulebookSearch by mutableStateOf<String?>(null)

    fun applyHpIncrease(conModifier: Int, rolledHp: Int = 6) {
        var hpIncrease = rolledHp + conModifier
        if (characterData.charClass == CharacterClass.WARLOCK) {
            hpIncrease += 1
        }
        maxHp += hpIncrease
        hitDice += 1
        currentHp = (currentHp + hpIncrease).coerceAtMost(maxHp)
        prefs.edit {
            putInt("maxHp", maxHp)
            putInt("hitDice", hitDice)
            putInt("currentHp", currentHp)
        }
        saveCurrentStateToFirestore()
    }

    fun updateAttributes(strMod: Int = 0, dexMod: Int = 0, conMod: Int = 0, intMod: Int = 0, wisMod: Int = 0, chaMod: Int = 0) {
        strength += strMod
        dexterity += dexMod
        constitution += conMod
        intelligence += intMod
        wisdom += wisMod
        charisma += chaMod

        prefs.edit {
            putInt("strength", strength)
            putInt("dexterity", dexterity)
            putInt("constitution", constitution)
            putInt("intelligence", intelligence)
            putInt("wisdom", wisdom)
            putInt("charisma", charisma)
        }
        saveCurrentStateToFirestore()
    }

    // Runs once per character prefs file; forces maxHp from JSON to clear stale/swapped prefs.
    private fun applySyncV3() {
        val key = "isSyncedStats_2026_03_25_v3"
        if (!prefs.getBoolean(key, false)) {
            maxHp = characterData.baseMaxHp
            currentHp = currentHp.coerceAtMost(maxHp)
            prefs.edit {
                putBoolean(key, true)
                putInt("maxHp", maxHp)
                putInt("currentHp", currentHp)
            }
        }
    }

    private fun saveBaseStats() {
        prefs.edit {
            putInt("strength", strength)
            putInt("dexterity", dexterity)
            putInt("constitution", constitution)
            putInt("intelligence", intelligence)
            putInt("wisdom", wisdom)
            putInt("charisma", charisma)
            putInt("maxHp", maxHp)
            putInt("level", level)
        }
    }

    fun getMaxSpellSlots(lvl: Int, slotLvl: Int): Int =
        DndCalculations.spellSlotCount(characterData.charClass, lvl, slotLvl)

    val proficiencyBonus: Int get() = DndCalculations.proficiencyBonus(level)

    // Trefferwürfelgröße je nach Klasse (Ranger d10, Hexenmeister d8)
    val hitDie: Int get() = when (characterData.charClass) {
        CharacterClass.RANGER -> 10
        CharacterClass.WARLOCK -> 8
        else -> 8
    }

    val strMod: Int get() = DndCalculations.abilityMod(strength)
    val dexMod: Int get() = DndCalculations.abilityMod(dexterity)
    val conMod: Int get() = DndCalculations.abilityMod(constitution)
    val intMod: Int get() = DndCalculations.abilityMod(intelligence)
    val wisMod: Int get() = DndCalculations.abilityMod(wisdom)
    val chaMod: Int get() = (charisma - 10) / 2

    val speed: Int get() = characterData.speed
    val initiative: Int get() = dexMod
    val passivePerception: Int get() = 10 + wisMod + if (characterData.proficientSkills.contains("Wahrnehmung")) proficiencyBonus else 0

    // Neue Metadata-Properties für die UI
    val characterName: String get() = characterData.name
    val characterRace: String get() = characterData.race
    val characterBackground: String get() = characterData.background
    val characterAlignment: String get() = characterData.alignment

    fun getSkillModifier(skillName: String): Int {
        val baseMod = when (skillName) {
            "Athletik" -> strMod
            "Akrobatik", "Fingerfertigkeit", "Heimlichkeit" -> dexMod
            "Arkane Kunde", "Geschichte", "Nachforschung", "Naturkunde", "Religionskunde" -> intMod
            "Mit Tieren umgehen", "Motiv erkennen", "Heilkunde", "Wahrnehmung", "Überlebenskunst" -> wisMod
            "Täuschen", "Einschüchtern", "Auftreten", "Überzeugen" -> chaMod
            else -> 0
        }

        var totalBonus = baseMod

        // "Überlebenskunst" in code corresponds to "Überleben" in JSON.
        val jsonSkillName = if (skillName == "Überlebenskunst") "Überleben" else skillName
        if (characterData.proficientSkills.contains(jsonSkillName)) {
            totalBonus += proficiencyBonus
            if (characterData.expertiseSkills.contains(skillName)) {
                totalBonus += proficiencyBonus
            }
        }

        // Gloves of Arcana +2
        if (skillName == "Arkane Kunde" && customLoot.any { it.name.contains("Handschuhe der arkanen Kunde", ignoreCase = true) }) {
            totalBonus += 2
        }

        return totalBonus
    }

    val spellAttackBonus: Int get() = if (characterData.charClass == CharacterClass.WARLOCK) proficiencyBonus + chaMod else proficiencyBonus + wisMod
    val spellSaveDc: Int get() = 8 + if (characterData.charClass == CharacterClass.WARLOCK) proficiencyBonus + chaMod else proficiencyBonus + wisMod
    val spellModifier: Int get() = if (characterData.charClass == CharacterClass.WARLOCK) chaMod else wisMod

    var maxHp by mutableIntStateOf(prefs.getInt("maxHp", characterData.baseMaxHp))
        private set
    var currentHp by mutableIntStateOf(prefs.getInt("currentHp", maxHp))
        private set
    var tempHp by mutableIntStateOf(prefs.getInt("tempHp", 0))
        private set
    var hitDice by mutableIntStateOf(prefs.getInt("hitDice", characterData.baseHitDice))
        private set

    // HP properties are now initialized — run one-time correction for the initial character.
    // (loadProfile has an early-return guard that skips this character, so it must run here.)
    init { applySyncV3() }

    var deathSaveSuccesses by mutableIntStateOf(prefs.getInt("deathSaveSuccesses", 0))
        private set
    var deathSaveFailures by mutableIntStateOf(prefs.getInt("deathSaveFailures", 0))
        private set

    fun updateDeathSaves(successes: Int, failures: Int) {
        deathSaveSuccesses = successes.coerceIn(0, 3)
        deathSaveFailures = failures.coerceIn(0, 3)
        prefs.edit {
            putInt("deathSaveSuccesses", deathSaveSuccesses)
            putInt("deathSaveFailures", deathSaveFailures)
        }
    }

    fun toggleHeroicInspiration(active: Boolean) {
        heroicInspiration = active
        prefs.edit { putBoolean("heroicInspiration_${activeCharacterId}", active) }
    }

    fun castGoodberry() {
        if (spellSlotsLevel1 > 0) {
            spellSlotsLevel1--
            goodberries += 10
            prefs.edit { 
                putInt("spellSlotsLevel1", spellSlotsLevel1)
                putInt("goodberries", goodberries)
            }
            snackbarMessage.value = "Gute Beeren gewirkt (+10 Beeren)"
        }
    }

    fun getWeaponName(index: Int): String {
        return prefs.getString("weaponName_$index", null) ?: when {
            characterData.charClass == com.example.dndcompanion.data.CharacterClass.RANGER -> {
                when(index) {
                    0 -> "Langbogen"
                    1 -> "Kurzschwert\n& Schild"
                    else -> "Shillelagh\n& Schild"
                }
            }
            else -> {
                when(index) {
                    0 -> "Kriegshammer\n(Pakt)"
                    else -> "Speer\n(Pakt)"
                }
            }
        }
    }

    fun saveWeaponName(index: Int, name: String) {
        prefs.edit { putString("weaponName_$index", name) }
    }

    fun takeDamage(amount: Int) {
        var remainingDamage = amount
        if (tempHp > 0) {
            if (tempHp >= remainingDamage) {
                tempHp -= remainingDamage
                remainingDamage = 0
            } else {
                remainingDamage -= tempHp
                tempHp = 0
            }
            prefs.edit { putInt("tempHp", tempHp) }
        }

        if (remainingDamage > 0) {
            currentHp = (currentHp - remainingDamage).coerceAtLeast(0)
            if (currentHp > 0) {
                updateDeathSaves(0, 0)
            }
            prefs.edit { putInt("currentHp", currentHp) }
        }
    }

    fun healManual(amount: Int) {
        currentHp = (currentHp + amount).coerceAtMost(maxHp)
        if (currentHp > 0) {
            updateDeathSaves(0, 0)
        }
        prefs.edit { putInt("currentHp", currentHp) }
    }

    fun modifyTempHp(amount: Int) {
        tempHp = (tempHp + amount).coerceAtLeast(0)
        prefs.edit { putInt("tempHp", tempHp) }
    }

    // Single source of truth: CombatViewModel writes this, CharacterViewModel reads it on-demand (e.g. AI summary)
    val currentWeapon: ActiveWeapon
        get() {
            val default = if (characterData.charClass == CharacterClass.RANGER) ActiveWeapon.LANGBOGEN.name else ActiveWeapon.KRIEGSHAMMER_PAKT.name
            return ActiveWeapon.valueOf(prefs.getString("currentWeapon", default) ?: default)
        }
    val currentArmorClass: Int
        get() {
            // Grund-RK
            var baseAc = if (isMageArmorActive) 13 else 10
            
            // Rüstung aus Inventar (die höchste zählt)
            var armorBonus = 0
            if (customLoot.any { it.name.contains("Plattenpanzer", ignoreCase = true) }) {
                baseAc = 18 // Heavy (no Dex)
            } else if (customLoot.any { it.name.contains("Kettenpanzer", ignoreCase = true) }) {
                baseAc = 16 // Heavy (no Dex)
            } else if (customLoot.any { it.name.contains("Brustpanzer", ignoreCase = true) }) {
                baseAc = 14 // Medium (max +2 Dex)
                armorBonus = dexMod.coerceAtMost(2)
            } else if (customLoot.any { it.name.contains("Schuppenpanzer", ignoreCase = true) }) {
                baseAc = 14 // Medium (max +2 Dex)
                armorBonus = dexMod.coerceAtMost(2)
            } else if (customLoot.any { it.name.contains("Beschlagene Lederrüstung", ignoreCase = true) }) {
                baseAc = 12 // Light (full Dex)
                armorBonus = dexMod
            } else if (customLoot.any { it.name.contains("Lederrüstung", ignoreCase = true) }) {
                baseAc = 11 // Light (full Dex)
                armorBonus = dexMod
            } else {
                armorBonus = dexMod
            }

            var ac = baseAc + armorBonus
            
            // Schild-Bonus
            if (isShieldEquipped && !isUsingTwoHanded && hasShieldInInventory) {
                ac += 2
            }
            
            return ac
        }

    // Finesse-Waffe: verwende den höheren Wert aus STR und DEX
    private val finesseWeaponMod: Int get() = maxOf(strMod, dexMod)

    val currentAttackBonus: String
        get() = when (currentWeapon) {
            ActiveWeapon.LANGBOGEN -> "+${proficiencyBonus + dexMod + 2}"
            ActiveWeapon.KURZSCHWERT_SCHILD -> "+${proficiencyBonus + finesseWeaponMod}"
            ActiveWeapon.SHILLELAGH_SCHILD -> "+${proficiencyBonus + wisMod}"
            ActiveWeapon.KRIEGSHAMMER_PAKT -> "+${proficiencyBonus + strMod}"
            ActiveWeapon.SPEER_PAKT -> "+${proficiencyBonus + strMod}"
        }

    val currentDamage: String
        get() = when (currentWeapon) {
            ActiveWeapon.LANGBOGEN -> "1W8 + $dexMod Stich (Verlangsamen: -3m Tempo)"
            ActiveWeapon.KURZSCHWERT_SCHILD -> "1W6 + $finesseWeaponMod Stich (Ärgern: Vorteil auf nächsten Angriff)"
            ActiveWeapon.SHILLELAGH_SCHILD -> {
                val die = if (isUsingTwoHanded) "1W10" else "1W8"
                "$die + $wisMod Wucht (Umwerfen: KON-Save SG 12)"
            }
            ActiveWeapon.KRIEGSHAMMER_PAKT -> {
                val die = if (isUsingTwoHanded) "1W10" else "1W8"
                "$die + $strMod Wucht (Stoß: bis zu 3m wegstoßen)"
            }
            ActiveWeapon.SPEER_PAKT -> {
                val die = if (isUsingTwoHanded) "1W8" else "1W6"
                "$die + $strMod Stich (Schwächen: Gegner hat Nachteil auf nächsten Angriff)"
            }
        }

    var spellSlotsLevel1 by mutableIntStateOf(prefs.getInt("spellSlotsLevel1", characterData.baseSpellSlotsLevel1))
        private set
    var spellSlotsLevel2 by mutableIntStateOf(prefs.getInt("spellSlotsLevel2", characterData.baseSpellSlotsLevel2))
        private set
    var spellSlotsLevel3 by mutableIntStateOf(prefs.getInt("spellSlotsLevel3", characterData.baseSpellSlotsLevel3))
        private set
    var spellSlotsLevel4 by mutableIntStateOf(prefs.getInt("spellSlotsLevel4", 0))
        private set
    var spellSlotsLevel5 by mutableIntStateOf(prefs.getInt("spellSlotsLevel5", 0))
        private set

    var huntersMarkFreeUses by mutableIntStateOf(prefs.getInt("huntersMarkFreeUses", 2))
        private set


    fun useSpellSlotLevel1() {
        if (spellSlotsLevel1 > 0) {
            spellSlotsLevel1--
            prefs.edit { putInt("spellSlotsLevel1", spellSlotsLevel1) }
        }
    }
    
    fun useSpellSlotLevel2() {
        if (spellSlotsLevel2 > 0) {
            spellSlotsLevel2--
            prefs.edit { putInt("spellSlotsLevel2", spellSlotsLevel2) }
        }
    }
    
    fun useSpellSlotLevel3() {
        if (spellSlotsLevel3 > 0) {
            spellSlotsLevel3--
            prefs.edit { putInt("spellSlotsLevel3", spellSlotsLevel3) }
        }
    }

    fun resetWarlockSlots() {
        spellSlotsLevel2 = characterData.baseSpellSlotsLevel2
        prefs.edit { putInt("spellSlotsLevel2", spellSlotsLevel2) }
    }

    fun applyMagicalCunning() {
        val maxSlots = if (level >= 17) 4 else if (level >= 11) 3 else 2
        val toRegain = kotlin.math.ceil(maxSlots / 2.0).toInt()
        
        if (level >= 9) {
            spellSlotsLevel5 = (spellSlotsLevel5 + toRegain).coerceAtMost(maxSlots)
            prefs.edit { putInt("spellSlotsLevel5", spellSlotsLevel5) }
        } else if (level >= 7) {
            spellSlotsLevel4 = (spellSlotsLevel4 + toRegain).coerceAtMost(maxSlots)
            prefs.edit { putInt("spellSlotsLevel4", spellSlotsLevel4) }
        } else if (level >= 5) {
            spellSlotsLevel3 = (spellSlotsLevel3 + toRegain).coerceAtMost(maxSlots)
            prefs.edit { putInt("spellSlotsLevel3", spellSlotsLevel3) }
        } else {
            spellSlotsLevel2 = (spellSlotsLevel2 + toRegain).coerceAtMost(maxSlots)
            prefs.edit { putInt("spellSlotsLevel2", spellSlotsLevel2) }
        }
    }

    fun useHuntersMarkFree() {
        if (huntersMarkFreeUses > 0) {
            huntersMarkFreeUses--
            prefs.edit { putInt("huntersMarkFreeUses", huntersMarkFreeUses) }
        }
    }

    var water by mutableFloatStateOf(prefs.getFloat("water", 2.0f))
        private set
    var rations by mutableIntStateOf(prefs.getInt("rations", 10))
        private set
    var goodberries by mutableIntStateOf(prefs.getInt("goodberries", 0))
        private set

    fun changeWater(amount: Float) {
        water = (water + amount).coerceAtLeast(0f)
        prefs.edit { putFloat("water", water) }
        saveInventoryToFirestore()
    }

    fun changeRations(amount: Int) {
        rations = (rations + amount).coerceAtLeast(0)
        prefs.edit { putInt("rations", rations) }
        saveInventoryToFirestore()
    }

    fun changeGoodberries(amount: Int) {
        goodberries = (goodberries + amount).coerceAtLeast(0)
        prefs.edit { putInt("goodberries", goodberries) }
        saveInventoryToFirestore()
    }

    fun eatGoodberry() {
        if (goodberries > 0 && currentHp < maxHp) {
            goodberries--
            currentHp = (currentHp + 1).coerceAtMost(maxHp)
            prefs.edit { 
                putInt("goodberries", goodberries)
                putInt("currentHp", currentHp)
            }
        }
    }

    // --- MÜNZEN (COINS) ---
    var coinsKM by mutableIntStateOf(prefs.getInt("coinsKM", 0))
        private set
    var coinsSM by mutableIntStateOf(prefs.getInt("coinsSM", 0))
        private set
    var coinsEM by mutableIntStateOf(prefs.getInt("coinsEM", 0))
        private set
    var coinsGM by mutableIntStateOf(prefs.getInt("coinsGM", 0))
        private set
    var coinsPM by mutableIntStateOf(prefs.getInt("coinsPM", 0))
        private set

    fun changeCoinsKM(amount: Int) {
        coinsKM = (coinsKM + amount).coerceAtLeast(0)
        prefs.edit { putInt("coinsKM", coinsKM) }
        saveInventoryToFirestore()
    }
    fun changeCoinsSM(amount: Int) {
        coinsSM = (coinsSM + amount).coerceAtLeast(0)
        prefs.edit { putInt("coinsSM", coinsSM) }
        saveInventoryToFirestore()
    }
    fun changeCoinsEM(amount: Int) {
        coinsEM = (coinsEM + amount).coerceAtLeast(0)
        prefs.edit { putInt("coinsEM", coinsEM) }
        saveInventoryToFirestore()
    }
    fun changeCoinsGM(amount: Int) {
        coinsGM = (coinsGM + amount).coerceAtLeast(0)
        prefs.edit { putInt("coinsGM", coinsGM) }
        saveInventoryToFirestore()
    }
    fun changeCoinsPM(amount: Int) {
        coinsPM = (coinsPM + amount).coerceAtLeast(0)
        prefs.edit { putInt("coinsPM", coinsPM) }
        saveInventoryToFirestore()
    }

    fun directSetCoinsKM(value: Int) { coinsKM = value.coerceAtLeast(0); prefs.edit { putInt("coinsKM", coinsKM) }; saveInventoryToFirestore() }
    fun directSetCoinsSM(value: Int) { coinsSM = value.coerceAtLeast(0); prefs.edit { putInt("coinsSM", coinsSM) }; saveInventoryToFirestore() }
    fun directSetCoinsEM(value: Int) { coinsEM = value.coerceAtLeast(0); prefs.edit { putInt("coinsEM", coinsEM) }; saveInventoryToFirestore() }
    fun directSetCoinsGM(value: Int) { coinsGM = value.coerceAtLeast(0); prefs.edit { putInt("coinsGM", coinsGM) }; saveInventoryToFirestore() }
    fun directSetCoinsPM(value: Int) { coinsPM = value.coerceAtLeast(0); prefs.edit { putInt("coinsPM", coinsPM) }; saveInventoryToFirestore() }

    var totalArrows by mutableIntStateOf(prefs.getInt("totalArrows", 20))
        private set
    var shotArrows by mutableIntStateOf(prefs.getInt("shotArrows", 0))
        private set

    fun shootArrow() {
        if (totalArrows > 0) {
            totalArrows--
            shotArrows++
            prefs.edit {
                putInt("totalArrows", totalArrows)
                putInt("shotArrows", shotArrows)
            }
        }
    }

    fun recoverArrows() {
        if (shotArrows > 0) {
            val recovered = shotArrows / 2 // Integer Division rundet automatisch ab
            totalArrows += recovered
            shotArrows = 0
            prefs.edit {
                putInt("totalArrows", totalArrows)
                putInt("shotArrows", shotArrows)
            }
        }
    }

    fun discardShotArrows() {
        shotArrows = 0
        prefs.edit { putInt("shotArrows", shotArrows) }
    }

    fun changeTotalArrows(amount: Int) {
        totalArrows = (totalArrows + amount).coerceAtLeast(0)
        prefs.edit { putInt("totalArrows", totalArrows) }
    }

    // --- EQUIPMENT-KATALOG ---
    val equipmentCatalog = mutableStateListOf<EquipmentCatalogItem>()

    // --- GEWICHTS-BERECHNUNG (in kg) ---
    val maxWeight: Double
        get() = DndCalculations.maxWeightKg(strength)  // D&D Traglast = STR × 15 Pfd. = STR × 7.5 kg
    val currentWeight: Double
        get() {
            var total = 0.0
            total += water * 2.5          // 1 Trinkschlauch (voll) = 2.5 kg
            total += rations * 1.0        // 1 Ration = 1 kg
            total += totalArrows * 0.02   // 1 Pfeil = ca. 0.02 kg
            total += customLoot.sumOf { it.amount * it.weight }
            return total
        }

    private fun saveLoot() {
        val json = gson.toJson(customLoot)
        prefs.edit { putString("customLoot", json) }
        if (!suppressInventoryFirestoreSync) saveInventoryToFirestore()
    }

    private fun loadLoot() {
        val jsonString = prefs.getString("customLoot", "") ?: ""
        if (jsonString.isNotEmpty()) {
            if (jsonString.startsWith("[")) {
                // Es ist sehr wahrscheinlich ein JSON-String
                try {
                    val type = object : TypeToken<List<InventoryItem>>() {}.type
                    val items: List<InventoryItem> = gson.fromJson(jsonString, type)
                    customLoot.clear()
                    customLoot.addAll(items)
                } catch (e: Exception) {
                    Log.w("CharacterVM", "Failed to parse custom loot JSON", e)
                    customLoot.clear()
                }
            } else {
                // Fallback für alte Speicherstände mit Strichpunkt und Pipe
                val items = jsonString.split(";").mapNotNull {
                    val parts = it.split("|")
                    if (parts.size == 2) InventoryItem(parts[0], parts[1].toIntOrNull() ?: 1) else null
                }
                customLoot.clear()
                customLoot.addAll(items)
            }
        } else {
            // Initiale Gegenstände beim allerersten Start laden
            customLoot.addAll(characterData.defaultLoot)
            saveLoot()
        }
    }

    fun addCustomLoot(itemName: String, weight: Double = 0.0, category: String = "Sonstiges", price: String? = null, quantity: Int = 1, notes: String? = null) {
        val index = customLoot.indexOfFirst { it.name.equals(itemName, ignoreCase = true) }
        if (index != -1) {
            val existingItem = customLoot[index]
            val newWeight = if (existingItem.weight == 0.0 && weight > 0.0) weight else existingItem.weight
            val newPrice = if (existingItem.price == null && price != null) price else existingItem.price
            val newNotes = if (existingItem.notes == null && notes != null) notes else existingItem.notes
            customLoot[index] = existingItem.copy(amount = existingItem.amount + quantity, weight = newWeight, price = newPrice, notes = newNotes)
        } else {
            customLoot.add(InventoryItem(itemName, quantity, weight, category, price = price, notes = notes))
        }
        saveLoot()
        snackbarMessage.value = "$itemName zum Rucksack hinzugefügt"
    }

    fun addFromCatalog(item: EquipmentCatalogItem) {
        val inventoryCategory = when {
            item.category.startsWith("Waffen") -> "Rüstung & Waffen"
            item.category == "Rüstung" -> "Rüstung & Waffen"
            item.category == "Werkzeug" -> "Werkzeug"
            item.category == "Ausrüstung" -> "Ausrüstung"
            else -> "Sonstiges"
        }
        addCustomLoot(item.name, item.weight, inventoryCategory, item.price)
    }

    fun buyItemFromCatalog(item: EquipmentCatalogItem): String {
        val priceInKM = parsePriceToKM(item.price)
        if (priceInKM <= 0) {
            addFromCatalog(item)
            return "${item.name} zum Rucksack hinzugefügt"
        }

        val totalOwnedKM = coinsKM + (coinsSM * 10) + (coinsEM * 50) + (coinsGM * 100) + (coinsPM * 1000)

        if (totalOwnedKM < priceInKM) {
            return "Zu wenig Geld! (Preis: ${item.price})"
        }

        val remainingKM = totalOwnedKM - priceInKM

        var tempRemaining = remainingKM
        val newPM = tempRemaining / 1000
        tempRemaining %= 1000

        val newGM = tempRemaining / 100
        tempRemaining %= 100

        // Skip optimizing EM, just use SM and KM to give exact change in common D&D fashion.
        val newSM = tempRemaining / 10
        tempRemaining %= 10

        val newKM = tempRemaining

        coinsPM = newPM
        coinsGM = newGM
        coinsSM = newSM
        coinsKM = newKM
        coinsEM = 0 // Convert any electrum they had into standard coins during exact change

        prefs.edit {
            putInt("coinsPM", coinsPM)
            putInt("coinsGM", coinsGM)
            putInt("coinsEM", coinsEM)
            putInt("coinsSM", coinsSM)
            putInt("coinsKM", coinsKM)
        }

        val inventoryCategory = when {
            item.category.startsWith("Waffen") -> "Rüstung & Waffen"
            item.category == "Rüstung" -> "Rüstung & Waffen"
            item.category == "Werkzeug" -> "Werkzeug"
            item.category == "Ausrüstung" -> "Ausrüstung"
            else -> "Sonstiges"
        }
        val itemName = item.name
        val index = customLoot.indexOfFirst { it.name.equals(itemName, ignoreCase = true) }
        if (index != -1) {
            val existingItem = customLoot[index]
            val newWeight = if (existingItem.weight == 0.0 && item.weight > 0.0) item.weight else existingItem.weight
            val newPrice = if (existingItem.price == null) item.price else existingItem.price
            customLoot[index] = existingItem.copy(amount = existingItem.amount + 1, weight = newWeight, price = newPrice)
        } else {
            customLoot.add(InventoryItem(itemName, 1, item.weight, inventoryCategory, price = item.price))
        }
        saveLoot()

        return "${item.name} für ${item.price} gekauft!"
    }

    fun sellItem(item: InventoryItem): String {
        val priceStr = item.price
        if (priceStr.isNullOrBlank() || priceStr == "-" || priceStr == "—") {
            removeCustomLoot(item.name)
            return "${item.name} abgelegt (kein Preis hinterlegt)"
        }
        val halfValueKM = parsePriceToKM(priceStr) / 2
        if (halfValueKM <= 0) {
            removeCustomLoot(item.name)
            return "${item.name} abgelegt"
        }

        // Distribute sell proceeds into coin denominations using existing changeCoins* functions
        var remaining = halfValueKM
        val pm = remaining / 1000; remaining %= 1000
        val gm = remaining / 100; remaining %= 100
        val sm = remaining / 10; remaining %= 10
        val km = remaining
        if (pm > 0) changeCoinsPM(pm)
        if (gm > 0) changeCoinsGM(gm)
        if (sm > 0) changeCoinsSM(sm)
        if (km > 0) changeCoinsKM(km)

        removeCustomLoot(item.name)

        val sellPriceDisplay = formatKMToCoins(halfValueKM)
        return "${item.name} verkauft für $sellPriceDisplay"
    }

    private fun formatKMToCoins(km: Int): String {
        if (km <= 0) return "0 KM"
        var remaining = km
        val parts = mutableListOf<String>()
        val pm = remaining / 1000; remaining %= 1000; if (pm > 0) parts.add("$pm PM")
        val gm = remaining / 100; remaining %= 100; if (gm > 0) parts.add("$gm GM")
        val sm = remaining / 10; remaining %= 10; if (sm > 0) parts.add("$sm SM")
        if (remaining > 0) parts.add("$remaining KM")
        return parts.joinToString(" ")
    }

    private fun parsePriceToKM(priceStr: String): Int {
        if (priceStr.isBlank() || priceStr == "-" || priceStr == "—") return 0
        val cleanStr = priceStr.replace(".", "")
        val regex = Regex("""(\d+)\s*(GM|SM|KM|EM|PM)""", RegexOption.IGNORE_CASE)
        var totalKM = 0
        val matches = regex.findAll(cleanStr)
        for (match in matches) {
            val amount = match.groupValues[1].toIntOrNull() ?: continue
            totalKM += when (match.groupValues[2].uppercase()) {
                "PM" -> amount * 1000
                "GM" -> amount * 100
                "EM" -> amount * 50
                "SM" -> amount * 10
                "KM" -> amount
                else -> 0
            }
        }
        return totalKM
    }

    // --- INVENTAR FIRESTORE ---
    private fun saveInventoryToFirestore() {
        val uid = activeCharacterId
        if (uid == "Athania" || uid == "Delat") return
        val lootJson = gson.toJson(customLoot.toList())
        val data = mapOf(
            "customLootJson" to lootJson,
            "coinsKM" to coinsKM,
            "coinsSM" to coinsSM,
            "coinsEM" to coinsEM,
            "coinsGM" to coinsGM,
            "coinsPM" to coinsPM,
            "totalArrows" to totalArrows,
            "shotArrows" to shotArrows,
            "water" to water,
            "rations" to rations,
            "goodberries" to goodberries
        )
        firestore.collection("users").document(uid)
            .collection("inventory").document("main")
            .set(data)
            .addOnFailureListener { Log.e("CharacterVM", "Firestore inventory save failed", it) }
    }

    private fun loadInventoryFromFirestore(uid: String) {
        if (uid == "Athania" || uid == "Delat") return
        firestore.collection("users").document(uid)
            .collection("inventory").document("main")
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val lootJson = doc.getString("customLootJson")
                    if (!lootJson.isNullOrEmpty()) {
                        try {
                            val type = object : TypeToken<List<InventoryItem>>() {}.type
                            val items: List<InventoryItem> = gson.fromJson(lootJson, type)
                            customLoot.clear()
                            customLoot.addAll(items)
                            prefs.edit { putString("customLoot", lootJson) }
                        } catch (e: Exception) {
                            Log.w("CharacterVM", "Failed to parse Firestore inventory loot", e)
                        }
                    }
                    doc.getLong("coinsKM")?.toInt()?.let { coinsKM = it; prefs.edit { putInt("coinsKM", it) } }
                    doc.getLong("coinsSM")?.toInt()?.let { coinsSM = it; prefs.edit { putInt("coinsSM", it) } }
                    doc.getLong("coinsEM")?.toInt()?.let { coinsEM = it; prefs.edit { putInt("coinsEM", it) } }
                    doc.getLong("coinsGM")?.toInt()?.let { coinsGM = it; prefs.edit { putInt("coinsGM", it) } }
                    doc.getLong("coinsPM")?.toInt()?.let { coinsPM = it; prefs.edit { putInt("coinsPM", it) } }
                    doc.getLong("totalArrows")?.toInt()?.let { totalArrows = it; prefs.edit { putInt("totalArrows", it) } }
                    doc.getLong("shotArrows")?.toInt()?.let { shotArrows = it; prefs.edit { putInt("shotArrows", it) } }
                    doc.getDouble("water")?.toFloat()?.let { water = it; prefs.edit { putFloat("water", it) } }
                    doc.getLong("rations")?.toInt()?.let { rations = it; prefs.edit { putInt("rations", it) } }
                    doc.getLong("goodberries")?.toInt()?.let { goodberries = it; prefs.edit { putInt("goodberries", it) } }
                }
            }
            .addOnFailureListener { Log.w("CharacterVM", "Failed to load Firestore inventory", it) }
    }

    // --- FREIE MERKMALE (TRAITS) ---    // --- FREE SPELLS LOGIC ---
    fun canCastAsRitual(spell: Spell): Boolean {
        if (!spell.isRitual) return false
        
        // Magier benötigen den Zauber nicht vorbereitet, solange er im globalen Buch (ihrem Zauberbuch) steht
        return if (characterData.charClass == CharacterClass.WARLOCK) {
            spell.isPrepared
        } else if (characterData.charClass == CharacterClass.RANGER) {
            spell.isPrepared
        } else {
            // Default: Muss vorbereitet sein (außer wir implementieren Wizard explizit)
            spell.isPrepared
        }
    }

    fun useTraitSpell(trait: TraitItem) {
        val index = customTraits.indexOf(trait)
        if (index != -1 && trait.currentUses > 0) {
            // Kopie erstellen und verringern, um UI-Update auszulösen
            val updatedTrait = trait.copy(currentUses = trait.currentUses - 1)
            customTraits[index] = updatedTrait

            // SPEZIAL-EFFEKTE basierend auf dem Zauber
            when (updatedTrait.grantedSpellId) {
                "Gute Beere" -> {
                    inventoryVm?.changeGoodberries(10) ?: run {
                        goodberries += 10
                        prefs.edit { putInt("goodberries", goodberries) }
                    }
                }
                "Falsches Leben" -> {
                    combatVm?.applyFalseLife() ?: applyFalseLife()
                }
            }
            saveTraits()
        }
    }

    fun useItemCharge(item: InventoryItem, spellId: String) {
        val cost = item.spellCharges?.get(spellId) ?: 0
        if (item.currentCharges >= cost) {
            item.currentCharges -= cost
            saveLoot()
        }
    }

    private fun saveTraits() {
        val json = gson.toJson(customTraits)
        prefs.edit { putString("customTraits", json) }
    }
    
    private fun loadTraits() {
        val jsonString = prefs.getString("customTraits", "") ?: ""
        if (jsonString.isNotEmpty()) {
            try {
                val type = object : TypeToken<List<TraitItem>>() {}.type
                val items: List<TraitItem> = gson.fromJson(jsonString, type)
                customTraits.clear()
                customTraits.addAll(items)
                
                // Sicherstellen, dass neue Standard-Merkmale (Free Spells) auch bei bestehenden Saves erscheinen
                // UND Namen, grantedSpellId und maxUses aktualisiert werden (Migration)
                var listChanged = false
                characterData.defaultTraits.forEach { defaultTrait ->
                    val existingBySpellId = if (defaultTrait.grantedSpellId != null) {
                        customTraits.indexOfFirst { it.grantedSpellId != null && it.grantedSpellId == defaultTrait.grantedSpellId }
                    } else -1

                    if (existingBySpellId != -1) {
                        // Trait mit gleicher grantedSpellId gefunden – Namen ggf. aktualisieren
                        if (customTraits[existingBySpellId].name != defaultTrait.name) {
                            customTraits[existingBySpellId] = customTraits[existingBySpellId].copy(name = defaultTrait.name)
                            listChanged = true
                        }
                    } else {
                        val nameIndex = customTraits.indexOfFirst { it.name == defaultTrait.name }
                        if (nameIndex == -1) {
                            // Neuer Trait – hinzufügen
                            customTraits.add(defaultTrait)
                            listChanged = true
                        } else {
                            // Trait per Name gefunden – grantedSpellId oder maxUses aktualisieren (Migration)
                            val existing = customTraits[nameIndex]
                            if (existing.grantedSpellId != defaultTrait.grantedSpellId || existing.maxUses != defaultTrait.maxUses) {
                                customTraits[nameIndex] = defaultTrait
                                listChanged = true
                            }
                        }
                    }
                }
                if (listChanged) saveTraits()
            } catch (e: Exception) {
                Log.w("CharacterVM", "Failed to load traits", e)
            }
        } else {
            // Initiale Merkmale beim allerersten Start laden
            customTraits.addAll(characterData.defaultTraits)
            saveTraits()
        }
    }

    fun addCustomTrait(name: String, desc: String) {
        customTraits.add(TraitItem(name, desc))
        saveTraits()
    }

    fun removeCustomTrait(index: Int) {
        if (index in customTraits.indices) {
            customTraits.removeAt(index)
            saveTraits()
        }
    }

    fun updateCustomTrait(index: Int, name: String, desc: String) {
        if (index in customTraits.indices) {
            customTraits[index] = TraitItem(name, desc)
            saveTraits()
        }
    }

    // --- FEATURE SELECTION UI ---
    var showFeatureSelection by mutableStateOf(false)
    var lastSelectedFeature by mutableStateOf<FeatureEntity?>(null)

    fun learnFeature(feature: FeatureEntity) {
        if (customTraits.none { it.name == feature.name }) {
            // If this feature grants multiple spells (e.g. Drow lineage → Feenfeuer + Dunkelheit),
            // add a separate TraitItem per spell instead of a single combined trait.
            val multiSpells = feature.grantedSpellIds
            if (!multiSpells.isNullOrEmpty()) {
                multiSpells.forEach { spellId ->
                    customTraits.add(TraitItem(
                        name = spellId,
                        desc = "1x pro Lange Rast kostenlos wirkbar.",
                        grantedSpellId = spellId,
                        maxUses = feature.grantedSpellUses.coerceAtLeast(1),
                        currentUses = feature.grantedSpellUses.coerceAtLeast(1)
                    ))
                }
            }

            customTraits.add(TraitItem(
                name = feature.name,
                desc = feature.description,
                maxUses = feature.grantedSpellUses,
                currentUses = feature.grantedSpellUses,
                grantedSpellId = if (multiSpells.isNullOrEmpty()) feature.grantedSpellId else null
            ))
            lastSelectedFeature = feature
            saveTraits()
        }
    }

    fun dismissFeatureSelection() {
        showFeatureSelection = false
        lastSelectedFeature = null
    }

    fun unlearnFeature(featureName: String) {
        customTraits.removeAll { it.name == featureName }
        saveTraits()
    }

    // --- TRAIT RESET HELPERS (called by CombatViewModel) ---
    fun resetTraitsForShortRest() {
        var changed = false
        customTraits.forEachIndexed { index, trait ->
            if (trait.resetOnShortRest && trait.currentUses < trait.maxUses) {
                customTraits[index] = trait.copy(currentUses = trait.maxUses)
                changed = true
            }
        }
        if (changed) saveTraits()
    }

    fun resetTraitsForLongRest() {
        var changed = false
        customTraits.forEachIndexed { index, trait ->
            if (trait.currentUses < trait.maxUses) {
                customTraits[index] = trait.copy(currentUses = trait.maxUses)
                changed = true
            }
        }
        if (changed) saveTraits()
    }

    // --- SIBLING VM VERBINDUNGEN ---
    private var combatVm: CombatViewModel? = null
    private var spellVm: SpellViewModel? = null
    private var inventoryVm: InventoryViewModel? = null

    fun connectSiblings(
        combatVm: CombatViewModel,
        spellVm: SpellViewModel,
        inventoryVm: InventoryViewModel
    ) {
        this.combatVm = combatVm
        this.spellVm = spellVm
        this.inventoryVm = inventoryVm
    }

    // --- WERTE ZURÜCKSETZEN ---
    fun resetToDefaults() {
        // Alle SharedPreferences löschen und Grundwerte gemäß characterData setzen
        prefs.edit { clear() }

        currentEP = characterData.baseEP
        level = characterData.baseLevel
        strength = characterData.baseStrength
        dexterity = characterData.baseDexterity
        constitution = characterData.baseConstitution
        intelligence = characterData.baseIntelligence
        wisdom = characterData.baseWisdom
        charisma = characterData.baseCharisma
        maxHp = characterData.baseMaxHp
        currentHp = characterData.baseMaxHp
        hitDice = characterData.baseHitDice
        spellSlotsLevel1 = characterData.baseSpellSlotsLevel1
        spellSlotsLevel2 = characterData.baseSpellSlotsLevel2
        spellSlotsLevel3 = characterData.baseSpellSlotsLevel3
        huntersMarkFreeUses = 2
        freeAmuletSpellUsed = false
        freeFaerieFireUsed = false
        freeDarknessUsed = false
        freeDruidSpellUsed = false
        val isRanger = characterData.charClass == CharacterClass.RANGER
        water = 2.0f
        rations = if (isRanger) 10 else 3
        goodberries = if (isRanger) 10 else 1
        coinsKM = if (isRanger) 20 else 0
        coinsSM = if (isRanger) 1 else 9
        coinsEM = 0
        coinsGM = if (isRanger) 44 else 72
        coinsPM = 0
        totalArrows = if (isRanger) 26 else 0
        shotArrows = 0
        deathSaveSuccesses = 0
        deathSaveFailures = 0
        activeBeastType = BeastType.SKY
        generalBookEntries.clear()
        saveGeneralBookEntries()
        grudgeBookEntries.clear()
        standardTactic = if (isRanger) "1. Zeichen des Jägers wirken (Bonusaktion)\n2. Mit Langbogen angreifen" else ""

        // Loot und Traits zurücksetzen
        customLoot.clear()
        customLoot.addAll(characterData.defaultLoot)
        saveLoot()
        customTraits.clear()
        customTraits.addAll(characterData.defaultTraits)
        saveTraits()
    }

    // --- PROFIL WECHSELN ---
    fun loadProfile(characterId: String) {
        if (activeCharacterId == characterId) return
        activeCharacterId = characterId
        characterData = characterRepository.getCharacterOrDefault(characterId)
        prefsManager.switchCharacter(characterId)

        // Use maxOf(prefs, jsonBase) so that DM upgrades in characters.json are always picked up,
        // but in-app increases (level-up dialog, edit dialog) are preserved if they are higher.
        val jsonBase = characterData  // set above from getCharacter() — always fresh JSON values
        currentEP = maxOf(prefs.getInt("currentEP", jsonBase.baseEP), jsonBase.baseEP)
        level = maxOf(prefs.getInt("level", jsonBase.baseLevel), jsonBase.baseLevel)
        strength = prefs.getInt("strength", jsonBase.baseStrength)
        dexterity = prefs.getInt("dexterity", jsonBase.baseDexterity)
        constitution = prefs.getInt("constitution", jsonBase.baseConstitution)
        intelligence = prefs.getInt("intelligence", jsonBase.baseIntelligence)
        wisdom = prefs.getInt("wisdom", jsonBase.baseWisdom)
        charisma = prefs.getInt("charisma", jsonBase.baseCharisma)
        maxHp = maxOf(prefs.getInt("maxHp", jsonBase.baseMaxHp), jsonBase.baseMaxHp)
        currentHp = prefs.getInt("currentHp", maxHp)
        tempHp = prefs.getInt("${characterId}_tempHp", 0)
        
        // --- ONE-TIME DATA SYNC / REPAIR v2 (only for legacy characters Athania/Delat) ---
        val syncKeyV2 = "isSyncedWithStatsFiles_2026_03_13_v2"
        val alreadySyncedV2 = prefs.getBoolean(syncKeyV2, false)

        if (!alreadySyncedV2 && (characterId == "Athania" || characterId == "Delat")) {
            val defaultLoot = characterData.defaultLoot
            val defaultLootNames = defaultLoot.map { it.name }
            val otherCharId = if (characterId == "Athania") "Delat" else "Athania"
            val otherDefaults = characterRepository.getCharacter(otherCharId)
            val otherLootNames = otherDefaults.defaultLoot.map { it.name }.filter { it !in defaultLootNames }
            val otherTraitNames = otherDefaults.defaultTraits.map { it.name }

            var lootChanged = false
            // Add/Update default items
            defaultLoot.forEach { default ->
                val existing = customLoot.find { it.name == default.name }
                if (existing == null) {
                    customLoot.add(default)
                    lootChanged = true
                } else if (existing.weight != default.weight || existing.category != default.category) {
                    val idx = customLoot.indexOf(existing)
                    customLoot[idx] = default
                    lootChanged = true
                }
            }
            
            // REMOVE items from the OTHER character or legacy names
            val itemsToRemove = listOf("Köcher", "Kleine Onyxstatue (Fokus)", "Flöte (alt)") + otherLootNames
            val finalLoot = customLoot.filter { it.name !in itemsToRemove || it.name in defaultLootNames }
            if (finalLoot.size != customLoot.size) {
                customLoot.clear()
                customLoot.addAll(finalLoot)
                lootChanged = true
            }
            if (lootChanged) saveLoot()

            var traitsChanged = false
            val defaultTraits = characterData.defaultTraits
            val defaultTraitNames = defaultTraits.map { it.name }
            defaultTraits.forEach { default ->
                val existing = customTraits.find { it.name == default.name }
                if (existing == null) {
                    customTraits.add(default)
                    traitsChanged = true
                } else if (existing.desc != default.desc || existing.maxUses != default.maxUses) {
                    val idx = customTraits.indexOf(existing)
                    customTraits[idx] = default
                    traitsChanged = true
                }
            }
            
            // REMOVE traits from the OTHER character or legacy
            // Wir entfernen alte Bezeichnungen, erlauben aber Feenfeuer, Dunkelheit, Wunden heilen ("good" spells) explizit
            val legacyTraitsToRemove = listOf("Gute Beere", "Pakt der Klinge (alt)") + otherTraitNames
            val finalTraits = customTraits.filter { it.name !in legacyTraitsToRemove || it.name in defaultTraitNames }
            if (finalTraits.size != customTraits.size) {
                customTraits.clear()
                customTraits.addAll(finalTraits)
                traitsChanged = true
            }
            if (traitsChanged) saveTraits()
            
            // Specialized Base Stats Correction (Force stats.md values)
            strength = characterData.baseStrength
            dexterity = characterData.baseDexterity
            constitution = characterData.baseConstitution
            intelligence = characterData.baseIntelligence
            wisdom = characterData.baseWisdom
            charisma = characterData.baseCharisma
            maxHp = characterData.baseMaxHp
            if (currentHp > maxHp) currentHp = maxHp

            // Specialized Fixes
            if (characterId == "Athania") {
                if (totalArrows < 26) totalArrows = 26
                if (coinsKM < 20) coinsKM = 20
                if (coinsSM < 1) coinsSM = 1
                if (coinsGM < 44) coinsGM = 44
                
                // Repariere versehentlich durch den vorherigen Bug gelöschte Traits
                val athaniaMissingTraits = listOf(
                    TraitItem("Feenfeuer", "1x pro Lange Rast kostenlos wirkbar.", grantedSpellId = "Feenfeuer", maxUses = 1, currentUses = 1, minLevel = 3),
                    TraitItem("Dunkelheit", "1x pro Lange Rast kostenlos wirkbar.", grantedSpellId = "Dunkelheit", maxUses = 1, currentUses = 1, minLevel = 5),
                    TraitItem("Elfen-Abstammungslinie (Drow)", "Du kennst Tanzende Lichter, Feenfeuer und Dunkelheit."),
                    TraitItem("Eingeweihter der Magie (Segnen)", "Du kannst Segnen 1x pro Lange Rast kostenlos wirken.", grantedSpellId = "Segnen", maxUses = 1, currentUses = 1),
                    TraitItem("Eingeweihter der Magie (Magierrüstung)", "Du kannst Magierrüstung 1x pro Lange Rast kostenlos wirken.", grantedSpellId = "Magierrüstung", maxUses = 1, currentUses = 1)
                )
                
                var repaired = false
                athaniaMissingTraits.forEach { missing ->
                    if (customTraits.none { it.name == missing.name }) {
                        customTraits.add(missing)
                        repaired = true
                    }
                }
                if (repaired) saveTraits()
            }
            // Migrate minLevel for Drow lineage traits (Feenfeuer lvl 3, Dunkelheit lvl 5)
            val minLevelMigKey = "minLevel_drow_migration_2026_04_10"
            if (!prefs.getBoolean(minLevelMigKey, false) && characterId == "Athania") {
                var migrated = false
                val feenIdx = customTraits.indexOfFirst { it.grantedSpellId == "Feenfeuer" }
                if (feenIdx != -1 && customTraits[feenIdx].minLevel != 3) {
                    customTraits[feenIdx] = customTraits[feenIdx].copy(minLevel = 3)
                    migrated = true
                }
                val dunIdx = customTraits.indexOfFirst { it.grantedSpellId == "Dunkelheit" }
                if (dunIdx != -1 && customTraits[dunIdx].minLevel != 5) {
                    customTraits[dunIdx] = customTraits[dunIdx].copy(minLevel = 5)
                    migrated = true
                }
                if (migrated) saveTraits()
                prefs.edit { putBoolean(minLevelMigKey, true) }
            }
            if (characterId == "Delat") {
                // Override Delat's mistakenly high SM to correct stats.json standard
                if (coinsSM > 9 || coinsSM < 9) coinsSM = 9
                if (coinsGM < 72) coinsGM = 72
                if (tempHp < 12) tempHp = 12
            }
            
            prefs.edit { putBoolean(syncKeyV2, true) }
            saveBaseStats()
        }
        // --- END SYNC ---

        applySyncV3()

        val syncKeyV4 = "isSyncedWithStatsFiles_2026_03_27_wealth"
        if (!prefs.getBoolean(syncKeyV4, false) && (characterId == "Athania" || characterId == "Delat")) {
            if (characterId == "Athania") {
                coinsKM = 20
                coinsSM = 1
                coinsGM = 44
            } else if (characterId == "Delat") {
                coinsKM = 0
                coinsSM = 9
                coinsGM = 72
            }
            prefs.edit {
                putInt("coinsKM", coinsKM)
                putInt("coinsSM", coinsSM)
                putInt("coinsGM", coinsGM)
                putBoolean(syncKeyV4, true)
            }
        }

        hitDice = prefs.getInt("hitDice", characterData.baseHitDice)
        
        deathSaveSuccesses = prefs.getInt("deathSaveSuccesses", 0)
        deathSaveFailures = prefs.getInt("deathSaveFailures", 0)
        
        spellSlotsLevel1 = prefs.getInt("spellSlotsLevel1", characterData.baseSpellSlotsLevel1)
        spellSlotsLevel2 = prefs.getInt("spellSlotsLevel2", characterData.baseSpellSlotsLevel2)
        spellSlotsLevel3 = prefs.getInt("spellSlotsLevel3", characterData.baseSpellSlotsLevel3)
        huntersMarkFreeUses = prefs.getInt("huntersMarkFreeUses", 2)
        
        freeAmuletSpellUsed = prefs.getBoolean("freeAmuletSpellUsed", false)
        freeFaerieFireUsed = prefs.getBoolean("freeFaerieFireUsed", false)
        freeDarknessUsed = prefs.getBoolean("freeDarknessUsed", false)
        freeDruidSpellUsed = prefs.getBoolean("freeDruidSpellUsed", false)
        freeMageArmorUsed = prefs.getBoolean("freeMageArmorUsed", false)
        freeBlessUsed = prefs.getBoolean("freeBlessUsed", false)
        freeMistyStepUsed = prefs.getBoolean("freeMistyStepUsed", false)
        
        water = prefs.getFloat("water", 2.0f)
        rations = prefs.getInt("rations", 10)
        goodberries = prefs.getInt("goodberries", 0)
        
        coinsKM = prefs.getInt("coinsKM", 0)
        coinsSM = prefs.getInt("coinsSM", 0)
        coinsEM = prefs.getInt("coinsEM", 0)
        coinsGM = prefs.getInt("coinsGM", 0)
        coinsPM = prefs.getInt("coinsPM", 0)
        
        totalArrows = prefs.getInt("totalArrows", 20)
        shotArrows = prefs.getInt("shotArrows", 0)
        
        val defaultTactic = if (characterData.charClass == CharacterClass.RANGER) "1. Zeichen des Jägers wirken (Bonusaktion)\n2. Mit Langbogen angreifen" else ""
        standardTactic = prefs.getString("standardTactic", defaultTactic) ?: ""
        
        suppressInventoryFirestoreSync = true
        loadLoot()
        suppressInventoryFirestoreSync = false
        loadTraits()
        loadBooks()
        loadFaqs()
        loadSpells()
        loadCompanion()
        resetChat()
        _activeCharacterIdFlow.value = characterId
    }

    fun applyFalseLife() {
        tempHp = 12
        prefs.edit { putInt("tempHp", tempHp) }
    }

    // --- BÜCHER & TAKTIK ---

    private fun saveGeneralBookEntries() {
        prefs.edit { putString("generalBookEntries", gson.toJson(generalBookEntries)) }
    }

    private fun loadBooks() {
        val generalJson = prefs.getString("generalBookEntries", "[]") ?: "[]"
        try {
            val type = object : TypeToken<List<BookEntry>>() {}.type
            generalBookEntries.clear()
            generalBookEntries.addAll(gson.fromJson(generalJson, type))

            // Migration: alte Einzelnotiz
            if (generalBookEntries.isEmpty()) {
                val oldGeneral = prefs.getString("generalNotes", "") ?: ""
                if (oldGeneral.isNotBlank()) {
                    addGeneralBookEntry(oldGeneral)
                    prefs.edit { remove("generalNotes") }
                }
            }

            // Migration: Groll-Buch → Notizbuch mit isGrudge=true
            val grudgeJson = prefs.getString("grudgeBookEntries", "[]") ?: "[]"
            val grudgeList: List<BookEntry> = try { gson.fromJson(grudgeJson, type) } catch (e: Exception) { emptyList() }
            if (grudgeList.isNotEmpty()) {
                generalBookEntries.addAll(grudgeList.map { it.copy(isGrudge = true) })
                generalBookEntries.sortByDescending { it.timestamp }
                saveGeneralBookEntries()
                prefs.edit { remove("grudgeBookEntries") }
            }
        } catch (e: Exception) {
            Log.w("CharacterVM", "Failed to load book entries from prefs", e)
        }
    }

    fun addGeneralBookEntry(text: String, isPublic: Boolean = false, isGrudge: Boolean = false, grudgeTargets: List<String> = emptyList()) {
        if (text.isNotBlank() && !isPublic) {
            val entry = BookEntry(text = text.trim(), isPublic = false, isGrudge = isGrudge, grudgeTargets = grudgeTargets)
            generalBookEntries.add(0, entry)
            saveGeneralBookEntries()
        }
    }

    fun updateGeneralBookEntry(id: String, newText: String, isPublic: Boolean = false, isGrudge: Boolean = false, grudgeTargets: List<String> = emptyList()) {
        if (newText.isNotBlank() && !isPublic) {
            val index = generalBookEntries.indexOfFirst { it.id == id }
            if (index != -1) {
                generalBookEntries[index] = generalBookEntries[index].copy(
                    text = newText.trim(),
                    isGrudge = isGrudge,
                    grudgeTargets = grudgeTargets
                )
                saveGeneralBookEntries()
            }
        }
    }

    fun deleteGeneralBookEntry(id: String) {
        val index = generalBookEntries.indexOfFirst { it.id == id }
        if (index != -1) {
            generalBookEntries.removeAt(index)
            saveGeneralBookEntries()
        }
    }

    var standardTactic by mutableStateOf(prefs.getString("standardTactic", "1. Zeichen des Jägers wirken (Bonusaktion)\n2. Mit Langbogen angreifen") ?: "")
        private set
    fun updateStandardTactic(text: String) {
        standardTactic = text
        prefs.edit { putString("standardTactic", standardTactic) }
    }

    fun removeCustomLoot(itemName: String) {
        val index = customLoot.indexOfFirst { it.name.equals(itemName, ignoreCase = true) }
        if (index != -1) {
            val existingItem = customLoot[index]
            if (existingItem.amount > 1) {
                customLoot[index] = existingItem.copy(amount = existingItem.amount - 1)
            } else {
                customLoot.removeAt(index)
            }
            saveLoot()
        }
    }

    var freeAmuletSpellUsed by mutableStateOf(prefs.getBoolean("freeAmuletSpellUsed", false))
        private set

    fun useFreeAmuletSpell() {
        if (!freeAmuletSpellUsed) {
            freeAmuletSpellUsed = true
            prefs.edit { putBoolean("freeAmuletSpellUsed", true) }
        }
    }

    var freeFaerieFireUsed by mutableStateOf(prefs.getBoolean("freeFaerieFireUsed", false))
        private set

    fun useFreeFaerieFire() {
        if (!freeFaerieFireUsed) {
            freeFaerieFireUsed = true
            prefs.edit { putBoolean("freeFaerieFireUsed", true) }
        }
    }

    var freeDarknessUsed by mutableStateOf(prefs.getBoolean("freeDarknessUsed", false))
        private set

    fun useFreeDarkness() {
        if (!freeDarknessUsed) {
            freeDarknessUsed = true
            prefs.edit { putBoolean("freeDarknessUsed", true) }
        }
    }

    var freeDruidSpellUsed by mutableStateOf(prefs.getBoolean("freeDruidSpellUsed", false))
        private set

    fun useFreeDruidSpell() {
        if (!freeDruidSpellUsed) {
            freeDruidSpellUsed = true
            prefs.edit { putBoolean("freeDruidSpellUsed", true) }
        }
    }

    var freeMageArmorUsed by mutableStateOf(prefs.getBoolean("freeMageArmorUsed", false))
        private set

    fun useFreeMageArmor() {
        if (!freeMageArmorUsed) {
            freeMageArmorUsed = true
            prefs.edit { putBoolean("freeMageArmorUsed", true) }
        }
    }

    var freeBlessUsed by mutableStateOf(prefs.getBoolean("freeBlessUsed", false))
        private set

    fun useFreeBless() {
        if (!freeBlessUsed) {
            freeBlessUsed = true
            prefs.edit { putBoolean("freeBlessUsed", true) }
        }
    }

    var freeMistyStepUsed by mutableStateOf(prefs.getBoolean("freeMistyStepUsed", false))
        private set

    fun useFreeMistyStep() {
        if (!freeMistyStepUsed) {
            freeMistyStepUsed = true
            prefs.edit { putBoolean("freeMistyStepUsed", true) }
        }
    }

    fun takeShortRest(hitDiceSpent: Int, rolledValue: Int) {
        if (hitDiceSpent <= hitDice && currentHp < maxHp) {
            hitDice -= hitDiceSpent
            val healAmount = rolledValue + (conMod * hitDiceSpent)
            currentHp = (currentHp + healAmount).coerceAtMost(maxHp)

            prefs.edit {
                putInt("hitDice", hitDice)
                putInt("currentHp", currentHp)
            }
        }
        
        // Paktmagie-Regeneration für Hexenmeister
        if (characterData.charClass == CharacterClass.WARLOCK) {
            spellSlotsLevel1 = getMaxSpellSlots(level, 1)
            spellSlotsLevel2 = getMaxSpellSlots(level, 2)
            spellSlotsLevel3 = getMaxSpellSlots(level, 3)
            spellSlotsLevel4 = getMaxSpellSlots(level, 4)
            spellSlotsLevel5 = getMaxSpellSlots(level, 5)
            
            prefs.edit { 
                putInt("spellSlotsLevel1", spellSlotsLevel1)
                putInt("spellSlotsLevel2", spellSlotsLevel2)
                putInt("spellSlotsLevel3", spellSlotsLevel3)
                putInt("spellSlotsLevel4", spellSlotsLevel4)
                putInt("spellSlotsLevel5", spellSlotsLevel5)
            }
        }
        
        // Traits mit Reset bei Kurzer Rast zurücksetzen
        var traitsChanged = false
        customTraits.forEachIndexed { index, trait ->
            if (trait.resetOnShortRest && trait.currentUses < trait.maxUses) {
                customTraits[index] = trait.copy(currentUses = trait.maxUses)
                traitsChanged = true
            }
        }
        if (traitsChanged) saveTraits()
    }

    var showRestWarningDialog by mutableStateOf(false)
        private set

    fun dismissRestWarningDialog() {
        showRestWarningDialog = false
    }

    fun attemptLongRest() {
        if (water < 0.5f || rations < 1) {
            showRestWarningDialog = true
        } else {
            forceLongRest(consumeResources = true)
        }
    }

    fun forceLongRestWithoutResources() {
        showRestWarningDialog = false
        forceLongRest(consumeResources = false)
    }

    private fun forceLongRest(consumeResources: Boolean) {
        currentHp = maxHp
        val recoveredHitDice = (level / 2).coerceAtLeast(1)
        hitDice = (hitDice + recoveredHitDice).coerceAtMost(level)

        spellSlotsLevel1 = getMaxSpellSlots(level, 1)
        spellSlotsLevel2 = getMaxSpellSlots(level, 2)
        spellSlotsLevel3 = getMaxSpellSlots(level, 3)
        spellSlotsLevel4 = getMaxSpellSlots(level, 4)
        spellSlotsLevel5 = getMaxSpellSlots(level, 5)

        huntersMarkFreeUses = 2
        freeAmuletSpellUsed = false
        freeFaerieFireUsed = false
        freeDarknessUsed = false
        freeDruidSpellUsed = false
        freeMageArmorUsed = false
        freeBlessUsed = false
        freeMistyStepUsed = false
        goodberries = 0
        geminiUsesToday = 0
        
        // Alle Traits bei langer Rast zurücksetzen
        customTraits.forEachIndexed { index, trait ->
            if (trait.currentUses < trait.maxUses) {
                customTraits[index] = trait.copy(currentUses = trait.maxUses)
            }
        }
        
        // Todeswürfe bei langer Rast zurücksetzen
        updateDeathSaves(0, 0)

        // Begleiter bei langer Rast vollständig regenerieren (HP auf Max, Tod aufheben)
        reviveCompanion()
        saveTraits()
        
        if (consumeResources) {
            changeWater(-0.5f)
            changeRations(-1)
        }

        prefs.edit {
            putInt("currentHp", currentHp)
            putInt("hitDice", hitDice)
            putInt("spellSlotsLevel1", spellSlotsLevel1)
            putInt("spellSlotsLevel2", spellSlotsLevel2)
            putInt("spellSlotsLevel3", spellSlotsLevel3)
            putInt("spellSlotsLevel4", spellSlotsLevel4)
            putInt("spellSlotsLevel5", spellSlotsLevel5)
            putInt("huntersMarkFreeUses", huntersMarkFreeUses)
            putBoolean("freeAmuletSpellUsed", freeAmuletSpellUsed)
            putBoolean("freeFaerieFireUsed", freeFaerieFireUsed)
            putBoolean("freeDarknessUsed", freeDarknessUsed)
            putBoolean("freeDruidSpellUsed", freeDruidSpellUsed)
            putBoolean("freeMageArmorUsed", freeMageArmorUsed)
            putBoolean("freeBlessUsed", freeBlessUsed)
            putBoolean("freeMistyStepUsed", freeMistyStepUsed)
            putInt("goodberries", goodberries)
            putInt("geminiUsesToday", geminiUsesToday)
        }
    }

    fun loadCompanion() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                if (characterData.charClass == CharacterClass.RANGER) {
                    val jsonString = context.assets.open("Rules/urtier.json").bufferedReader().use { it.readText() }
                    val fileDto: UrtierFileDto = gson.fromJson(jsonString, UrtierFileDto::class.java)
                    val targetName = when(activeBeastType) {
                        BeastType.LAND -> "Urtier des Landes"
                        BeastType.SKY -> "Urtier des Himmels"
                        BeastType.SEA -> "Urtier des Meeres"
                    }
                    companionData = fileDto.urtiere.find { it.name == targetName }
                } else if (characterData.charClass == CharacterClass.WARLOCK) {
                    val jsonString = context.assets.open("Rules/vertrauter.json").bufferedReader().use { it.readText() }
                    companionData = gson.fromJson(jsonString, CompanionDto::class.java)
                } else {
                    companionData = null
                }
            } catch (e: Exception) {
                Log.e("CharacterVM", "Error loading companion", e)
                companionData = null
            }
        }
    }

    val capyMaxHp: Int get() {
        val base = if (activeBeastType == BeastType.SKY || activeBeastType == BeastType.SEA) 4 else 5
        val mult = if (activeBeastType == BeastType.SKY || activeBeastType == BeastType.SEA) 4 else 5
        return if (characterData.charClass == CharacterClass.RANGER) base + (mult * level) else 24 // Sphinx has fixed 24
    }
    var capyCurrentHp by mutableIntStateOf(prefs.getInt("capyCurrentHp_${characterData.id}_${prefs.getString("activeBeastType", BeastType.SKY.name) ?: BeastType.SKY.name}", prefs.getInt("capyCurrentHp_${characterData.id}", 20)))
        private set

    var companionIsDead by mutableStateOf(prefs.getBoolean("companionIsDead_${characterData.id}_${prefs.getString("activeBeastType", BeastType.SKY.name) ?: BeastType.SKY.name}", prefs.getBoolean("companionIsDead_${characterData.id}", false)))

    fun toggleBeastType(type: BeastType) {
        // Aktuellen Stand sichern
        prefs.edit {
            putInt("capyCurrentHp_${characterData.id}_${activeBeastType.name}", capyCurrentHp)
            putBoolean("companionIsDead_${characterData.id}_${activeBeastType.name}", companionIsDead)
        }
        
        activeBeastType = type
        loadCompanion()
        
        // Neuen Stand laden
        capyCurrentHp = prefs.getInt("capyCurrentHp_${characterData.id}_${activeBeastType.name}", capyMaxHp)
        companionIsDead = prefs.getBoolean("companionIsDead_${characterData.id}_${activeBeastType.name}", false)
        
        if (capyCurrentHp > capyMaxHp) capyCurrentHp = capyMaxHp
        
        prefs.edit {
            putString("activeBeastType", activeBeastType.name)
            putInt("capyCurrentHp_${characterData.id}_${activeBeastType.name}", capyCurrentHp)
            putBoolean("companionIsDead_${characterData.id}_${activeBeastType.name}", companionIsDead)
        }
    }

    fun takeCapyDamage(amount: Int) {
        capyCurrentHp = (capyCurrentHp - amount).coerceAtLeast(0)
        if (capyCurrentHp == 0) companionIsDead = true
        prefs.edit { 
            putInt("capyCurrentHp_${characterData.id}_${activeBeastType.name}", capyCurrentHp)
            putBoolean("companionIsDead_${characterData.id}_${activeBeastType.name}", companionIsDead)
        }
    }

    fun healCapy(amount: Int) {
        capyCurrentHp = (capyCurrentHp + amount).coerceAtMost(capyMaxHp)
        if (capyCurrentHp > 0) companionIsDead = false
        prefs.edit { 
            putInt("capyCurrentHp_${characterData.id}_${activeBeastType.name}", capyCurrentHp)
            putBoolean("companionIsDead_${characterData.id}_${activeBeastType.name}", companionIsDead)
        }
    }

    fun reviveCompanion() {
        capyCurrentHp = capyMaxHp
        companionIsDead = false
        prefs.edit { 
            putInt("capyCurrentHp_${characterData.id}_${activeBeastType.name}", capyCurrentHp)
            putBoolean("companionIsDead_${characterData.id}_${activeBeastType.name}", companionIsDead)
        }
    }

    val capyAc: Int get() = if (characterData.charClass == CharacterClass.RANGER) 13 + wisMod else 13
    val capyAttackBonus: String get() = if (characterData.charClass == CharacterClass.RANGER) "+$spellAttackBonus" else "+5"
    val capyDamage: String get() = if (characterData.charClass == CharacterClass.RANGER) {
        if (activeBeastType == BeastType.SKY) "1W4 + 3 + $wisMod Hieb" else if(activeBeastType == BeastType.SEA) "1W6 + 2 + $wisMod Stich" else "1W8 + 2 + $wisMod Hieb"
    } else "1W4+3 Hieb + 2W6 Gleißend"
    
    val capySpeed: String get() = companionData?.bewegungsrate?.entries?.joinToString(", ") { "${it.key.replaceFirstChar { c -> c.uppercase() }}: ${it.value}" } ?: ""
    val capySpecial: String get() = companionData?.merkmale?.joinToString("\n") { "${it.name}: ${it.beschreibung}" } ?: ""

    // --- HILFE: CHAT & FAQ ---

    var currentUsedModel by mutableStateOf("Bereit")
        private set
    var geminiUsesToday by mutableIntStateOf(prefs.getInt("geminiUsesToday", 0))
        private set
    val geminiMax = 20

    private val systemPrompt: String get() = """
        Du bist unser D&D 2024 Regel-Assistent. Dein Ziel ist es, Fragen basierend auf unseren Hausregeln (Handbuch/Zauberbuch) und dem Charakterblatt von ${characterData.name} zu beantworten.
        
        FORMATRICHTLINIE (SEHR WICHTIG):
        Antworte AUSSCHLIESSLICH im JSON-Format. Verwende exakt diese Schlüsselstruktur und erzeuge keinen Text außerhalb der JSON-Klammern:
        {
          "lokale_antwort": "Deine Antwort NUR basierend auf den bereitgestellten Handbüchern/Stats. Wenn nichts gefunden, schreibe 'Keine spezifischen Informationen gefunden.'",
          "externe_antwort": "Deine Antwort basierend auf deinem allgemeinen Wissen über D&D 2024. Gehe auf die Klasse und das Volk des Charakters ein, falls relevant.",
          "kapitel_link": "NUR der exakte Name eines HANDBUCH-Kapitels aus den '--- Quelle: ... ---' Markierungen (z.B. '3. Klassen', '7. Kampf'). Erlaubte Werte: '1. Gameplay', '2. Völker', '3. Klassen', '4. Herkünfte', '5. Talente', '6. Ausrüstung', '7. Kampf', '8. Zauber', 'Zauberbuch Übersicht'. WICHTIG: Wenn die Antwort aus dem CHARAKTERBLATT kommt (Stats, Begleiter/Capys, Inventar, Zauberplätze, Tagebuch etc.) und NICHT aus einem Handbuch-Kapitel, setze den Wert auf null!",
          "suchbegriff": "Ein kurzes Stichwort (1-2 Worte) aus dem Kapitel, das exakt zu deiner Antwort passt, um im UI genau zu dieser Regel zu scrollen (z.B. 'Zaubertricks' oder 'Kampfstile'). Nur setzen wenn kapitel_link gesetzt ist.",
          "faq_titel": "Ein knackiges, kurzes Schlagwort (max 3 Worte), das diese Antwort zusammenfasst (z.B. 'Initiative', 'Paktmagie', 'Rüstungsklasse'), falls der User die Info ins FAQ aufnehmen will. IMMER setzen!"
        }
    """.trimIndent()

private val model25Flash = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val model25FlashLite = GenerativeModel(
        modelName = "gemini-2.5-flash-lite-preview",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private var activeChatSession = model25Flash.startChat()

    fun loadFaqs() {
        val faqKey = "savedFaqs_${characterData.name}"
        val faqString = prefs.getString(faqKey, "") ?: ""
        if (faqString.isNotEmpty()) {
            if (faqString.startsWith("[")) {
                // Es ist sehr wahrscheinlich ein JSON-String
                try {
                    val type = object : TypeToken<List<FaqItem>>() {}.type
                    val items: List<FaqItem> = gson.fromJson(faqString, type)
                    faqList.clear()
                    faqList.addAll(items)
                } catch (e: Exception) {
                    Log.w("CharacterVM", "Failed to parse FAQ JSON", e)
                    faqList.clear()
                }
            } else {
                // Fallback für alte Speicherstände
                val items = faqString.split("||").mapNotNull {
                    val parts = it.split("|:|")
                    if (parts.size == 2) FaqItem(parts[0], parts[1]) else null
                }
                faqList.clear()
                faqList.addAll(items)
            }
        } else {
            faqList.clear()
        }
    }

    private fun saveFaqs() {
        val faqKey = "savedFaqs_${characterData.name}"
        val json = gson.toJson(faqList)
        prefs.edit { putString(faqKey, json) }
    }

    // --- SPELBOOK (ZAUBERBUCH) ---

    // Room Search Flows

    private fun saveSpells() {
        val json = gson.toJson(allSpells)
        prefs.edit { putString("savedSpells", json) }
    }



    private fun loadGlobalFeatures() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val fileName = "Rules/merkmale.json"
                val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
                val featureListDto: FeatureListDto = gson.fromJson(jsonString, FeatureListDto::class.java)
                
                globalFeatures.clear()
                globalFeatures.addAll(featureListDto.features.map { it.toFeature() })
            } catch (e: Exception) {
                Log.e("CharacterVM", "Error loading features", e)
            }
        }
    }

    fun getAvailableFeatures(): List<Feature> {
        val charClassEn = characterData.charClass.name // e.g. "RANGER"
        val charClassDe = when(charClassEn) {
            "RANGER" -> "Waldläufer"
            "WARLOCK" -> "Hexenmeister"
            else -> charClassEn
        }
        val charRace = characterData.race.lowercase()
        // Simplify race (e.g. "Elf-Drow" or "Elf (Drow)" -> "elf")
        val simpleRace = charRace.split("-", "(", " ").first()

        return globalFeatures.filter { feature ->
            val classMatch = feature.classReq.isEmpty() || feature.classReq.any { it.equals(charClassDe, ignoreCase = true) || it.equals(charClassEn, ignoreCase = true) }
            val raceMatch = feature.raceReq.isEmpty() || feature.raceReq.any { req ->
                val reqLow = req.lowercase()
                charRace == reqLow || simpleRace == reqLow || charRace.startsWith(reqLow)
            }
            val levelMatch = level >= feature.levelReq
            
            classMatch && raceMatch && levelMatch
        }
    }

    private fun loadSpells() {
        val spellString = prefs.getString("savedSpells", "") ?: ""
        if (spellString.isNotEmpty()) {
            try {
                val type = object : TypeToken<List<Spell>>() {}.type
                val items: List<Spell> = gson.fromJson(spellString, type)
                allSpells.clear()
                @Suppress("SENSELESS_COMPARISON")
                val safeItems = items.map { spell ->
                    if (spell.classes == null) spell.copy(classes = emptyList()) else spell
                }
                allSpells.addAll(safeItems)

                // --- ONE-TIME SPELL SYNC v2 (Stand 13.03.2026 - Strict Separation) ---
                val syncKeyV2 = "isSyncedWithSpells_2026_03_13_v2"
                val alreadySyncedV2 = prefs.getBoolean(syncKeyV2, false)

                if (!alreadySyncedV2 && (characterData.id == "Athania" || characterData.id == "Delat")) {
                    val defaultSpells = getDefaultSpells()
                    val defaultNames = defaultSpells.map { it.name }
                    val otherSpells = if (characterData.charClass == CharacterClass.RANGER) getDelatDefaultSpells() else getAthaniaDefaultSpells()
                    val otherSpellNames = otherSpells.map { it.name }
                    
                    var changed = false
                    defaultSpells.forEach { default ->
                        val existing = allSpells.find { it.name == default.name }
                        if (existing == null) {
                            allSpells.add(default)
                            changed = true
                        } else if (existing.description != default.description || existing.level != default.level) {
                            val idx = allSpells.indexOf(existing)
                            allSpells[idx] = default
                            changed = true
                        }
                    }
                    
                    // REMOVE spells from the OTHER character or legacy
                    val legacyNames = listOf("Heilendes Wort", "Gute Beere")
                    val filtered = allSpells.filter { (it.name !in legacyNames && it.name !in otherSpellNames) || it.name in defaultNames }
                    if (filtered.size != allSpells.size) {
                        allSpells.clear()
                        allSpells.addAll(filtered)
                        changed = true
                    }

                    if (changed) saveSpells()
                    prefs.edit { putBoolean(syncKeyV2, true) }
                }
                // --- END SYNC ---

            } catch (e: Exception) {
                Log.w("CharacterVM", "Fehler beim Spell-Sync", e)
            }
        } else {
            // Wenn leer, Standard-Zauber laden
            allSpells.addAll(getDefaultSpells())
            saveSpells()
        }
    }

    private fun getDefaultSpells(): List<Spell> {
        return if (characterData.charClass == CharacterClass.WARLOCK) getDelatDefaultSpells() else getAthaniaDefaultSpells()
    }

    private fun getDelatDefaultSpells(): List<Spell> {
        return listOf(
            Spell(name = "Schauerlicher Strahl", level = 0, castingTime = "1 Aktion", range = "36 m", duration = "Sofort", componentsV = true, componentsS = true, description = "1d10 Kraftschaden.", isPrepared = true),
            Spell(name = "Totenläuten", level = 0, castingTime = "1 Aktion", range = "18 m", duration = "Sofort", componentsV = true, componentsS = true, description = "1W8/1W12 Nekrotisch (WIS RW).", isPrepared = true),
            Spell(name = "Einfache Illusion", level = 0, castingTime = "1 Aktion", range = "9 m", duration = "1 Min.", componentsV = true, componentsS = true, componentsM = true, description = "Erzeugt ein Bild oder Geräusch.", isPrepared = true),
            Spell(name = "Donnerschlag", level = 0, castingTime = "1 Aktion", range = "Selbst", duration = "Sofort", componentsV = true, description = "1W6 Donnerschaden (KON RW).", isPrepared = true),
            Spell(name = "Paktwaffe", level = 0, castingTime = "1 Bonusaktion", range = "Selbst", duration = "Bis zur Entlassung", description = "Beschwört eine Paktwaffe.", isPrepared = true),
            Spell(name = "Magierhand", level = 0, castingTime = "1 Aktion", range = "9 m", duration = "1 Min.", componentsV = true, componentsS = true, description = "Eine schwebende Spektralhand.", isPrepared = true),
            
            Spell(name = "Verwünschen", level = 1, castingTime = "1 Bonusaktion", range = "27 m", duration = "1 Std.", componentsV = true, componentsS = true, componentsM = true, description = "Extra 1W6 Schaden bei Treffern.", isPrepared = true),
            Spell(name = "Magie Entdecken", level = 1, castingTime = "1 Aktion (Ritual)", range = "Selbst", duration = "Konzentration, 10 Min.", componentsV = true, componentsS = true, description = "Spürt Magie in 9m.", isPrepared = true),
            Spell(name = "Falsches Leben", level = 1, castingTime = "1 Aktion", range = "Selbst", duration = "1 Std.", componentsV = true, componentsS = true, componentsM = true, description = "2W4 + 4 temporäre HP.", isPrepared = true),
            Spell(name = "Magierrüstung", level = 1, castingTime = "1 Aktion", range = "Berührung", duration = "8 Std.", componentsV = true, componentsS = true, componentsM = true, description = "RK wird 13 + GES.", isPrepared = true),
            Spell(name = "Dissonantes Flüstern", level = 1, castingTime = "1 Aktion", range = "18 m", duration = "Sofort", componentsV = true, description = "3W6 Psychisch + Flucht (WIS RW).", isPrepared = true),
            Spell(name = "Tashas fürchterlicher Lachanfall", level = 1, castingTime = "1 Aktion", range = "9 m", duration = "Konzentration, 1 Min.", componentsV = true, componentsS = true, componentsM = true, description = "Ziel bricht in Lachen aus (WEI RW).", isPrepared = true),
            Spell(name = "Segnen", level = 1, castingTime = "1 Aktion", range = "9 m", duration = "Konzentration, 1 Min.", componentsV = true, componentsS = true, componentsM = true, description = "+1W4 auf Angriffs/Rettungswürfe.", isPrepared = true),

            Spell(name = "Gedanken Wahrnehmen", level = 2, castingTime = "1 Aktion", range = "Selbst", duration = "Konzentration, 1 Min.", componentsV = true, componentsS = true, componentsM = true, description = "Liest Oberflächengedanken.", isPrepared = true),
            Spell(name = "Macht der Vorstellungskraft", level = 2, castingTime = "1 Aktion", range = "18 m", duration = "Konzentration, 1 Min.", componentsV = true, componentsS = true, componentsM = true, description = "Erschafft eine mentale Illusion (INT RW).", isPrepared = true),
            Spell(name = "Nebelschritt", level = 2, castingTime = "1 Bonusaktion", range = "9 m", duration = "Sofort", componentsV = false, componentsS = true, description = "Teleportation.", isPrepared = true),
            Spell(name = "Unsichtbarkeit", level = 2, castingTime = "1 Aktion", range = "Berührung", duration = "Konzentration, 1 Std.", componentsV = true, componentsS = true, componentsM = true, description = "Ziel wird unsichtbar.", isPrepared = true),
            Spell(name = "Einflüsterung", level = 2, castingTime = "1 Aktion", range = "9 m", duration = "Konzentration, 8 Std.", componentsV = true, componentsM = true, description = "Gibt einer Kreatur einen Befehl (WEI RW).", isPrepared = true),
            Spell(name = "Spiegelbilder", level = 2, castingTime = "1 Aktion", range = "Selbst", duration = "1 Std.", componentsV = true, componentsS = true, description = "Erschafft 3 Abbilder.", isPrepared = true)
        )
    }

    fun toggleSpellPrepared(spellId: String) {
        val index = allSpells.indexOfFirst { it.id == spellId }
        if (index != -1) {
            val spell = allSpells[index]
            allSpells[index] = spell.copy(isPrepared = !spell.isPrepared)
            saveSpells()
        }
    }

    fun addNewSpell(spell: Spell) {
        allSpells.add(spell)
        saveSpells()
    }

    fun removeSpell(spellId: String) {
        allSpells.removeAll { it.id == spellId }
        saveSpells()
    }

    private fun getAthaniaDefaultSpells(): List<Spell> {
        return listOf(
            Spell(name = "Tanzende Lichter", level = 0, castingTime = "1 Aktion", range = "36 m", duration = "Konzentration, 1 Min.", description = "Erschafft 4 Lichter.", isPrepared = true),
            Spell(name = "Göttliche Führung", level = 0, castingTime = "1 Aktion", range = "Berührung", duration = "Konzentration, 1 Min.", description = "+1W4 auf Attributswurf.", isPrepared = true),
            Spell(name = "Shillelagh", level = 0, castingTime = "1 Bonusaktion", range = "Berührung", duration = "1 Min.", description = "Waffe nutzt WEI für Angriff/Schaden (1W8).", isPrepared = true),
            Spell(name = "Kalte Hand", level = 0, castingTime = "1 Aktion", range = "Berührung", duration = "Sofort", description = "Nekrotischer Angriff.", isPrepared = true),
            
            Spell(name = "Verstricken", level = 1, castingTime = "1 Aktion", range = "27 m", duration = "Konzentration, 1 Min.", description = "Pflanzen halten Gegner fest (ST RW).", isPrepared = true),
            Spell(name = "Wunden heilen", level = 1, castingTime = "1 Aktion", range = "Berührung", duration = "Sofort", description = "2W8 + WEI Heilung.", isPrepared = true),
            Spell(name = "Gute Beeren", level = 1, castingTime = "1 Aktion", range = "Berührung", duration = "Sofort", description = "10 Beeren, heilen 1 TP.", isPrepared = true),
            Spell(name = "Mit Tieren sprechen", level = 1, castingTime = "1 Aktion (Ritual)", range = "Selbst", duration = "10 Min.", description = "Kommunikation mit Tieren.", isPrepared = true),
            Spell(name = "Zeichen des Jägers", level = 1, castingTime = "1 Bonusaktion", range = "27 m", duration = "Konzentration, 1 Std.", description = "+1W6 Kraftschaden.", isPrepared = true),
            Spell(name = "Nebelwolke", level = 1, castingTime = "1 Aktion", range = "36 m", duration = "Konzentration, 1 Std.", description = "Erschafft Nebel.", isPrepared = true),
            Spell(name = "Feenfeuer", level = 1, castingTime = "1 Aktion", range = "18 m", duration = "Konzentration, 1 Min.", description = "Vorteil auf Angriffe gegen Ziele (GES RW).", isPrepared = true),
            Spell(name = "Lange Schritte", level = 1, castingTime = "1 Aktion", range = "Berührung", duration = "1 Std.", description = "+3m Bewegungsrate.", isPrepared = true)
        )
    }

    private fun getCharacterContext(): String {
        val stModStr = if (strMod >= 0) "+$strMod" else "$strMod"
        val geModStr = if (dexMod >= 0) "+$dexMod" else "$dexMod"
        val koModStr = if (conMod >= 0) "+$conMod" else "$conMod"
        val inModStr = if (intMod >= 0) "+$intMod" else "$intMod"
        val weModStr = if (wisMod >= 0) "+$wisMod" else "$wisMod"
        val chModStr = if (chaMod >= 0) "+$chaMod" else "$chaMod"

        val preparedSpells = allSpells.filter { it.isPrepared }.joinToString(", ") { "${it.name} (Lvl ${it.level})" }
        val allKnownSpells = allSpells.joinToString(", ") { "${it.name} (Lvl ${it.level})" }
        val inventoryStr = customLoot.joinToString(", ") { "${it.amount}x ${it.name}" }
        val notes = generalBookEntries.joinToString(" | ") { it.text }
        val grudges = generalBookEntries.filter { it.isGrudge }.joinToString(" | ") { "[Groll gegen ${it.grudgeTargets.joinToString()}] ${it.text}" }
        val traitsStr = customTraits.joinToString(" | ") { "${it.name}: ${it.desc.replace("\n", " ")}" }

        val className = if (characterData.charClass == com.example.dndcompanion.data.CharacterClass.RANGER) "Waldläufer (Beast Master)" else "Warlock (Pakt der Klinge)"
        val race = characterData.race.ifBlank { if (characterData.charClass == CharacterClass.RANGER) "Elf (Waldelf / Feenblut)" else "Mensch" }
        val primaryCastingInfo = if (characterData.charClass == com.example.dndcompanion.data.CharacterClass.RANGER) {
            "Zauberplätze: G1: $spellSlotsLevel1, G2: $spellSlotsLevel2, G3: $spellSlotsLevel3"
        } else {
            val maxSlots = characterData.baseSpellSlotsLevel2
            "Paktmagie: $spellSlotsLevel2/$maxSlots (Level 2 Slots)"
        }

        val baseContext = """
            KONTEXT CHARAKTERBLATT ${characterData.name.uppercase()}:
            Klasse: $className
            Volk: $race
            Level: $level, EP: $currentEP
            HP: $currentHp/$maxHp (Temp HP: $tempHp), Trefferwürfel: $hitDice/$level
            Werte: ST $strength ($stModStr), GE $dexterity ($geModStr), KO $constitution ($koModStr), IN $intelligence ($inModStr), WE $wisdom ($weModStr), CH $charisma ($chModStr)
            Rüssi-Klasse: $currentArmorClass, Initiative: $geModStr
            Waffe: ${currentWeapon.name} (Bonus: +$currentAttackBonus, Schaden: $currentDamage)
            $primaryCastingInfo
            Vorbereitete Zauber: $preparedSpells
            Alle bekannten Zauber: $allKnownSpells
            Merkmale/Fähigkeiten: $traitsStr
            Vorrätig: $water L Wasser, $rations Rationen, $goodberries Beeren, $totalArrows Pfeile
            Geld: $coinsGM GM, $coinsSM SM
            Inventar: $inventoryStr
            Tagebuch: $notes
            Buch des Grolls: $grudges
        """.trimIndent()

        if (characterData.charClass == com.example.dndcompanion.data.CharacterClass.RANGER) {
            val landHp = 5 + 5 * level
            val skyHp = 4 + 4 * level
            val seaHp = 4 + 4 * level
            val beastAc = 13 + proficiencyBonus

            return baseContext + "\n\n" + """
                --- BEGLEITER (alle 3 Urtier-Formen) ---
                Aktuell aktiv: ${activeBeastType.name} (HP: $capyCurrentHp/$capyMaxHp)
                Angriffsbonus (alle): +$spellAttackBonus, Rettungswürfe (alle): +$proficiencyBonus
                
                Urtier des Landes: HP: $landHp/$landHp, AC: $beastAc
                Schaden: 1W8 + $wisMod Hieb, Geschwindigkeit: Laufen 12m, Klettern 12m
                Spezial: Ansturm
                
                Urtier des Himmels: HP: $skyHp/$skyHp, AC: $beastAc
                Schaden: 1W4 + $wisMod Hieb, Geschwindigkeit: Fliegen 18m, Laufen 3m
                Spezial: Vorbeifliegen
                
                Urtier des Meeres: HP: $seaHp/$seaHp, AC: $beastAc
                Schaden: 1W6 + $wisMod Stich, Geschwindigkeit: Schwimmen 18m, Laufen 1.5m
                Spezial: Unter Wasser atmen, Amphibisch
            """.trimIndent()
        }

        return baseContext
    }

    private suspend fun getManualContext(query: String): String {
        return try {
            val context = getApplication<Application>()
            val assets = context.assets
            val sb = StringBuilder()

            // 1. Satzzeichen entfernen, alles in Kleinbuchstaben
            val cleanQuery = query.lowercase().replace(Regex("[^a-zäöüß0-9 ]"), "")
            
            // 2. Füllwörter ignorieren, auch Wortstämme (vereinfacht) berücksichtigen
            val stopWords = setOf("was", "wie", "ist", "ein", "eine", "der", "die", "das", "und", "oder", "kann", "ich", "mich", "mir", "für", "von", "aus", "mit", "sind", "macht", "darf", "wenn", "dann", "wir", "ihr", "sie", "als", "auf", "bei", "bis", "gibt", "es", "mein", "meine", "welche", "welcher", "habe", "tun", "soll", "werden", "können", "muss")
            // Zuweisung auch von Teilbegriffen (z.b. "Zaubertricks" -> "Tricks" oder "Zauber", aber wir suchen einfach als Substring)
            val keywords = cleanQuery.split(" ").filter { it.length > 3 && !stopWords.contains(it) }.toMutableList()
            
            // Spezifische D&D Begriffs-Helfer: Wenn "zaubertricks" gefragt wird, erweitere das auf "tricks" und "zaubertrick", da es in der markdown evtl "Tricks" heißt.
            if ("zaubertricks" in keywords) keywords.add("tricks")
            if ("zaubertrick" in keywords) keywords.add("trick")
            if ("langbogen" in keywords) keywords.add("bogen")

            if (keywords.isEmpty()) return "Keine spezifischen Handbuch-Einträge gefunden."

            // 3. Durchsuche die Room-Datenbank
            val db = AppDatabase.getDatabase(context).rulebookDao()
            val bestParagraphs = mutableListOf<Pair<Int, String>>()
            
            // Führe für jedes Keyword eine Suche aus und bewerte die Ergebnisse
            withContext(Dispatchers.IO) {
                for (kw in keywords) {
                    val searchString = "%$kw%"
                    
                    // Regeln durchsuchen
                    db.searchRules(searchString).first().forEach { rule ->
                        val score = keywords.count { rule.title.lowercase().contains(it) || rule.content.lowercase().contains(it) }
                        if (score > 0) bestParagraphs.add(Pair(score, "--- Quelle: ${rule.category} ---\nTitel: ${rule.title}\nInhalt: ${rule.content}"))
                    }
                    // Waffen durchsuchen
                    db.searchWeapons(searchString).first().forEach { weapon ->
                        val score = keywords.count { weapon.name.lowercase().contains(it) || weapon.category.lowercase().contains(it) || weapon.properties.joinToString().lowercase().contains(it) }
                        if (score > 0) bestParagraphs.add(Pair(score, "--- Quelle: Waffen ---\nName: ${weapon.name}\nSchaden: ${weapon.damage}\nEigenschaften: ${weapon.properties.joinToString()}"))
                    }
                    // Rüstung durchsuchen
                    db.searchArmor(searchString).first().forEach { armor ->
                        val score = keywords.count { armor.name.lowercase().contains(it) || armor.category.lowercase().contains(it) }
                        if (score > 0) bestParagraphs.add(Pair(score, "--- Quelle: Rüstung ---\nName: ${armor.name}\nRK: ${armor.baseAC}"))
                    }
                    // Werkzeuge durchsuchen
                    db.searchTools(searchString).first().forEach { tool ->
                        val score = keywords.count { tool.name.lowercase().contains(it) || tool.category.lowercase().contains(it) }
                        if (score > 0) bestParagraphs.add(Pair(score, "--- Quelle: Werkzeuge ---\nName: ${tool.name}\nKategorie: ${tool.category}\nPreis: ${tool.price}"))
                    }
                    // Völker durchsuchen
                    db.searchSpecies(searchString).first().forEach { species ->
                        val score = keywords.count { species.name.lowercase().contains(it) || species.traits.joinToString { t -> t.name + t.description }.lowercase().contains(it) }
                        if (score > 0) bestParagraphs.add(Pair(score, "--- Quelle: Völker ---\nVolk: ${species.name}\nEigenschaften: ${species.traits.joinToString { t -> "${t.name}: ${t.description}" }}"))
                    }
                    // Klassen durchsuchen
                    db.searchClasses(searchString).first().forEach { cls ->
                        val score = keywords.count { cls.name.lowercase().contains(it) || cls.classFeatures.joinToString { f -> f.name + f.description }.lowercase().contains(it) }
                        if (score > 0) bestParagraphs.add(Pair(score, "--- Quelle: Klassen ---\nKlasse: ${cls.name}\nMerkmale: ${cls.classFeatures.take(5).joinToString { f -> "${f.name}: ${f.description}" }}"))
                    }
                    // Merkmale durchsuchen
                    db.searchFeatures(searchString).first().forEach { feature ->
                        val score = keywords.count { feature.name.lowercase().contains(it) || feature.description.lowercase().contains(it) }
                        if (score > 0) bestParagraphs.add(Pair(score, "--- Quelle: Merkmale/Talente ---\nName: ${feature.name}\nTyp: ${feature.type}\nBeschreibung: ${feature.description}"))
                    }
                }
            }
            
            // 4. Sortiere nach den meisten Treffern und nimm die besten 10 Absätze für mehr Kontext (Duplikate entfernen durch Distinct)
            bestParagraphs.distinctBy { it.second }.sortedByDescending { it.first }.take(15).forEach { 
                sb.append(it.second).append("\n\n")
            }
            
            // 5. Durchsuche zusätzlich das Zauberbuch (JSON Daten via globalSpellbook)
            withContext(Dispatchers.IO) {
                for (kw in keywords) {
                    db.searchSpells("%$kw%").first().forEach { spell ->
                        val descriptionString = "Zauber: ${spell.name} (Grad ${spell.level})\nKlassen: ${spell.classes.joinToString()}\nBeschreibung: ${spell.description}"
                        if (!sb.contains(spell.name)) {
                            sb.append("--- Quelle: ZAUBERBUCH ---\n")
                            sb.append(descriptionString).append("\n\n")
                        }
                    }
                }
            }
            
            // Fallback, falls absolut gar kein Wort aus der Frage im Buch steht
            if (sb.isEmpty()) "Keine spezifischen Handbuch-Einträge gefunden für: ${keywords.joinToString(", ")}" else sb.toString()
            
        } catch (e: Exception) {
            "Fehler beim Laden lokaler Regeln: ${e.message}"
        }
    }

    fun sendMessageToBot(message: String) {
        // Fix: Parameter explizit benennen (text = ..., isUser = ...)
        chatHistory.add(ChatMessage(text = message, isUser = true))
        val loadingIndex = chatHistory.size
        chatHistory.add(ChatMessage(text = "... analysiere Regeln ...", isUser = false))

        viewModelScope.launch {
            try {
                if (geminiUsesToday < geminiMax) {
                    val charContext = getCharacterContext()
                    val manualContext = getManualContext(message)
                    val finalPrompt = "$systemPrompt\n\n$charContext\n\nHANDBUCH-AUSZÜGE:\n$manualContext\n\nFRAGE: $message"

                    try {
                        currentUsedModel = "Gemini 2.5 Flash"
                        val response = activeChatSession.sendMessage(finalPrompt)
                        finalizeResponse(loadingIndex, response.text)
                    } catch (e: Exception) {
                        currentUsedModel = "Gemini 2.5 Flash-Lite (Fallback)"
                        val fallbackSession = model25FlashLite.startChat(history = activeChatSession.history)
                        val response = fallbackSession.sendMessage(finalPrompt)
                        activeChatSession = fallbackSession
                        finalizeResponse(loadingIndex, response.text)
                    }
                } else {
                    throw Exception("Tageslimit für Gemini erreicht ($geminiMax)")
                }
            } catch (e: Exception) {
                android.util.Log.e("GeminiError", "Error during Gemini API call", e)
                val errorMsg = if (e.localizedMessage?.contains("MissingFieldException") == true) {
                    "Das Modell konnte nicht gefunden werden (API-Key/Quota)."
                } else {
                    e.localizedMessage ?: "Unbekannter Fehler"
                }
                
                // Wir zwingen auch den Fehler in die Split-Ansicht, damit du ihn sehen kannst!
                chatHistory[loadingIndex] = chatHistory[loadingIndex].copy(
                    text = "System",
                    localText = "SYSTEMFEHLER",
                    externalText = "Die Anfrage konnte nicht verarbeitet werden.\nGrund: $errorMsg",
                    chapterLink = null
                )
            }
        }
    }

    private fun finalizeResponse(index: Int, text: String?) {
        geminiUsesToday++
        prefs.edit { putInt("geminiUsesToday", geminiUsesToday) }
        
        val rawText = text ?: "{}"
        
        var parsedLocal: String? = "Keine spezifischen Handbuch-Einträge gefunden."
        var parsedExternal: String? = "Keine allgemeinen Informationen von Gemini."
        var parsedLink: String? = null
        var parsedSearchTerm: String? = null
        var parsedFaqTitle: String? = "Regelerklärung"

        try {
            // Sicherer Regex, der alle Varianten von Markdown-JSON-Blöcken entfernt
            val cleanJson = rawText.replace(Regex("```json\n?", RegexOption.IGNORE_CASE), "")
                                   .replace(Regex("```\n?", RegexOption.IGNORE_CASE), "")
                                   .trim()
            
            val json = JSONObject(cleanJson)

            if (json.has("lokale_antwort") && !json.isNull("lokale_antwort")) {
                val l = json.getString("lokale_antwort")
                if (l.isNotBlank()) parsedLocal = l
            }
            if (json.has("externe_antwort") && !json.isNull("externe_antwort")) {
                val e = json.getString("externe_antwort")
                if (e.isNotBlank()) parsedExternal = e
            }
            // Validierung: kapitel_link nur akzeptieren, wenn ein valides Keyword drinsteckt
            // Damit kleine Abweichungen (z.B. "1. gameplay" vs "Gameplay") toleriert werden.
            val validChapterKeywords = listOf(
                "gameplay", "völker", "klassen", "herkünfte", "talente",
                "ausrüstung", "kampf", "zauber", "zauberbuch"
            )
            if (json.has("kapitel_link") && !json.isNull("kapitel_link")) {
                val k = json.getString("kapitel_link")
                if (k.isNotBlank() && k != "null" && validChapterKeywords.any { k.contains(it, ignoreCase = true) }) {
                    parsedLink = k
                }
            }
            if (json.has("suchbegriff") && !json.isNull("suchbegriff") && parsedLink != null) {
                val s = json.getString("suchbegriff")
                if (s.isNotBlank() && s != "null") parsedSearchTerm = s
            }
            if (json.has("faq_titel") && !json.isNull("faq_titel")) {
                val f = json.getString("faq_titel")
                if (f.isNotBlank() && f != "null") parsedFaqTitle = f
            }

        } catch (e: Exception) {
            parsedLocal = "Fehler beim Auswerten der Daten."
            parsedExternal = "Konnte das JSON nicht parsen.\nRohtext von Gemini:\n$rawText"
        }

        chatHistory[index] = chatHistory[index].copy(
            text = "System",
            localText = parsedLocal,
            externalText = parsedExternal,
            chapterLink = parsedLink,
            chapterSearchTerm = parsedSearchTerm,
            faqTitle = parsedFaqTitle
        )
    }

    fun resetChat() {
        chatHistory.clear()
        activeChatSession = model25Flash.startChat()
        currentUsedModel = "Bereit"
    }

    fun addChatToFaq(question: String, answer: String) {
        faqList.add(FaqItem(question, answer))
        saveFaqs()
    }

    fun removeFaq(item: FaqItem) {
        faqList.remove(item)
        saveFaqs()
    }

    fun updateFaq(oldItem: FaqItem, newQuestion: String, newAnswer: String) {
        val index = faqList.indexOf(oldItem)
        if (index != -1) {
            faqList[index] = FaqItem(newQuestion, newAnswer)
            saveFaqs()
        }
    }
}