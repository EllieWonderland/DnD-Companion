# Offene Aufgaben

## Bug 1 – Ausrüstungskatalog: Kategorien doppelt
Im Ausrüstungskatalog im Rucksack sind alle Kategorien doppelt vertreten.
**Status:** Erledigt

---

## Bug 2 – Athania: Falscher Name bei kostenlosem Zauber
Bei Athania steht im Zauber-Tab unter „Kostenlose Zauber" noch „Erzfeind" – korrekt wäre „Zeichen des Jägers".
**Status:** Erledigt

---

## Bug/Feature 3 – Paktmagie & Kostenlose Zauber: Automatisierung unvollständig
Probleme:
- Bei Delat taucht „Schild" trotz Auswahl nicht in den kostenlosen Zaubern auf.
- „Magierrüstung" verschwindet trotz Abwahl nicht aus Paktmagie.
- Zauber, die zur Paktmagie gehören, müssen automatisch im Paktmagie-Feld erscheinen.
- Zauber, die kostenlos durch Talente oder Gegenstände gewirkt werden können, müssen automatisch
  im zugehörigen Feld erscheinen (z. B. bei Athania im Feld „Zauberplätze Grad 1"), damit sie von
  dort aus gewirkt werden können und der Zähler korrekt arbeitet.
- Wenn durch Level Up weitere Zauberplätze oder andere Slots hinzukommen, muss dies ebenfalls
  automatisch geregelt werden.

Aufgabe: Mechanik für beide Charaktere (Athania & Delat) vollständig und korrekt automatisieren.
Ggf. assets/rules prüfen für die zugrunde liegenden Regeln.
**Status:** Erledigt

## Schritt 4 - Preise im Inventar anzeigen
[x] RucksackScreen.kt: Item-Karte zeigt price-String aus EquipmentEntity.
**Status:** Erledigt
*Commit-Notiz:* Feat: Display equipment prices in RucksackScreen (InventoryItem price property, CharacterViewModel catalog transfer, InventoryRow UI display)


## Feature - Gegenst�nde kaufen & Feedback
- Feedback beim Hinzuf�gen (�ber snackbarMessage) ist sowohl beim Katalog als auch in der Eingabemaske voll verkn�pft.
- Neuer Kaufen-Button im Katalog ('Einkaufswagen') zieht bei Erfolg den Betrag optimal gewechselt ab (in PM, GM, SM, KM).
**Status:** Erledigt
*Commit-Notiz:* Feat: Add logic to buy items from catalog including exact coin change deduction

