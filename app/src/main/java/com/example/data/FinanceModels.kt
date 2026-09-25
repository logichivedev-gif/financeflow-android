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
    val partnerContribution: Double = 0.0, 
    val waterBilling: String = "Mensual", 
    val waterCost: Double = 40.0,
    val isWaterAdded: Boolean = false,
    val electricityBilling: String = "Mensual", 
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
    @PrimaryKey val id: String, 
    val name: String,
    val limitAmount: Double, 
    val isFixed: Boolean, 
    val isPaid: Boolean = false, 
    val isFromWizardExtra: Boolean = false, 
    val billingCycle: String = "Mensual", 
    val rawAmount: Double = 0.0, 
    val isAdded: Boolean = true, 
    val payDay: Int? = null, 
    val isVariableBill: Boolean = false, 
    val assumedByPartner: Boolean = false, 
    val isFinancing: Boolean = false, 
    val monthsRemaining: Int? = null, 
    val totalInstallments: Int? = null, 
    val currentInstallment: Int? = null, 
    val financingStartDate: Long? = null, 
    val isSkippedThisMonth: Boolean = false, 
    val isInsurance: Boolean = false, 
    val isCashPayment: Boolean = false, 
    val isArchived: Boolean = false 
) {
    val isInsurancePolicy: Boolean
        get() = isInsurance

    val effectiveTotalInstallments: Int
        get() {
            if (totalInstallments != null && totalInstallments > 0) return totalInstallments
            val rem = monthsRemaining ?: 0
            val curr = currentInstallment ?: 1
            val paid = if (isPaid) curr else (curr - 1).coerceAtLeast(0)
            val deduced = paid + rem
            return if (deduced > 0) deduced else if (rem > 0) rem else 1
        }

    val paidInstallmentsCount: Int
        get() {
            val total = effectiveTotalInstallments
            if (monthsRemaining != null) {
                return (total - monthsRemaining.coerceIn(0, total)).coerceIn(0, total)
            }
            val curr = currentInstallment ?: 1
            return if (isPaid) curr.coerceIn(0, total) else (curr - 1).coerceIn(0, total)
        }

    val effectiveCurrentInstallment: Int
        get() {
            val total = effectiveTotalInstallments
            if (total <= 0) return 1
            if (currentInstallment != null && currentInstallment > 0) {
                
                val rem = monthsRemaining
                if (rem != null && currentInstallment == 1 && total > 2 && rem < total) {
                    val activeIndex = (total - rem + (if (isPaid) 0 else 1)).coerceIn(1, total)
                    return activeIndex
                }
                return currentInstallment.coerceIn(1, total)
            }
            val rem = monthsRemaining ?: total
            val activeIndex = (total - rem + (if (isPaid) 0 else 1)).coerceIn(1, total)
            return activeIndex
        }

    val remainingInstallments: Int
        get() {
            val total = effectiveTotalInstallments
            val paid = paidInstallmentsCount
            return (total - paid).coerceAtLeast(0)
        }
}

@Entity(tableName = "variable_expenses")
data class VariableExpenseEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: String, 
    val categoryName: String, 
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis()
)
