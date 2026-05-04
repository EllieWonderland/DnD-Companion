# Offene Aufgaben – D&D Companion Refactoring

Geordnet nach Priorität. Jede abgeschlossene Task wird direkt committed und gepusht.

---

## Priorität 1 – Fundament: Login & Multi-User-Architektur

> Alles weitere baut hierauf auf. Ohne Login ist keine Benutzertrennung möglich.

### 1.1 Firebase Authentication einrichten
- [x] Firebase Auth SDK als Gradle-Dependency hinzufügen
- [x] `LoginScreen.kt` erstellen (E-Mail + Passwort)
- [x] `AuthViewModel.kt` erstellen (Login, Logout, aktuellen User halten)
- [x] `MainActivity.kt` anpassen: vor dem Haupt-UI Auth-Zustand prüfen – nicht eingeloggt → LoginScreen
- [ ] Beim ersten Login: Weiterleitung zum Charakter-Setup-Assistenten (→ Task 3.1)
- [x] Logout-Funktion im Profil-Tab einbauen
- [x] Passwort-Reset-Flow (Forgot Password)

### 1.2 Charakterdaten pro Benutzer (Firestore statt JSON)
- [x] Firestore-Collection `users/{uid}/character` anlegen (gespeichert als Gson-JSON unter `data`-Feld)
- [x] `CharacterRepository.kt`: `getCharacterFlowFromFirestore`, `saveCharacterToFirestore`, `getCharacterOrDefault` hinzugefügt
- [x] Lokaler Room-Cache via bestehendem CharacterEntity – uid wird als character.id verwendet (kein Schema-Bruch nötig)
- [x] `CharacterViewModel.kt`: `loadUserCharacter(uid)` hinzugefügt; `loadProfile` nutzt `getCharacterOrDefault`; Firestore-Listener seeded und cached in Room
- [x] Bestehende Charaktere: Beim ersten Login ohne Firestore-Daten wird automatisch Athania-Defaults (Ranger) als Seed genutzt
- [x] Alle `characterData.id == "Athania/Delat"` auf `characterData.charClass == RANGER/WARLOCK` umgestellt; Legacy-Sync-Blöcke auf Athania/Delat beschränkt
- [x] Charakter-Wechsel-Dropdown aus ProfilScreen entfernt (jeder User hat einen eigenen Charakter)
- [x] `MainActivity.kt`: `LaunchedEffect(uid)` ruft `loadUserCharacter(uid)` nach Login auf
- [ ] `characters.json` löschen (nach vollständiger Migration → Task 3.x)

### 1.3 Gruppen-Zugangssteuerung
- [x] Firestore Security Rules: `users/{uid}/*` nur für eigenen User lesbar/schreibbar
- [x] Gruppen-Collections (`groupChat`, `globalQuests`, `publicGeneralNotes`, `publicGrudgeNotes`, `groupLootItems`, `groupLootCoins`) für alle eingeloggten User lesbar/schreibbar
- [x] `GroupViewModel.kt` bleibt wie bisher (kein User-Filter nötig)
- [x] `firestore.rules` + `firebase.json` im Projektstamm angelegt (deploybar via `firebase deploy --only firestore:rules`)

---

## Priorität 2 – Navigation umbenennen & Begleiter-Tab entfernen

> Schnell umzusetzen, schafft sofort die neue App-Struktur.

### 2.1 Tabs umbenennen
- [x] `MainActivity.kt`: Bottom-Navigation-Tab „Bücher" → **„Bibliothek"**
- [x] `MainActivity.kt`: Bottom-Navigation-Tab „Hilfe" → **„Chat"**
- [x] `BucherScreen.kt` interne Referenzen: `BucherLibraryView.kt` zeigt bereits „Bibliothek" — keine weiteren Strings
- [x] `HelpScreen.kt` interne Referenzen: keine user-facing „Hilfe"-Strings vorhanden — keine Änderung nötig

### 2.2 Begleiter-Tab entfernen
- [x] Bottom-Navigation-Tab „Begleiter" vollständig aus `MainActivity.kt` entfernen
- [x] `CompanionScreen`-Composable aus `MainActivity.kt` entfernt (war nicht in eigener Datei)
- [x] `CompanionSelectionDialog.kt` gelöscht
- [x] Begleiter-Daten (Urtier/Vertrauter) als Read-only-Karte am Ende von `ProfilScreen.kt` hinzugefügt (HP-Balken, RK, Tempo, GEFALLEN-Status)
- [x] Bottom-Nav-Indizes angepasst: Athania=0, Chat=1, Bibliothek=2
- [x] `urtier.json`, `vertrauter.json` in der Asset-Struktur belassen
- [x] `CompanionDto`, `BeastType`-Enum in `CharacterModels.kt` belassen

