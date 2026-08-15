package com.example

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.example.data.NavigationTab
import com.example.ui.components.AdminDashboardDialog
import com.example.ui.components.AdminPinDialog
import com.example.ui.components.AuthDialog
import com.example.ui.components.ConnectingOverlay
import com.example.ui.components.BottomNavBar
import com.example.ui.components.ConfettiBalloonsOverlay
import com.example.ui.components.HeaderBar
import com.example.ui.components.SplashScreen
import com.example.ui.screens.CashOutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MineScreen
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.DataCashTheme
import com.example.utils.NetworkUtils
import com.example.viewmodel.DataCashViewModel

import android.util.Log
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            try {
                if (FirebaseApp.getApps(applicationContext).isEmpty()) {
                    try {
                        FirebaseApp.initializeApp(applicationContext)
                    } catch (e: Throwable) {
                        val options = com.google.firebase.FirebaseOptions.Builder()
                            .setApplicationId("1:123456789012:android:1234567890abcdef")
                            .setApiKey("AIzaSyDummyApiKeyDataCashPKApp")
                            .setProjectId("datacash-pk-app")
                            .build()
                        FirebaseApp.initializeApp(applicationContext, options)
                    }
                }
            } catch (e: Throwable) {
                Log.e("MainActivity", "FirebaseApp init failed: ${e.message}")
            }
            try {
                enableEdgeToEdge()
            } catch (e: Throwable) {
                Log.e("MainActivity", "enableEdgeToEdge failed: ${e.message}")
            }
            setContent {
                val viewModel: DataCashViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current

                DataCashTheme(darkTheme = uiState.isDarkTheme) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (uiState.isUserLoggedIn) {
                            DataCashMainApp(
                                uiState = uiState,
                                onToggleTheme = { viewModel.toggleTheme() },
                                onTabSelected = { viewModel.selectTab(it) },
                                onStartSelling = {
                                    val isOnline = NetworkUtils.isNetworkAvailable(context)
                                    viewModel.startSelling(isOnline)
                                },
                                onStopSelling = { viewModel.stopSelling() },
                                onCashOutCurrent = { viewModel.triggerCashOutCelebration() },
                                onResetSelling = { viewModel.resetSelling() },
                                onWithdrawSubmit = { method, holder, number, amount ->
                                    viewModel.submitWithdrawal(method, holder, number, amount)
                                },
                                onConfirmWithdrawal = { viewModel.confirmPendingWithdrawal() },
                                onClearNotice = { viewModel.clearUserNotice() },
                                onOpenAuth = { isSignUp -> viewModel.openAuthDialog(isSignUp) },
                                onCloseAuth = { viewModel.closeAuthDialog() },
                                onAuthTabSwitch = { isSignUp -> viewModel.setAuthSignUpMode(isSignUp) },
                                onSignIn = { email, pass -> viewModel.signInWithFirebase(email, pass, context) },
                                onSignUp = { email, pass, name -> viewModel.signUpWithFirebase(email, pass, name, context) },
                                onGoogleSignIn = { viewModel.signInWithGoogle() },
                                onNetworkConnectionLost = { viewModel.onNetworkConnectionLost() },
                                onLogout = { viewModel.signOutFirebase() },
                                onOpenAdminPin = { viewModel.openAdminPinDialog() },
                                onCloseAdminPin = { viewModel.closeAdminPinDialog() },
                                onVerifyAdminPin = { pin -> viewModel.verifyAdminPin(pin) },
                                onCloseAdminDashboard = { viewModel.closeAdminDashboard() },
                                onApproveWithdrawal = { id -> viewModel.approveWithdrawalRequest(id) },
                                onRejectWithdrawal = { id -> viewModel.rejectWithdrawalRequest(id) }
                            )
                        } else {
                            LoginScreen(
                                isLoading = uiState.isAuthLoading,
                                errorMessage = uiState.authErrorMessage,
                                onGoogleSignIn = { viewModel.signInWithGoogle() },
                                onEmailSignIn = { email, pass -> viewModel.signInWithFirebase(email, pass, context) },
                                onEmailSignUp = { email, pass, name -> viewModel.signUpWithFirebase(email, pass, name, context) },
                                isDarkTheme = uiState.isDarkTheme,
                                onToggleTheme = { viewModel.toggleTheme() }
                            )
                        }

                        // Fullscreen Loading Overlay during Firebase authentication transition
                        ConnectingOverlay(
                            isVisible = uiState.isAuthLoading,
                            title = "Connecting to device...",
                            subtitle = "Authenticating session & syncing user profile..."
                        )

                        // Specification 4: Seamless fade out after 5 seconds to reveal main dashboard
                        AnimatedVisibility(
                            visible = uiState.isSplashActive,
                            enter = fadeIn(),
                            exit = fadeOut(animationSpec = tween(durationMillis = 700))
                        ) {
                            SplashScreen(
                                progress = uiState.splashProgress,
                                onSkip = { viewModel.dismissSplash() }
                            )
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            Log.e("MainActivity", "Fatal startup error prevented: ${e.message}", e)
        }
    }
}

