import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}




val versionPropsFile = file("version.properties")
val versionProps = Properties()

if (versionPropsFile.canRead()) {
    versionPropsFile.inputStream().use { versionProps.load(it) }
} else {
    versionProps.setProperty("VERSION_CODE", "14")
    versionProps.setProperty("VERSION_MAJOR", "2")
    versionProps.setProperty("VERSION_MINOR", "2")
    versionProps.setProperty("VERSION_PATCH", "0")
    versionProps.setProperty("CHANGES_COUNT", "0")
    versionPropsFile.outputStream().use { versionProps.store(it, "Version Configuration") }
}

var currentVersionCode = (versionProps.getProperty("VERSION_CODE") ?: "14").toIntOrNull() ?: 14
val versionMajor = versionProps.getProperty("VERSION_MAJOR") ?: "2"
val versionMinor = versionProps.getProperty("VERSION_MINOR") ?: "2"
val versionPatch = versionProps.getProperty("VERSION_PATCH") ?: "0"
var changesCount = (versionProps.getProperty("CHANGES_COUNT") ?: "0").toIntOrNull() ?: 0

val isBuildTask = gradle.startParameter.taskNames.any { taskName ->
    taskName.contains("assemble", ignoreCase = true) ||
    taskName.contains("bundle", ignoreCase = true) ||
    taskName.contains("build", ignoreCase = true) ||
    taskName.contains("compile", ignoreCase = true)
}

if (isBuildTask) {
    currentVersionCode += 1
    versionProps.setProperty("VERSION_CODE", currentVersionCode.toString())
    versionProps.setProperty("CHANGES_COUNT", changesCount.toString())
    versionPropsFile.outputStream().use { versionProps.store(it, "Auto-incremented on build") }
}

if (changesCount >= 10) {
    logger.warn("""
    ********************************************************************************
    ⚠️  ALERTA DE LÍMITE DE VERSIÓN (REGLA DE LOS 10 CAMBIOS):
    Actualmente hay $changesCount / 10 cambios registrados en version.properties.
    Es momento de preparar y publicar el Release v$versionMajor.$versionMinor.$versionPatch en GitHub!
    Recuerda reiniciar CHANGES_COUNT=0 tras publicar la release.
    ********************************************************************************
    """.trimIndent())
}

val computedVersionName = versionProps.getProperty("VERSION_NAME") ?: "$versionMajor.$versionMinor.$versionPatch"

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.financeflow.rfqbxz"
    minSdk = 24
    targetSdk = 36
    versionCode = currentVersionCode
    versionName = computedVersionName

    buildConfigField("int", "CHANGES_COUNT", changesCount.toString())

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      val localKeystore = file("${rootDir}/debug.keystore")
      storeFile = if (localKeystore.exists()) {
          localKeystore
      } else {
          file(System.getProperty("user.home") + "/.android/debug.keystore")
      }
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = if (file("${rootDir}/debug.keystore").exists()) {
        signingConfigs.getByName("debugConfig")
      } else {
        signingConfigs.getByName("debug")
      }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}



secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}



dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  
  implementation(libs.androidx.activity.compose)
  
  
  
  
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material3.windowsizeclass)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  
  implementation(libs.retrofit)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.androidx.biometric)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
