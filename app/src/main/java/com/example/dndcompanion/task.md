# ToDo & Roadmap D&D Companion

## Aktuell (Phase 1: Athania Perfektionieren)
- [x] **Fehler beheben:** Workflow `/error-fix` ausführen, Build reparieren.
  - *Commit:* `git commit -m "fix: resolve AGP 9.0 & Kotlin plugin incompatibility in gradle.properties"`
- [x] **Übungsbonus in Profil-UI anzeigen:** Den berechneten `proficiencyBonus` sichtbar im ProfilScreen einbauen.
  - *Commit:* `git commit -m "feat: show proficiency bonus on profile screen"`
- [x] **Zauberbuch integrieren:** Die neuen JSON-Dateien aus `Rules/Zauberbuch` (Stufe 0-9) per JSON-Parser in die App einlesen und ein durchsuchbares, filterbares Zauberbuch (`SpellbookScreen`) implementieren. So kann man vorbereitete Zauber direkt aus der kompletten Liste auswählen.
  - *Commit:* `git commit -m "feat: integrate full spellbook catalog from JSON assets and replace manual entry"`
- [x] **Zauberbuch anpassen:** Eingeklappte Icons anzeigen für ausgerüstete Zauber in der Bibliothek. Die Zauber nach der Klasse "Waldläufer" oder "Hexenmeister" filtern.
  - *Commit:* `git commit -m "feat: add equipped badge to spell cards and filter global spellbook by character class"`
- [x] **Rasten und Lebenspunkte:** Lange Rast warnt bei fehlendem Wasser/Nahrung. Todesrettungswürfe erhalten Reaktionen bei 3 Erfolgen/Fehlschlägen.
  - *Commit:* `git commit -m "feat: add resource warnings to long rest and dialogs for death save outcomes"`
- [x] **Klassifikationen und Freie Zauber:** Druiden-Zauber haben eigenen Filter & grünes Design. Maximal 1 Level-1-Druidenzauber wählbar. `Feenfeuer` und `Dunkelheit` als kostenlose 1x/Tag Zauber hinzugefügt.
  - *Commit:* `git commit -m "feat: add druid spell restrictions, styling and free casts for faerie fire and darkness"`
- [x] **Navigation & Pfeilköcher:** Hardware-Zurück-Button navigiert jetzt zwischen den Tabs anstatt die App zu schließen. Die "Pfeilköcher" Buttons im Kampfscreen wurden klar beschriftet und optisch angepasst.
  - *Commit:* `git commit -m "fix: app navigation via back button and improve quiver ui"`
- [x] **Handbuch / Regelwerk-Referenz einbinden:** Einen Nachschlage-Bereich in der App schaffen, der die Markdown-Dateien aus `Rules/Handbuch` ausliest. Zustände (Conditions), Kampfregeln, etc. können dort sauber formatiert und klickbar angezeigt werden. Inklusive Zauberbuch. Inkl. horizontalem Swipen, Suche und klickbarem Index.
  - *Commit:* `git commit -m "feat: enhance rulebook ui with horizontal swipe, search and jumpable index"`
- [x] **LLM-Kontext (RAG) erweitern (Chatbot):** 
  - Als Quelle für den Chatbot 2 Bereiche anzeigen: Oben eine Antwort anhand der eigenen Quellen (Rules/Handbuch, Rules/Zauberbuch, Charaktersheets, Stats, etc.), unten einen Bereich mit der Antwort anhand anderer Quellen (Internet, Gemini Datenbank etc.).
  - [x] Robuste JSON-Antworten & Markdown-Rendering im Chat integriert
  - [x] Klickbare Chapter-Links zur Navigation ins Regelbuch implementiert
  - [x] RAG-Kontext um Charakterklasse (Waldläufer, Beast Master) und Volk erweitert
  - [x] Chat Source Link klickbar machen und Styling anpassen (Kapitel-Badge)
  - [x] Navigation: Klick auf Source Link öffnet BucherScreen und scrollt zum entsprechenden Kapitel/Absatz
  - [x] LLM Context Expansion: Sicherstellen, dass Gemini Profile, Stats, Rucksack, Capy, Notizbuch und Buch des Grolls sehen kann
  - [x] Lokale Suche Verbessern: Suchfunktion muss "Zaubertricks" auch in den Zauberbüchern / Spellbook.md finden und nutzen (Zauber JSONs durchsuchen)
  - *Commit:* `git commit -m "feat: make chatbot source links clickable to navigate to rulebook chapters"`
