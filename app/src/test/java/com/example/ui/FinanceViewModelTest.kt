package com.example.ui

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.ExpenseCategory
import com.example.data.FinanceDatabase
import com.example.data.FinanceRepository
import com.example.data.FinancialProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FinanceViewModelTest {

    private lateinit var database: FinanceDatabase
    private lateinit var repository: FinanceRepository
    private lateinit var viewModel: FinanceViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FinanceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FinanceRepository(database.financeDao())
        viewModel = FinanceViewModel(repository, database)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        database.close()
    }

    @Test
    fun `calculo saldo disponible real resta fijos impagados e incluye pago en efectivo`() = runTest {
        backgroundScope.launch { viewModel.saldoRestanteDisponible.collect {} }
        backgroundScope.launch { viewModel.dbCategories.collect {} }
        backgroundScope.launch { viewModel.dbProfile.collect {} }

        // Given: Saldo en banco de 968.80 €, Renta en efectivo de 380.00 € impagada, y Recibo Luz de 60.00 € impagado
        val profile = FinancialProfile(
            id = 1,
            currentBankBalance = 968.80,
            monthlyIncome = 2000.0,
            isWizardComplete = true
        )
        repository.saveFinancialProfile(profile)

        val categories = listOf(
            ExpenseCategory(
                id = "renta",
                name = "Renta / Alquiler",
                limitAmount = 380.0,
                isFixed = true,
                isPaid = false,
                isCashPayment = true,
                isAdded = true
            ),
            ExpenseCategory(
                id = "luz",
                name = "Luz",
                limitAmount = 60.0,
                isFixed = true,
                isPaid = false,
                isCashPayment = false,
                isAdded = true
            )
        )
        repository.saveCategories(categories)
        advanceUntilIdle()

        viewModel.dbProfile.first { it?.currentBankBalance == 968.80 }
        viewModel.dbCategories.first { it.size == 2 }

        // Expected available balance: 968.80 - 380.00 - 60.00 = 528.80
        val saldoDisponible = viewModel.saldoRestanteDisponible.first { it == 528.80 }
        assertEquals(528.80, saldoDisponible, 0.01)
    }

    @Test
    fun `comportamiento flag isPaid mantiene saldo disponible estable y descuenta de banco`() = runTest {
        backgroundScope.launch { viewModel.saldoRestanteDisponible.collect {} }
        backgroundScope.launch { viewModel.dbCategories.collect {} }
        backgroundScope.launch { viewModel.dbProfile.collect {} }

        // Given: Saldo en banco de 968.80 €, Renta de 380.00 € impagada
        val profile = FinancialProfile(
            id = 1,
            currentBankBalance = 968.80,
            monthlyIncome = 2000.0,
            isWizardComplete = true
        )
        repository.saveFinancialProfile(profile)

        val categories = listOf(
            ExpenseCategory(
                id = "renta",
                name = "Renta / Alquiler",
                limitAmount = 380.0,
                isFixed = true,
                isPaid = false,
                isCashPayment = true,
                isAdded = true
            )
        )
        repository.saveCategories(categories)
        advanceUntilIdle()

        viewModel.dbProfile.first { it?.currentBankBalance == 968.80 }
        viewModel.dbCategories.first { it.size == 1 }

        val saldoInicial = viewModel.saldoRestanteDisponible.first { it == 588.80 }
        assertEquals(588.80, saldoInicial, 0.01)

        // When: Marcar la factura como pagada
        viewModel.setGastoFijoPaid("renta", true)
        advanceUntilIdle()

        // Wait for profile and category updates in ViewModel StateFlows
        viewModel.dbProfile.first { it?.currentBankBalance == 588.80 }
        viewModel.dbCategories.first { catList -> catList.find { it.id == "renta" }?.isPaid == true }

        // Then:
        // 1. La factura está marcada como pagada
        val updatedCategory = repository.getAllCategoriesDirect().find { it.id == "renta" }
        assertEquals(true, updatedCategory?.isPaid)

        // 2. El saldo de banco se reduce automáticamente de 968.80 a 588.80
        val updatedProfile = repository.getProfileDirect()
        assertEquals(588.80, updatedProfile?.currentBankBalance ?: 0.0, 0.01)

        // 3. El saldo disponible real se mantiene estable en 588.80
        val saldoTrasPago = viewModel.saldoRestanteDisponible.first { it == 588.80 }
        assertEquals(588.80, saldoTrasPago, 0.01)
    }

    @Test
    fun `independencia de ingresos teoricos cuando modo banco esta activo`() = runTest {
        backgroundScope.launch { viewModel.saldoRestanteDisponible.collect {} }
        backgroundScope.launch { viewModel.dbCategories.collect {} }
        backgroundScope.launch { viewModel.dbProfile.collect {} }

        // Given: Saldo de banco activo en 1000.0 €, pero ingresos teoricos de 3000 € e ingresos de pareja de 1500 €
        val profile = FinancialProfile(
            id = 1,
            currentBankBalance = 1000.0,
            monthlyIncome = 3000.0,
            partnerContribution = 1500.0,
            isWizardComplete = true
        )
        repository.saveFinancialProfile(profile)

        val categories = listOf(
            ExpenseCategory(
                id = "internet",
                name = "Internet",
                limitAmount = 50.0,
                isFixed = true,
                isPaid = false,
                isAdded = true
            )
        )
        repository.saveCategories(categories)
        advanceUntilIdle()

        viewModel.dbProfile.first { it?.currentBankBalance == 1000.0 }
        viewModel.dbCategories.first { it.size == 1 }

        // Expected: Base value = 1000.0 (currentBankBalance), NOT 3000 + 1500 = 4500.
        // Expected available = 1000.0 - 50.0 = 950.0
        val saldoDisponible = viewModel.saldoRestanteDisponible.first { it == 950.0 }
        assertEquals(950.0, saldoDisponible, 0.01)
    }

    @Test
    fun `campo isArchived en ExpenseCategory y soporte en DAO y Repository`() = runTest {
        // Given: Categoría no archivada por defecto (isArchived = false)
        val category = ExpenseCategory(
            id = "gimnasio_antiguo",
            name = "Gimnasio Antiguo",
            limitAmount = 35.0,
            isFixed = true,
            isPaid = false,
            isAdded = true
        )
        assertEquals(false, category.isArchived)

        repository.saveCategory(category)
        advanceUntilIdle()

        // Verify initial state
        val savedCategory = repository.getAllCategoriesDirect().find { it.id == "gimnasio_antiguo" }
        assertEquals(false, savedCategory?.isArchived)
        assertEquals(0, repository.getArchivedCategoriesDirect().size)

        // When: Archivar la categoría
        repository.archiveCategory("gimnasio_antiguo")
        advanceUntilIdle()

        // Then:
        val archivedCategory = repository.getAllCategoriesDirect().find { it.id == "gimnasio_antiguo" }
        assertEquals(true, archivedCategory?.isArchived)

        val archivedList = repository.getArchivedCategoriesDirect()
        assertEquals(1, archivedList.size)
        assertEquals("gimnasio_antiguo", archivedList[0].id)

        // When: Desarchivar
        repository.unarchiveCategory("gimnasio_antiguo")
        advanceUntilIdle()

        val unarchivedList = repository.getArchivedCategoriesDirect()
        assertEquals(0, unarchivedList.size)
    }

    @Test
    fun `financiacion que llega a su ultima cuota se archiva automaticamente`() = runTest {
        // Given: Financiación con 1 cuota restante
        val financingCategory = ExpenseCategory(
            id = "financiacion_movil",
            name = "Financiación Móvil",
            limitAmount = 50.0,
            isFixed = true,
            isFinancing = true,
            monthsRemaining = 1,
            isPaid = false,
            isAdded = true,
            isArchived = false
        )
        repository.saveCategory(financingCategory)
        advanceUntilIdle()

        val saved = repository.getAllCategoriesDirect().find { it.id == "financiacion_movil" }
        assertEquals(false, saved?.isArchived)
        assertEquals(1, saved?.monthsRemaining)

        // When: Pagar la última cuota a través de payFinancingInstallment
        viewModel.payFinancingInstallment("financiacion_movil").join()
        advanceUntilIdle()

        // Then: La financiación debe quedar con monthsRemaining = 0, isAdded = false y isArchived = true en el Repository
        val updated = repository.getAllCategoriesDirect().find { it.id == "financiacion_movil" }
        assertEquals(true, updated?.isArchived)
        assertEquals(0, updated?.monthsRemaining)
        assertEquals(false, updated?.isAdded)

        val archivedCategories = repository.getArchivedCategoriesDirect()
        assertEquals(1, archivedCategories.size)
        assertEquals("financiacion_movil", archivedCategories[0].id)
    }

    @Test
    fun `startNewMonth archiva automaticamente financiacions con ultima cuota completada`() = runTest {
        // Given: Financiación con 1 cuota restante
        val financing = ExpenseCategory(
            id = "prestamo_ordenador",
            name = "Préstamo Ordenador",
            limitAmount = 100.0,
            isFixed = true,
            isFinancing = true,
            monthsRemaining = 1,
            isPaid = false,
            isAdded = true,
            isArchived = false
        )
        repository.saveCategory(financing)
        advanceUntilIdle()

        // When: Se inicia un nuevo mes
        viewModel.startNewMonth().join()
        advanceUntilIdle()

        // Then: monthsRemaining llega a 0 y pasa a isArchived = true
        val updated = repository.getAllCategoriesDirect().find { it.id == "prestamo_ordenador" }
        assertEquals(true, updated?.isArchived)
        assertEquals(0, updated?.monthsRemaining)
        assertEquals(false, updated?.isAdded)
    }

    @Test
    fun `dbCategories excluye categorias archivadas o financiaciones finalizadas`() = runTest {
        backgroundScope.launch { viewModel.dbCategories.collect {} }
        backgroundScope.launch { viewModel.dbProfile.collect {} }
        repository.saveFinancialProfile(FinancialProfile(id = 1, monthlyIncome = 2000.0))

        val activeFixed = ExpenseCategory(
            id = "internet_fibra",
            name = "Internet Fibra",
            limitAmount = 40.0,
            isFixed = true,
            isAdded = true,
            isArchived = false
        )
        val archivedFinancing = ExpenseCategory(
            id = "financiacion_finalizada",
            name = "Financiación Coche",
            limitAmount = 200.0,
            isFixed = true,
            isFinancing = true,
            monthsRemaining = 0,
            isAdded = false,
            isArchived = true
        )
        repository.saveCategories(listOf(activeFixed, archivedFinancing))
        advanceUntilIdle()

        val activeList = viewModel.dbCategories.first { it.isNotEmpty() }
        assertEquals(1, activeList.size)
        assertEquals("internet_fibra", activeList[0].id)
        assertEquals(false, activeList.any { it.isArchived })
    }

    @Test
    fun `dbArchivedCategories y eliminacion definitiva funcionan correctamente`() = runTest {
        backgroundScope.launch { viewModel.dbArchivedCategories.collect {} }
        backgroundScope.launch { viewModel.dbCategories.collect {} }
        backgroundScope.launch { viewModel.dbProfile.collect {} }
        repository.saveFinancialProfile(FinancialProfile(id = 1, monthlyIncome = 2000.0))

        val archivedFinancing = ExpenseCategory(
            id = "financiacion_liquidada",
            name = "Préstamo Liquidado",
            limitAmount = 150.0,
            isFixed = true,
            isFinancing = true,
            monthsRemaining = 0,
            isAdded = false,
            isArchived = true
        )
        repository.saveCategories(listOf(archivedFinancing))
        advanceUntilIdle()

        // Verify it appears in dbArchivedCategories
        val archivedList = viewModel.dbArchivedCategories.first { it.isNotEmpty() }
        assertEquals(1, archivedList.size)
        assertEquals("financiacion_liquidada", archivedList[0].id)
        assertEquals(true, archivedList[0].isArchived)

        // Delete permanently
        viewModel.deleteArchivedCategoryPermanent("financiacion_liquidada").join()
        advanceUntilIdle()

        val allCategories = repository.getAllCategoriesDirect()
        assertEquals(0, allCategories.size)
    }

    @Test
    fun `logica universal por ciclo filtra facturas dentro de la ventana de cobro dinamica`() = runTest {
        backgroundScope.launch { viewModel.saldoRestanteDisponible.collect {} }
        backgroundScope.launch { viewModel.dbCategories.collect {} }
        backgroundScope.launch { viewModel.dbProfile.collect {} }

        // Simular fecha fija: 12 de septiembre de 2026
        val simulatedToday = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.SEPTEMBER, 12, 10, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        viewModel.setReferenceCalendarForTesting(simulatedToday)

        // Usuario cobra el día 10. Saldo actual en banco: 1500.0 €
        // Ventana dinámica del ciclo: 12-septiembre a 09-octubre
        val profile = FinancialProfile(
            id = 1,
            currentBankBalance = 1500.0,
            monthlyIncome = 2500.0,
            incomeDay = 10,
            isWizardComplete = true
        )
        repository.saveFinancialProfile(profile)

        val categories = listOf(
            // Recibo 1: payDay = 15 (15-sept) -> DENTRO de la ventana -> SE DEBE RESTAR
            ExpenseCategory(
                id = "electricidad",
                name = "Electricidad",
                limitAmount = 100.0,
                isFixed = true,
                isPaid = false,
                payDay = 15,
                isAdded = true
            ),
            // Recibo 2: payDay = 5 (05-oct, antes del cobro del 10) -> DENTRO de la ventana -> SE DEBE RESTAR
            ExpenseCategory(
                id = "seguro",
                name = "Seguro",
                limitAmount = 50.0,
                isFixed = true,
                isPaid = false,
                payDay = 5,
                isAdded = true
            ),
            // Recibo 3: payDay = 10 (10-oct, cae con la nueva nómina) -> FUERA -> NO SE RESTA
            ExpenseCategory(
                id = "alquiler_siguiente",
                name = "Alquiler Siguiente Mes",
                limitAmount = 600.0,
                isFixed = true,
                isPaid = false,
                payDay = 10,
                isAdded = true
            ),
            // Recibo 4: payDay = 11 (11-oct, después de la nómina) -> FUERA -> NO SE RESTA
            ExpenseCategory(
                id = "gimnasio",
                name = "Gimnasio",
                limitAmount = 40.0,
                isFixed = true,
                isPaid = false,
                payDay = 11,
                isAdded = true
            ),
            // Recibo 5: payDay = 15 pero ya pagado (isPaid = true) -> YA PAGADO -> NO SE RESTA
            ExpenseCategory(
                id = "agua_pagada",
                name = "Agua Pagada",
                limitAmount = 30.0,
                isFixed = true,
                isPaid = true,
                payDay = 15,
                isAdded = true
            ),
            // Recibo 6: payDay = null (compromiso activo sin día fijo) -> SE RESTA
            ExpenseCategory(
                id = "suscripcion",
                name = "Suscripción",
                limitAmount = 20.0,
                isFixed = true,
                isPaid = false,
                payDay = null,
                isAdded = true
            )
        )
        repository.saveCategories(categories)
        advanceUntilIdle()

        // Gastos obligatorios del ciclo = 100.0 (electricidad) + 50.0 (seguro) + 20.0 (suscripción) = 170.0 €
        // Saldo Libre Real = 1500.0 - 170.0 = 1330.0 €
        val saldoDisponible = viewModel.saldoRestanteDisponible.first { it == 1330.0 }
        assertEquals(1330.0, saldoDisponible, 0.01)

        // Limpiar simulación de fecha
        viewModel.setReferenceCalendarForTesting(null)
    }

    @Test
    fun `logica universal ventana antes de cobrar en el mismo mes`() = runTest {
        backgroundScope.launch { viewModel.saldoRestanteDisponible.collect {} }
        backgroundScope.launch { viewModel.dbCategories.collect {} }
        backgroundScope.launch { viewModel.dbProfile.collect {} }

        // Simular fecha fija: 05 de septiembre de 2026
        val simulatedToday = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.SEPTEMBER, 5, 10, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        viewModel.setReferenceCalendarForTesting(simulatedToday)

        // Usuario cobra el día 10. Saldo actual en banco: 800.0 €
        // Ventana dinámica: 05-septiembre a 09-septiembre
        val profile = FinancialProfile(
            id = 1,
            currentBankBalance = 800.0,
            monthlyIncome = 2000.0,
            incomeDay = 10,
            isWizardComplete = true
        )
        repository.saveFinancialProfile(profile)

        val categories = listOf(
            // Recibo con payDay = 7 -> DENTRO de la ventana (05 a 09 sept) -> SE RESTA
            ExpenseCategory(
                id = "recibo_7",
                name = "Recibo Día 7",
                limitAmount = 70.0,
                isFixed = true,
                isPaid = false,
                payDay = 7,
                isAdded = true
            ),
            // Recibo con payDay = 15 -> Se pagará tras cobrar el día 10 -> NO SE RESTA AHORA
            ExpenseCategory(
                id = "recibo_15",
                name = "Recibo Día 15",
                limitAmount = 150.0,
                isFixed = true,
                isPaid = false,
                payDay = 15,
                isAdded = true
            )
        )
        repository.saveCategories(categories)
        advanceUntilIdle()

        // Saldo Libre Real = 800.0 - 70.0 = 730.0 €
        val saldoDisponible = viewModel.saldoRestanteDisponible.first { it == 730.0 }
        assertEquals(730.0, saldoDisponible, 0.01)

        viewModel.setReferenceCalendarForTesting(null)
    }

    @Test
    fun `isBillInCycleWindow evalua correctamente fronteras del ciclo temporal`() {
        val calSept12 = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.SEPTEMBER, 12, 0, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val incomeDay = 10

        // Ventana: 12-sept a 09-oct
        assertEquals(true, FinanceViewModel.isBillInCycleWindow(12, incomeDay, calSept12)) // Hoy
        assertEquals(true, FinanceViewModel.isBillInCycleWindow(15, incomeDay, calSept12)) // 15-sept
        assertEquals(true, FinanceViewModel.isBillInCycleWindow(5, incomeDay, calSept12))  // 05-oct
        assertEquals(true, FinanceViewModel.isBillInCycleWindow(9, incomeDay, calSept12))  // 09-oct (último día)
        assertEquals(false, FinanceViewModel.isBillInCycleWindow(10, incomeDay, calSept12)) // 10-oct (nuevo cobro)
        assertEquals(false, FinanceViewModel.isBillInCycleWindow(11, incomeDay, calSept12)) // 11-oct (post cobro)
        assertEquals(true, FinanceViewModel.isBillInCycleWindow(null, incomeDay, calSept12)) // Sin payDay
    }
}