---

## Priorität 3 – Charakter-Tab: vollständig editierbar + Ersteinrichtung

> Ersetzt den bisherigen Athania/Delat-Tab. Jeder Spieler verwaltet seinen eigenen Charakter.

### 3.1 Charakter-Setup-Assistent (erster Login)
- [x] `CharacterSetupScreen.kt` erstellt (5-stufiger Wizard):
  - Schritt 1: Name, Rasse, Klasse (RANGER/WARLOCK via Karten), Unterklasse
  - Schritt 2: Attribute (STR, DEX, CON, INT, WIS, CHA) mit 4W6-Würfelsimulator + manuellen ±-Buttons
  - Schritt 3: Max HP (mit CON-Mod-Tipp), Trefferwürfel (W6/W8/W10/W12-Chips), Rüstungsklasse
  - Schritt 4: Startausrüstung (klassenbasierte Vorauswahl, Checkbox-Liste)
  - Schritt 5: Hintergrundgeschichte / Notizen (optional)
- [x] Wizard speichert in Firestore (`users/{uid}/character`) + Room via `saveCharacterFromSetup`
- [x] Wizard nur beim ersten Login: Flag `setupComplete` in `users/{uid}/setup/main`; `CharacterRepository.isSetupComplete/markSetupComplete` + `CharacterViewModel.checkSetupComplete/markSetupComplete`
- [x] Bestehende Sicherheitsregeln decken `users/{uid}/setup/**` bereits ab
- [ ] „Charakter bearbeiten"-Button im Profil öffnet einzelne Setup-Schritte zum Nachbearbeiten (→ Task 3.3)
- [ ] Level im Wizard wählbar machen (1–20, Schritt 1); Gruppe ist bereits Level 3–5
- [ ] Startausrüstung frei wählbar: Dropdown aus `equipment.json` + Freitext-Eingabe statt fester klassenbasierter Liste

### 3.2 Charakter-Tab umbenennen
- [x] `AthaniaScreen.kt` in `CharacterScreen.kt` umbenennen (neue Datei `ui/screens/CharacterScreen.kt`, `AthaniaTab` → `CharacterTab`)
- [x] Alle Tabs intern und im Bottom-Nav: „Athania"/„Delat" → **„Charakter"**
- [x] Tab-Icon: charClass-basiertes Fallback-Portrait (RANGER → athania, WARLOCK → delat); dynamisches Storage-Laden → Task 3.3

### 3.3 Profil-Tab editierbar machen
- [x] `CharacterEditDialog` um Textfelder erweitert: Name, Rasse, Unterklasse, Gesinnung, Hintergrund, Aussehen, Sprachen, Ideal, Makel
- [x] `saveCharacterData`: persistiert jetzt auch in Firestore + berechnet `passivePerception` neu
- [x] `applyHpIncrease` + `updateAttributes`: rufen `saveCurrentStateToFirestore()` auf (Level-Up → Firestore)
- [x] Portrait: Klick öffnet Galerie-Picker; Upload nach Firebase Storage (`users/{uid}/portrait.jpg`); URL in `CharacterData.portraitUrl`; Anzeige via Coil `AsyncImage`; Fallback classbased
- [x] Alle `if/else`-Klassenabfragen in `ProfilScreen` auf exhaustive `when` umgestellt
- [x] `portraitUrl: String = ""` zu `CharacterData` hinzugefügt (Gson-kompatibel, kein Room-Schema-Bruch)
- [x] Firebase Storage + Coil als Abhängigkeiten hinzugefügt
- [x] Unterklasse als klickbarer Link ins Regelwerk (bereits implementiert)