- [x] **Regelwerk - Bugfixes & Visuals:** 
  - Weißen Hintergrund gegen weiße Schrift auf farbigem Hintergrund tauschen.
  - Gesuchtes Wort im Text farblich/fett markieren (Highlighting).
  - *Commit:* `git commit -m "fix: rulebook apply dark theme and add text highlighting for search"`
- [x] **Rucksack - Gewicht & Gegenstands-Auswahl:** 
  - Auf tatsächliche Gewichte aus der Datei `equipment.md` zugreifen und summiert anzeigen.
  - Neben dem freien Eingabefeld sollten Items für den Rucksack auch über eine Liste / Suche ausgewählt werden können (Daten aus `kapitel6_equipment.md` einlesen).
  - *Commit:* `git commit -m "feat: inventory weight tracking and equipment item selection from md"`
- [x] **Levelaufstieg (Level Up) - Logik & UI:** 
  - Noch einmal auf vollständige Logik prüfen (berücksichtigt das System automatisch neue Zauberplätze, neue Zauber, Rüstungsklasse, Trefferwürfel, etc.?).
  - Das Fenster für den Level Up muss direkt im Kampf-Fenster auftauchen, sobald man aufsteigt.
  - 2 Buttons zur Wahl stellen: "Jetzt Level Up durchführen" (öffnet Profil/Dialog für Attributs-/Zauberauswahl) oder "Später erledigen" (Fenster taucht beim nächsten Öffnen des Charakter-Profils wieder auf).
  - *Commit:* `git commit -m "feat: improve level up logic and add combat screen popup notification"`
- [x] **Bücher/Notizen in "Privat" und "Öffentlich" splitten:** Die Notizbücher (Books/Tagebücher) so umbauen, dass ein Eintrag ein Flag erhält oder es getrennte Listen für `Profil-spezifisch` und `Gruppen-Öffentlich` gibt. Öffentliche Notizen werden in Echtzeit über Firebase Firestore zwischen den Geräten synchronisiert.
  - *Commit:* `git commit -m "feat: split notebooks into private and public categories and sync via firestore"`

## Phase 2: Fundament für Multi-Charakter
- [x] **CharacterViewModel abstrahieren:** Die harten Werte für "Athania" in eine flexiblere Datenklasse `CharacterData` auslagern. Das ViewModel sollte die aktuellen Daten basierend auf dem gewählten Charakter laden.
  - *Commit:* `git commit -m "refactor: abstract CharacterViewModel to use flexible CharacterData class"`
- [x] **Ressourcen trennen:** Globale Daten (Handbuch, Zauberbuch-Definitionen, gemeinsame Notizen) werden von charakterspezifischen Instanz-Daten (Inventar, HP, Attributswerte, vorbereitete Zauber, FAQ) getrennt gespeichert. 
  - *Commit:* `git commit -m "refactor: separate global resources from character-specific instance data"`

## Phase 3: Hexenmeister Delat hinzufügen
- [x] **Profil-Wechsler UI:** Einen Screen oder einen Button in der Navigation basteln, um in Echtzeit zwischen Athania (`Ranger`) und Delat (`Warlock`) zu wechseln.
  - *Commit:* `git commit -m "feat: implement profile switcher UI for multiple characters"`
- [x] **Dynamisches Menü:** Je nach aktiver Klasse das Burger-Menü anpassen. Ein Ranger sieht seine `Bestie`, ein Warlock sieht `Paktmagie`-Management oder `Schauerliche Anrufungen`.
  - *Commit:* `git commit -m "feat: add dynamic navigation menu based on active character class"`
