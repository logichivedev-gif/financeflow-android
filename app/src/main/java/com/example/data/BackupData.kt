package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupData(
    val profile: FinancialProfile?,
    val categories: List<ExpenseCategory>,
    val expenses: List<VariableExpenseEntry>
)
