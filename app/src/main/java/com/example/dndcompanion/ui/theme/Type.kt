package com.example.dndcompanion.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.dndcompanion.R

// Fantasy-Schriftarten
val MedievalSharp = FontFamily(
    Font(R.font.medievalsharp, FontWeight.Normal)
)

val Almendra = FontFamily(
    Font(R.font.almendra_regular, FontWeight.Normal),
    Font(R.font.almendra_bold, FontWeight.Bold)
)

val GrenzeGotisch = FontFamily(
    Font(R.font.grenze_gotisch, FontWeight.Normal)
)

// Material Typography mit Fantasy-Fonts
val Typography = Typography(
    // Große Überschriften – MedievalSharp
    displayLarge = TextStyle(
        fontFamily = MedievalSharp,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
        color = TintenSchwarz
    ),
    displayMedium = TextStyle(
        fontFamily = MedievalSharp,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
        color = TintenSchwarz
    ),
    displaySmall = TextStyle(
        fontFamily = MedievalSharp,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        color = TintenSchwarz
    ),

    // Titel – MedievalSharp
    titleLarge = TextStyle(
        fontFamily = MedievalSharp,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = TintenSchwarz
    ),
    titleMedium = TextStyle(
        fontFamily = MedievalSharp,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
        color = TintenSchwarz
    ),
    titleSmall = TextStyle(
        fontFamily = MedievalSharp,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = TintenSchwarz
    ),

    // Body-Text – Almendra
    bodyLarge = TextStyle(
        fontFamily = Almendra,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 27.sp,
        letterSpacing = 0.5.sp,
        color = TintenSchwarz
    ),
    bodyMedium = TextStyle(
        fontFamily = Almendra,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.25.sp,
        color = TintenSchwarz
    ),
    bodySmall = TextStyle(
        fontFamily = Almendra,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.4.sp,
        color = TintenSchwarz // Von TintenBraun auf Schwarz für besseren Kontrast
    ),

    // Labels – Almendra
    labelLarge = TextStyle(
        fontFamily = Almendra,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
        color = TintenSchwarz
    ),
    labelMedium = TextStyle(
        fontFamily = Almendra,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.5.sp,
        color = TintenSchwarz
    ),
    labelSmall = TextStyle(
        fontFamily = Almendra,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.5.sp,
        color = TintenBraun
    ),

    // Headlines – Almendra Bold
    headlineLarge = TextStyle(
        fontFamily = Almendra,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
        color = TintenSchwarz
    ),
    headlineMedium = TextStyle(
        fontFamily = Almendra,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
        color = TintenSchwarz
    ),
    headlineSmall = TextStyle(
        fontFamily = Almendra,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        color = TintenSchwarz
    )
)

// Spezial-Style für Zahlenwerte (HP, Attribute, etc.) – Grenze Gotisch
val GrenzeGotischStyle = TextStyle(
    fontFamily = GrenzeGotisch,
    fontWeight = FontWeight.Normal,
    fontSize = 32.sp,
    lineHeight = 36.sp,
    letterSpacing = 0.sp,
    color = TintenSchwarz
)

val GrenzeGotischSmall = TextStyle(
    fontFamily = GrenzeGotisch,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp,
    color = TintenSchwarz
)