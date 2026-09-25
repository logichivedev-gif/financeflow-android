package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.R
import com.example.ui.common.*
import com.example.ui.components.AboutDialog
import com.example.ui.components.UnifiedSettingsDialog
import com.example.ui.components.UserAvatarView
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.wizard.WizardStep1Screen
import com.example.ui.screens.wizard.WizardStep2Screen
import com.example.ui.screens.wizard.WizardStep3Screen
import com.example.ui.screens.wizard.WizardStep4Screen
import kotlinx.coroutines.launch

private data class DrawerMenuItem(
    val titleRes: Int,
    val icon: ImageVector,
    val dialogType: SettingDialogType,
    val tabIndex: Int,
    val tag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceApp(
    viewModel: FinanceViewModel,
    windowWidthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val dbProfile by viewModel.dbProfile.collectAsStateWithLifecycle()
    val isWizardComplete = dbProfile?.isWizardComplete == true

    val selectedIconId = dbProfile?.selectedIcon ?: "trending"
    val appLogoIcon = when (selectedIconId) {
        "wallet" -> Icons.Default.AccountBalanceWallet
        "savings" -> Icons.Default.Savings
        "payments" -> Icons.Default.Payments
        "chart" -> Icons.Default.ShowChart
        "star" -> Icons.Default.Star
        else -> Icons.Default.TrendingUp
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var activeDialog by remember { mutableStateOf(SettingDialogType.NONE) }
    var selectedSettingsTab by remember { mutableStateOf(0) }
    var showAboutDialog by remember { mutableStateOf(false) }

    
    val activeThemeId = dbProfile?.selectedTheme ?: "azul"
    LaunchedEffect(activeThemeId) {
        when (activeThemeId) {
            "verde" -> {
                currentThemeTeal = Color(0xFF16A34A)
                currentThemeLightBlue = Color(0xFFDCFCE7)
            }
            "minimalista" -> {
                currentThemeTeal = Color(0xFF1F2937)
                currentThemeLightBlue = Color(0xFFE5E7EB)
            }
            "naranja" -> {
                currentThemeTeal = Color(0xFFEA580C)
                currentThemeLightBlue = Color(0xFFFFEAD5)
            }
            "purpura" -> {
                currentThemeTeal = Color(0xFF7C3AED)
                currentThemeLightBlue = Color(0xFFF3E8FF)
            }
            else -> {
                currentThemeTeal = Color(0xFF0061A4)
                currentThemeLightBlue = Color(0xFFD1E4FF)
            }
        }
    }

    val activeCurrency = dbProfile?.selectedCurrency ?: defaultCurrencySymbol
    LaunchedEffect(activeCurrency) {
        currentCurrencySymbol = activeCurrency
    }

    val drawerItems = listOf(
        DrawerMenuItem(R.string.menu_settings, Icons.Default.Settings, SettingDialogType.SETTINGS, 0, "drawer_menu_settings"),
        DrawerMenuItem(R.string.menu_profile, Icons.Default.Person, SettingDialogType.PROFILE, 0, "drawer_menu_profile"),
        DrawerMenuItem(R.string.menu_wizard_fixed, Icons.Default.Build, SettingDialogType.WIZARD_FIXED, 2, "drawer_menu_wizard_fixed"),
        DrawerMenuItem(R.string.menu_preferences, Icons.Default.Palette, SettingDialogType.PREFERENCES, 1, "drawer_menu_preferences"),
        DrawerMenuItem(R.string.menu_backup, Icons.Default.Share, SettingDialogType.BACKUP, 3, "drawer_menu_backup")
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isWizardComplete,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF0F121A),
                modifier = Modifier.width(310.dp)
            ) {
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatarView(
                            profile = dbProfile,
                            size = 48.dp,
                            modifier = Modifier.border(2.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = dbProfile?.userName?.ifBlank { "Usuario" } ?: "Usuario",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = (-0.3).sp
                                )
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "FINANCEFLOW",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8),
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
                Spacer(modifier = Modifier.height(8.dp))

                val customDrawerItemColors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.12f),
                    selectedIconColor = Color(0xFF38BDF8),
                    selectedTextColor = Color(0xFF38BDF8),
                    unselectedContainerColor = Color.Transparent,
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFFE2E8F0)
                )

                drawerItems.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(stringResource(id = item.titleRes), fontWeight = FontWeight.Bold) },
                        icon = { Icon(item.icon, contentDescription = null) },
                        selected = activeDialog == item.dialogType,
                        onClick = {
                            scope.launch { drawerState.close() }
                            selectedSettingsTab = item.tabIndex
                            activeDialog = item.dialogType
                        },
                        colors = customDrawerItemColors,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp).testTag(item.tag)
                    )
                }

                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.menu_about), fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showAboutDialog = true
                    },
                    colors = customDrawerItemColors,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp).testTag("drawer_menu_about")
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(id = R.string.menu_report_error), fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        sendFeedback(context)
                    },
                    colors = customDrawerItemColors,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp).testTag("drawer_menu_report_error")
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(horizontal = 20.dp))

                Text(
                    text = "Versión ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(start = 24.dp, bottom = 16.dp, top = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(FinanceSoftBg),
            topBar = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isWizardComplete) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(percent = 50))
                                        .background(FinanceTeal.copy(alpha = 0.08f))
                                        .clickable {
                                            scope.launch {
                                                drawerState.open()
                                            }
                                        }
                                        .size(40.dp)
                                        .testTag("hamburger_menu_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Abrir menú",
                                        tint = FinanceTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FinanceTeal.copy(alpha = 0.10f))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = appLogoIcon,
                                    contentDescription = "Logo",
                                    tint = FinanceTeal,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "FinanceFlow",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = FinanceSlateDark,
                                        letterSpacing = (-0.5).sp
                                    )
                                )
                                Text(
                                    text = "100% Libre y Solidario",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FinanceTeal
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = { showAboutDialog = true },
                            modifier = Modifier.testTag("about_app_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Acerca de la app",
                                tint = FinanceSlateLight
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(FinanceSoftBg),
                color = FinanceSoftBg
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        is Screen.WizardStep1 -> WizardStep1Screen(viewModel, windowWidthSizeClass)
                        is Screen.WizardStep2 -> WizardStep2Screen(viewModel, windowWidthSizeClass)
                        is Screen.WizardStep3 -> WizardStep3Screen(viewModel, windowWidthSizeClass)
                        is Screen.WizardStep4 -> WizardStep4Screen(viewModel, windowWidthSizeClass)
                        is Screen.Dashboard -> DashboardScreen(
                            viewModel = viewModel,
                            windowWidthSizeClass = windowWidthSizeClass,
                            onShowAbout = { showAboutDialog = true },
                            onOpenManagementMenu = {
                                selectedSettingsTab = 3
                                activeDialog = SettingDialogType.BACKUP
                            }
                        )
                    }
                }
            }
        }
    }

    
    if (activeDialog == SettingDialogType.SETTINGS ||
        activeDialog == SettingDialogType.PROFILE ||
        activeDialog == SettingDialogType.WIZARD_FIXED ||
        activeDialog == SettingDialogType.PREFERENCES ||
        activeDialog == SettingDialogType.BACKUP ||
        activeDialog == SettingDialogType.DATABASE
    ) {
        val tabToOpen = when (activeDialog) {
            SettingDialogType.PROFILE -> 0
            SettingDialogType.PREFERENCES -> 1
            SettingDialogType.WIZARD_FIXED -> 2
            SettingDialogType.BACKUP, SettingDialogType.DATABASE -> 3
            else -> selectedSettingsTab
        }
        UnifiedSettingsDialog(
            initialTab = tabToOpen,
            dbProfile = dbProfile,
            viewModel = viewModel,
            onDismiss = { activeDialog = SettingDialogType.NONE }
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}