### 3.4 Kampf-Tab editierbar machen
- [x] `CombatScreen.kt`: Max-HP manuell einstellbar (Stift-Icon öffnet Dialog)
- [x] Trefferwürfelanzahl editierbar (selber Dialog wie Max-HP)
- [x] Eigene Waffen frei hinzufügen / entfernen (Name, Schaden, Trefferbonus als Freitextfelder; erscheinen im Waffen-Dropdown)
- [x] Rüstungsklasse manuell überschreiben (0 = auto aus Inventar berechnen)
- [x] Combat-States in Firestore speichern: maxHp, currentHp, tempHp, hitDice, manualArmorClass, customCombatWeapons

### 3.5 Zauber-Tab editierbar machen
- [ ] `ZauberScreen.kt`: Zauberliste nach Klasse filtern (aus IMPLEMENTATION_PLAN.md, Schritt 1)
- [ ] Zauber frei hinzufügen/entfernen (auch homebrew Zauber als Freitext)
- [ ] Zauberschlitze editierbar (max. Slots pro Stufe anpassbar)
- [ ] Alle Zauber-States in Firestore speichern

### 3.6 Rucksack-Tab editierbar machen
- [ ] `RucksackScreen.kt`: Münzen frei eintippbar (nicht nur per +/–)
- [ ] Eigene Gegenstände mit Name, Gewicht, Menge, Notizen eintragen
- [ ] Preisanzeige aus equipment.json (aus IMPLEMENTATION_PLAN.md, Schritt 4 – bereits erledigt, ggf. in Firestore übernehmen)
- [ ] Alle Inventar-States in Firestore speichern

### 3.7 Multiklassen-Support
- [ ] `CharacterData.charClass: CharacterClass` → `charClasses: List<CharacterClass>` (primäre Klasse + optionale Multiklasse)
- [ ] Room-Entity + Firestore-Serialisierung anpassen (kein Schema-Bruch durch Gson-Default `[]`)
- [ ] Setup-Wizard Schritt 1: zweite Klasse optional wählbar
- [ ] `CharacterScreen.kt` + Bottom-Nav: Portrait anhand primärer Klasse bestimmen (bis Portrait-Upload via Task 3.3 fertig)
- [ ] Alle Verwendungen von `charClass` (Theme-Check, Zauberfilter, Legacy-Sync) auf `charClasses.first()` oder primäre Klasse migrieren

---

## Priorität 4 – Neuer „Lore"-Tab

> Ersetzt den Begleiter-Tab vollständig. Gruppeninhalt, für alle zugänglich.

### 4.1 Lore-Tab Grundstruktur
- [ ] `LoreScreen.kt` erstellen mit `TabRow` und `HorizontalPager` (4 Sub-Tabs)
- [ ] In `MainActivity.kt`: neuen Bottom-Tab „Lore" an der Position des Begleiter-Tabs einfügen
- [ ] Firebase Security Rules: alle `lore/*`-Collections für eingeloggte User lese-/schreibbar

### 4.2 Sub-Tab: Quests
- [ ] Layout von `BucherQuestlogDetailView.kt` übernehmen
- [ ] Firestore-Collection `lore/quests` (kann bestehende `quests`-Collection umziehen oder aliasieren)
- [ ] Quest erstellen: Titel, Beschreibung, Status (Offen / In Bearbeitung / Abgeschlossen), Ort
- [ ] Quest editieren und löschen
- [ ] Filter nach Status
- [ ] `GroupViewModel.kt` um `loreQuests`-Flow erweitern (oder separate `LoreViewModel.kt`)

### 4.3 Sub-Tab: Karten
- [ ] `LoreMapsTab.kt` erstellen
- [ ] Foto aufnehmen (Kamera-Intent) oder aus Galerie wählen
- [ ] Foto in Firebase Storage hochladen (`lore/maps/{uid}/{timestamp}`)
- [ ] Firestore-Collection `lore/maps`: `{url, title, description, uploadedBy, timestamp}`
- [ ] Karten-Grid-Ansicht mit Bildvorschau
- [ ] Karte antippen → Vollbild-Ansicht mit Zoom (Pinch-to-Zoom)
- [ ] Karte löschen (nur eigene oder DM)
- [ ] `CameraPermission` und `GalleryPermission` korrekt anfragen

### 4.4 Sub-Tab: Houserules
- [ ] `LoreHouserulesTab.kt` erstellen
- [ ] Firestore-Collection `lore/houserules`
- [ ] Houserule erstellen: Titel, Regeltext (Markdown-Support), Kategorie
- [ ] Houserule editieren und löschen
- [ ] Suchfeld für Houserules
- [ ] Markdown-Rendering (bereits via `RichText` im Projekt vorhanden)

