package com.example.domain

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "Esta API forma parte del núcleo crítico del motor de tesorería y liquidez de FinanceFlow. Cualquier alteración de su lógica matemática o reglas de ciclo puede ocasionar discrepancias financieras y desajustes de saldo. Requiere consentimiento explícito (@OptIn(CriticalFinancialEngine::class))."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class CriticalFinancialEngine
