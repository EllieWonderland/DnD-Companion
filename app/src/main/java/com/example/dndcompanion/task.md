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
- [ ] **LLM-Kontext (RAG) erweitern (Chatbot):** 
  - Als Quelle für den Chatbot 2 Bereiche anzeigen: Oben eine Antwort anhand der eigenen Quellen (Rules/Handbuch, Rules/Zauberbuch, Charaktersheets, Stats, etc.), unten einen Bereich mit der Antwort anhand anderer Quellen (Internet, Gemini Datenbank etc.).
  - *Commit:* `git commit -m "feat: enhance chatbot with split answer sources for local and external data"`
- [ ] **Regelwerk - Bugfixes & Visuals:** 
  - Weißen Hintergrund gegen weiße Schrift auf farbigem Hintergrund tauschen.
  - Gesuchtes Wort im Text farblich/fett markieren (Highlighting).
  - *Commit:* `git commit -m "fix: rulebook apply dark theme and add text highlighting for search"`
- [ ] **Rucksack - Gewicht & Gegenstands-Auswahl:** 
  - Auf tatsächliche Gewichte aus der Datei `equipment.md` zugreifen und summiert anzeigen.
  - Neben dem freien Eingabefeld sollten Items für den Rucksack auch über eine Liste / Suche ausgewählt werden können (Daten aus `kapitel6_equipment.md` einlesen).
  - *Commit:* `git commit -m "feat: inventory weight tracking and equipment item selection from md"`
- [ ] **Levelaufstieg (Level Up) - Logik & UI:** 
  - Noch einmal auf vollständige Logik prüfen (berücksichtigt das System automatisch neue Zauberplätze, neue Zauber, Rüstungsklasse, Trefferwürfel, etc.?).
  - Das Fenster für den Level Up muss direkt im Kampf-Fenster auftauchen, sobald man aufsteigt.
  - 2 Buttons zur Wahl stellen: "Jetzt Level Up durchführen" (öffnet Profil/Dialog für Attributs-/Zauberauswahl) oder "Später erledigen" (Fenster taucht beim nächsten Öffnen des Charakter-Profils wieder auf).
  - *Commit:* `git commit -m "feat: improve level up logic and add combat screen popup notification"`
- [ ] **Bücher/Notizen in "Privat" und "Öffentlich" splitten:** Die Notizbücher (Books/Tagebücher) so umbauen, dass ein Eintrag ein Flag erhält oder es getrennte Listen für `Profil-spezifisch` und `Gruppen-Öffentlich` gibt.
  - *Commit:* `git commit -m "feat: split notebooks into private and public categories"`

## Bald (Phase 2: Fundament für Multi-Charakter)
- [ ] **CharacterViewModel abstrahieren:** Die harten Werte für "Athania" in eine flexiblere Datenklasse `CharacterData` auslagern. Das ViewModel sollte die aktuellen Daten basierend auf dem gewählten Charakter laden.
  - *Commit:* `git commit -m "refactor: abstract CharacterViewModel to use flexible CharacterData class"`
- [ ] **Ressourcen trennen:** Globale Daten (Handbuch, Zauberbuch-Definitionen, gemeinsame Notizen) werden von charakterspezifischen Instanz-Daten (Inventar, HP, Attributswerte, vorbereitete Zauber, FAQ) getrennt gespeichert. 
  - *Commit:* `git commit -m "refactor: separate global resources from character-specific instance data"`

## Später (Phase 3: Hexenmeister Delat hinzufügen)
- [ ] **Profil-Wechsler UI:** Einen Screen oder einen Button in der Navigation basteln, um in Echtzeit zwischen Athania (`Ranger`) und Delat (`Warlock`) zu wechseln.
  - *Commit:* `git commit -m "feat: implement profile switcher UI for multiple characters"`
- [ ] **Dynamisches Menü:** Je nach aktiver Klasse das Burger-Menü anpassen. Ein Ranger sieht seine `Bestie`, ein Warlock sieht `Paktmagie`-Management oder `Schauerliche Anrufungen`.
  - *Commit:* `git commit -m "feat: add dynamic navigation menu based on active character class"`
- [ ] **Warlock-Logik (Delat) hinterlegen:** Paktmagie-Slots (die nach einer kurzen Rast zurückkehren!), Zaubertricks und warlock-spezifische Eigenheiten in den Stats hinterlegen.
  - *Commit:* `git commit -m "feat: implement warlock-specific logic, pact magic and eldritch invocations"`
- [ ] **FAQ privatisieren:** FAQ für Athania und Delat getrennt laden und editierbar machen.
  - *Commit:* `git commit -m "feat: separate FAQ datastore per character profile"`
