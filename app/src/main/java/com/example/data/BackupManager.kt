package com.example.data

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class BackupManager(
    private val repository: FinanceRepository,
    private val database: FinanceDatabase
) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(BackupData::class.java).indent("  ")

    /**
     * Exporta toda la base de datos local a un String JSON legible listo para ser compartido o guardado.
     */
    suspend fun exportBackup(context: Context): String = withContext(Dispatchers.IO) {
        try {
            val profile = repository.getProfileDirect()
            val categories = repository.getAllCategoriesDirect()
            val expenses = database.financeDao().getVariableExpensesFlow().first()

            val backup = BackupData(
                profile = profile,
                categories = categories,
                expenses = expenses
            )

            return@withContext adapter.toJson(backup)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext ""
        }
    }

    /**
     * Importa y valida un String JSON de copia de seguridad reemplazando la base de datos actual.
     * Utiliza una transacción para garantizar la atomicidad en caso de fallos.
     */
    suspend fun importBackup(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupData = adapter.fromJson(jsonString) ?: return@withContext false

            // Validación rigurosa de estructura mínima
            if (backupData.profile == null && backupData.categories.isEmpty() && backupData.expenses.isEmpty()) {
                return@withContext false
            }

            // Realizamos la limpieza e inserción masiva secuencial
            // Para asegurar atomicidad completa, iniciamos una transacción manual en SQLite
            database.beginTransaction()
            try {
                val dao = database.financeDao()

                // 1. Limpiar datos existentes
                dao.clearFinancialProfile()
                dao.clearExpenseCategories()
                dao.clearVariableExpenses()

                // 2. Restaurar Perfil Financiero
                backupData.profile?.let {
                    dao.insertFinancialProfile(it)
                }

                // 3. Restaurar Categorías de Gasto
                if (backupData.categories.isNotEmpty()) {
                    dao.insertExpenseCategories(backupData.categories)
                }

                // 4. Restaurar Gastos Variables
                if (backupData.expenses.isNotEmpty()) {
                    for (expense in backupData.expenses) {
                        dao.insertVariableExpense(expense)
                    }
                }

                database.setTransactionSuccessful()
                return@withContext true
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext false
            } finally {
                database.endTransaction()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    /**
     * Comprueba si existe una copia de seguridad automática diaria previa en el almacenamiento local.
     */
    fun hasLatestDailyBackup(context: Context): Boolean {
        val backupFile = java.io.File(context.filesDir, DailyBackupWorker.BACKUP_FILE_NAME)
        return backupFile.exists() && backupFile.length() > 0L
    }

    /**
     * Restaura rápidamente la base de datos a partir del archivo backup_previous.db generado por DailyBackupWorker.
     * Cierra los descriptores abiertos, reemplaza el archivo SQLite y reinicia la conexión Room de forma segura.
     */
    suspend fun restoreLatestBackup(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupFile = java.io.File(context.filesDir, DailyBackupWorker.BACKUP_FILE_NAME)
            if (!backupFile.exists() || backupFile.length() == 0L) {
                android.util.Log.e("BackupManager", "No se encontró archivo de backup previo para restaurar.")
                return@withContext false
            }

            // 1. Cerrar la conexión actual de Room
            FinanceDatabase.closeDatabase()

            val dbFile = context.getDatabasePath(FinanceDatabase.DATABASE_NAME)
            val walFile = java.io.File(dbFile.path + "-wal")
            val shmFile = java.io.File(dbFile.path + "-shm")

            // 2. Limpiar ficheros auxiliares WAL y SHM para evitar incongruencias
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            // 3. Sobreescribir el archivo de base de datos principal con la copia de respaldo
            backupFile.copyTo(dbFile, overwrite = true)

            // 4. Reabrir e instanciar la base de datos para verificar su consistencia
            val newDb = FinanceDatabase.getDatabase(context)
            val profile = newDb.financeDao().getFinancialProfileDirect()
            android.util.Log.i("BackupManager", "✅ Restauración rápida de backup completada con éxito. Perfil: ${profile?.userName}")
            return@withContext true
        } catch (e: Exception) {
            android.util.Log.e("BackupManager", "❌ Error al restaurar backup previo", e)
            return@withContext false
        }
    }
}

