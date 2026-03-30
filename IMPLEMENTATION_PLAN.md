# IMPLEMENTATION_PLAN.md
# D&D Companion App – Vollständige JSON-Integration

---

## 1. Architektur-Übersicht

Die App hat eine solidere Basis als zunächst angenommen. Kern-Spielmechaniken (Short Rest, Feat-Auswahl, Gewichts-Tracking) sind bereits korrekt umgesetzt. Die verbleibenden Lücken sind **gezielt und überschaubar**: fehlende UI-Verbindungen zu vorhandener Logik, ein fehlender DAO-Query und ein komplett fehlendes Cross-Reference-System.

---

## 2. Betroffene Dateien

### Zu ändern:
| Datei | Änderung |
|---|---|
| `data/database/RulebookDao.kt` | Neue Query: Zauber nach Klasse filtern |
| `ui/screens/ZauberScreen.kt` | Klassen-Filter-Toggle einbauen |
| `ui/screens/ProfilScreen.kt` | Unterklassen-Name als klickbaren Link zur Detailansicht |
| `ui/screens/BucherRulebookDetailView.kt` | Unterklassen-Detailansicht; verlinkbare Regeln |
| `ui/screens/RucksackScreen.kt` | Preise aus `equipment.json` anzeigen |
| `data/database/RuleEntity.kt` | `linkedRuleIds`-Feld ergänzen |
| `data/database/AppDatabaseCallback.kt` | Cross-References beim DB-Befüllen generieren |

### Neu zu erstellen:
| Datei | Zweck |
|---|---|
| `ui/screens/CompanionSelectionDialog.kt` | UI für Tierart-Wechsel (toggleBeastType() ist vorhanden, aber kein Dialog) |

---

## 3. Schritt-für-Schritt-Umsetzung

---

### Schritt 1 – Zauberlisten-Filter nach Klasse *(Priorität: Hoch)*

**Was fehlt (präzise):**
- `RulebookDao` hat keine Query, die nach Klasse filtert
- `ZauberScreen` hat keinen Filter-Toggle

**Umsetzung:**
1. `RulebookDao.kt`: neue Query ergänzen:
   ```sql
   SELECT * FROM spells WHERE classes LIKE '%' || :className || '%'
   ```
2. `SpellViewModel.kt`: neues StateFlow `classFilterEnabled: Boolean` (default `true`); `loadSpells()` nutzt die neue Query wenn aktiviert
3. `ZauberScreen.kt`: Toggle-Chip "Nur Klassenzauber" in der Such-/Filter-Leiste; bindet `classFilterEnabled` ans ViewModel

**Testbar:** Athania sieht nur Ranger-Zauber; Toggle deaktivieren → alle Zauber sichtbar; Delat sieht nur Warlock-Zauber.

---

### Schritt 2 – Unterklasse als klickbarer Link *(Priorität: Niedrig)*

**Was fehlt (präzise):**
- Unterklassen-Name ist nicht klickbar / führt zu keiner Detailansicht

**Umsetzung:**
1. `ProfilScreen.kt`: Unterklassen-Text als klickbares `TextButton` oder unterstrichener Link
2. `BucherRulebookDetailView.kt`: Unterklassen-Detailansicht ergänzen – zeigt Subclass-Features aus `CharacterOptionEntity.Subclass` mit Level-Anforderungen

**Testbar:** Auf Unterklasse "Herrin der Tiere" in Athania's Profil tippen → Detailansicht mit allen Subklassen-Features öffnet sich.

---

### Schritt 3 – Begleiter-Auswahl UI *(Priorität: Mittel)*

**Was fehlt (präzise):**
- Es gibt keinen Dialog oder UI-Element, über das der Spieler die Tierart wechseln kann

**Umsetzung:**
1. `CompanionSelectionDialog.kt` erstellen: zeigt alle 3 Urtier-Formen (Himmel/Land/See) als wählbare Karten mit Kurzinfos (Bewegungsrate, Hauptangriff)
2. Im Begleiter-Screen/-Tab: "Tierart wechseln"-Button, der den Dialog öffnet und `toggleBeastType()` aufruft
3. Bei Warlock: Button ausblenden (Sphinx ist fix; Erweiterung auf weitere Formen als zukünftiger Schritt)

**Testbar:** Athania → Begleiter-Tab → "Tierart wechseln" → Dialog zeigt 3 Optionen; Auswahl wechselt die angezeigte Stat-Karte; nach App-Neustart bleibt die Auswahl erhalten.

