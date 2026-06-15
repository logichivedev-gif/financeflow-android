package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.example.data.FinanceDatabase
import com.example.data.FinanceRepository
import com.example.data.UpdateChecker
import com.example.data.UpdateInfo
import com.example.ui.FinanceApp
import com.example.ui.FinanceViewModel
import com.example.ui.components.UpdateDialog
import com.example.ui.theme.MyApplicationTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {

    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            FinanceDatabase::class.java,
            "finance_flow.db"
        )
            .addMigrations(
                FinanceDatabase.MIGRATION_4_5,
                FinanceDatabase.MIGRATION_5_6,
                FinanceDatabase.MIGRATION_6_7,
                FinanceDatabase.MIGRATION_7_8,
                FinanceDatabase.MIGRATION_8_9,
                FinanceDatabase.MIGRATION_9_10,
                FinanceDatabase.MIGRATION_10_11,
                FinanceDatabase.MIGRATION_11_12
            )
            .build()
    }

    private val repository by lazy {
        FinanceRepository(database.financeDao())
    }

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
        setContent {
            // Set up factory dynamically to bridge Room dependencies cleanly without heavy DI frameworks
            val viewModel: FinanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
                            return FinanceViewModel(repository) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class")
                    }
                }
            )

            val dbProfileState = viewModel.dbProfile.collectAsStateWithLifecycle()
            val theme = dbProfileState.value?.selectedTheme ?: "azul"

            // Local state for system updates
            val updateInfo = remember { mutableStateOf<UpdateInfo?>(null) }

            LaunchedEffect(Unit) {
                try {
                    val info = UpdateChecker.checkForUpdates(BuildConfig.VERSION_NAME)
                    if (info != null && info.isNewUpdateAvailable) {
                        updateInfo.value = info
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            MyApplicationTheme(theme = theme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        FinanceApp(viewModel = viewModel)

                        // Overlaid dialogue for independent package update
                        updateInfo.value?.let { info ->
                            UpdateDialog(
                                updateInfo = info,
                                onDismiss = { updateInfo.value = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