- [x] **Warlock-Logik (Delat) hinterlegen:** Paktmagie-Slots (die nach einer kurzen Rast zurückkehren!), Zaubertricks und warlock-spezifische Eigenheiten in den Stats hinterlegen.
  - *Commit:* `git commit -m "feat: implement warlock-specific logic, pact magic and eldritch invocations"`
- [x] **FAQ privatisieren:** FAQ für Athania und Delat getrennt laden und editierbar machen.
  - *Commit:* `git commit -m "feat: separate FAQ datastore per character profile"`
  - [x] **In-App Gruppen-Chat (IC & OOC):** Über Firebase einen Chat einbauen, bei dem Spieler "In-Character" oder "Out-Of-Character" miteinander am Tisch texten können (z.B. für Heimlichkeiten).
- [x] **Geteiltes Questlog:** Ein synchronisiertes Auftragsbuch einbauen. Sobald ein Spieler eine aktive Quest anlegt oder als erfüllt markiert, wird das bei allen geupdatet.
## Phase 4: Metrisches System, FAQ & Gemini Update
- [x] **Gemini Update:** Standard-Modell auf `gemini-2.5-flash` und Fallback auf `gemini-2.5-flash-lite` ändern.
- [x] **FAQ Privatisieren:** FAQ für jeden Charakter getrennt speichern und laden.
- [x] **Metrisches System (Gewicht):** Alle "Pfd." durch "kg" ersetzen (auch in der Traglastberechnung und Parsing-Logik).
- [x] **Metrisches System (Reichweiten):** Alle Entfernungen/Reichweiten ("Fuß", "ft.") in Meter und Felder (".m / . Felder") umwandeln.
- [x] **Dauerhafte und temporäre HP:** Athania hat 40 dauerhafte HP, Delat aber nur 35 dauerhafte HP und 12 temporäre HP.
- [x] **Stats korrekt ziehen:** Delat zieht die noch nicht seine eigenen Stats (Waffen, Rucksack, Zauber etc.), sondern die von Athania. Umstellen auf stats_delat.md

## Phase 5: Neues Layout (Pergament & Stein-Optik)
### Stufe 1: Design-Fundament (Theme, Fonts, Farben, Assets)
- [x] **Farbpalette erweitern** (`Color.kt`): `Pergament`, `Waldgruen`, `OchsenblutRot`, `Bronze`, `HexenLila`, `WaldGold`.
- [x] **Schriftarten einbinden** (`Type.kt` + `res/font/`): `MedievalSharp`, `Almendra`, `Grenze Gotisch`.
- [x] **Theme.kt & System-UI**: Neues `ColorScheme`, `dynamicColor = false`, StatusBar auf `WaldgruenDunkel`.
- [x] **Assets vorbereiten**: `pergament_hintergrund.png` + `steinerne_attributskarte.png` nach `res/drawable/`.
- [x] **Basis-Composables**: `PergamentBackground`, `PergamentCard`, `SteinCard` (in `PergamentComposables.kt`).

### Stufe 2: Navigation & App-Rahmen (`MainActivity.kt`)
- [x] **Bottom-Navigation**: WaldgruenDunkel-Hintergrund, WaldGold-Icons, Almendra-Schrift.

### Stufe 3: ProfilScreen
- [x] Hintergrund & Charakter-Karte (Wachssiegel-Optik).
- [x] **Attribut-Boxen**: Stein-Hintergrund, `Grenze Gotisch` Schrift für Werte.
- [x] **Merkmale/Traits**: Pergament-Rollen-Design.

### Stufe 4: CombatScreen
- [x] **HP-Anzeige**: Glasampullen/Runen, **Temp HP** als oberer Eisblau-Balken (zuerst abbauen).
- [x] **Buttons & Retter**: Bronze-HP-Buttons, Runen-Todesrettungswürfe.
- [x] **Waffen-Boxen**: Athania (Holztafeln), Delat (Metallschilde).
- [x] **Angriffs-Boxen**: Metallschild-Optik, charakterspezifische Akzente (`WaldGold` vs `HexenLila`).
- [x] **Urtier-Sektion (Athania)**: Eigener Pergament-Rahmen, Capy-Holzschnitt, Beast-Type Tabs.

