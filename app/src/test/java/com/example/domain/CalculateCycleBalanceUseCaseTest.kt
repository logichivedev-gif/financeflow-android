package com.example.domain

import com.example.data.ExpenseCategory
import com.example.data.FinancialProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.Calendar

@OptIn(CriticalFinancialEngine::class)
class CalculateCycleBalanceUseCaseTest {

    private val useCase = CalculateCycleBalanceUseCase()

    @Test
    fun `use case calcula compromisos de ciclo universalmente para cualquier dia de cobro`() {
        // Usuario que cobra el 28 de cada mes.
        // Fecha actual: 15 de marzo. Fin de ciclo: 27 de marzo (Día_Cobro - 1)
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.MARCH, 15, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val categories = listOf(
            // Vence el 20 de marzo (antes del 28) y no pagado -> DENTRO DEL CICLO
            ExpenseCategory(id = "1", name = "Gimnasio", limitAmount = 40.0, isFixed = true, isPaid = false, payDay = 20),
            // Vence el 25 de marzo (antes del 28) y pagado -> NO PENDIENTE
            ExpenseCategory(id = "2", name = "Agua", limitAmount = 50.0, isFixed = true, isPaid = true, payDay = 25),
            // Vence el 29 de marzo (después del cobro del 28) -> FUERA DE CICLO
            ExpenseCategory(id = "3", name = "Seguro", limitAmount = 100.0, isFixed = true, isPaid = false, payDay = 29)
        )

        val pending = useCase.getPendingExpensesForCycle(categories, incomeDay = 28, currentDate = cal)
        assertEquals(40.0, pending, 0.001)
    }

    @Test
    fun `use case maneja meses cortos como febrero con cobros a dia 31 sin excepciones`() {
        // Cobro el 31, fecha actual: 10 de febrero. Próximo cobro: 28 de febrero.
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.FEBRUARY, 10, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val categories = listOf(
            // Recibo el día 27 de febrero
            ExpenseCategory(id = "1", name = "Luz", limitAmount = 80.0, isFixed = true, isPaid = false, payDay = 27),
            // Recibo el día 31 (se ajusta a max 28 en feb, fin de ciclo es 27 -> cae en próximo cobro)
            ExpenseCategory(id = "2", name = "Alquiler", limitAmount = 700.0, isFixed = true, isPaid = false, payDay = 31)
        )

        val pending = useCase.getPendingExpensesForCycle(categories, incomeDay = 31, currentDate = cal)
        assertEquals(80.0, pending, 0.001)
    }

    @Test
    fun `use case calcula saldo libre real correctamente descontando fijos del saldo de banco`() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 16, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val profile = FinancialProfile(
            incomeDay = 10,
            currentBankBalance = 884.0,
            monthlyIncome = 1500.0
        )

        val categories = listOf(
            ExpenseCategory(id = "1", name = "Alquiler Oct", limitAmount = 380.0, isFixed = true, isPaid = true, payDay = 5),
            ExpenseCategory(id = "2", name = "Prestamo Oct", limitAmount = 162.76, isFixed = true, isPaid = true, payDay = 1),
            ExpenseCategory(id = "3", name = "Luz Sept", limitAmount = 135.45, isFixed = true, isPaid = false, payDay = 25),
            ExpenseCategory(id = "4", name = "Cofidis Oct", limitAmount = 100.0, isFixed = true, isPaid = true, payDay = 4),
            ExpenseCategory(id = "5", name = "Tarjeta Oct", limitAmount = 116.59, isFixed = true, isPaid = true, payDay = 5)
        )

        // Compromisos totales = 894.80 €
        // Saldo Libre = 884.0 - 894.80 = -10.80 €
        val saldoLibre = useCase(currentBank = 884.0, profile = profile, categories = categories, currentDate = cal)
        assertEquals(-10.80, saldoLibre, 0.01)
    }
}
