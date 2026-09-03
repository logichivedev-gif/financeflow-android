package com.example.ui.screens.variable

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ExpenseCategory
import com.example.data.VariableExpenseEntry
import com.example.ui.FinanceViewModel
import com.example.ui.common.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VariableSpentPane(
    viewModel: FinanceViewModel,
    dbCategories: List<ExpenseCategory>,
    variableExpenses: List<VariableExpenseEntry>,
    totalSpent: Double,
    windowWidthSizeClass: WindowWidthSizeClass
) {
    val dbProfile by viewModel.dbProfile.collectAsStateWithLifecycle()
    val currencySymbol = dbProfile?.selectedCurrency ?: "€"

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var selectedCategoryForQuickAdd by remember { mutableStateOf<String?>(null) }

    val variableCategories = dbCategories.filter { !it.isFixed && !it.isArchived && it.isAdded }
    val totalBudget = variableCategories.sumOf { it.limitAmount }
    val remainingBudget = maxOf(0.0, totalBudget - totalSpent)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Budget Overview Card
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
                            text = "Presupuestos y Gastos Variables",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = FinanceSlateDark
                        )
                        Text(
                            text = "${variableCategories.size} categorías activas",
                            style = MaterialTheme.typography.bodySmall,
                            color = FinanceSlateLight
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { showAddExpenseDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("btn_quick_add_expense")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Anotar Gasto", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = { showAddCategoryDialog = true },
                            modifier = Modifier.testTag("btn_add_category")
                        ) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = "Nueva Categoría", tint = FinanceTeal)
                        }
                    }
                }

                Divider(color = FinanceBorder.copy(alpha = 0.6f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Presupuesto Total", style = MaterialTheme.typography.labelSmall, color = FinanceSlateLight)
                        Text(
                            text = formatCurrency(totalBudget, currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = FinanceSlateDark
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Gastado", style = MaterialTheme.typography.labelSmall, color = FinanceSlateLight)
                        Text(
                            text = formatCurrency(totalSpent, currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (totalSpent > totalBudget && totalBudget > 0) CardBorderRed else FinanceSlateDark
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Disponible", style = MaterialTheme.typography.labelSmall, color = FinanceSlateLight)
                        Text(
                            text = formatCurrency(remainingBudget, currencySymbol),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (remainingBudget > 0) Color(0xFF16A34A) else CardBorderRed
                        )
                    }
                }
            }
        }

        // Categories with Progress
        Text(
            text = "Categorías de Gasto Cotidiano",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = FinanceSlateDark
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 340.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (variableCategories.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "No tienes categorías variables activas. Pulsa '+ Nueva Categoría' para empezar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FinanceSlateLight,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(variableCategories, key = { it.id }) { cat ->
                    val spentInCat = variableExpenses.filter { it.categoryName.equals(cat.name, ignoreCase = true) }.sumOf { it.amount }
                    val progress = if (cat.limitAmount > 0) (spentInCat / cat.limitAmount).toFloat().coerceIn(0f, 1f) else 0f
                    val isExceeded = cat.limitAmount > 0 && spentInCat > cat.limitAmount

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("var_cat_card_${cat.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, if (isExceeded) CardBorderRed.copy(alpha = 0.5f) else FinanceBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(FinanceTeal.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = FinanceTeal, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = cat.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = FinanceSlateDark
                                        )
                                        Text(
                                            text = "${formatCurrency(spentInCat, currencySymbol)} de ${formatCurrency(cat.limitAmount, currencySymbol)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isExceeded) CardBorderRed else FinanceSlateLight
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            selectedCategoryForQuickAdd = cat.name
                                            showAddExpenseDialog = true
                                        },
                                        modifier = Modifier.size(32.dp).testTag("quick_add_cat_${cat.id}")
                                    ) {
                                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Anotar aquí", tint = FinanceTeal, modifier = Modifier.size(20.dp))
                                    }

                                    IconButton(
                                        onClick = { editingCategory = cat },
                                        modifier = Modifier.size(32.dp).testTag("edit_cat_${cat.id}")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = FinanceSlateLight, modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteVariableBudgetDb(cat.id) },
                                        modifier = Modifier.size(32.dp).testTag("delete_cat_${cat.id}")
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = CardBorderRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            // Progress Bar
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (isExceeded) CardBorderRed else FinanceTeal,
                                trackColor = Color(0xFFF1F5F9)
                            )
                        }
                    }
                }
            }

            // Recent Expenses Section
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Últimos Gastos Registrados (${variableExpenses.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = FinanceSlateDark
                    )
                }
            }

            if (variableExpenses.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "No has anotado ningún gasto este mes todavía.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FinanceSlateLight,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(variableExpenses.sortedByDescending { it.timestamp }, key = { it.id }) { entry ->
                    val dateFormat = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }
                    val dateStr = remember(entry.timestamp) { dateFormat.format(Date(entry.timestamp)) }

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("expense_entry_${entry.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.categoryName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = FinanceSlateDark
                                )
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FinanceSlateLight
                                )
                            }

                            Text(
                                text = "- ${formatCurrency(entry.amount, currencySymbol)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = CardBorderRed
                            )

                            IconButton(
                                onClick = { viewModel.deleteVariableSpentEntry(entry.id) },
                                modifier = Modifier.size(32.dp).testTag("delete_expense_${entry.id}")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Eliminar Gasto", tint = FinanceSlateLight, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Record Expense
    if (showAddExpenseDialog) {
        AddExpenseEntryDialog(
            categories = variableCategories,
            initialCategory = selectedCategoryForQuickAdd,
            currencySymbol = currencySymbol,
            onDismiss = {
                showAddExpenseDialog = false
                selectedCategoryForQuickAdd = null
            },
            onSave = { catName, amount ->
                viewModel.recordVariableExpense(catName, amount)
                showAddExpenseDialog = false
                selectedCategoryForQuickAdd = null
            }
        )
    }

    // Dialog: Add Variable Category
    if (showAddCategoryDialog) {
        AddEditVariableCategoryDialog(
            category = null,
            currencySymbol = currencySymbol,
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name, limit ->
                viewModel.addVariableBudgetDb(name, limit)
                showAddCategoryDialog = false
            }
        )
    }

    // Dialog: Edit Variable Category
    editingCategory?.let { category ->
        AddEditVariableCategoryDialog(
            category = category,
            currencySymbol = currencySymbol,
            onDismiss = { editingCategory = null },
            onSave = { name, limit ->
                viewModel.updateVariableBudgetDb(category.id, name, limit)
                editingCategory = null
            }
        )
    }
}