@Composable
fun DataCashMainApp(
    uiState: com.example.viewmodel.DataCashUiState,
    onToggleTheme: () -> Unit,
    onTabSelected: (NavigationTab) -> Unit,
    onStartSelling: () -> Unit,
    onStopSelling: () -> Unit,
    onCashOutCurrent: () -> Unit,
    onResetSelling: () -> Unit,
    onWithdrawSubmit: (com.example.data.PaymentMethod, String, String, String) -> Boolean,
    onConfirmWithdrawal: () -> Unit,
    onClearNotice: () -> Unit,
    onOpenAuth: (Boolean) -> Unit,
    onCloseAuth: () -> Unit,
    onAuthTabSwitch: (Boolean) -> Unit,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onNetworkConnectionLost: () -> Unit,
    onLogout: () -> Unit,
    onOpenAdminPin: () -> Unit,
    onCloseAdminPin: () -> Unit,
    onVerifyAdminPin: (String) -> Unit,
    onCloseAdminDashboard: () -> Unit,
    onApproveWithdrawal: (String) -> Unit,
    onRejectWithdrawal: (String) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity

    // Requirement 2: Keep Screen Awake (No Screen Timeout) while selling is active
    DisposableEffect(uiState.isSellingActive) {
        if (uiState.isSellingActive) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Monitor active network connectivity while MB selling is active
    LaunchedEffect(uiState.isSellingActive) {
        if (uiState.isSellingActive) {
            while (isActive) {
                delay(1000L)
                val isOnline = NetworkUtils.isNetworkAvailable(context)
                if (!isOnline) {
                    onNetworkConnectionLost()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                HeaderBar(
                    userProfile = uiState.userProfile,
                    isDarkTheme = uiState.isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onAvatarClick = { onTabSelected(NavigationTab.MINE) }
                )
            },
            bottomBar = {
                BottomNavBar(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = onTabSelected
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF047857) // Dark Green
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = snackbarData.visuals.message,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (uiState.selectedTab) {
                    NavigationTab.HOME -> {
                        HomeScreen(
                            uiState = uiState,
                            onStartSelling = onStartSelling,
                            onStopSelling = onStopSelling,
                            onCashOutCurrent = onCashOutCurrent,
                            onResetSelling = onResetSelling
                        )
                    }

                    NavigationTab.CASH_OUT -> {
                        CashOutScreen(
                            uiState = uiState,
                            onWithdrawSubmit = { method, holder, num, amt ->
                                onWithdrawSubmit(method, holder, num, amt)
                            }
                        )
                    }

                    NavigationTab.MINE -> {
                        MineScreen(
                            uiState = uiState,
                            onOpenAuth = onOpenAuth,
                            onLogoutConfirmed = {
                                onLogout()
                                onTabSelected(NavigationTab.HOME)
                            },
                            onOpenAdminPin = onOpenAdminPin
                        )
                    }
                }
            }
        }

        // Admin PIN Dialog
        if (uiState.isAdminPinDialogOpen) {
            AdminPinDialog(
                errorMessage = uiState.adminPinError,
                onDismiss = onCloseAdminPin,
                onSubmitPin = onVerifyAdminPin
            )
        }

        // Admin Dashboard Fullscreen Overlay
        if (uiState.isAdminDashboardOpen) {
            AdminDashboardDialog(
                withdrawalList = uiState.withdrawalHistory,
                onDismiss = onCloseAdminDashboard,
                onApproveRequest = onApproveWithdrawal,
                onRejectRequest = onRejectWithdrawal
            )
        }

        // Auth Dialog if active
        if (uiState.showAuthDialog) {
            AuthDialog(
                isSignUp = uiState.isSignUpMode,
                isLoading = uiState.isAuthLoading,
                errorMessage = uiState.authErrorMessage,
                onDismiss = onCloseAuth,
                onTabSwitch = onAuthTabSwitch,
                onSignIn = onSignIn,
                onSignUp = onSignUp,
                onGoogleSignIn = onGoogleSignIn
            )
        }

        // Fullscreen Confetti Balloons Overlay when Cashing out (3 SECONDS)
        if (uiState.isCelebrationActive) {
            ConfettiBalloonsOverlay(
                message = uiState.celebrationMessage
            )
        }

        // Dialog alert for user notice if active
        uiState.userNoticeMessage?.let { notice ->
            val isPendingConfirmation = uiState.pendingWithdrawalRecord != null

            AlertDialog(
                onDismissRequest = onClearNotice,
                title = { Text("DataCash PK Notification", fontWeight = FontWeight.Bold) },
                text = { Text(notice) },
                confirmButton = {
                    Button(
                        onClick = {
                            if (isPendingConfirmation) {
                                onConfirmWithdrawal()
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "⚡ Request Successful! Your withdrawal is pending approval. You can check the status in your cash-out history.",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } else {
                                onClearNotice()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoEmerald)
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = if (isPendingConfirmation) {
                    @Composable {
                        OutlinedButton(onClick = onClearNotice) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }
                    }
                } else null
            )
        }
    }
}
