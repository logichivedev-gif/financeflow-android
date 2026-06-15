package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.R
import com.example.data.ExpenseCategory
import com.example.data.VariableExpenseEntry
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

val defaultCurrencySymbol: String = try {
    java.util.Currency.getInstance(java.util.Locale.getDefault()).symbol
} catch (e: Exception) {
    "€"
}

var currentThemeTeal by mutableStateOf(Color(0xFF0061A4))
var currentThemeLightBlue by mutableStateOf(Color(0xFFD1E4FF))
var currentCurrencySymbol by mutableStateOf(defaultCurrencySymbol)

val FinanceTeal: Color
    get() = currentThemeTeal

val FinanceSlateDark = Color(0xFF1A1A1A)
val FinanceSlateLight = Color(0xFF44474E)
val FinanceSoftBg = Color(0xFFF4F6F8)
val CardBorderRed = Color(0xFFEF4444)

val FinanceLightBlue: Color
    get() = currentThemeLightBlue

val FinanceBorder = Color(0xFFDEE3EB)

val CurrencySymbol: String
    get() = currentCurrencySymbol

fun Double.formatCurrency(): String {
    return try {
        val esLocale = Locale("es", "ES")
        val symbols = DecimalFormatSymbols(esLocale)
        val formatter = DecimalFormat("#,##0.00", symbols)
        val formatted = formatter.format(this)
        when (currentCurrencySymbol) {
            "$" -> "$ $formatted"
            "£" -> "£ $formatted"
            "¥" -> "¥ $formatted"
            "COP" -> "COP $formatted"
            "MXN" -> "MXN $formatted"
            "ARS" -> "ARS $formatted"
            "CLP" -> "CLP $formatted"
            "PEN" -> "PEN $formatted"
            "R$" -> "R$ $formatted"
            "US$" -> "US$ $formatted"
            else -> "$formatted $currentCurrencySymbol"
        }
    } catch (e: Exception) {
        val formattedString = String.format("%.2f", this)
        "$formattedString $currentCurrencySymbol"
    }
}

