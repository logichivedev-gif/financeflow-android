# 🚀 FinanceFlow - Notas de la Versión v2.2.0

¡Gran actualización acumulativa con mejoras clave en automatización, seguimiento financiero y diseño adaptativo!

---

### 💳 1. Financiaciones y Préstamos Inteligentes (Klarna, Openbank, etc.)
- **Cálculo exacto de fecha de fin**: Fórmula ajustada a `startDate.plusMonths(totalInstallments - 1)` para reflejar fielmente el último mes de vencimiento sin desfases de base 0 (ejemplo: préstamo de 9 meses iniciado en marzo concluye en noviembre).
- **Nuevo visor interactivo "Plan de Pago"**: Modal accesible desde la tarjeta con desglose cuota a cuota, montos individuales, fechas exactas de cobro y estado en tiempo real (Pagada, Activa o Pendiente).
- **Consistencia total de cuotas**: Separación estricta entre `totalInstallments` (duración total, ej. 9), `currentInstallment` (cuota activa, ej. 7) y `remainingInstallments` (`total - pagadas`, ej. 2).
- **Control de cuota actual y total**: Selector en el diálogo de edición/creación para indicar la cuota en curso y el total de plazos sin sobreescritura accidental.
- **Barra de progreso visual**: Indicador gráfico de amortización dentro de la tarjeta con botón de avance mensual cuota a cuota.
- **Migración Room v21**: Persistencia segura en base de datos sin pérdida de información de préstamos previos.

---

### 🏦 2. Interceptor Bancario y Soporte Google Wallet
- **Prioridad absoluta para ingresos y abonos**: Detección sin falsos positivos en transferencias, traspasos entre cuentas propias y Bizum recibidos (CaixaBank, Imagin, Openbank, etc.).
- **Compatibilidad con Google Wallet**: Integración del paquete de cartera de Google con motor anti-duplicados inteligente para evitar dobles cargos cuando la app bancaria y Wallet notifican a la vez.
- **Nuevas palabras clave de gastos y financiación**: Reconocimiento automático de cuotas, financiaciones, adeudos directos y recibos SEPA.

---

### 🛡️ 3. Copias de Seguridad Automáticas 24h
- **DailyBackupWorker**: Respaldo automático programado cada 24 horas mediante Android WorkManager.
- **Retención local optimizada**: Mantiene siempre copias recientes en el almacenamiento seguro del dispositivo para prevenir pérdidas de datos.

---

### 📱 4. Diseño Adaptativo y Visualización Ampliada
- **Rejilla en Gastos Variables**: Distribución adaptativa mediante `LazyVerticalGrid` en pantallas grandes, plegables y tablets.
- **Panel de Análisis Equilibrado**: Distribución 50/50 en pantallas anchas con métricas de Ritmo de Gasto Diario para un control visual instantáneo.

---

### ⚖️ 6. Unificación Matemática de Saldo Real y Margen Disponible
- **Evaluación precisa del saldo bancario**: Validación estricta con `currentBankBalance != -1.0 && currentBankBalance >= 0.0` para admitir saldos reales en cero (0.00 €) sin confundirlos con el valor por defecto sin configurar (-1.0).
- **Reserva de presupuesto variable pendiente**: El saldo restante disponible ahora descuenta el presupuesto variable necesario para finalizar el mes (`projectedRemainingVariable = totalVariableBudget - totalSpent`), evitando falsos márgenes holgados.
- **Sincronización total entre pantallas**: Fórmula unificada `Saldo Real = Base Bancaria - Fijos Pendientes - Variable Restante` aplicada idénticamente en el Resumen del Dashboard, la Proyección mensual y el Panel de Análisis.

---

### 🧩 7. Modularización y Limpieza de DashboardScreen
- **Componentes desacoplados**: Extracción limpia de submódulos (`DashboardBottomBar`, `DashboardNavRail`, `SummaryHeader`, `SaldoDisponibleCard`, `UpdateBalanceCard`, `MetricsRow`, `ProgressFixedCard`, `AccordionCard`, `MonthProjectionCard`, etc.).
- **Corrección de tipos de dominio Room**: Vinculación de las firmas de tarjetas y diálogos de suscripción con `ExpenseCategory` y `MonthProjection`, garantizando compilación limpia y sincronización reactiva.

---

