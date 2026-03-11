# ToDo & Roadmap D&D Companion

## Aktuell (Phase 1: Athania Perfektionieren)
- [x] **Fehler beheben:** Workflow `/error-fix` ausführen, Build reparieren.
  - *Commit:* `git commit -m "fix: resolve AGP 9.0 & Kotlin plugin incompatibility in gradle.properties"`
- [/] **Übungsbonus in Profil-UI anzeigen:** Den berechneten `proficiencyBonus` sichtbar im ProfilScreen einbauen.
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

## Bald (Phase 2: Fundament für Multi-Charakter)
- [x] **CharacterViewModel abstrahieren:** Die harten Werte für "Athania" in eine flexiblere Datenklasse `CharacterData` auslagern. Das ViewModel sollte die aktuellen Daten basierend auf dem gewählten Charakter laden.
  - *Commit:* `git commit -m "refactor: abstract CharacterViewModel to use flexible CharacterData class"`
- [x] **Ressourcen trennen:** Globale Daten (Handbuch, Zauberbuch-Definitionen, gemeinsame Notizen) werden von charakterspezifischen Instanz-Daten (Inventar, HP, Attributswerte, vorbereitete Zauber, FAQ) getrennt gespeichert. 
  - *Commit:* `git commit -m "refactor: separate global resources from character-specific instance data"`

## Später (Phase 3: Hexenmeister Delat hinzufügen)
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
- [ ] **Machbarkeits-Check & Assets** (Status: Teilweise möglich, Texturen/Fonts fehlen noch im Repo)
  - [ ] Vorhandene Bilder nutzen: `athania.png`, `delat.png`, `zauberbuch.png`, `regelwerk.png`, `notizbuch.png`, `grollbuch.png`.
  - [ ] Fehlende Texturen besorgen/erstellen: Pergament-Hintergrund, Steinmetz-Platten-Textur.
  - [ ] Fehlende Schriftarten (Fonts) einbinden: Fantasy-Serife, Gotisch/Runen-Type.
- [ ] **Fundament: Farbpalette und Texturen**
  - [ ] Hintergrund: Helles Gelb durch verwitterte Pergamenttextur ersetzen.
  - [ ] Farbakzente: Tiefes Waldgrün für Header, Ochsenblutrot/Dunkelbraun für Buttons/Akzente.
  - [ ] Tiefe: Dezente Schatten für übereinanderliegende Pergamentstücke und Lederriemen.
  - [ ] Profil-Karten: Attribute als Steinmetz-Platten oder Pergamentrollen mit Wachssiegeln gestalten.
- [ ] **Typografie & Schriftarten**
  - [ ] Fantasy-Schriftarten integrieren (Grenze Gotisch, Almendra und MedievalSharp von Google Fonts hinzufügen).
  - [ ] Zahlenwerte: Klobigere, graviert aussehende Schrift für HP und Attribute.
- [ ] **Ikonographie & Symbole**
  - [ ] Navigations-Icons als Holzschnitte/Tintenzeichnungen gestalten.
  - [ ] Capy-Icon: Detaillierterer Capybara-Kopf oder heraldische Pfote.
  - [ ] Attributs-Icons: Faust (STR), geflügelter Schuh (DEX), Herz (CON), Geist (INT), Auge (WIS), Maske (CHA).
- [ ] **Layout-Details & UI-Elemente**
  - [ ] Verwitterte Ränder: Unregelmäßige, zerrissene Kanten für alle Karten und Tabellen.
  - [ ] HP-Leiste: Glasampulle (Flüssigkeits-Optik) oder leuchtende, erlöschende Runen.
  - [ ] HP-Buttons: Schaltflächen als Bronze- oder Eisenplättchen-Optik.
  - [ ] Angriffs-Boxen: Gravierte Metallschilde oder Holztafeln für "Bestienschlag" etc.
  - [ ] Flexible Boxengrößen: Die Boxen sollen sich an die Textlänge anpassen.

## Phase 6: Gruppen-Features & Ausblick
- [x] **Geteilte Schatztruhe (Gruppen-Loot):** Ein Inventar, aus dem jeder Spieler in Echtzeit Gold und Items nehmen oder hineinlegen kann.
- [ ] **Echtzeit-Initiative-Tracker:** Ein eigener Kampf-Bildschirm, der die Zug-Reihenfolge live zeigt.
- [ ] **Status-Ping & SOS (Gruppen-Ansicht):** Heiler-Ansicht für HP und Zustände der Gruppe.

## Phase 7: Bugfixes
## Phase 7: Bugfixes
- [x] **Chatbot-Identität:** Der Chatbot nennt den Charakter manchmal noch "Athania", auch wenn Delat aktiv ist.
- [x] **Temporäre HP Button:** Der Zauber/das Merkmal "Unholde Vitalität" (False Life) für Delat sollte einen direkt nutzbaren Button haben, um die 12 Temp HP (oder 1W8+4 nach 2024er Regeln) schnell zu erneuern.



