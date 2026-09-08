package com.example.ui.screens.fixedbills

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BillingFrequency
import com.example.data.ExpenseCategory
import com.example.ui.FinanceViewModel
import com.example.ui.common.*
import com.example.ui.components.SubscriptionLogo

@Composable
fun FixedBillsPane(
    viewModel: FinanceViewModel,
    fixedExpenses: List<ExpenseCategory>,
    windowWidthSizeClass: WindowWidthSizeClass,
    onNavigateToHistory: () -> Unit = {}
) {
    val context = LocalContext.current
    val dbProfile by viewModel.dbProfile.collectAsStateWithLifecycle()
    val currencySymbol = dbProfile?.selectedCurrency ?: "€"

    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var viewingPlanCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("TODOS") } // "TODOS", "PENDIENTES", "PAGADOS", "FINANCIACIONES"

    val activeList = fixedExpenses.filter { !it.isArchived }

    val filteredList = activeList.filter { item ->
        val matchesSearch = item.name.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "PENDIENTES" -> !item.isPaid && !item.isSkippedThisMonth
            "PAGADOS" -> item.isPaid
            "FINANCIACIONES" -> item.isFinancing
            else -> true
        }
        matchesSearch && matchesFilter
    }

    val totalActive = activeList.filter { !it.assumedByPartner && !it.isSkippedThisMonth }.sumOf { it.limitAmount }
    val paidActive = activeList.filter { !it.assumedByPartner && !it.isSkippedThisMonth && it.isPaid }.sumOf { it.limitAmount }
    val pendingActive = totalActive - paidActive

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, FinanceBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Obligaciones y Gastos Fijos",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = FinanceSlateDark
                        )
                        Text(
                            text = "${activeList.size} gastos registrados",
                            style = MaterialTheme.typography.bodySmall,
                            color = FinanceSlateLight
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón de acceso a la Bandeja de Historial
                        IconButton(
                            onClick = { onNavigateToHistory() },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(FinanceTeal.copy(alpha = 0.12f))
                                .testTag("history_bills_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Bandeja de Historial",
                                tint = FinanceTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Botón de Nuevo Gasto Fijo
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("add_fixed_bill_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nuevo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Divider(color = FinanceBorder.copy(alpha = 0.6f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Mes", style = MaterialTheme.typography.labelSmall, color = FinanceSlateLight)
                        Text(
                            text = formatCurrency(totalActive, currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = FinanceSlateDark
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pagado", style = MaterialTheme.typography.labelSmall, color = FinanceSlateLight)
                        Text(
                            text = formatCurrency(paidActive, currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF16A34A)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Pendiente", style = MaterialTheme.typography.labelSmall, color = FinanceSlateLight)
                        Text(
                            text = formatCurrency(pendingActive, currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (pendingActive > 0) CardBorderRed else Color(0xFF16A34A)
                        )
                    }
                }
            }
        }

        // Search & Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar gasto...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = FinanceSlateLight) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("search_fixed_bills_input")
            )
        }

        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "TODOS" to "Todos",
                "PENDIENTES" to "Pendientes",
                "PAGADOS" to "Pagados",
                "FINANCIACIONES" to "Deudas/Cuotas"
            ).forEach { (filterKey, filterLabel) ->
                val isSelected = selectedFilter == filterKey
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filterKey },
                    label = { Text(filterLabel, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FinanceTeal.copy(alpha = 0.15f),
                        selectedLabelColor = FinanceTeal
                    ),
                    modifier = Modifier.testTag("filter_chip_$filterKey")
                )
            }
        }

        // Bills List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = FinanceSlateLight.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                    Text("No hay gastos fijos en este filtro", style = MaterialTheme.typography.bodyMedium, color = FinanceSlateLight)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 340.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    FixedBillCard(
                        category = item,
                        currencySymbol = currencySymbol,
                        onTogglePaid = { viewModel.setGastoFijoPaid(item.id, !item.isPaid) },
                        onToggleCash = { viewModel.toggleCashPayment(item.id, !item.isCashPayment) },
                        onTogglePartner = { viewModel.toggleCategoryAssumedByPartner(item.id, !item.assumedByPartner) },
                        onToggleSkipped = { viewModel.toggleCategorySkippedThisMonth(item.id, !item.isSkippedThisMonth) },
                        onPayInstallment = { viewModel.payFinancingInstallment(item.id) },
                        onEdit = { editingCategory = item },
                        onDelete = { viewModel.deleteFixedBillCategory(item.id) },
                        onViewPlan = { viewingPlanCategory = item }
                    )
                }
            }
        }
    }

    // Dialog: Add Fixed Bill
    if (showAddDialog) {
        AddEditFixedBillDialog(
            category = null,
            currencySymbol = currencySymbol,
            onDismiss = { showAddDialog = false },
            onSave = { name, amount, isFinancing, months, payDay, isCash, billingCycle, totalInst, currentInst, startMs ->
                viewModel.addFixedBillDb(
                    name = name,
                    amount = amount,
                    isFinancing = isFinancing,
                    monthsRemaining = months,
                    payDay = payDay,
                    billingCycle = billingCycle,
                    isCashPayment = isCash,
                    totalInstallments = totalInst,
                    currentInstallment = currentInst,
                    financingStartDate = startMs
                )
                showAddDialog = false
            }
        )
    }

    // Dialog: Edit Fixed Bill
    editingCategory?.let { category ->
        AddEditFixedBillDialog(
            category = category,
            currencySymbol = currencySymbol,
            onDismiss = { editingCategory = null },
            onSave = { name, amount, isFinancing, months, payDay, isCash, billingCycle, totalInst, currentInst, startMs ->
                viewModel.updateFixedBillDb(
                    id = category.id,
                    name = name,
                    amount = amount,
                    isFinancing = isFinancing,
                    monthsRemaining = months,
                    payDay = payDay,
                    billingCycle = billingCycle,
                    isCashPayment = isCash,
                    totalInstallments = totalInst,
                    currentInstallment = currentInst,
                    financingStartDate = startMs
                )
                editingCategory = null
            }
        )
    }

    // Dialog: Payment Plan
    viewingPlanCategory?.let { category ->
        PaymentPlanDialog(
            category = category,
            currencySymbol = currencySymbol,
            onDismiss = { viewingPlanCategory = null }
        )
    }
}

