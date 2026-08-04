package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.EarningRecord
import com.example.data.NavigationTab
import com.example.data.PaymentMethod
import com.example.data.UserProfile
import com.example.data.WithdrawalRecord
import com.example.data.WithdrawalStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.data.FirebaseRepository

data class DataCashUiState(
    val isSplashActive: Boolean = true,
    val splashProgress: Float = 0f,
    val isDarkTheme: Boolean = false,
    val selectedTab: NavigationTab = NavigationTab.HOME,
    val userProfile: UserProfile = UserProfile(),
    val availableBalance: Double = 1250.0,
    val todaysEarnings: Double = 450.0,
    val totalEarnings: Double = 5800.0,
    val totalMbSold: Double = 18450.0,
    val totalWithdrawn: Double = 4200.0,
    
    // Firebase Auth & Cloud Firestore Sync State
    val isUserLoggedIn: Boolean = false,
    val authUserUid: String? = null,
    val isAuthLoading: Boolean = false,
    val authErrorMessage: String? = null,
    val showAuthDialog: Boolean = false,
    val isSignUpMode: Boolean = false,
    val cloudSyncStatus: String = "Cloud & Local Sync Ready",

    // Admin Panel State (Owner: Bilal Iqbal Jamali)
    val isAdminPinDialogOpen: Boolean = false,
    val isAdminDashboardOpen: Boolean = false,
    val adminPinError: String? = null,

    // Live Selling Widget
    val isSellingActive: Boolean = false,
    val currentWidgetMbSold: Double = 0.0,
    val isCelebrationActive: Boolean = false,
    val celebrationMessage: String = "",
    
    // History Lists
    val earningHistory: List<EarningRecord> = listOf(
        EarningRecord(mbSold = 3000.0, pkrEarned = 1000.0, timestamp = "Today, 02:15 PM"),
        EarningRecord(mbSold = 1500.0, pkrEarned = 500.0, timestamp = "Today, 11:30 AM"),
        EarningRecord(mbSold = 2100.0, pkrEarned = 700.0, timestamp = "Yesterday, 08:45 PM"),
        EarningRecord(mbSold = 4500.0, pkrEarned = 1500.0, timestamp = "31 Jul, 04:20 PM")
    ),
    val withdrawalHistory: List<WithdrawalRecord> = listOf(
        WithdrawalRecord(
            id = "TXN-982101",
            paymentMethod = PaymentMethod.EASYPAISA,
            accountHolder = "Muhammad Ali",
            accountNumber = "03019876543",
            requestedAmount = 1500.0,
            status = WithdrawalStatus.PENDING_1_HR,
            timestamp = "Just Now",
            userName = "Muhammad Ali"
        ),
        WithdrawalRecord(
            id = "TXN-982102",
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            accountHolder = "Bilal Iqbal Jamali",
            accountNumber = "Meezan Bank - PK36MEZN00012345678901",
            requestedAmount = 2500.0,
            status = WithdrawalStatus.PENDING_1_HR,
            timestamp = "Today, 10:15 AM",
            userName = "Bilal Iqbal Jamali",
            bankName = "Meezan Bank"
        ),
        WithdrawalRecord(
            id = "TXN-982103",
            paymentMethod = PaymentMethod.EASYPAISA,
            accountHolder = "Bilal Iqbal Jamali",
            accountNumber = "03001234567",
            requestedAmount = 1000.0,
            status = WithdrawalStatus.COMPLETED,
            timestamp = "31 Jul 2026, 05:00 PM"
        ),
        WithdrawalRecord(
            id = "TXN-982104",
            paymentMethod = PaymentMethod.JAZZCASH,
            accountHolder = "Bilal Iqbal Jamali",
            accountNumber = "03009876543",
            requestedAmount = 3200.0,
            status = WithdrawalStatus.COMPLETED,
            timestamp = "28 Jul 2026, 02:15 PM"
        )
    ),
    val userNoticeMessage: String? = null
) {
    val currentWidgetEarnings: Double
        get() = currentWidgetMbSold / 3.0
}

class DataCashViewModel : ViewModel() {

    private val firebaseRepository = FirebaseRepository()

    private val _uiState = MutableStateFlow(DataCashUiState())
    val uiState: StateFlow<DataCashUiState> = _uiState.asStateFlow()

    init {
        // Check current Firebase User on startup
        checkInitialFirebaseUser()

        // Start 5-second Splash Timer
        startSplashTimer()
        
        // Start Live Selling Coroutine ticker (7 MB/s when active)
        startMbSellingTicker()
    }

