package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.MainActivity

class NotificationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("NotificationReceiver", "Received alarm trigger or system boot. Action: ${intent.action}")
        
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            // Re-schedule notifications on device boot
            val workRequest = OneTimeWorkRequestBuilder<ScheduleNotificationsWorker>().build()
            WorkManager.getInstance(context.applicationContext).enqueue(workRequest)
            return
        }

        val expenseId = intent.getStringExtra("EXPENSE_ID") ?: return
        val expenseName = intent.getStringExtra("EXPENSE_NAME") ?: "Gasto"
        val expenseAmount = intent.getDoubleExtra("EXPENSE_AMOUNT", 0.0)

        showNotification(context, expenseId, expenseName, expenseAmount)
    }

    private fun showNotification(context: Context, id: String, name: String, amount: Double) {
        val channelId = "FINANCIAL_ALERTS_CHANNEL"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertas Financieras",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios de próximos pagos de gastos fijos."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id.hashCode(),
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val amountStr = String.format("%.2f €", amount)
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Próximo pago")
            .setContentText("Mañana se cobra el $name ($amountStr)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(id.hashCode(), notification)
        Log.d("NotificationReceiver", "Displayed notification for expenseID: $id")
    }
}