---

### Schritt 4 – Preise im Inventar anzeigen *(Priorität: Niedrig)*

**Was fehlt (präzise):**
- Preise werden nirgends in `RucksackScreen` angezeigt

**Umsetzung:**
1. `RucksackScreen.kt`: in der Item-Karte (neben Name und Gewicht) den `price`-String aus `EquipmentEntity` anzeigen
2. Kein Parsing/Summenrechnung nötig – einfache String-Anzeige reicht (z.B. "5 GM")

**Testbar:** Inventar öffnen → jedes Item zeigt seinen Preis aus `equipment.json` neben dem Gewicht.

---

### Schritt 5 – Regelkreuz-Verlinkung *(Priorität: Niedrig)*

**Was fehlt (präzise):**
- `RuleEntity` hat **kein** `linkedRuleIds`-Feld
- `RulebookDao` hat keine Query für verknüpfte Regeln
- `BucherRulebookDetailView` zeigt keine verwandten Regeln
- Zustände in `CombatScreen` zeigen keinen Regeltext

**Umsetzung:**
1. `RuleEntity.kt`: Feld `linkedRuleIds: String` ergänzen (kommaseparierte IDs)
2. `AppDatabaseCallback.kt`: beim DB-Befüllen automatische Links generieren – alle Regeln mit gemeinsamem Tag (z.B. `"condition"`) werden untereinander verlinkt
3. `RulebookDao.kt`: Query `getRulesByIds(ids: List<String>)` ergänzen
4. `BucherRulebookDetailView.kt`: "Verwandte Regeln"-Sektion am Ende jeder Regelkarte; Klick öffnet die verlinkte Regel
5. `CombatScreen.kt`: beim Hinzufügen eines Zustands (z.B. "Betäubt") Info-Karte mit Regeltext einblenden (Lookup via `title LIKE '%Betäubt%'`)

**Testbar:** In Bücher-Tab eine Zustandsregel öffnen → verwandte Zustände erscheinen als verlinkter Abschnitt; In Kampf Zustand markieren → Regeltext als Tooltip sichtbar.

---

## 4. Edge Cases & Abhängigkeiten

### Reihenfolge

Schritt 2, 5, 6, 7 sind **vollständig unabhängig voneinander** und können parallel oder in beliebiger Reihenfolge umgesetzt werden.

Schritt 8 hat eine **interne Abhängigkeit**: `RuleEntity`-Migration muss vor der `AppDatabaseCallback`-Änderung und der UI-Änderung erfolgen.

```
Schritt 2 (Spell-Filter)        ← unabhängig
Schritt 5 (Unterklassen-Link)   ← unabhängig
Schritt 6 (Begleiter-UI)        ← unabhängig, nutzt vorhandene ViewModel-Logik
Schritt 7 (Preise)              ← unabhängig, nur UI-Ergänzung
Schritt 8 (Regellinks):
    → erst RuleEntity migrieren
    → dann AppDatabaseCallback erweitern
    → dann BucherRulebookDetailView & CombatScreen
```

### Android-spezifische Stolperfallen

| Thema | Problem | Lösung |
|---|---|---|
| Room DB-Migration (Schritt 8) | Neues `linkedRuleIds`-Feld in `RuleEntity` bricht bestehende DB | `MIGRATION_X_Y` mit `ALTER TABLE rules ADD COLUMN linkedRuleIds TEXT NOT NULL DEFAULT ''` |
| LIKE-Query mit Listen (Schritt 2) | `classes` ist als `List<String>` im Speicher, aber in der DB als kommaseparierter String (TypeConverter) | Sicherstellen, dass der TypeConverter `List<String>` → `"Waldläufer,Kleriker"` schreibt und die LIKE-Query dieses Format erwartet |
| `toggleBeastType()` vs. Dialog (Schritt 6) | `toggleBeastType()` rotiert nur zyklisch (Land→Sky→Sea); Dialog braucht direkten Zugriff auf alle 3 Optionen als Liste | `urtier.json`-Namen direkt aus dem geladenen `companionData`-State lesen oder zusätzliche ViewModel-Funktion `setBeastType(BeastType)` ergänzen |
| Preis-String (Schritt 7) | `price`-Feld ist ein Rohstring ("5 GM") – kein Int | Einfach als String anzeigen, keine Berechnung; Parsing nur wenn später eine Gesamtwert-Anzeige gewünscht wird |
