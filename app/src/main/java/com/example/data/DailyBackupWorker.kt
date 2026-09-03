package com.example.data

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Worker para ejecutar copias de seguridad automáticas diarias (cada 24 horas)
 * con rotación y sobreescritura automática en el almacenamiento privado de la app.
 */
class DailyBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val context = applicationContext
        Log.d(TAG, "Iniciando proceso de backup diario automático...")

        try {
            // 1. Forzar checkpoint de WAL en la base de datos Room si está activa
            try {
                val db = FinanceDatabase.getDatabase(context)
                db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { cursor ->
                    cursor.moveToFirst()
                }
                Log.d(TAG, "PRAGMA wal_checkpoint(FULL) ejecutado con éxito")
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo forzar checkpoint WAL (puede que la DB esté en otro estado): ${e.message}")
            }

            // 2. Localizar el archivo principal de la base de datos
            var dbFile = context.getDatabasePath(FinanceDatabase.DATABASE_NAME)
            if (!dbFile.exists()) {
                val alternativeDbFile = context.getDatabasePath("finance_database.db")
                if (alternativeDbFile.exists()) {
                    dbFile = alternativeDbFile
                }
            }

            if (!dbFile.exists()) {
                Log.w(TAG, "El archivo de base de datos no existe aún en el disco. Omitiendo backup diario.")
                return@withContext Result.success()
            }

            // 3. Crear o sobreescribir backup_previous.db en context.filesDir
            val backupFile = File(context.filesDir, BACKUP_FILE_NAME)
            
            // Copia directa y atómica sobrescribiendo el archivo del día anterior
            dbFile.copyTo(backupFile, overwrite = true)

            // 4. Registrar timestamp del último backup
            context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
                .edit()
                .putLong("last_daily_backup_time", System.currentTimeMillis())
                .apply()

            Log.i(
                TAG,
                "✅ Backup diario completado exitosamente: ${backupFile.length()} bytes en ${backupFile.absolutePath}"
            )
            return@withContext Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error durante la ejecución del backup diario", e)
            return@withContext Result.retry()
        }
    }

    companion object {
        const val TAG = "DailyBackupWorker"
        const val WORK_NAME = "DailyBackupWork"
        const val BACKUP_FILE_NAME = "backup_previous.db"

        /**
         * Programa el worker periódico con WorkManager para ejecutarse cada 24 horas
         * con restricción de batería no baja (setRequiresBatteryNotLow).
         */
        fun schedulePeriodicBackup(context: Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()

                val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyBackupWorker>(
                    24, TimeUnit.HOURS
                )
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    dailyWorkRequest
                )

                Log.d(TAG, "DailyBackupWorker programado exitosamente cada 24h (ExistingPeriodicWorkPolicy.KEEP)")
            } catch (e: Exception) {
                Log.e(TAG, "Error programando DailyBackupWorker con WorkManager", e)
            }
        }
    }
}
