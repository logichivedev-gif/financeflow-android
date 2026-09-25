package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.FinanceDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FinanceTransactionsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val appContext = context.applicationContext
        val db = FinanceDatabase.getDatabase(appContext)

        // Run on background IO thread to query database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val profile = db.financeDao().getFinancialProfileDirect()
                val categories = db.financeDao().getAllCategoriesDirect()
                val variableExpenses = db.financeDao().getVariableExpensesDirect()

                // Calculate available balance
                val currency = profile?.selectedCurrency ?: "€"
                val baseIncome = (profile?.monthlyIncome ?: 0.0) + (profile?.partnerContribution ?: 0.0)
                val isCustomBankBalance = (profile?.currentBankBalance ?: -1.0) > 0.0
                val baseValue = if (isCustomBankBalance) (profile?.currentBankBalance ?: 0.0) else baseIncome
                val incomeDay = profile?.incomeDay ?: 1
                val pendingFixed = com.example.ui.FinanceViewModel.getPendingExpensesForCycle(categories, incomeDay)

                val saldoRestante = baseValue - pendingFixed

                // Format amount
                val balanceText = WidgetUpdateHelper.formatWidgetCurrency(saldoRestante, currency)

                // Update views
                withContext(Dispatchers.Main) {
                    for (appWidgetId in appWidgetIds) {
                        val views = RemoteViews(appContext.packageName, R.layout.widget_transactions_layout)

                        // Set Balance
                        views.setTextViewText(R.id.widget_tx_balance, balanceText)

                        // Set Transactions rows
                        if (variableExpenses.isEmpty()) {
                            views.setViewVisibility(R.id.widget_tx_empty, View.VISIBLE)
                            views.setViewVisibility(R.id.widget_tx_row_1, View.GONE)
                            views.setViewVisibility(R.id.widget_tx_row_2, View.GONE)
                            views.setViewVisibility(R.id.widget_tx_row_3, View.GONE)
                        } else {
                            views.setViewVisibility(R.id.widget_tx_empty, View.GONE)

                            // Row 1
                            val tx1 = variableExpenses.getOrNull(0)
                            if (tx1 != null) {
                                views.setViewVisibility(R.id.widget_tx_row_1, View.VISIBLE)
                                views.setTextViewText(R.id.widget_tx_category_1, tx1.categoryName)
                                views.setTextViewText(
                                    R.id.widget_tx_amount_1,
                                    "-${WidgetUpdateHelper.formatWidgetCurrency(tx1.amount, currency)}"
                                )
                            } else {
                                views.setViewVisibility(R.id.widget_tx_row_1, View.GONE)
                            }

                            // Row 2
                            val tx2 = variableExpenses.getOrNull(1)
                            if (tx2 != null) {
                                views.setViewVisibility(R.id.widget_tx_row_2, View.VISIBLE)
                                views.setTextViewText(R.id.widget_tx_category_2, tx2.categoryName)
                                views.setTextViewText(
                                    R.id.widget_tx_amount_2,
                                    "-${WidgetUpdateHelper.formatWidgetCurrency(tx2.amount, currency)}"
                                )
                            } else {
                                views.setViewVisibility(R.id.widget_tx_row_2, View.GONE)
                            }

                            // Row 3
                            val tx3 = variableExpenses.getOrNull(2)
                            if (tx3 != null) {
                                views.setViewVisibility(R.id.widget_tx_row_3, View.VISIBLE)
                                views.setTextViewText(R.id.widget_tx_category_3, tx3.categoryName)
                                views.setTextViewText(
                                    R.id.widget_tx_amount_3,
                                    "-${WidgetUpdateHelper.formatWidgetCurrency(tx3.amount, currency)}"
                                )
                            } else {
                                views.setViewVisibility(R.id.widget_tx_row_3, View.GONE)
                            }
                        }

                        // Create PendingIntent to launch MainActivity on click
                        val clickIntent = Intent(appContext, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            appContext,
                            1,
                            clickIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_tx_balance, pendingIntent)
                        views.setOnClickPendingIntent(R.id.widget_tx_list, pendingIntent)

                        // Push updates
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
