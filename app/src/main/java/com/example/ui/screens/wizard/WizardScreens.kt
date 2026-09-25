package com.example.ui.screens.wizard

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FinanceViewModel
import com.example.ui.common.*
import com.example.ui.components.SubscriptionLogo
import kotlinx.coroutines.launch

@Composable
fun WizardStep1Screen(
    viewModel: FinanceViewModel,
    windowWidthSizeClass: WindowWidthSizeClass
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val incomeInput by viewModel.incomeInput.collectAsStateWithLifecycle()
    val incomeDayInput by viewModel.incomeDayInput.collectAsStateWithLifecycle()
    val wizardBankBalanceInput by viewModel.wizardBankBalanceInput.collectAsStateWithLifecycle()

    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val content = inputStream?.bufferedReader(Charsets.UTF_8)?.use { reader -> reader.readText() } ?: ""
                    if (content.isNotEmpty()) {
                        val ok = viewModel.importDataJson(content)
                        if (ok) {
                            Toast.makeText(context, "✅ Copia restaurada correctamente", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "❌ Formato .fflow inválido", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "❌ Error al leer archivo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        containerColor = FinanceSoftBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(FinanceTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "FinanceFlow",
                        tint = FinanceTeal,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "Bienvenido a FinanceFlow",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = FinanceSlateDark,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Paso 1 de 4: Configuración Inicial\nDefine tus ingresos netos mensuales y tu día de cobro habitual para calibrar el ciclo contable.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FinanceSlateLight,
                    textAlign = TextAlign.Center
                )

                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, FinanceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = incomeInput,
                            onValueChange = { viewModel.setIncomeInput(it) },
                            label = { Text("Salario Neto Mensual (€)") },
                            placeholder = { Text("Ej: 1800.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Payments, contentDescription = null, tint = FinanceTeal)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wizard_income_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = incomeDayInput,
                            onValueChange = { viewModel.updateIncomeDayInput(it) },
                            label = { Text("Día de Cobro Mensual (1 - 31)") },
                            placeholder = { Text("Ej: 1") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = FinanceTeal)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wizard_income_day_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = wizardBankBalanceInput,
                            onValueChange = { viewModel.setWizardBankBalanceInput(it) },
                            label = { Text("Saldo Actual en Cuenta Bancaria (€) (Opcional)") },
                            placeholder = { Text("Ej: 2450.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = FinanceSlateLight)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wizard_bank_balance_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                
                Button(
                    onClick = { viewModel.completeStep1() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("wizard_step1_continue_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Continuar al Paso 2 ➔",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                
                TextButton(
                    onClick = { restoreFileLauncher.launch("*/*") },
                    modifier = Modifier.testTag("wizard_restore_backup_btn")
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = null, tint = FinanceSlateLight, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("¿Ya tienes una copia de seguridad? Restaurar .fflow", color = FinanceSlateLight, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun WizardStep2Screen(
    viewModel: FinanceViewModel,
    windowWidthSizeClass: WindowWidthSizeClass
) {
    val fixedExpenses by viewModel.fixedExpenses.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = FinanceSoftBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Paso 2 de 4: Gastos Fijos",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = FinanceSlateDark
                )

                Text(
                    text = "Añade o ajusta tus obligaciones mensuales recurrentes (alquiler, gimnasio, internet, préstamos, etc.):",
                    style = MaterialTheme.typography.bodySmall,
                    color = FinanceSlateLight,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(fixedExpenses, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, FinanceBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = item.name,
                                        onValueChange = { viewModel.updateFixedExpense(item.id, it, item.amount, item.payDay) },
                                        label = { Text("Nombre del Gasto") },
                                        modifier = Modifier.weight(1f).testTag("wizard_fixed_name_${item.id}"),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    IconButton(
                                        onClick = { viewModel.deleteFixedExpense(item.id) },
                                        modifier = Modifier.testTag("wizard_fixed_delete_${item.id}")
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = CardBorderRed)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = item.amount,
                                        onValueChange = { viewModel.updateFixedExpense(item.id, item.name, it, item.payDay) },
                                        label = { Text("Importe (€)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1.2f).testTag("wizard_fixed_amount_${item.id}"),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = item.payDay,
                                        onValueChange = { viewModel.updateFixedExpense(item.id, item.name, item.amount, it) },
                                        label = { Text("Día Pago (1-31)") },
                                        placeholder = { Text("Ej: 5") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f).testTag("wizard_fixed_payday_${item.id}"),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = { viewModel.addCustomFixedExpense() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("wizard_add_fixed_btn"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, FinanceTeal)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = FinanceTeal)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Añadir Otro Gasto Fijo", color = FinanceTeal, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.completeStep2() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("wizard_step2_continue_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Continuar al Paso 3 ➔",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun WizardStep3Screen(
    viewModel: FinanceViewModel,
    windowWidthSizeClass: WindowWidthSizeClass
) {
    val variableBudgets by viewModel.variableBudgets.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = FinanceSoftBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Paso 3 de 4: Presupuestos Variables",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = FinanceSlateDark
                )

                Text(
                    text = "Establece tus límites mensuales deseados para gastos cotidianos flexibles (comida, combustible, ocio):",
                    style = MaterialTheme.typography.bodySmall,
                    color = FinanceSlateLight,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(variableBudgets, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, FinanceBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = item.name,
                                    onValueChange = { viewModel.updateVariableBudget(item.id, it, item.amount) },
                                    label = { Text("Categoría") },
                                    modifier = Modifier.weight(1.3f).testTag("wizard_var_name_${item.id}"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = item.amount,
                                    onValueChange = { viewModel.updateVariableBudget(item.id, item.name, it) },
                                    label = { Text("Límite (€)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f).testTag("wizard_var_amount_${item.id}"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )

                                IconButton(
                                    onClick = { viewModel.deleteVariableBudget(item.id) },
                                    modifier = Modifier.testTag("wizard_var_delete_${item.id}")
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = CardBorderRed)
                                }
                            }
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = { viewModel.addCustomVariableBudget() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("wizard_add_variable_btn"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, FinanceTeal)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = FinanceTeal)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Añadir Otra Categoría Variable", color = FinanceTeal, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.completeStep3() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("wizard_step3_continue_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Continuar al Paso 4 ➔",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun WizardStep4Screen(
    viewModel: FinanceViewModel,
    windowWidthSizeClass: WindowWidthSizeClass
) {
    val hasPets by viewModel.hasPets.collectAsStateWithLifecycle()
    val petsCost by viewModel.petsCost.collectAsStateWithLifecycle()

    val hasKids by viewModel.hasKids.collectAsStateWithLifecycle()
    val kidsCost by viewModel.kidsCost.collectAsStateWithLifecycle()

    val sharedExpenses by viewModel.sharedExpenses.collectAsStateWithLifecycle()
    val partnerContribution by viewModel.partnerContribution.collectAsStateWithLifecycle()

    val isWaterEnabled by viewModel.isWaterEnabled.collectAsStateWithLifecycle()
    val waterCost by viewModel.waterCost.collectAsStateWithLifecycle()
    val waterBilling by viewModel.waterBilling.collectAsStateWithLifecycle()

    val isElectricityEnabled by viewModel.isElectricityEnabled.collectAsStateWithLifecycle()
    val electricityCost by viewModel.electricityCost.collectAsStateWithLifecycle()
    val electricityBilling by viewModel.electricityBilling.collectAsStateWithLifecycle()

    val netflixActive by viewModel.netflixActive.collectAsStateWithLifecycle()
    val netflixCost by viewModel.netflixCost.collectAsStateWithLifecycle()

    val hboActive by viewModel.hboActive.collectAsStateWithLifecycle()
    val hboCost by viewModel.hboCost.collectAsStateWithLifecycle()

    val disneyActive by viewModel.disneyActive.collectAsStateWithLifecycle()
    val disneyCost by viewModel.disneyCost.collectAsStateWithLifecycle()

    val juegosActive by viewModel.juegosActive.collectAsStateWithLifecycle()
    val juegosCost by viewModel.juegosCost.collectAsStateWithLifecycle()

    val amazonPrimeActive by viewModel.amazonPrimeActive.collectAsStateWithLifecycle()
    val amazonPrimeCost by viewModel.amazonPrimeCost.collectAsStateWithLifecycle()

    val spotifyActive by viewModel.spotifyActive.collectAsStateWithLifecycle()
    val spotifyCost by viewModel.spotifyCost.collectAsStateWithLifecycle()

    val youtubePremiumActive by viewModel.youtubePremiumActive.collectAsStateWithLifecycle()
    val youtubePremiumCost by viewModel.youtubePremiumCost.collectAsStateWithLifecycle()

    val youtubeMusicActive by viewModel.youtubeMusicActive.collectAsStateWithLifecycle()
    val youtubeMusicCost by viewModel.youtubeMusicCost.collectAsStateWithLifecycle()

    val customStreams by viewModel.customStreams.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = FinanceSoftBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .padding(20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Paso 4 de 4: Suscripciones y Familia",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = FinanceSlateDark
                )

                Text(
                    text = "Personaliza suministros básicos, mascotas, hijos o suscripciones de streaming:",
                    style = MaterialTheme.typography.bodySmall,
                    color = FinanceSlateLight,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, FinanceBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Suministros del Hogar", fontWeight = FontWeight.Bold, color = FinanceSlateDark)

                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = FinanceTeal, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Agua", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Switch(
                                        checked = isWaterEnabled,
                                        onCheckedChange = { viewModel.setWaterEnabled(it) },
                                        colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal)
                                    )
                                }
                                AnimatedVisibility(visible = isWaterEnabled) {
                                    OutlinedTextField(
                                        value = waterCost,
                                        onValueChange = { viewModel.setWaterCost(it) },
                                        label = { Text("Importe Factura Agua (€)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }

                                Divider(color = FinanceBorder.copy(alpha = 0.5f))

                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Luz y Electricidad", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Switch(
                                        checked = isElectricityEnabled,
                                        onCheckedChange = { viewModel.setElectricityEnabled(it) },
                                        colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal)
                                    )
                                }
                                AnimatedVisibility(visible = isElectricityEnabled) {
                                    OutlinedTextField(
                                        value = electricityCost,
                                        onValueChange = { viewModel.setElectricityCost(it) },
                                        label = { Text("Importe Factura Luz (€)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, FinanceBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Mascotas e Hijos", fontWeight = FontWeight.Bold, color = FinanceSlateDark)

                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Pets, contentDescription = null, tint = FinanceTeal, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Tengo Mascotas", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Switch(
                                        checked = hasPets,
                                        onCheckedChange = { viewModel.setHasPets(it) },
                                        colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal)
                                    )
                                }
                                AnimatedVisibility(visible = hasPets) {
                                    OutlinedTextField(
                                        value = petsCost,
                                        onValueChange = { viewModel.setPetsCost(it) },
                                        label = { Text("Gasto mensual mascotas (€)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }

                                Divider(color = FinanceBorder.copy(alpha = 0.5f))

                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ChildCare, contentDescription = null, tint = FinanceTeal, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Tengo Hijos", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Switch(
                                        checked = hasKids,
                                        onCheckedChange = { viewModel.setHasKids(it) },
                                        colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal)
                                    )
                                }
                                AnimatedVisibility(visible = hasKids) {
                                    OutlinedTextField(
                                        value = kidsCost,
                                        onValueChange = { viewModel.setKidsCost(it) },
                                        label = { Text("Gasto mensual hijos (€)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, FinanceBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Suscripciones de Streaming", fontWeight = FontWeight.Bold, color = FinanceSlateDark)

                                listOf(
                                    Triple("Netflix", netflixActive, netflixCost) to { act: Boolean -> viewModel.setNetflixActive(act) },
                                    Triple("HBO Max", hboActive, hboCost) to { act: Boolean -> viewModel.setHboActive(act) },
                                    Triple("Disney+", disneyActive, disneyCost) to { act: Boolean -> viewModel.setDisneyActive(act) },
                                    Triple("Spotify", spotifyActive, spotifyCost) to { act: Boolean -> viewModel.setSpotifyActive(act) },
                                    Triple("Amazon Prime", amazonPrimeActive, amazonPrimeCost) to { act: Boolean -> viewModel.setAmazonPrimeActive(act) },
                                    Triple("YouTube Premium", youtubePremiumActive, youtubePremiumCost) to { act: Boolean -> viewModel.setYoutubePremiumActive(act) }
                                ).forEach { (info, toggle) ->
                                    val (name, active, cost) = info
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            SubscriptionLogo(title = name, modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(name, style = MaterialTheme.typography.bodyMedium)
                                        }
                                        Switch(
                                            checked = active,
                                            onCheckedChange = { toggle(it) },
                                            colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.completeWizard() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("wizard_complete_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Finalizar y Comenzar 🎉",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}
