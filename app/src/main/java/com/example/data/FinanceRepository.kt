package com.example.data

import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val financeDao: FinanceDao) {
    val financialProfile: Flow<FinancialProfile?> = financeDao.getFinancialProfileFlow()
    val activeCategories: Flow<List<ExpenseCategory>> = financeDao.getActiveCategoriesFlow()
    val variableExpenses: Flow<List<VariableExpenseEntry>> = financeDao.getVariableExpensesFlow()

    suspend fun getProfileDirect(): FinancialProfile? = financeDao.getFinancialProfileDirect()
    suspend fun getAllCategoriesDirect(): List<ExpenseCategory> = financeDao.getAllCategoriesDirect()

    suspend fun saveFinancialProfile(profile: FinancialProfile) {
        financeDao.insertFinancialProfile(profile)
    }

    suspend fun saveCategories(categories: List<ExpenseCategory>) {
        financeDao.insertExpenseCategories(categories)
    }

    suspend fun saveCategory(category: ExpenseCategory) {
        financeDao.insertExpenseCategory(category)
    }

    suspend fun updateCategory(category: ExpenseCategory) {
        financeDao.updateExpenseCategory(category)
    }

    suspend fun setCategoryPaid(id: String, isPaid: Boolean) {
        financeDao.updateCategoryPaidStatus(id, isPaid)
    }

    suspend fun deleteCategory(id: String) {
        financeDao.deleteCategoryById(id)
    }

    suspend fun addVariableExpense(expense: VariableExpenseEntry) {
        financeDao.insertVariableExpense(expense)
    }

    suspend fun deleteVariableExpense(id: Int) {
        financeDao.deleteVariableExpenseById(id)
    }

    suspend fun clearVariableExpensesOnly() {
        financeDao.clearVariableExpenses()
    }

    suspend fun resetAllData() {
        financeDao.clearFinancialProfile()
        financeDao.clearExpenseCategories()
        financeDao.clearVariableExpenses()
    }
}