### Stufe 5: ZauberScreen
- [x] **Slots**: Kristall-Fläschchen (Ranger), Runen (Warlock). Skalierung mit Level (G2/G3).
- [x] **Hintergrund**: Dunkleres Pergament für die Zauberliste.
- [x] **Dialoge**: "Rasten"-Dialoge im Pergament-Look.
- [x] **Buttons**: Bronze für Kurze Rast, Waldgruen für Lange Rast. Schule-Symbol und Pergament-Finish.

### Stufe 6: RucksackScreen & Dialoge
- [x] **Traglast**: Seil-/Ketten-Balken (Gefahrenfarben orange/rot).
- [x] **EquipmentPickerDialog**: Komplettes Redesing des Item-Katalogs (Listen, Filter, Suche).
- [x] **Geldbeutel**: Münz-Icons & Goldener Rahmen.

### Stufe 7: BücherScreen (Bibliothek)
- [x] **Übersicht**: Buch-Asset-Illustrationen (`notizbuch.png`, etc.) statt Emojis.
- [x] **Detail-Ansichten**: `SpellbookDetailView`, `QuestlogDetailView` und Gruppen-Chat (Papyrus) redesignen.

### Stufe 8: HelpScreen (Regel-Chat & FAQ)
- [x] Chat-Blasen: User (Tintenfass), Bot (versiegelte Briefe).
  - *Notiz für Commit:* `feat: redesign chat bubbles with parchment styling`
- [x] Gemini-Slots als Runen-Leiste.
### Stufe 9: Finale Politur & Accessibility
  - [x] **Typografie & Kontrast:** Umstellung auf 16sp Standard, 1,5 Zeilenabstand und APCA Lc >= 75 (Lc >= 90 für Warnungen).
    - *Notiz für Commit:* `style: update global typography and accessibility to 16sp and 1.5 line height`
  - [x] **Touch-Targets:** Alle klickbaren Elemente auf mindestens 48x48dp vergrößern.
    - *Notiz für Commit:* `style: increase touch targets to 48dp across all screens for better accessibility`
  - [x] **Dynamik:** HP-Balken mit `animateFloatAsState` für flüssige Übergänge versehen.
    - *Notiz für Commit:* `feat: add smooth HP bar animations using animateFloatAsState`
  - [x] **Ikonographie:** Bottom-Navigation Icons durch stilisierte Grafik-Assets ersetzen.
    - *Notiz für Commit:* `ui: replace navigation emojis with stylized parchment-style icons`
  - [x] **Dialoge:** EP-, Rasten- und Level-Up-Dialoge im Pergament-Design vereinheitlichen.
    - *Notiz für Commit:* `ui: redesign EP and rest dialogs with consistent parchment theme& accessibility`
- [x] **Phase 7: UI Polish - Splash Screen**
    - [x] Integration & Refinement <!-- id: 14 -->
    - [x] Add entry point to Character Profile <!-- id: 15 -->
    - [x] Connect feature selection to character sheet (visibility) <!-- id: 16 -->
    - [/] Verify UI on different screen sizes <!-- id: 17 -->
    *Notiz für Commit: Splash Screen verschönert und an das App-Thema angepasst (Pergament & Stein).*

- [ ] Cleanup & Debugging <!-- id: 18 -->
    - [ ] Investigate manual trait dialog trigger issues <!-- id: 19 -->
    - [ ] Replace manual dialog in `ZauberScreen.kt` with catalog <!-- id: 20 -->
    - [ ] Integrated catalog into `LevelUpDialog.kt` (feat selection) <!-- id: 21 -->
    - [ ] Verify persistence of learned features <!-- id: 22 -->
- [x] **Phase 6: Beeren-Mechanik & Combat UI**
    - [x] `eatGoodberry()` Logik im CharacterViewModel implementieren.
    - [x] Merge-Konflikte in `ProfilScreen.kt`, `ZauberScreen.kt` und `CharacterViewModel.kt` beheben.
