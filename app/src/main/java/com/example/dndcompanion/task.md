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

- [x] Cleanup & Debugging <!-- id: 18 -->
    - [x] Investigate manual trait dialog trigger issues <!-- id: 19 -->
    - [x] Replace manual dialog in `ZauberScreen.kt` with catalog <!-- id: 20 -->
    - [x] Integrated catalog into `LevelUpDialog.kt` (feat selection) <!-- id: 21 -->
    - [x] Verify persistence of learned features <!-- id: 22 -->
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
- [x] Delat-Rettungswürfe: Korrektur auf Cha, Wis (geübt)
  *Notiz: Korrektur der Rettungswürfe für Delat.*
- [x] Vertrauter: "Sphinx der Wunder" für Delat hinzufügen (eigener Tab)
  *Notiz: Begleiter-Tab dynamisch für Urtier und Sphinx umgesetzt.*
- [x] Charakter-Profil: Ideal und Makel anzeigen
  *Notiz: Ideal und Makel im Profil-Screen ergänzen.*

### Stufe 11: Optische Anpassungen
- [x] Zauber-Details: Komplette Beschreibung beim Ausklappen anzeigen
  *Notiz: Spell-detail-Ansicht verbessern.*
  *Commit:* `feat: make free spell traits completely expandable with full description in ZauberScreen`
- [x] Metrik-Rundung: Alle Entfernungen auf 0,5m runden (keine Kommazahlen wie ,24)
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
- [x] **Regelwerk:**
    - [x] Regelwerk auf Verwendung der neuen JSON-Dateien umstellen
    - [x] Room anlegen und verwenden
    - [x] *Neu:* Globale Suche im Regelwerk (Reiter "Global", durchsucht alle Kategorien & Ausrüstung)
    - [x] *Neu:* Chatbot-RAG Anbindung an Room-DB statt Markdown-Dateien
    *Notiz für Commit: feat: integrate Room database with Gemini RAG Chatbot and implement Global rulebook search tab*
- [x] **Feature-Katalog Integration:**
    - [x] JSON-Struktur für Merkmale/Talente definieren und `features.json` erstellen (heißt jetzt `merkmale.json`).
    - [x] Daten aus Markdown-Dateien in `merkmale.json` extrahieren (Talente, Klassenmerkmale, Volksmerkmale).
    - [x] `FeatureEntity` in Room anlegen und `CharacterViewModel` um Ladelogik für `merkmale.json` erweitern.
    - [x] UI zur Auswahl von Merkmalen/Talenten implementieren (z.B. Filterung nach Klasse/Rasse beim Level-Up oder in einem `FeaturePickerDialog`).
    *Notiz für Commit: feat: implement FeaturePickerDialog and integrate into LevelUpDialog and ProfileScreen*

### Sonstiges / Backlog
- [x] **Erweiterte Suche (Regelwerk):** Globale Suche verbessern (Suche im Fließtext, Suche nach Unterklassen, Fuzzy Search).
  *Notiz: SQLite LIKE-Suche erweitert, um JSON-Felder für Subklassen und Traits sowie Eigenschaften zu durchsuchen.**
- [x] **Zauberbuch in Room-Datenbank migrieren:**
    - [x] `SpellEntity` und DAO Methoden anlegen.
    - [x] `AppDatabaseCallback` anpassen, um `spellbook.json` zu parsen.
    - [x] `CharacterViewModel` refactorn (Entfernen von `globalSpellbook` zugunsten von Room Flows).
    - [x] UI-Anpassungen (ZauberScreen & BucherScreen) an das neue `StateFlow` binden.
    - [x] Volle Beschreibungen für vorbereitete Zauber und kostenlose Talente im Zauber-Tab anzeigen.
    *Notiz für Commit: feat: display full spell texts from Room DB for prepared spells and free traits in ZauberScreen*

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
- [x] **HP Anzeige:** HP anzeige verbessern
- [x] **Gruppen-Chat:** Möglichkeit, Chat komplett zu archivieren und IC/OOC wieder trennen
- [x] **Levelaufstieg:** Dynamische Anzeige, wieviele EP/HP noch für den Levelaufstieg benötigt werden
- [x] **Urtier:** HP vom Urtier des Landes sinkt automatisch von 25 auf 20, wenn ich zum Urtier des Himmels und anschließend wieder zurück wechsle

