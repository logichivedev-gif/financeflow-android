package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BillingFrequency {
    MENSUAL,
    BIMENSUAL,
    TRIMESTRAL,
    ANUAL;

    override fun toString(): String {
        return when (this) {
            MENSUAL -> "Mensual"
            BIMENSUAL -> "Bimensual"
            TRIMESTRAL -> "Trimestral"
            ANUAL -> "Anual"
        }
    }

    companion object {
        fun fromString(value: String): BillingFrequency {
            return when (value.uppercase()) {
                "MENSUAL" -> MENSUAL
                "BIMENSUAL" -> BIMENSUAL
                "TRIMESTRAL" -> TRIMESTRAL
                "ANUAL" -> ANUAL
                else -> {
                    when (value) {
                        "Mensual" -> MENSUAL
                        "Bimensual" -> BIMENSUAL
                        "Trimestral" -> TRIMESTRAL
                        "Anual" -> ANUAL
                        else -> MENSUAL
                    }
                }
            }
        }
    }
}

@Entity(tableName = "financial_profile")
data class FinancialProfile(
    @PrimaryKey val id: Int = 1,
    val monthlyIncome: Double = 0.0,
    val isWizardComplete: Boolean = false,
    val hasPets: Boolean = false,
    val petsCost: Double = 30.0,
    val hasKids: Boolean = false,
    val kidsCost: Double = 100.0,
    val sharedExpenses: Boolean = false,
    val partnerContribution: Double = 0.0, // fixed monthly partner contribution cash
    val waterBilling: String = "Mensual", // "Mensual" or "Bimensual"
    val waterCost: Double = 40.0,
    val isWaterAdded: Boolean = false,
    val electricityBilling: String = "Mensual", // "Mensual" or "Bimensual"
    val electricityCost: Double = 60.0,
    val isElectricityAdded: Boolean = false,
    val currentBankBalance: Double = -1.0,
    val lastActiveYear: Int = 0,
    val lastActiveMonth: Int = 0,
    val incomeDay: Int = 1,
    val hideNewMonthBanner: Boolean = false,
    val hideSmartCalendarBanner: Boolean = false,
    val selectedTheme: String = "azul",
    val themeMode: String = "system",
    val selectedIcon: String = "trending",
    val isProUser: Boolean = false,
    val selectedCurrency: String = "€",
    val isBiometricEnabled: Boolean = false,
    val isPaymentNotificationsEnabled: Boolean = true,
    val paymentNotificationHoursLead: Int = 48,
    val customCycleStartDay: Int = 1,
    val isHighPerformanceMode: Boolean = false,
    val userName: String = "Usuario",
    val avatarId: String = "avatar_1",
    val customAvatarUri: String = "",
    val isBankNotificationInterceptorEnabled: Boolean = false
)

@Entity(tableName = "expense_categories")
data class ExpenseCategory(
    @PrimaryKey val id: String, // unique id, e.g. "alquiler", "gimnasio", "luz", etc.
    val name: String,
    val limitAmount: Double, // calculated monthly limit or monthly equivalent cost
    val isFixed: Boolean, // true for fixed expenses (facturas), false for variable budgets
    val isPaid: Boolean = false, // true if marked paid for fixed expenses
    val isFromWizardExtra: Boolean = false, // if added in step 4 extras
    val billingCycle: String = "Mensual", // "Mensual" or "Bimensual" for supplies
    val rawAmount: Double = 0.0, // original user input amount
    val isAdded: Boolean = true, // true if active
    val payDay: Int? = null, // Set payDay (day of month, e.g. 5, 10, etc.)
    val isVariableBill: Boolean = false, // If this fixed expense changes details/amount each month (like luz, agua, etc.)
    val assumedByPartner: Boolean = false, // If the partner pays this bill at 100% and it shouldn't subtract from user's budget
    val isFinancing: Boolean = false, // If it is a financing/loan/installment
    val monthsRemaining: Int? = null, // Number of months remaining for financing
    val totalInstallments: Int? = null, // Total de cuotas (ej: 12, 24, 36)
    val currentInstallment: Int? = null, // Cuota actual (ej: 1, 5, etc.)
    val financingStartDate: Long? = null, // Fecha de inicio de la financiación (timestamp ms)
    val isSkippedThisMonth: Boolean = false, // If a bimonthly/quarterly expense is skipped or not due this month
    val isInsurance: Boolean = false, // If it is an insurance policy (coche, hogar, salud, vida, etc.)
    val isCashPayment: Boolean = false, // If paid in cash / non-bank payment
    val isArchived: Boolean = false // If marked as archived for fixed expenses history
) {
    val isInsurancePolicy: Boolean
        get() = isInsurance

    val effectiveTotalInstallments: Int
        get() = totalInstallments ?: monthsRemaining ?: 1

    val effectiveCurrentInstallment: Int
        get() {
            if (currentInstallment != null && currentInstallment > 0) return currentInstallment
            val total = effectiveTotalInstallments
            val rem = monthsRemaining ?: total
            return (total - rem + 1).coerceIn(1, total)
        }
}

@Entity(tableName = "variable_expenses")
data class VariableExpenseEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: String, // references ExpenseCategory ID, e.g., "comida"
    val categoryName: String, // Display name e.g., "Comida"
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis()
)
