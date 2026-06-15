package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
    @Query("SELECT * FROM expense_categories WHERE isAdded = 1")
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

    @Query("DELETE FROM expense_categories WHERE id = :id")
    suspend fun deleteCategoryById(id: String)

    // Variable Daily Expenses
    @Query("SELECT * FROM variable_expenses ORDER BY timestamp DESC")
    fun getVariableExpensesFlow(): Flow<List<VariableExpenseEntry>>

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
    version = 12,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
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
    }
}
