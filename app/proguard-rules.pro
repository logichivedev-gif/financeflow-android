# ==============================================================================
# REGLAS ESPECÍFICAS DE OPTIMIZACIÓN Y OFUSCACIÓN (v1.3.0)
# ==============================================================================

# Conservar lineas y trazas de error originales para depurar crashes en producción
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ------------------------------------------------------------------------------
# ARQUITECTURA DE PERSISTENCIA: ROOM DATABASE
# ------------------------------------------------------------------------------
# Conservar las anotaciones de Room y evitar el renombrado de clases generadas
-keepclassmembers class * {
    @androidx.room.Dao *;
    @androidx.room.Database *;
}
-keep class * implements androidx.room.RoomOpenHelper
-keep class androidx.room.RoomDatabase { java.util.concurrent.locks.ReentrantReadWriteLock mCloseLock; }

# BLINDAJE CRÍTICO: Conservar tus entidades físicas de la base de datos intactas
# (Previene errores NoSuchMethodException o mapeos de tabla rotos)
-keep class com.example.data.** { *; }

# ------------------------------------------------------------------------------
# SERIALIZACIÓN Y BACKUP: MOSHI JSON
# ------------------------------------------------------------------------------
# Mantener metadatos obligatorios de firmas y anotaciones para el procesador KSP
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Evitar que R8 elimine las clases generadas por moshi-kotlin-codegen
-keep class *JsonAdapter { <init>(...); }
-keep class * implements com.squareup.moshi.JsonAdapter
-keep @interface com.squareup.moshi.JsonQualifier

# Conservar nombres de campos serializados para que no muten en el archivo JSON
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}