### Stufe 13: Finishing Touches (Aufgaben für Gemini 3.1 Pro)
- [x] **Code Analyse:** Code auf mögliche Verbesserungen prüfen
- [x] **Refactoring:** Code und Dateien aufräumen
- [x] **Accessibility:** Accessibility Check durchführen

### Stufe 14: Code-Qualität & Refactoring (basierend auf Code-Analyse)

#### Kritische Fixes
- [x] **GlobalScope-Leak beheben:** `GlobalScope` in `AppDatabase.kt` durch einen dedizierten `applicationScope` (`CoroutineScope(SupervisorJob())`) ersetzen.
- [x] **Firebase Error-Handling:** Firestore-Listener geben bei Fehlern kein Feedback. Snackbar/Log bei Fehler ergänzen (`CharacterViewModel.kt`).
- [x] **Todeswürfe Auto-Reset:** `updateDeathSaves(0, 0)` in `forceLongRest()` ergänzt — Todeswürfe werden jetzt bei langer Rast automatisch zurückgesetzt.

#### Moderate Verbesserungen
- [x] **CharacterViewModel aufteilen:** 2.659 Zeilen God Object in 5 fokussierte ViewModels gesplittet: `CombatViewModel`, `SpellViewModel`, `InventoryViewModel`, `GroupViewModel` + schlankes `CharacterViewModel`. Alle neuen VMs reagieren via `activeCharacterIdFlow` auf Charakterwechsel.
- [x] **SharedPreferences kapseln:** `PrefsManager`-Klasse erstellen, die alle `getSharedPreferences()`-Aufrufe zentral verwaltet.
- [x] **Feature-Logik entkoppeln:** String-Matching (`feature.name.contains("Erzfeind")`) durch Feature-Metadaten in der JSON-Struktur ersetzen.
- [x] **Waffen-Desync beheben:** `equippedWeaponName` und `InventoryItem.name` synchronisieren oder auf eine einzige Quelle reduzieren.

#### Code-Qualität
- [x] **Lange Screens aufteilen:** `BucherScreen.kt` (1.399 Z.), `ZauberScreen.kt` (1.122 Z.) und `CombatScreen.kt` (701 Z.) in Sub-Composables auslagern.
- [x] **Doppelte DAO-Methoden konsolidieren:** 8× duplizierte `searchX()` / `searchXRaw()` Paare in `RulebookDao.kt` vereinfachen.
- [x] **Logging hinzufügen:** Stille Fehler in DB-Operationen und Firebase mit `android.util.Log` sichtbar machen.
- [x] **Unit Tests:** Kritische Logik testen (Slot-Berechnung Ranger vs. Warlock, Schadensformel, Gewichtskapazität).

---

## Daten-Audit: Kapitel vs. JSON (Stand 2026-03-18)

### A. Datenvollständigkeit (Kapitel → JSON)

| Kapitel | Inhalt | Status | JSON-Datei |
| :--- | :--- | :--- | :--- |
| Kapitel 1 | Gameplay, Würfel, Attribute, Aktionen, Kampf-Grundlagen | ✅ Vollständig | `rules.json` (22 Gameplay-Einträge) |
| Kapitel 2 | Spezies (10 Völker) | ✅ Vollständig | `character_options.json` (species-Array) |
| Kapitel 3 | Klassen + Unterklassen (12 Klassen) | ❌ KRITISCH UNVOLLSTÄNDIG | `character_options.json` (nur Mönch, Paladin, Schurke); `merkmale.json` (nur Waldläufer+Hexenmeister) |
| Kapitel 4 | Hintergründe (16 Backgrounds) | ⚠️ Vorhanden, falsch platziert | `character_options.json` (classes[3].backgrounds) |
| Kapitel 5 | Talente (Origin, Stil, Allgemein, Episch) | ⚠️ Vorhanden, falsch platziert | `character_options.json` (classes[3].feats) |
| Kapitel 6 | Waffen, Rüstungen, Werkzeuge, Ausrüstung, Reittiere | ✅ Vollständig | `equipment.json` (194 Items) |
| Kapitel 6 | Dienstleistungen | ✅ Vollständig | `rules.json` (4 Einträge) + `equipment.json` (1 Eintrag) |
| Kapitel 7 | Zaubermechanik, Rituale, Komponenten, Bereiche | ✅ Vollständig | `rules.json` (7 Zauber-Einträge) |
| Kapitel 8 | Kampfablauf, Aktionen, Zustände (15 Stück) | ✅ Vollständig | `rules.json` (19 Kampf&Zustände-Einträge) |

