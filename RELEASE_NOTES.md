# 🚀 FinanceFlow - Notas de la Versión v2.2.0

¡Gran actualización acumulativa con mejoras clave en automatización, seguimiento financiero y diseño adaptativo!

---

### 💳 1. Financiaciones y Préstamos Inteligentes (Klarna, Openbank, etc.)
- **Cálculo automático de fecha de fin**: Cada tarjeta de gasto financiado calcula y muestra de forma clara el mes y año en que concluye el préstamo (ejemplo: *"Finaliza en Marzo de 2027"*).
- **Control de cuota actual y total**: Nuevo selector en el diálogo de edición/creación para indicar la cuota en curso y el total de plazos (ej. *"Cuota 3 de 12"*).
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

### ⚙️ 5. Ciclo de Vida y Versionado Automatizado
- **Auto-incremento en Gradle**: Gestión de `versionCode` y `versionName` vinculada a `version.properties`.
- **Regla de los 10 Cambios**: Sistema de control con contador de cambios (`CHANGES_COUNT`) y alertas de compilación para planificar los próximos releases de forma ordenada.
