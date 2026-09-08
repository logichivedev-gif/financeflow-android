package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.R
import com.example.data.FinancialProfile
import com.example.ui.FinanceViewModel
import kotlinx.coroutines.launch

private val FinanceTeal = Color(0xFF0061A4)
private val FinanceSlateDark = Color(0xFF1F2937)
private val FinanceSlateLight = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedSettingsDialog(
    initialTab: Int = 0,
    dbProfile: FinancialProfile?,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(initialTab.coerceIn(0, 3)) }

    val currentProfile = dbProfile ?: FinancialProfile()
    var editIncome by remember(currentProfile) { mutableStateOf(currentProfile.monthlyIncome.toString()) }
    var editIncomeDay by remember(currentProfile) { mutableStateOf(currentProfile.incomeDay.toString()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp)),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FinanceSlateDark)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(id = R.string.settings_title),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_settings_btn")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                // Scrollable Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFF8FAFC),
                    contentColor = FinanceTeal,
                    edgePadding = 12.dp,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = FinanceTeal
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("👤 Perfil", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        selectedContentColor = FinanceTeal,
                        unselectedContentColor = FinanceSlateLight
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("🎨 Apariencia", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        selectedContentColor = FinanceTeal,
                        unselectedContentColor = FinanceSlateLight
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("⚡ Fijos & Wizard", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        selectedContentColor = FinanceTeal,
                        unselectedContentColor = FinanceSlateLight
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("🔒 Seguridad & Notif.", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        selectedContentColor = FinanceTeal,
                        unselectedContentColor = FinanceSlateLight
                    )
                }

                // Tab Content Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    when (selectedTab) {
                        0 -> ProfileSettingsTab(
                            currentProfile = currentProfile,
                            viewModel = viewModel,
                            onCancel = onDismiss,
                            onSaveSuccess = onDismiss
                        )
                        1 -> AppearanceSettingsTab(
                            currentProfile = currentProfile,
                            viewModel = viewModel
                        )
                        2 -> ServicesWizardSettingsTab(
                            viewModel = viewModel
                        )
                        3 -> SecurityAndPerformanceTab(
                            currentProfile = currentProfile,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSettingsTab(
    currentProfile: FinancialProfile,
    viewModel: FinanceViewModel,
    onCancel: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val currency = currentProfile.selectedCurrency

    var editUserName by remember(currentProfile) { mutableStateOf(currentProfile.userName) }
    var selectedAvatarId by remember(currentProfile) { mutableStateOf(currentProfile.avatarId) }
    var customAvatarUri by remember(currentProfile) { mutableStateOf(currentProfile.customAvatarUri) }
    var editIncome by remember(currentProfile) { mutableStateOf(currentProfile.monthlyIncome.toString()) }
    var editIncomeDay by remember(currentProfile) { mutableStateOf(currentProfile.incomeDay.toString()) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                val inputStream = context.contentResolver.openInputStream(selectedUri)
                if (inputStream != null) {
                    context.filesDir.listFiles { file -> file.name.startsWith("custom_user_avatar_") }?.forEach { it.delete() }
                    val avatarFile = java.io.File(context.filesDir, "custom_user_avatar_${System.currentTimeMillis()}.jpg")
                    avatarFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    customAvatarUri = Uri.fromFile(avatarFile).toString()
                    selectedAvatarId = "custom"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Identidad y Foto de Usuario
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "👤 Identidad de Usuario",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = FinanceSlateDark,
                    modifier = Modifier.align(Alignment.Start)
                )

                // Current Selected Avatar Preview
                Box(
                    contentAlignment = Alignment.BottomEnd
                ) {
                    UserAvatarView(
                        profile = currentProfile,
                        avatarIdOverride = selectedAvatarId,
                        customUriOverride = customAvatarUri,
                        size = 80.dp,
                        modifier = Modifier.border(3.dp, FinanceTeal.copy(alpha = 0.3f), CircleShape)
                    )
                    IconButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(FinanceTeal)
                            .testTag("pick_custom_photo_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Cambiar foto",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Text field for user name
                OutlinedTextField(
                    value = editUserName,
                    onValueChange = { editUserName = it },
                    label = { Text("Nombre de usuario") },
                    placeholder = { Text("Ej. Víctor") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = FinanceTeal) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_user_name_input")
                )

                // Avatar presets picker title
                Text(
                    text = "Selecciona un Avatar o una Foto de tu Galería:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = FinanceSlateLight,
                    modifier = Modifier.align(Alignment.Start)
                )

                // Grid/Row of Preset Avatars + Gallery button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PRESET_AVATARS.forEach { preset ->
                        val isSelected = selectedAvatarId == preset.id
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(preset.backgroundColor)
                                .then(
                                    if (isSelected) Modifier.border(3.dp, Color.Black, CircleShape)
                                    else Modifier
                                )
                                .clickable {
                                    selectedAvatarId = preset.id
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = preset.emoji, fontSize = 20.sp)
                        }
                    }

                    // Gallery photo selection button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0))
                            .then(
                                if (selectedAvatarId == "custom") Modifier.border(3.dp, FinanceTeal, CircleShape)
                                else Modifier
                            )
                            .clickable { photoPickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Galería",
                            tint = FinanceSlateDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Section 2: Income settings Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "💰 Configuración de Ingresos",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = FinanceSlateDark
                )
                Text(
                    text = "Ajusta tu sueldo neto mensual recibido y el día del mes exacto en que ingresas tu nómina para el cálculo correcto del ciclo contable.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FinanceSlateLight
                )

                OutlinedTextField(
                    value = editIncome,
                    onValueChange = { input -> editIncome = input.replace(',', '.').filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Ingreso Neto Mensual ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_income_input")
                )

                OutlinedTextField(
                    value = editIncomeDay,
                    onValueChange = { editIncomeDay = it.filter { c -> c.isDigit() } },
                    label = { Text("Día de Cobro (1-31)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_income_day_input")
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.testTag("cancel_profile_btn")
            ) {
                Text("Cancelar")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val incomeValue = editIncome.toDoubleOrNull() ?: currentProfile.monthlyIncome
                    val dayValue = editIncomeDay.toIntOrNull()?.coerceIn(1, 31) ?: currentProfile.incomeDay
                    viewModel.updateFullUserProfile(
                        userName = editUserName.ifBlank { "Usuario" },
                        avatarId = selectedAvatarId,
                        customAvatarUri = customAvatarUri,
                        income = incomeValue,
                        day = dayValue
                    )
                    Toast.makeText(context, "✅ Perfil e identidad actualizados", Toast.LENGTH_SHORT).show()
                    onSaveSuccess()
                },
                colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                modifier = Modifier.testTag("save_profile_btn")
            ) {
                Text("Guardar Cambios")
            }
        }
    }
}

@Composable
private fun AppearanceSettingsTab(
    currentProfile: FinancialProfile,
    viewModel: FinanceViewModel
) {
    val hideNewMonthBannerState by viewModel.hideNewMonthBanner.collectAsStateWithLifecycle()
    val hideSmartCalendarBannerState by viewModel.hideSmartCalendarBanner.collectAsStateWithLifecycle()
    val selectedTheme = currentProfile.selectedTheme
    val selectedIconId = currentProfile.selectedIcon
    val currentCurrency = currentProfile.selectedCurrency

    val cardBg = Color(0xFFF8FAFC)
    val cardTitleColor = FinanceSlateDark
    val cardBodyColor = FinanceSlateLight
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banners
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Banners Informativos", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = cardTitleColor)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mostrar Banner 'Nuevo Mes'", style = MaterialTheme.typography.bodySmall, color = cardTitleColor, modifier = Modifier.weight(1f))
                    Switch(
                        checked = !hideNewMonthBannerState,
                        onCheckedChange = { viewModel.setHideNewMonthBanner(!it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = primaryColor),
                        modifier = Modifier.testTag("dialog_switch_new_month_banner")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mostrar Banner 'Calendario Inteligente'", style = MaterialTheme.typography.bodySmall, color = cardTitleColor, modifier = Modifier.weight(1f))
                    Switch(
                        checked = !hideSmartCalendarBannerState,
                        onCheckedChange = { viewModel.setHideSmartCalendarBanner(!it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = primaryColor),
                        modifier = Modifier.testTag("dialog_switch_smart_calendar_banner")
                    )
                }
            }
        }

        // Color theme
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Acento de Color", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = cardTitleColor)

                listOf(
                    Triple("azul", "Azul Nobara", Color(0xFF0061A4)),
                    Triple("verde", "Verde Dinero", Color(0xFF16A34A)),
                    Triple("minimalista", "Minimalista Oscuro", Color(0xFF1F2937)),
                    Triple("naranja", "Naranja Coral", Color(0xFFEA580C)),
                    Triple("purpura", "Púrpura Elegante", Color(0xFF7C3AED))
                ).forEach { (themeId, themeName, themeColor) ->
                    val isSelected = selectedTheme == themeId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) primaryColor.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable { viewModel.setSelectedTheme(themeId) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(themeColor)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = themeName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                            color = if (isSelected) primaryColor else cardTitleColor,
                            modifier = Modifier.weight(1f)
                        )
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.setSelectedTheme(themeId) },
                            colors = RadioButtonDefaults.colors(selectedColor = primaryColor),
                            modifier = Modifier.testTag("radio_theme_$themeId")
                        )
                    }
                }
            }
        }

        // Icon Selector
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Icono de la App", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = cardTitleColor)
                Text("Selecciona el símbolo principal de la app:", style = MaterialTheme.typography.bodySmall, color = cardBodyColor)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        Pair("trending", Icons.Default.TrendingUp),
                        Pair("wallet", Icons.Default.AccountBalanceWallet),
                        Pair("savings", Icons.Default.Savings),
                        Pair("payments", Icons.Default.Payments),
                        Pair("chart", Icons.Default.ShowChart),
                        Pair("star", Icons.Default.Star)
                    ).forEach { (iconId, iconVec) ->
                        val isSelected = selectedIconId == iconId
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) primaryColor.copy(alpha = 0.12f) else Color.Transparent)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) primaryColor else cardBodyColor.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setSelectedIcon(iconId) }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVec,
                                contentDescription = iconId,
                                tint = if (isSelected) primaryColor else cardBodyColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Currency Selector
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Moneda Base Global", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = cardTitleColor)

                listOf(
                    Pair("€", "Euro (€)"),
                    Pair("$", "Dólar ($)"),
                    Pair("£", "Libra (£)"),
                    Pair("¥", "Yen / Yuan (¥)"),
                    Pair("COP", "Peso Col (COP)"),
                    Pair("MXN", "Peso Mex (MXN)")
                ).chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { (currSymbol, currName) ->
                            val isSelected = currentCurrency == currSymbol
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) primaryColor.copy(alpha = 0.12f) else Color.Transparent)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isSelected) primaryColor else cardBodyColor.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setSelectedCurrency(currSymbol) }
                                    .padding(horizontal = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) primaryColor else cardBodyColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = currSymbol,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = if (isSelected) Color.White else cardTitleColor,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = currName,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                        color = if (isSelected) primaryColor else cardTitleColor,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServicesWizardSettingsTab(
    viewModel: FinanceViewModel
) {
    val hasPets by viewModel.hasPets.collectAsStateWithLifecycle()
    val petsCost by viewModel.petsCost.collectAsStateWithLifecycle()
    val hasKids by viewModel.hasKids.collectAsStateWithLifecycle()
    val kidsCost by viewModel.kidsCost.collectAsStateWithLifecycle()
    val sharedExpenses by viewModel.sharedExpenses.collectAsStateWithLifecycle()
    val partnerCont by viewModel.partnerContribution.collectAsStateWithLifecycle()
    val isWaterEnabled by viewModel.isWaterEnabled.collectAsStateWithLifecycle()
    val waterBilling by viewModel.waterBilling.collectAsStateWithLifecycle()
    val waterCost by viewModel.waterCost.collectAsStateWithLifecycle()
    val isElectricityEnabled by viewModel.isElectricityEnabled.collectAsStateWithLifecycle()
    val electricityBilling by viewModel.electricityBilling.collectAsStateWithLifecycle()
    val electricityCost by viewModel.electricityCost.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "⚡ Servicios y Gastos del Wizard",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = FinanceSlateDark
                )
                Text(
                    text = "Ajusta las cuotas y estados activos de suministros (Agua, Luz), mascotas, hijos y gastos compartidos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FinanceSlateLight
                )

                // Mascotas
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Pets, contentDescription = null, tint = FinanceSlateLight, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mascotas", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                        Switch(
                            checked = hasPets,
                            onCheckedChange = { viewModel.togglePetsDashboard(it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal)
                        )
                    }
                    if (hasPets) {
                        OutlinedTextField(
                            value = petsCost,
                            onValueChange = { viewModel.updatePetsCostDashboard(it) },
                            label = { Text("Cuota estimada mensual mascotas") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().padding(start = 28.dp, top = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                // Hijos
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.ChildCare, contentDescription = null, tint = FinanceSlateLight, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hijos", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                        Switch(
                            checked = hasKids,
                            onCheckedChange = { viewModel.toggleKidsDashboard(it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal)
                        )
                    }
                    if (hasKids) {
                        OutlinedTextField(
                            value = kidsCost,
                            onValueChange = { viewModel.updateKidsCostDashboard(it) },
                            label = { Text("Cuota estimada mensual hijos") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().padding(start = 28.dp, top = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                // Gastos Compartidos
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = FinanceSlateLight, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gastos Compartidos Pareja", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                        Switch(
                            checked = sharedExpenses,
                            onCheckedChange = { viewModel.toggleSharedExpensesDashboard(it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal)
                        )
                    }
                    if (sharedExpenses) {
                        OutlinedTextField(
                            value = partnerCont,
                            onValueChange = { viewModel.updatePartnerContributionDashboard(it) },
                            label = { Text("Aporte fijo mensual pareja") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().padding(start = 28.dp, top = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                // Agua
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Suministro de Agua", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                        Switch(
                            checked = isWaterEnabled,
                            onCheckedChange = { viewModel.toggleWaterSupplyDashboard(it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal)
                        )
                    }
                    if (isWaterEnabled) {
                        Row(modifier = Modifier.fillMaxWidth().padding(start = 28.dp, top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = waterCost,
                                onValueChange = { viewModel.updateWaterCostDashboard(it) },
                                label = { Text("Importe") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilterChip(
                                    selected = waterBilling == "Mensual",
                                    onClick = { viewModel.updateWaterBillingCycleDashboard("Mensual") },
                                    label = { Text("Mensual", fontSize = 11.sp) }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                FilterChip(
                                    selected = waterBilling == "Bimensual",
                                    onClick = { viewModel.updateWaterBillingCycleDashboard("Bimensual") },
                                    label = { Text("Bimensual", fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                // Luz
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFEAB308), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Suministro de Luz / Electricidad", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                        Switch(
                            checked = isElectricityEnabled,
                            onCheckedChange = { viewModel.toggleElectricitySupplyDashboard(it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal)
                        )
                    }
                    if (isElectricityEnabled) {
                        Row(modifier = Modifier.fillMaxWidth().padding(start = 28.dp, top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = electricityCost,
                                onValueChange = { viewModel.updateElectricityCostDashboard(it) },
                                label = { Text("Importe") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilterChip(
                                    selected = electricityBilling == "Mensual",
                                    onClick = { viewModel.updateElectricityBillingCycleDashboard("Mensual") },
                                    label = { Text("Mensual", fontSize = 11.sp) }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                FilterChip(
                                    selected = electricityBilling == "Bimensual",
                                    onClick = { viewModel.updateElectricityBillingCycleDashboard("Bimensual") },
                                    label = { Text("Bimensual", fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityAndPerformanceTab(
    currentProfile: FinancialProfile,
    viewModel: FinanceViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isBiometricEnabled = currentProfile.isBiometricEnabled
    val isPaymentNotificationsEnabled = currentProfile.isPaymentNotificationsEnabled
    val hoursLead = currentProfile.paymentNotificationHoursLead
    var editCycleDay by remember(currentProfile.customCycleStartDay) { mutableStateOf(currentProfile.customCycleStartDay.toString()) }
    val isHighPerformanceMode = currentProfile.isHighPerformanceMode

    var showBatteryGuideDialog by remember { mutableStateOf(false) }
    val isBankInterceptorEnabled = currentProfile.isBankNotificationInterceptorEnabled

    val checkBatteryOptimization = {
        val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as? android.os.PowerManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
        } else {
            true
        }
    }

    val checkNotificationListener = {
        val flat = android.provider.Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        flat != null && flat.contains(context.packageName)
    }

    var isBatteryOptimizationIgnored by remember { mutableStateOf(checkBatteryOptimization()) }
    var isListenerPermissionGranted by remember { mutableStateOf(checkNotificationListener()) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isBatteryOptimizationIgnored = checkBatteryOptimization()
                isListenerPermissionGranted = checkNotificationListener()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showBatteryGuideDialog) {
        AlertDialog(
            onDismissRequest = { showBatteryGuideDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BatterySaver,
                        contentDescription = null,
                        tint = Color(0xFF0284C7)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Guía de Segundo Plano y Autostart",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Para evitar que Android o la capa de personalización de tu dispositivo cierre el detector de notificaciones bancarias en segundo plano, sigue estos pasos:",
                        style = MaterialTheme.typography.bodySmall,
                        color = FinanceSlateDark
                    )

                    // Xiaomi
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("📱 Xiaomi / Redmi / POCO (MIUI / HyperOS):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = FinanceSlateDark)
                            Text("1. Ajustes -> Aplicaciones -> Gestionar aplicaciones -> Finance Flow.\n2. Activa 'Inicio automático' (Autostart).\n3. En 'Ahorro de batería', selecciona 'Sin restricciones'.", fontSize = 11.sp, color = FinanceSlateLight)
                        }
                    }

                    // Samsung
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("📱 Samsung (One UI):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = FinanceSlateDark)
                            Text("1. Ajustes -> Aplicaciones -> Finance Flow -> Batería.\n2. Selecciona la opción 'No restringida'.", fontSize = 11.sp, color = FinanceSlateLight)
                        }
                    }

                    // Huawei / Honor
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("📱 Huawei / Honor (EMUI / MagicOS):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = FinanceSlateDark)
                            Text("1. Ajustes -> Batería -> Inicio de aplicaciones.\n2. Busca Finance Flow y cámbialo a 'Gestionar manualmente' (activando Inicio automático, Inicio secundario y Ejecución en segundo plano).", fontSize = 11.sp, color = FinanceSlateLight)
                        }
                    }

                    // Oppo / Realme / Vivo
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("📱 OPPO / Realme / Vivo (ColorOS / Funtouch):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = FinanceSlateDark)
                            Text("1. Ajustes -> Aplicaciones -> Gestión de aplicaciones -> Finance Flow.\n2. En 'Uso de batería', permite la ejecución en segundo plano e Inicio automático.", fontSize = 11.sp, color = FinanceSlateLight)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showBatteryGuideDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal)
                ) {
                    Text("Entendido")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 🔒 1. Seguridad Biométrica
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = FinanceTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(id = R.string.setting_biometric_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = FinanceSlateDark,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = {
                            viewModel.setBiometricEnabled(it)
                            val statusMsg = if (it) "🔒 Seguridad biométrica/PIN activada" else "🔓 Biometría desactivada"
                            Toast.makeText(context, statusMsg, Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal),
                        modifier = Modifier.testTag("setting_biometric_switch")
                    )
                }
                Text(
                    stringResource(id = R.string.setting_biometric_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = FinanceSlateLight
                )
                if (isBiometricEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "🟢 Bloqueo biométrico activo. Se requerirá autenticación al iniciar.",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF047857)
                        )
                    }
                }
            }
        }

        // 🔔 2. Alertas y Recordatorios de Pago
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFEAB308))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(id = R.string.setting_notifications_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = FinanceSlateDark,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isPaymentNotificationsEnabled,
                        onCheckedChange = {
                            viewModel.setPaymentNotificationsEnabled(it, hoursLead)
                            val statusMsg = if (it) "🔔 Recordatorios nativos activados ($hoursLead h antes)" else "🔕 Notificaciones desactivadas"
                            Toast.makeText(context, statusMsg, Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal),
                        modifier = Modifier.testTag("setting_notifications_switch")
                    )
                }
                Text(
                    stringResource(id = R.string.setting_notifications_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = FinanceSlateLight
                )

                if (isPaymentNotificationsEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Aviso de antelación:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = FinanceSlateDark)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(24, 48, 72).forEach { hours ->
                                FilterChip(
                                    selected = hoursLead == hours,
                                    onClick = { viewModel.setPaymentNotificationsEnabled(true, hours) },
                                    label = { Text("${hours}h antes", fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 🏦 3. Detector de Notificaciones Bancarias (v2.1.0)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = Color(0xFF0284C7)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Detector de Notificaciones Bancarias",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = FinanceSlateDark
                        )
                        Text(
                            text = "v${BuildConfig.VERSION_NAME} • Interceptor v2 & Google Wallet",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF0284C7)
                        )
                    }
                    Switch(
                        checked = isBankInterceptorEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                viewModel.setBankNotificationInterceptorEnabled(true)
                                val flat = android.provider.Settings.Secure.getString(
                                    context.contentResolver,
                                    "enabled_notification_listeners"
                                )
                                val isGranted = flat != null && flat.contains(context.packageName)
                                if (!isGranted) {
                                    Toast.makeText(
                                        context,
                                        "⚠️ Activa el 'Acceso a notificaciones' en Android para permitir la lectura de pagos del banco",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    try {
                                        context.startActivity(android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                    } catch (e: Exception) {
                                        // fallback
                                    }
                                } else {
                                    com.example.service.BankNotificationListenerService.ensureServiceBound(context)
                                    Toast.makeText(context, "🏦 Detector de notificaciones bancarias activado y vinculado", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                viewModel.setBankNotificationInterceptorEnabled(false)
                                Toast.makeText(context, "⏹️ Detector de notificaciones bancarias desactivado", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF0284C7)),
                        modifier = Modifier.testTag("bank_interceptor_switch")
                    )
                }

                Text(
                    text = "Al recibir una notificación de tu app bancaria correspondiente a un gasto fijo pendiente, se marcará automáticamente como pagado (isPaid = true) de forma transparente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FinanceSlateLight
                )

                if (isBankInterceptorEnabled) {
                    // Permission Status Badge
                    if (!isListenerPermissionGranted) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF2F2),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Falta Permiso del Sistema",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF991B1B)
                                    )
                                    Text(
                                        text = "Concede permiso de Acceso a Notificaciones.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF7F1D1D)
                                    )
                                }
                                Button(
                                    onClick = {
                                        try {
                                            context.startActivity(android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Abre Ajustes -> Notificaciones en Android", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Activar", fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFECFDF5),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Permiso concedido. Escuchando activamente.",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF065F46),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = {
                                        com.example.service.BankNotificationListenerService.ensureServiceBound(context)
                                        Toast.makeText(context, "🔄 Señal de vinculación enviada al servicio", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF065F46)),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Reconectar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Background & Battery Guide Section
                    if (isBatteryOptimizationIgnored) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFECFDF5),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "✓ Optimización de batería desactivada. Inicio automático configurado.",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF065F46)
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF0F9FF))
                                .border(1.dp, Color(0xFFBAE6FD), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BatterySaver,
                                    contentDescription = null,
                                    tint = Color(0xFF0369A1),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Optimización de Batería e Inicio Automático",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF0369A1)
                                )
                            }

                            Text(
                                text = "Para garantizar que el servicio no se cierre en segundo plano:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF0F172A)
                            )

                            Text(
                                text = "• Batería: Configurar la app en 'Sin Restricciones'.\n• Autostart: Activar 'Inicio Automático' en Xiaomi, Samsung, Huawei, Oppo, Vivo.",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFF334155)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                                data = android.net.Uri.parse("package:${context.packageName}")
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            try {
                                                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                    data = android.net.Uri.parse("package:${context.packageName}")
                                                }
                                                context.startActivity(intent)
                                            } catch (ex: Exception) {
                                                Toast.makeText(context, "Abre Ajustes de Batería en Android", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("🔋 Ajustes Batería", fontSize = 11.sp, maxLines = 1)
                                }

                                Button(
                                    onClick = { showBatteryGuideDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("💡 Guía Autostart", fontSize = 11.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 📅 3. Cierre de Ciclo Personalizado
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF7C3AED))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(id = R.string.setting_cycle_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = FinanceSlateDark
                    )
                }
                Text(
                    stringResource(id = R.string.setting_cycle_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = FinanceSlateLight
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = editCycleDay,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            editCycleDay = filtered
                            val dayNum = filtered.toIntOrNull()
                            if (dayNum != null && dayNum in 1..31) {
                                viewModel.setCustomCycleStartDay(dayNum)
                            }
                        },
                        label = { Text("Día inicio ciclo (1-31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("setting_cycle_day_input")
                    )
                    Button(
                        onClick = {
                            val dayNum = editCycleDay.toIntOrNull()?.coerceIn(1, 31) ?: 1
                            viewModel.setCustomCycleStartDay(dayNum)
                            Toast.makeText(context, "📅 Día de ciclo guardado: Día $dayNum", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal)
                    ) {
                        Text("Aplicar")
                    }
                }
            }
        }

        // ⚡ 4. Modo de Alto Rendimiento
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = Color(0xFFEA580C))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(id = R.string.setting_performance_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = FinanceSlateDark,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isHighPerformanceMode,
                        onCheckedChange = {
                            viewModel.setHighPerformanceMode(it)
                            val statusMsg = if (it) "⚡ Modo Alto Rendimiento activado" else "✨ Transiciones visuales completas activadas"
                            Toast.makeText(context, statusMsg, Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFEA580C)),
                        modifier = Modifier.testTag("setting_performance_switch")
                    )
                }
                Text(
                    stringResource(id = R.string.setting_performance_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = FinanceSlateLight
                )
            }
        }

        // 💾 5. Copia de Seguridad Diaria (24h) y Restauración
        var showConfirmRestoreDialog by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFF0284C7))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Copia de Seguridad Automática (24h)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = FinanceSlateDark,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "El sistema genera automáticamente un respaldo local rotativo cada 24 horas (backup_previous.db) para proteger tus finanzas en caso de fallo crítico.",
                    style = MaterialTheme.typography.bodySmall,
                    color = FinanceSlateLight
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.example.data.DailyBackupWorker>().build()
                                    androidx.work.WorkManager.getInstance(context).enqueue(workRequest)
                                    Toast.makeText(context, "💾 Creando copia de seguridad inmediata...", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Forzar Backup", fontSize = 11.sp, maxLines = 1)
                    }

                    Button(
                        onClick = {
                            if (viewModel.hasLatestDailyBackup(context)) {
                                showConfirmRestoreDialog = true
                            } else {
                                Toast.makeText(context, "Aún no existe una copia de seguridad previa de 24h.", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restaurar Previo", fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }

        if (showConfirmRestoreDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmRestoreDialog = false },
                title = { Text("¿Restaurar copia previa?") },
                text = {
                    Text("Esta acción restaurará la base de datos a partir de backup_previous.db (el estado del día anterior). Los cambios no guardados se sustituirán.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmRestoreDialog = false
                            coroutineScope.launch {
                                val success = viewModel.restoreLatestDailyBackup(context)
                                if (success) {
                                    Toast.makeText(context, "✅ Base de datos restaurada correctamente", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "❌ Error al restaurar la copia de seguridad", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Confirmar Restauración")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmRestoreDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
