# Kapitel 7: Zauber (Spells)

## 1. Grundlagen der Magie

### Zauberstufen (Spell Levels)
Jeder Zauber hat eine Stufe von 0 bis 9.
* **Stufe 0:** Diese Zauber werden als **Zaubertricks (Cantrips)** bezeichnet. Sie verbrauchen keine Zauberplätze und können beliebig oft gewirkt werden, sobald ein Charakter sie erlernt hat.
* **Stufen 1 bis 9:** Um einen dieser Zauber zu wirken, muss ein Charakter einen **Zauberplatz (Spell Slot)** der entsprechenden oder einer höheren Stufe ausgeben.

### Zauberplätze (Spell Slots)
* **Verbrauch:** Das Wirken eines Zaubers (Stufe 1+) verbraucht einen Zauberplatz. 
* **Hochzaubern (Upcasting):** Wird ein Zauber mit einem Platz einer höheren Stufe gewirkt, nimmt er für diesen Moment diese höhere Stufe an. Viele Zauber haben Zusatzeffekte (meist mehr Schaden oder mehr Ziele), wenn sie mit höheren Plätzen gewirkt werden.
* **Regeneration:** Verbrauchte Zauberplätze werden in der Regel nach Abschluss einer **Langen Rast** (Long Rest) vollständig wiederhergestellt. Manche Klassen (wie der Hexenmeister) regenerieren sie bei einer Kurzen Rast.

### Zauber-Rettungswurf-SG und Angriffsbonus
Wenn ein Zauber einen Angriffswurf oder einen Rettungswurf erfordert, basieren diese auf dem Zauberattribut der Klasse, die den Zauber gewährt.
* **Zauber-Rettungswurf-SG:** 8 + Übungsbonus + Zauberattributs-Modifikator
* **Zauber-Angriffsbonus:** Übungsbonus + Zauberattributs-Modifikator

---

## 2. Parameter eines Zaubers

Jeder Zauber ist durch spezifische Parameter definiert, die für die App-Logik strikt eingehalten werden müssen.

### Zauberzeit (Casting Time)
* **Aktion:** Die häufigste Zauberzeit. Im Kampf erfordert dies die *Magische Aktion*.
* **Bonusaktion:** Ein Zauber mit dieser Zauberzeit ist besonders schnell. *WICHTIGE 2024-REGEL:* Wenn du in deinem Zug einen Zauber als Bonusaktion wirkst, darfst du in demselben Zug nur noch einen einzigen weiteren Zauber wirken, und dieser **muss** ein Zaubertrick (Cantrip) mit einer Zauberzeit von 1 Aktion sein.
* **Reaktion:** Wird im Bruchteil einer Sekunde als Antwort auf einen spezifischen Auslöser gewirkt (z.B. *Schild* oder *Gegenzauber*).
* **Längere Zeiten:** Manche Zauber benötigen Minuten oder Stunden. Dies erfordert im Kampf die Konzentration für die gesamte Dauer des Wirkens.

### Rituale (Rituals)
* Einige Zauber tragen das Tag **(Ritual)**.
* **2024-Regel:** JEDER Charakter, der einen Ritual-Zauber vorbereitet (oder in seinem Zauberbuch hat, falls Magier), kann ihn als Ritual wirken. Es wird kein spezielles "Ritualzauberer"-Klassenmerkmal mehr benötigt.
* Das Wirken als Ritual erhöht die normale Zauberzeit um **10 Minuten**, verbraucht dafür aber **keinen Zauberplatz**.

### Reichweite (Range)
* **Selbst (Self):** Der Zauber wirkt nur auf den Zaubernden selbst oder hat seinen Ursprung beim Zaubernden.
* **Berührung (Touch):** Der Zaubernde muss das Ziel physisch berühren.
* **Entfernung (Fernkampf):** Angegeben in Fuß (z.B. 30, 60, 120). Du musst das Ziel sehen können, es sei denn, der Zauber sagt etwas anderes. Eine direkte, ununterbrochene Linie (Line of Effect) ist zwingend erforderlich (Totale Deckung blockiert Zauber).

### Komponenten (Components)
* **Verbal (V):** Erfordert das laute Sprechen mystischer Worte. Kann nicht gewirkt werden, wenn der Zaubernde geknebelt ist oder sich im Wirkungsbereich von *Stille* befindet.
* **Somatisch (S):** Erfordert freie Handbewegungen. Der Zaubernde benötigt mindestens eine freie Hand.
* **Material (M):** Erfordert bestimmte Gegenstände. 
  * *Fokus-Nutzung:* Ein Material, das keine Goldkosten hat und nicht vom Zauber verbraucht wird, kann durch einen **Zauberfokus** (Arkaner Fokus, Heiliges Symbol, Druidenfokus etc.) oder einen **Komponentenbeutel** ersetzt werden.
  * *Hand-Nutzung (2024-Regel):* Die Hand, die für die Materialkomponente oder den Fokus genutzt wird, **darf dieselbe Hand sein**, die für die Somatische (S) Komponente genutzt wird. Hat der Zauber jedoch **nur (S)** und **kein (M)**, MUSS die Hand komplett leer sein (kein Fokus).
  * *Kosten / Verbrauch:* Hat die Materialkomponente einen Preis in Gold (z.B. ein Diamant im Wert von 300 GM) oder gibt der Zauber an, dass sie "verbraucht" wird, MUSS die spezifische Komponente vorhanden sein. Ein Fokus kann sie nicht ersetzen.

