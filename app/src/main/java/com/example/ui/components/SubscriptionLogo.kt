package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SubscriptionLogo(title: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val drawableName = remember(title) {
        val lower = title.lowercase()
        when {
            lower.contains("netflix") -> "netflix"
            lower.contains("disney") -> "disney"
            lower.contains("hbo") || lower.contains("max") -> "icons8_hbo_max_100"
            lower.contains("spotify") -> "spotify"
            lower.contains("youtube_music") -> "yt_music"
            lower.contains("youtube") -> "youtube"
            lower.contains("amazon") || lower.contains("prime") -> "prime"
            lower.contains("playstation") || lower.contains("juegos") || lower.contains("psn") -> "icons8_playstation_96"
            else -> ""
        }
    }

    val resourceId = remember(drawableName) {
        if (drawableName.isNotEmpty()) {
            context.resources.getIdentifier(drawableName, "drawable", context.packageName)
        } else 0
    }

    if (resourceId != 0) {
        Image(
            painter = painterResource(id = resourceId),
            contentDescription = title,
            contentScale = ContentScale.Fit,
            modifier = modifier
                .shadow(1.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(4.dp)
        )
    } else {
        val fallbackBgColor = remember(title) {
            when {
                title.contains("netflix", ignoreCase = true) -> Color(0xFFE50914)
                title.contains("hbo", ignoreCase = true) || title.contains("max", ignoreCase = true) -> Color(0xFF1F1C2C)
                title.contains("disney", ignoreCase = true) -> Color(0xFF113CCF)
                title.contains("spotify", ignoreCase = true) -> Color(0xFF1DB954)
                title.contains("youtube", ignoreCase = true) -> Color(0xFFFF0000)
                title.contains("prime", ignoreCase = true) || title.contains("amazon", ignoreCase = true) -> Color(0xFF00A8E1)
                title.contains("playstation", ignoreCase = true) || title.contains("psn", ignoreCase = true) || title.contains("ps4", ignoreCase = true) || title.contains("ps5", ignoreCase = true) -> Color(0xFF0037AE)
                title.contains("xbox", ignoreCase = true) -> Color(0xFF107C10)
                title.contains("steam", ignoreCase = true) || title.contains("juegos", ignoreCase = true) -> Color(0xFF171A21)
                title.contains("apple", ignoreCase = true) -> Color(0xFF000000)
                title.contains("dazn", ignoreCase = true) -> Color(0xFF000000)
                title.contains("nintendo", ignoreCase = true) -> Color(0xFFE60012)
                else -> {
                    val hash = title.hashCode()
                    val colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF0284C7),
                        Color(0xFF0D9488),
                        Color(0xFF4F46E5),
                        Color(0xFF7C3AED),
                        Color(0xFFDB2777),
                        Color(0xFFDC2626)
                    )
                    colors[kotlin.math.abs(hash) % colors.size]
                }
            }
        }

        val fallbackChar = title.firstOrNull()?.uppercaseChar()?.toString() ?: "S"

        Box(
            modifier = modifier
                .background(fallbackBgColor)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fallbackChar,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}
