package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class FinanceActionsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val appContext = context.applicationContext

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(appContext.packageName, R.layout.widget_actions_layout)

            // 1. Add Expense Button Intent
            val expenseIntent = Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("action", "add_expense")
            }
            val expensePendingIntent = PendingIntent.getActivity(
                appContext,
                101,
                expenseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_add_expense, expensePendingIntent)

            // 2. Add Income/Balance Button Intent
            val incomeIntent = Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("action", "add_income")
            }
            val incomePendingIntent = PendingIntent.getActivity(
                appContext,
                102,
                incomeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_add_income, incomePendingIntent)

            // 3. Open App Button Intent
            val appIntent = Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val appPendingIntent = PendingIntent.getActivity(
                appContext,
                103,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_view_app, appPendingIntent)

            // Push updates
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