enum class SettingDialogType {
    NONE,
    PROFILE,
    WIZARD_FIXED,
    PREFERENCES,
    DATABASE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceApp(viewModel: FinanceViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val dbProfile by viewModel.dbProfile.collectAsStateWithLifecycle()
    val isWizardComplete = dbProfile?.isWizardComplete == true

    val selectedIconId = dbProfile?.selectedIcon ?: "trending"
    val appLogoIcon = when(selectedIconId) {
        "wallet" -> Icons.Default.AccountBalanceWallet
        "savings" -> Icons.Default.Savings
        "payments" -> Icons.Default.Payments
        "chart" -> Icons.Default.ShowChart
        "star" -> Icons.Default.Star
        else -> Icons.Default.TrendingUp
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var activeDialog by remember { mutableStateOf(SettingDialogType.NONE) }
    var showAboutDialog by remember { mutableStateOf(false) }

    // Synchronize local UI colors to profile selected theme
    val activeThemeId = dbProfile?.selectedTheme ?: "azul"
    LaunchedEffect(activeThemeId) {
        when (activeThemeId) {
            "verde" -> {
                currentThemeTeal = Color(0xFF16A34A)
                currentThemeLightBlue = Color(0xFFDCFCE7)
            }
            "minimalista" -> {
                currentThemeTeal = Color(0xFF1F2937)
                currentThemeLightBlue = Color(0xFFE5E7EB)
            }
            "naranja" -> {
                currentThemeTeal = Color(0xFFEA580C)
                currentThemeLightBlue = Color(0xFFFFEAD5)
            }
            "purpura" -> {
                currentThemeTeal = Color(0xFF7C3AED)
                currentThemeLightBlue = Color(0xFFF3E8FF)
            }
            else -> {
                currentThemeTeal = Color(0xFF0061A4)
                currentThemeLightBlue = Color(0xFFD1E4FF)
            }
        }
    }

    val activeCurrency = dbProfile?.selectedCurrency ?: defaultCurrencySymbol
    LaunchedEffect(activeCurrency) {
        currentCurrencySymbol = activeCurrency
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isWizardComplete,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF0F121A), // Fondo base ultra oscuro
                modifier = Modifier.width(310.dp)
            ) {
                // 1. CABECERA MULTICAPA CON TU LOGO DE FONDO
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Altura contundente para que luzca el diseño
                ) {
                    // CAPA A: La imagen real de tu logo escalada y recortada de fondo
                    Image(
                        painter = painterResource(id = R.drawable.syntax_forge_logo),
                        contentDescription = null,
                        contentScale = ContentScale.Crop, // Recorta y llena el espacio con impacto
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(alpha = 0.22f) // Ajusta la opacidad aquí (0.22 es sutil y elegante)
                    )

                    // CAPA B: Degradado de fusión para oscurecer la base y que los textos resalten
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF0F121A).copy(alpha = 0.8f),
                                        Color(0xFF0F121A) // Fundido negro puro al llegar a las opciones
                                    )
                                )
                            )
                    )

                    // CAPA C: Los textos de la marca posicionados de forma limpia abajo a la izquierda
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "FinanceFlow",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            )
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981)) // LED verde de sistema activo
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SYNTAX FORGE DEV",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8), // Azul eléctrico corporativo
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. OPCIONES DE NAVEGACIÓN ESTILIZADAS (ITEMS)
                val customDrawerItemColors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.12f),
                    selectedIconColor = Color(0xFF38BDF8),
                    selectedTextColor = Color(0xFF38BDF8),
                    unselectedContainerColor = Color.Transparent,
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFFE2E8F0) // Texto claro sobre el menú oscuro
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.menu_profile), fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    selected = activeDialog == SettingDialogType.PROFILE,
                    onClick = {
                        scope.launch { drawerState.close() }
                        activeDialog = SettingDialogType.PROFILE
                    },
                    colors = customDrawerItemColors,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp).testTag("drawer_menu_profile")
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.menu_wizard_fixed), fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Build, contentDescription = null) },
                    selected = activeDialog == SettingDialogType.WIZARD_FIXED,
                    onClick = {
                        scope.launch { drawerState.close() }
                        activeDialog = SettingDialogType.WIZARD_FIXED
                    },
                    colors = customDrawerItemColors,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp).testTag("drawer_menu_wizard_fixed")
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.menu_preferences), fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    selected = activeDialog == SettingDialogType.PREFERENCES,
                    onClick = {
                        scope.launch { drawerState.close() }
                        activeDialog = SettingDialogType.PREFERENCES
                    },
                    colors = customDrawerItemColors,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp).testTag("drawer_menu_preferences")
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.menu_about), fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showAboutDialog = true
                    },
                    colors = customDrawerItemColors,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp).testTag("drawer_menu_about")
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.menu_report_error), fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        sendFeedback(context)
                    },
                    colors = customDrawerItemColors,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp).testTag("drawer_menu_report_error")
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 20.dp))

                // Botón destructivo inferior integrado estéticamente sin romper la vista
                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.menu_database), fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    selected = activeDialog == SettingDialogType.DATABASE,
                    onClick = {
                        scope.launch { drawerState.close() }
                        activeDialog = SettingDialogType.DATABASE
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                        selectedIconColor = Color(0xFFEF4444),
                        selectedTextColor = Color(0xFFEF4444),
                        unselectedIconColor = Color(0xFFEF4444).copy(alpha = 0.8f),
                        unselectedTextColor = Color(0xFFEF4444).copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp).testTag("drawer_menu_database")
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(FinanceSoftBg),
            topBar = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isWizardComplete) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(percent = 50))
                                        .background(FinanceTeal.copy(alpha = 0.08f))
                                        .clickable {
                                            scope.launch {
                                                drawerState.open()
                                            }
                                        }
                                        .size(40.dp)
                                        .testTag("hamburger_menu_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Abrir menú",
                                        tint = FinanceTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            // Logo with background pill matching theme
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FinanceTeal.copy(alpha = 0.10f))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = appLogoIcon,
                                    contentDescription = "Logo",
                                    tint = FinanceTeal,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "FinanceFlow",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = FinanceSlateDark,
                                        letterSpacing = (-0.5).sp
                                    )
                                )
                                Text(
                                    text = "100% Libre y Solidario",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FinanceTeal
                                    )
                                )
                            }
                        }

                        // Elegant, low-profile info icon on the right
                        IconButton(
                            onClick = { showAboutDialog = true },
                            modifier = Modifier.testTag("about_app_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Acerca de la app",
                                tint = FinanceSlateLight
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(FinanceSoftBg),
                color = FinanceSoftBg
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        is Screen.WizardStep1 -> WizardStep1Screen(viewModel)
                        is Screen.WizardStep2 -> WizardStep2Screen(viewModel)
                        is Screen.WizardStep3 -> WizardStep3Screen(viewModel)
                        is Screen.WizardStep4 -> WizardStep4Screen(viewModel)
                        is Screen.Dashboard -> DashboardScreen(viewModel, onShowAbout = { showAboutDialog = true })
                    }
                }
            }
        }
    }

    // Modal Dialog 1: Perfil e Ingresos
    if (activeDialog == SettingDialogType.PROFILE) {
        val currentProfile = dbProfile ?: com.example.data.FinancialProfile()
        var editIncome by remember { mutableStateOf(currentProfile.monthlyIncome.toString()) }
        var editIncomeDay by remember { mutableStateOf(currentProfile.incomeDay.toString()) }

        AlertDialog(
            onDismissRequest = { activeDialog = SettingDialogType.NONE },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = FinanceTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Perfil e Ingresos", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Configura tu neto mensual recibido y el día del mes correspondiente a tu cobro.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FinanceSlateLight
                    )

                    OutlinedTextField(
                        value = editIncome,
                        onValueChange = { editIncome = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Ingreso Neto Mensual ($currentCurrencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_income_input")
                    )

                    OutlinedTextField(
                        value = editIncomeDay,
                        onValueChange = { editIncomeDay = it.filter { c -> c.isDigit() } },
                        label = { Text("Día de Cobro (1-31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_income_day_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val incomeValue = editIncome.toDoubleOrNull() ?: currentProfile.monthlyIncome
                        val dayValue = editIncomeDay.toIntOrNull()?.coerceIn(1, 31) ?: currentProfile.incomeDay
                        viewModel.updateProfileIncomeAndDay(incomeValue, dayValue)
                        activeDialog = SettingDialogType.NONE
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                    modifier = Modifier.testTag("save_profile_btn")
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { activeDialog = SettingDialogType.NONE },
                    modifier = Modifier.testTag("cancel_profile_btn")
                ) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal Dialog 2: Gastos fijos del Wizard
    if (activeDialog == SettingDialogType.WIZARD_FIXED) {
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

        AlertDialog(
            onDismissRequest = { activeDialog = SettingDialogType.NONE },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = FinanceTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gastos fijos del Wizard", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                ) {
                    item {
                        Text(
                            "Ajusta los estados activos y cuotas de tus gastos periódicos especiales.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FinanceSlateLight
                        )
                    }

                    // Mascotas
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(imageVector = Icons.Default.Pets, contentDescription = null, tint = FinanceSlateLight, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Mascotas", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                Switch(
                                    checked = hasPets,
                                    onCheckedChange = { viewModel.togglePetsDashboard(it) },
                                    colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal),
                                    modifier = Modifier.testTag("dialog_pets_switch")
                                )
                            }
                            AnimatedVisibility(visible = hasPets) {
                                OutlinedTextField(
                                    value = petsCost,
                                    onValueChange = { viewModel.updatePetsCostDashboard(it) },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("dialog_pets_cost_input"),
                                    label = { Text("Importe Mensual Mascotas ($currentCurrencySymbol)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    // Hijos
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(imageVector = Icons.Default.ChildCare, contentDescription = null, tint = FinanceSlateLight, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Hijos / Bebés", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                Switch(
                                    checked = hasKids,
                                    onCheckedChange = { viewModel.toggleKidsDashboard(it) },
                                    colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal),
                                    modifier = Modifier.testTag("dialog_kids_switch")
                                )
                            }
                            AnimatedVisibility(visible = hasKids) {
                                OutlinedTextField(
                                    value = kidsCost,
                                    onValueChange = { viewModel.updateKidsCostDashboard(it) },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("dialog_kids_cost_input"),
                                    label = { Text("Importe Mensual Hijos ($currentCurrencySymbol)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    // Pareja (Shared)
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(imageVector = Icons.Default.People, contentDescription = null, tint = FinanceSlateLight, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Gastos Compartidos (Pareja)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                Switch(
                                    checked = sharedExpenses,
                                    onCheckedChange = { viewModel.toggleSharedExpensesDashboard(it) },
                                    colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal),
                                    modifier = Modifier.testTag("dialog_shared_switch")
                                )
                            }
                            AnimatedVisibility(visible = sharedExpenses) {
                                OutlinedTextField(
                                    value = partnerCont,
                                    onValueChange = { viewModel.updatePartnerContributionDashboard(it) },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("dialog_shared_contrib_input"),
                                    label = { Text("Aportación Mensual Pareja ($currentCurrencySymbol)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    // Suministros: Agua
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(imageVector = Icons.Default.WaterDrop, contentDescription = null, tint = FinanceSlateLight, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Suministro de Agua", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                Switch(
                                    checked = isWaterEnabled,
                                    onCheckedChange = { viewModel.toggleWaterSupplyDashboard(it) },
                                    colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal),
                                    modifier = Modifier.testTag("dialog_water_switch")
                                )
                            }
                            AnimatedVisibility(visible = isWaterEnabled) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = waterCost,
                                        onValueChange = { viewModel.updateWaterCostDashboard(it) },
                                        modifier = Modifier.fillMaxWidth().testTag("dialog_water_cost_input"),
                                        label = { Text("Importe Factura de Agua ($currentCurrencySymbol)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Frecuencia:", style = MaterialTheme.typography.bodySmall, color = FinanceSlateLight)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf("Mensual", "Bimensual").forEach { cycle ->
                                                val isSelected = waterBilling == cycle
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { viewModel.updateWaterBillingCycleDashboard(cycle) },
                                                    label = { Text(cycle, fontSize = 11.sp) },
                                                    modifier = Modifier.testTag("dialog_water_cycle_$cycle")
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Suministros: Electricidad
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = FinanceSlateLight, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Luz y Electricidad", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                Switch(
                                    checked = isElectricityEnabled,
                                    onCheckedChange = { viewModel.toggleElectricitySupplyDashboard(it) },
                                    colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal),
                                    modifier = Modifier.testTag("dialog_electricity_switch")
                                )
                            }
                            AnimatedVisibility(visible = isElectricityEnabled) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = electricityCost,
                                        onValueChange = { viewModel.updateElectricityCostDashboard(it) },
                                        modifier = Modifier.fillMaxWidth().testTag("dialog_electricity_cost_input"),
                                        label = { Text("Importe Factura de Luz ($currentCurrencySymbol)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Frecuencia:", style = MaterialTheme.typography.bodySmall, color = FinanceSlateLight)
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf("Mensual", "Bimensual").forEach { cycle ->
                                                val isSelected = electricityBilling == cycle
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { viewModel.updateElectricityBillingCycleDashboard(cycle) },
                                                    label = { Text(cycle, fontSize = 11.sp) },
                                                    modifier = Modifier.testTag("dialog_electricity_cycle_$cycle")
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeDialog = SettingDialogType.NONE },
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                    modifier = Modifier.testTag("close_wizard_fixed_btn")
                ) {
                    Text("Hecho")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal Dialog 3: Preferencias de UI
    if (activeDialog == SettingDialogType.PREFERENCES) {
        val hideNewMonthBannerState by viewModel.hideNewMonthBanner.collectAsStateWithLifecycle()
        val hideSmartCalendarBannerState by viewModel.hideSmartCalendarBanner.collectAsStateWithLifecycle()
        val currentProfile = dbProfile ?: com.example.data.FinancialProfile()
        val selectedTheme = currentProfile.selectedTheme
        val isPremium = dbProfile?.isProUser == true

        AlertDialog(
            onDismissRequest = { activeDialog = SettingDialogType.NONE },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = FinanceTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Preferencias de UI", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Configura tus preferencias visuales y reactiva banners ocultos o cambia la paleta de colores de la interfaz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FinanceSlateLight
                    )

                    // Banners switches
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Banners Informativos", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = FinanceSlateDark)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mostrar Banner 'Nuevo Mes'", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Switch(
                                checked = !hideNewMonthBannerState,
                                onCheckedChange = { viewModel.setHideNewMonthBanner(!it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal),
                                modifier = Modifier.testTag("dialog_switch_new_month_banner")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mostrar Banner 'Calendario Inteligente'", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Switch(
                                checked = !hideSmartCalendarBannerState,
                                onCheckedChange = { viewModel.setHideSmartCalendarBanner(!it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal),
                                modifier = Modifier.testTag("dialog_switch_smart_calendar_banner")
                            )
                        }
                    }

                    Divider()

                    // Color palette selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Temas de Color", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = FinanceSlateDark)

                        listOf(
                            Triple("azul", "Azul Nobara", Color(0xFF0061A4)),
                            Triple("verde", "Verde Dinero", Color(0xFF16A34A)),
                            Triple("minimalista", "Minimalista Oscuro", Color(0xFF1F2937)),
                            Triple("naranja", "Naranja Coral", Color(0xFFEA580C)),
                            Triple("purpura", "Púrpura Elegante", Color(0xFF7C3AED))
                        ).forEach { (themeId, themeName, themeColor) ->
                            val isSelected = selectedTheme == themeId
                            val selectThemeAction = {
                                viewModel.setSelectedTheme(themeId)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) FinanceTeal.copy(alpha = 0.08f) else Color.Transparent)
                                    .clickable { selectThemeAction() }
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
                                    color = if (isSelected) FinanceTeal else FinanceSlateDark,
                                    modifier = Modifier.weight(1f)
                                )
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectThemeAction() },
                                    colors = RadioButtonDefaults.colors(selectedColor = FinanceTeal),
                                    modifier = Modifier.testTag("radio_theme_$themeId")
                                )
                            }
                        }
                    }

                    Divider()

                    // Icon Selector Section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Icono de la App", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = FinanceSlateDark)
                        Text("Selecciona el símbolo principal de tu marca e interfaz de usuario:", style = MaterialTheme.typography.bodySmall, color = FinanceSlateLight)

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
                                val selectIconAction = {
                                    viewModel.setSelectedIcon(iconId)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) FinanceTeal.copy(alpha = 0.12f) else Color.Transparent)
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isSelected) FinanceTeal else Color.LightGray.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectIconAction() }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = iconVec,
                                        contentDescription = iconId,
                                        tint = if (isSelected) FinanceTeal else FinanceSlateLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    Divider()

                    // Currency Selector Section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Moneda Base de la Aplicación", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = FinanceSlateDark)
                        Text("Cambia el símbolo monetario global de FinanceFlow para todos tus presupuestos, resúmenes y cálculos financieros:", style = MaterialTheme.typography.bodySmall, color = FinanceSlateLight)

                        val currentCurrency = dbProfile?.selectedCurrency ?: "€"

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                                .background(if (isSelected) FinanceTeal.copy(alpha = 0.12f) else Color.Transparent)
                                                .border(
                                                    width = 1.5.dp,
                                                    color = if (isSelected) FinanceTeal else Color.LightGray.copy(alpha = 0.3f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable { viewModel.setSelectedCurrency(currSymbol) }
                                                .padding(horizontal = 10.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) FinanceTeal else Color.LightGray.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = currSymbol,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                        color = if (isSelected) Color.White else FinanceSlateDark,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                                Text(
                                                    text = currName,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                                    color = if (isSelected) FinanceTeal else FinanceSlateDark,
                                                    fontSize = 11.sp,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeDialog = SettingDialogType.NONE },
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                    modifier = Modifier.testTag("close_preferences_btn")
                ) {
                    Text("Aceptar")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Modal Dialog 4: Base de datos
    if (activeDialog == SettingDialogType.DATABASE) {
        AlertDialog(
            onDismissRequest = { activeDialog = SettingDialogType.NONE },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CardBorderRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restablecer Base de Datos", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = CardBorderRed)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "⚠️ ¡Atención! Esta acción borrará de forma permanente todos tus datos, ingresados en el asistente, presupuestos, gastos adicionales, facturas mensuales y registros históricos.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = FinanceSlateDark
                    )
                    Text(
                        "Al proceder, la aplicación se reiniciará por completo, purgando la base de datos local y abriendo de nuevo el asistente de configuración inicial.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FinanceSlateLight
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        activeDialog = SettingDialogType.NONE
                        viewModel.resetSystemConfiguration()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CardBorderRed),
                    modifier = Modifier.testTag("confirm_database_reset_btn")
                ) {
                    Text("Restablecer Todo", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { activeDialog = SettingDialogType.NONE },
                    modifier = Modifier.testTag("cancel_database_reset_btn")
                ) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Dialogo Informativo "Acerca de"
    if (showAboutDialog) {
        val context = androidx.compose.ui.platform.LocalContext.current
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = Color.White,
            title = null,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(FinanceTeal, Color(0xFF0D9488))
                                )
                            )
                            .padding(vertical = 24.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "FinanceFlow",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = (-0.5).sp
                                )
                            )
                            Text(
                                text = "Versión Libre e Inteligente",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.85f)),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Privacidad y Control Real 🔒",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = FinanceTeal)
                        )
                        Text(
                            text = "FinanceFlow es un desarrollo altruista y 100% libre de publicidad. No hay suscripciones, limitaciones de registro, ni datos vendidos al exterior. Toda tu base de datos se guarda de forma segura únicamente en tu propio dispositivo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FinanceSlateDark
                        )
                        HorizontalDivider(color = FinanceBorder.copy(alpha = 0.5f))
                        Text(
                            text = "Cálculos Inteligentes Locales 🧮",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = FinanceTeal)
                        )
                        Text(
                            text = "FinanceFlow calcula en tiempo real tus presupuestos, el dinero semanal libre distribuido y tus proyecciones de ahorro de forma precisa y 100% offline. Sin APIs externas lentas ni pérdida de conexión.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FinanceSlateDark
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            uriHandler.openUri("https://ko-fi.com/logichivedev")
                        } catch (e: Exception) {}
                        showAboutDialog = false
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
                    onClick = { showAboutDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Entendido, cerrar", color = FinanceSlateLight, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun StepBarHeader(step: Int, title: String, subtitle: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                clip = true
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        border = BorderStroke(1.dp, Color(0xFFECEFF1))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 1..4) {
                    val isActive = i <= step
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(if (isActive) FinanceTeal else FinanceLightBlue)
                    )
                }
            }

            Text(
                text = "PASO $step DE 4",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = FinanceTeal,
                    letterSpacing = 1.5.sp
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = FinanceSlateDark,
                    fontSize = 20.sp
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = FinanceSlateLight,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun WizardStep1Screen(viewModel: FinanceViewModel) {
    val incomeInput by viewModel.incomeInput.collectAsStateWithLifecycle()
    val wizardBankBalanceInput by viewModel.wizardBankBalanceInput.collectAsStateWithLifecycle()
    val incomeDayInput by viewModel.incomeDayInput.collectAsStateWithLifecycle()
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // 🛠️ CAPA 1: Forzamos el fondo gris claro de la app para que las tarjetas blancas contrasten y "floten"
            .background(FinanceSoftBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        StepBarHeader(
            step = 1,
            title = "Ingreso Mensual",
            subtitle = "Configura tus ingresos mensuales y tu saldo disponible actual para tus cálculos."
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 🛠️ CAPA 2: La tarjeta contenedora ahora tiene bordes muy redondeados y sombra real con profundidad
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(28.dp),
                    clip = true
                ),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White) // Blanco puro sobre fondo gris
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono metido dentro de una pastilla de color suave para que no parezca flotando a lápiz
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(FinanceTeal.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalAtm,
                        contentDescription = "Moneda",
                        tint = FinanceTeal,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Tus Ingresos Promedio",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = FinanceSlateDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo de texto estilizado de neobanco
                OutlinedTextField(
                    value = incomeInput,
                    onValueChange = {
                        isError = false
                        viewModel.setIncomeInput(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_income_input"),
                    label = { Text("Ingresos totales o nómina ($currentCurrencySymbol)") },
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraBold,
                        color = FinanceTeal
                    ),
                    placeholder = { Text("0.00", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    singleLine = true,
                    // Bordes muy redondeados que rompen la estructura recta anterior
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FinanceTeal,
                        unfocusedBorderColor = FinanceBorder.copy(alpha = 0.7f),
                        focusedContainerColor = FinanceSoftBg.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Banner informativo refinado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FinanceSoftBg, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = FinanceSlateLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Si tu sueldo varía cada mes o cobras pagas extras prorrateadas, introduce una media estimada.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        color = FinanceSlateLight
                    )
                }

                if (isError) {
                    Text(
                        text = "Por favor ingresa un monto válido superior a cero.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = FinanceBorder.copy(alpha = 0.5f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(24.dp))

                // Pastilla para el segundo icono (Calendario)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(FinanceTeal.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = "Calendario",
                        tint = FinanceTeal,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Día del Mes de tu Ingreso Principal",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = FinanceSlateDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = incomeDayInput,
                    onValueChange = { input ->
                        val cleaned = input.filter { it.isDigit() }
                        val dayVal = cleaned.toIntOrNull()
                        if (dayVal == null || (dayVal in 1..31)) {
                            viewModel.updateIncomeDayInput(cleaned)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_income_day_input"),
                    label = { Text("Día del mes (1 - 31)") },
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraBold,
                        color = FinanceTeal
                    ),
                    placeholder = { Text("10", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FinanceTeal,
                        unfocusedBorderColor = FinanceBorder.copy(alpha = 0.7f),
                        focusedContainerColor = FinanceSoftBg.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                val sliderDayValue = incomeDayInput.toIntOrNull()?.toFloat()?.coerceIn(1f, 31f) ?: 10f
                Slider(
                    value = sliderDayValue,
                    onValueChange = { newValue ->
                        viewModel.updateIncomeDayInput(newValue.toInt().toString())
                    },
                    valueRange = 1f..31f,
                    steps = 29,
                    modifier = Modifier.fillMaxWidth().testTag("setup_income_day_slider"),
                    colors = SliderDefaults.colors(
                        activeTrackColor = FinanceTeal,
                        inactiveTrackColor = FinanceLightBlue,
                        thumbColor = FinanceTeal
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0F2FE), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color(0xFF0369A1),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "El sistema utiliza este día para asegurar y retener proactivamente el dinero de tus facturas.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        color = Color(0xFF0369A1)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = FinanceBorder.copy(alpha = 0.5f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(24.dp))

                // Pastilla para el tercer icono (Banco)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "Banco",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "¿Cuánto tienes en el banco hoy?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = FinanceSlateDark
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = wizardBankBalanceInput,
                    onValueChange = { viewModel.setWizardBankBalanceInput(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_bank_balance_input"),
                    label = { Text("Disponible en tu banco actualmente ($currentCurrencySymbol)") },
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    ),
                    placeholder = { Text("0.00 (opcional)", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0F172A),
                        unfocusedBorderColor = FinanceBorder.copy(alpha = 0.7f),
                        focusedContainerColor = FinanceSoftBg.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD1FAE5), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Check",
                        tint = Color(0xFF065F46),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "¡Indicando lo que tienes hoy, el cálculo de tu saldo disponible real se activará inmediatamente!",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        color = Color(0xFF065F46)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Botón principal Premium con altura y esquinas contundentes
                Button(
                    onClick = {
                        val parsed = incomeInput.toDoubleOrNull()
                        if (parsed != null && parsed > 0.0) {
                            viewModel.completeStep1()
                        } else {
                            isError = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .shadow(4.dp, RoundedCornerShape(18.dp))
                        .testTag("step1_next_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal)
                ) {
                    Text(
                        "Continuar al Paso 2",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White)
                    )
                }
            }
        }
    }
}

@Composable
fun WizardStep2Screen(viewModel: FinanceViewModel) {
    val fixedExpenses by viewModel.fixedExpenses.collectAsStateWithLifecycle()

    val premiumTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = FinanceTeal,
        unfocusedBorderColor = FinanceBorder.copy(alpha = 0.7f),
        focusedContainerColor = FinanceSoftBg.copy(alpha = 0.5f),
        unfocusedContainerColor = Color.White,
        focusedLabelColor = FinanceTeal,
        unfocusedLabelColor = FinanceSlateLight,
        cursorColor = FinanceTeal
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FinanceSoftBg)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        StepBarHeader(
            step = 2,
            title = "Gastos Fijos Obligatorios",
            subtitle = "Configura tus facturas mensuales obligatorias directamente en un solo lugar."
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(fixedExpenses, key = { it.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(28.dp),
                            clip = true
                        ),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(FinanceTeal.copy(alpha = 0.1f))
                                    .padding(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = "Bill",
                                    tint = FinanceTeal,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            OutlinedTextField(
                                value = item.name,
                                onValueChange = { viewModel.updateFixedExpense(item.id, it, item.amount, item.payDay) },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("fixed_name_${item.id}"),
                                label = { Text("Nombre") },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                colors = premiumTextFieldColors
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedTextField(
                                value = item.amount,
                                onValueChange = { viewModel.updateFixedExpense(item.id, item.name, it, item.payDay) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("fixed_amt_${item.id}"),
                                label = { Text("Monto (€)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(16.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = FinanceTeal,
                                    fontWeight = FontWeight.Bold
                                ),
                                colors = premiumTextFieldColors
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = { viewModel.deleteFixedExpense(item.id) },
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .testTag("delete_fixed_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = CardBorderRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 56.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = item.payDay,
                                onValueChange = { viewModel.updateFixedExpense(item.id, item.name, item.amount, it) },
                                modifier = Modifier
                                    .width(130.dp)
                                    .testTag("fixed_payday_${item.id}"),
                                label = { Text("Día pago (Opc.)", fontSize = 11.sp) },
                                placeholder = { Text("Ej: 5", fontSize = 11.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                colors = premiumTextFieldColors
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Día del mes para auto-pagar",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = FinanceSlateLight)
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
                        .height(52.dp)
                        .testTag("add_custom_fixed"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FinanceTeal),
                    border = BorderStroke(1.5.dp, FinanceTeal)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "+ Añadir Gasto Personalizado",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.completeStep2() },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .shadow(4.dp, RoundedCornerShape(18.dp))
                .testTag("step2_next_button"),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal)
        ) {
            Text(
                "Siguiente: Presupuestos Variables",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun WizardStep3Screen(viewModel: FinanceViewModel) {
    val variableBudgets by viewModel.variableBudgets.collectAsStateWithLifecycle()

    val premiumTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = FinanceTeal,
        unfocusedBorderColor = FinanceBorder.copy(alpha = 0.7f),
        focusedContainerColor = FinanceSoftBg.copy(alpha = 0.5f),
        unfocusedContainerColor = Color.White,
        focusedLabelColor = FinanceTeal,
        unfocusedLabelColor = FinanceSlateLight,
        cursorColor = FinanceTeal
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FinanceSoftBg)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        StepBarHeader(
            step = 3,
            title = "Presupuestos de Gastos",
            subtitle = "Límites mensuales sugeridos para comida, ocio, transporte, etc."
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(variableBudgets, key = { it.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(28.dp),
                            clip = true
                        ),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(FinanceTeal.copy(alpha = 0.1f))
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wallet,
                                contentDescription = "Wallet",
                                tint = FinanceTeal,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        OutlinedTextField(
                            value = item.name,
                            onValueChange = { viewModel.updateVariableBudget(item.id, it, item.amount) },
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("var_name_${item.id}"),
                            label = { Text("Categoría") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            colors = premiumTextFieldColors
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = item.amount,
                            onValueChange = { viewModel.updateVariableBudget(item.id, item.name, it) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("var_amt_${item.id}"),
                            label = { Text("Tope (€)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = FinanceTeal,
                                fontWeight = FontWeight.Bold
                            ),
                            colors = premiumTextFieldColors
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = { viewModel.deleteVariableBudget(item.id) },
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .testTag("delete_var_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = CardBorderRed
                            )
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { viewModel.addCustomVariableBudget() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("add_custom_var"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FinanceTeal),
                    border = BorderStroke(1.5.dp, FinanceTeal)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "+ Añadir Presupuesto Personalizado",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.completeStep3() },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .shadow(4.dp, RoundedCornerShape(18.dp))
                .testTag("step3_next_button"),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal)
        ) {
            Text(
                "Siguiente: Extras y Personalización",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun WizardStep4Screen(viewModel: FinanceViewModel) {
    val hasPets by viewModel.hasPets.collectAsStateWithLifecycle()
    val petsCost by viewModel.petsCost.collectAsStateWithLifecycle()
    val hasKids by viewModel.hasKids.collectAsStateWithLifecycle()
    val kidsCost by viewModel.kidsCost.collectAsStateWithLifecycle()
    val sharedExpenses by viewModel.sharedExpenses.collectAsStateWithLifecycle()
    val waterBilling by viewModel.waterBilling.collectAsStateWithLifecycle()
    val waterCost by viewModel.waterCost.collectAsStateWithLifecycle()
    val isWaterEnabled by viewModel.isWaterEnabled.collectAsStateWithLifecycle()
    val electricityBilling by viewModel.electricityBilling.collectAsStateWithLifecycle()
    val electricityCost by viewModel.electricityCost.collectAsStateWithLifecycle()
    val isElectricityEnabled by viewModel.isElectricityEnabled.collectAsStateWithLifecycle()
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

    val premiumTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = FinanceTeal,
        unfocusedBorderColor = FinanceBorder.copy(alpha = 0.7f),
        focusedContainerColor = FinanceSoftBg.copy(alpha = 0.5f),
        unfocusedContainerColor = Color.White,
        focusedLabelColor = FinanceTeal,
        unfocusedLabelColor = FinanceSlateLight,
        cursorColor = FinanceTeal
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FinanceSoftBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StepBarHeader(
                step = 4,
                title = "Filtros y Personalización",
                subtitle = "Agrega tus características de hogar, suministros y suscripciones digitales."
            )
        }

        item {
            Text(
                text = "Perfil de Hogar",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                color = FinanceSlateDark,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(28.dp),
                        clip = true
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(FinanceTeal.copy(alpha = 0.1f))
                                .padding(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Pets, contentDescription = "Pets", tint = FinanceTeal, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tengo Mascotas", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                            Text("Alimentación y veterinaria", style = MaterialTheme.typography.bodySmall, color = FinanceSlateLight)
                        }
                        Switch(
                            checked = hasPets,
                            onCheckedChange = { viewModel.setHasPets(it) },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = FinanceTeal,
                                checkedThumbColor = Color.White
                            ),
                            modifier = Modifier.testTag("pets_switch")
                        )
                    }

                    AnimatedVisibility(visible = hasPets) {
                        OutlinedTextField(
                            value = petsCost,
                            onValueChange = { viewModel.setPetsCost(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp)
                                .testTag("pets_cost_input"),
                            label = { Text("Gasto mensual estimado (€)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = FinanceTeal, fontWeight = FontWeight.Bold),
                            colors = premiumTextFieldColors
                        )
                    }

                    HorizontalDivider(color = FinanceBorder.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(FinanceTeal.copy(alpha = 0.1f))
                                .padding(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ChildCare, contentDescription = "Kids", tint = FinanceTeal, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tengo Hijos", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                            Text("Pañales, educación, cuidado", style = MaterialTheme.typography.bodySmall, color = FinanceSlateLight)
                        }
                        Switch(
                            checked = hasKids,
                            onCheckedChange = { viewModel.setHasKids(it) },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = FinanceTeal,
                                checkedThumbColor = Color.White
                            ),
                            modifier = Modifier.testTag("kids_switch")
                        )
                    }

                    AnimatedVisibility(visible = hasKids) {
                        OutlinedTextField(
                            value = kidsCost,
                            onValueChange = { viewModel.setKidsCost(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp)
                                .testTag("kids_cost_input"),
                            label = { Text("Gasto mensual estimado (€)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = FinanceTeal, fontWeight = FontWeight.Bold),
                            colors = premiumTextFieldColors
                        )
                    }

                    HorizontalDivider(color = FinanceBorder.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(FinanceTeal.copy(alpha = 0.1f))
                                .padding(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.People, contentDescription = "Shared", tint = FinanceTeal, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Gastos Compartidos (Pareja)", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                            Text("Optimizar fondo presupuestario común", style = MaterialTheme.typography.bodySmall, color = FinanceSlateLight)
                        }
                        Switch(
                            checked = sharedExpenses,
                            onCheckedChange = { viewModel.setSharedExpenses(it) },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = FinanceTeal,
                                checkedThumbColor = Color.White
                            ),
                            modifier = Modifier.testTag("shared_switch")
                        )
                    }

                    AnimatedVisibility(visible = sharedExpenses) {
                        Column {
                            Spacer(modifier = Modifier.height(14.dp))
                            val partnerContrib by viewModel.partnerContribution.collectAsStateWithLifecycle()
                            OutlinedTextField(
                                value = partnerContrib,
                                onValueChange = { viewModel.setPartnerContribution(it) },
                                modifier = Modifier.fillMaxWidth().testTag("partner_contribution_input"),
                                label = { Text("Aportación mensual de la pareja (€)") },
                                placeholder = { Text("Ej: 450") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(16.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = FinanceTeal, fontWeight = FontWeight.Bold),
                                colors = premiumTextFieldColors
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Hogar Suministros Básicos",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                color = FinanceSlateDark
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(28.dp),
                        clip = true
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(FinanceTeal.copy(alpha = 0.1f))
                                .padding(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = "Electricity", tint = FinanceTeal, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Suministro de Luz", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                            Text("Configuración de luz local", style = MaterialTheme.typography.bodySmall, color = FinanceSlateLight)
                        }
                        Switch(
                            checked = isElectricityEnabled,
                            onCheckedChange = { viewModel.setElectricityEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = FinanceTeal,
                                checkedThumbColor = Color.White
                            ),
                            modifier = Modifier.testTag("electricity_switch")
                        )
                    }

                    AnimatedVisibility(visible = isElectricityEnabled) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = electricityCost,
                                    onValueChange = { viewModel.setElectricityCost(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("electricity_cost"),
                                    label = { Text("Monto de Luz") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(16.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = FinanceTeal, fontWeight = FontWeight.Bold),
                                    colors = premiumTextFieldColors
                                )

                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text("Ciclo de Factura:", style = MaterialTheme.typography.bodySmall, color = FinanceSlateLight)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        FilterChip(
                                            selected = electricityBilling == "Mensual",
                                            onClick = { viewModel.setElectricityBilling("Mensual") },
                                            label = { Text("Mensual", fontSize = 11.sp) },
                                            modifier = Modifier.testTag("ele_billing_mensual")
                                        )
                                        FilterChip(
                                            selected = electricityBilling == "Bimensual",
                                            onClick = { viewModel.setElectricityBilling("Bimensual") },
                                            label = { Text("Bimensual", fontSize = 11.sp) },
                                            modifier = Modifier.testTag("ele_billing_bimensual")
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            val elePayDay by viewModel.electricityPayDayValue.collectAsStateWithLifecycle()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = elePayDay,
                                    onValueChange = { viewModel.setElectricityPayDay(it) },
                                    modifier = Modifier
                                        .width(130.dp)
                                        .testTag("electricity_payday"),
                                    label = { Text("Día pago (Opc.)", fontSize = 11.sp) },
                                    placeholder = { Text("Ej: 15", fontSize = 11.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(14.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    colors = premiumTextFieldColors
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Día del mes para auto-pagar luz",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = FinanceSlateLight)
                                )
                            }

                            if (electricityBilling == "Bimensual") {
                                val original = electricityCost.toDoubleOrNull() ?: 0.0
                                Text(
                                    text = "Fórmula: ${original.formatCurrency()} bimensual se provisiona como ${(original/2).formatCurrency()} mensual en el Dashboard.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = FinanceTeal, fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = FinanceBorder.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(FinanceTeal.copy(alpha = 0.1f))
                                .padding(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.WaterDrop, contentDescription = "Water", tint = FinanceTeal, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Suministro de Agua", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                            Text("Configuración de agua doméstica", style = MaterialTheme.typography.bodySmall, color = FinanceSlateLight)
                        }
                        Switch(
                            checked = isWaterEnabled,
                            onCheckedChange = { viewModel.setWaterEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = FinanceTeal,
                                checkedThumbColor = Color.White
                            ),
                            modifier = Modifier.testTag("water_switch")
                        )
                    }

                    AnimatedVisibility(visible = isWaterEnabled) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = waterCost,
                                    onValueChange = { viewModel.setWaterCost(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("water_cost"),
                                    label = { Text("Monto de Agua") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(16.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = FinanceTeal, fontWeight = FontWeight.Bold),
                                    colors = premiumTextFieldColors
                                )

                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text("Ciclo de Factura:", style = MaterialTheme.typography.bodySmall, color = FinanceSlateLight)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        FilterChip(
                                            selected = waterBilling == "Mensual",
                                            onClick = { viewModel.setWaterBilling("Mensual") },
                                            label = { Text("Mensual", fontSize = 11.sp) },
                                            modifier = Modifier.testTag("water_billing_mensual")
                                        )
                                        FilterChip(
                                            selected = waterBilling == "Bimensual",
                                            onClick = { viewModel.setWaterBilling("Bimensual") },
                                            label = { Text("Bimensual", fontSize = 11.sp) },
                                            modifier = Modifier.testTag("water_billing_bimensual")
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            val watPayDay by viewModel.waterPayDayValue.collectAsStateWithLifecycle()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = watPayDay,
                                    onValueChange = { viewModel.setWaterPayDay(it) },
                                    modifier = Modifier
                                        .width(130.dp)
                                        .testTag("water_payday"),
                                    label = { Text("Día pago (Opc.)", fontSize = 11.sp) },
                                    placeholder = { Text("Ej: 18", fontSize = 11.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(14.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    colors = premiumTextFieldColors
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Día del mes para auto-pagar agua",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = FinanceSlateLight)
                                )
                            }

                            if (waterBilling == "Bimensual") {
                                val original = waterCost.toDoubleOrNull() ?: 0.0
                                Text(
                                    text = "Fórmula: ${original.formatCurrency()} bimensual se provisiona como ${(original/2).formatCurrency()} mensual en el Dashboard.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = FinanceTeal, fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Streaming y Suscripciones",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                color = FinanceSlateDark,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SubscriptionConfigRow(
                    title = "Netflix",
                    isActive = netflixActive,
                    onActiveChange = { viewModel.setNetflixActive(it) },
                    cost = netflixCost,
                    onCostChange = { viewModel.setNetflixCost(it) },
                    tagPrefix = "netflix"
                )

                SubscriptionConfigRow(
                    title = "HBO Max",
                    isActive = hboActive,
                    onActiveChange = { viewModel.setHboActive(it) },
                    cost = hboCost,
                    onCostChange = { viewModel.setHboCost(it) },
                    tagPrefix = "hbo"
                )

                SubscriptionConfigRow(
                    title = "Disney+",
                    isActive = disneyActive,
                    onActiveChange = { viewModel.setDisneyActive(it) },
                    cost = disneyCost,
                    onCostChange = { viewModel.setDisneyCost(it) },
                    tagPrefix = "disney"
                )

                SubscriptionConfigRow(
                    title = "Juegos (PlayStation/Xbox/Steam)",
                    isActive = juegosActive,
                    onActiveChange = { viewModel.setJuegosActive(it) },
                    cost = juegosCost,
                    onCostChange = { viewModel.setJuegosCost(it) },
                    tagPrefix = "juegos"
                )

                SubscriptionConfigRow(
                    title = "Amazon Prime",
                    isActive = amazonPrimeActive,
                    onActiveChange = { viewModel.setAmazonPrimeActive(it) },
                    cost = amazonPrimeCost,
                    onCostChange = { viewModel.setAmazonPrimeCost(it) },
                    tagPrefix = "amazon_prime"
                )

                SubscriptionConfigRow(
                    title = "Spotify",
                    isActive = spotifyActive,
                    onActiveChange = { viewModel.setSpotifyActive(it) },
                    cost = spotifyCost,
                    onCostChange = { viewModel.setSpotifyCost(it) },
                    tagPrefix = "spotify"
                )

                SubscriptionConfigRow(
                    title = "YouTube Premium",
                    isActive = youtubePremiumActive,
                    onActiveChange = { viewModel.setYoutubePremiumActive(it) },
                    cost = youtubePremiumCost,
                    onCostChange = { viewModel.setYoutubePremiumCost(it) },
                    tagPrefix = "youtube_premium"
                )

                SubscriptionConfigRow(
                    title = "YouTube Music",
                    isActive = youtubeMusicActive,
                    onActiveChange = { viewModel.setYoutubeMusicActive(it) },
                    cost = youtubeMusicCost,
                    onCostChange = { viewModel.setYoutubeMusicCost(it) },
                    tagPrefix = "youtube_music"
                )
            }
        }

        if (customStreams.isNotEmpty()) {
            item {
                Text(
                    text = "Suscripciones Personalizadas",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = FinanceSlateLight,
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 4.dp)
                )
            }

            items(customStreams) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            clip = true
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFECEFF1))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = item.name,
                            onValueChange = { viewModel.updateCustomSubscription(item.id, it, item.amount) },
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("cs_name_${item.id}"),
                            label = { Text("Nombre") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = premiumTextFieldColors
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedTextField(
                            value = item.amount,
                            onValueChange = { viewModel.updateCustomSubscription(item.id, item.name, it) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cs_amt_${item.id}"),
                            label = { Text("Monto (€)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = FinanceTeal, fontWeight = FontWeight.Bold),
                            colors = premiumTextFieldColors
                        )

                        IconButton(onClick = { viewModel.deleteCustomSub(item.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = CardBorderRed)
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { viewModel.addCustomSubscription() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("add_custom_subscription"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FinanceTeal),
                border = BorderStroke(1.5.dp, FinanceTeal)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(6.dp))
                Text("+ Añadir Suscripción Personalizada", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold))
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.completeWizard() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(4.dp, RoundedCornerShape(18.dp))
                    .testTag("wizard_complete_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal)
            ) {
                Text(
                    "Finalizar y Ver Dashboard",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color.White)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SubscriptionConfigRow(
    title: String,
    isActive: Boolean,
    onActiveChange: (Boolean) -> Unit,
    cost: String,
    onCostChange: (String) -> Unit,
    tagPrefix: String
) {
    var showDialog by remember { mutableStateOf(false) }
    var tempPriceStr by remember(cost) { mutableStateOf(cost) }
    var localIsActive by remember(isActive) { mutableStateOf(isActive) }
    var billingCycle by remember { mutableStateOf("Mensual") }

    val numericCost = cost.toDoubleOrNull() ?: 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isActive) 3.dp else 1.dp,
                shape = RoundedCornerShape(16.dp),
                clip = true
            )
            .clickable { showDialog = true }
            .testTag("${tagPrefix}_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color.White else Color(0xFFF9FAFB)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) Color(0xFFECEFF1) else Color(0xFFE0E0E0).copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubscriptionLogo(
                title = title,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = if (isActive) FinanceSlateDark else FinanceSlateDark.copy(alpha = 0.5f)
                )
                Text(
                    text = if (isActive) "Suscripción activa • $billingCycle" else "Desactivada",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isActive) FinanceSlateLight else FinanceSlateLight.copy(alpha = 0.4f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (isActive) {
                    Text(
                        text = numericCost.formatCurrency(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = FinanceSlateDark
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                } else {
                    Text(
                        text = "Configurar",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = FinanceTeal
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FinanceTeal.copy(alpha = 0.08f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .widthIn(max = 440.dp)
                .wrapContentHeight()
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp)),
            containerColor = Color.White,
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp, 4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0))
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SubscriptionLogo(
                            title = title,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FinanceSlateDark
                                )
                            )
                            Text(
                                text = "Ajustes de pago recurrente",
                                style = MaterialTheme.typography.bodySmall,
                                color = FinanceSlateLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (localIsActive) Color(0xFFE6F4EA) else Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (localIsActive) Icons.Default.Link else Icons.Default.LinkOff,
                                    contentDescription = "Link state",
                                    tint = if (localIsActive) Color(0xFF10B981) else FinanceSlateLight,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Vincular Suscripción",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = FinanceSlateDark
                                )
                                Text(
                                    text = if (localIsActive) "Cálculo activo en dashboard" else "Suscripción desvinculada",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FinanceSlateLight
                                )
                            }
                        }
                        Switch(
                            checked = localIsActive,
                            onCheckedChange = { localIsActive = it },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = FinanceTeal,
                                checkedThumbColor = Color.White
                            ),
                            modifier = Modifier.testTag("${tagPrefix}_modal_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AnimatedVisibility(visible = localIsActive) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = tempPriceStr,
                                onValueChange = { newVal ->
                                    val filtered = newVal.filter { it.isDigit() || it == '.' || it == ',' }
                                        .replace(',', '.')
                                    tempPriceStr = filtered
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("${tagPrefix}_modal_cost_input"),
                                label = { Text("Importe mensual (€)") },
                                placeholder = { Text("0.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    color = FinanceTeal,
                                    fontWeight = FontWeight.Bold
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FinanceTeal,
                                    focusedLabelColor = FinanceTeal,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocalAtm,
                                        contentDescription = "€",
                                        tint = FinanceTeal
                                    )
                                },
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Ciclo de Cobro:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = FinanceSlateLight,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Mensual", "Anual").forEach { cycle ->
                                    val isSelected = billingCycle == cycle
                                    val cardColor = if (isSelected) FinanceTeal else Color(0xFFF1F5F9)
                                    val textColor = if (isSelected) Color.White else FinanceSlateDark

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(cardColor)
                                            .clickable { billingCycle = cycle }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cycle,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = textColor
                                        )
                                    }
                                }
                            }

                            if (billingCycle == "Anual") {
                                val costValue = tempPriceStr.toDoubleOrNull() ?: 0.0
                                Text(
                                    text = "Nota: La facturación Anual de ${costValue.formatCurrency()} se dividirá en ${(costValue / 12).formatCurrency()} mensuales para tu presupuesto del dashboard.",
                                    style = MaterialTheme.typography.labelSmall.copy(color = FinanceTeal),
                                    modifier = Modifier.padding(top = 10.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalPrice = if (billingCycle == "Anual") {
                            val computedCost = (tempPriceStr.toDoubleOrNull() ?: 0.0) / 12
                            String.format(java.util.Locale.US, "%.2f", computedCost)
                        } else {
                            tempPriceStr
                        }
                        onActiveChange(localIsActive)
                        onCostChange(finalPrice)
                        showDialog = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("${tagPrefix}_modal_save_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal)
                ) {
                    Text(
                        text = "Guardar Cambios",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        localIsActive = false
                        onActiveChange(false)
                        showDialog = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .testTag("${tagPrefix}_modal_remove_button")
                ) {
                    Text(
                        text = "Desvincular Suscripción",
                        color = CardBorderRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }
}

@Composable
fun DashboardScreen(viewModel: FinanceViewModel, onShowAbout: () -> Unit) {
    val dbProfile by viewModel.dbProfile.collectAsStateWithLifecycle()
    val dbCategories by viewModel.dbCategories.collectAsStateWithLifecycle()
    val dbVariableExpenses by viewModel.dbVariableExpenses.collectAsStateWithLifecycle()
    val totalFixedAmount by viewModel.totalGastosFijos.collectAsStateWithLifecycle()
    val totalVariableSpent by viewModel.totalGastosVariables.collectAsStateWithLifecycle()
    val saldoRestanteDisponible by viewModel.saldoRestanteDisponible.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var activeTab by remember { mutableStateOf(DashboardTab.RESUMEN) }
    val income = dbProfile?.monthlyIncome ?: 0.0
    val fixedExpenses = dbCategories.filter { it.isFixed }
    val paidFixedAmount = fixedExpenses.filter { it.isPaid }.sumOf { it.limitAmount }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = activeTab == DashboardTab.RESUMEN,
                    onClick = { activeTab = DashboardTab.RESUMEN },
                    label = { Text("Resumen", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                    icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Resumen") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FinanceTeal,
                        selectedTextColor = FinanceTeal,
                        indicatorColor = FinanceTeal.copy(alpha = 0.1f),
                        unselectedIconColor = FinanceSlateLight,
                        unselectedTextColor = FinanceSlateLight
                    ),
                    modifier = Modifier.testTag("tab_resumen")
                )
                NavigationBarItem(
                    selected = activeTab == DashboardTab.GASTOS_FIJOS,
                    onClick = { activeTab = DashboardTab.GASTOS_FIJOS },
                    label = { Text("Fijos", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                    icon = { Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = "Gastos Fijos") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FinanceTeal,
                        selectedTextColor = FinanceTeal,
                        indicatorColor = FinanceTeal.copy(alpha = 0.1f),
                        unselectedIconColor = FinanceSlateLight,
                        unselectedTextColor = FinanceSlateLight
                    ),
                    modifier = Modifier.testTag("tab_fijos")
                )
                NavigationBarItem(
                    selected = activeTab == DashboardTab.GASTOS_VARIABLES,
                    onClick = { activeTab = DashboardTab.GASTOS_VARIABLES },
                    label = { Text("Variables", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                    icon = { Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = "Gastos Variables") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FinanceTeal,
                        selectedTextColor = FinanceTeal,
                        indicatorColor = FinanceTeal.copy(alpha = 0.1f),
                        unselectedIconColor = FinanceSlateLight,
                        unselectedTextColor = FinanceSlateLight
                    ),
                    modifier = Modifier.testTag("tab_variables")
                )
                NavigationBarItem(
                    selected = activeTab == DashboardTab.ANALISIS,
                    onClick = { activeTab = DashboardTab.ANALISIS },
                    label = { Text("Análisis", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                    icon = { Icon(imageVector = Icons.Default.TrendingUp, contentDescription = "Análisis") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FinanceTeal,
                        selectedTextColor = FinanceTeal,
                        indicatorColor = FinanceTeal.copy(alpha = 0.1f),
                        unselectedIconColor = FinanceSlateLight,
                        unselectedTextColor = FinanceSlateLight
                    ),
                    modifier = Modifier.testTag("tab_analisis")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(FinanceSoftBg)
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    DashboardTab.RESUMEN -> SummaryPane(
                        viewModel = viewModel,
                        income = income,
                        paidFixed = paidFixedAmount,
                        totalFixed = totalFixedAmount,
                        saldoRestante = saldoRestanteDisponible,
                        variableSpent = totalVariableSpent
                    )
                    DashboardTab.GASTOS_FIJOS -> FixedBillsPane(
                        viewModel = viewModel,
                        fixedExpenses = fixedExpenses
                    )
                    DashboardTab.GASTOS_VARIABLES -> VariableSpentPane(
                        viewModel = viewModel,
                        dbCategories = dbCategories,
                        variableExpenses = dbVariableExpenses,
                        totalSpent = totalVariableSpent
                    )
                    DashboardTab.ANALISIS -> AnalysisPane(
                        viewModel = viewModel,
                        income = income,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryPane(
    viewModel: FinanceViewModel,
    income: Double,
    paidFixed: Double,
    totalFixed: Double,
    saldoRestante: Double,
    variableSpent: Double
) {
    val dbProfile by viewModel.dbProfile.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    var isAccordionOpen by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Resumen Financiero",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = FinanceSlateDark
            )
        }

        // HÈRO COMPONENT: PREMIUM CREDIT CARD VISUAL FOR SALDO DISPONIBLE REAL
        item {
            val isPositive = saldoRestante >= 0
            val gradient = if (isPositive) {
                Brush.linearGradient(
                    colors = listOf(Color(0xFF2563EB), Color(0xFF6D28D9)), // Elegant Blue to Purple Violet Gradient
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(Color(0xFFF43F5E), Color(0xFF881337)), // Warm Coral Rose to Deep Maroon Red Gradient
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), clip = true)
                    .testTag("remaining_payment_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .background(gradient)
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Modern minimalist credit card virtual microchip
                                Box(
                                    modifier = Modifier
                                        .size(36.dp, 26.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White.copy(alpha = 0.25f))
                                        .border(1.dp, Color.White.copy(alpha = 0.40f), RoundedCornerShape(6.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize(0.6f)
                                            .align(Alignment.Center)
                                            .border(0.5.dp, Color.White.copy(alpha = 0.3f))
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "SALDO DISPONIBLE REAL",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Text(
                            text = saldoRestante.formatCurrency(),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp,
                                color = Color.White
                            ),
                            modifier = Modifier.testTag("remaining_payment_text")
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "ESTADO DE CUENTA",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.60f),
                                        letterSpacing = 1.sp
                                    )
                                )
                                Text(
                                    text = "Dinero neto remanente",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                )
                            }

                            // Revolut/Mastercard style overlapping circular shapes
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.35f))
                                )
                                Box(
                                    modifier = Modifier
                                        .offset(x = (-10).dp)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f))
                                )
                            }
                        }
                    }
                }
            }
        }

        // FINANCIAL OVERVIEW PILL METRICS ROW (INGRESOS, FIJOS COBERTURA, VARIABLES)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Ingresos
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE6F4EA)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalAtm,
                                    contentDescription = "Sueldo",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ingresos",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = FinanceSlateLight
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = income.formatCurrency(),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = FinanceSlateDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (dbProfile?.sharedExpenses == true) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE6F4EA))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Compartido", fontSize = 8.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Card 2: Fijos Pagados
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F0FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Fijos",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Fijos",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = FinanceSlateLight
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = paidFixed.formatCurrency(),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = FinanceSlateDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "de ${totalFixed.formatCurrency()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = FinanceSlateLight
                        )
                    }
                }

                // Card 3: Spent Var
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = "Variable",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Variables",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = FinanceSlateLight
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = variableSpent.formatCurrency(),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = FinanceSlateDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "acumulado",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = FinanceSlateLight
                        )
                    }
                }
            }
        }

        // PROGRESS OF FIXED BILLS CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                val progressFraction = if (totalFixed > 0.0) (paidFixed / totalFixed).toFloat() else 0.0f
                val percentage = (progressFraction * 100).toInt()

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progreso de Facturas Fijas",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = FinanceSlateDark
                        )
                        Text(
                            text = "$percentage%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = FinanceTeal)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progressFraction.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .testTag("fixed_progress_bar"),
                        color = FinanceTeal,
                        trackColor = FinanceLightBlue
                    )
                }
            }
        }

        // FOLDABLE REFINED PERFIL Y FAMILIA ACCORDION
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("situation_config_accordion"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAccordionOpen = !isAccordionOpen }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(FinanceTeal.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Edit Situacion",
                                    tint = FinanceTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Perfil y Familia",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = FinanceSlateDark
                            )
                        }
                        Icon(
                            imageVector = if (isAccordionOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = FinanceSlateLight
                        )
                    }

                    AnimatedVisibility(visible = isAccordionOpen) {
                        Spacer(modifier = Modifier.height(12.dp))
                        AccordionContent(viewModel)
                    }
                }
            }
        }

        // RESET SYSTEM APP CONFIGURATION
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.resetSystemConfiguration() }
                    .testTag("reset_configuration_button"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBorderRed.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, CardBorderRed.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", tint = CardBorderRed)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Cambiar de Situación / Reiniciar Configuración",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CardBorderRed
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AnalysisPane(
    viewModel: FinanceViewModel,
    income: Double,
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val dbProfile by viewModel.dbProfile.collectAsStateWithLifecycle()
    val dbCategories by viewModel.dbCategories.collectAsStateWithLifecycle()
    val dbVariableExpenses by viewModel.dbVariableExpenses.collectAsStateWithLifecycle()
    val monthProjection by viewModel.monthProjection.collectAsStateWithLifecycle()

    val digitalBalanceInput by viewModel.digitalBalanceInput.collectAsStateWithLifecycle()
    val gastoVariableHoyInput by viewModel.gastoVariableHoyInput.collectAsStateWithLifecycle()

    val initialBalance = if (dbProfile != null && dbProfile!!.currentBankBalance >= 0.0) dbProfile!!.currentBankBalance else income
    val digitalBalance = digitalBalanceInput.toDoubleOrNull() ?: initialBalance

    val calendar = java.util.Calendar.getInstance()
    val elapsedDays = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val totalDaysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    val remainingDays = maxOf(1, totalDaysInMonth - elapsedDays)

    // Get standard midnight today Start
    val calMidnight = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    val todayStart = calMidnight.timeInMillis

    val realGastoVariableHoy = dbVariableExpenses.filter { it.timestamp >= todayStart }.sumOf { it.amount }
    val gastoVariableHoy = gastoVariableHoyInput.toDoubleOrNull() ?: realGastoVariableHoy

    val unpaidFixedExpenses = dbCategories.filter { category ->
        category.isFixed && !category.assumedByPartner && (
                !category.isPaid ||
                        (dbProfile?.let { prof -> elapsedDays < prof.incomeDay && category.payDay != null && category.payDay <= prof.incomeDay } ?: false)
                )
    }
    val saldoRetenido = unpaidFixedExpenses.sumOf { it.limitAmount }

    // Nueva Fórmula de Estado Crítico ("Caja Negra / Peor Escenario"):
    // Resultado Neto = Saldo Actual - Fijos Pendientes - (Gasto Diario Promedio * Días Restantes)
    val cycleDays = dbProfile?.let { viewModel.getFinancialCycleDays(it.incomeDay) } ?: Triple(elapsedDays, remainingDays, totalDaysInMonth)
    val cycleRemainingDays = cycleDays.second
    val averageDailyVariable = monthProjection?.averageDailyVariable ?: 0.0
    val projectedRemainingVariable = averageDailyVariable * cycleRemainingDays
    val simulatedExtraSpentToday = if (gastoVariableHoy > realGastoVariableHoy) gastoVariableHoy - realGastoVariableHoy else 0.0
    val margenLibre = digitalBalance - saldoRetenido - projectedRemainingVariable - simulatedExtraSpentToday

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Análisis y Proyecciones",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = FinanceSlateDark
            )
        }

        // 1. MOTOR DE ANÁLISIS FINANCIERO INTELIGENTE
        item {
            var isEngineMinimized by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("intelligent_finance_engine_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, FinanceTeal.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(FinanceTeal.copy(alpha = 0.1f))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Motor Inteligente",
                                tint = FinanceTeal,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Motor de Análisis Financiero",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = FinanceSlateDark
                            )
                            Text(
                                text = "Diferenciación estricta de dinero libre vs. comprometido",
                                style = MaterialTheme.typography.bodySmall,
                                color = FinanceSlateLight
                            )
                        }
                        IconButton(
                            onClick = { isEngineMinimized = !isEngineMinimized }
                        ) {
                            Icon(
                                imageVector = if (isEngineMinimized) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = if (isEngineMinimized) "Expandir/Minimizar" else "Minimizar/Expandir",
                                tint = FinanceSlateLight
                            )
                        }
                    }

                    if (!isEngineMinimized) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Inputs from user to customize parameters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = digitalBalanceInput,
                                onValueChange = { viewModel.updateDigitalBalanceInput(it) },
                                label = { Text("Tu saldo en banco ($currentCurrencySymbol)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Saldo actualizado correctamente")
                                        }
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FinanceTeal,
                                    unfocusedBorderColor = FinanceBorder,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = gastoVariableHoyInput,
                                onValueChange = { viewModel.updateGastoVariableHoyInput(it) },
                                label = { Text("Gasto variable hoy ($currentCurrencySymbol)", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Gasto variable actualizado correctamente")
                                        }
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FinanceTeal,
                                    unfocusedBorderColor = FinanceBorder,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 🚨 TARJETA: SALDO RETENIDO / COMPROMETIDO
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                            border = BorderStroke(1.dp, Color(0xFFFECDD3))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Saldo Retenido",
                                        tint = Color(0xFFE11D48),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🚨 Dinero Comprometido (Fijos pendientes)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF9F1239)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Total retenido: ${saldoRetenido.formatCurrency()}",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color(0xFF4C0519)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Desglose de recibos inminentes:",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF9F1239)
                                )
                                if (unpaidFixedExpenses.isEmpty()) {
                                    Text(
                                        text = "(Ninguno pendiente. ¡Todos tus pagos fijos están cubiertos!)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFBE123C)
                                    )
                                } else {
                                    unpaidFixedExpenses.forEach { bill ->
                                        Text(
                                            text = "• ${bill.name}: ${bill.limitAmount.formatCurrency()} (Día ${bill.payDay ?: "estimado"})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFBE123C)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Dinero Blindado: Este monto está reservado para asegurar tus recibos del mes.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                    color = Color(0xFFE11D48)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 🟢 TARJETA: MARGEN LIBRE / DISPONIBLE PARA OCIO
                        val isPositiveNet = margenLibre > 0.0
                        val statusText = if (isPositiveNet) "SÍ TE SOBRA" else "LÍMITE EXCEDIDO"
                        val statusBg = if (isPositiveNet) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                        val statusBorder = if (isPositiveNet) Color(0xFFBBF7D0) else Color(0xFFFCA5A5)
                        val statusColor = if (isPositiveNet) Color(0xFF16A34A) else Color(0xFFDC2626)
                        val statusLabelColor = if (isPositiveNet) Color(0xFF14532D) else Color(0xFF7F1D1D)

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = statusBg),
                            border = BorderStroke(1.dp, statusBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = if (isPositiveNet) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = "Margen Libre",
                                        tint = statusColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isPositiveNet) "🟢 Margen Libre (Disponible para ocio/ahorro)" else "🛑 Límite Excedido (Alerta de descubierto)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = statusColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Resultado neto: ${margenLibre.formatCurrency()}",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = statusLabelColor
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(statusColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = statusText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = statusColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isPositiveNet) {
                                        "Te quedarán libre ${margenLibre.formatCurrency()} en tu cuenta para pasar los $cycleRemainingDays días restantes hasta el próximo ingreso."
                                    } else {
                                        "¡Atención! Tu velocidad actual de gasto proyecta un descubierto financiero de ${(-margenLibre).formatCurrency()} antes de finalizar los $cycleRemainingDays días del ciclo."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = statusLabelColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. PROYECCIÓN DE FIN DE MES
        item {
            var isProjectionMinimized by remember { mutableStateOf(false) }

            monthProjection?.let { proj ->
                val isPositive = proj.projectedMonthEndBalance >= 0
                val accentColor = if (isPositive) FinanceTeal else CardBorderRed
                val cardBg = if (isPositive) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                val cardBorder = if (isPositive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("month_projection_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = "Predicción",
                                    tint = accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Proyección de Fin de Mes",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = FinanceSlateDark
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(accentColor.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isPositive) "Estable" else "Riesgo",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                                IconButton(
                                    onClick = { isProjectionMinimized = !isProjectionMinimized },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isProjectionMinimized) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                        contentDescription = if (isProjectionMinimized) "Expandir" else "Minimizar",
                                        tint = FinanceSlateLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        if (!isProjectionMinimized) {
                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Al ritmo actual, finalizarás el mes con un saldo estimado de:",
                                style = MaterialTheme.typography.bodySmall,
                                color = FinanceSlateLight
                            )

                            Text(
                                text = proj.projectedMonthEndBalance.formatCurrency(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = accentColor
                                ),
                                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                thickness = 1.dp,
                                color = cardBorder
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Grid analysis
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Gasto Diario Prom.", style = MaterialTheme.typography.bodySmall, color = FinanceSlateLight)
                                    Text("${proj.averageDailyVariable.formatCurrency()}/día", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = FinanceSlateDark)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Proyección Variable", style = MaterialTheme.typography.bodySmall, color = FinanceSlateLight)
                                    Text(proj.projectedVariable.formatCurrency(), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = FinanceSlateDark)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Fijos Pendientes", style = MaterialTheme.typography.bodySmall, color = FinanceSlateLight)
                                    Text(proj.pendingFixed.formatCurrency(), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = FinanceSlateDark)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Comparativa Ingresos", style = MaterialTheme.typography.bodySmall, color = FinanceSlateLight)
                                    Text(proj.baseIncome.formatCurrency(), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = FinanceSlateDark)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. REGISTRAR IMPREVISTO
        item {
            var emergencyAmount by remember { mutableStateOf("") }
            var emergencyConcept by remember { mutableStateOf("") }
            var isEmergencyMinimized by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("emergency_expense_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Emergencia",
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Registrar Imprevisto / Emergencia",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF7C2D12)
                            )
                        }
                        IconButton(
                            onClick = { isEmergencyMinimized = !isEmergencyMinimized },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isEmergencyMinimized) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = if (isEmergencyMinimized) "Expandir" else "Minimizar",
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (!isEmergencyMinimized) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "¿Ha surgido algún gasto sorpresa? Regístralo rápidamente aquí de forma directa.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9A3412)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = emergencyConcept,
                                onValueChange = { emergencyConcept = it },
                                label = { Text("¿Qué ha pasado?") },
                                placeholder = { Text("Ej: Dentista, Avería") },
                                modifier = Modifier.weight(1.3f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFEA580C),
                                    unfocusedBorderColor = Color(0xFFFDBA74),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = emergencyAmount,
                                onValueChange = { newValue ->
                                    emergencyAmount = newValue.filter { it.isDigit() || it == '.' }
                                },
                                label = { Text("Importe (€)") },
                                placeholder = { Text("Ej: 120") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFEA580C),
                                    unfocusedBorderColor = Color(0xFFFDBA74),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val amt = emergencyAmount.toDoubleOrNull() ?: 0.0
                                if (amt > 0.0) {
                                    val concept = emergencyConcept.ifBlank { "Imprevisto de Emergencia" }
                                    viewModel.recordVariableExpense(concept, amt)
                                    emergencyAmount = ""
                                    emergencyConcept = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = emergencyAmount.toDoubleOrNull() != null && (emergencyAmount.toDoubleOrNull() ?: 0.0) > 0.0
                        ) {
                            Text("Registrar Gasto de Emergencia", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }


    }
}

@Composable
fun AccordionContent(viewModel: FinanceViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val hasPets by viewModel.hasPets.collectAsStateWithLifecycle()
    val petsCost by viewModel.petsCost.collectAsStateWithLifecycle()
    val hasKids by viewModel.hasKids.collectAsStateWithLifecycle()
    val kidsCost by viewModel.kidsCost.collectAsStateWithLifecycle()
    val sharedExpenses by viewModel.sharedExpenses.collectAsStateWithLifecycle()
    val isWaterEnabled by viewModel.isWaterEnabled.collectAsStateWithLifecycle()
    val waterBilling by viewModel.waterBilling.collectAsStateWithLifecycle()
    val waterCost by viewModel.waterCost.collectAsStateWithLifecycle()
    val isElectricityEnabled by viewModel.isElectricityEnabled.collectAsStateWithLifecycle()
    val electricityBilling by viewModel.electricityBilling.collectAsStateWithLifecycle()
    val electricityCost by viewModel.electricityCost.collectAsStateWithLifecycle()
    val dbCategories by viewModel.dbCategories.collectAsStateWithLifecycle()

    var showAddSubDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(top = 12.dp, start = 8.dp, end = 8.dp)) {
        Divider(modifier = Modifier.padding(bottom = 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Pets, contentDescription = "Mascotas", tint = FinanceSlateLight)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("¿Tengo Mascotas?", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            }
            Switch(
                checked = hasPets,
                onCheckedChange = { viewModel.togglePetsDashboard(it) },
                colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal),
                modifier = Modifier.testTag("accordion_pets_switch")
            )
        }
        AnimatedVisibility(visible = hasPets) {
            OutlinedTextField(
                value = petsCost,
                onValueChange = { viewModel.updatePetsCostDashboard(it) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("accordion_pets_cost"),
                label = { Text("Importe Mensual Mascotas (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.ChildCare, contentDescription = "Hijos", tint = FinanceSlateLight)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("¿Tengo Hijos/Bebés?", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            }
            Switch(
                checked = hasKids,
                onCheckedChange = { viewModel.toggleKidsDashboard(it) },
                colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal),
                modifier = Modifier.testTag("accordion_kids_switch")
            )
        }
        AnimatedVisibility(visible = hasKids) {
            OutlinedTextField(
                value = kidsCost,
                onValueChange = { viewModel.updateKidsCostDashboard(it) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("accordion_kids_cost"),
                label = { Text("Importe Mensual Hijos (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.People, contentDescription = "Compartidos", tint = FinanceSlateLight)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gastos Compartidos (Pareja)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            Switch(
                checked = sharedExpenses,
                onCheckedChange = { viewModel.toggleSharedExpensesDashboard(it) },
                colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal),
                modifier = Modifier.testTag("accordion_shared_switch")
            )
        }

        AnimatedVisibility(visible = sharedExpenses) {
            Column {
                val partnerContrib by viewModel.partnerContribution.collectAsStateWithLifecycle()
                OutlinedTextField(
                    value = partnerContrib,
                    onValueChange = { viewModel.updatePartnerContributionDashboard(it) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("accordion_partner_contribution"),
                    label = { Text("Aportación Fija de la Pareja (€)") },
                    placeholder = { Text("Ej: 400") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = "Luz", tint = FinanceSlateLight)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Servicio de Luz", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            Switch(
                checked = isElectricityEnabled,
                onCheckedChange = { viewModel.toggleElectricitySupplyDashboard(it) },
                colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal),
                modifier = Modifier.testTag("accordion_luz_switch")
            )
        }
        AnimatedVisibility(visible = isElectricityEnabled) {
            Column {
                OutlinedTextField(
                    value = electricityCost,
                    onValueChange = { viewModel.updateElectricityCostDashboard(it) },
                    modifier = Modifier.fillMaxWidth().testTag("accordion_luz_cost"),
                    label = { Text("Importe Estimado Luz (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Ciclo de Luz:", fontSize = 12.sp, modifier = Modifier.weight(1f))
                    FilterChip(
                        selected = electricityBilling == "Mensual",
                        onClick = { viewModel.updateElectricityBillingCycleDashboard("Mensual") },
                        label = { Text("Mensual") },
                        modifier = Modifier.testTag("accordion_luz_mensual")
                    )
                    FilterChip(
                        selected = electricityBilling == "Bimensual",
                        onClick = { viewModel.updateElectricityBillingCycleDashboard("Bimensual") },
                        label = { Text("Bimensual") },
                        modifier = Modifier.testTag("accordion_luz_bimensual")
                    )
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.WaterDrop, contentDescription = "Agua", tint = FinanceSlateLight)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Servicio de Agua", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            Switch(
                checked = isWaterEnabled,
                onCheckedChange = { viewModel.toggleWaterSupplyDashboard(it) },
                colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal),
                modifier = Modifier.testTag("accordion_agua_switch")
            )
        }
        AnimatedVisibility(visible = isWaterEnabled) {
            Column {
                OutlinedTextField(
                    value = waterCost,
                    onValueChange = { viewModel.updateWaterCostDashboard(it) },
                    modifier = Modifier.fillMaxWidth().testTag("accordion_agua_cost"),
                    label = { Text("Importe Estimado Agua (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Ciclo de Agua:", fontSize = 12.sp, modifier = Modifier.weight(1f))
                    FilterChip(
                        selected = waterBilling == "Mensual",
                        onClick = { viewModel.updateWaterBillingCycleDashboard("Mensual") },
                        label = { Text("Mensual") },
                        modifier = Modifier.testTag("accordion_agua_mensual")
                    )
                    FilterChip(
                        selected = waterBilling == "Bimensual",
                        onClick = { viewModel.updateWaterBillingCycleDashboard("Bimensual") },
                        label = { Text("Bimensual") },
                        modifier = Modifier.testTag("accordion_agua_bimensual")
                    )
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Suscripciones y Streaming",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = FinanceSlateDark
            )
            TextButton(
                onClick = { showAddSubDialog = true },
                modifier = Modifier.testTag("add_subscription_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = FinanceTeal)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Elegir o Añadir", color = FinanceTeal, fontWeight = FontWeight.SemiBold)
            }
        }

        val activeSubs = dbCategories.filter {
            it.isFixed && it.isFromWizardExtra &&
                    it.id != "luz" && it.id != "agua" &&
                    it.id != "mascotas" && it.id != "hijos"
        }

        if (activeSubs.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = FinanceSlateLight.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Sin suscripciones activas",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = FinanceSlateDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Añade Netflix, Spotify, Amazon Prime o tus propias suscripciones personalizadas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FinanceSlateLight,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                activeSubs.forEach { sub ->
                    var isEditingPrice by remember(sub.id, sub.limitAmount) { mutableStateOf(false) }
                    var tempPriceStr by remember(sub.id, sub.limitAmount) { mutableStateOf(sub.limitAmount.toInt().toString()) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, FinanceBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SubscriptionLogo(
                                title = sub.name,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sub.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = FinanceSlateDark
                                )
                                if (!isEditingPrice) {
                                    Text(
                                        text = "${sub.limitAmount.formatCurrency()}/mes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FinanceSlateLight
                                    )
                                }
                            }

                            if (isEditingPrice) {
                                OutlinedTextField(
                                    value = tempPriceStr,
                                    onValueChange = { newVal ->
                                        val filtered = newVal.filter { it.isDigit() || it == '.' }
                                        tempPriceStr = filtered
                                    },
                                    label = { Text("Importe (€)", fontSize = 10.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(110.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = FinanceTeal,
                                        unfocusedBorderColor = FinanceBorder,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                viewModel.updateStreamingCostDashboard(sub.id, tempPriceStr, sub.name)
                                                isEditingPrice = false
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Guardar",
                                                tint = FinanceTeal
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            } else {
                                IconButton(onClick = { isEditingPrice = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar precio",
                                        tint = FinanceSlateLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    viewModel.removeStreamingDashboard(sub.id)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar suscripción",
                                    tint = CardBorderRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddSubDialog) {
            var subNameInput by remember { mutableStateOf("") }
            var subPriceInput by remember { mutableStateOf("") }

            val presetPlatforms = listOf(
                Triple("netflix", "Netflix", "15"),
                Triple("hbo_max", "HBO Max", "10"),
                Triple("disney_plus", "Disney+", "10"),
                Triple("amazon_prime", "Amazon Prime", "5"),
                Triple("spotify", "Spotify", "11"),
                Triple("youtube_premium", "YouTube Premium", "13"),
                Triple("youtube_music", "YouTube Music", "10"),
                Triple("juegos", "Suscripción Juegos", "15")
            )
            val availablePresets = presetPlatforms.filter { preset ->
                activeSubs.none { it.id == preset.first }
            }

            AlertDialog(
                onDismissRequest = { showAddSubDialog = false },
                title = {
                    Text(
                        text = "Añadir Suscripción",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (availablePresets.isNotEmpty()) {
                            Text(
                                text = "Elige una plataforma popular:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = FinanceSlateDark
                            )

                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                availablePresets.forEach { (id, title, defaultCost) ->
                                    val brandColor = when {
                                        title.contains("netflix", ignoreCase = true) -> Color(0xFFE50914)
                                        title.contains("hbo", ignoreCase = true) || title.contains("max", ignoreCase = true) -> Color(0xFF1F1C2C)
                                        title.contains("disney", ignoreCase = true) -> Color(0xFF113CCF)
                                        title.contains("spotify", ignoreCase = true) -> Color(0xFF1DB954)
                                        title.contains("youtube", ignoreCase = true) -> Color(0xFFFF0000)
                                        title.contains("prime", ignoreCase = true) || title.contains("amazon", ignoreCase = true) -> Color(0xFF00A8E1)
                                        else -> FinanceTeal
                                    }

                                    Card(
                                        modifier = Modifier
                                            .clickable {
                                                viewModel.toggleStreamingDashboard(id, true, title, defaultCost)
                                                showAddSubDialog = false
                                            }
                                            .testTag("preset_chip_$id"),
                                        colors = CardDefaults.cardColors(containerColor = brandColor.copy(alpha = 0.12f)),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, brandColor.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(brandColor, shape = RoundedCornerShape(4.dp))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = brandColor
                                            )
                                        }
                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                        }

                        Text(
                            text = "O añade una personalizada:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = FinanceSlateDark
                        )

                        OutlinedTextField(
                            value = subNameInput,
                            onValueChange = { subNameInput = it },
                            label = { Text("Nombre del servicio") },
                            placeholder = { Text("Ej: Gimnasio, DAZN...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FinanceTeal,
                                unfocusedBorderColor = FinanceBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = subPriceInput,
                            onValueChange = { subPriceInput = it },
                            label = { Text("Precio Mensual (€)") },
                            placeholder = { Text("Ej: 19.99") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FinanceTeal,
                                unfocusedBorderColor = FinanceBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val price = subPriceInput.toDoubleOrNull() ?: 0.0
                            if (subNameInput.isNotBlank() && price > 0.0) {
                                viewModel.addNewCustomSubDashboard(subNameInput, subPriceInput)
                                showAddSubDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                        enabled = subNameInput.isNotBlank() && subPriceInput.toDoubleOrNull() != null
                    ) {
                        Text("Guardar", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddSubDialog = false }) {
                        Text("Cancelar", color = CardBorderRed)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = "Banners", tint = FinanceSlateLight)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Banners Informativos", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = FinanceSlateDark)
                Text("Reactiva los banners que hayas cerrado en la sección de facturas.", style = MaterialTheme.typography.bodySmall, color = FinanceSlateLight)
            }
        }

        val hideNewMonthBannerState by viewModel.hideNewMonthBanner.collectAsStateWithLifecycle()
        val hideSmartCalendarBannerState by viewModel.hideSmartCalendarBanner.collectAsStateWithLifecycle()

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mostrar Banner 'Nuevo Mes'", style = MaterialTheme.typography.bodyMedium, color = FinanceSlateDark, modifier = Modifier.weight(1f))
            Switch(
                checked = !hideNewMonthBannerState,
                onCheckedChange = { viewModel.setHideNewMonthBanner(!it) },
                colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal),
                modifier = Modifier.testTag("switch_show_new_month_banner")
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mostrar Banner 'Calendario Inteligente'", style = MaterialTheme.typography.bodyMedium, color = FinanceSlateDark, modifier = Modifier.weight(1f))
            Switch(
                checked = !hideSmartCalendarBannerState,
                onCheckedChange = { viewModel.setHideSmartCalendarBanner(!it) },
                colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal),
                modifier = Modifier.testTag("switch_show_smart_calendar_banner")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { sendFeedback(context) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("feedback_report_button"),
            colors = ButtonDefaults.textButtonColors(contentColor = FinanceSlateLight)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Enviar Feedback",
                    tint = FinanceSlateLight,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "¿Has detectado un error o falta algo?",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

private fun sendFeedback(context: android.content.Context) {
    val emailIntent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
        data = android.net.Uri.parse("mailto:")
        putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("logichive.dev@gmail.com"))
        putExtra(android.content.Intent.EXTRA_SUBJECT, "Soporte y Reporte de Errores - FinanceFlow v1.0")

        val bodyText = """
            Hola,
            
            [Describe aquí tu error o sugerencia]
            
            -- Información Técnica --
            App Version: 1.0
            Device Model: ${android.os.Build.MODEL}
            Android Version: ${android.os.Build.VERSION.SDK_INT} (API ${android.os.Build.VERSION.SDK_INT})
        """.trimIndent()

        putExtra(android.content.Intent.EXTRA_TEXT, bodyText)
    }

    try {
        context.startActivity(android.content.Intent.createChooser(emailIntent, "Enviar correo con:"))
    } catch (e: Exception) {
        // Safe catch if no activity handles the mail client
    }
}

@Composable
fun DashboardStreamingToggle(
    id: String,
    title: String,
    isActive: Boolean,
    cost: String,
    onActiveChange: (Boolean) -> Unit,
    onCostChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubscriptionLogo(
                title = title,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
            Switch(
                checked = isActive,
                onCheckedChange = onActiveChange,
                colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal),
                modifier = Modifier.testTag("accordion_switch_$id")
            )
        }
        AnimatedVisibility(visible = isActive) {
            OutlinedTextField(
                value = cost,
                onValueChange = onCostChange,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp).testTag("accordion_cost_$id"),
                label = { Text("Precio Mensual (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FinanceTeal,
                    unfocusedBorderColor = FinanceBorder
                )
            )
        }
    }
}

@Composable
fun FixedBillsPane(viewModel: FinanceViewModel, fixedExpenses: List<ExpenseCategory>) {
    val dbProfile by viewModel.dbProfile.collectAsStateWithLifecycle()
    var expandedCardId by remember { mutableStateOf<String?>(null) }
    var showNewMonthDialog by remember { mutableStateOf(false) }
    var billToDelete by remember { mutableStateOf<ExpenseCategory?>(null) }
    val todayDay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)

    val hideNewMonthBanner by viewModel.hideNewMonthBanner.collectAsStateWithLifecycle()
    val hideSmartCalendarBanner by viewModel.hideSmartCalendarBanner.collectAsStateWithLifecycle()

    // Add fixed bill dialog states
    var showAddBillDialog by remember { mutableStateOf(false) }
    var addBillName by remember { mutableStateOf("") }
    var addBillAmount by remember { mutableStateOf("") }
    var addBillIsFinancing by remember { mutableStateOf(false) }
    var addBillMonthsRemaining by remember { mutableStateOf("") }
    var addBillPayDay by remember { mutableStateOf("") }

    // Dialog to add custom fixed bill or financing
    if (showAddBillDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddBillDialog = false
                addBillName = ""
                addBillAmount = ""
                addBillIsFinancing = false
                addBillMonthsRemaining = ""
                addBillPayDay = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = FinanceTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Añadir Factura o Financiación",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = FinanceSlateDark
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Agrega una nueva factura periódica o registra una financiación para restar de tu presupuesto disponible.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FinanceSlateLight
                    )

                    OutlinedTextField(
                        value = addBillName,
                        onValueChange = { addBillName = it },
                        label = { Text("Nombre del Gasto Fijo") },
                        placeholder = { Text("Ej: Financiación Coche, Netflix") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_bill_name_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FinanceTeal,
                            focusedLabelColor = FinanceTeal
                        )
                    )

                    OutlinedTextField(
                        value = addBillAmount,
                        onValueChange = { addBillAmount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Importe Mensual (€)") },
                        placeholder = { Text("Ej: 154.50") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_bill_amount_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FinanceTeal,
                            focusedLabelColor = FinanceTeal
                        )
                    )

                    OutlinedTextField(
                        value = addBillPayDay,
                        onValueChange = { addBillPayDay = it.filter { c -> c.isDigit() } },
                        label = { Text("Día de Pago Opcional (1-31)") },
                        placeholder = { Text("Ej: 5 (vence el día 5)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_bill_payday_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FinanceTeal,
                            focusedLabelColor = FinanceTeal
                        )
                    )

                    // Switch for Financing/Loan
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(FinanceSoftBg)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Es una Financiación / Préstamo",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = FinanceSlateDark
                            )
                            Text(
                                "Indica si tiene un número limitado de cuotas mensuales.",
                                style = MaterialTheme.typography.bodySmall,
                                color = FinanceSlateLight
                            )
                        }
                        Switch(
                            checked = addBillIsFinancing,
                            onCheckedChange = { checked ->
                                addBillIsFinancing = checked
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal)
                        )
                    }

                    AnimatedVisibility(visible = addBillIsFinancing) {
                        OutlinedTextField(
                            value = addBillMonthsRemaining,
                            onValueChange = { addBillMonthsRemaining = it.filter { c -> c.isDigit() } },
                            label = { Text("Cuotas Restantes (Meses)") },
                            placeholder = { Text("Ej: 12 (se quitará tras 12 meses)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("add_bill_months_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FinanceTeal,
                                focusedLabelColor = FinanceTeal
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = addBillAmount.toDoubleOrNull()
                        val mLeft = addBillMonthsRemaining.toIntOrNull()
                        val payD = addBillPayDay.toIntOrNull()?.coerceIn(1, 31)
                        if (addBillName.isNotBlank() && amt != null && amt > 0.0) {
                            viewModel.addFixedBillDb(
                                name = addBillName.trim(),
                                amount = amt,
                                isFinancing = addBillIsFinancing,
                                monthsRemaining = if (addBillIsFinancing) mLeft else null,
                                payDay = payD
                            )
                            showAddBillDialog = false
                            addBillName = ""
                            addBillAmount = ""
                            addBillIsFinancing = false
                            addBillMonthsRemaining = ""
                            addBillPayDay = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Guardar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddBillDialog = false
                        addBillName = ""
                        addBillAmount = ""
                        addBillIsFinancing = false
                        addBillMonthsRemaining = ""
                        addBillPayDay = ""
                    }
                ) {
                    Text("Cancelar", color = FinanceSlateLight)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header with Title AND Add Button!
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mis Facturas de Gasto Fijo",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = FinanceSlateDark
                        )
                        Text(
                            text = "Marca o edita tus cobros periódicos y financiaciones.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FinanceSlateLight
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { showAddBillDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("add_new_fixed_bill_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Añadir", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // New Period Roll Reset Control
            item {
                AnimatedVisibility(visible = !hideNewMonthBanner) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = FinanceTeal.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, FinanceTeal.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .padding(end = 28.dp) // Leave space for close button
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "🔄 ¿Ha comenzado un nuevo mes?",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = FinanceTeal
                                        )
                                        Text(
                                            text = "La aplicación realiza un reinicio inteligente automáticamente cuando detecta un cambio de mes real en tu calendario, pero puedes forzar un reinicio manual aquí si lo deseas.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = FinanceSlateLight
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { showNewMonthDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Iniciar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            IconButton(
                                onClick = { viewModel.setHideNewMonthBanner(true) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(28.dp)
                                    .testTag("dismiss_new_month_banner")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Ocultar banner",
                                    tint = FinanceSlateLight.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Smart Calendar Auto-checking Status Card
            val autoManagedCount = fixedExpenses.count { it.payDay != null }
            item {
                AnimatedVisibility(visible = !hideSmartCalendarBanner) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp)
                            .testTag("smart_calendar_card"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                        border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .padding(end = 28.dp), // Leave space for close button
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Autochequeo Inteligente",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "📅 Calendario Inteligente Activo",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF0369A1)
                                    )
                                    Text(
                                        text = "Las facturas se marcan automáticamente como pagadas cuando llega su día de pago (hoy es día $todayDay). Tienes $autoManagedCount fijos auto-gestionados.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF0A5881)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.setHideSmartCalendarBanner(true) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(28.dp)
                                    .testTag("dismiss_smart_calendar_banner")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Ocultar banner",
                                    tint = Color(0xFF0369A1).copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Display empty state or bills
            if (fixedExpenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = "Empty", tint = FinanceSlateLight, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No hay facturas registradas.", color = FinanceSlateLight, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                items(fixedExpenses, key = { it.id }) { item ->
                    val isExpanded = expandedCardId == item.id
                    var payDayInput by remember(item.id, item.payDay) { mutableStateOf(item.payDay?.toString() ?: "") }
                    var amountInput by remember(item.id, item.limitAmount) { mutableStateOf(item.limitAmount.toString()) }

                    // Financing support states for editing
                    var isFinancingEdit by remember(item.id, item.isFinancing) { mutableStateOf(item.isFinancing) }
                    var monthsRemainingEdit by remember(item.id, item.monthsRemaining) { mutableStateOf(item.monthsRemaining?.toString() ?: "") }

                    fun commitChanges() {
                        val dayInt = payDayInput.toIntOrNull()
                        val payD = if (dayInt == null) null else dayInt.coerceIn(1, 31)
                        val amtDouble = amountInput.toDoubleOrNull()
                        val mLeft = monthsRemainingEdit.toIntOrNull()

                        if (amtDouble != null && amtDouble > 0.0) {
                            viewModel.updateFixedBillDb(
                                id = item.id,
                                name = item.name,
                                amount = amtDouble,
                                isFinancing = isFinancingEdit,
                                monthsRemaining = if (isFinancingEdit) mLeft else null,
                                payDay = payD
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isExpanded) {
                                    commitChanges()
                                    expandedCardId = null
                                } else {
                                    expandedCardId = item.id
                                }
                            }
                            .testTag("fixed_card_${item.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isPaid) Color(0xFFF0FDF4) else if (isExpanded) Color(0xFFF8FAFC) else Color.White
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (item.isPaid) FinanceTeal.copy(alpha = 0.5f) else if (isExpanded) FinanceTeal else FinanceBorder
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 3.dp else 1.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isPaid,
                                    onCheckedChange = { viewModel.setGastoFijoPaid(item.id, it) },
                                    colors = CheckboxDefaults.colors(checkedColor = FinanceTeal),
                                    modifier = Modifier.testTag("checkbox_fixed_${item.id}").size(32.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FinanceTeal.copy(alpha = 0.05f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val isLuz = item.id == "luz" || item.name.contains("luz", ignoreCase = true)
                                    val isAgua = item.id == "agua" || item.name.contains("agua", ignoreCase = true)
                                    val isMascota = item.id == "mascotas" || item.id == "pets" || item.name.contains("mascota", ignoreCase = true)
                                    val isHijos = item.id == "kids" || item.id == "hijos" || item.name.contains("hijo", ignoreCase = true)
                                    val isCompartido = item.id == "shared" || item.name.contains("compartido", ignoreCase = true)

                                    if (isLuz) {
                                        Icon(imageVector = Icons.Default.Lightbulb, contentDescription = "Luz", tint = Color(0xFFEAB308), modifier = Modifier.size(20.dp))
                                    } else if (isAgua) {
                                        Icon(imageVector = Icons.Default.WaterDrop, contentDescription = "Agua", tint = Color(0xFF0EA5E9), modifier = Modifier.size(20.dp))
                                    } else if (isMascota) {
                                        Icon(imageVector = Icons.Default.Pets, contentDescription = "Mascotas", tint = Color(0xFFF97316), modifier = Modifier.size(20.dp))
                                    } else if (isHijos) {
                                        Icon(imageVector = Icons.Default.ChildCare, contentDescription = "Hijos", tint = Color(0xFFEC4899), modifier = Modifier.size(20.dp))
                                    } else if (isCompartido) {
                                        Icon(imageVector = Icons.Default.People, contentDescription = "Compartidos", tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
                                    } else {
                                        SubscriptionLogo(title = item.name, modifier = Modifier.fillMaxSize())
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.isPaid) FinanceTeal else FinanceSlateDark
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        if (item.payDay != null) {
                                            val isAutoPaid = item.isPaid && todayDay >= item.payDay
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isAutoPaid) Color(0xFFDCFCE7) else FinanceTeal.copy(alpha = 0.08f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (isAutoPaid) "✨ Auto-pagado (Día ${item.payDay})" else "📅 Día ${item.payDay}",
                                                    fontSize = 11.sp,
                                                    color = if (isAutoPaid) Color(0xFF15803D) else FinanceTeal,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Display variable label/alert
                                    if (item.isVariableBill) {
                                        Box(
                                            modifier = Modifier
                                                .padding(vertical = 2.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFFEF3C7))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.TrendingUp,
                                                    contentDescription = "Cambiante",
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Gasto Cambiante (Varía cada mes)",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF92400E),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    // Display financing label/alert
                                    if (item.isFinancing) {
                                        Box(
                                            modifier = Modifier
                                                .padding(vertical = 2.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFFFEAD5))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CalendarMonth,
                                                    contentDescription = "Financiación",
                                                    tint = Color(0xFFEA580C),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (item.monthsRemaining != null) "Financiación (${item.monthsRemaining} meses rest.)" else "Financiación",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFFC2410C),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    if (item.assumedByPartner) {
                                        Box(
                                            modifier = Modifier
                                                .padding(vertical = 2.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFF3E8FF))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.People,
                                                    contentDescription = "Pagado por pareja",
                                                    tint = Color(0xFF7E22CE),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Asumida 100% por mi pareja",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF6B21A8),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    if ((item.id == "luz" || item.id == "agua") && item.billingCycle == "Bimensual") {
                                        Text(
                                            text = "Factura Bimensual: ${item.rawAmount.formatCurrency()} (media mensual: ${item.limitAmount.formatCurrency()})",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = FinanceTeal,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    } else if (item.isFromWizardExtra && (item.id == "luz" || item.id == "agua")) {
                                        Text(
                                            text = "Factura Mensual: ${item.limitAmount.formatCurrency()}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = FinanceSlateLight)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = item.limitAmount.formatCurrency(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            textDecoration = if (item.assumedByPartner) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                        ),
                                        color = if (item.assumedByPartner) FinanceSlateLight else if (item.isPaid) FinanceTeal else FinanceSlateDark
                                    )
                                    if (item.assumedByPartner) {
                                        Text(
                                            text = "A cargo de ella",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF7E22CE)
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        tint = FinanceSlateLight.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // EXPANDED CONFIGURATION AREA
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF1F5F9))
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Configuración del Gasto Fijo / Financiación",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = FinanceSlateDark
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = payDayInput,
                                            onValueChange = { newValue ->
                                                payDayInput = newValue.filter { it.isDigit() }
                                            },
                                            modifier = Modifier.weight(1f),
                                            label = { Text("Día de Pago (1-31)") },
                                            placeholder = { Text("Ej: 5") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = FinanceTeal,
                                                unfocusedBorderColor = FinanceBorder,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            )
                                        )

                                        OutlinedTextField(
                                            value = amountInput,
                                            onValueChange = { newValue ->
                                                amountInput = newValue.filter { it.isDigit() || it == '.' }
                                            },
                                            modifier = Modifier.weight(1f),
                                            label = { Text("Importe Mensual (€)") },
                                            placeholder = { Text("Ej: 50") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = FinanceTeal,
                                                unfocusedBorderColor = FinanceBorder,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Switch for isFinancing
                                    val toggleFinancingEditAction = { checked: Boolean ->
                                        isFinancingEdit = checked
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White)
                                            .clickable { toggleFinancingEditAction(!isFinancingEdit) }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Es una financiación / préstamo",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = FinanceSlateDark
                                            )
                                            Text(
                                                text = "Indica si tiene cuotas limitadas y se quitará automáticamente al finalizar.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = FinanceSlateLight
                                            )
                                        }
                                        Switch(
                                            checked = isFinancingEdit,
                                            onCheckedChange = { toggleFinancingEditAction(it) },
                                            colors = SwitchDefaults.colors(checkedThumbColor = FinanceTeal)
                                        )
                                    }

                                    if (isFinancingEdit) {
                                        Spacer(modifier = Modifier.height(10.dp))

                                        OutlinedTextField(
                                            value = monthsRemainingEdit,
                                            onValueChange = { newValue ->
                                                monthsRemainingEdit = newValue.filter { it.isDigit() }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            label = { Text("Cuotas Restantes (Meses)") },
                                            placeholder = { Text("Ej: 12") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = FinanceTeal,
                                                unfocusedBorderColor = FinanceBorder,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Switch indicating whether it varies each month
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White)
                                            .clickable { viewModel.toggleCategoryIsVariableBill(item.id, !item.isVariableBill) }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Es un gasto cambiante",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = FinanceSlateDark
                                            )
                                            Text(
                                                text = "Indica si el valor varía mes a mes (ej. luz, agua) para recordarte actualizar su monto.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = FinanceSlateLight
                                            )
                                        }
                                        Switch(
                                            checked = item.isVariableBill,
                                            onCheckedChange = { viewModel.toggleCategoryIsVariableBill(item.id, it) },
                                            colors = SwitchDefaults.colors(checkedThumbColor = FinanceTeal)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Switch indicating whether the partner assumes it 100%
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White)
                                            .clickable { viewModel.toggleCategoryAssumedByPartner(item.id, !item.assumedByPartner) }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Asumido por mi pareja (100%)",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = FinanceSlateDark
                                            )
                                            Text(
                                                text = "Ella/Él paga esta factura entera. Ya no restará de tu dinero disponible del mes.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = FinanceSlateLight
                                            )
                                        }
                                        Switch(
                                            checked = item.assumedByPartner,
                                            onCheckedChange = { viewModel.toggleCategoryAssumedByPartner(item.id, it) },
                                            colors = SwitchDefaults.colors(checkedThumbColor = FinanceTeal)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                billToDelete = item
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = CardBorderRed
                                            ),
                                            border = BorderStroke(1.dp, CardBorderRed.copy(alpha = 0.5f)),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.testTag("delete_fixed_bill_${item.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Dar de baja",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Dar de baja", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                commitChanges()
                                                expandedCardId = null
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                                            modifier = Modifier.testTag("save_fixed_bill_${item.id}")
                                        ) {
                                            Text("Aceptar", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    billToDelete?.let { targetBill ->
        AlertDialog(
            onDismissRequest = { billToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Dar de baja",
                        tint = CardBorderRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("¿Dar de baja factura?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Text(
                    text = "¿Estás totalmente seguro de dar de baja la factura de \"${targetBill.name}\"? " +
                            "Al eliminarla, se restará de forma de permanente de tu presupuesto y los cálculos o proyecciones de fin de mes del Dashboard se actualizarán inmediatamente.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFixedBillCategory(targetBill.id)
                        if (expandedCardId == targetBill.id) {
                            expandedCardId = null
                        }
                        billToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_bill_btn")
                ) {
                    Text("Sí, dar de baja", color = CardBorderRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { billToDelete = null },
                    modifier = Modifier.testTag("cancel_delete_bill_btn")
                ) {
                    Text("Cancelar", color = FinanceSlateLight)
                }
            }
        )
    }
}

@Composable
fun VariableSpentPane(
    viewModel: FinanceViewModel,
    dbCategories: List<ExpenseCategory>,
    variableExpenses: List<VariableExpenseEntry>,
    totalSpent: Double
) {
    val variableTargets = dbCategories.filter { !it.isFixed }
    var selectedCategoryName by remember { mutableStateOf("") }
    var transactionAmountInput by remember { mutableStateOf("") }

    var showQuickRecordDialogForCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var quickRecordAmountInput by remember { mutableStateOf("") }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var addCategoryName by remember { mutableStateOf("") }
    var addCategoryLimit by remember { mutableStateOf("") }

    var showEditCategoryDialogForCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var editCategoryName by remember { mutableStateOf("") }
    var editCategoryLimit by remember { mutableStateOf("") }

    var showDeleteConfirmationForCategory by remember { mutableStateOf<ExpenseCategory?>(null) }

    LaunchedEffect(variableTargets) {
        if (selectedCategoryName.isBlank() && variableTargets.isNotEmpty()) {
            selectedCategoryName = variableTargets.first().name
        }
    }

    if (showQuickRecordDialogForCategory != null) {
        val budget = showQuickRecordDialogForCategory!!
        AlertDialog(
            onDismissRequest = {
                showQuickRecordDialogForCategory = null
                quickRecordAmountInput = ""
            },
            title = {
                Text(
                    text = "Registrar Gasto en ${budget.name}",
                    fontWeight = FontWeight.Bold,
                    color = FinanceSlateDark
                )
            },
            text = {
                Column {
                    Text(
                        text = "Introduce la cantidad exacta gastada en esta categoría (admite decimales):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FinanceSlateLight,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = quickRecordAmountInput,
                        onValueChange = { newValue ->
                            quickRecordAmountInput = newValue.filter { it.isDigit() || it == '.' }
                        },
                        label = { Text("Importe (€)") },
                        placeholder = { Text("Ej: 143.78") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("quick_record_amount_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FinanceTeal,
                            focusedLabelColor = FinanceTeal
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountVal = quickRecordAmountInput.toDoubleOrNull()
                        if (amountVal != null && amountVal > 0.0) {
                            viewModel.recordVariableExpense(budget.name, amountVal)
                        }
                        showQuickRecordDialogForCategory = null
                        quickRecordAmountInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Registrar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showQuickRecordDialogForCategory = null
                        quickRecordAmountInput = ""
                    }
                ) {
                    Text("Cancelar", color = FinanceSlateLight)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = {
                Text(
                    text = "Añadir Categoría Variable",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = FinanceSlateDark
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = addCategoryName,
                        onValueChange = { addCategoryName = it },
                        label = { Text("Nombre de la categoría") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_budget_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = addCategoryLimit,
                        onValueChange = { addCategoryLimit = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Límite mensual (€)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("add_budget_limit_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limitVal = addCategoryLimit.toDoubleOrNull() ?: 0.0
                        if (addCategoryName.isNotBlank() && limitVal > 0.0) {
                            viewModel.addVariableBudgetDb(addCategoryName.trim(), limitVal)
                            showAddCategoryDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_new_budget_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Añadir", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddCategoryDialog = false },
                    modifier = Modifier.testTag("cancel_new_budget_btn")
                ) {
                    Text("Cancelar", color = FinanceSlateLight)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    showEditCategoryDialogForCategory?.let { targetCategory ->
        AlertDialog(
            onDismissRequest = { showEditCategoryDialogForCategory = null },
            title = {
                Text(
                    text = "Editar Categoría Variable",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = FinanceSlateDark
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editCategoryName,
                        onValueChange = { editCategoryName = it },
                        label = { Text("Nombre de la categoría") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_budget_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editCategoryLimit,
                        onValueChange = { editCategoryLimit = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Límite mensual (€)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("edit_budget_limit_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limitVal = editCategoryLimit.toDoubleOrNull() ?: 0.0
                        if (editCategoryName.isNotBlank() && limitVal > 0.0) {
                            viewModel.updateVariableBudgetDb(targetCategory.id, editCategoryName.trim(), limitVal)
                            showEditCategoryDialogForCategory = null
                        }
                    },
                    modifier = Modifier.testTag("save_edit_budget_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Guardar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditCategoryDialogForCategory = null },
                    modifier = Modifier.testTag("cancel_edit_budget_btn")
                ) {
                    Text("Cancelar", color = FinanceSlateLight)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    showDeleteConfirmationForCategory?.let { targetCategory ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationForCategory = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Dar de baja",
                        tint = CardBorderRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("¿Eliminar categoría?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Text(
                    text = "¿Estás seguro de eliminar la categoría de gasto \"${targetCategory.name}\"? Se borrará permanentemente de tu presupuesto actual de variables.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteVariableBudgetDb(targetCategory.id)
                        showDeleteConfirmationForCategory = null
                    },
                    modifier = Modifier.testTag("confirm_delete_budget_btn")
                ) {
                    Text("Sí, eliminar", color = CardBorderRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmationForCategory = null },
                    modifier = Modifier.testTag("cancel_delete_budget_btn")
                ) {
                    Text("Cancelar", color = FinanceSlateLight)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Control de Gastos Diarios",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = FinanceSlateDark
                )
            }

            if (variableTargets.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No has definido presupuestos de variables en el Wizard.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FinanceSlateLight
                            )
                        }
                    }
                }
            } else {
                items(variableTargets) { budget ->
                    val categoryExpenses = variableExpenses.filter {
                        it.categoryId == budget.id || it.categoryName.lowercase().trim() == budget.name.lowercase().trim()
                    }
                    val spentOnThis = categoryExpenses.sumOf { it.amount }
                    val limitOnThis = budget.limitAmount
                    val fillRatio = if (limitOnThis > 0.0) spentOnThis / limitOnThis else 0.0
                    val percent = (fillRatio * 100).toInt()

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("variable_budget_card_${budget.id}"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = budget.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = FinanceSlateDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    var showMenu by remember { mutableStateOf(false) }
                                    Box {
                                        IconButton(
                                            onClick = { showMenu = true },
                                            modifier = Modifier.size(36.dp).testTag("budget_menu_btn_${budget.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Opciones",
                                                tint = FinanceSlateLight,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false },
                                            modifier = Modifier.background(Color.White)
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Editar categoría") },
                                                onClick = {
                                                    showMenu = false
                                                    showEditCategoryDialogForCategory = budget
                                                    editCategoryName = budget.name
                                                    editCategoryLimit = budget.limitAmount.toInt().toString()
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Edit",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                },
                                                modifier = Modifier.testTag("edit_budget_item_${budget.id}")
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Eliminar categoría", color = CardBorderRed) },
                                                onClick = {
                                                    showMenu = false
                                                    showDeleteConfirmationForCategory = budget
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete",
                                                        tint = CardBorderRed,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                },
                                                modifier = Modifier.testTag("delete_budget_item_${budget.id}")
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${spentOnThis.formatCurrency()} / ${limitOnThis.formatCurrency()}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (spentOnThis > limitOnThis) CardBorderRed else FinanceSlateLight
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { fillRatio.toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .testTag("budget_progress_${budget.id}"),
                                color = if (spentOnThis > limitOnThis) CardBorderRed else FinanceTeal,
                                trackColor = Color(0xFFF1F5F9)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            var sliderValue by remember(budget.id, limitOnThis) { mutableStateOf(limitOnThis.toFloat()) }
                            var manualInputStr by remember(budget.id, limitOnThis) { mutableStateOf(limitOnThis.toInt().toString()) }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Modificar tope a: ${sliderValue.toInt()} €",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FinanceTeal
                                )
                                if (spentOnThis > limitOnThis) {
                                    Text(
                                        text = "¡Superado por ${(spentOnThis - limitOnThis).formatCurrency()}!",
                                        color = CardBorderRed,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "Disponible: ${(limitOnThis - spentOnThis).formatCurrency()}",
                                        color = FinanceSlateLight,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = manualInputStr,
                                    onValueChange = { newVal ->
                                        val filtered = newVal.filter { it.isDigit() }
                                        manualInputStr = filtered
                                        filtered.toFloatOrNull()?.let { num ->
                                            val bounded = num.coerceIn(0f, 100000f)
                                            sliderValue = bounded.coerceIn(0f, 1000f)
                                            viewModel.updateCategoryLimitAmount(budget.id, bounded.toDouble())
                                        }
                                    },
                                    label = { Text("Tope manual (€)", fontSize = 11.sp) },
                                    placeholder = { Text("Monto") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .width(125.dp)
                                        .testTag("manual_budget_input_${budget.id}"),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = FinanceTeal,
                                        unfocusedBorderColor = FinanceBorder,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Slider(
                                    value = sliderValue.coerceIn(0f, 1000f),
                                    onValueChange = { newVal ->
                                        sliderValue = newVal
                                        manualInputStr = newVal.toInt().toString()
                                    },
                                    onValueChangeFinished = {
                                        val rounded = (sliderValue / 5f).toInt() * 5.0
                                        viewModel.updateCategoryLimitAmount(budget.id, rounded)
                                        manualInputStr = rounded.toInt().toString()
                                    },
                                    valueRange = 0f..1000f,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("slider_budget_${budget.id}"),
                                    colors = SliderDefaults.colors(
                                        activeTrackColor = FinanceTeal,
                                        thumbColor = FinanceTeal
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    showQuickRecordDialogForCategory = budget
                                    quickRecordAmountInput = ""
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("quick_record_btn_${budget.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal.copy(alpha = 0.08f)),
                                elevation = null
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Registrar Gasto",
                                        tint = FinanceTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Registrar Gasto",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = FinanceTeal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Historial de Gastos Diarios",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = FinanceSlateDark
                )
            }

            if (variableExpenses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, FinanceBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = "Empty", tint = FinanceSlateLight, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Aún no has registrado ningún gasto diario.", color = FinanceSlateLight, fontSize = 13.sp)
                            }
                        }
                    }
                }
            } else {
                items(variableExpenses, key = { it.id }) { expense ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("variable_spent_card_${expense.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, FinanceBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(FinanceTeal.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Receipt, contentDescription = "Gasto", tint = FinanceTeal, modifier = Modifier.size(20.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(expense.categoryName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = FinanceSlateDark)
                                val formDate = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(expense.timestamp)
                                Text(formDate, fontSize = 11.sp, color = FinanceSlateLight)
                            }

                            Text(
                                expense.amount.formatCurrency(),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = FinanceSlateDark
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { viewModel.deleteVariableSpentEntry(expense.id) },
                                modifier = Modifier.testTag("delete_var_trans_${expense.id}")
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = CardBorderRed, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                showAddCategoryDialog = true
                addCategoryName = ""
                addCategoryLimit = ""
            },
            containerColor = FinanceTeal,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_variable_category_fab"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Añadir Categoría",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SubscriptionLogo(title: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val drawableName = remember(title) {
        val lower = title.lowercase()
        when {
            lower.contains("netflix") -> "netflix"

            // 🛠️ CORRECCIÓN AQUÍ: Cambia "disney_plus" por "disney"
            lower.contains("disney") -> "disney"

            // 🛠️ CORRECCIÓN EXTRA: Cambia "hbo_max" por "icons8_hbo_max_100" (o como lo renombraras)
            lower.contains("hbo") || lower.contains("max") -> "icons8_hbo_max_100"

            lower.contains("spotify") -> "spotify"
            lower.contains("youtube_music") -> "yt_music" // Tu archivo se llama 'yt_music.png'
            lower.contains("youtube") -> "youtube"

            // 🛠️ CORRECCIÓN EXTRA: Tu archivo es 'prime.png', no 'prime_video'
            lower.contains("amazon") || lower.contains("prime") -> "prime"

            // 🛠️ CORRECCIÓN EXTRA: Tu archivo es 'icons8_playstation_96.png'
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
            contentScale = ContentScale.Fit, // 🛠️ 'Fit' mantiene la proporción original del logo
            modifier = modifier
                .shadow(1.dp, RoundedCornerShape(12.dp)) // Eleva la tarjeta del logo
                .clip(RoundedCornerShape(12.dp))         // Bordes redondeados premium
                .background(Color.White)                 // 🛠️ Forzamos fondo blanco puro de banquero
                .padding(4.dp)                           // 🛠️ Margen interno para que el logo respire
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
                        Color(0xFF0F172A), // Slate
                        Color(0xFF0284C7), // Sky
                        Color(0xFF0D9488), // Teal
                        Color(0xFF4F46E5), // Indigo
                        Color(0xFF7C3AED), // Violet
                        Color(0xFFDB2777), // Pink
                        Color(0xFFDC2626)  // Red
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