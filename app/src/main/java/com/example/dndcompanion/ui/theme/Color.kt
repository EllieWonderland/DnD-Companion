package com.example.dndcompanion.ui.theme

import androidx.compose.ui.graphics.Color

// === Pergament & Stein – Farbpalette ===

// Pergament-Töne (Hintergrund & Karten)
val Pergament = Color(0xFFF2E6C9)          // Warmes Pergament-Beige – Seitenhintergrund
val PergamentDunkel = Color(0xFFD4C4A0)    // Abgedunkelter Rand / Kartenhintergrund
val PergamentHell = Color(0xFFFAF3E3)      // Aufgehelltes Pergament – Oberflächen, Button-Text

// Wald & Natur – Strukturfarbe (Screen-Titel, Section-Header, Heilung, HP-Balken gesund)
val Waldgruen = Color(0xFF2E5339)          // Tiefes Waldgrün
val WaldgruenDunkel = Color(0xFF1B3526)    // Sehr dunkles Grün (StatusBar, Karten-Header)
val WaldgruenHell = Color(0xFF4A7C59)      // Mittleres Grün (Temp-HP-Anzeige, Hover)

// Charakter-Akzente – ausschließlich via accentColor = MaterialTheme.colorScheme.tertiary
val OchsenblutRot = Color(0xFF8B2500)      // Athania / Ranger – primäre Aktions-Buttons
val OchsenblutRotHell = Color(0xFFB33A00)  // Athania – tertiaryContainer / Hover
val HexenLila = Color(0xFF391F4F)          // Delat / Warlock – primäre Aktions-Buttons
val HexenLilaHell = Color(0xFF5A3D80)      // Delat – tertiaryContainer / Hover

// Metall-Töne – Sekundäre UI-Elemente (Kurze Rast, Trennlinien, Rahmen)
val Bronze = Color(0xFF9C7A3C)             // Warmes Bronze
val BronzeHell = Color(0xFFBE9B5C)         // Helles Bronze (secondaryContainer)
val BronzeDunkel = Color(0xFF7A5C2A)       // Dunkles Bronze (outline, Schatten)
val EisenGrau = Color(0xFF6B6B6B)          // Deaktivierte Elemente

// Funktionsfarben – semantisch, charakterunabhängig
val TodRuneRot = Color(0xFFCC3333)         // Error-State + Todesrettung Misserfolg
val TodRuneGruen = Color(0xFF339933)       // Erfolg-State + Todesrettung Erfolg
val TintenSchwarz = Color(0xFF1A1409)      // Primärtext auf Pergament
val TintenBraun = Color(0xFF3D2B1F)        // Sekundärtext / Untertitel
