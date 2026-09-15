package com.example.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FinanceViewModel
import com.example.ui.common.FinanceBorder
import com.example.ui.common.FinanceSlateLight
import com.example.ui.common.FinanceTeal
import kotlinx.coroutines.launch

@Composable
fun BackupAndRestoreSection(
    viewModel: FinanceViewModel,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Exportar manual (.fflow / .json)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.exportBackupToUri(context, it) { success ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        if (success) "Copia de seguridad (.fflow) guardada con éxito" 
                        else "Error al guardar la copia de seguridad"
                    )
                }
            }
        }
    }

    // Importar manual (.fflow / .json)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importBackupFromUri(context, it) { success ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        if (success) "¡Datos restaurados correctamente!" 
                        else "El archivo seleccionado no es válido"
                    )
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // CARD 1: BACKUP MANUAL (.fflow)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, FinanceBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SdStorage, contentDescription = null, tint = FinanceTeal, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copia de Seguridad Manual (.fflow)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Exporta e importa tus datos en un archivo físico para migrar entre dispositivos.", style = MaterialTheme.typography.bodySmall, color = FinanceSlateLight)
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { exportLauncher.launch("financeflow_backup_${System.currentTimeMillis()}.fflow") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Exportar .fflow", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("*/*", "application/json")) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Importar", color = FinanceTeal, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // CARD 2: BACKUP AUTOMÁTICO ROTATIVO 24H (En /Documents/FinanceFlow/)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, FinanceBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = FinanceTeal, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copia de Seguridad Automática (24h)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Guarda un respaldo rotativo en 'Documents/FinanceFlow/'. Inmune a desinstalaciones.", style = MaterialTheme.typography.bodySmall, color = FinanceSlateLight)
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            viewModel.triggerAutoBackupExternal(context) { success ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(if (success) "Backup 24h generado en Documents/FinanceFlow" else "Error al generar backup")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Forzar Backup", fontSize = 12.sp) }
                    Button(
                        onClick = {
                            viewModel.restoreAutoBackupExternal(context) { success ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(if (success) "¡Respaldo de 24h restaurado!" else "No se encontró respaldo previo en Documents/FinanceFlow")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Restaurar Previo", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                }
            }
        }
    }
}