- [x] Syntaxfehler nach dem Pull korrigieren (geleeckte Codeblöcke und Merge-Markierungen).
    - [x] Manuellen Beeren-Zauber-Button im Rucksack entfernt.
    - [x] "Beere essen" Button im Kampf-Tab (unter HP) hinzufügen.
    - [x] Sichtbarkeit des Buttons an Beeren-Bestand koppeln.
    - [x] **Universeller Support:** Sichtbarkeit im Rucksack für alle Charaktere (auch Delat) umgesetzt.
    *Notiz für Commit: Refine Goodberry interaction and ensure universal support for all character profiles.*

### Stufe 10: Verbesserungen nach erstem Spiel
- [x] Option, Waffen bei "Waffen ausrüsten" zu ändern
  *Notiz: Waffenauswahl-System flexibler gestaltet (Umbenennung möglich).*
- [x] Gute Beeren essen Bug: Button fehlt oder Sichtbarkeit fehlerhaft (Tagesration klären)
  *Notiz: Button ist nun für Ranger immer sichtbar (wenn Slot vorhanden) und Snackbar zeigt Aktion.*
- [x] Urtier-Begleiter: Stats vervollständigen (siehe urtier.json)
  *Notiz: Urtier-Stats vollständig aus JSON geladen und inklusive Attributen angezeigt.*
- [x] Heroische Inspiration: Markierung im Kampftab hinzufügen
  *Notiz: Checkbox für heroische Inspiration hinzugefügt.*
- [x] Rucksack-Transfer: Gewichte korrigieren & optisches Feedback (Snackbar)
  *Notiz: Gewicht-Parsing verfeinert und Snackbar-Feedback beim Hinzufügen integriert.*
- [x] Capy (Urtier): Tod und Wiederbelebung implementieren
  *Notiz: Tod-Status und Wiederbelebungs-Mechanik (mit Slot-Check für Ranger) umgesetzt.*
- [x] Trefferwürfel-Anzeige: Format auf `Aktuell / MaxW-Typ` ändern (z.B. 4 / 4W10)
  *Notiz: Hit Dice Anzeigeformat auf dynamischen Würfeltyp korrigiert.*
- [x] Lebenspunkte Delat: Temp HP nicht in Totalsatz einrechnen
  *Notiz: HP und Temp-HP Anzeige strikt getrennt (Zähler und Balken).*
- [x] Delat Level-Up Bug: Werte-Verbesserung dem richtigen Charakter zuordnen
  *Notiz: Level-Up Bug bei Profil-Wechsel beheben (Athania wurde fälschlicherweise verbessert).*
- [ ] Delat-Rettungswürfe: Korrektur auf Cha, Wis (geübt)
  *Notiz: Korrektur der Rettungswürfe für Delat.*
- [x] Vertrauter: "Sphinx der Wunder" für Delat hinzufügen (eigener Tab)
  *Notiz: Begleiter-Tab dynamisch für Urtier und Sphinx umgesetzt.*
- [ ] Charakter-Profil: Ideal und Makel anzeigen
  *Notiz: Ideal und Makel im Profil-Screen ergänzen.*

### Stufe 11: Optische Anpassungen
- [x] Zauber-Details: Komplette Beschreibung beim Ausklappen anzeigen
  *Notiz: Spell-detail-Ansicht verbessern.*
  *Commit:* `feat: make free spell traits completely expandable with full description in ZauberScreen`
- [ ] Metrik-Rundung: Alle Entfernungen auf 0,5m runden (keine Kommazahlen wie ,24)
  *Notiz: Entfernungsangaben auf 0,5m runden.*
## Bugfixes
- [x] **Freie Zauber Filter:** "Wunden heilen" (bzw. "Heilendes Wort") wird bei den kostenlosen Zaubern gelistet, obwohl es im Zauberbuch nicht ausgerüstet/vorbereitet ist. Die Verknüpfung Gegenstand <-> Zaubertab prüfen.
  *Commit:* `fix: dynamically show Amulet/Druid focus free spell based on prepared spells`