    private fun checkInitialFirebaseUser() {
        try {
            val user = firebaseRepository.getCurrentUser()
            if (user != null) {
                _uiState.update {
                    it.copy(
                        isUserLoggedIn = true,
                        authUserUid = user.uid,
                        userProfile = UserProfile(
                            name = user.displayName ?: "DataCash User",
                            email = user.email ?: "user@datacash.app"
                        ),
                        cloudSyncStatus = "Firebase Connected: ${user.email}"
                    )
                }
                loadUserDataFromFirestore(user.uid)
            }
        } catch (e: Throwable) {
            // Firebase unavailable or missing configuration
        }
    }

    private fun loadUserDataFromFirestore(uid: String) {
        viewModelScope.launch {
            val data = firebaseRepository.fetchUserData(uid)
            if (data != null) {
                val name = data["name"] as? String ?: _uiState.value.userProfile.name
                val email = data["email"] as? String ?: _uiState.value.userProfile.email
                val balance = (data["availableBalance"] as? Number)?.toDouble() ?: _uiState.value.availableBalance
                val todays = (data["todaysEarnings"] as? Number)?.toDouble() ?: _uiState.value.todaysEarnings
                val totalEarn = (data["totalEarnings"] as? Number)?.toDouble() ?: _uiState.value.totalEarnings
                val mbSold = (data["totalMbSold"] as? Number)?.toDouble() ?: _uiState.value.totalMbSold
                val withdrawn = (data["totalWithdrawn"] as? Number)?.toDouble() ?: _uiState.value.totalWithdrawn

                _uiState.update { state ->
                    state.copy(
                        userProfile = UserProfile(name = name, email = email),
                        availableBalance = balance,
                        todaysEarnings = todays,
                        totalEarnings = totalEarn,
                        totalMbSold = mbSold,
                        totalWithdrawn = withdrawn,
                        cloudSyncStatus = "Firestore Data Synced"
                    )
                }
            }
        }
    }

    fun openAuthDialog(isSignUp: Boolean = false) {
        _uiState.update {
            it.copy(
                showAuthDialog = true,
                isSignUpMode = isSignUp,
                authErrorMessage = null
            )
        }
    }

    fun closeAuthDialog() {
        _uiState.update {
            it.copy(
                showAuthDialog = false,
                authErrorMessage = null
            )
        }
    }

    fun setAuthSignUpMode(isSignUp: Boolean) {
        _uiState.update {
            it.copy(
                isSignUpMode = isSignUp,
                authErrorMessage = null
            )
        }
    }

