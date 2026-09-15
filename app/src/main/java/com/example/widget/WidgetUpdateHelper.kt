package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.data.FinanceDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WidgetUpdateHelper {

    fun updateAllWidgets(context: Context) {
        val appContext = context.applicationContext

        // Update Balance Widget
        val balanceIntent = Intent(appContext, FinanceBalanceWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(appContext).getAppWidgetIds(
                ComponentName(appContext, FinanceBalanceWidgetProvider::class.java)
            )
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        appContext.sendBroadcast(balanceIntent)

        // Update Transactions Widget
        val transactionsIntent = Intent(appContext, FinanceTransactionsWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(appContext).getAppWidgetIds(
                ComponentName(appContext, FinanceTransactionsWidgetProvider::class.java)
            )
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        appContext.sendBroadcast(transactionsIntent)

        // Update Actions Widget
        val actionsIntent = Intent(appContext, FinanceActionsWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(appContext).getAppWidgetIds(
                ComponentName(appContext, FinanceActionsWidgetProvider::class.java)
            )
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        appContext.sendBroadcast(actionsIntent)
    }

    fun formatWidgetCurrency(amount: Double, currency: String): String {
        return try {
            val esLocale = java.util.Locale("es", "ES")
            val symbols = java.text.DecimalFormatSymbols(esLocale)
            val formatter = java.text.DecimalFormat("#,##0.00", symbols)
            val formatted = formatter.format(amount)
            when (currency) {
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
                else -> "$formatted $currency"
            }
        } catch (e: Exception) {
            val formattedString = String.format("%.2f", amount)
            "$formattedString $currency"
        }
    }
}