### B. Strukturprobleme (JSON-Verwertbarkeit)

- [x] **`character_options.json` Klassen unvollständig:** Das `classes`-Array enthält nur Mönch, Paladin und Schurke. Die anderen 9 Klassen fehlen komplett: **Barbar, Barde, Kleriker, Druide, Kämpfer, Waldläufer, Zauberer, Hexenmeister, Magier**. Athania (Waldläufer) und Delat (Hexenmeister) sind damit NICHT in character_options.json vertreten.
- [x] **`character_options.json` Strukturfehler:** `classes[3]` ist kein Klassen-Objekt, sondern `{"backgrounds": [...], "feats": [...]}`. Backgrounds und Feats sind falsch in das Classes-Array eingebettet statt eigene Top-Level-Keys zu sein. **Risiko: App-Code, der über classes iteriert, kann abstürzen oder Backgrounds/Feats ignorieren.**
- [x] **`merkmale.json` Volksmerkmale unvollständig:** Nur Elf und Zwerg haben Volksmerkmale. Fehlend: Aasimar, Drachenblütiger, Gnom, Goliath, Halbling, Mensch, Ork, Tiefling. (Vollständige Daten sind in `character_options.json` vorhanden, aber nicht im merkmale.json-Format für den FeaturePickerDialog.)
- [x] **`equipment.json` Gewichtseinheit:** Alle Gewichte im Feld `weightLb` (Pfund). Die App zeigt kg an. Prüfen ob die Konvertierung (×0,453) im App-Code korrekt erfolgt oder ob ein Fehler besteht.
- [x] **`equipment.json` adventuring_gear ohne `category`-Feld:** Die 93 Abenteuerausrüstungs-Items haben kein `category`-Feld (die anderen Kategorien wie Waffen/Rüstungen haben es). Dies kann den EquipmentPickerDialog-Filter beeinträchtigen.

### C. Inkonsistenzen: Athania (stats.md)

- [x] **Kampfstab-Schaden falsch:** `stats.md` zeigt `1W4 Wucht` für den Kampfstab. Laut Kapitel 6 macht ein Kampfstab `1W6 Wucht` (einhändig) / `1W8 Wucht` (zweihändig). Muss korrigiert werden.
- [x] **"D/W (Lange Schritte)" unbekannte Notation:** Eintrag ist Lange Schritte (Longstrider), ein Level-1 Druide/Waldläufer-Zauber. Notation zu "Lange Schritte (D/W) - Aktion, Berührung, 1 Std. — Bewegungsrate +3m" klargestellt.
- [x] **Drow-Volksmerkmale unvollständig in stats.md:** Drow-Magie Merkmal mit Tanzende Lichter, Feenfeuer (St.3 1x/LR), Dunkelheit (St.5 1x/LR) in Volksmerkmale-Abschnitt ergänzt.
- [x] **"Kalte Hand (T)":** Das Suffix "(T)" stand für die Schulabkürzung, unklar notiert. Beschreibung zu "Nekromantie" und korrekter Regeltext (kein HP-Regenerieren, Vorteil gg. Untote) aktualisiert.

### D. Inkonsistenzen: Delat (stats_delat.md)