- [x] **Chatbot-Identität:** Der Chatbot nennt den Charakter manchmal noch "Athania", auch wenn Delat aktiv ist.
- [x] **Temporäre HP Button:** Der Zauber/das Merkmal "Unholde Vitalität" (False Life) für Delat sollte einen direkt nutzbaren Button haben, um die 12 Temp HP (oder 1W8+4 nach 2024er Regeln) schnell zu erneuern.
- [x] **Warlock Pakt-Logik vs. G1 Slots:** Warlocks haben keine G1 Slots, aber Delat hat Zauber aus Talente (Segnen, Nebelschritt, Magierrüstung), die 1/LR ohne Slot gewirkt werden können. Diese fehlen im Tracking.
- [x] **Unholde Vitalität (At-Will):** Sicherstellen, dass der Button für Unholde Vitalität unendlich oft nutzbar bleibt (nicht ausgrauen), da es eine schauerliche Anrufung ist.
- [x] **Zauberbuch-Vollständigkeit (Delat):** Prüfen, ob alle Zauber aus stats_delat.md im Charakterblatt hinterlegt sind.
- [x] **Dynamische FAQ-Schlagworte:** Gemini soll automatisch passende Titel/Tags für FAQ-Einträge generieren (statt statisch "Regelerklärung").
- [x] **Rucksack:** Der Geldbeutel stimmt in der ausgeklappten und eingeklappten Version nicht überein , die Ausschöpfung der Traglast ist nicht erkennbar und bei Neuer Fund kann man die Kategorie nicht mehr auswählen (drop-down).
- [x] **Accessability Check:** Kontraste optimiert (Merkmale auf OchsenblutRot, Zauber-Typ auf PergamentHell). Schriftgrößen auf 16sp erhöht.
- [x] **Konsistenzcheck:** Buttongrößen vereinheitlicht (Weapon 130dp, HP 60dp). Athania/Delat Talente-Layout angeglichen.
- [x] **Stats Check:** Überprüfe nochmal die Stats von Athania und Delat. Sind alle Werte korrekt? Sind alle Zauber und Merkmale hinterlegt?
  - [x] Grunddaten, Kampf-Stats und Inventar von Delat verifiziert.
  - [x] Diskrepante/Hardcodierte Werte identifiziert (Skills, Initiative, Volk, Hintergrund etc.).
  - [x] Fehlende Zauber in der Konfiguration identifiziert.
  - [x] Implementierungsplan zur Bereinigung und Vervollständigung umgesetzt.
  - [x] **Bug-Fix Build:** Behebung der Build-Fehler nach Delat-Integration.
  *Notiz: Build-Fehler (Unresolved references, Syntax) behoben. "Riverside"-Leak in MainActivity entfernt, Heroische Inspiration wiederhergestellt und Snackbar-Layout korrigiert.*
- [x] **Kostenlose Zauber:** Layout für Athania umgesetzt (kompakte Sektion). 1/LR Tracking visuell integriert.
- [x] **Zauberbuch:** Die Schrift ist auf dem Hintergrund im Zauberbuch besser lesbar (Kontraste optimiert, auch in der Bibliothek).
  - Note for Commit: Fix readability issues in Spellbook (both in equip screen and library) by using high-contrast parchment theme and fixing button text colors. Add missing imports in BucherScreen.kt.
- [x] **Pfeilköcher:** Anzeige für Nicht-Waldläufer (wie Delat) ausgeblendet.
- [x] **Free Spells Mechanik implementieren (Rituale, Features, Items)**
  - [x] Ritual-System: (Ritual) Tag im JSON parsen und als Button in ZauberCard anzeigen.
  - [x] Feature-Tracking: TraitItem erweitert um currentUses/maxUses und grantedSpellId.
  - [x] Item-Charges: InventoryItem erweitert um charges-system.
  - [x] Validierung: Abgleich mit freespells.md und stats_delat.md.
    - [x] Athania-Spezial: Zeichen des Jägers (2/LR), Gute Beeren, Wunden heilen ergänzt.
    - [x] UI-Fix: Scroll-Probleme und Padding im Verwalter-Dialog behoben (Spacer + 40dp).
    - [x] Logik-Fix: `loadTraits` ergänzt nun fehlende Standard-Merkmale bei bestehenden Saves.
    - [x] Sichtbarkeit: Freie Zauber aus Merkmalen erscheinen nur, wenn sie im Zauberbuch vorbereitet sind.
