package com.example

import android.os.Bundle
import android.os.Build
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.FinanceDatabase
import com.example.data.FinanceRepository
import com.example.data.UpdateChecker
import com.example.data.UpdateInfo
import com.example.ui.FinanceApp
import com.example.ui.FinanceViewModel
import com.example.ui.components.UpdateDialog
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.DailyBackupWorker
import com.example.data.ScheduleNotificationsWorker
import java.util.concurrent.TimeUnit

class MainActivity : FragmentActivity() {

    private val database by lazy {
        FinanceDatabase.getDatabase(applicationContext)
    }

    private val repository by lazy {
        FinanceRepository(database.financeDao())
    }

    private val isAuthenticated = MutableStateFlow(false)
    private var isBiometricPromptShowing = false

    fun promptBiometricAuthentication() {
        if (isFinishing || isDestroyed || isBiometricPromptShowing) return
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isAuthenticated.value = true
                    isBiometricPromptShowing = false
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    isBiometricPromptShowing = false
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso Protegido")
            .setSubtitle("Autentícate para acceder a Syntax Forge")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        try {
            isBiometricPromptShowing = true
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            e.printStackTrace()
            isBiometricPromptShowing = false
        }
    }

    override fun onStop() {
        super.onStop()
        
        lifecycleScope.launch {
            val profile = repository.getProfileDirect()
            if (profile?.isBiometricEnabled == true) {
                isAuthenticated.value = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val profile = repository.getProfileDirect()
            if (profile?.isBiometricEnabled == true && !isAuthenticated.value && !isBiometricPromptShowing) {
                promptBiometricAuthentication()
            }
            if (profile?.isBankNotificationInterceptorEnabled == true) {
                com.example.service.BankNotificationListenerService.ensureServiceBound(applicationContext)
            }
        }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        
        try {
            val workRequest = PeriodicWorkRequestBuilder<ScheduleNotificationsWorker>(
                1, TimeUnit.DAYS
            ).build()
            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "ScheduleNotificationsWork",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        
        DailyBackupWorker.schedulePeriodicBackup(applicationContext)


        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        
        lifecycleScope.launch {
            try {
                combine(
                    repository.financialProfile,
                    repository.activeCategories,
                    repository.variableExpenses
                ) { _, _, _ ->
                    com.example.widget.WidgetUpdateHelper.updateAllWidgets(applicationContext)
                }.collect {}
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        setContent {
            
            val viewModel: FinanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
                            return FinanceViewModel(repository, database) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class")
                    }
                }
            )

            val dbProfileState = viewModel.dbProfile.collectAsStateWithLifecycle()
            val profile = dbProfileState.value
            val theme = profile?.selectedTheme ?: "azul"
            val isDarkTheme = false
            val isBiometricEnabled = profile?.isBiometricEnabled == true
            val isAuthedState by isAuthenticated.collectAsStateWithLifecycle()

            
            val isUnlocked = !isBiometricEnabled || isAuthedState

            
            val updateInfo = remember { mutableStateOf<UpdateInfo?>(null) }

            LaunchedEffect(isBiometricEnabled, isAuthedState) {
                if (isBiometricEnabled && !isAuthedState && !isBiometricPromptShowing) {
                    promptBiometricAuthentication()
                }
            }

            LaunchedEffect(Unit) {
                try {
                    val info = UpdateChecker.checkForUpdates(BuildConfig.VERSION_NAME)
                    if (info != null && info.isNewUpdateAvailable) {
                        updateInfo.value = info
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                
                try {
                    val intentUri = intent?.data
                    if (intentUri != null) {
                        contentResolver.openInputStream(intentUri)?.use { stream ->
                            val content = stream.bufferedReader().use { it.readText() }
                            if (content.isNotBlank()) {
                                val success = viewModel.importDataJson(content)
                                if (success) {
                                    android.widget.Toast.makeText(this@MainActivity, "✅ Archivo .fflow importado automáticamente con éxito", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            MyApplicationTheme(darkTheme = isDarkTheme, theme = theme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isUnlocked) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val windowSizeClass = calculateWindowSizeClass(this@MainActivity)
                            FinanceApp(
                                viewModel = viewModel,
                                windowWidthSizeClass = windowSizeClass.widthSizeClass
                            )

                            
                            updateInfo.value?.let { info ->
                                UpdateDialog(
                                    updateInfo = info,
                                    onDismiss = { updateInfo.value = null }
                                )
                            }
                        }
                    } else {
                        BiometricLockScreen(
                            onUnlockClick = { promptBiometricAuthentication() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BiometricLockScreen(
    onUnlockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFF0F121A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0061A4).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Acceso Protegido",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Bloqueo biométrico activo. Autentícate para acceder a Syntax Forge.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF94A3B8),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = onUnlockClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061A4)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("biometric_unlock_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Desbloquear con Biometría / PIN",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }
    }
}