### 4.5 Sub-Tab: Stories / Notizbuch (Gruppe)
- [ ] Layout von `BucherGroupChatDetailView.kt` / `BucherBookDetailView.kt` übernehmen
- [ ] Firestore-Collection `lore/stories`
- [ ] Story/Notiz erstellen: Titel, Text (Markdown), Autor, Datum
- [ ] Alle Mitglieder können lesen und schreiben
- [ ] Einträge bearbeiten und löschen (nur eigene)
- [ ] Chronologische Sortierung (neueste zuerst)

---

## Priorität 5 – Bibliothek: Notizbuch → Tagebuch (persönlich)

> Umbenennung und Umstrukturierung des persönlichen Notizbuchs.

### 5.1 Notizbuch in Tagebuch umbenennen
- [ ] In `BucherLibraryView.kt`: Eintrag „Notizbuch" → **„Tagebuch"**
- [ ] `BucherBookDetailView.kt` (oder relevante Komponente): Titel und Strings anpassen
- [ ] Interne Variablen/Klassen umbenennen: `bookEntries`, `BookEntry` → Beibehaltung oder Umbenennung nach `DiaryEntry`

### 5.2 Tagebuch-Layout nach Gruppen-Vorbild anpassen
- [ ] Layout von `BucherGroupChatDetailView.kt` übernehmen (Listenansicht mit Einträgen, Tipp-FAB)
- [ ] Eintrag hat: Titel, Fließtext (Markdown), Datum, optional Stimmungs-Tag
- [ ] Chronologische Sortierung

### 5.3 Tagebuch persönlich & privat machen
- [ ] Daten in Firestore unter `users/{uid}/diary` speichern (nicht unter `publicGeneralNotes`)
- [ ] Security Rules: nur eigener User kann `users/{uid}/diary` lesen/schreiben
- [ ] Bestehende lokale `BookEntry`-Daten (falls vorhanden) zur Migration anbieten

### 5.4 Bestehende Gruppen-Notizbuch-Einträge sichern
- [ ] `publicGeneralNotes` bleibt als Stories-Basis im Lore-Tab (→ Task 4.5)
- [ ] Kein Datenverlust: bestehende Einträge als Stories in `lore/stories` umziehen

---

## Priorität 6 – Qualität, Zuverlässigkeit, UI/UX

> Verbesserungen ohne die sich alle anderen Tasks schlechter anfühlen.

