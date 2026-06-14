# FinanceFlow 📱💼

[![Android Dev](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white&style=for-the-badge)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white&style=for-the-badge)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white&style=for-the-badge)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Database-Room%20SQLite-3DDC84?logo=sqlite&logoColor=white&style=for-the-badge)](https://developer.android.com/training/data-storage/room)

**FinanceFlow** es una aplicación nativa de Android de clase premium, diseñada con enfoque *offline-first* para el control inteligente, seguro y ultra visual de tus ingresos, gastos fijos, variables y presupuestos del hogar. 

Desarrollada bajo el sello de calidad tecnológica de **Syntax Forge DEV**, la aplicación rompe con la estética tradicional grisácea al ofrecer una interfaz de usuario vibrante, con contrastes profundos de morfismo de cristal (*glassmorphism*), gradientes de fusión y una consistencia excepcional basada en **Material Design 3**.

---

## ✨ Características Exclusivas

### 🎨 1. Menú Lateral Premium Multicapa (Glassmorphism)
* **Estética de Contraste Inverso**: Fondos ultra oscuros (`#0F121A`) que contrastan elegantemente con la interfaz clara general, evocando la experiencia de las apps fintech de alto nivel.
* **Cabecera de Marca Integrada**: Fondo dinámico e interactivo integrado con el logo vectorial personalizado de **Syntax Forge DEV**, escalado con `ContentScale.Crop` a una sutil opacidad del `22%` y fundido con un degradado vertical de tres colores.
* **Píldora Indicadora de Estado**: Cuenta con un LED verde de sistema activo ("SYNTAX FORGE DEV") que da la sensación de un servicio en tiempo real y alta fidelidad.

### 💱 2. Selector Inteligente de Moneda Base Global
* **Símbolo Global Reactivo**: Cambia la moneda base directamente en el panel de **Preferencias de UI**. El símbolo se actualiza instantáneamente en todos los textos explicativos, fórmulas de cálculo, sliders de topes, inputs y balances de tu panel.
* **Monedas Soportadas**: Soporte enriquecido para Euro (`€`), Dólar (`$`), Libra (`£`), Yen (`¥`), Peso Colombiano (`COP`), Peso Mexicano (`MXN`) y más.
* **Alineación Flexible de Símbolos**: Se adapta de forma nativa a la lectura de cada moneda (ej. prefijo para `$` y sufijo para `€`).

### 🚀 3. Flujo Inicial Interactiva (Setup Wizard)
* **Paso a Paso Inteligente**: Un asistente secuencial para configurar cómodamente tus ingresos, balance en bancos, gastos fijos recurrentes (mascotas, hijos, agua, luz y la aportación de tu pareja).
* **Bloqueo Seguro de Gestos**: Impide navegar de forma imprevista si no has completado la configuración inicial, garantizando la consistencia lógica de los cálculos de nómina.

### 📊 4. Panel de Control Financiero (Dashboard)
* **Visualización de Balances**: Muestra con exactitud tu balance disponible disponible, ingresos, gastos fijos acumulados y gastos variables totales del mes actual.
* **Cálculos y Fórmulas Precisas**: Fórmulas nativas adaptadas a provisiones bimensuales que te muestran exactamente cómo se distribuyen tus gastos en el presupuesto mensual.

---

## 🛠️ Stack Tecnológico

* **Lenguaje**: [Kotlin](https://kotlinlang.org/) (100% Nativo) con Corrutinas y Flows para gestión asíncrona no bloqueante.
* **Interfaz de Usuario**: [Jetpack Compose](https://developer.android.com/jetpack/compose) con controles declarativos y animaciones interactivas fluidas.
* **Esquema de Diseño**: [Material Design 3 (M3)](https://m3.material.io/) con tipografía de alto impacto y paleta dinámica optimizada.
* **Persistencia de Datos**: [Room Database (SQLite)](https://developer.android.com/training/data-storage/room) con definición de entidades y **migración incremental de base de datos** (Migración 11 a 12 implementada de forma segura para dar soporte a la selección flexible de moneda).
* **Arquitectura**: Arquitectura limpia orientada a componentes (**MVVM**) con reactividad en tiempo de ejecución (`MutableStateFlow`, `collectAsStateWithLifecycle`).

---

## 🚀 Instalación y Configuración

Sigue estos pasos para compilar, probar y desplegar el proyecto de manera local:

### Requisitos Previos
* **Android Studio** Ladybug (versión recomendada o superior).
* **JDK 17** o superior configurado en tu entorno de desarrollo.
* Dispositivo físico Android o Emulador con **SDK 26 (Android 8.0 Oreo)** o superior.

### Instrucciones de Compilación
1. Clona el repositorio desde GitHub:
   ```bash
   git clone https://github.com/tu-usuario/financeflow-android.git
   ```
2. Abre Android Studio y selecciona **Open an Existing Project** localizando el directorio clonado.
3. Deja que Gradle descargue las dependencias y sincronice el proyecto por primera vez.
4. Para compilar una versión de prueba en modo Debug, corre la tarea de Gradle directamente:
   ```bash
   # Compilar APK en modo Debug
   ./gradlew assembleDebug
   ```
   *(Nota: Si compilas desde la terminal de nuestro hosting o entorno de AI Studio, utiliza simplemente la tarea `gradle assembleDebug`)*

---

## 📂 Estructura Principal del Proyecto

```
/app/src/main/
├── java/com/example/
│   ├── MainActivity.kt           # Punto de entrada de la visualización nativa
│   ├── data/
│   │   ├── FinanceDatabase.kt    # Base de datos Room, migraciones SQLite (Versión 12)
│   │   └── FinanceModels.kt      # Modelos de datos para perfiles, categorías y gastos
│   └── ui/
│       ├── FinanceApp.kt         # Pantallas, Wizard, ModalDrawer y lógica Compose
│       └── FinanceViewModel.kt   # Motor de estado MVVM e interacciones con el repositorio
└── res/
    ├── drawable/
    │   └── syntax_forge_logo.xml # Espectacular logo vectorial de Syntax Forge DEV
    └── values/
        └── strings.xml           # Archivo centralizado de strings de la aplicación
```

---

## 🎨 Diseñado y Desarrollado por
Este proyecto ha sido estilizado y estructurado con pasión y rigor de ingeniería por el equipo de diseño y arquitectura de **Syntax Forge DEV** 💻🛠️.

---
*¿Tienes dudas o quieres reportar un error? Utiliza el botón integrado de **Reportar Errores 🐞** dentro del menú de la app para ponerte en contacto automáticamente.*
