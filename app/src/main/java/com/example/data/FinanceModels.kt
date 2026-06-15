package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val selectedIcon: String = "trending",
    val isProUser: Boolean = false,
    val selectedCurrency: String = "€"
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
    val monthsRemaining: Int? = null // Number of months remaining for financing
)

@Entity(tableName = "variable_expenses")
data class VariableExpenseEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: String, // references ExpenseCategory ID, e.g., "comida"
    val categoryName: String, // Display name e.g., "Comida"
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis()
)
