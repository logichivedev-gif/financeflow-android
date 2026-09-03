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
}