    fun signInWithFirebase(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
            val result = firebaseRepository.signInWithEmail(email, pass)
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        showAuthDialog = false,
                        isUserLoggedIn = true,
                        authUserUid = user.uid,
                        userProfile = UserProfile(
                            name = user.displayName ?: email.substringBefore("@").capitalize(),
                            email = user.email ?: email
                        ),
                        cloudSyncStatus = "Firebase Connected",
                        userNoticeMessage = "Welcome back, ${user.email}! Firebase session active."
                    )
                }
                loadUserDataFromFirestore(user.uid)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authErrorMessage = error.localizedMessage ?: "Sign in failed. Check credentials."
                    )
                }
            }
        }
    }

    fun signUpWithFirebase(email: String, pass: String, name: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
            val result = firebaseRepository.signUpWithEmail(email, pass, name)
            result.onSuccess { user ->
                val displayName = name.ifBlank { email.substringBefore("@") }
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        showAuthDialog = false,
                        isUserLoggedIn = true,
                        authUserUid = user.uid,
                        userProfile = UserProfile(
                            name = displayName,
                            email = user.email ?: email
                        ),
                        cloudSyncStatus = "Firebase Account Created & Synced",
                        userNoticeMessage = "Firebase account created successfully for $email!"
                    )
                }
                syncUserDataToFirestore()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authErrorMessage = error.localizedMessage ?: "Sign up failed. Ensure email is valid and password is at least 6 characters."
                    )
                }
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
            val currentEmail = _uiState.value.userProfile.email.ifBlank { "bj889780@gmail.com" }
            _uiState.update {
                it.copy(
                    isAuthLoading = false,
                    showAuthDialog = false,
                    isUserLoggedIn = true,
                    authUserUid = "google-uid-${System.currentTimeMillis()}",
                    userProfile = UserProfile(
                        name = "Google User",
                        email = currentEmail
                    ),
                    cloudSyncStatus = "Google Sign-In Active",
                    userNoticeMessage = "Successfully logged in with Google ($currentEmail)"
                )
            }
        }
    }

    fun signOutFirebase() {
        firebaseRepository.signOut()
        _uiState.update {
            it.copy(
                isUserLoggedIn = false,
                authUserUid = null,
                cloudSyncStatus = "Guest Mode",
                userNoticeMessage = "Logged out from Firebase session."
            )
        }
    }

    private fun syncUserDataToFirestore() {
        val uid = _uiState.value.authUserUid ?: return
        val state = _uiState.value
        viewModelScope.launch {
            firebaseRepository.saveUserData(
                uid = uid,
                name = state.userProfile.name,
                email = state.userProfile.email,
                availableBalance = state.availableBalance,
                todaysEarnings = state.todaysEarnings,
                totalEarnings = state.totalEarnings,
                totalMbSold = state.totalMbSold,
                totalWithdrawn = state.totalWithdrawn
            )
        }
    }

    private fun startSplashTimer() {
        viewModelScope.launch {
            val totalDurationMs = 5000L
            val intervalMs = 100L
            var elapsed = 0L
            while (elapsed < totalDurationMs && _uiState.value.isSplashActive) {
                delay(intervalMs)
                elapsed += intervalMs
                val progress = elapsed.toFloat() / totalDurationMs.toFloat()
                _uiState.update { it.copy(splashProgress = progress.coerceAtMost(1f)) }
            }
            _uiState.update { it.copy(isSplashActive = false) }
        }
    }

    fun dismissSplash() {
        _uiState.update { it.copy(isSplashActive = false) }
    }

    private fun startMbSellingTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1000L) // tick every 1 second
                val state = _uiState.value
                if (state.isSellingActive) {
                    val rateMbPerSec = 14.0
                    val nextMbSold = state.currentWidgetMbSold + rateMbPerSec
                    _uiState.update {
                        it.copy(currentWidgetMbSold = nextMbSold)
                    }
                }
            }
        }
    }

    fun onNetworkConnectionLost() {
        if (_uiState.value.isSellingActive) {
            autoCashOutFinished("Your MBs are finished. Earning credited automatically.")
        }
    }

    fun toggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun selectTab(tab: NavigationTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun startSelling(isOnline: Boolean = true) {
        if (!isOnline) {
            _uiState.update {
                it.copy(
                    isSellingActive = false,
                    userNoticeMessage = "Notice: You do not have an active internet connection or MBs to sell."
                )
            }
            return
        }
        _uiState.update { it.copy(isSellingActive = true) }
    }

    fun stopSelling() {
        _uiState.update { it.copy(isSellingActive = false) }
    }

    fun resetSelling() {
        _uiState.update { it.copy(currentWidgetMbSold = 0.0) }
    }

    fun autoCashOutFinished(customMessage: String? = null) {
        val currentState = _uiState.value
        if (currentState.currentWidgetMbSold <= 0) {
            _uiState.update {
                it.copy(
                    isSellingActive = false,
                    userNoticeMessage = customMessage
                )
            }
            return
        }

        val mbSold = currentState.currentWidgetMbSold
        val pkrEarned = currentState.currentWidgetEarnings

        _uiState.update {
            it.copy(
                isSellingActive = false,
                isCelebrationActive = true,
                celebrationMessage = "+Rs. %.2f Cash Added!".format(pkrEarned),
                userNoticeMessage = customMessage
            )
        }

        viewModelScope.launch {
            val newRecord = EarningRecord(mbSold = mbSold, pkrEarned = pkrEarned)
            _uiState.update { state ->
                state.copy(
                    availableBalance = state.availableBalance + pkrEarned,
                    todaysEarnings = state.todaysEarnings + pkrEarned,
                    totalEarnings = state.totalEarnings + pkrEarned,
                    totalMbSold = state.totalMbSold + mbSold,
                    earningHistory = listOf(newRecord) + state.earningHistory,
                    currentWidgetMbSold = 0.0
                )
            }

            val uid = _uiState.value.authUserUid
            if (uid != null) {
                firebaseRepository.recordEarningSale(uid, newRecord)
                syncUserDataToFirestore()
            }

            delay(3000L)
            _uiState.update { it.copy(isCelebrationActive = false, celebrationMessage = "") }
        }
    }

    fun triggerCashOutCelebration() {
        autoCashOutFinished(null)
    }

    fun submitWithdrawal(
        paymentMethod: PaymentMethod,
        accountHolder: String,
        accountNumber: String,
        amountText: String
    ): Boolean {
        val amount = amountText.toDoubleOrNull()
        if (amount == null) {
            _uiState.update { it.copy(userNoticeMessage = "Please enter a valid numeric amount.") }
            return false
        }

        if (accountHolder.isBlank()) {
            _uiState.update { it.copy(userNoticeMessage = "Please enter Account Holder Name.") }
            return false
        }

        if (accountNumber.isBlank()) {
            _uiState.update { it.copy(userNoticeMessage = "Please enter Mobile / Account Number.") }
            return false
        }

        if (amount < 300.0) {
            _uiState.update { it.copy(userNoticeMessage = "Minimum withdrawal amount is PKR 300.") }
            return false
        }

        if (amount > 15000.0) {
            _uiState.update { it.copy(userNoticeMessage = "Notice: The maximum withdrawal limit is PKR 15,000 per request. Please enter a valid amount.") }
            return false
        }

        val currentBal = _uiState.value.availableBalance
        if (amount > currentBal) {
            _uiState.update { it.copy(userNoticeMessage = "Insufficient balance! Your current balance is PKR %.2f.".format(currentBal)) }
            return false
        }

        val record = WithdrawalRecord(
            paymentMethod = paymentMethod,
            accountHolder = accountHolder,
            accountNumber = accountNumber,
            requestedAmount = amount,
            adminFee = 50.0,
            status = WithdrawalStatus.PENDING_1_HR
        )

        _uiState.update { state ->
            state.copy(
                availableBalance = state.availableBalance - amount,
                totalWithdrawn = state.totalWithdrawn + amount,
                withdrawalHistory = listOf(record) + state.withdrawalHistory,
                userNoticeMessage = "Withdrawal request of PKR %.2f submitted successfully! (Net Receiving: PKR %.2f after PKR 50 fee)".format(amount, record.netAmount)
            )
        }

        // Sync to Firestore
        val uid = _uiState.value.authUserUid
        if (uid != null) {
            viewModelScope.launch {
                firebaseRepository.recordCashOutRequest(uid, record)
                syncUserDataToFirestore()
            }
        }
        return true
    }

    fun clearUserNotice() {
        _uiState.update { it.copy(userNoticeMessage = null) }
    }

    // Admin Dashboard Functions (Owner: Bilal Iqbal Jamali)
    fun openAdminPinDialog() {
        _uiState.update {
            it.copy(
                isAdminPinDialogOpen = true,
                adminPinError = null
            )
        }
    }

    fun closeAdminPinDialog() {
        _uiState.update {
            it.copy(
                isAdminPinDialogOpen = false,
                adminPinError = null
            )
        }
    }

    fun verifyAdminPin(enteredPin: String) {
        if (enteredPin.trim() == "bj@#?") {
            _uiState.update {
                it.copy(
                    isAdminPinDialogOpen = false,
                    isAdminDashboardOpen = true,
                    adminPinError = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    adminPinError = "Incorrect Security Code. Access Denied."
                )
            }
        }
    }

    fun closeAdminDashboard() {
        _uiState.update {
            it.copy(isAdminDashboardOpen = false)
        }
    }

    fun approveWithdrawalRequest(requestId: String) {
        val currentHistory = _uiState.value.withdrawalHistory
        val target = currentHistory.find { it.id == requestId } ?: return

        val updatedHistory = currentHistory.map { record ->
            if (record.id == requestId) {
                record.copy(status = WithdrawalStatus.COMPLETED)
            } else record
        }

        _uiState.update { state ->
            state.copy(
                withdrawalHistory = updatedHistory,
                userNoticeMessage = "Withdrawal #${target.id} of PKR ${"%.2f".format(target.requestedAmount)} APPROVED by Owner Bilal Iqbal Jamali!"
            )
        }

        viewModelScope.launch {
            firebaseRepository.updateWithdrawalStatus(requestId, WithdrawalStatus.COMPLETED)
        }
    }

    fun rejectWithdrawalRequest(requestId: String) {
        val currentHistory = _uiState.value.withdrawalHistory
        val target = currentHistory.find { it.id == requestId } ?: return

        val updatedHistory = currentHistory.map { record ->
            if (record.id == requestId) {
                record.copy(status = WithdrawalStatus.REJECTED)
            } else record
        }

        // Refund requested amount back to available balance
        _uiState.update { state ->
            state.copy(
                availableBalance = state.availableBalance + target.requestedAmount,
                totalWithdrawn = (state.totalWithdrawn - target.requestedAmount).coerceAtLeast(0.0),
                withdrawalHistory = updatedHistory,
                userNoticeMessage = "Withdrawal #${target.id} REJECTED! PKR ${"%.2f".format(target.requestedAmount)} refunded to user balance."
            )
        }

        viewModelScope.launch {
            firebaseRepository.updateWithdrawalStatus(requestId, WithdrawalStatus.REJECTED)
            syncUserDataToFirestore()
        }
    }
}
