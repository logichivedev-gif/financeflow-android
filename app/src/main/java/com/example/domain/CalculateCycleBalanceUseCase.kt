package com.example.domain

import com.example.data.ExpenseCategory
import com.example.data.FinancialProfile
import java.time.LocalDate
import java.util.Calendar

/**
 * Caso de Uso de Dominio cerrado y aislado para el cálculo de saldo y compromisos
 * por ciclo dinámico de cobro (Día_Actual -> Día_Cobro - 1).
 *
 * El motor matemático central está protegido con @CriticalFinancialEngine para evitar
 * modificaciones inadvertidas en capas superiores de UI o ViewModel.
 */
class CalculateCycleBalanceUseCase {

    @CriticalFinancialEngine
    operator fun invoke(
        currentBank: Double,
        profile: FinancialProfile,
        categories: List<ExpenseCategory>,
        currentDate: Calendar = Calendar.getInstance()
    ): Double {
        val pendingFixed = getPendingExpensesForCycle(categories, profile.incomeDay, currentDate)

        val baseMoney = if (profile.currentBankBalance != -1.0 && profile.currentBankBalance >= 0.0) {
            currentBank
        } else {
            profile.monthlyIncome + profile.partnerContribution
        }

        return baseMoney - pendingFixed
    }

    @CriticalFinancialEngine
    fun isCategoryPendingInCycle(
        category: ExpenseCategory,
        incomeDay: Int,
        currentDate: Calendar = Calendar.getInstance()
    ): Boolean {
        if (category.isArchived) return false
        if (!category.isFixed && !category.isFinancing) return false
        if (category.assumedByPartner) return false
        if (category.isSkippedThisMonth) return false

        val validIncomeDay = if (incomeDay in 1..31) incomeDay else 1

        val todayCal = (currentDate.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val currentDay = todayCal.get(Calendar.DAY_OF_MONTH)

        val nextIncomeCal = (todayCal.clone() as Calendar).apply {
            if (currentDay >= validIncomeDay) {
                add(Calendar.MONTH, 1)
            }
            val maxDays = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, minOf(validIncomeDay, maxDays))
        }

        val cycleEndCal = (nextIncomeCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }

        val payDay = category.payDay
        if (payDay == null || payDay <= 0) {
            return !category.isPaid
        }

        val nextExpenseCal = if (payDay >= currentDay) {
            if (!category.isPaid) {
                (todayCal.clone() as Calendar).apply {
                    val maxDays = getActualMaximum(Calendar.DAY_OF_MONTH)
                    set(Calendar.DAY_OF_MONTH, minOf(payDay, maxDays))
                }
            } else {
                null
            }
        } else {
            (todayCal.clone() as Calendar).apply {
                add(Calendar.MONTH, 1)
                val maxDays = getActualMaximum(Calendar.DAY_OF_MONTH)
                set(Calendar.DAY_OF_MONTH, minOf(payDay, maxDays))
            }
        }

        return nextExpenseCal != null && !nextExpenseCal.after(cycleEndCal)
    }

    @CriticalFinancialEngine
    fun getPendingCategoriesForCycle(
        categories: List<ExpenseCategory>,
        incomeDay: Int,
        currentDate: Calendar = Calendar.getInstance()
    ): List<ExpenseCategory> {
        return categories.filter { isCategoryPendingInCycle(it, incomeDay, currentDate) }
    }

    @CriticalFinancialEngine
    fun getPendingExpensesForCycle(
        dbCategories: List<ExpenseCategory>,
        incomeDay: Int,
        currentDate: Calendar = Calendar.getInstance()
    ): Double {
        return getPendingCategoriesForCycle(dbCategories, incomeDay, currentDate).sumOf { category ->
            if (category.rawAmount > 0.0 && category.billingCycle != "Mensual") category.rawAmount else category.limitAmount
        }
    }

    @CriticalFinancialEngine
    fun getPendingExpensesForCycle(
        dbCategories: List<ExpenseCategory>,
        incomeDay: Int,
        currentDate: LocalDate
    ): Double {
        val cal = Calendar.getInstance().apply {
            set(currentDate.year, currentDate.monthValue - 1, currentDate.dayOfMonth, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return getPendingExpensesForCycle(dbCategories, incomeDay, cal)
    }

    @CriticalFinancialEngine
    fun isBillInCycleWindow(
        payDay: Int?,
        incomeDay: Int,
        currentDate: Calendar = Calendar.getInstance()
    ): Boolean {
        if (payDay == null || payDay <= 0) return true
        val validIncomeDay = if (incomeDay in 1..31) incomeDay else 1

        val todayCal = (currentDate.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

        val nextIncomeCal = (todayCal.clone() as Calendar).apply {
            if (todayDay >= validIncomeDay) {
                add(Calendar.MONTH, 1)
            }
            val maxDays = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, minOf(validIncomeDay, maxDays))
        }

        val cycleEndCal = (nextIncomeCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }

        val billCalCurrent = (todayCal.clone() as Calendar).apply {
            val maxDays = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, minOf(payDay, maxDays))
        }
        if (!billCalCurrent.before(todayCal) && !billCalCurrent.after(cycleEndCal)) {
            return true
        }

        if (cycleEndCal.get(Calendar.MONTH) != todayCal.get(Calendar.MONTH) ||
            cycleEndCal.get(Calendar.YEAR) != todayCal.get(Calendar.YEAR)
        ) {
            val billCalNext = (cycleEndCal.clone() as Calendar).apply {
                val maxDays = getActualMaximum(Calendar.DAY_OF_MONTH)
                set(Calendar.DAY_OF_MONTH, minOf(payDay, maxDays))
            }
            if (!billCalNext.before(todayCal) && !billCalNext.after(cycleEndCal)) {
                return true
            }
        }

        return false
    }

    companion object {
        val INSTANCE = CalculateCycleBalanceUseCase()
    }
}