- [x] **Waffen-Logik & Meisterschaften:**
    - [x] "Zweihändig anlegen" für alle vielseitigen Waffen (Speer, Hammer, Shillelagh) ermöglichen.
    - [x] AC-Abzug (-2), wenn eine "Schild-Waffe" zweihändig geführt wird.
    - [x] Weapon Masteries Texte aktualisieren (Stoß, Schwächen, Umwerfen etc.).
    - [x] Delats Basis-RK auf 12 korrigieren.
    - [x] Masteries-Effekte im Combat-Tab detaillierter beschreiben.
- [x] **UI-Optimierung:** 
    - [x] Button zum Hinzufügen von Zaubern im Zauberbuch fixiert am unteren Rand (immer sichtbar).
    - [x] Merkmale/Talente für Delat vervollständigt (Eingeweihter der Magie, Feenberührt) und Namen präzisiert.
- [x] **UI-Fixes:**
    - [x] Buttongröße für "Kriegshammer (Pakt)" in `CombatScreen.kt` prüfen und flexibel gestalten.
    - [x] Generelle Prüfung der Button-Flexibilität in der Kampf-Ansicht.
      *Notiz für Commit: ui: refactor combat buttons to use flexible layouts and weight modifiers to prevent text clipping*
- [x] **Icon-Fix:**
    - [x] Icons in `res/drawable` prüfen und fehlende Icons hinzufügen bzw. alte austauschen.
      *Notiz für Commit: ui: replace redundant placeholder icons with specific assets (kampf, rucksack, vertrauter, hilfe) in navigation tabs*
- [ ] **Regelwerk:**
    - [ ] Regelwerk auf Verwendung der neuen JSON-Dateien umstellen
    - [ ] Room anlegen und verwenden
- [ ] **Feature-Katalog Integration:**
    - [ ] JSON-Struktur für Merkmale/Talente definieren und `features.json` erstellen.
    - [ ] Daten aus Markdown-Dateien in `features.json` extrahieren (Talente, Klassenmerkmale, Volksmerkmale).
    - [ ] `CharacterViewModel` um Ladelogik für den Feature-Katalog erweitern.
    - [ ] UI zur Auswahl von Merkmalen/Talenten implementieren (gefiltert nach Klasse/Rasse).

### Stufe 12: Playtest Refinement
- [x] HP-Anzeige aufräumen: Doppelte Balken und Zähler entfernen <!-- id: 460_1 -->
  *	Commit: `ui: cleanup HP display and separate temp HP`
- [x] Gute Beere Logik: Wirken verbraucht Slot, Essen ausgegraut bei 0 <!-- id: 460_2 -->
  *	Commit: `feat: implement Good Berry cast logic and UI states`
- [x] Dynamisches Waffensystem: Auswahl nur aus Inventar, automatische RK-Anpassung <!-- id: 460_3 -->
  *	Commit: `feat: implement inventory-based dynamic weapon system`
- [x] Schild-Logik: Checkbox für RK (+2), nur aktiv wenn Schild im Inventar <!-- id: 460_4 -->
  *	Commit: `feat: add shield logic and automatic AC adjustment`
- [x] Kampf-UI: "Standard-Taktik" über Loot-Buttons verschieben <!-- id: 460_5 -->
  *	Commit: `ui: move standard tactics above loot button`
- [x] Zauber-UI: Werte-Karten verkleinern, Rast-Buttons vergrößern <!-- id: 460_6 -->
  *	Commit: `ui: optimize spell tab layout and rest buttons`
- [x] Begleiter-Refinement: Urtier-Wiederbelebung nach langer Rast <!-- id: 460_7 -->
  *	Commit: `feat: automatic companion revival after long rest`
- [x] Inventar-Bugcheck: Gewichte beim Katalog-Hinzufügen validieren <!-- id: 460_8 -->
  *	Commit: `fix: ensure weights are correctly transferred from catalog`

### Zukünftige Aufgaben