- [x] **Rüstungsübung "Mittlere" falsch:** `stats_delat.md` listet unter Rüstungsübung "Leichte, Mittlere". Hexenmeister haben in D&D 2024 **nur Leichte Rüstung**. Mittlere Rüstung ist eine Fehleingabe und muss entfernt werden. Dies beeinflusst die RK-Berechnung und Ausrüstungsauswahl.
- [x] **"Paktwaffe" als Zaubertrick falsch:** `Paktwaffe` steht in Delats Zaubertrick-Liste (Level 0). Paktwaffe ist kein Zaubertrick, sondern eine Funktion der Schauerlichen Anrufung "Pakt der Klinge". Sollte aus der Zauberbuch-Liste entfernt und stattdessen nur unter Merkmale/Anrufungen geführt werden.
- [x] **"Macht der Tiefe" auf Stufe 4 falsch:** "Macht der Tiefe" ist ein Stufe-10-Merkmal des Patrons der Großen Alten. Delat ist erst auf Stufe 4 und kann dieses Merkmal regelkonform nicht besitzen. Es muss aus den Merkmalen entfernt werden.
- [x] **HP-Maximum 47 falsch:** Berechnung für Stufe-4-Hexenmeister mit KON 15 (+2) und Zwergenzähigkeit: 8 (W8 max Stufe 1) + 3×5 (W8-Schnitt) + 4×2 (KON) + 4×1 (Zwergenzähigkeit) = **35 Max-HP**. Eingetragen sind 35/47. Die 47 entspricht 35 + 12 (Temp HP aus "Unholde Vitalität") — Temp HP dürfen NICHT zum Max-HP addiert werden. **App-Bug: Temp HP werden fälschlicherweise in Max-HP eingerechnet.**

### E. Regelabweichungen (2024 Edition)

- [x] **Kapitel 8 "Unsichtbar"-Zustand-Widerspruch in rules.json und kapitel8_combat_conditions.md:** Kapitel 7 (und kapitel8.md Zeile 115) sagt: Wenn eine Kreatur dich durch Wahren Blick/Blindsicht sehen kann, profitierst du **nicht** von Vorteil/Nachteil. Kapitel 1 (rules.json) und kapitel1.md sagen hingegen das Gegenteil (allgemeiner Wortlaut). Diese Inkonsistenz zwischen kapitel1 und kapitel8 existiert in den Quelldokumenten — muss für den RAG-Chatbot eindeutig klargestellt werden (Kapitel 8 hat Vorrang als speziellere Regel).
- [x] **Kapitel 6 Hinweis Maßeinheiten veraltet:** kapitel6_equipment.md Zeile 5 sagt noch "Alle Gewichtsangaben sind in Pfund (Pfd. / lb.)". Die App nutzt aber kg. Die .md-Datei sollte aktualisiert werden, damit der RAG-Chatbot keine veralteten Maßeinheiten nennt.

---

## Daten-Audit: Restliche assets/Rules (Stand 2026-03-18)

Geprüfte Dateien: `Zauberbuch/Spellbook.md`, `Zauberbuch/spellbook.json`, `freespells.md`, `stats.json`, `urtier.json`, `vertrauter.json`

### F. spellbook.json — Strukturprobleme & Vollständigkeit

- [x] **Trailing Whitespace in `classes`-Feld:** 13 Zauber haben einen Leerzeichen-Fehler am Ende des Klassennamens (z. B. `"Hexenmeister "`, `"Kleriker "`, `"Magier "`, `"Zauberer "`). **App-Bug: String-Vergleich `class == "Hexenmeister"` schlägt fehl → Delats Zauber werden nicht korrekt gefiltert.** Per Python-Skript bereinigt (5 Einträge getroffen).
- [x] **5 Zauber ohne `description`-Feld:** Die Beschwörungs-Zauber `Celestisches Wesen herbeirufen`, `Drachen herbeirufen`, `Elementar herbeirufen`, `Feenwesen herbeirufen` und `Gegenstände beleben` haben nur ein `summon_stat_block`-Feld aber keine Beschreibung. Der RAG-Chatbot kann diese Zauber nicht erklären.
- [x] **18 Zauber mit abweichenden deutschen Namen (Spellbook.md ↔ spellbook.json):** Spellbook.md und spellbook.json verwenden verschiedene Übersetzungen für dieselben Zauber. Der RAG-Chatbot und die Suchfunktion könnten Zauber nicht zuordnen:
  | Spellbook.md | spellbook.json | Englisch |
  | :--- | :--- | :--- |
  | Klirren | Zerbersten | Shatter |
  | Schwächungsstrahl | Schwächestrahl | Ray of Enfeeblement |
  | Treffsicherer Schlag | Zielsicherer Schlag | True Strike |
  | Wortgewandtheit | Redegewandtheit | Glibness |
  | Machtwort Betäuben | Wort der Macht Betäubung | Power Word Stun |
  | Machtwort Tod | Wort der Macht: Tod | Power Word Kill |
  | Grauen | Unheimliches Schicksal | Weird |
  | Kraftkäfig | Energiekäfig | Forcecage |
  | Unterweltler beschwören | Unhold herbeirufen | Summon Fiend |
  | Waldwesen beschwören | Wesen des Waldes beschwören | Conjure Woodland Beings |
  | Fee beschwören | Feenwesen beschwören | Conjure Fey |
  | Bestie beschwören | Tier herbeirufen | Summon Beast |
  | Halluzinatorisches Gelände | Scheingelände | Hallucinatory Terrain |
  | Magierüstung | Magierrüstung | Mage Armor |
  | Magische Stille | Stille | Silence |
  | Flamme erzeugen | Flammen erzeugen | Produce Flame |
  | Greifende Ranke | Schlingranke | Grasping Vine |
  | Salve beschwören | Pfeilsalve beschwören | Conjure Volley |
