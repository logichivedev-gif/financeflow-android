# FinanceFlow 📱

An independent, native Android personal finance application designed under an **offline-first** philosophy and structured around custom salary cycles.

---

## Features

- **Strict "Black Box" Financial Engine:** Net balance factors in projected daily variable expenses alongside upcoming fixed bills to prevent accidental overspending.
- **Multi-language Support:** Native adaptability (English/Spanish) using optimized system string resources.
- **Autonomous Update Notifications:** Built-in update checker that securely queries the GitHub API to notify users of new releases.
- **Dynamic Currency & UX:** Automatically adapts to the host system's local currency symbol and clears focus/keyboards smoothly upon data entry.
- **Absolute Privacy:** No cloud sync, no third-party bank linking. All data remains securely stored on your local device via Room Database.

---

## Español

Aplicación nativa de finanzas personales basada en ciclos de nómina independientes y diseñada bajo una filosofía **offline-first**.

- **Algoritmo de Extrema Estrictez:** El margen libre real descuenta la proyección de gasto variable restante del ciclo y los recibos fijos inminentes para evitar falsos optimistas.
- **Privacidad Absoluta:** Sin registros ni sincronización en la nube. Tus datos financieros se almacenan localmente utilizando tecnología Room.
- **Soporte Multiidioma Nativo:** Adaptabilidad limpia mediante recursos del sistema (ES/EN).
- **Notificación de Actualizaciones:** Sistema autónomo que comprueba lanzamientos directamente desde la API pública de GitHub.

---

## Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Local Storage:** SQLite via Room Database
- **Asynchrony:** Kotlin Coroutines & Flow
