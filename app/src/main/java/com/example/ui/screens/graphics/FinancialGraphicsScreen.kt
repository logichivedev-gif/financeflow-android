package com.example.ui.screens.graphics

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.FinancialProfile
import com.example.domain.CalculateCycleBalanceUseCase
import com.example.domain.CriticalFinancialEngine
import com.example.ui.FinanceViewModel
import com.example.ui.common.*

private val ColorFixedSlices = Color(0xFF0284C7)      // Cyan/Blue
private val ColorVariableSlices = Color(0xFFF59E0B)   // Amber/Orange
private val ColorSavingsSlices = Color(0xFF10B981)    // Emerald Green
private val ColorDeficitSlices = Color(0xFFEF4444)    // Red

@OptIn(ExperimentalMaterial3Api::class, CriticalFinancialEngine::class)
@Composable
fun FinancialGraphicsScreen(
    viewModel: FinanceViewModel,
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val dbProfile by viewModel.dbProfile.collectAsStateWithLifecycle()
    val dbCategories by viewModel.dbCategories.collectAsStateWithLifecycle()
    val dbVariableExpenses by viewModel.dbVariableExpenses.collectAsStateWithLifecycle()
    val bankBalance by viewModel.bankBalance.collectAsStateWithLifecycle()
    val projectedRemainingVariable by viewModel.projectedRemainingVariable.collectAsStateWithLifecycle()

    val currencySymbol = dbProfile?.selectedCurrency ?: defaultCurrencySymbol
    val incomeDay = dbProfile?.incomeDay ?: 1
    val currentCal = viewModel.getCurrentCalendar()

    // Consumo directo del motor matemático protegido con @CriticalFinancialEngine
    val useCase = viewModel.calculateCycleBalanceUseCase
    val pendingFixedInCycle = useCase.getPendingExpensesForCycle(dbCategories, incomeDay, currentCal)
    val rawCycleBalance = useCase(bankBalance, dbProfile ?: FinancialProfile(), dbCategories, currentCal)

    val totalFixed = remember(dbCategories) {
        dbCategories.filter { !it.isArchived && it.isFixed && !it.assumedByPartner && !it.isSkippedThisMonth }
            .sumOf { it.limitAmount }
    }
    val paidFixed = remember(dbCategories) {
        dbCategories.filter { !it.isArchived && it.isFixed && it.isPaid }
            .sumOf { it.limitAmount }
    }

    val totalVariableSpent = remember(dbVariableExpenses) { dbVariableExpenses.sumOf { it.amount } }
    val totalVariableBudget = remember(dbCategories) {
        dbCategories.filter { !it.isArchived && !it.isFixed && it.isAdded }.sumOf { it.limitAmount }
    }

    // Saldo Libre Real neto tras descontar el presupuesto variable pendiente
    val saldoLibreReal = rawCycleBalance - projectedRemainingVariable
    val effectiveSavings = maxOf(0.0, saldoLibreReal)

    // Segmentos para el gráfico de dona
    val fixedSegmentAmount = totalFixed
    val variableSegmentAmount = maxOf(totalVariableSpent, totalVariableBudget)
    val savingsSegmentAmount = effectiveSavings
    val totalPie = fixedSegmentAmount + variableSegmentAmount + savingsSegmentAmount

    val fixedPct = if (totalPie > 0) (fixedSegmentAmount / totalPie * 100).toFloat() else 0f
    val variablePct = if (totalPie > 0) (variableSegmentAmount / totalPie * 100).toFloat() else 0f
    val savingsPct = if (totalPie > 0) (savingsSegmentAmount / totalPie * 100).toFloat() else 0f

    val cycleDays = remember(incomeDay) { viewModel.getFinancialCycleDays(incomeDay) }
    val remainingDays = cycleDays.second

    val isCompact = windowWidthSizeClass == WindowWidthSizeClass.Compact

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(FinanceSoftBg),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(id = R.string.graphics_title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = FinanceSlateDark,
                                letterSpacing = (-0.5).sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(id = R.string.graphics_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = FinanceTeal
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("graphics_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = FinanceSlateDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        if (isCompact) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(FinanceSoftBg)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tarjeta central de la Dona Canvas
                DonutChartCard(
                    fixedPct = fixedPct,
                    variablePct = variablePct,
                    savingsPct = savingsPct,
                    saldoLibreReal = saldoLibreReal,
                    currencySymbol = currencySymbol
                )

                // Tarjetas con desglose detallado de métricas
                GraphicsBreakdownList(
                    currencySymbol = currencySymbol,
                    totalFixed = totalFixed,
                    pendingFixedInCycle = pendingFixedInCycle,
                    paidFixed = paidFixed,
                    fixedPct = fixedPct,
                    totalVariableSpent = totalVariableSpent,
                    totalVariableBudget = totalVariableBudget,
                    variablePct = variablePct,
                    saldoLibreReal = saldoLibreReal,
                    savingsPct = savingsPct
                )

                // Tarjeta de información del ciclo universal de tesorería
                CycleEngineInfoCard(
                    incomeDay = incomeDay,
                    remainingDays = remainingDays
                )
            }
        } else {
            // Layout adaptativo para Tablets y pantallas expandidas
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(FinanceSoftBg)
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    DonutChartCard(
                        fixedPct = fixedPct,
                        variablePct = variablePct,
                        savingsPct = savingsPct,
                        saldoLibreReal = saldoLibreReal,
                        currencySymbol = currencySymbol,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GraphicsBreakdownList(
                        currencySymbol = currencySymbol,
                        totalFixed = totalFixed,
                        pendingFixedInCycle = pendingFixedInCycle,
                        paidFixed = paidFixed,
                        fixedPct = fixedPct,
                        totalVariableSpent = totalVariableSpent,
                        totalVariableBudget = totalVariableBudget,
                        variablePct = variablePct,
                        saldoLibreReal = saldoLibreReal,
                        savingsPct = savingsPct
                    )

                    CycleEngineInfoCard(
                        incomeDay = incomeDay,
                        remainingDays = remainingDays
                    )
                }
            }
        }
    }
}

