# AGENTS.md - Guía de Desarrollo y Backlog del Proyecto

## Regla de los 10 Cambios por Versión (Ciclo de Releases)
A partir de la versión v2.2.0:
1. **Agrupación en bloques de 10 ítems**: Los cambios confirmados (nuevas funciones, correcciones o mejoras) se acumulan de 1 en 1.
2. **Registro y control**: Se mantiene el contador `CHANGES_COUNT` en `app/version.properties` y se incrementa en +1 con cada cambio/tarea implementada.
3. **Alerta de Límite (10/10)**: Al alcanzar `CHANGES_COUNT = 10`, Gradle emite una advertencia formal en la consola y se avisa al usuario para cerrar el ciclo y preparar la Release en GitHub.
4. **Cierre y Reset**: Al publicar la release, se actualiza el número de versión (ej. de 2.2.0 a 2.3.0) y `CHANGES_COUNT` vuelve a 0.
5. **Notas de la Versión Automáticas**: En cada preparación o cierre de versión, se redactan todas las novedades, mejoras y cambios detallados en el archivo raíz `RELEASE_NOTES.md` para alimentar directamente la publicación automática en GitHub.

## Backlog / Tareas Pendientes

- **Refactor de estilos a tokens Material3 para Modo Oscuro**:
  - Reemplazar colores estáticos/hardcoded (`Color.White`, `Color.Black`, `Color(0xFF...)`) en tarjetas, tarjetas del Dashboard y componentes modales por los tokens semánticos de `MaterialTheme.colorScheme` (`surface`, `background`, `onSurface`, `surfaceVariant`, `onSurfaceVariant`, etc.).
  - Definir la paleta completa en `Color.kt` / `Theme.kt` para `DarkColorScheme` garantizando un contraste óptimo en todos los contenedores y textos.
  - Reactivar el selector de modo de tema (Claro / Oscuro / Sistema) en `UnifiedSettingsDialog.kt` y la sincronización global en `MainActivity.kt`.
