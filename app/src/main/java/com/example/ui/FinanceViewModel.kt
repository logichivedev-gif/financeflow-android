package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import org.json.JSONArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Remoción completa de diagnósticos por IA externos para mantener lógica inteligente local pura e infalible
sealed class Screen {
    object WizardStep1 : Screen()
    object WizardStep2 : Screen()
    object WizardStep3 : Screen()
    object WizardStep4 : Screen()
    object Dashboard : Screen()
}

enum class DashboardTab {
    RESUMEN, GASTOS_FIJOS, GASTOS_VARIABLES, ANALISIS
}

data class WizardFixedExpense(
    val id: String,
    val name: String,
    val amount: String,
    val payDay: String = ""
)

data class WizardVariableBudget(
    val id: String,
    val name: String,
    val amount: String
)

data class WizardCustomStream(
    val id: String,
    val name: String,
    val amount: String
)

data class MonthProjection(
    val baseIncome: Double,
    val totalFixed: Double,
    val totalVariable: Double,
    val averageDailyVariable: Double,
    val projectedVariable: Double,
    val pendingFixed: Double,
    val projectedMonthEndSpent: Double,
    val projectedMonthEndBalance: Double
)

class FinanceViewModel(val repository: FinanceRepository) : ViewModel() {

    // Current Screen Navigation Flow
    private val _currentScreen = MutableStateFlow<Screen>(Screen.WizardStep1)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private var isInitialScreenSet = false
    private var isRolloverChecked = false

    // Raw input states for the Step Wizard
    private val _incomeInput = MutableStateFlow("")
    val incomeInput: StateFlow<String> = _incomeInput.asStateFlow()

    private val _wizardBankBalanceInput = MutableStateFlow("")
    val wizardBankBalanceInput: StateFlow<String> = _wizardBankBalanceInput.asStateFlow()

    private val _incomeDayInput = MutableStateFlow("10")
    val incomeDayInput: StateFlow<String> = _incomeDayInput.asStateFlow()

    // Step 2 elements
    private val _fixedExpenses = MutableStateFlow<List<WizardFixedExpense>>(
        listOf(
            WizardFixedExpense("alquiler", "Alquiler", "600"),
            WizardFixedExpense("gimnasio", "Gimnasio", "30"),
            WizardFixedExpense("internet", "Internet", "40"),
            WizardFixedExpense("tarjeta_credito", "Tarjeta de Crédito", "50")
        )
    )
    val fixedExpenses: StateFlow<List<WizardFixedExpense>> = _fixedExpenses.asStateFlow()

    // Step 3 elements
    private val _variableBudgets = MutableStateFlow<List<WizardVariableBudget>>(
        listOf(
            WizardVariableBudget("comida", "Comida", "250"),
            WizardVariableBudget("transporte", "Transporte", "100"),
            WizardVariableBudget("ocio", "Ocio", "150")
        )
    )
    val variableBudgets: StateFlow<List<WizardVariableBudget>> = _variableBudgets.asStateFlow()

    // Step 4 elements & Accordion Controls
    private val _hasPets = MutableStateFlow(false)
    val hasPets: StateFlow<Boolean> = _hasPets.asStateFlow()

    private val _petsCost = MutableStateFlow("30")
    val petsCost: StateFlow<String> = _petsCost.asStateFlow()

    private val _hasKids = MutableStateFlow(false)
    val hasKids: StateFlow<Boolean> = _hasKids.asStateFlow()

    private val _kidsCost = MutableStateFlow("150")
    val kidsCost: StateFlow<String> = _kidsCost.asStateFlow()

    private val _sharedExpenses = MutableStateFlow(false)
    val sharedExpenses: StateFlow<Boolean> = _sharedExpenses.asStateFlow()

    private val _partnerContribution = MutableStateFlow("")
    val partnerContribution: StateFlow<String> = _partnerContribution.asStateFlow()

    // Supplies Predefined
    private val _waterBilling = MutableStateFlow("Mensual") // "Mensual" or "Bimensual"
    val waterBilling: StateFlow<String> = _waterBilling.asStateFlow()

    private val _waterCost = MutableStateFlow("40")
    val waterCost: StateFlow<String> = _waterCost.asStateFlow()

    private val _isWaterEnabled = MutableStateFlow(true)
    val isWaterEnabled: StateFlow<Boolean> = _isWaterEnabled.asStateFlow()

    private val _waterPayDayValue = MutableStateFlow("")
    val waterPayDayValue: StateFlow<String> = _waterPayDayValue.asStateFlow()

    private val _electricityBilling = MutableStateFlow("Mensual") // "Mensual" or "Bimensual"
    val electricityBilling: StateFlow<String> = _electricityBilling.asStateFlow()

    private val _electricityCost = MutableStateFlow("60")
    val electricityCost: StateFlow<String> = _electricityCost.asStateFlow()

    private val _isElectricityEnabled = MutableStateFlow(true)
    val isElectricityEnabled: StateFlow<Boolean> = _isElectricityEnabled.asStateFlow()

    private val _electricityPayDayValue = MutableStateFlow("")
    val electricityPayDayValue: StateFlow<String> = _electricityPayDayValue.asStateFlow()

    // Popular Streaming subscriptions
    private val _netflixActive = MutableStateFlow(false)
    val netflixActive: StateFlow<Boolean> = _netflixActive.asStateFlow()
    private val _netflixCost = MutableStateFlow("15")
    val netflixCost: StateFlow<String> = _netflixCost.asStateFlow()

    private val _hboActive = MutableStateFlow(false)
    val hboActive: StateFlow<Boolean> = _hboActive.asStateFlow()
    private val _hboCost = MutableStateFlow("10")
    val hboCost: StateFlow<String> = _hboCost.asStateFlow()

    private val _disneyActive = MutableStateFlow(false)
    val disneyActive: StateFlow<Boolean> = _disneyActive.asStateFlow()
    private val _disneyCost = MutableStateFlow("11")
    val disneyCost: StateFlow<String> = _disneyCost.asStateFlow()

    private val _juegosActive = MutableStateFlow(false)
    val juegosActive: StateFlow<Boolean> = _juegosActive.asStateFlow()
    private val _juegosCost = MutableStateFlow("20")
    val juegosCost: StateFlow<String> = _juegosCost.asStateFlow()

    private val _amazonPrimeActive = MutableStateFlow(false)
    val amazonPrimeActive: StateFlow<Boolean> = _amazonPrimeActive.asStateFlow()
    private val _amazonPrimeCost = MutableStateFlow("5")
    val amazonPrimeCost: StateFlow<String> = _amazonPrimeCost.asStateFlow()

    private val _spotifyActive = MutableStateFlow(false)
    val spotifyActive: StateFlow<Boolean> = _spotifyActive.asStateFlow()
    private val _spotifyCost = MutableStateFlow("11")
    val spotifyCost: StateFlow<String> = _spotifyCost.asStateFlow()

    private val _youtubePremiumActive = MutableStateFlow(false)
    val youtubePremiumActive: StateFlow<Boolean> = _youtubePremiumActive.asStateFlow()
    private val _youtubePremiumCost = MutableStateFlow("14")
    val youtubePremiumCost: StateFlow<String> = _youtubePremiumCost.asStateFlow()

    private val _youtubeMusicActive = MutableStateFlow(false)
    val youtubeMusicActive: StateFlow<Boolean> = _youtubeMusicActive.asStateFlow()
    private val _youtubeMusicCost = MutableStateFlow("10")
    val youtubeMusicCost: StateFlow<String> = _youtubeMusicCost.asStateFlow()

    // Banner hide configurations
    private val _hideNewMonthBanner = MutableStateFlow(false)
    val hideNewMonthBanner: StateFlow<Boolean> = _hideNewMonthBanner.asStateFlow()