@Composable
private fun DonutChartCard(
    fixedPct: Float,
    variablePct: Float,
    savingsPct: Float,
    saldoLibreReal: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Distribución del Ciclo Activo",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = FinanceSlateDark
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(240.dp)
                    .testTag("graphics_donut_canvas")
            ) {
                DonutCanvas(
                    fixedPct = fixedPct,
                    variablePct = variablePct,
                    savingsPct = savingsPct,
                    isDeficit = saldoLibreReal < 0.0
                )

                // Contenido central de la dona
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Saldo Libre Real",
                        style = MaterialTheme.typography.labelSmall,
                        color = FinanceSlateLight,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatCurrency(saldoLibreReal, currencySymbol),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (saldoLibreReal >= 0) ColorSavingsSlices else ColorDeficitSlices
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        color = if (saldoLibreReal >= 0) ColorSavingsSlices.copy(alpha = 0.12f) else ColorDeficitSlices.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = if (saldoLibreReal >= 0) "✅ Tesorería Cubierta" else "⚠️ Déficit de Ciclo",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (saldoLibreReal >= 0) ColorSavingsSlices else ColorDeficitSlices,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Indicadores de la leyenda en línea
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendIndicatorItem(color = ColorFixedSlices, label = "Fijos", pct = fixedPct)
                LegendIndicatorItem(color = ColorVariableSlices, label = "Variables", pct = variablePct)
                LegendIndicatorItem(
                    color = if (saldoLibreReal >= 0) ColorSavingsSlices else ColorDeficitSlices,
                    label = if (saldoLibreReal >= 0) "Libre" else "Déficit",
                    pct = savingsPct
                )
            }
        }
    }
}

