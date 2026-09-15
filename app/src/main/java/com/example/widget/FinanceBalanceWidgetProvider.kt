package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.FinanceDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FinanceBalanceWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val appContext = context.applicationContext
        val db = FinanceDatabase.getDatabase(appContext)

        // Run on background IO thread to query database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val profile = db.financeDao().getFinancialProfileDirect()
                val categories = db.financeDao().getAllCategoriesDirect()
                val variableExpenses = db.financeDao().getVariableExpensesDirect()

                // Calculate calculations
                val currency = profile?.selectedCurrency ?: "€"
                val baseIncome = (profile?.monthlyIncome ?: 0.0) + (profile?.partnerContribution ?: 0.0)
                val isCustomBankBalance = (profile?.currentBankBalance ?: -1.0) > 0.0
                val baseValue = if (isCustomBankBalance) (profile?.currentBankBalance ?: 0.0) else baseIncome
                val incomeDay = profile?.incomeDay ?: 1

                val pendingFixed = categories.filter { category ->
                    !category.isArchived && (category.isFixed || category.isFinancing) && !category.assumedByPartner && !category.isPaid && !category.isSkippedThisMonth &&
                    com.example.ui.FinanceViewModel.isBillInCycleWindow(category.payDay, incomeDay)
                }.sumOf { it.limitAmount }

                val saldoRestante = baseValue - pendingFixed

                // Format amount
                val balanceText = WidgetUpdateHelper.formatWidgetCurrency(saldoRestante, currency)

                // Update views
                withContext(Dispatchers.Main) {
                    for (appWidgetId in appWidgetIds) {
                        val views = RemoteViews(appContext.packageName, R.layout.widget_balance_layout)

                        // Set Balance
                        views.setTextViewText(R.id.widget_balance_amount, balanceText)

                        // Set Status Badge and Styles based on balance amount
                        when {
                            saldoRestante < 0.0 -> {
                                views.setTextViewText(R.id.widget_status_text, "📌 Balance en riesgo")
                                views.setInt(
                                    R.id.widget_status_badge_container,
                                    "setBackgroundResource",
                                    R.drawable.widget_status_danger
                                )
                            }
                            saldoRestante <= 50.0 -> {
                                views.setTextViewText(R.id.widget_status_text, "⚠️ Margen limitado")
                                views.setInt(
                                    R.id.widget_status_badge_container,
                                    "setBackgroundResource",
                                    R.drawable.widget_status_warning
                                )
                            }
                            else -> {
                                views.setTextViewText(R.id.widget_status_text, "✅ Fondo disponible real")
                                views.setInt(
                                    R.id.widget_status_badge_container,
                                    "setBackgroundResource",
                                    R.drawable.widget_status_safe
                                )
                            }
                        }

                        // Create PendingIntent to launch MainActivity on click
                        val clickIntent = Intent(appContext, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            appContext,
                            0,
                            clickIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_balance_amount, pendingIntent)
                        views.setOnClickPendingIntent(R.id.widget_app_title, pendingIntent)
                        views.setOnClickPendingIntent(R.id.widget_status_badge_container, pendingIntent)

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