- [x] **4 Zauber aus Spellbook.md fehlen komplett in spellbook.json:** Weder unter ihrem noch unter einem alternativen Namen auffindbar: `Donnerschritt` (Thunder Step, Stufe 3), `Aberration beschwören` (Summon Aberration, Stufe 4), `Geistige Trübung` (Befuddlement, Stufe 8), `Schwächere Genesung` (Lesser Restoration, Stufe 2). Für Delat und Athania aktuell nicht kritisch, da sie diese Zauber nicht haben — aber für Vollständigkeit der App-Datenbank relevant.

### G. stats.json — Inkonsistenzen Delat & Athania

- [x] **Delat: Current HP (55) überschreitet Max HP (43):** `current: 55` bei `max: 43` und `temp: 12` bedeutet, dass Temp HP in die Current HP eingerechnet wurden. Korrigiert: `current: 43`.
- [x] **Delat: Macht der Tiefe noch in classFeatures:** Stufe-10-Feature des Patrons der Großen Alten entfernt aus stats.json.
- [x] **Delat: Sprachen ohne Trennzeichen:** `"GemeinspracheElfischZwergisch"` → `"Gemeinsprache, Elfisch, Zwergisch"`.
- [x] **Delat: Furcht + Spiegelbilder ohne `source`-Feld:** `source: ""` → `source: "Hexenmeister"` für beide.
- [x] **Athania: Kampfstab-Schaden "1W4"** → `"1W6 Wucht"` in stats.json korrigiert.
- [x] **Athania: Dornenhagel `castingTime: "Reaktion"` falsch:** → `"BA"` korrigiert.

### H. urtier.json — Strukturproblem

- [x] **Attribut-Keys Deutsch vs. Englisch (Inkonsistenz mit stats.json):** `STA→STR, GES→DEX, KON→CON, WEI→WIS` in urtier.json vereinheitlicht.
- [x] **HP und RK als Freitext (nicht berechnet):** Z. B. `"trefferpunkte": "5 plus das Fünffache deiner Waldläuferstufe"` und `"ruestungsklasse": "13 plus dein Weisheitsmodifikator"`. Die App muss diese Formeln zur Laufzeit parsen/berechnen. Besser wäre ein berechneter Wert + eine `formula`-Eigenschaft für den Hinweistext.

### I. vertrauter.json — Strukturproblem

- [x] **Attribut-Keys Deutsch (STA, GES, KON, WEI):** `STA→STR, GES→DEX, KON→CON, WEI→WIS` in vertrauter.json vereinheitlicht.
- [x] **`resistenzen` und `fertigkeiten` als String statt Array:** Beide Felder zu Arrays konvertiert.

### J. freespells.md — Kein Handlungsbedarf (Dokumentation)

- **Keine JSON-Übertragung notwendig.** `freespells.md` ist eine Entwicklerdokumentation, die die Logik für slot-freies Zaubern (Rituale, Fähigkeiten, Gegenstände) als pseudocode beschreibt. Diese Logik gehört in den App-Code, nicht in eine JSON-Datei. Die drei Regeln sind bereits im RAG-Kontext durch `rules.json` und die Merkmale abgedeckt.