@Composable
fun FixedBillCard(
    category: ExpenseCategory,
    currencySymbol: String,
    onTogglePaid: () -> Unit,
    onToggleCash: () -> Unit,
    onTogglePartner: () -> Unit,
    onToggleSkipped: () -> Unit,
    onPayInstallment: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewPlan: () -> Unit = {}
) {
    var expandedOptions by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fixed_bill_card_${category.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                category.isSkippedThisMonth -> Color(0xFFF1F5F9).copy(alpha = 0.7f)
                category.isPaid -> Color(0xFFF0FDF4)
                else -> Color.White
            }
        ),
        border = BorderStroke(
            1.dp,
            when {
                category.isSkippedThisMonth -> Color.LightGray.copy(alpha = 0.5f)
                category.isPaid -> Color(0xFFBBF7D0)
                else -> FinanceBorder
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Paid Checkbox
                Checkbox(
                    checked = category.isPaid,
                    onCheckedChange = { onTogglePaid() },
                    colors = CheckboxDefaults.colors(checkedColor = FinanceTeal),
                    modifier = Modifier.testTag("checkbox_paid_${category.id}")
                )

                Spacer(modifier = Modifier.width(6.dp))

                SubscriptionLogo(title = category.name, modifier = Modifier.size(24.dp))

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (category.isPaid) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (category.isPaid) FinanceSlateLight else FinanceSlateDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (category.payDay != null && category.payDay > 0) {
                            Text(
                                text = "Día ${category.payDay}",
                                style = MaterialTheme.typography.labelSmall,
                                color = FinanceTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (category.billingCycle != "Mensual") {
                            Text(
                                text = "🗓️ ${category.billingCycle}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF0284C7),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (category.isFinancing) {
                            val rem = category.remainingInstallments
                            val total = category.effectiveTotalInstallments
                            val current = category.effectiveCurrentInstallment
                            Text(
                                text = "Cuota $current/$total ($rem restantes)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF7C3AED),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (category.isCashPayment) {
                            Text("💵 Efectivo", style = MaterialTheme.typography.labelSmall, color = Color(0xFF16A34A))
                        }

                        if (category.assumedByPartner) {
                            Text("🤝 Pareja", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0284C7))
                        }

                        if (category.isSkippedThisMonth) {
                            Text("⏸️ Omitido", style = MaterialTheme.typography.labelSmall, color = Color(0xFFEA580C))
                        }
                    }

                    // Destacado de fecha de fin de la financiación
                    if (category.isFinancing) {
                        val endText = calculateFinancingEndDate(category)
                        if (endText.isNotBlank()) {
                            Surface(
                                color = Color(0xFF7C3AED).copy(alpha = 0.10f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = Color(0xFF7C3AED),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = endText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF7C3AED),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(category.limitAmount, currencySymbol),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (category.isPaid) Color(0xFF16A34A) else FinanceSlateDark
                    )
                    if (category.billingCycle != "Mensual" && category.rawAmount > 0.0) {
                        Text(
                            text = "(${formatCurrency(category.rawAmount, currencySymbol)} / recibo)",
                            style = MaterialTheme.typography.labelSmall,
                            color = FinanceSlateLight
                        )
                    }
                }

                IconButton(
                    onClick = { expandedOptions = !expandedOptions },
                    modifier = Modifier.size(32.dp).testTag("options_btn_${category.id}")
                ) {
                    Icon(
                        if (expandedOptions) Icons.Default.ExpandLess else Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = FinanceSlateLight
                    )
                }
            }

            // Expanded Actions Area
            AnimatedVisibility(visible = expandedOptions) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Toggle Cash
                            FilterChip(
                                selected = category.isCashPayment,
                                onClick = { onToggleCash() },
                                label = { Text("Efectivo", fontSize = 11.sp) }
                            )
                            // Toggle Partner
                            FilterChip(
                                selected = category.assumedByPartner,
                                onClick = { onTogglePartner() },
                                label = { Text("Asume Pareja", fontSize = 11.sp) }
                            )
                            // Toggle Skip
                            FilterChip(
                                selected = category.isSkippedThisMonth,
                                onClick = { onToggleSkipped() },
                                label = { Text("Omitir este mes", fontSize = 11.sp) }
                            )
                        }
                    }

                    if (category.isFinancing && category.remainingInstallments > 0) {
                        val total = category.effectiveTotalInstallments
                        val current = category.effectiveCurrentInstallment
                        val rem = category.remainingInstallments
                        val paidCount = category.paidInstallmentsCount
                        val progress = (paidCount.toFloat() / maxOf(total, 1).toFloat()).coerceIn(0f, 1f)
                        
                        Surface(
                            color = Color(0xFF7C3AED).copy(alpha = 0.07f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onViewPlan() }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Cuota $current de $total ($rem restantes)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF7C3AED)
                                    )
                                    Text(
                                        text = calculateFinancingEndDate(category),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF7C3AED)
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF7C3AED),
                                    trackColor = Color(0xFF7C3AED).copy(alpha = 0.20f)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "Ver Plan de Pago ($total cuotas) ➔",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF7C3AED),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { onPayInstallment() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Avanzar 1 Mes Pagado ($rem restantes)", fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onEdit() },
                            modifier = Modifier.testTag("edit_fixed_btn_${category.id}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = FinanceTeal)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar", color = FinanceTeal, fontSize = 12.sp)
                        }

                        TextButton(
                            onClick = { onDelete() },
                            modifier = Modifier.testTag("delete_fixed_btn_${category.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = CardBorderRed)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Eliminar", color = CardBorderRed, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditFixedBillDialog(
    category: ExpenseCategory?,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        amount: Double,
        isFinancing: Boolean,
        monthsRemaining: Int?,
        payDay: Int?,
        isCash: Boolean,
        billingCycle: String,
        totalInstallments: Int?,
        currentInstallment: Int?,
        financingStartDate: Long?
    ) -> Unit
) {
    var nameText by remember { mutableStateOf(category?.name ?: "") }
    val initialAmount = when {
        category == null -> ""
        category.rawAmount > 0.0 -> category.rawAmount.toString()
        category.limitAmount > 0.0 -> category.limitAmount.toString()
        else -> ""
    }
    var amountText by remember { mutableStateOf(initialAmount) }
    var billingCycle by remember { mutableStateOf(category?.billingCycle ?: "Mensual") }
    var payDayText by remember { mutableStateOf(category?.payDay?.toString() ?: "") }
    var isFinancing by remember { mutableStateOf(category?.isFinancing ?: false) }
    val initialTotal = when {
        category?.totalInstallments != null && category.totalInstallments > 0 -> category.totalInstallments
        category?.monthsRemaining != null && category.monthsRemaining > 0 -> {
            val curr = category.currentInstallment ?: 1
            val paid = if (category.isPaid) curr else (curr - 1).coerceAtLeast(0)
            paid + category.monthsRemaining
        }
        else -> 12
    }
    var totalMonthsText by remember { mutableStateOf(initialTotal.toString()) }
    var currentInstallmentText by remember { mutableStateOf((category?.effectiveCurrentInstallment ?: 1).toString()) }
    var isCash by remember { mutableStateOf(category?.isCashPayment ?: false) }

    val parsedAmount = amountText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val monthlyEquivalent = when (BillingFrequency.fromString(billingCycle)) {
        BillingFrequency.MENSUAL -> parsedAmount
        BillingFrequency.BIMENSUAL -> parsedAmount / 2.0
        BillingFrequency.TRIMESTRAL -> parsedAmount / 3.0
        BillingFrequency.ANUAL -> parsedAmount / 12.0
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = if (category == null) "Nuevo Gasto Fijo / Obligación" else "Editar Gasto Fijo",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = FinanceSlateDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Nombre del Gasto") },
                    placeholder = { Text("Ej: Klarna, Openbank, Seguro Coche") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_fixed_name_input")
                )

                // Frequency / Periodicity Selector
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Periodicidad del recibo",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = FinanceSlateDark
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Mensual", "Bimensual", "Trimestral", "Anual").forEach { freq ->
                            val isSelected = billingCycle.equals(freq, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { billingCycle = freq },
                                label = {
                                    Text(
                                        text = freq,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FinanceTeal.copy(alpha = 0.15f),
                                    selectedLabelColor = FinanceTeal
                                ),
                                modifier = Modifier.weight(1f).testTag("freq_chip_$freq")
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = {
                        Text(
                            if (billingCycle == "Mensual") "Importe Mensual ($currencySymbol)"
                            else "Importe por Recibo $billingCycle ($currencySymbol)"
                        )
                    },
                    placeholder = { Text("Ej: 120.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_fixed_amount_input")
                )

                if (billingCycle != "Mensual" && parsedAmount > 0.0) {
                    Surface(
                        color = FinanceTeal.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "💡 Impacto mensual calculado:",
                                style = MaterialTheme.typography.labelSmall,
                                color = FinanceTeal
                            )
                            Text(
                                text = "${formatCurrency(monthlyEquivalent, currencySymbol)} / mes",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = FinanceTeal
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = payDayText,
                    onValueChange = { payDayText = it },
                    label = { Text("Día de Pago (1 - 31) (Opcional)") },
                    placeholder = { Text("Ej: 5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_fixed_payday_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("¿Es una Financiación / Préstamo?", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isFinancing,
                        onCheckedChange = { isFinancing = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal)
                    )
                }

                AnimatedVisibility(visible = isFinancing) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = currentInstallmentText,
                                onValueChange = { currentInstallmentText = it },
                                label = { Text("Cuota Actual") },
                                placeholder = { Text("1") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("dialog_fixed_current_installment_input")
                            )
                            OutlinedTextField(
                                value = totalMonthsText,
                                onValueChange = { totalMonthsText = it },
                                label = { Text("Total Cuotas") },
                                placeholder = { Text("12") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("dialog_fixed_months_input")
                            )
                        }

                        val parsedTotal = (totalMonthsText.toIntOrNull() ?: 1).coerceAtLeast(1)
                        val parsedCurrent = (currentInstallmentText.toIntOrNull() ?: 1).coerceIn(1, parsedTotal)
                        val paidCount = if (category?.isPaid == true) parsedCurrent else (parsedCurrent - 1).coerceAtLeast(0)
                        val calculatedRemaining = (parsedTotal - paidCount).coerceAtLeast(0)
                        val calculatedStartMs = category?.financingStartDate ?: calculateEstimatedStartDateMs(parsedCurrent, payDayText.toIntOrNull())
                        val previewDate = calculateFinancingEndDate(
                            monthsRemaining = calculatedRemaining,
                            totalInstallments = parsedTotal,
                            currentInstallment = parsedCurrent,
                            startDateMs = calculatedStartMs,
                            isPaid = category?.isPaid ?: false
                        )

                        Surface(
                            color = Color(0xFF7C3AED).copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = Color(0xFF7C3AED),
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text(
                                        text = "Previsión: $paidCount pagadas • $calculatedRemaining restantes",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF7C3AED)
                                    )
                                    Text(
                                        text = previewDate,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF7C3AED)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("¿Se paga en Efectivo?", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isCash,
                        onCheckedChange = { isCash = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = FinanceTeal)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val payDay = payDayText.toIntOrNull()?.coerceIn(1, 31)
                    val parsedTotal = (totalMonthsText.toIntOrNull() ?: 1).coerceAtLeast(1)
                    val parsedCurrent = (currentInstallmentText.toIntOrNull() ?: 1).coerceIn(1, parsedTotal)
                    val paidCount = if (category?.isPaid == true) parsedCurrent else (parsedCurrent - 1).coerceAtLeast(0)
                    val calculatedRemaining = (parsedTotal - paidCount).coerceAtLeast(0)
                    val calculatedStartMs = category?.financingStartDate ?: calculateEstimatedStartDateMs(parsedCurrent, payDay)
                    if (nameText.isNotBlank() && amount > 0.0) {
                        onSave(
                            nameText.trim(),
                            amount,
                            isFinancing,
                            calculatedRemaining,
                            payDay,
                            isCash,
                            billingCycle,
                            parsedTotal,
                            parsedCurrent,
                            calculatedStartMs
                        )
                    }
                },
                enabled = nameText.isNotBlank() && (amountText.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0.0,
                colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                modifier = Modifier.testTag("dialog_fixed_save_btn")
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = FinanceSlateLight)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun PaymentPlanDialog(
    category: ExpenseCategory,
    currencySymbol: String,
    onDismiss: () -> Unit
) {
    val planItems = remember(category) { generateInstallmentPlan(category) }
    val total = category.effectiveTotalInstallments
    val rem = category.remainingInstallments
    val paid = category.paidInstallmentsCount
    val totalFinanced = category.limitAmount * total
    val paidAmount = category.limitAmount * paid
    val remainingAmount = category.limitAmount * rem

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Plan de Pago: ${category.name}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = FinanceSlateDark
                    )
                    Text(
                        text = "Total $total cuotas • $rem restantes",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF7C3AED),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = Color(0xFF7C3AED).copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Financiado:", style = MaterialTheme.typography.labelMedium, color = FinanceSlateLight)
                            Text(totalFinanced.formatCurrency(currencySymbol), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = FinanceSlateDark)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Amortizado ($paid cuotas):", style = MaterialTheme.typography.labelMedium, color = FinanceSlateLight)
                            Text(paidAmount.formatCurrency(currencySymbol), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF16A34A))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pendiente ($rem cuotas):", style = MaterialTheme.typography.labelMedium, color = FinanceSlateLight)
                            Text(remainingAmount.formatCurrency(currencySymbol), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF7C3AED))
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = Color(0xFF7C3AED).copy(alpha = 0.2f))
                        Text(
                            text = "🗓️ " + calculateFinancingEndDate(category),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF7C3AED)
                        )
                    }
                }

                Text(
                    text = "Desglose Cuota a Cuota ($total cuotas)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = FinanceSlateDark
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(planItems) { item ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                item.isPaid -> Color(0xFFDCFCE7).copy(alpha = 0.5f)
                                item.isCurrent -> Color(0xFFF3E8FF)
                                else -> Color(0xFFF8FAFC)
                            },
                            border = BorderStroke(
                                1.dp,
                                when {
                                    item.isPaid -> Color(0xFF22C55E).copy(alpha = 0.3f)
                                    item.isCurrent -> Color(0xFF7C3AED)
                                    else -> Color(0xFFE2E8F0)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Cuota ${item.installmentNumber} de ${item.totalInstallments}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (item.isCurrent) Color(0xFF7C3AED) else FinanceSlateDark
                                    )
                                    Text(
                                        text = item.dateLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FinanceSlateLight,
                                        fontSize = 11.sp
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = item.amount.formatCurrency(currencySymbol),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = FinanceSlateDark
                                    )
                                    when {
                                        item.isPaid -> {
                                            Surface(
                                                color = Color(0xFF22C55E).copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "✓ Pagada",
                                                    color = Color(0xFF16A34A),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                        item.isCurrent -> {
                                            Surface(
                                                color = Color(0xFF7C3AED).copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = if (category.isPaid) "✓ Pagada" else "● Activa",
                                                    color = if (category.isPaid) Color(0xFF16A34A) else Color(0xFF7C3AED),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                        else -> {
                                            Text(
                                                text = "Pendiente",
                                                color = FinanceSlateLight,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 10.sp
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
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal)
            ) {
                Text("Entendido", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}