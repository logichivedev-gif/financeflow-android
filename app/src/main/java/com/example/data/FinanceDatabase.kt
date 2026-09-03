package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    // Financial Profile
    @Query("SELECT * FROM financial_profile WHERE id = 1")
    fun getFinancialProfileFlow(): Flow<FinancialProfile?>

    @Query("SELECT * FROM financial_profile WHERE id = 1")
    suspend fun getFinancialProfileDirect(): FinancialProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinancialProfile(profile: FinancialProfile)

    // Expense Categories
    @Query("SELECT * FROM expense_categories WHERE isAdded = 1 AND isArchived = 0")
    fun getActiveCategoriesFlow(): Flow<List<ExpenseCategory>>

    @Query("SELECT * FROM expense_categories")
    suspend fun getAllCategoriesDirect(): List<ExpenseCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseCategories(categories: List<ExpenseCategory>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseCategory(category: ExpenseCategory)

    @Update
    suspend fun updateExpenseCategory(category: ExpenseCategory)

    @Query("UPDATE expense_categories SET isPaid = :isPaid WHERE id = :id")
    suspend fun updateCategoryPaidStatus(id: String, isPaid: Boolean)

    @Query("UPDATE expense_categories SET isArchived = :isArchived WHERE id = :id")
    suspend fun updateCategoryArchivedStatus(id: String, isArchived: Boolean)

    @Query("SELECT * FROM expense_categories WHERE isArchived = 1")
    fun getArchivedCategoriesFlow(): Flow<List<ExpenseCategory>>

    @Query("SELECT * FROM expense_categories WHERE isArchived = 1")
    suspend fun getArchivedCategoriesDirect(): List<ExpenseCategory>

    @Query("DELETE FROM expense_categories WHERE id = :id")
    suspend fun deleteCategoryById(id: String)

    // Variable Daily Expenses
    @Query("SELECT * FROM variable_expenses ORDER BY timestamp DESC")
    fun getVariableExpensesFlow(): Flow<List<VariableExpenseEntry>>

    @Query("SELECT * FROM variable_expenses ORDER BY timestamp DESC")
    suspend fun getVariableExpensesDirect(): List<VariableExpenseEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariableExpense(expense: VariableExpenseEntry)

    @Query("DELETE FROM variable_expenses WHERE id = :id")
    suspend fun deleteVariableExpenseById(id: Int)

    // Purge logic for situation resets
    @Query("DELETE FROM financial_profile")
    suspend fun clearFinancialProfile()

    @Query("DELETE FROM expense_categories")
    suspend fun clearExpenseCategories()

    @Query("DELETE FROM variable_expenses")
    suspend fun clearVariableExpenses()
}

@Database(
    entities = [FinancialProfile::class, ExpenseCategory::class, VariableExpenseEntry::class],
    version = 21,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expense_categories ADD COLUMN totalInstallments INTEGER")
                db.execSQL("ALTER TABLE expense_categories ADD COLUMN currentInstallment INTEGER")
                db.execSQL("ALTER TABLE expense_categories ADD COLUMN financingStartDate INTEGER")
            }
        }

        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expense_categories ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN themeMode TEXT NOT NULL DEFAULT 'system'")
            }
        }

        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN isBankNotificationInterceptorEnabled INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN userName TEXT NOT NULL DEFAULT 'Usuario'")
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN avatarId TEXT NOT NULL DEFAULT 'avatar_1'")
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN customAvatarUri TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN isBiometricEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN isPaymentNotificationsEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN paymentNotificationHoursLead INTEGER NOT NULL DEFAULT 48")
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN customCycleStartDay INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN isHighPerformanceMode INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expense_categories ADD COLUMN isCashPayment INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expense_categories ADD COLUMN isInsurance INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expense_categories ADD COLUMN isSkippedThisMonth INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN selectedCurrency TEXT NOT NULL DEFAULT '€'")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN isProUser INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN lastActiveYear INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN lastActiveMonth INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN incomeDay INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN hideNewMonthBanner INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN hideSmartCalendarBanner INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN selectedTheme TEXT NOT NULL DEFAULT 'azul'")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE financial_profile ADD COLUMN selectedIcon TEXT NOT NULL DEFAULT 'trending'")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expense_categories ADD COLUMN isFinancing INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE expense_categories ADD COLUMN monthsRemaining INTEGER")
            }
        }

        const val DATABASE_NAME = "finance_flow.db"

        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun closeDatabase() {
            synchronized(this) {
                try {
                    if (INSTANCE?.isOpen == true) {
                        INSTANCE?.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    INSTANCE = null
                }
            }
        }

        fun getDatabase(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    DATABASE_NAME
                ).addMigrations(
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10,
                    MIGRATION_10_11,
                    MIGRATION_11_12,
                    MIGRATION_12_13,
                    MIGRATION_13_14,
                    MIGRATION_14_15,
                    MIGRATION_15_16,
                    MIGRATION_16_17,
                    MIGRATION_17_18,
                    MIGRATION_18_19,
                    MIGRATION_19_20,
                    MIGRATION_20_21
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