    private val _hideSmartCalendarBanner = MutableStateFlow(false)
    val hideSmartCalendarBanner: StateFlow<Boolean> = _hideSmartCalendarBanner.asStateFlow()

    // Custom streams from step 4
    private val _customStreams = MutableStateFlow<List<WizardCustomStream>>(emptyList())
    val customStreams: StateFlow<List<WizardCustomStream>> = _customStreams.asStateFlow()

    // Motor de Análisis Financiero Inteligente (Interactive Fields & State Sync)
    private val _digitalBalanceInput = MutableStateFlow("")
    val digitalBalanceInput: StateFlow<String> = _digitalBalanceInput.asStateFlow()

    private val _gastoVariableHoyInput = MutableStateFlow("")
    val gastoVariableHoyInput: StateFlow<String> = _gastoVariableHoyInput.asStateFlow()

    fun updateDigitalBalanceInput(input: String) {
        _digitalBalanceInput.value = input
        val parsed = input.toDoubleOrNull() ?: -1.0
        viewModelScope.launch {
            val existed = repository.getProfileDirect()
            if (existed != null) {
                repository.saveFinancialProfile(existed.copy(currentBankBalance = parsed))
            }
        }
    }

    fun updateGastoVariableHoyInput(input: String) {
        _gastoVariableHoyInput.value = input
    }

