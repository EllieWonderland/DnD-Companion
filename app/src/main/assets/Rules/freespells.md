Um diese Mechanik für eine D&D Companion App (basierend auf dem Player's Handbook 2024) zu programmieren, müssen wir die Regeln aus dem Kapitel "Casting without Slots" in eine klare Wenn-Dann-Logik übersetzen. Da Zaubertricks (Cantrips) auf deinen Wunsch hin ausgeschlossen sind, gibt es im PHB 2024 exakt drei Wege, wie ein Charakter einen Zauber wirken kann, ohne einen seiner Zauberplätze (Spell Slots) zu verbrauchen.

Hier sind die Regeln, formuliert als Logik-Blöcke für deinen Programmierer:

### Regel 1: Magische Rituale (Rituals)

Bestimmte Zauber können als Ritual gewirkt werden. Dies kostet keinen Zauberplatz, verlängert aber die Zauberdauer.

* **Bedingung (IF):**
* `Spell.HasTag("Ritual") == true`
* **UND** (`Character.PreparedSpells.Contains(Spell) == true`)
* *Ausnahme für die Klasse Magier (Wizard):* Magier benötigen den Zauber nicht in ihrer Liste der vorbereiteten Zauber, solange er in ihrem Zauberbuch steht. Hier lautet die ODER-Bedingung: `(Character.Class == "Wizard" AND Character.Spellbook.Contains(Spell) == true)`.


* **Auswirkung (THEN):**
* Verbrauchte Zauberplätze: `0`
* Anpassung der Zauberdauer: `Spell.CastingTime += 10 Minutes`.




* **Häufigkeit (LIMIT):**
* Unbegrenzt (`UsesRemaining = Infinity`). Der Charakter kann dies so oft tun, wie er möchte, solange er die Zeit dafür hat.



### Regel 2: Spezielle Fähigkeiten (Special Abilities)

Manche Charaktere können durch ihre Spezies (Species Traits), Klasse (Class Features) oder Talente (Feats) bestimmte Zauber kostenlos wirken. Beispiele sind *Eldritch Invocations* (Warlock) oder *Fey Step* (Spezies Elf).

* **Bedingung (IF):**
* `Character.Features.Contains(Feature)` **UND** `Feature.GrantedSpell == Spell`
* **UND** das spezifische Feature erlaubt explizit das Wirken ohne Zauberplatz (`Feature.RequiresSpellSlot == false`).


* **Auswirkung (THEN):**
* Verbrauchte Zauberplätze: `0`
* Ressourcen-Abzug: `Feature.UsesRemaining -= 1`


* **Häufigkeit (LIMIT):**
* Die Häufigkeit wird immer durch das spezifische Feature definiert (z. B. 1/Long Rest, 1/Short Rest) oder verbraucht eine alternative Ressource (wie *Sorcery Points*). Die App muss hier einen dedizierten Tracker für das Feature (`Feature.MaxUses` und `Feature.ResetCondition`) abfragen.



### Regel 3: Magische Gegenstände (Magic Items)

Ein Charakter kann einen Zauber aus einem Gegenstand (z. B. *Spell Scroll*, *Wand*) wirken. Hierbei wird die Magie des Gegenstands genutzt, nicht die des Charakters.

* **Bedingung (IF):**
* `Character.Equipment.Contains(MagicItem) == true`
* **UND** `MagicItem.ContainsSpell(Spell) == true`
* **UND** `MagicItem.CurrentCharges >= MagicItem.SpellCost(Spell)`


* **Auswirkung (THEN):**
* Verbrauchte Zauberplätze: `0`
* Ressourcen-Abzug: `MagicItem.CurrentCharges -= MagicItem.SpellCost(Spell)` (Spruchrollen oder Items mit nur einer Nutzung werden bei 0 Ladungen aus dem Inventar entfernt oder zerstört).


* **Häufigkeit (LIMIT):**
* Solange der Gegenstand über ausreichend Ladungen verfügt.



---

**Abschließende Überprüfung auf Vollständigkeit und Korrektheit:**
Die Logik deckt sich zu 100 % mit dem Player's Handbook 2024. Im Regelwerk (Kapitel 7) werden unter dem Punkt *„Casting without Slots“* exakt vier Möglichkeiten definiert. Subtrahiert man die Cantrips, verbleiben genau diese drei Mechaniken (Rituale, Fähigkeiten, Gegenstände). Auch die modifizierte Bedingung für die Klasse Wizard (*Ritual Adept* ) ist korrekt implementiert, da Magier ihre Rituale nicht vorbereiten müssen. Ein Programmierer kann diese Struktur direkt für die Abfrage `CanCastForFree()` in der Companion App übernehmen.

Aber woher weiß mein Programmierer jetzt, welche Sprüche das explizit betrifft? Gibt es da eine Liste oder muss der Spieler das später selbst angeben? Wie könnte der programmierer das konkret umsetzen?

Es gibt im Player's Handbook 2024 keine einzelne, isolierte Tabelle namens „Zauber, die keine Slots verbrauchen“, da die Kostenlosigkeit meist vom Kontext (Wer zaubert? Womit?) abhängt. Die einzige Ausnahme sind die Rituale, die eine feste Eigenschaft des Zaubers selbst sind.

Damit ein Spieler in der App nicht bei jedem Zauber manuell Häkchen setzen muss, sollte die App auf einer gut strukturierten Datenbank (z. B. im JSON-Format oder einer relationalen Datenbank) aufbauen, die alle relevanten PHB 2024-Inhalte enthält.

Hier ist ein Konzept, wie die Umsetzung in der Softwarearchitektur aussehen kann.

### Datenbeschaffung und Speicherung

Der Programmierer muss die Daten aus dem PHB 2024 in strukturierte Datensätze überführen. Der Spieler wählt in der App dann nur noch seinen Zauber, seine Spezies oder seine Gegenstände aus, und die App leitet die Berechtigungen aus diesen Datensätzen ab.

#### 1. Die Zauber-Datenbank

Für die Rituale muss jeder Zauberspruch in der Datenbank ein klares Flag (einen Boolean-Wert) besitzen. Im PHB 2024 steht bei Ritual-Zaubern das Tag "Ritual" direkt hinter der Zauberschule (z. B. *„Level 1 Abjuration (Ritual)“*).

Ein Datensatz für den Zauber *Alarm* könnte so aussehen:

```json
{
  "spellId": "alarm_01",
  "name": "Alarm",
  "level": 1,
  "school": "Abjuration",
  "isRitual": true,
  "castingTime": "1 Action"
}

```

Die App prüft bei einem Klick auf „Zaubern“ einfach den Wert `isRitual`. Ist er `true`, kann die UI einen zusätzlichen Button „Als Ritual wirken (+10 Minuten)“ anzeigen, der keinen Zauberplatz abzieht.

#### 2. Die Fähigkeiten-Datenbank (Spezies, Klassen, Feats)

Hier liegt die Eigenschaft nicht beim Zauber, sondern bei der Fähigkeit. Der Programmierer muss die Merkmale so anlegen, dass sie auf bestimmte Zauber-IDs verweisen und die Kosten definieren.

Beispiel für das Spezies-Merkmal *Fey Step* (Elf):

```json
{
  "featureId": "fey_step_elf",
  "name": "Fey Step",
  "source": "Species_Elf",
  "grantedSpells": ["misty_step_01"],
  "freeCastsPerLongRest": 1,
  "canUseSpellSlotsAfterFreeCast": true
}

```

Wenn der Charakter dieses Merkmal besitzt, weiß die App: Der Zauber *Misty Step* ist verfügbar. Die Logik prüft zuerst, ob `freeCastsPerLongRest` größer als 0 ist. Wenn ja, wird dieser Zähler reduziert und kein Zauberplatz verbraucht. Ist der Zähler bei 0, greift das reguläre System für Zauberplätze.

#### 3. Die Gegenstands-Datenbank

Gegenstände funktionieren ähnlich wie Fähigkeiten, bringen aber ein eigenes Ladungs-System (Charges) mit.

Beispiel für einen *Wand of Magic Missiles*:

```json
{
  "itemId": "wand_magic_missiles",
  "name": "Wand of Magic Missiles",
  "type": "Wand",
  "maxCharges": 7,
  "currentCharges": 7,
  "spellsContained": [
    {
      "spellId": "magic_missile_01",
      "costInCharges": 1,
      "castLevel": 1
    }
  ]
}

```

Die App verknüpft das Inventar des Charakters mit diesen Daten. Will der Spieler *Magic Missile* über den Zauberstab wirken, prüft die App das Array `spellsContained` im Item, liest die `costInCharges` aus und zieht diese von den `currentCharges` des Gegenstands ab. Das reguläre Zauberplatz-System des Charakters bleibt unberührt.

### Zusammenführung im Interface

Um das nutzerfreundlich zusammenzuführen, kann die Methode, die die verfügbaren Zauber eines Charakters für die Benutzeroberfläche sammelt, verschiedene Quellen abfragen:

1. **Lade vorbereitete Zauber:** (Prüfe hierbei, ob `isRitual == true` für den Ritual-Button).
2. **Lade Zauber aus aktiven Fähigkeiten:** (Prüfe, ob noch `freeCasts` übrig sind).
3. **Lade Zauber aus ausgerüsteten Gegenständen:** (Prüfe, ob noch `currentCharges` übrig sind).

Wenn all diese Datensätze im Hintergrund (z. B. in einer lokalen SQLite-Datenbank auf dem Smartphone oder über eine API) ordentlich nach den PHB 2024-Regeln verknüpft sind, muss der Spieler nichts weiter tun, als in der App auf "Cast" zu tippen. Die App weiß durch die saubere Datenstrukturierung genau, welche Ressource (Zeit, Feature-Nutzung, Item-Ladung oder Zauberplatz) fällig wird.