@Composable
fun AddExpenseEntryDialog(
    categories: List<ExpenseCategory>,
    initialCategory: String?,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (categoryName: String, amount: Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedCategoryName by remember {
        mutableStateOf(initialCategory ?: categories.firstOrNull()?.name ?: "Comida")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "Anotar Gasto Variable",
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
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Importe del Gasto ($currencySymbol)") },
                    placeholder = { Text("Ej: 15.50") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_expense_amount_input")
                )

                Text("Selecciona la Categoría:", style = MaterialTheme.typography.bodySmall, color = FinanceSlateLight)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val displayCats = if (categories.isNotEmpty()) categories.map { it.name } else listOf("Comida", "Transporte", "Ocio")
                    displayCats.take(4).forEach { catName ->
                        val isSelected = selectedCategoryName == catName
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryName = catName },
                            label = { Text(catName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FinanceTeal.copy(alpha = 0.15f),
                                selectedLabelColor = FinanceTeal
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.replace(',', '.').toDoubleOrNull() ?: 0.0
                    if (amount > 0.0) {
                        onSave(selectedCategoryName, amount)
                    }
                },
                enabled = (amountText.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0.0,
                colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                modifier = Modifier.testTag("dialog_expense_save_btn")
            ) {
                Text("Guardar Gasto", fontWeight = FontWeight.Bold)
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
fun AddEditVariableCategoryDialog(
    category: ExpenseCategory?,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (name: String, limit: Double) -> Unit
) {
    var nameText by remember { mutableStateOf(category?.name ?: "") }
    var limitText by remember { mutableStateOf(category?.limitAmount?.let { if (it > 0) it.toString() else "" } ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = if (category == null) "Nueva Categoría Variable" else "Editar Presupuesto",
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
                    label = { Text("Nombre de la Categoría") },
                    placeholder = { Text("Ej: Gasolina, Supermercado") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_var_cat_name_input")
                )

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Límite Mensual Deseado ($currencySymbol)") },
                    placeholder = { Text("Ej: 200.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("dialog_var_cat_limit_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitText.replace(',', '.').toDoubleOrNull() ?: 0.0
                    if (nameText.isNotBlank() && limit > 0.0) {
                        onSave(nameText.trim(), limit)
                    }
                },
                enabled = nameText.isNotBlank() && (limitText.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0.0,
                colors = ButtonDefaults.buttonColors(containerColor = FinanceTeal),
                modifier = Modifier.testTag("dialog_var_cat_save_btn")
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