### ✨ 8. Barra de Progreso Dinámica con Chispas (SparklingProgressBar)
- **Componente Canvas personalizado**: Reemplazo de la barra lineal nativa por `SparklingProgressBar` en la tarjeta de progreso de facturas fijas.
- **Física de partículas y optimización de energía**: Emisión de chispas en el borde de corte con gravedad y desvanecimiento alpha exclusivamente mientras la barra se anima (`animationJob.isActive`). Reposo total a 0 FPS una vez alcanzado el objetivo.
- **Acabado visual incandescente**: Degradado suave de relleno (`Brush.horizontalGradient`) y punto de contacto destellante.

---

### 🛡️ 9. Respaldo Permanente 24h en Almacenamiento Público y Backup Manual (.fflow)
- **Persistencia inmune a desinstalaciones**: La copia rotativa de seguridad de 24 horas ahora se guarda directamente en `/Documents/FinanceFlow/backup_previous_24h.json` (`Environment.DIRECTORY_DOCUMENTS`), sobreviviendo a desinstalaciones de APK y cambios de firmas.
- **Doble respaldo en segundo plano y Worker**: `DailyBackupWorker` y la opción de forzado manual generan la copia en JSON en almacenamiento público con fallback de caché interna.
- **Recuperación de Backup Manual (.fflow)**: Restauración de la sección en `SettingsScreen.kt` y `UnifiedSettingsDialog.kt` con soporte completo de Storage Access Framework (`CreateDocument` y `OpenDocument`) para exportar e importar ficheros `.fflow`/`.json` en cualquier almacenamiento local o en la nube.
- **Restauración Inteligente**: `restoreAutoBackupExternal` localiza y valida automáticamente el respaldo en `Documents/FinanceFlow/`, garantizando la reconstrucción total de la base de datos de Room.

---

### 🎯 10. Motor Universal de Saldo Libre Real por Ciclo de Ingreso Dinámico y Recurrentes Multi-Mes
- **Ventana temporal dinámica de cobro**: Cálculo matemático ajustado a la ventana $Día\_Actual \rightarrow (Próximo\_Cobro - 1\_día)$ en función del `incomeDay` de cada usuario (ejemplo: si cobra el 10 y hoy es 16-sept, la ventana abarca del 16-sept al 09-oct).
- **Tratamiento estricto de gastos fijos recurrentes de principio de mes**: Si un gasto fijo recurrente tiene `payDay < currentDay` (ejemplo: Alquiler día 5) y ya se pagó este mes, el motor detecta que vencerá nuevamente a principios del mes que viene (5 de octubre) antes de la próxima nómina (10 de octubre), computándolo obligatoriamente en el saldo libre para evitar saldos falsamente positivos y prevenir descubiertos.
- **Fórmula Universal sin distorsión**: $\text{Saldo Libre Real} = \text{Saldo\_Banco\_Actual} - \text{Gastos\_Obligatorios\_del\_Ciclo}$.
- **Interoperabilidad `LocalDate` y `Calendar`**: Métodos `getPendingExpensesForCycle` y `getPendingCategoriesForCycle` en `FinanceViewModel` compatibles con `Calendar` y `java.time.LocalDate` para consumo unificado en Dashboard, Análisis y Widgets.
- **Suite de tests unitarios al 100% en verde**: Validación automatizada en `FinanceViewModelTest` cubriendo el caso exacto de déficit (-10,80 € frente al saldo falso de 759 €), días de cobro intermedios, recibos previos/posteriores y widgets sincronizados.

---

### 🏛️ 11. Arquitectura de Dominio Aislada y Protección de Compilador (@CriticalFinancialEngine)
- **Anotación `@CriticalFinancialEngine`**: Anotación personalizada con `@RequiresOptIn(level = RequiresOptIn.Level.ERROR)` para bloquear cambios no intencionados o refactorizaciones accidentales en el motor de tesorería y liquidez tanto en IDE como en compilación.
- **Caso de Uso de Dominio (`CalculateCycleBalanceUseCase`)**: Encapsulación completa de la lógica matemática del ciclo en la capa de dominio (`com.example.domain`), dejando a `FinanceViewModel` como simple consumidor que expone el estado reactivo hacia la UI sin mezclar fórmulas en los composables de Jetpack Compose.
- **Pruebas unitarias de dominio dedicadas**: Validación exhaustiva en `CalculateCycleBalanceUseCaseTest` cubriendo cobros en días variables (ej. día 28), meses cortos (febrero con cobros a día 31 sin desbordes de fecha) y cálculo exacto de saldo libre real.