    // Database Reactive Flows (Offline-First State Sync)
    val dbProfile: StateFlow<FinancialProfile?> = repository.financialProfile
        .map { profile -> profile?.copy(isProUser = true) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dbCategories: StateFlow<List<ExpenseCategory>> = combine(repository.activeCategories, repository.financialProfile) { categories, profile ->
        categories
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dbVariableExpenses: StateFlow<List<VariableExpenseEntry>> = repository.variableExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalGastosFijos: StateFlow<Double> = dbCategories
        .map { categories -> categories.filter { it.isFixed && !it.assumedByPartner }.sumOf { it.limitAmount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalGastosVariables: StateFlow<Double> = dbVariableExpenses
        .map { expenses -> expenses.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun isBillScheduledDateInPastOrToday(payDay: Int, incomeDay: Int): Boolean {
        val today = java.util.Calendar.getInstance()
        val todayDay = today.get(java.util.Calendar.DAY_OF_MONTH)

        val startFinancialCalendar = java.util.Calendar.getInstance()
        val m1: Int
        val y1: Int
        if (todayDay >= incomeDay) {
            m1 = today.get(java.util.Calendar.MONTH)
            y1 = today.get(java.util.Calendar.YEAR)
        } else {
            startFinancialCalendar.add(java.util.Calendar.MONTH, -1)
            m1 = startFinancialCalendar.get(java.util.Calendar.MONTH)
            y1 = startFinancialCalendar.get(java.util.Calendar.YEAR)
        }

        val scheduled = java.util.Calendar.getInstance()
        scheduled.clear()

        if (payDay >= incomeDay) {
            scheduled.set(java.util.Calendar.YEAR, y1)
            scheduled.set(java.util.Calendar.MONTH, m1)
            val maxDays = scheduled.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            val finalDay = minOf(payDay, maxDays)
            scheduled.set(java.util.Calendar.DAY_OF_MONTH, finalDay)
        } else {
            scheduled.set(java.util.Calendar.YEAR, y1)
            scheduled.set(java.util.Calendar.MONTH, m1)
            scheduled.add(java.util.Calendar.MONTH, 1)
            val maxDays = scheduled.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            val finalDay = minOf(payDay, maxDays)
            scheduled.set(java.util.Calendar.DAY_OF_MONTH, finalDay)
        }

        val scheduledYear = scheduled.get(java.util.Calendar.YEAR)
        val scheduledMonth = scheduled.get(java.util.Calendar.MONTH)
        val scheduledDay = scheduled.get(java.util.Calendar.DAY_OF_MONTH)

        val todayYear = today.get(java.util.Calendar.YEAR)
        val todayMonth = today.get(java.util.Calendar.MONTH)
        val todayDayVal = today.get(java.util.Calendar.DAY_OF_MONTH)

        if (todayYear > scheduledYear) return true
        if (todayYear < scheduledYear) return false

        if (todayMonth > scheduledMonth) return true
        if (todayMonth < scheduledMonth) return false

        return todayDayVal >= scheduledDay
    }

    fun getFinancialCycleDays(incomeDay: Int): Triple<Int, Int, Int> {
        val today = java.util.Calendar.getInstance()
        val todayDay = today.get(java.util.Calendar.DAY_OF_MONTH)

        val startCal = java.util.Calendar.getInstance()
        val endCal = java.util.Calendar.getInstance()

        if (todayDay >= incomeDay) {
            startCal.set(java.util.Calendar.DAY_OF_MONTH, incomeDay)
            endCal.set(java.util.Calendar.DAY_OF_MONTH, incomeDay)
            endCal.add(java.util.Calendar.MONTH, 1)
            endCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        } else {
            startCal.add(java.util.Calendar.MONTH, -1)
            startCal.set(java.util.Calendar.DAY_OF_MONTH, incomeDay)
            endCal.set(java.util.Calendar.DAY_OF_MONTH, incomeDay)
            endCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }

        startCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        startCal.set(java.util.Calendar.MINUTE, 0)
        startCal.set(java.util.Calendar.SECOND, 0)
        startCal.set(java.util.Calendar.MILLISECOND, 0)

        endCal.set(java.util.Calendar.HOUR_OF_DAY, 23)
        endCal.set(java.util.Calendar.MINUTE, 59)
        endCal.set(java.util.Calendar.SECOND, 59)

        val todayTime = today.timeInMillis
        val startTime = startCal.timeInMillis
        val endTime = endCal.timeInMillis

        val totalMs = endTime - startTime
        val totalDays = maxOf(1, (totalMs / (1000L * 60 * 60 * 24)).toInt() + 1)

        val elapsedMs = maxOf(0L, todayTime - startTime)
        val elapsedDays = maxOf(1, (elapsedMs / (1000L * 60 * 60 * 24)).toInt() + 1)

        val remainingDays = maxOf(0, totalDays - elapsedDays)

        return Triple(elapsedDays, remainingDays, totalDays)
    }

    val saldoRestanteDisponible: StateFlow<Double> = combine(dbProfile, dbCategories, totalGastosVariables) { profile, categories, variable ->
        if (profile == null) 0.0
        else {
            val useBankBalance = profile.currentBankBalance >= 0.0
            val baseValue = if (useBankBalance) {
                profile.currentBankBalance
            } else {
                profile.monthlyIncome + profile.partnerContribution
            }

            val adjustedFixed = if (useBankBalance) {
                categories.filter { category ->
                    category.isFixed && !category.assumedByPartner && !category.isPaid
                }.sumOf { it.limitAmount }
            } else {
                categories.filter { category ->
                    category.isFixed && !category.assumedByPartner
                }.sumOf { it.limitAmount }
            }

            baseValue - adjustedFixed - variable
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthProjection: StateFlow<MonthProjection?> = combine(
        dbProfile,
        dbCategories,
        dbVariableExpenses
    ) { profile, categories, variableExpenses ->
        if (profile == null) return@combine null

        val baseIncome = profile.monthlyIncome + profile.partnerContribution
        val totalFixed = categories.filter { it.isFixed && !it.assumedByPartner }.sumOf { it.limitAmount }
        val totalVariable = variableExpenses.sumOf { it.amount }

        val cycleDays = getFinancialCycleDays(profile.incomeDay)
        val elapsedDays = cycleDays.first
        val remainingDays = cycleDays.second
        val totalDaysInMonth = cycleDays.third

        val averageDailyVariable = totalVariable / elapsedDays
        val projectedVariable = totalVariable + (averageDailyVariable * remainingDays)

        val pendingFixed = categories.filter { category ->
            category.isFixed && !category.assumedByPartner && !category.isPaid
        }.sumOf { it.limitAmount }

        val projectedMonthEndSpent = projectedVariable + totalFixed
        val projectedMonthEndBalance = baseIncome - projectedMonthEndSpent

        MonthProjection(
            baseIncome = baseIncome,
            totalFixed = totalFixed,
            totalVariable = totalVariable,
            averageDailyVariable = averageDailyVariable,
            projectedVariable = projectedVariable,
            pendingFixed = pendingFixed,
            projectedMonthEndSpent = projectedMonthEndSpent,
            projectedMonthEndBalance = projectedMonthEndBalance
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Interactive Dashboard Controls - real-time calculations as requested
    fun updateCategoryPayDay(id: String, payDay: Int?) {
        viewModelScope.launch {
            val existed = repository.getAllCategoriesDirect().find { it.id == id } ?: return@launch
            repository.saveCategory(existed.copy(payDay = payDay))
            runSmartAutoCheck()
        }
    }

    fun runSmartAutoCheck() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val categories = repository.getAllCategoriesDirect()
                val profile = repository.getProfileDirect() ?: return@withContext
                val incomeDay = profile.incomeDay
                val toUpdate = categories.filter { category ->
                    category.isFixed &&
                            category.isAdded &&
                            category.payDay != null &&
                            isBillScheduledDateInPastOrToday(category.payDay, incomeDay) &&
                            !category.isPaid
                }
                if (toUpdate.isNotEmpty()) {
                    val updated = toUpdate.map { it.copy(isPaid = true) }
                    repository.saveCategories(updated)
                }
            }
        }
    }

    fun toggleCategoryIsVariableBill(id: String, isVariable: Boolean) {
        viewModelScope.launch {
            val existed = repository.getAllCategoriesDirect().find { it.id == id } ?: return@launch
            repository.saveCategory(existed.copy(isVariableBill = isVariable))
        }
    }

    fun updateCategoryLimitAmount(id: String, amount: Double) {
        viewModelScope.launch {
            val existed = repository.getAllCategoriesDirect().find { it.id == id } ?: return@launch
            repository.saveCategory(existed.copy(limitAmount = amount, rawAmount = amount))
        }
    }

    fun deleteFixedBillCategory(id: String) {
        viewModelScope.launch {
            repository.deleteCategory(id)
        }
    }

    fun addFixedBillDb(name: String, amount: Double, isFinancing: Boolean, monthsRemaining: Int?, payDay: Int?) {
        viewModelScope.launch {
            val id = "fixed_${java.util.UUID.randomUUID()}"
            val profile = repository.getProfileDirect()
            val isPro = true
            val categories = repository.getAllCategoriesDirect().filter { it.isAdded }
            val existingFinancing = categories.count { it.isFinancing }

            val finalIsFinancing = if (!isPro && existingFinancing >= 2) false else isFinancing
            val finalMonths = if (finalIsFinancing) monthsRemaining else null

            repository.saveCategory(
                com.example.data.ExpenseCategory(
                    id = id,
                    name = name,
                    limitAmount = amount,
                    isFixed = true,
                    rawAmount = amount,
                    isAdded = true,
                    isFinancing = finalIsFinancing,
                    monthsRemaining = finalMonths,
                    payDay = payDay
                )
            )
        }
    }

    fun updateFixedBillDb(id: String, name: String, amount: Double, isFinancing: Boolean, monthsRemaining: Int?, payDay: Int?) {
        viewModelScope.launch {
            val existed = repository.getAllCategoriesDirect().find { it.id == id } ?: return@launch
            val profile = repository.getProfileDirect()
            val isPro = true
            val categories = repository.getAllCategoriesDirect().filter { it.id != id && it.isAdded }
            val existingFinancing = categories.count { it.isFinancing }

            val finalIsFinancing = if (!isPro && existingFinancing >= 2) false else isFinancing
            val finalMonths = if (finalIsFinancing) monthsRemaining else null

            repository.saveCategory(
                existed.copy(
                    name = name,
                    limitAmount = amount,
                    rawAmount = amount,
                    isFinancing = finalIsFinancing,
                    monthsRemaining = finalMonths,
                    payDay = payDay
                )
            )
        }
    }

    fun addVariableBudgetDb(name: String, limit: Double) {
        viewModelScope.launch {
            val id = "var_${java.util.UUID.randomUUID()}"
            repository.saveCategory(
                com.example.data.ExpenseCategory(
                    id = id,
                    name = name,
                    limitAmount = limit,
                    isFixed = false,
                    rawAmount = limit,
                    isAdded = true
                )
            )
        }
    }

    fun updateVariableBudgetDb(id: String, name: String, limit: Double) {
        viewModelScope.launch {
            val existed = repository.getAllCategoriesDirect().find { it.id == id } ?: return@launch
            repository.saveCategory(
                existed.copy(
                    name = name,
                    limitAmount = limit,
                    rawAmount = limit
                )
            )
        }
    }

    fun deleteVariableBudgetDb(id: String) {
        viewModelScope.launch {
            repository.deleteCategory(id)
        }
    }

    fun startNewMonth() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val categories = repository.getAllCategoriesDirect()
                val profile = repository.getProfileDirect()
                val isPro = true

                var financingCount = 0
                val resetCategories = categories.map { category ->
                    if (category.isFixed) {
                        if (category.isFinancing) {
                            financingCount++
                            if (!isPro && financingCount > 2) {
                                // Excess financing for free users - disable to prevent ghost subtractions
                                category.copy(isAdded = false, isPaid = false)
                            } else {
                                val rem = category.monthsRemaining
                                if (rem != null) {
                                    val nextRem = rem - 1
                                    if (nextRem <= 0) {
                                        category.copy(isAdded = false, monthsRemaining = 0, isPaid = false)
                                    } else {
                                        category.copy(monthsRemaining = nextRem, isPaid = false)
                                    }
                                } else {
                                    category.copy(isPaid = false)
                                }
                            }
                        } else {
                            category.copy(isPaid = false)
                        }
                    } else {
                        category
                    }
                }
                repository.saveCategories(resetCategories)
                repository.clearVariableExpensesOnly()

                if (profile != null) {
                    val today = java.util.Calendar.getInstance()
                    val todayDay = today.get(java.util.Calendar.DAY_OF_MONTH)
                    val incomeDay = profile.incomeDay
                    val m1: Int
                    val y1: Int
                    if (todayDay >= incomeDay) {
                        m1 = today.get(java.util.Calendar.MONTH) + 1
                        y1 = today.get(java.util.Calendar.YEAR)
                    } else {
                        val tempCal = java.util.Calendar.getInstance()
                        tempCal.add(java.util.Calendar.MONTH, -1)
                        m1 = tempCal.get(java.util.Calendar.MONTH) + 1
                        y1 = tempCal.get(java.util.Calendar.YEAR)
                    }
                    repository.saveFinancialProfile(profile.copy(
                        lastActiveYear = y1,
                        lastActiveMonth = m1
                    ))
                }
            }
            runSmartAutoCheck()
        }
    }

    private fun checkAndHandleMonthRollover(profile: FinancialProfile) {
        if (isRolloverChecked) return
        if (!profile.isWizardComplete) return
        isRolloverChecked = true

        val today = java.util.Calendar.getInstance()
        val todayDay = today.get(java.util.Calendar.DAY_OF_MONTH)
        val incomeDay = profile.incomeDay

        val m1: Int
        val y1: Int
        if (todayDay >= incomeDay) {
            m1 = today.get(java.util.Calendar.MONTH) + 1
            y1 = today.get(java.util.Calendar.YEAR)
        } else {
            val tempCal = java.util.Calendar.getInstance()
            tempCal.add(java.util.Calendar.MONTH, -1)
            m1 = tempCal.get(java.util.Calendar.MONTH) + 1
            y1 = tempCal.get(java.util.Calendar.YEAR)
        }

        viewModelScope.launch {
            if (profile.lastActiveYear == 0 && profile.lastActiveMonth == 0) {
                // First run since feature introduced, just record current month
                withContext(Dispatchers.IO) {
                    repository.saveFinancialProfile(profile.copy(
                        lastActiveYear = y1,
                        lastActiveMonth = m1
                    ))
                }
            } else if (profile.lastActiveYear != y1 || profile.lastActiveMonth != m1) {
                // Actual month rollover happened!
                startNewMonth()
            }
        }
    }

    init {
        // Observe DB to check if wizard is complete and synchronize state
        viewModelScope.launch {
            repository.financialProfile.collect { profile ->
                if (profile != null) {
                    checkAndHandleMonthRollover(profile)
                    // Prepopulate values if found
                    _incomeInput.value = if (profile.monthlyIncome > 0) profile.monthlyIncome.toString() else ""
                    _incomeDayInput.value = profile.incomeDay.toString()
                    _wizardBankBalanceInput.value = if (profile.currentBankBalance >= 0.0) {
                        val str = profile.currentBankBalance.toString()
                        if (str.endsWith(".0")) str.substring(0, str.length - 2) else str
                    } else ""
                    if (profile.currentBankBalance >= 0.0) {
                        val str = profile.currentBankBalance.toString()
                        _digitalBalanceInput.value = if (str.endsWith(".0")) str.substring(0, str.length - 2) else str
                    }
                    _hasPets.value = profile.hasPets
                    _petsCost.value = profile.petsCost.toString()
                    _hasKids.value = profile.hasKids
                    _kidsCost.value = profile.kidsCost.toString()
                    _sharedExpenses.value = profile.sharedExpenses
                    _partnerContribution.value = if (profile.partnerContribution > 0) profile.partnerContribution.toLong().toString() else ""
                    _waterBilling.value = profile.waterBilling
                    _waterCost.value = profile.waterCost.toString()
                    _isWaterEnabled.value = profile.isWaterAdded
                    _electricityBilling.value = profile.electricityBilling
                    _electricityCost.value = profile.electricityCost.toString()
                    _isElectricityEnabled.value = profile.isElectricityAdded
                    _hideNewMonthBanner.value = profile.hideNewMonthBanner
                    _hideSmartCalendarBanner.value = profile.hideSmartCalendarBanner

                    if (!isInitialScreenSet) {
                        if (profile.isWizardComplete) {
                            _currentScreen.value = Screen.Dashboard
                        } else {
                            _currentScreen.value = Screen.WizardStep1
                        }
                        isInitialScreenSet = true
                    }
                } else {
                    if (!isInitialScreenSet) {
                        _currentScreen.value = Screen.WizardStep1
                        isInitialScreenSet = true
                    }
                }
            }
        }

        // Prepopulate based on existing categories if wizard is complete
        viewModelScope.launch {
            repository.activeCategories.collect { categories ->
                if (categories.isNotEmpty() && dbProfile.value?.isWizardComplete == true) {
                    // Populate stream active statuses from DB
                    categories.find { it.id == "netflix" }?.let {
                        _netflixActive.value = it.isAdded
                        _netflixCost.value = it.rawAmount.toString()
                    }
                    categories.find { it.id == "hbo_max" }?.let {
                        _hboActive.value = it.isAdded
                        _hboCost.value = it.rawAmount.toString()
                    }
                    categories.find { it.id == "disney_plus" }?.let {
                        _disneyActive.value = it.isAdded
                        _disneyCost.value = it.rawAmount.toString()
                    }
                    categories.find { it.id == "juegos" }?.let {
                        _juegosActive.value = it.isAdded
                        _juegosCost.value = it.rawAmount.toString()
                    }
                    categories.find { it.id == "amazon_prime" }?.let {
                        _amazonPrimeActive.value = it.isAdded
                        _amazonPrimeCost.value = it.rawAmount.toString()
                    }
                    categories.find { it.id == "spotify" }?.let {
                        _spotifyActive.value = it.isAdded
                        _spotifyCost.value = it.rawAmount.toString()
                    }
                    categories.find { it.id == "youtube_premium" }?.let {
                        _youtubePremiumActive.value = it.isAdded
                        _youtubePremiumCost.value = it.rawAmount.toString()
                    }
                    categories.find { it.id == "youtube_music" }?.let {
                        _youtubeMusicActive.value = it.isAdded
                        _youtubeMusicCost.value = it.rawAmount.toString()
                    }
                }
            }
        }
        runSmartAutoCheck()
    }

    // Step 1 Actions
    fun setIncomeInput(input: String) {
        _incomeInput.value = input.filter { it.isDigit() || it == '.' }
    }

    fun setWizardBankBalanceInput(input: String) {
        _wizardBankBalanceInput.value = input.filter { it.isDigit() || it == '.' }
    }

    fun updateIncomeDayInput(input: String) {
        _incomeDayInput.value = input.filter { it.isDigit() }
    }

    fun completeStep1() {
        val income = _incomeInput.value.toDoubleOrNull() ?: 0.0
        val bankBal = _wizardBankBalanceInput.value.toDoubleOrNull() ?: -1.0
        val dayInc = _incomeDayInput.value.toIntOrNull() ?: 1
        viewModelScope.launch {
            val existed = repository.getProfileDirect()
            val newProfile = existed?.copy(
                monthlyIncome = income,
                currentBankBalance = bankBal,
                incomeDay = dayInc
            ) ?: FinancialProfile(
                monthlyIncome = income,
                currentBankBalance = bankBal,
                incomeDay = dayInc
            )
            repository.saveFinancialProfile(newProfile)
            if (bankBal >= 0.0) {
                val str = bankBal.toString()
                _digitalBalanceInput.value = if (str.endsWith(".0")) str.substring(0, str.length - 2) else str
            }
            _currentScreen.value = Screen.WizardStep2
        }
    }

    // Step 2 Actions (Fixed Expenses editing)
    fun updateFixedExpense(id: String, name: String, amount: String, payDay: String) {
        _fixedExpenses.value = _fixedExpenses.value.map { item ->
            if (item.id == id) item.copy(
                name = name,
                amount = amount.filter { it.isDigit() || it == '.' },
                payDay = payDay.filter { it.isDigit() }
            ) else item
        }
    }

    fun deleteFixedExpense(id: String) {
        _fixedExpenses.value = _fixedExpenses.value.filter { it.id != id }
    }

    fun addCustomFixedExpense() {
        val uniqueId = "custom_fixed_${UUID.randomUUID()}"
        _fixedExpenses.value = _fixedExpenses.value + WizardFixedExpense(uniqueId, "Gasto Personalizado", "0")
    }

    fun completeStep2() {
        _currentScreen.value = Screen.WizardStep3
    }

    // Step 3 Actions (Variable Budgets editing)
    fun updateVariableBudget(id: String, name: String, amount: String) {
        _variableBudgets.value = _variableBudgets.value.map { item ->
            if (item.id == id) item.copy(name = name, amount = amount.filter { it.isDigit() || it == '.' }) else item
        }
    }

    fun deleteVariableBudget(id: String) {
        _variableBudgets.value = _variableBudgets.value.filter { it.id != id }
    }

    fun addCustomVariableBudget() {
        val uniqueId = "custom_var_${UUID.randomUUID()}"
        _variableBudgets.value = _variableBudgets.value + WizardVariableBudget(uniqueId, "Presupuesto Nuevo", "0")
    }

    fun completeStep3() {
        _currentScreen.value = Screen.WizardStep4
    }

    // Step 4 Actions & Accordion state modifications
    fun setHasPets(value: Boolean) { _hasPets.value = value }
    fun setPetsCost(valStr: String) { _petsCost.value = valStr.filter { it.isDigit() || it == '.' } }

    fun setHasKids(value: Boolean) { _hasKids.value = value }
    fun setKidsCost(valStr: String) { _kidsCost.value = valStr.filter { it.isDigit() || it == '.' } }

    fun setSharedExpenses(value: Boolean) { _sharedExpenses.value = value }
    fun setPartnerContribution(valStr: String) { _partnerContribution.value = valStr.filter { it.isDigit() || it == '.' } }

    fun setWaterBilling(billing: String) { _waterBilling.value = billing }
    fun setWaterCost(valStr: String) { _waterCost.value = valStr.filter { it.isDigit() || it == '.' } }
    fun setWaterEnabled(value: Boolean) { _isWaterEnabled.value = value }
    fun setWaterPayDay(value: String) { _waterPayDayValue.value = value.filter { it.isDigit() } }

    fun setElectricityBilling(billing: String) { _electricityBilling.value = billing }
    fun setElectricityCost(valStr: String) { _electricityCost.value = valStr.filter { it.isDigit() || it == '.' } }
    fun setElectricityEnabled(value: Boolean) { _isElectricityEnabled.value = value }
    fun setElectricityPayDay(value: String) { _electricityPayDayValue.value = value.filter { it.isDigit() } }

    fun setNetflixActive(value: Boolean) { _netflixActive.value = value }
    fun setNetflixCost(valStr: String) { _netflixCost.value = valStr.filter { it.isDigit() || it == '.' } }

    fun setHboActive(value: Boolean) { _hboActive.value = value }
    fun setHboCost(valStr: String) { _hboCost.value = valStr.filter { it.isDigit() || it == '.' } }

    fun setDisneyActive(value: Boolean) { _disneyActive.value = value }
    fun setDisneyCost(valStr: String) { _disneyCost.value = valStr.filter { it.isDigit() || it == '.' } }

    fun setJuegosActive(value: Boolean) { _juegosActive.value = value }
    fun setJuegosCost(valStr: String) { _juegosCost.value = valStr.filter { it.isDigit() || it == '.' } }

    fun setAmazonPrimeActive(value: Boolean) { _amazonPrimeActive.value = value }
    fun setAmazonPrimeCost(valStr: String) { _amazonPrimeCost.value = valStr.filter { it.isDigit() || it == '.' } }

    fun setSpotifyActive(value: Boolean) { _spotifyActive.value = value }
    fun setSpotifyCost(valStr: String) { _spotifyCost.value = valStr.filter { it.isDigit() || it == '.' } }

    fun setYoutubePremiumActive(value: Boolean) { _youtubePremiumActive.value = value }
    fun setYoutubePremiumCost(valStr: String) { _youtubePremiumCost.value = valStr.filter { it.isDigit() || it == '.' } }

    fun setYoutubeMusicActive(value: Boolean) { _youtubeMusicActive.value = value }
    fun setYoutubeMusicCost(valStr: String) { _youtubeMusicCost.value = valStr.filter { it.isDigit() || it == '.' } }

    fun addCustomSubscription() {
        val uniqueId = "custom_sub_${UUID.randomUUID()}"
        _customStreams.value = _customStreams.value + WizardCustomStream(uniqueId, "Streaming Nuevo", "10")
    }

    fun updateCustomSubscription(id: String, name: String, amount: String) {
        _customStreams.value = _customStreams.value.map { item ->
            if (item.id == id) item.copy(name = name, amount = amount.filter { it.isDigit() || it == '.' }) else item
        }
    }

    fun deleteCustomSub(id: String) {
        _customStreams.value = _customStreams.value.filter { it.id != id }
    }

    // Complete Wizard Action (Construct Category Schema + Complete Profile)
    fun completeWizard() {
        viewModelScope.launch {
            val inc = _incomeInput.value.toDoubleOrNull() ?: 0.0
            val pCost = _petsCost.value.toDoubleOrNull() ?: 0.0
            val kCost = _kidsCost.value.toDoubleOrNull() ?: 0.0

            val waterVal = _waterCost.value.toDoubleOrNull() ?: 0.0
            val eleVal = _electricityCost.value.toDoubleOrNull() ?: 0.0

            val calendar = java.util.Calendar.getInstance()
            val profile = FinancialProfile(
                id = 1,
                monthlyIncome = inc,
                isWizardComplete = true,
                hasPets = _hasPets.value,
                petsCost = pCost,
                hasKids = _hasKids.value,
                kidsCost = kCost,
                sharedExpenses = _sharedExpenses.value,
                partnerContribution = _partnerContribution.value.toDoubleOrNull() ?: 0.0,
                waterBilling = _waterBilling.value,
                waterCost = waterVal,
                isWaterAdded = _isWaterEnabled.value,
                electricityBilling = _electricityBilling.value,
                electricityCost = eleVal,
                isElectricityAdded = _isElectricityEnabled.value,
                currentBankBalance = _wizardBankBalanceInput.value.toDoubleOrNull() ?: -1.0,
                lastActiveYear = calendar.get(java.util.Calendar.YEAR),
                lastActiveMonth = calendar.get(java.util.Calendar.MONTH) + 1,
                incomeDay = _incomeDayInput.value.toIntOrNull() ?: 1
            )

            // Compile Category List
            val categoriesToInsert = mutableListOf<ExpenseCategory>()

            // 1. Wizard Fixed Obligations
            _fixedExpenses.value.forEach { fe ->
                val amt = fe.amount.toDoubleOrNull() ?: 0.0
                val pd = fe.payDay.toIntOrNull()?.coerceIn(1, 31)
                categoriesToInsert.add(
                    ExpenseCategory(
                        id = fe.id,
                        name = fe.name,
                        limitAmount = amt,
                        isFixed = true,
                        isPaid = false,
                        rawAmount = amt,
                        payDay = pd
                    )
                )
            }

            // 2. Wizard Variable Budgets
            _variableBudgets.value.forEach { vb ->
                val amt = vb.amount.toDoubleOrNull() ?: 0.0
                categoriesToInsert.add(
                    ExpenseCategory(
                        id = vb.id,
                        name = vb.name,
                        limitAmount = amt,
                        isFixed = false,
                        rawAmount = amt
                    )
                )
            }

            // 3. Pets Extra
            if (_hasPets.value) {
                categoriesToInsert.add(
                    ExpenseCategory(
                        id = "mascotas",
                        name = "Mascotas",
                        limitAmount = pCost,
                        isFixed = true,
                        isPaid = false,
                        isFromWizardExtra = true,
                        rawAmount = pCost
                    )
                )
            }

            // 4. Kids Extra
            if (_hasKids.value) {
                categoriesToInsert.add(
                    ExpenseCategory(
                        id = "hijos",
                        name = "Hijos/Bebés",
                        limitAmount = kCost,
                        isFixed = true,
                        isPaid = false,
                        isFromWizardExtra = true,
                        rawAmount = kCost
                    )
                )
            }

            // 5. Electricity Predefined Supply
            if (_isElectricityEnabled.value) {
                val cycle = _electricityBilling.value
                val limit = if (cycle == "Bimensual") eleVal / 2.0 else eleVal
                val pd = _electricityPayDayValue.value.toIntOrNull()?.coerceIn(1, 31)
                categoriesToInsert.add(
                    ExpenseCategory(
                        id = "luz",
                        name = "Suministro Luz",
                        limitAmount = limit,
                        isFixed = true,
                        isPaid = false,
                        isFromWizardExtra = true,
                        billingCycle = cycle,
                        rawAmount = eleVal,
                        isVariableBill = true,
                        payDay = pd
                    )
                )
            }

            // 6. Water Predefined Supply
            if (_isWaterEnabled.value) {
                val cycle = _waterBilling.value
                val limit = if (cycle == "Bimensual") waterVal / 2.0 else waterVal
                val pd = _waterPayDayValue.value.toIntOrNull()?.coerceIn(1, 31)
                categoriesToInsert.add(
                    ExpenseCategory(
                        id = "agua",
                        name = "Suministro Agua",
                        limitAmount = limit,
                        isFixed = true,
                        isPaid = false,
                        isFromWizardExtra = true,
                        billingCycle = cycle,
                        rawAmount = waterVal,
                        isVariableBill = true,
                        payDay = pd
                    )
                )
            }

            // 7. Streaming / Subscriptions Info
            if (_netflixActive.value) {
                val amt = _netflixCost.value.toDoubleOrNull() ?: 0.0
                categoriesToInsert.add(
                    ExpenseCategory(
                        id = "netflix",
                        name = "Netflix",
                        limitAmount = amt,
                        isFixed = true,
                        isPaid = false,
                        isFromWizardExtra = true,
                        rawAmount = amt
                    )
                )
            }
            if (_hboActive.value) {
                val amt = _hboCost.value.toDoubleOrNull() ?: 0.0
                categoriesToInsert.add(
                    ExpenseCategory(
                        id = "hbo_max",
                        name = "HBO Max",
                        limitAmount = amt,
                        isFixed = true,
                        isPaid = false,
                        isFromWizardExtra = true,
                        rawAmount = amt
                    )
                )
            }
            if (_disneyActive.value) {
                val amt = _disneyCost.value.toDoubleOrNull() ?: 0.0
                categoriesToInsert.add(
                    ExpenseCategory(
                        id = "disney_plus",
                        name = "Disney+",
                        limitAmount = amt,
                        isFixed = true,
                        isPaid = false,
                        isFromWizardExtra = true,
                        rawAmount = amt
                    )
                )
            }
            if (_juegosActive.value) {
                val amt = _juegosCost.value.toDoubleOrNull() ?: 0.0
                categoriesToInsert.add(
                    ExpenseCategory(
                        id = "juegos",
                        name = "Suscripción Juegos",
                        limitAmount = amt,
                        isFixed = true,
                        isPaid = false,
                        isFromWizardExtra = true,
                        rawAmount = amt
                    )
                )
            }

            if (_amazonPrimeActive.value) {
                val amt = _amazonPrimeCost.value.toDoubleOrNull() ?: 0.0
                categoriesToInsert.add(
                    ExpenseCategory(
                        id = "amazon_prime",
                        name = "Amazon Prime",
                        limitAmount = amt,
                        isFixed = true,
                        isPaid = false,
                        isFromWizardExtra = true,
                        rawAmount = amt
                    )
                )
            }
            if (_spotifyActive.value) {
                val amt = _spotifyCost.value.toDoubleOrNull() ?: 0.0
                categoriesToInsert.add(
                    ExpenseCategory(
                        id = "spotify",
                        name = "Spotify",
                        limitAmount = amt,
                        isFixed = true,
                        isPaid = false,
                        isFromWizardExtra = true,
                        rawAmount = amt
                    )
                )
            }
            if (_youtubePremiumActive.value) {
                val amt = _youtubePremiumCost.value.toDoubleOrNull() ?: 0.0
                categoriesToInsert.add(
                    ExpenseCategory(
                        id = "youtube_premium",
                        name = "YouTube Premium",
                        limitAmount = amt,
                        isFixed = true,
                        isPaid = false,
                        isFromWizardExtra = true,
                        rawAmount = amt
                    )
                )
            }
            if (_youtubeMusicActive.value) {
                val amt = _youtubeMusicCost.value.toDoubleOrNull() ?: 0.0
                categoriesToInsert.add(
                    ExpenseCategory(
                        id = "youtube_music",
                        name = "YouTube Music",
                        limitAmount = amt,
                        isFixed = true,
                        isPaid = false,
                        isFromWizardExtra = true,
                        rawAmount = amt
                    )
                )
            }

            // 8. Custom Subscriptions Added in step 4
            _customStreams.value.forEach { cs ->
                val amt = cs.amount.toDoubleOrNull() ?: 0.0
                categoriesToInsert.add(
                    ExpenseCategory(
                        id = cs.id,
                        name = cs.name,
                        limitAmount = amt,
                        isFixed = true,
                        isPaid = false,
                        isFromWizardExtra = true,
                        rawAmount = amt
                    )
                )
            }

            storagePurgeAndRepopulate(profile, categoriesToInsert)
        }
    }

    private suspend fun storagePurgeAndRepopulate(profile: FinancialProfile, categories: List<ExpenseCategory>) {
        repository.resetAllData()
        repository.saveFinancialProfile(profile)
        repository.saveCategories(categories)
        runSmartAutoCheck()
        _currentScreen.value = Screen.Dashboard
    }

    // Interactive Dashboard Controls - real-time calculations as requested!
    fun setGastoFijoPaid(id: String, paid: Boolean) {
        viewModelScope.launch {
            repository.setCategoryPaid(id, paid)
        }
    }

    // Accordion Adjuster: toggling pets in dashboard
    fun togglePetsDashboard(active: Boolean) {
        _hasPets.value = active
        viewModelScope.launch {
            val prof = repository.getProfileDirect() ?: return@launch
            val updatedProfile = prof.copy(hasPets = active)
            repository.saveFinancialProfile(updatedProfile)

            if (active) {
                val cost = _petsCost.value.toDoubleOrNull() ?: 30.0
                repository.saveCategory(
                    ExpenseCategory(
                        id = "mascotas",
                        name = "Mascotas",
                        limitAmount = cost,
                        isFixed = true,
                        rawAmount = cost,
                        isFromWizardExtra = true
                    )
                )
            } else {
                repository.deleteCategory("mascotas")
            }
        }
    }

    fun updatePetsCostDashboard(costStr: String) {
        val filtered = costStr.filter { it.isDigit() || it == '.' }
        _petsCost.value = filtered
        val price = filtered.toDoubleOrNull() ?: 30.0
        if (_hasPets.value) {
            viewModelScope.launch {
                val prof = repository.getProfileDirect() ?: return@launch
                repository.saveFinancialProfile(prof.copy(petsCost = price))

                val currentCategory = repository.getAllCategoriesDirect().find { it.id == "mascotas" }
                val isPaid = currentCategory?.isPaid ?: false
                repository.saveCategory(
                    ExpenseCategory(
                        id = "mascotas",
                        name = "Mascotas",
                        limitAmount = price,
                        isFixed = true,
                        isPaid = isPaid,
                        rawAmount = price,
                        isFromWizardExtra = true
                    )
                )
            }
        }
    }

    // Accordion Adjuster: toggling kids in dashboard
    fun toggleKidsDashboard(active: Boolean) {
        _hasKids.value = active
        viewModelScope.launch {
            val prof = repository.getProfileDirect() ?: return@launch
            val updatedProfile = prof.copy(hasKids = active)
            repository.saveFinancialProfile(updatedProfile)

            if (active) {
                val cost = _kidsCost.value.toDoubleOrNull() ?: 150.0
                repository.saveCategory(
                    ExpenseCategory(
                        id = "hijos",
                        name = "Hijos/Bebés",
                        limitAmount = cost,
                        isFixed = true,
                        rawAmount = cost,
                        isFromWizardExtra = true
                    )
                )
            } else {
                repository.deleteCategory("hijos")
            }
        }
    }

    fun updateKidsCostDashboard(costStr: String) {
        val filtered = costStr.filter { it.isDigit() || it == '.' }
        _kidsCost.value = filtered
        val price = filtered.toDoubleOrNull() ?: 150.0
        if (_hasKids.value) {
            viewModelScope.launch {
                val prof = repository.getProfileDirect() ?: return@launch
                repository.saveFinancialProfile(prof.copy(kidsCost = price))

                val currentCategory = repository.getAllCategoriesDirect().find { it.id == "hijos" }
                val isPaid = currentCategory?.isPaid ?: false
                repository.saveCategory(
                    ExpenseCategory(
                        id = "hijos",
                        name = "Hijos/Bebés",
                        limitAmount = price,
                        isFixed = true,
                        isPaid = isPaid,
                        rawAmount = price,
                        isFromWizardExtra = true
                    )
                )
            }
        }
    }

    fun toggleSharedExpensesDashboard(active: Boolean) {
        _sharedExpenses.value = active
        viewModelScope.launch {
            val prof = repository.getProfileDirect() ?: return@launch
            repository.saveFinancialProfile(prof.copy(sharedExpenses = active))
        }
    }

    fun updatePartnerContributionDashboard(contribStr: String) {
        val filtered = contribStr.filter { it.isDigit() || it == '.' }
        _partnerContribution.value = filtered
        val contrib = filtered.toDoubleOrNull() ?: 0.0
        viewModelScope.launch {
            val prof = repository.getProfileDirect() ?: return@launch
            repository.saveFinancialProfile(prof.copy(partnerContribution = contrib))
        }
    }

    fun toggleCategoryAssumedByPartner(id: String, assumed: Boolean) {
        viewModelScope.launch {
            val existed = repository.getAllCategoriesDirect().find { it.id == id } ?: return@launch
            repository.saveCategory(existed.copy(assumedByPartner = assumed))
        }
    }

    // Predefined supplies billing cycles changes
    fun toggleWaterSupplyDashboard(active: Boolean) {
        _isWaterEnabled.value = active
        viewModelScope.launch {
            val prof = repository.getProfileDirect() ?: return@launch
            repository.saveFinancialProfile(prof.copy(isWaterAdded = active))
            if (active) {
                val cost = _waterCost.value.toDoubleOrNull() ?: 40.0
                val cycle = _waterBilling.value
                val limit = if (cycle == "Bimensual") cost / 2.0 else cost
                repository.saveCategory(
                    ExpenseCategory(
                        id = "agua",
                        name = "Suministro Agua",
                        limitAmount = limit,
                        isFixed = true,
                        isFromWizardExtra = true,
                        billingCycle = cycle,
                        rawAmount = cost
                    )
                )
            } else {
                repository.deleteCategory("agua")
            }
        }
    }

    fun updateWaterBillingCycleDashboard(cycle: String) {
        _waterBilling.value = cycle
        if (_isWaterEnabled.value) {
            viewModelScope.launch {
                val prof = repository.getProfileDirect() ?: return@launch
                repository.saveFinancialProfile(prof.copy(waterBilling = cycle))
                val cost = _waterCost.value.toDoubleOrNull() ?: 40.0
                val limit = if (cycle == "Bimensual") cost / 2.0 else cost

                val currentCategory = repository.getAllCategoriesDirect().find { it.id == "agua" }
                val isPaid = currentCategory?.isPaid ?: false
                repository.saveCategory(
                    ExpenseCategory(
                        id = "agua",
                        name = "Suministro Agua",
                        limitAmount = limit,
                        isFixed = true,
                        isPaid = isPaid,
                        isFromWizardExtra = true,
                        billingCycle = cycle,
                        rawAmount = cost
                    )
                )
            }
        }
    }

    fun updateWaterCostDashboard(costStr: String) {
        val filtered = costStr.filter { it.isDigit() || it == '.' }
        _waterCost.value = filtered
        val cost = filtered.toDoubleOrNull() ?: 40.0
        if (_isWaterEnabled.value) {
            viewModelScope.launch {
                val prof = repository.getProfileDirect() ?: return@launch
                repository.saveFinancialProfile(prof.copy(waterCost = cost))
                val cycle = _waterBilling.value
                val limit = if (cycle == "Bimensual") cost / 2.0 else cost

                val currentCategory = repository.getAllCategoriesDirect().find { it.id == "agua" }
                val isPaid = currentCategory?.isPaid ?: false
                repository.saveCategory(
                    ExpenseCategory(
                        id = "agua",
                        name = "Suministro Agua",
                        limitAmount = limit,
                        isFixed = true,
                        isPaid = isPaid,
                        isFromWizardExtra = true,
                        billingCycle = cycle,
                        rawAmount = cost
                    )
                )
            }
        }
    }

    // Electricity Predefined supply changes
    fun toggleElectricitySupplyDashboard(active: Boolean) {
        _isElectricityEnabled.value = active
        viewModelScope.launch {
            val prof = repository.getProfileDirect() ?: return@launch
            repository.saveFinancialProfile(prof.copy(isElectricityAdded = active))
            if (active) {
                val cost = _electricityCost.value.toDoubleOrNull() ?: 60.0
                val cycle = _electricityBilling.value
                val limit = if (cycle == "Bimensual") cost / 2.0 else cost
                repository.saveCategory(
                    ExpenseCategory(
                        id = "luz",
                        name = "Suministro Luz",
                        limitAmount = limit,
                        isFixed = true,
                        isFromWizardExtra = true,
                        billingCycle = cycle,
                        rawAmount = cost
                    )
                )
            } else {
                repository.deleteCategory("luz")
            }
        }
    }

    fun updateElectricityBillingCycleDashboard(cycle: String) {
        _electricityBilling.value = cycle
        if (_isElectricityEnabled.value) {
            viewModelScope.launch {
                val prof = repository.getProfileDirect() ?: return@launch
                repository.saveFinancialProfile(prof.copy(electricityBilling = cycle))
                val cost = _electricityCost.value.toDoubleOrNull() ?: 60.0
                val limit = if (cycle == "Bimensual") cost / 2.0 else cost

                val currentCategory = repository.getAllCategoriesDirect().find { it.id == "luz" }
                val isPaid = currentCategory?.isPaid ?: false
                repository.saveCategory(
                    ExpenseCategory(
                        id = "luz",
                        name = "Suministro Luz",
                        limitAmount = limit,
                        isFixed = true,
                        isPaid = isPaid,
                        isFromWizardExtra = true,
                        billingCycle = cycle,
                        rawAmount = cost
                    )
                )
            }
        }
    }

    fun updateElectricityCostDashboard(costStr: String) {
        val filtered = costStr.filter { it.isDigit() || it == '.' }
        _electricityCost.value = filtered
        val cost = filtered.toDoubleOrNull() ?: 60.0
        if (_isElectricityEnabled.value) {
            viewModelScope.launch {
                val prof = repository.getProfileDirect() ?: return@launch
                repository.saveFinancialProfile(prof.copy(electricityCost = cost))
                val cycle = _electricityBilling.value
                val limit = if (cycle == "Bimensual") cost / 2.0 else cost

                val currentCategory = repository.getAllCategoriesDirect().find { it.id == "luz" }
                val isPaid = currentCategory?.isPaid ?: false
                repository.saveCategory(
                    ExpenseCategory(
                        id = "luz",
                        name = "Suministro Luz",
                        limitAmount = limit,
                        isFixed = true,
                        isPaid = isPaid,
                        isFromWizardExtra = true,
                        billingCycle = cycle,
                        rawAmount = cost
                    )
                )
            }
        }
    }

    // Popular Streaming subscriptions changes dashboard
    fun toggleStreamingDashboard(id: String, active: Boolean, defaultName: String, priceStr: String) {
        when (id) {
            "netflix" -> _netflixActive.value = active
            "hbo_max" -> _hboActive.value = active
            "disney_plus" -> _disneyActive.value = active
            "juegos" -> _juegosActive.value = active
            "amazon_prime" -> _amazonPrimeActive.value = active
            "spotify" -> _spotifyActive.value = active
            "youtube_premium" -> _youtubePremiumActive.value = active
            "youtube_music" -> _youtubeMusicActive.value = active
        }

        val amt = priceStr.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
        viewModelScope.launch {
            if (active) {
                repository.saveCategory(
                    ExpenseCategory(
                        id = id,
                        name = defaultName,
                        limitAmount = amt,
                        isFixed = true,
                        isFromWizardExtra = true,
                        rawAmount = amt
                    )
                )
            } else {
                repository.deleteCategory(id)
            }
        }
    }

    fun removeStreamingDashboard(id: String) {
        when (id) {
            "netflix" -> _netflixActive.value = false
            "hbo_max" -> _hboActive.value = false
            "disney_plus" -> _disneyActive.value = false
            "juegos" -> _juegosActive.value = false
            "amazon_prime" -> _amazonPrimeActive.value = false
            "spotify" -> _spotifyActive.value = false
            "youtube_premium" -> _youtubePremiumActive.value = false
            "youtube_music" -> _youtubeMusicActive.value = false
        }
        viewModelScope.launch {
            repository.deleteCategory(id)
        }
    }

    fun updateStreamingCostDashboard(id: String, priceStr: String, defaultName: String) {
        val filtered = priceStr.filter { it.isDigit() || it == '.' }
        when (id) {
            "netflix" -> _netflixCost.value = filtered
            "hbo_max" -> _hboCost.value = filtered
            "disney_plus" -> _disneyCost.value = filtered
            "juegos" -> _juegosCost.value = filtered
            "amazon_prime" -> _amazonPrimeCost.value = filtered
            "spotify" -> _spotifyCost.value = filtered
            "youtube_premium" -> _youtubePremiumCost.value = filtered
            "youtube_music" -> _youtubeMusicCost.value = filtered
        }

        val amt = filtered.toDoubleOrNull() ?: 0.0
        viewModelScope.launch {
            val currentCategory = repository.getAllCategoriesDirect().find { it.id == id }
            val isPaid = currentCategory?.isPaid ?: false
            repository.saveCategory(
                ExpenseCategory(
                    id = id,
                    name = defaultName,
                    limitAmount = amt,
                    isFixed = true,
                    isPaid = isPaid,
                    isFromWizardExtra = true,
                    rawAmount = amt
                )
            )
        }
    }

    fun addNewCustomSubDashboard(name: String, priceStr: String) {
        val uniqueId = "custom_sub_${UUID.randomUUID()}"
        val filtered = priceStr.filter { it.isDigit() || it == '.' }
        val amt = filtered.toDoubleOrNull() ?: 0.0
        viewModelScope.launch {
            repository.saveCategory(
                ExpenseCategory(
                    id = uniqueId,
                    name = name.ifBlank { "Suscripción" },
                    limitAmount = amt,
                    isFixed = true,
                    isFromWizardExtra = true,
                    rawAmount = amt
                )
            )
        }
    }

    // Variable spent inputs
    fun recordVariableExpense(categoryName: String, amountVal: Double) {
        val catId = categoryName.lowercase().trim().replace(" ", "_")
        viewModelScope.launch {
            repository.addVariableExpense(
                VariableExpenseEntry(
                    categoryId = catId,
                    categoryName = categoryName,
                    amount = amountVal
                )
            )
        }
    }

    fun deleteVariableSpentEntry(id: Int) {
        viewModelScope.launch {
            repository.deleteVariableExpense(id)
        }
    }

    // Force system database wipe and return to step 1 of wizard!
    fun resetSystemConfiguration() {
        viewModelScope.launch {
            repository.resetAllData()
            // Reset wizard variables to original recommendations
            _incomeInput.value = ""
            _fixedExpenses.value = listOf(
                WizardFixedExpense("alquiler", "Alquiler", "600"),
                WizardFixedExpense("gimnasio", "Gimnasio", "30"),
                WizardFixedExpense("internet", "Internet", "40"),
                WizardFixedExpense("tarjeta_credito", "Tarjeta de Crédito", "50")
            )
            _variableBudgets.value = listOf(
                WizardVariableBudget("comida", "Comida", "250"),
                WizardVariableBudget("transporte", "Transporte", "100"),
                WizardVariableBudget("ocio", "Ocio", "150")
            )
            _hasPets.value = false
            _petsCost.value = "30"
            _hasKids.value = false
            _kidsCost.value = "150"
            _sharedExpenses.value = false
            _waterBilling.value = "Mensual"
            _waterCost.value = "40"
            _isWaterEnabled.value = true
            _electricityBilling.value = "Mensual"
            _electricityCost.value = "60"
            _isElectricityEnabled.value = true
            _netflixActive.value = false
            _netflixCost.value = "15"
            _hboActive.value = false
            _hboCost.value = "10"
            _disneyActive.value = false
            _disneyCost.value = "11"
            _juegosActive.value = false
            _juegosCost.value = "20"
            _amazonPrimeActive.value = false
            _amazonPrimeCost.value = "5"
            _spotifyActive.value = false
            _spotifyCost.value = "11"
            _youtubePremiumActive.value = false
            _youtubePremiumCost.value = "14"
            _youtubeMusicActive.value = false
            _youtubeMusicCost.value = "10"
            _customStreams.value = emptyList()

            // Navigate back
            _currentScreen.value = Screen.WizardStep1
        }
    }

    fun setHideNewMonthBanner(hide: Boolean) {
        _hideNewMonthBanner.value = hide
        viewModelScope.launch {
            val prof = repository.getProfileDirect() ?: return@launch
            repository.saveFinancialProfile(prof.copy(hideNewMonthBanner = hide))
        }
    }

    fun setHideSmartCalendarBanner(hide: Boolean) {
        _hideSmartCalendarBanner.value = hide
        viewModelScope.launch {
            val prof = repository.getProfileDirect() ?: return@launch
            repository.saveFinancialProfile(prof.copy(hideSmartCalendarBanner = hide))
        }
    }

    fun setSelectedTheme(theme: String) {
        viewModelScope.launch {
            val prof = repository.getProfileDirect() ?: return@launch
            repository.saveFinancialProfile(prof.copy(selectedTheme = theme))
        }
    }

    fun setSelectedIcon(icon: String) {
        viewModelScope.launch {
            val prof = repository.getProfileDirect() ?: return@launch
            repository.saveFinancialProfile(prof.copy(selectedIcon = icon))
        }
    }

    fun setSelectedCurrency(currency: String) {
        viewModelScope.launch {
            val prof = repository.getProfileDirect() ?: return@launch
            repository.saveFinancialProfile(prof.copy(selectedCurrency = currency))
        }
    }

    fun updateProfileIncomeAndDay(income: Double, day: Int) {
        viewModelScope.launch {
            val prof = repository.getProfileDirect() ?: return@launch
            repository.saveFinancialProfile(prof.copy(monthlyIncome = income, incomeDay = day))
        }
    }

    fun setProUserStatus(isPro: Boolean) {
        viewModelScope.launch {
            val prof = repository.getProfileDirect() ?: FinancialProfile()
            repository.saveFinancialProfile(prof.copy(isProUser = true))
        }
    }
}