@Composable
private fun DonutCanvas(
    fixedPct: Float,
    variablePct: Float,
    savingsPct: Float,
    isDeficit: Boolean
) {
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "donut_animation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 28.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)

        val total = fixedPct + variablePct + savingsPct
        if (total <= 0f) {
            // Círculo placeholder si no hay datos
            drawArc(
                color = Color(0xFFE2E8F0),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            return@Canvas
        }

        val fixedAngle = (fixedPct / 100f) * 360f * animatedProgress
        val variableAngle = (variablePct / 100f) * 360f * animatedProgress
        val savingsAngle = (savingsPct / 100f) * 360f * animatedProgress

        val spacing = if (total > 0 && (fixedPct > 0 && variablePct > 0 || savingsPct > 0)) 3f else 0f
        var currentAngle = -90f

        // 1. Segmento Gastos Fijos
        if (fixedAngle > 0f) {
            val sweep = maxOf(0f, fixedAngle - spacing)
            drawArc(
                color = ColorFixedSlices,
                startAngle = currentAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            currentAngle += fixedAngle
        }

        // 2. Segmento Gastos Variables
        if (variableAngle > 0f) {
            val sweep = maxOf(0f, variableAngle - spacing)
            drawArc(
                color = ColorVariableSlices,
                startAngle = currentAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            currentAngle += variableAngle
        }

        // 3. Segmento Capacidad de Ahorro / Saldo Libre Real
        if (savingsAngle > 0f) {
            val sweep = maxOf(0f, savingsAngle - spacing)
            drawArc(
                color = if (isDeficit) ColorDeficitSlices else ColorSavingsSlices,
                startAngle = currentAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun LegendIndicatorItem(color: Color, label: String, pct: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$label (${pct.toInt()}%)",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = FinanceSlateDark,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun GraphicsBreakdownList(
    currencySymbol: String,
    totalFixed: Double,
    pendingFixedInCycle: Double,
    paidFixed: Double,
    fixedPct: Float,
    totalVariableSpent: Double,
    totalVariableBudget: Double,
    variablePct: Float,
    saldoLibreReal: Double,
    savingsPct: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Tarjeta Gastos Fijos
        BreakdownMetricCard(
            title = "Gastos Fijos y Obligaciones",
            amount = formatCurrency(totalFixed, currencySymbol),
            pct = fixedPct,
            color = ColorFixedSlices,
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            badge1Label = "Pendientes ciclo: ${formatCurrency(pendingFixedInCycle, currencySymbol)}",
            badge2Label = "Ya pagados: ${formatCurrency(paidFixed, currencySymbol)}",
            footerText = "Calculado con CalculateCycleBalanceUseCase según payDay individual."
        )

        // 2. Tarjeta Gastos Variables
        BreakdownMetricCard(
            title = "Gastos Variables y Diario",
            amount = formatCurrency(totalVariableSpent, currencySymbol),
            pct = variablePct,
            color = ColorVariableSlices,
            icon = Icons.Default.ShoppingBag,
            badge1Label = "Gastado: ${formatCurrency(totalVariableSpent, currencySymbol)}",
            badge2Label = "Presupuestado: ${formatCurrency(totalVariableBudget, currencySymbol)}",
            footerText = "Consumo acumulado frente a topes asignados por categoría."
        )

        // 3. Tarjeta Saldo Libre Real / Capacidad de Ahorro
        BreakdownMetricCard(
            title = "Capacidad de Ahorro / Saldo Libre",
            amount = formatCurrency(saldoLibreReal, currencySymbol),
            pct = savingsPct,
            color = if (saldoLibreReal >= 0) ColorSavingsSlices else ColorDeficitSlices,
            icon = Icons.Default.Savings,
            badge1Label = if (saldoLibreReal >= 0) "Margen Neto Positivo" else "Déficit de Tesorería",
            badge2Label = "Motor @CriticalFinancialEngine",
            footerText = "Dinero 100% disponible tras blindar los compromisos hasta la próxima nómina."
        )
    }
}

@Composable
private fun BreakdownMetricCard(
    title: String,
    amount: String,
    pct: Float,
    color: Color,
    icon: ImageVector,
    badge1Label: String,
    badge2Label: String,
    footerText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = FinanceSlateDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${pct.toInt()}% del total asignado",
                            style = MaterialTheme.typography.labelSmall,
                            color = FinanceSlateLight
                        )
                    }
                }

                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = color
                )
            }

            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = color.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = badge1Label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp
                    )
                }

                Surface(
                    color = FinanceSoftBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = badge2Label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = FinanceSlateDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp
                    )
                }
            }

            Text(
                text = footerText,
                style = MaterialTheme.typography.bodySmall,
                color = FinanceSlateLight,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun CycleEngineInfoCard(
    incomeDay: Int,
    remainingDays: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF16A34A).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Motor de Tesorería en Tiempo Real",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF166534)
                )
                Text(
                    text = "Próximo cobro: Día $incomeDay ($remainingDays días restantes de ciclo). Cálculo blindado matemáticamente de cobro a cobro.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF15803D),
                    fontSize = 12.sp
                )
            }
        }
    }
}
