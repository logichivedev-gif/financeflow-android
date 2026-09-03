package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.ui.common.FinanceBorder
import com.example.ui.common.FinanceSlateDark
import com.example.ui.common.FinanceSlateLight

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "FinanceFlow",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = FinanceSlateDark
                )
                Text(
                    text = if (BuildConfig.CHANGES_COUNT > 0) {
                        "Versión ${BuildConfig.VERSION_NAME} (Progreso: ${BuildConfig.CHANGES_COUNT}/10 cambios)"
                    } else {
                        "Versión ${BuildConfig.VERSION_NAME}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = FinanceSlateLight
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Una app financiera 100% privada, solidaria y offline. Creada con pasión para el control total de tu economía sin intermediarios ni suscripciones.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FinanceSlateDark
                )
                HorizontalDivider(color = FinanceBorder.copy(alpha = 0.5f))
                Text(
                    text = "Desarrollador: Victor Villatoro",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = FinanceSlateLight
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        uriHandler.openUri("https://ko-fi.com/logichivedev")
                    } catch (e: Exception) {}
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("☕ Invítame a un café (Opcional)", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar", color = FinanceSlateLight, modifier = Modifier.fillMaxWidth())
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
