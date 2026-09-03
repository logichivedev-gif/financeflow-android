package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.FinancialProfile

data class PresetAvatar(
    val id: String,
    val name: String,
    val emoji: String,
    val backgroundColor: Color
)

val PRESET_AVATARS = listOf(
    PresetAvatar("avatar_1", "Clásico", "👤", Color(0xFF0061A4)),
    PresetAvatar("avatar_2", "Elegante", "👩", Color(0xFFEC4899)),
    PresetAvatar("avatar_3", "Moderno", "🧑", Color(0xFF8B5CF6)),
    PresetAvatar("avatar_4", "Astuto", "🦊", Color(0xFFF97316)),
    PresetAvatar("avatar_5", "Cohete", "🚀", Color(0xFF10B981)),
    PresetAvatar("avatar_6", "Finanzas", "💎", Color(0xFFEAB308))
)

@Composable
fun UserAvatarView(
    profile: FinancialProfile?,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    avatarIdOverride: String? = null,
    customUriOverride: String? = null
) {
    val avatarId = avatarIdOverride ?: profile?.avatarId ?: "avatar_1"
    val customUri = customUriOverride ?: profile?.customAvatarUri ?: ""

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (avatarId == "custom" && customUri.isNotBlank()) {
            AsyncImage(
                model = customUri,
                contentDescription = "Avatar de Usuario",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val preset = PRESET_AVATARS.find { it.id == avatarId } ?: PRESET_AVATARS[0]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(preset.backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = preset.emoji,
                    fontSize = (size.value * 0.48f).sp
                )
            }
        }
    }
}
