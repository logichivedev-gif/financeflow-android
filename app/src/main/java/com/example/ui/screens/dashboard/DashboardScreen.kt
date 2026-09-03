package com.example.ui.screens.dashboard

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.activity.compose.BackHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.FinanceViewModel
import com.example.ui.common.*
import com.example.ui.components.SubscriptionLogo
import com.example.ui.screens.fixedbills.FixedBillsPane
import com.example.ui.screens.history.ArchivedCategoriesPane
import com.example.ui.screens.variable.VariableSpentPane
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class DashboardTab {
    RESUMEN,
    GASTOS_FIJOS,
    GASTOS_VARIABLES,
    ANALISIS
}

@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    windowWidthSizeClass: WindowWidthSizeClass,
    onShowAbout: () -> Unit,
    onOpenManagementMenu: () -> Unit = {}
) {
    val context = LocalContext.current
    val dbProfile by viewModel.dbProfile.collectAsStateWithLifecycle()
    val dbCategories by viewModel.dbCategories.collectAsStateWithLifecycle()
    val dbVariableExpenses by viewModel.dbVariableExpenses.collectAsStateWithLifecycle()
    val totalFixedAmount by viewModel.totalGastosFijos.collectAsStateWithLifecycle()
    val totalVariableSpent by viewModel.totalGastosVariables.collectAsStateWithLifecycle()
    val saldoRestanteDisponible by viewModel.saldoRestanteDisponible.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var activeTab by remember { mutableStateOf(DashboardTab.RESUMEN) }
    var showHistoryPane by remember { mutableStateOf(false) }

    // Manejo de redirección desde los accesos rápidos de los widgets (v1.5.0)
    val activity = context as? ComponentActivity
    LaunchedEffect(activity, activity?.intent) {
        val action = activity?.intent?.getStringExtra("action")
        if (action != null) {
            when (action) {
                "add_expense" -> {
                    activeTab = DashboardTab.GASTOS_VARIABLES
                }
                "add_income" -> {
                    activeTab = DashboardTab.RESUMEN
                }
            }
            activity.intent.removeExtra("action")
        }
    }

    // Sincronización reactiva automática de todos los widgets al cambiar el saldo o los gastos
    LaunchedEffect(saldoRestanteDisponible, dbVariableExpenses) {
        com.example.widget.WidgetUpdateHelper.updateAllWidgets(context)
    }
    val income = dbProfile?.monthlyIncome ?: 0.0
    val fixedExpenses = dbCategories.filter { it.isFixed && !it.isArchived }
    val paidFixedAmount = fixedExpenses.filter { it.isPaid }.sumOf { it.limitAmount }

    val isCompact = windowWidthSizeClass == WindowWidthSizeClass.Compact

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isCompact) {
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
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(FinanceSoftBg)
        ) {
            if (!isCompact) {
                NavigationRail(
                    containerColor = Color.White,
                    header = {
                        val selectedIconId = dbProfile?.selectedIcon ?: "trending"
                        val appLogoIcon = when(selectedIconId) {
                            "wallet" -> Icons.Default.AccountBalanceWallet
                            "savings" -> Icons.Default.Savings
                            "payments" -> Icons.Default.Payments
                            "chart" -> Icons.Default.ShowChart
                            "star" -> Icons.Default.Star
                            else -> Icons.Default.TrendingUp
                        }
                        Box(
                            modifier = Modifier
                                .padding(vertical = 24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(FinanceTeal.copy(alpha = 0.10f))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = appLogoIcon,
                                contentDescription = "Logo",
                                tint = FinanceTeal,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    NavigationRailItem(
                        selected = activeTab == DashboardTab.RESUMEN,
                        onClick = { activeTab = DashboardTab.RESUMEN },
                        label = { Text("Resumen", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Resumen") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = FinanceTeal,
                            selectedTextColor = FinanceTeal,
                            indicatorColor = FinanceTeal.copy(alpha = 0.1f),
                            unselectedIconColor = FinanceSlateLight,
                            unselectedTextColor = FinanceSlateLight
                        ),
                        modifier = Modifier.testTag("rail_tab_resumen")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    NavigationRailItem(
                        selected = activeTab == DashboardTab.GASTOS_FIJOS,
                        onClick = { activeTab = DashboardTab.GASTOS_FIJOS },
                        label = { Text("Fijos", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        icon = { Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = "Gastos Fijos") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = FinanceTeal,
                            selectedTextColor = FinanceTeal,
                            indicatorColor = FinanceTeal.copy(alpha = 0.1f),
                            unselectedIconColor = FinanceSlateLight,
                            unselectedTextColor = FinanceSlateLight
                        ),
                        modifier = Modifier.testTag("rail_tab_fijos")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    NavigationRailItem(
                        selected = activeTab == DashboardTab.GASTOS_VARIABLES,
                        onClick = { activeTab = DashboardTab.GASTOS_VARIABLES },
                        label = { Text("Variables", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        icon = { Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = "Gastos Variables") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = FinanceTeal,
                            selectedTextColor = FinanceTeal,
                            indicatorColor = FinanceTeal.copy(alpha = 0.1f),
                            unselectedIconColor = FinanceSlateLight,
                            unselectedTextColor = FinanceSlateLight
                        ),
                        modifier = Modifier.testTag("rail_tab_variables")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    NavigationRailItem(
                        selected = activeTab == DashboardTab.ANALISIS,
                        onClick = { activeTab = DashboardTab.ANALISIS },
                        label = { Text("Análisis", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        icon = { Icon(imageVector = Icons.Default.TrendingUp, contentDescription = "Análisis") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = FinanceTeal,
                            selectedTextColor = FinanceTeal,
                            indicatorColor = FinanceTeal.copy(alpha = 0.1f),
                            unselectedIconColor = FinanceSlateLight,
                            unselectedTextColor = FinanceSlateLight
                        ),
                        modifier = Modifier.testTag("rail_tab_analisis")
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
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
                            variableSpent = totalVariableSpent,
                            windowWidthSizeClass = windowWidthSizeClass,
                            onOpenManagementMenu = onOpenManagementMenu
                        )
                        DashboardTab.GASTOS_FIJOS -> {
                            if (showHistoryPane) {
                                BackHandler { showHistoryPane = false }
                                ArchivedCategoriesPane(
                                    viewModel = viewModel,
                                    onBack = { showHistoryPane = false }
                                )
                            } else {
                                FixedBillsPane(
                                    viewModel = viewModel,
                                    fixedExpenses = fixedExpenses,
                                    windowWidthSizeClass = windowWidthSizeClass,
                                    onNavigateToHistory = { showHistoryPane = true }
                                )
                            }
                        }
                        DashboardTab.GASTOS_VARIABLES -> VariableSpentPane(
                            viewModel = viewModel,
                            dbCategories = dbCategories,
                            variableExpenses = dbVariableExpenses,
                            totalSpent = totalVariableSpent,
                            windowWidthSizeClass = windowWidthSizeClass
                        )
                        DashboardTab.ANALISIS -> AnalysisPane(
                            viewModel = viewModel,
                            income = income,
                            snackbarHostState = snackbarHostState,
                            windowWidthSizeClass = windowWidthSizeClass
                        )
                    }
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
    variableSpent: Double,
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    onOpenManagementMenu: () -> Unit = {}
) {
    val dbProfile by viewModel.dbProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isAccordionOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.runSmartAutoCheck()
    }

    val isCompact = windowWidthSizeClass == WindowWidthSizeClass.Compact

    val headerContent = @Composable {
        Text(
            text = "Resumen Financiero",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = FinanceSlateDark
        )
    }

    val saldoDisponibleCard = @Composable {
        var showSummaryInfoModal by remember { mutableStateOf(false) }

        val isPositiveNet = saldoRestante >= 0
        val statusColor = if (isPositiveNet) Color(0xFF16A34A) else Color(0xFFDC2626)
        val statusBg = if (isPositiveNet) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
        val statusBorder = if (isPositiveNet) Color(0xFFBBF7D0) else Color(0xFFFCA5A5)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("remaining_payment_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = statusBg),
            border = BorderStroke(1.5.dp, statusBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (isPositiveNet) "🟢 SALDO DISPONIBLE REAL" else "🛑 ALERTA DE DESCUBIERTO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = statusColor
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // BOTÓN DE SINCRONIZACIÓN PREMIUM (v1.5.0)
                        var isSyncing by remember { mutableStateOf(false) }
                        val rotationAngle by animateFloatAsState(
                            targetValue = if (isSyncing) 360f else 0f,
                            animationSpec = tween(
                                durationMillis = 1000,
                                easing = LinearEasing
                            ),
                            label = "sync_rotation"
                        )

                        LaunchedEffect(isSyncing) {
                            if (isSyncing) {
                                delay(1000)
                                com.example.widget.WidgetUpdateHelper.updateAllWidgets(context)
                                isSyncing = false
                                Toast.makeText(
                                    context,
                                    "¡Sincronización exitosa!\nSaldos, fijos pendientes y widgets de inicio actualizados.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        IconButton(
                            onClick = { isSyncing = true },
                            modifier = Modifier
                                .size(36.dp)
                                .graphicsLayer { rotationZ = rotationAngle }
                                .testTag("credit_card_sync_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sincronizar saldo y widgets",
                                tint = statusColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Icono (i) para Divulgación Progresiva
                        IconButton(
                            onClick = { showSummaryInfoModal = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Ver explicación detallada",
                                tint = statusColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // NÚMERO GIGANTE CENTRAL (38.sp)
                Text(
                    text = saldoRestante.formatCurrency(),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 38.sp
                    ),
                    color = statusColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("remaining_payment_text")
                )

                Spacer(modifier = Modifier.height(4.dp))

                val shortStatusLabel = when {
                    saldoRestante < 0.0 -> "Límite excedido • Riesgo de descubierto"
                    saldoRestante <= 50.0 -> "Margen ajustado • Mantén precaución"
                    else -> "Situación saneada • Facturas cubiertas"
                }

                Text(
                    text = shortStatusLabel,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = statusColor.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }
        }

        if (showSummaryInfoModal) {
            val fullExplanation = when {
                saldoRestante < 0.0 -> stringResource(id = R.string.status_danger)
                saldoRestante <= 50.0 -> stringResource(id = R.string.status_warning)
                else -> stringResource(id = R.string.status_safe)
            }

            AlertDialog(
                onDismissRequest = { showSummaryInfoModal = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = {
                    Text(
                        text = "Explicación del Saldo Disponible",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = fullExplanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = FinanceSlateDark
                        )
                        HorizontalDivider(color = FinanceBorder.copy(alpha = 0.5f))
                        Text(
                            text = "💡 Fórmula aplicada:\n[Saldo real en banco] - [Facturas pendientes del mes actual]",
                            style = MaterialTheme.typography.labelSmall,
                            color = FinanceSlateLight
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSummaryInfoModal = false }) {
                        Text("Entendido", fontWeight = FontWeight.Bold, color = FinanceTeal)
                    }
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }

    val updateBalanceCard = @Composable {
        var isEditingBalance by remember { mutableStateOf(false) }
        var bankBalanceInput by remember(dbProfile?.currentBankBalance) {
            mutableStateOf(if ((dbProfile?.currentBankBalance ?: 0.0) > 0.0) dbProfile!!.currentBankBalance.toString() else "")
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Saldo Actual en Banco",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = FinanceSlateDark
                        )
                        Text(
                            text = "Actualízalo con la app de tu banco cuando quieras.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FinanceSlateLight
                        )
                    }
                    if (!isEditingBalance) {
                        TextButton(
                            onClick = { isEditingBalance = true },
                            modifier = Modifier.testTag("update_bank_balance_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = FinanceTeal)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Modificar", color = FinanceTeal, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (isEditingBalance) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = bankBalanceInput,
                            onValueChange = { bankBalanceInput = it.replace(',', '.').filter { c -> c.isDigit() || c == '.' } },
                            placeholder = { Text("Ej: 1450.50") },
                            modifier = Modifier.weight(1f).testTag("bank_balance_text_field"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FinanceTeal,
                                unfocusedBorderColor = FinanceBorder
                            )
                        )
                        Button(
                            onClick = {
                                val newBal = bankBalanceInput.toDoubleOrNull() ?: 0.0
                                viewModel.updateBankBalanceDashboard(newBal)
                                isEditingBalance = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                            modifier = Modifier.testTag("save_bank_balance_btn")
                        ) {
                            Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    val metricsRow = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card 1: Ingreso
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
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
                    }
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
                    } else {
                        Text(
                            text = "mensual",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = FinanceSlateLight
                        )
                    }
                }
            }

            // Card 2: Fijos Pagados
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
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
                    }
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
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
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
                    }
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

    val progressFixedCard = @Composable {
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

    val accordionCard = @Composable {
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
                        .clickable { isAccordionOpen = !isAccordionOpen },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Ajustes rápidos",
                            tint = FinanceTeal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ajustes Rápidos de Situación",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = FinanceSlateDark
                        )
                    }
                    Icon(
                        imageVector = if (isAccordionOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isAccordionOpen) "Colapsar" else "Expandir",
                        tint = FinanceSlateLight
                    )
                }

                AnimatedVisibility(visible = isAccordionOpen) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
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

                        // Mascotas
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Pets, contentDescription = "Mascotas", tint = FinanceSlateLight)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mascotas", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
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
                                label = { Text("Importe Estimado Mascotas (€)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // Hijos
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.ChildCare, contentDescription = "Hijos", tint = FinanceSlateLight)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hijos", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
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
                                label = { Text("Importe Estimado Hijos (€)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        // Pareja
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

                        // Luz
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

                        // Agua
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

                        // Suscripciones
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
                                                        val filtered = newVal.replace(',', '.').filter { it.isDigit() || it == '.' }
                                                        tempPriceStr = filtered
                                                    },
                                                    label = { Text("Importe (€)", fontSize = 10.sp) },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
            }
        }
    }

    val resetCard = @Composable {
        var showConfirmDialog by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = "Asistente Inicial",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = FinanceSlateDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Reinicia la configuración guiada de 4 pasos si deseas reconfigurar tu perfil.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FinanceSlateLight
                        )
                    }
                    Button(
                        onClick = { showConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("reset_wizard_btn")
                    ) {
                        Text("Reconfigurar", color = FinanceTeal, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                icon = {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, tint = FinanceTeal)
                },
                title = {
                    Text("¿Reconfigurar Asistente?", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("Volverás al Asistente Inicial de 4 pasos para configurar tu sueldo y categorías. Tus facturas personalizadas se mantendrán.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetToWizard()
                            showConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Sí, Reconfigurar", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Cancelar", color = FinanceSlateLight)
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }

    if (isCompact) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            headerContent()
            saldoDisponibleCard()
            updateBalanceCard()
            metricsRow()
            progressFixedCard()
            accordionCard()
            resetCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            headerContent()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Columna Izquierda (Equilibrada - 50%)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    saldoDisponibleCard()
                    updateBalanceCard()
                    metricsRow()
                }

                // Columna Derecha (Equilibrada - 50%)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    progressFixedCard()
                    accordionCard()
                    resetCard()
                }
            }
        }
    }
}

@Composable
fun AnalysisPane(
    viewModel: FinanceViewModel,
    income: Double,
    snackbarHostState: SnackbarHostState,
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val dbProfile by viewModel.dbProfile.collectAsStateWithLifecycle()
    val dbCategories by viewModel.dbCategories.collectAsStateWithLifecycle()
    val dbVariableExpenses by viewModel.dbVariableExpenses.collectAsStateWithLifecycle()
    val monthProjection by viewModel.monthProjection.collectAsStateWithLifecycle()

    val digitalBalanceInput by viewModel.digitalBalanceInput.collectAsStateWithLifecycle()
    val gastoVariableHoyInput by viewModel.gastoVariableHoyInput.collectAsStateWithLifecycle()

    val initialBalance = if (dbProfile != null && dbProfile!!.currentBankBalance > 0.0) dbProfile!!.currentBankBalance else income
    val digitalBalance = digitalBalanceInput.toDoubleOrNull() ?: initialBalance

    val calendar = java.util.Calendar.getInstance()
    val elapsedDays = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val totalDaysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    val remainingDays = maxOf(1, totalDaysInMonth - elapsedDays)

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
        !category.isArchived && (category.isFixed || category.isFinancing) && !category.assumedByPartner && !category.isSkippedThisMonth && !category.isPaid &&
                viewModel.shouldRetainUnpaidFixedExpense(category.payDay, dbProfile?.incomeDay ?: 0)
    }
    val saldoRetenido = monthProjection?.pendingFixed ?: unpaidFixedExpenses.sumOf { it.limitAmount }

    val totalVariableBudget = dbCategories.filter { !it.isFixed && it.isAdded }.sumOf { it.limitAmount }
    val totalVariableSpent = dbVariableExpenses.sumOf { it.amount }
    val projectedRemainingVariable = maxOf(0.0, totalVariableBudget - totalVariableSpent)

    val simulatedExtraSpentToday = if (gastoVariableHoy > realGastoVariableHoy) gastoVariableHoy - realGastoVariableHoy else 0.0

    val cycleDays = dbProfile?.let { viewModel.getFinancialCycleDays(it.incomeDay) } ?: Triple(elapsedDays, remainingDays, totalDaysInMonth)
    val cycleRemainingDays = cycleDays.second

    val isCompact = windowWidthSizeClass == WindowWidthSizeClass.Compact

    val margenLibre = digitalBalance - saldoRetenido - projectedRemainingVariable - simulatedExtraSpentToday

    val headerContent = @Composable {
        Text(
            text = "Análisis y Proyecciones",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = FinanceSlateDark
        )
    }

    val margenLibreHeroBannerAndMetrics = @Composable {
        var showEngineInfoModal by remember { mutableStateOf(false) }

        val isPositiveNet = margenLibre > 0.0
        val statusColor = if (isPositiveNet) Color(0xFF16A34A) else Color(0xFFDC2626)
        val statusBg = if (isPositiveNet) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
        val statusBorder = if (isPositiveNet) Color(0xFFBBF7D0) else Color(0xFFFCA5A5)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // BANNER PRINCIPAL
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("intelligent_finance_hero_banner"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = statusBg),
                border = BorderStroke(1.5.dp, statusBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isPositiveNet) "🟢 MARGEN LIBRE DISPONIBLE" else "🛑 ALERTA DE DESCUBIERTO",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = statusColor
                            )
                        }

                        IconButton(
                            onClick = { showEngineInfoModal = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Ver explicación detallada",
                                tint = statusColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = margenLibre.formatCurrency(),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 38.sp
                        ),
                        color = statusColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isPositiveNet) "Dinero real disponible sin comprometer recibos ($cycleRemainingDays días restantes de ciclo)" else "Superas tu límite disponible para este ciclo ($cycleRemainingDays días restantes)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = statusColor.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // MÉTRICAS DE ANÁLISIS EN BLOQUE COMPACTO (3 COLUMNAS HORIZONTALES)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, FinanceBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = "Saldo Banco",
                                tint = FinanceTeal,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Saldo Banco", fontSize = 11.sp, color = FinanceSlateLight, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = digitalBalance.formatCurrency(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = FinanceSlateDark,
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(FinanceBorder)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Comprometido",
                                tint = Color(0xFFE11D48),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Comprometido", fontSize = 11.sp, color = Color(0xFF9F1239), fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = saldoRetenido.formatCurrency(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4C0519),
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(32.dp)
                            .background(FinanceBorder)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPositiveNet) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = "Margen Libre",
                                tint = statusColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Margen Libre", fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = margenLibre.formatCurrency(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            maxLines = 1
                        )
                    }
                }
            }

            // Inputs para personalizar saldo y simular gasto variable
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = digitalBalanceInput,
                    onValueChange = { viewModel.updateDigitalBalanceInput(it) },
                    label = { Text("Tu saldo en banco (${CurrencySymbol})", fontSize = 11.sp) },
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
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = gastoVariableHoyInput,
                    onValueChange = { viewModel.updateGastoVariableHoyInput(it) },
                    label = { Text("Simular gasto hoy (${CurrencySymbol})", fontSize = 11.sp) },
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
                                snackbarHostState.showSnackbar("Simulación de gasto aplicada")
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FinanceTeal,
                        unfocusedBorderColor = FinanceBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        if (showEngineInfoModal) {
            AlertDialog(
                onDismissRequest = { showEngineInfoModal = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = FinanceTeal,
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = {
                    Text(
                        text = "Motor de Dinero Disponible",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Este motor calcula exactamente cuánto dinero puedes gastar libremente sin comprometer el pago de tus recibos futuros ni tus presupuestos asignados.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FinanceSlateDark
                        )
                        HorizontalDivider(color = FinanceBorder.copy(alpha = 0.5f))
                        Text(
                            text = "Fórmula aplicada paso a paso:\n" +
                                    "1. Saldo actual en banco: ${digitalBalance.formatCurrency()}\n" +
                                    "2. Menos facturas pendientes (retenidas): -${saldoRetenido.formatCurrency()}\n" +
                                    "3. Menos presupuesto variable restante: -${projectedRemainingVariable.formatCurrency()}\n" +
                                    (if (simulatedExtraSpentToday > 0.0) "4. Menos gasto simulado extra hoy: -${simulatedExtraSpentToday.formatCurrency()}\n" else "") +
                                    "= Margen Libre: ${margenLibre.formatCurrency()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = FinanceSlateDark
                        )
                        HorizontalDivider(color = FinanceBorder.copy(alpha = 0.5f))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = statusBg),
                            border = BorderStroke(1.dp, statusBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isPositiveNet) "🟢 Margen Libre: ${margenLibre.formatCurrency()}" else "🛑 Límite Excedido: ${margenLibre.formatCurrency()}",
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isPositiveNet) {
                                        "Te quedan ${margenLibre.formatCurrency()} disponibles en tu cuenta para los $cycleRemainingDays días restantes del ciclo sin poner en riesgo ningún pago fijo."
                                    } else {
                                        "¡Atención! Tu velocidad actual de gasto proyecta un descubierto de ${(-margenLibre).formatCurrency()} antes de finalizar los $cycleRemainingDays días restantes del ciclo."
                                    },
                                    fontSize = 12.sp,
                                    color = statusColor
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showEngineInfoModal = false }) {
                        Text("Entendido", fontWeight = FontWeight.Bold, color = FinanceTeal)
                    }
                }
            )
        }
    }

    val dailyPaceCard = @Composable {
        val dailyRecommended = maxOf(0.0, margenLibre) / maxOf(1, cycleRemainingDays)
        val isPositive = margenLibre > 0.0

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_pace_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, FinanceBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(FinanceTeal.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Ritmo",
                                tint = FinanceTeal,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Ritmo de Gasto Diario",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = FinanceSlateDark
                            )
                            Text(
                                text = "$cycleRemainingDays días restantes para fin de ciclo",
                                style = MaterialTheme.typography.bodySmall,
                                color = FinanceSlateLight
                            )
                        }
                    }
                }

                HorizontalDivider(color = FinanceBorder.copy(alpha = 0.6f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Presupuesto sugerido:",
                            style = MaterialTheme.typography.bodySmall,
                            color = FinanceSlateLight
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = dailyRecommended.formatCurrency(),
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (isPositive) FinanceTeal else CardBorderRed
                            )
                            Text(
                                text = " / día",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = FinanceSlateLight,
                                modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isPositive) Color(0xFFF0FDF4) else Color(0xFFFEF2F2))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isPositive) "Sostenible" else "Ajustar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isPositive) Color(0xFF16A34A) else Color(0xFFDC2626)
                        )
                    }
                }

                Text(
                    text = if (isPositive) {
                        "Gastando como máximo ${dailyRecommended.formatCurrency()} al día en variables, llegarás a fin de mes con todas tus facturas cubiertas."
                    } else {
                        "Has rebasado el margen de seguridad para este ciclo. Conviene posponer compras no esenciales."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = FinanceSlateLight
                )
            }
        }
    }

    val monthProjectionCard = @Composable {
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
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
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
                        HorizontalDivider(color = cardBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Saldo Final Estimado", fontSize = 12.sp, color = FinanceSlateLight)
                                Text(
                                    text = proj.projectedMonthEndBalance.formatCurrency(),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = accentColor
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Gasto Fijo Total", fontSize = 12.sp, color = FinanceSlateLight)
                                Text(
                                    text = proj.totalFixed.formatCurrency(),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = FinanceSlateDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Facturas pendientes por pagar:", fontSize = 12.sp, color = FinanceSlateLight)
                            Text(
                                text = proj.pendingFixed.formatCurrency(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (proj.pendingFixed > 0.0) CardBorderRed else FinanceTeal
                            )
                        }
                    }
                }
            }
        }
    }

    if (isCompact) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            headerContent()
            margenLibreHeroBannerAndMetrics()
            monthProjectionCard()
            dailyPaceCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            headerContent()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Columna Izquierda (50% equilibrada)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    margenLibreHeroBannerAndMetrics()
                }

                // Columna Derecha (50% equilibrada)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    monthProjectionCard()
                    dailyPaceCard()
                }
            }
        }
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