### 6.1 Offline-Support verbessern
- [ ] Firestore Offline-Persistenz aktivieren (`FirebaseFirestore.getInstance().firestoreSettings`)
- [ ] Room-Datenbank als vollständigen Offline-Cache für Charakterdaten nutzen (Sync-Strategie: Firestore → Room bei Online, Room → UI immer)
- [ ] Netzwerk-Status-Indikator (kleines Banner „Offline – Änderungen werden synchronisiert")
- [ ] Konflikte lösen: Timestamp-basiertes „last write wins"

### 6.2 Fehlerbehandlung & Loading States
- [ ] Alle Firestore-Calls mit Try/Catch und User-Feedback (Snackbar / Toast)
- [ ] Loading-Indikatoren bei Netzwerkanfragen (CircularProgressIndicator)
- [ ] Leere Zustände mit Illustration und Hinweistext (z. B. „Noch keine Quests vorhanden – füge eine hinzu!")
- [ ] Fehlerhafte Foto-Uploads: Retry-Button und Fehlermeldung

### 6.3 UI/UX-Verbesserungen
- [ ] Einheitliches Dark-Mode-Theme für alle neuen Screens (Lore, Login, Setup-Wizard)
- [ ] Animationen beim Tab-Wechsel verbessern (Crossfade statt hartem Schnitt)
- [ ] Pull-to-Refresh auf allen Firestore-Listen
- [ ] Swipe-to-Delete auf Quest-, Story-, Houserule-Einträgen (mit Undo-Snackbar)
- [ ] FAB (Floating Action Button) einheitlich für „Neu anlegen"-Aktionen in allen Listen-Screens
- [ ] Lange Texte: Truncation mit „Mehr anzeigen"-Button in Listenansichten
- [ ] Keyboard-Handling: `ImeAction.Done` / `Next` korrekt gesetzt, kein verdecktes Keyboard

### 6.4 Würfelwurf-Tool
- [ ] Einfaches Dice-Roller-Widget (W4, W6, W8, W10, W12, W20, W100)
- [ ] Zugang per FAB oder dedizierter Button in Kampf-Tab
- [ ] Wurf-Ergebnisse kurz animiert anzeigen
- [ ] Optionaler Modifikator (+/–)

### 6.5 Push-Benachrichtigungen
- [ ] Firebase Cloud Messaging (FCM) einrichten
- [ ] Benachrichtigung bei neuer Gruppen-Chat-Nachricht
- [ ] Benachrichtigung bei neuer Quest / Quest-Statusänderung
- [ ] Benachrichtigung bei neuer Karte / Story im Lore-Tab
- [ ] Benachrichtigungseinstellungen im Profil (an/aus je Kategorie)

### 6.6 Charakter-Portrait & Avatare
- [ ] Standard-Avatare je Klasse (Ranger, Warlock, Fighter, …) als Drawable-Ressourcen bereitstellen
- [ ] Eigenes Foto wählen (Galerie/Kamera) → Firebase Storage
- [ ] Portrait im Bottom-Tab-Icon und im Profil anzeigen
- [ ] Avatare in Gruppen-Chat und Lore-Einträgen anzeigen (kleines Rund-Icon neben dem Eintrag)

### 6.7 Zuber-/Regelwerk-Verbesserungen (aus IMPLEMENTATION_PLAN)
- [ ] Unterklasse als klickbarer Link (IMPLEMENTATION_PLAN Schritt 2)
- [ ] Regelkreuz-Verlinkung in BucherRulebookDetailView (IMPLEMENTATION_PLAN Schritt 5)
- [ ] Zauberlisten-Filter nach Klasse in ZauberScreen (IMPLEMENTATION_PLAN Schritt 1)

### 6.8 Accessibility & Performance
- [ ] Content Descriptions für alle Icons und Bilder (Screen Reader)
- [ ] Mindestgröße für Touch-Targets (48 dp)
- [ ] LazyColumn statt Column in allen langen Listen (bereits teilweise vorhanden, prüfen)
- [ ] Firestore-Listener richtig aufräumen (`.remove()` in `onCleared()` der ViewModels)
- [ ] Speicherlecks durch CompositionLocal/Lifecycle prüfen (LeakCanary kurz einbinden, danach entfernen)

### 6.9 Code-Qualität
- [ ] `AthaniaScreen.kt` → `CharacterScreen.kt` umbenennen (Datei + Klasse)
- [ ] Alle Strings in `strings.xml` auslagern (aktuell viele Hardcode-Strings in Composables)
- [ ] Veraltete Begleiter-Logik aus `CharacterViewModel.kt` entfernen (nach Task 2.2)
- [ ] `IMPLEMENTATION_PLAN.md` nach Abschluss in `task.md` aufgehen lassen und löschen

---

## Priorität 7 – Zukünftige Ideen

> Noch nicht geplant. Wird laufend ergänzt.

- Spieler können eigene Klassen und Rassen als Homebrew anlegen
- Initiative-Tracker für Kämpfe (inkl. Monster-HP)
- NPC-Datenbank für den DM
- Karten-Annotationen (Pins auf Fotos setzen)
- Charakter-PDF-Export (Charakterbogen)
- Integration mit D&D Beyond oder anderen offiziellen Quellen
- Mehrere Kampagnen / Gruppen pro Account
- Spielsitzungs-Timer und Notiz-Assistent (KI-gestützt via Gemini)
- Loot-Verteilung mit automatischer Münz-Berechnung

---

## Bugfixes

> Wird laufend mit gefundenen Problemen gefüllt.

*(noch keine Einträge)*

---

## Erledigte Tasks

- [x] Schritt 4 – Preise im Inventar anzeigen (aus IMPLEMENTATION_PLAN.md)
- [x] Route spellbook edits through SpellViewModel (fix broken add/remove/toggle)
- [x] Restore text formatting in ProfilScreen
- [x] Task 1.1 – Firebase Authentication einrichten (LoginScreen, AuthViewModel, Auth-Gate in MainActivity, Logout im ProfilScreen, Passwort-Reset)
- [x] Task 1.2 – Charakterdaten pro Benutzer: Firestore-Repository, loadUserCharacter, Dropdown-Entfernung, class-basierte Logik