### Wirkungsdauer (Duration)
* **Spontan (Instantaneous):** Die Magie geschieht im Bruchteil einer Sekunde und verschwindet sofort (z.B. *Feuerball*).
* **Festgelegte Zeit:** Der Zauber hält für die angegebene Zeit (Runden, Minuten, Stunden, Tage) an. Der Zaubernde kann einen solchen Zauber jederzeit (keine Aktion erforderlich) vorzeitig beenden.
* **Konzentration (Concentration):** Der Zauber erfordert mentale Fokussierung. Du kannst immer **nur einen** Konzentrations-Zauber gleichzeitig aufrechterhalten. Konzentration bricht in folgenden Fällen:
  1. **Anderer Zauber:** Du wirkst einen anderen Zauber, der Konzentration erfordert.
  2. **Schaden erleiden:** Du erleidest Schaden. Du musst einen Konstitutions-Rettungswurf ablegen. Der SG ist **10 oder die Hälfte des erlittenen Schadens** (was auch immer höher ist). Bei einem Fehlschlag endet der Zauber.
  3. **Kampfunfähigkeit / Tod:** Du bist *Kampfunfähig* (Incapacitated) oder stirbst.

---

## 3. Wirkungsbereiche (Areas of Effect)

Ein Flächenzauber erfasst alle Kreaturen und Objekte in seinem Bereich. Der Ursprungspunkt (Point of Origin) ist bei einigen Formen inkludiert, bei anderen nicht. Totale Deckung blockiert den Wirkungsbereich.

* **Kegel (Cone):** Erstreckt sich in eine Richtung vom Zaubernden weg. Die Breite an einem beliebigen Punkt entspricht der Entfernung zum Ursprungspunkt. Ursprungspunkt wird nicht mit in die Fläche einbezogen (es sei denn, der Zaubernde will es).
* **Würfel (Cube):** Die Größe wird durch die Länge einer Kante angegeben. Der Ursprungspunkt liegt auf einer beliebigen Seite des Würfels.
* **Zylinder (Cylinder):** Ein Kreis mit einem definierten Radius auf dem Boden, der sich um eine festgelegte Höhe nach oben erstreckt. Der Ursprungspunkt ist das Zentrum des Kreises.
* **Linie (Line):** Eine gerade Linie definierter Länge und Breite.
* **Sphäre (Sphere):** Erstreckt sich vom Ursprungspunkt in alle Richtungen. Die Größe wird als Radius ausgedrückt.
* **Emanation (Neue 2024-Regel):** Ersetzt viele alte "Aura"-Effekte. Eine Emanation erstreckt sich in einem definierten Radius **um eine Kreatur oder ein Objekt** herum und bewegt sich mit diesem Ursprung mit. Der Bereich der Kreatur/des Objekts selbst ist **nicht** Teil der Emanation, es sei denn, der Zauber gibt dies explizit an.

---

## 4. Wichtige Anpassungen in der App-Logik (2024 Edition)

Für die Berechnung und das Regel-Enforcement innerhalb deiner App solltest du diese Änderungen zur Version 2014 zwingend beachten:

1. **Heilmagie & Untote/Konstrukte:** Zauber wie *Wunden heilen* (Cure Wounds) oder *Heilendes Wort* (Healing Word) funktionieren in den 2024-Regeln nun problemlos bei Untoten und Konstrukten (es sei denn, der Zauber schließt sie explizit aus).
2. **Gegenzauber (Counterspell):** Funktioniert jetzt als Konstitutions-Rettungswurf für das Ziel, anstatt eines Attributswurfs für den Ausführenden. Wirft das Ziel den Save erfolgreich, wird der Zauber gewirkt, ABER der Zauberplatz für den Gegenzauber wird nicht verbraucht.
3. **Göttliches Niederstrecken (Divine Smite):** Ist jetzt ein regulärer Paladin-Zauber der Stufe 1 (Bonusaktion, verbal, material). Er unterliegt damit allen Regeln für Zauber (inklusive *Gegenzauber* und der Regel für Bonusaktions-Zauber).
4. **Unsichtbarkeit (Invisibility Condition):** Angriffe aus der Unsichtbarkeit haben immer Vorteil, Angriffe gegen Unsichtbare haben immer Nachteil. Das gilt in 2024 selbst dann, wenn der Gegner Blindsicht (Blindsight) oder Wahren Blick (Truesight) hat! (Die Sichtbarkeits-Erkennung negiert nur das "Versteckt"-Sein, nicht die mechanischen Kampfboni des Zustands "Unsichtbar").