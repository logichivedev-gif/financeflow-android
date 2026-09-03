package com.example.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.util.Calendar

class ScheduleNotificationsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("ScheduleWorker", "Scheduling notifications...")
        val context = applicationContext
        
        try {
            // Instancia única a través del Singleton para evitar duplicar descriptores de Room
            val database = FinanceDatabase.getDatabase(context)

            val fixedExpenses = database.financeDao().getAllCategoriesDirect()
                .filter { it.isFixed && it.isAdded && it.payDay != null && !it.isSkippedThisMonth && !it.isArchived }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val sharedPrefs = context.getSharedPreferences("notification_alarms", Context.MODE_PRIVATE)

            for (item in fixedExpenses) {
                val payDay = item.payDay ?: continue
                val triggerTime = calculateTriggerTime(payDay)

                // ID único de alarma
                val alarmId = item.id.hashCode()

                val intent = Intent(context, NotificationAlarmReceiver::class.java).apply {
                    putExtra("EXPENSE_ID", item.id)
                    putExtra("EXPENSE_NAME", item.name)
                    putExtra("EXPENSE_AMOUNT", item.limitAmount)
                }

                // Paso 2: Idempotencia en AlarmManager usando FLAG_NO_CREATE
                val existingPendingIntent = PendingIntent.getBroadcast(
                    context,
                    alarmId,
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )

                val savedTriggerTime = sharedPrefs.getLong("trigger_${item.id}", -1L)

                if (existingPendingIntent != null && savedTriggerTime == triggerTime) {
                    Log.d("ScheduleWorker", "Alarma para ${item.name} ya programada para el timestamp $triggerTime. Abortando llamada.")
                    continue
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    alarmId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
                
                // Guardar confirmación en SharedPreferences
                sharedPrefs.edit().putLong("trigger_${item.id}", triggerTime).apply()
                Log.d("ScheduleWorker", "Alarma programada por primera vez o actualizada para ${item.name} (día $payDay) en $triggerTime")
            }

            return Result.success()

        } catch (e: Exception) {
            Log.e("ScheduleWorker", "Error al programar alertas", e)
            return Result.retry()
        }
    }

    private fun calculateTriggerTime(payDay: Int): Long {
        val nowTime = System.currentTimeMillis()

        // 1. Calcular el momento exacto de la alerta para el ciclo actual (el día anterior al payDay a las 09:00 AM)
        val currentCycleAlert = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, payDay)
            add(Calendar.DAY_OF_MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 2. Comparar el timestamp de la alerta contra el tiempo actual del sistema
        return if (currentCycleAlert.timeInMillis <= nowTime) {
            val now = Calendar.getInstance()
            val isSameDay = now.get(Calendar.YEAR) == currentCycleAlert.get(Calendar.YEAR) &&
                    now.get(Calendar.DAY_OF_YEAR) == currentCycleAlert.get(Calendar.DAY_OF_YEAR)

            if (isSameDay) {
                // Si es el mismo día y ya pasó la hora de la alarma, disparar 10 segundos después para recuperar la alerta
                nowTime + 10000
            } else {
                // Si ya es un día posterior, programar para el siguiente ciclo mensual
                Calendar.getInstance().apply {
                    add(Calendar.MONTH, 1)
                    set(Calendar.DAY_OF_MONTH, payDay)
                    add(Calendar.DAY_OF_MONTH, -1)
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
        } else {
            // Mantener el tiempo del ciclo actual
            currentCycleAlert.timeInMillis
        }
    }
}
