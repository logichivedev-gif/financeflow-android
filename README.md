# FinanceFlow 📱

An independent, native Android personal finance application designed under an **offline-first** philosophy, structured around custom salary cycles, and published by **Syntax Forge DEV**.

---

## Technical Features (v1.1.0)

- **Strict "Black Box" Financial Engine:** Net balance calculation strictly factors in projected daily variable expenses along with upcoming fixed bills to prevent false-positive green indicators:
  $$Margen\,Libre = Saldo\,Actual - Fijos\,Pendientes - (Gasto\,Diario\,Promedio \times Días\,Restantes)$$
- **Fluid Keyboard & Focus UX:** Solved input obstruction by integrating `LocalSoftwareKeyboardController` and `LocalFocusManager`. Virtual keyboards hide instantly upon `ImeAction.Done`, clarifying backend Room persistence via immediate Material Design 3 Snackbars.
- **Autonomous Update Notifications:** Built-in network utility (`UpdateChecker`) that asynchronous-safely (`Dispatchers.IO`) queries the GitHub Releases API to parse assets and notify users via a non-cancellable custom `UpdateDialog`.
- **Multi-language Support:** Native internationalization (English/Spanish) using decoupled system string resources (`stringResource`).
- **Dynamic Currency Localization:** Removed hardcoded currency layouts. Employs `Currency.getInstance(Locale.getDefault()).symbol` to seamlessly map locale-specific parameters (e.g., `$`, `€`).
- **Resource Patching:** Resolved framework initialization crashes by forcing standard `<?xml version="1.0" encoding="utf-8"?>` encoding parameters on custom vector drawables.
- **Absolute Privacy:** Strict offline architectures. No cloud sync or third-party banking APIs. Data is completely localized via SQLite/Room.

---

## Español

Aplicación nativa de finanzas personales basada en ciclos de nómina independientes y desarrollada por **Syntax Forge DEV**.

- **Algoritmo de Extrema Estrictez ("Peor Escenario"):** El margen libre real descuenta la proyección de gasto variable restante del ciclo y los recibos fijos inminentes para erradicar balances engañosos.
- **Interacción de Entrada Optimizada (UX):** Control estricto de foco y ocultación del teclado virtual al confirmar datos, asegurando la visibilidad completa del Snackbar de confirmación.
- **Notificación de Actualizaciones:** Sistema autónomo en segundo plano que comprueba nuevos despliegues de APKs a través de la API de GitHub.
- **Soporte Multiidioma Nativo:** Adaptabilidad limpia mediante recursos independientes del sistema (ES/EN).
- **Divisa Flexible:** Detección automática del símbolo monetario del sistema operativo.
- **Privacidad Total:** Base de datos relacional Room 100% aislada en el dispositivo.

---

## Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Local Storage:** SQLite via Room Database
- **Asynchrony:** Kotlin Coroutines & Flow
