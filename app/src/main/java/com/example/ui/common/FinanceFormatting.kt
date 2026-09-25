package com.example.ui.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
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

fun Double.formatCurrency(symbol: String = currentCurrencySymbol): String {
    return try {
        val esLocale = Locale.Builder().setLanguage("es").setRegion("ES").build()
        val symbols = DecimalFormatSymbols(esLocale)
        val formatter = DecimalFormat("#,##0.00", symbols)
        val formatted = formatter.format(this)
        when (symbol) {
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
            else -> "$formatted $symbol"
        }
    } catch (e: Exception) {
        val formattedString = String.format(Locale.getDefault(), "%.2f", this)
        "$formattedString $symbol"
    }
}

@JvmName("formatCurrencyDirect")
fun formatCurrency(amount: Double, symbol: String = currentCurrencySymbol): String {
    return amount.formatCurrency(symbol)
}

enum class SettingDialogType {
    NONE,
    SETTINGS,
    PROFILE,
    WIZARD_FIXED,
    PREFERENCES,
    DATABASE,
    BACKUP
}

fun sendFeedback(context: android.content.Context) {
    try {
        val emailIntent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:villatorovictor58@gmail.com")
            putExtra(android.content.Intent.EXTRA_SUBJECT, "[FinanceFlow] Reporte de Error o Sugerencia")
            putExtra(
                android.content.Intent.EXTRA_TEXT,
                "Hola Victor,\n\nEscribe aquí tu duda, sugerencia o detalle del fallo:\n\n---\nDispositivo: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}\nAndroid: ${android.os.Build.VERSION.RELEASE}\nVersión de App: ${com.example.BuildConfig.VERSION_NAME}\n"
            )
        }
        context.startActivity(android.content.Intent.createChooser(emailIntent, "Enviar reporte vía Email"))
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "No se encontró una aplicación de correo electrónico configurada", android.widget.Toast.LENGTH_SHORT).show()
    }
}

/**
 * Calcula y formatea la fecha en que concluye una financiación o préstamo (ej: "Finaliza en Noviembre de 2026").
 * Soporta cálculo mediante java.time.YearMonth y java.util.Calendar para máxima compatibilidad.
 * Regla: La fecha de fin es startDate.plusMonths(totalInstallments - 1)
 */
fun calculateFinancingEndDate(
    monthsRemaining: Int?,
    totalInstallments: Int? = null,
    currentInstallment: Int? = null,
    startDateMs: Long? = null,
    isPaid: Boolean = false
): String {
    if (monthsRemaining == null && totalInstallments == null) return ""
    if (monthsRemaining != null && monthsRemaining <= 0) return "Finalizado"

    val localeEs = Locale.Builder().setLanguage("es").setRegion("ES").build()

    return try {
        val total = if (totalInstallments != null && totalInstallments > 0) totalInstallments else (monthsRemaining ?: 1)
        val current = (currentInstallment ?: 1).coerceIn(1, maxOf(total, 1))

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val endYearMonth = if (startDateMs != null && startDateMs > 0) {
                val instant = java.time.Instant.ofEpochMilli(startDateMs)
                val startYm = java.time.YearMonth.from(instant.atZone(java.time.ZoneId.systemDefault()))
                
                startYm.plusMonths((total - 1).coerceAtLeast(0).toLong())
            } else {
                val currentYm = java.time.YearMonth.now()
                
                
                val monthsAhead = (total - current).coerceAtLeast(0).toLong()
                currentYm.plusMonths(monthsAhead)
            }
            val monthName = endYearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, localeEs)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeEs) else it.toString() }
            "Finaliza en $monthName de ${endYearMonth.year}"
        } else {
            val cal = java.util.Calendar.getInstance()
            if (startDateMs != null && startDateMs > 0) {
                cal.timeInMillis = startDateMs
                cal.add(java.util.Calendar.MONTH, (total - 1).coerceAtLeast(0))
            } else {
                val monthsAhead = (total - current).coerceAtLeast(0)
                cal.add(java.util.Calendar.MONTH, monthsAhead)
            }
            val monthName = cal.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.LONG, localeEs)
                ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeEs) else it.toString() } ?: ""
            "Finaliza en $monthName de ${cal.get(java.util.Calendar.YEAR)}"
        }
    } catch (e: Exception) {
        val cal = java.util.Calendar.getInstance()
        val total = if (totalInstallments != null && totalInstallments > 0) totalInstallments else (monthsRemaining ?: 1)
        val current = (currentInstallment ?: 1).coerceIn(1, maxOf(total, 1))
        cal.add(java.util.Calendar.MONTH, (total - current).coerceAtLeast(0))
        val monthName = cal.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.LONG, localeEs)
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeEs) else it.toString() } ?: ""
        "Finaliza en $monthName de ${cal.get(java.util.Calendar.YEAR)}"
    }
}

fun calculateFinancingEndDate(category: com.example.data.ExpenseCategory): String {
    if (!category.isFinancing) return ""
    return calculateFinancingEndDate(
        monthsRemaining = category.remainingInstallments,
        totalInstallments = category.effectiveTotalInstallments,
        currentInstallment = category.effectiveCurrentInstallment,
        startDateMs = category.financingStartDate,
        isPaid = category.isPaid
    )
}

fun calculateEstimatedStartDateMs(currentInstallment: Int, payDay: Int? = null): Long {
    val cal = java.util.Calendar.getInstance()
    if (payDay != null && payDay in 1..31) {
        cal.set(java.util.Calendar.DAY_OF_MONTH, payDay.coerceIn(1, 28))
    }
    cal.add(java.util.Calendar.MONTH, -(currentInstallment - 1).coerceAtLeast(0))
    return cal.timeInMillis
}

data class InstallmentPlanItem(
    val installmentNumber: Int,
    val totalInstallments: Int,
    val dateLabel: String,
    val amount: Double,
    val isPaid: Boolean,
    val isCurrent: Boolean
)

fun generateInstallmentPlan(category: com.example.data.ExpenseCategory): List<InstallmentPlanItem> {
    val total = category.effectiveTotalInstallments
    val current = category.effectiveCurrentInstallment
    val localeEs = Locale.Builder().setLanguage("es").setRegion("ES").build()

    val payDay = category.payDay ?: 1
    val startCal = java.util.Calendar.getInstance().apply {
        if (category.financingStartDate != null && category.financingStartDate > 0) {
            timeInMillis = category.financingStartDate
        } else {
            set(java.util.Calendar.DAY_OF_MONTH, payDay.coerceIn(1, 28))
            add(java.util.Calendar.MONTH, -(current - 1).coerceAtLeast(0))
        }
    }

    return (1..total).map { num ->
        val itemCal = (startCal.clone() as java.util.Calendar).apply {
            add(java.util.Calendar.MONTH, num - 1)
        }
        val dayFormatted = String.format(localeEs, "%02d", itemCal.get(java.util.Calendar.DAY_OF_MONTH))
        val monthName = itemCal.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.LONG, localeEs)
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(localeEs) else it.toString() } ?: ""
        val year = itemCal.get(java.util.Calendar.YEAR)
        val dateLabel = "$dayFormatted de $monthName de $year"

        val isItemPaid = if (num < current) {
            true
        } else if (num == current) {
            category.isPaid
        } else {
            false
        }

        InstallmentPlanItem(
            installmentNumber = num,
            totalInstallments = total,
            dateLabel = dateLabel,
            amount = category.limitAmount,
            isPaid = isItemPaid,
            isCurrent = (num == current)
        )
    }
}


