# FinanceFlow 📱

An independent, native Android personal finance application designed under an offline-first philosophy, structured around custom salary cycles, and published by Syntax Forge DEV.

## Technical Features (v1.2.0)

* **Strict "Black Box" Financial Engine:** Net balance calculation strictly factors in projected daily variable expenses along with upcoming fixed bills to prevent false-positive green indicators:
    $$Margen\,Libre = Saldo\,Actual - Fijos\,Pendientes - (Gasto\,Diario\,Promedio \times Días\,Restantes)$$
* **Double-Tier Clean UI Architecture:** Reengineered the main ledger layout from a congested horizontal row into a decoupled vertical column hierarchy. The top tier isolates the primary action nodes (Checkbox, Avatar, Title, and Price), while the independent bottom tier allocates a `14.sp` high-legibility layout for complex metadata strings without text truncation or ellipsis.
* **Deterministic List Stability & Dynamic Sorting:** Fixed erratic UI item shuffling during state mutations by injecting deterministic unique constraints (`key = { it.id }`) inside the `LazyColumn` core. Extended the `FinanceViewModel` with a dedicated `SortOrder` state machine to swap layout indexing at runtime via three custom `FilterChip` UI nodes: `BY_DATE` (chronological billing), `BY_AMOUNT` (descending debt magnitude), and `BY_TYPE` (structural grouping).
* **Atomic Local Backup Engine (JSON):** Implemented a secure local data-transfer infrastructure utilizing Moshi (`KotlinJsonAdapterFactory`) driven via KSP. The backup layer encapsulates the active profile state (`FinancialProfile`), expense categories (`ExpenseCategory`), and transaction history (`VariableExpenseEntry`) into standalone `.json` targets, utilizing low-level atomic transaction blocks (`beginTransaction`, `setTransactionSuccessful`, `endTransaction`) to revert state corruption in case of malformed file loading.
* **Storage Access Framework (SAF) Sandboxing:** Migrated local storage operations into Android's native SAF layer using `ActivityResultContracts.CreateDocument("application/json")` and `ActivityResultContracts.GetContent()` contracts. Automates a dynamic structured file naming pattern (`financeflow_backup_[timestamp].json`). Decoupled backup workflows into isolated `SettingDialogType.BACKUP` configurations to physically segregate data-preservation mechanics from destructive master-reset routines (`SettingDialogType.DATABASE`).
* **Universal Localization Architecture:** Designed an internationalization scheme using decoupled system string resources (`stringResource`). Drop regional naming patterns in base resources for universal concepts (*Ciclos de Pago*, *Introducir Sueldo Neto*, *Registrar Gasto*, *Categoría de Gastos Fijos*), and implemented targeted variants matching runtime device locales to fluidly swap technical and financial definitions (e.g., *Nómina* vs *Planilla* and *Saldo disponible* vs *Presupuesto Disponible*).
* **Fluid Keyboard & Focus UX:** Solved input obstruction by integrating `LocalSoftwareKeyboardController` and `LocalFocusManager`. Virtual keyboards hide instantly upon `ImeAction.Done` while shifting cursor focus away to ensure persistent Material Design 3 Snackbars or confirmation views deploy clear of overlay blocks.
* **Autonomous Update & Support Handlers:** Built-in network utility (`UpdateChecker`) that asynchronously queries the GitHub Releases API. Upgraded the tech-support subsystem to dynamically resolve the target installation context (`packageManager.getPackageInfo`) instead of employing hardcoded version strings, appending structural runtime values safely.

---

## Español

Aplicación nativa de finanzas personales basada en ciclos de nómina independientes y desarrollada por Syntax Forge DEV.

* **Algoritmo de Extrema Estrictez ("Peor Escenario"):** El margen libre real descuenta la proyección de gasto variable restante del ciclo y los recibos fijos inminentes para erradicar balances engañosos.
* **Arquitectura de Doble Nivel Estructural:** Rediseño completo de las tarjetas de consumo pasando de un formato horizontal saturado a una disposición vertical en dos niveles independientes. El nivel superior aísla los datos primarios (Nombre, Icono y Precio) y el nivel inferior reserva un espacio dedicado con tipografía de alta legibilidad (`14.sp`) que permite la plena lectura de estados complejos sin truncado de palabras.
* **Estabilización Determinista y Ordenación Dinámica:** Corrección de reordenamientos aleatorios de celdas mediante la asignación estricta de identificadores únicos (`key = { it.id }`) en el `LazyColumn`. El motor lógico se integra en el `FinanceViewModel` a través del estado `SortOrder`, permitiendo al usuario alternar criterios de ordenación en tiempo real por medio de `FilterChips`: por fecha (`BY_DATE`), por monto (`BY_AMOUNT`) o agrupados por naturaleza (`BY_TYPE`).
* **Motor Transaccional de Copias de Seguridad (JSON):** Infraestructura de transferencia de datos local utilizando Moshi mediante procesamiento KSP. Centraliza la exportación e importación del perfil y transacciones en archivos estructurados `.json`, forzando la atomicidad en la persistencia de Room (`beginTransaction`, `setTransactionSuccessful`, `endTransaction`) para asegurar la reversión total del sistema si el archivo de lectura está corrupto.
* **Aislamiento de Almacenamiento vía SAF:** Implementación de contratos nativos de Android (`CreateDocument` y `GetContent`) para delegar el control de los archivos locales al usuario sugiriendo el nombre estructurado `financeflow_backup_[timestamp].json`. Se modularizan los menús de configuración separando físicamente el módulo de respaldos (`SettingDialogType.BACKUP`) de la zona de peligro de formateo total de fábrica (`SettingDialogType.DATABASE`).
* **Localización y Adaptación Cultural Universal:** Estrategia de internacionalización nativa apoyada en recursos independientes del sistema (`stringResource`). Limpia modismos regionales en recursos base utilizando términos universales (*Ciclos de Pago*, *Introducir Sueldo Neto*, *Registrar Gasto*, *Categoría de Gastos Fijos*) e inyecta variantes localizadas que adaptan dinámicamente palabras clave (ej: *Nómina* vs *Planilla* y *Saldo disponible* vs *Presupuesto Disponible*) dependiendo de las preferencias lingüísticas internas del dispositivo.
* **Interacción de Entrada Optimizada (UX):** Control estricto de foco (`LocalFocusManager`) y ocultación del teclado virtual al confirmar datos cuantitativos rápidos mediante la acción de "Listo / Guardar", asegurando la visibilidad completa del Snackbar de confirmación en pantalla.
* **Soporte Técnico e Inyección de Metadatos:** El sistema de reporte de errores por correo electrónico consulta dinámicamente el gestor de paquetes del dispositivo (`packageManager.getPackageInfo`) para recuperar e inyectar la versión exacta compilada del sistema, eliminando textos estáticos en el código fuente y evitando discrepancias de versión.

---

## Tech Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material Design 3)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Local Storage:** SQLite via Room Database (Inyección de dependencias nativa)
* **Serialization Engine:** Moshi JSON (KSP Adapter Generation via `@JsonClass`)
* **Asynchrony:** Kotlin Coroutines (`Dispatchers.IO`) & Flow
* **System Integration:** Storage Access Framework (SAF)
* **Version Parameters:** Version Code `3` | Version Name `1.2.0`
