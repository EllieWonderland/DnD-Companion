package com.example.dndcompanion.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dndcompanion.R

/**
 * Hintergrund-Composable mit Pergament-Textur.
 * Wird als Wrapper für jeden Screen verwendet.
 */
@Composable
fun PergamentBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.pergament_hintergrund),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        content()
    }
}

/**
 * Standard-Karte im Pergament-Stil.
 * Verwendet als grundlegendes Card-Element im neuen Design.
 */
@Composable
fun PergamentCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(12.dp),
                ambientColor = BronzeDunkel,
                spotColor = BronzeDunkel
            )
            .border(
                width = 1.dp,
                color = PergamentDunkel,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = PergamentHell
        )
    ) {
        content()
    }
}

/**
 * Steinplatten-Card für Attributs-Boxen.
 * Hat einen dunkleren, massiveren Look.
 */
@Composable
fun SteinCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(8.dp),
                ambientColor = Color(0xFF3A3A3A),
                spotColor = Color(0xFF3A3A3A)
            )
            .border(
                width = 2.dp,
                color = EisenGrau,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFBEB5A0) // Steingrau mit warmer Tendenz
        )
    ) {
        content()
    }
}

/**
 * Bronze-Button-Style: Metallisch-warmer Button.
 */
@Composable
fun MetallButtonColors() = androidx.compose.material3.ButtonDefaults.buttonColors(
    containerColor = Bronze,
    contentColor = Color.White,
    disabledContainerColor = EisenGrau,
    disabledContentColor = Color.White.copy(alpha = 0.6f)
)
