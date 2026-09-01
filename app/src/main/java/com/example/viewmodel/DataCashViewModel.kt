package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.EarningRecord
import com.example.data.NavigationTab
import com.example.data.PaymentMethod
import com.example.data.UserProfile
import com.example.data.WithdrawalRecord
import com.example.data.WithdrawalStatus
import com.example.data.FirebaseRepository
import com.example.utils.SessionManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DataCashUiState(
    val isSplashActive: Boolean = true,
    val splashProgress: Float = 0f,
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
    val authSuccessMessage: String? = null,
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
            status = WithdrawalStatus.COMPLETED,
            timestamp = "2 Days Ago, 02:15 PM",
            userName = "Muhammad Ali",
            createdMs = System.currentTimeMillis() - 48 * 60 * 60 * 1000L
        ),
        WithdrawalRecord(
            id = "TXN-982102",
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            accountHolder = "Bilal Iqbal Jamali",
            accountNumber = "Meezan Bank - PK36MEZN00012345678901",
            requestedAmount = 2500.0,
            status = WithdrawalStatus.COMPLETED,
            timestamp = "3 Days Ago, 10:15 AM",
            userName = "Bilal Iqbal Jamali",
            bankName = "Meezan Bank",
            createdMs = System.currentTimeMillis() - 72 * 60 * 60 * 1000L
        ),
        WithdrawalRecord(
            id = "TXN-982103",
            paymentMethod = PaymentMethod.EASYPAISA,
            accountHolder = "Bilal Iqbal Jamali",
            accountNumber = "03001234567",
            requestedAmount = 1000.0,
            status = WithdrawalStatus.COMPLETED,
            timestamp = "31 Jul 2026, 05:00 PM",
            createdMs = System.currentTimeMillis() - 96 * 60 * 60 * 1000L
        ),
        WithdrawalRecord(
            id = "TXN-982104",
            paymentMethod = PaymentMethod.JAZZCASH,
            accountHolder = "Bilal Iqbal Jamali",
            accountNumber = "03009876543",
            requestedAmount = 3200.0,
            status = WithdrawalStatus.COMPLETED,
            timestamp = "28 Jul 2026, 02:15 PM",
            createdMs = System.currentTimeMillis() - 120 * 60 * 60 * 1000L
        )
    ),
    val userNoticeMessage: String? = null,
    val pendingWithdrawalRecord: WithdrawalRecord? = null
) {
    val currentWidgetEarnings: Double
        get() = currentWidgetMbSold / 3.0
}

class DataCashViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseRepository = FirebaseRepository(application)

    private val _uiState = MutableStateFlow(DataCashUiState())
    val uiState: StateFlow<DataCashUiState> = _uiState.asStateFlow()

    init {
        // 1. Check & restore persistent session on startup (Firebase + SharedPreferences)
        checkInitialFirebaseUser()

        // 2. Listen to Firebase Auth state changes
        listenToAuthStateChanges()

        // 3. Start 5-second Splash Timer
        startSplashTimer()
        
        // 4. Start Live Selling Coroutine ticker (7 MB/s when active)
        startMbSellingTicker()
    }

    private fun checkInitialFirebaseUser() {
        try {
            val context = getApplication<Application>()
            val isLocallyLoggedIn = SessionManager.isUserLoggedIn(context)
            val firebaseUser = firebaseRepository.getCurrentUser()

            if (firebaseUser != null || isLocallyLoggedIn) {
                val uid = firebaseUser?.uid ?: SessionManager.getUserUid(context) ?: "user-uid-default"
                val savedProfile = SessionManager.getUserProfile(context)
                val resolvedName = firebaseUser?.displayName ?: savedProfile.name
                val resolvedEmail = firebaseUser?.email ?: savedProfile.email
                val resolvedPhoto = firebaseUser?.photoUrl?.toString() ?: savedProfile.photoUrl
                val savedBalances = SessionManager.getSavedBalances(context)

                val initialBal = if (savedBalances.balance > 0) savedBalances.balance else 1250.0
                val initialTodays = if (savedBalances.todays > 0) savedBalances.todays else 450.0
                val initialTotal = if (savedBalances.total > 0) savedBalances.total else 5800.0
                val initialMb = if (savedBalances.mbSold > 0) savedBalances.mbSold else 18450.0
                val initialWithdrawn = if (savedBalances.withdrawn > 0) savedBalances.withdrawn else 4200.0

                _uiState.update {
                    it.copy(
                        isUserLoggedIn = true,
                        authUserUid = uid,
                        userProfile = UserProfile(
                            name = resolvedName,
                            email = resolvedEmail,
                            photoUrl = resolvedPhoto
                        ),
                        availableBalance = initialBal,
                        todaysEarnings = initialTodays,
                        totalEarnings = initialTotal,
                        totalMbSold = initialMb,
                        totalWithdrawn = initialWithdrawn,
                        cloudSyncStatus = "Firebase Connected: $resolvedEmail"
                    )
                }
                loadUserDataFromFirestore(uid)
            } else {
                _uiState.update {
                    it.copy(
                        isUserLoggedIn = false,
                        authUserUid = null,
                        cloudSyncStatus = "Authentication Required"
                    )
                }
            }
        } catch (e: Throwable) {
            val context = getApplication<Application>()
            val isLocallyLoggedIn = SessionManager.isUserLoggedIn(context)
            if (isLocallyLoggedIn) {
                val uid = SessionManager.getUserUid(context) ?: "local-user"
                val profile = SessionManager.getUserProfile(context)
                val balances = SessionManager.getSavedBalances(context)
                _uiState.update {
                    it.copy(
                        isUserLoggedIn = true,
                        authUserUid = uid,
                        userProfile = profile,
                        availableBalance = balances.balance,
                        todaysEarnings = balances.todays,
                        totalEarnings = balances.total,
                        cloudSyncStatus = "Persistent Session Active"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isUserLoggedIn = false,
                        authUserUid = null,
                        cloudSyncStatus = "Authentication Required"
                    )
                }
            }
        }
    }

    private fun listenToAuthStateChanges() {
        firebaseRepository.addAuthStateListener { user ->
            if (user != null) {
                val context = getApplication<Application>()
                val name = user.displayName ?: _uiState.value.userProfile.name
                val email = user.email ?: _uiState.value.userProfile.email
                val photoUrl = user.photoUrl?.toString() ?: _uiState.value.userProfile.photoUrl

                SessionManager.saveSession(
                    context = context,
                    uid = user.uid,
                    name = name,
                    email = email,
                    photoUrl = photoUrl,
                    balance = _uiState.value.availableBalance,
                    todays = _uiState.value.todaysEarnings,
                    total = _uiState.value.totalEarnings,
                    mbSold = _uiState.value.totalMbSold,
                    withdrawn = _uiState.value.totalWithdrawn
                )

                _uiState.update {
                    it.copy(
                        isUserLoggedIn = true,
                        authUserUid = user.uid,
                        userProfile = UserProfile(name = name, email = email, photoUrl = photoUrl),
                        cloudSyncStatus = "Firebase Connected: $email"
                    )
                }
            }
        }
    }

    private fun loadUserDataFromFirestore(uid: String) {
        viewModelScope.launch {
            val data = firebaseRepository.fetchUserData(uid)
            if (data != null) {
                val name = data["name"] as? String ?: _uiState.value.userProfile.name
                val email = data["email"] as? String ?: _uiState.value.userProfile.email
                val photoUrl = data["photoUrl"] as? String ?: _uiState.value.userProfile.photoUrl
                val balance = (data["availableBalance"] as? Number)?.toDouble() ?: _uiState.value.availableBalance
                val todays = (data["todaysEarnings"] as? Number)?.toDouble() ?: _uiState.value.todaysEarnings
                val totalEarn = (data["totalEarnings"] as? Number)?.toDouble() ?: _uiState.value.totalEarnings
                val mbSold = (data["totalMbSold"] as? Number)?.toDouble() ?: _uiState.value.totalMbSold
                val withdrawn = (data["totalWithdrawn"] as? Number)?.toDouble() ?: _uiState.value.totalWithdrawn

                val context = getApplication<Application>()
                SessionManager.saveSession(
                    context = context,
                    uid = uid,
                    name = name,
                    email = email,
                    photoUrl = photoUrl,
                    balance = balance,
                    todays = todays,
                    total = totalEarn,
                    mbSold = mbSold,
                    withdrawn = withdrawn
                )

                _uiState.update { state ->
                    state.copy(
                        userProfile = UserProfile(name = name, email = email, photoUrl = photoUrl),
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

    fun signInWithFirebase(email: String, pass: String, context: android.content.Context? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
            try {
                val result = firebaseRepository.signInWithEmail(email, pass, context)
                result.onSuccess { user ->
                    val displayName = user.displayName ?: email.substringBefore("@").replaceFirstChar { char -> char.uppercase() }
                    val userEmail = user.email ?: email
                    val photo = user.photoUrl?.toString()

                    val appCtx = getApplication<Application>()
                    SessionManager.saveSession(
                        context = appCtx,
                        uid = user.uid,
                        name = displayName,
                        email = userEmail,
                        photoUrl = photo,
                        balance = _uiState.value.availableBalance,
                        todays = _uiState.value.todaysEarnings,
                        total = _uiState.value.totalEarnings,
                        mbSold = _uiState.value.totalMbSold,
                        withdrawn = _uiState.value.totalWithdrawn
                    )

                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            showAuthDialog = false,
                            isUserLoggedIn = true,
                            authUserUid = user.uid,
                            selectedTab = NavigationTab.HOME,
                            userProfile = UserProfile(
                                name = displayName,
                                email = userEmail,
                                photoUrl = photo
                            ),
                            cloudSyncStatus = "Firebase Connected: $userEmail",
                            userNoticeMessage = null
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
            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authErrorMessage = e.localizedMessage ?: "Sign in failed. Check credentials."
                    )
                }
            }
        }
    }

    fun signUpWithFirebase(email: String, pass: String, name: String, context: android.content.Context? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
            try {
                val result = firebaseRepository.signUpWithEmail(email, pass, name, context)
                result.onSuccess { user ->
                    val displayName = name.ifBlank { email.substringBefore("@") }
                    val userEmail = user.email ?: email
                    val photo = user.photoUrl?.toString()

                    val appCtx = getApplication<Application>()
                    SessionManager.saveSession(
                        context = appCtx,
                        uid = user.uid,
                        name = displayName,
                        email = userEmail,
                        photoUrl = photo,
                        balance = 1250.0,
                        todays = 450.0,
                        total = 5800.0,
                        mbSold = 18450.0,
                        withdrawn = 4200.0
                    )

                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            showAuthDialog = false,
                            isUserLoggedIn = true,
                            authUserUid = user.uid,
                            selectedTab = NavigationTab.HOME,
                            userProfile = UserProfile(
                                name = displayName,
                                email = userEmail,
                                photoUrl = photo
                            ),
                            cloudSyncStatus = "Firebase Account Created & Synced",
                            userNoticeMessage = null
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
            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authErrorMessage = e.localizedMessage ?: "Sign up failed. Please try again."
                    )
                }
            }
        }
    }

    fun sendPasswordReset(email: String, context: Context? = null) {
        viewModelScope.launch {
            val trimmedEmail = email.trim()
            if (trimmedEmail.isBlank() || !trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
                _uiState.update {
                    it.copy(
                        authErrorMessage = "Please enter a valid email address to reset password.",
                        authSuccessMessage = null
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null, authSuccessMessage = null) }
            try {
                val result = firebaseRepository.sendPasswordResetEmail(trimmedEmail, context)
                result.onSuccess {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authErrorMessage = null,
                            authSuccessMessage = "Password reset instructions sent to $trimmedEmail. Check your inbox to proceed."
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authErrorMessage = error.localizedMessage ?: "Failed to send password reset email.",
                            authSuccessMessage = null
                        )
                    }
                }
            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authErrorMessage = e.localizedMessage ?: "Failed to send password reset email.",
                        authSuccessMessage = null
                    )
                }
            }
        }
    }

    fun clearAuthMessages() {
        _uiState.update { it.copy(authErrorMessage = null, authSuccessMessage = null) }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true, authErrorMessage = null) }
            try {
                val serverClientId = try {
                    val idRes = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                    if (idRes != 0) context.getString(idRes) else "782447979527-datacashpk.apps.googleusercontent.com"
                } catch (e: Throwable) {
                    "782447979527-datacashpk.apps.googleusercontent.com"
                }

                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.id.substringBefore("@")
                    val userEmail = googleIdTokenCredential.id
                    val photo = googleIdTokenCredential.profilePictureUri?.toString()

                    val firebaseResult = firebaseRepository.signInWithGoogleCredential(idToken, context)
                    if (firebaseResult.isSuccess) {
                        val fbUser = firebaseResult.getOrNull()
                        val finalUid = fbUser?.uid ?: "google-$userEmail"
                        val finalName = fbUser?.displayName ?: displayName
                        val finalEmail = fbUser?.email ?: userEmail
                        val finalPhoto = fbUser?.photoUrl?.toString() ?: photo

                        completeSuccessfulSignIn(finalUid, finalName, finalEmail, finalPhoto)
                    } else {
                        val error = firebaseResult.exceptionOrNull()
                        _uiState.update {
                            it.copy(
                                isAuthLoading = false,
                                authErrorMessage = error?.localizedMessage ?: "Firebase Google authentication failed."
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authErrorMessage = "Could not retrieve Google credentials. Please try again."
                        )
                    }
                }
            } catch (e: GetCredentialCancellationException) {
                // User canceled or dismissed the Google account chooser bottom sheet
                Log.d("DataCashViewModel", "Google Sign-In canceled by user")
                _uiState.update { it.copy(isAuthLoading = false) }
            } catch (e: androidx.credentials.exceptions.NoCredentialException) {
                Log.w("DataCashViewModel", "NoCredentialException: No Google accounts found on device")
                _uiState.update {
                    it.copy(
                        isAuthLoading = false,
                        authErrorMessage = "No Google account found on this device. Please sign in with email/password or add a Google account in Android Settings."
                    )
                }
            } catch (e: androidx.credentials.exceptions.GetCredentialCustomException) {
                val msg = e.localizedMessage ?: ""
                Log.w("DataCashViewModel", "GetCredentialCustomException: $msg", e)
                if (msg.contains("cancel", ignoreCase = true) || msg.contains("16", ignoreCase = true)) {
                    _uiState.update { it.copy(isAuthLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authErrorMessage = "Google Sign-In was not completed. Please try again or use Email & Password."
                        )
                    }
                }
            } catch (e: androidx.credentials.exceptions.GetCredentialException) {
                val msg = e.localizedMessage ?: ""
                Log.w("DataCashViewModel", "GetCredentialException: $msg", e)
                if (msg.contains("cancel", ignoreCase = true) || msg.contains("16", ignoreCase = true)) {
                    _uiState.update { it.copy(isAuthLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authErrorMessage = if (msg.isNotBlank()) "Google Sign-In error: $msg" else "Google Sign-In failed. Please try again or use Email login."
                        )
                    }
                }
            } catch (e: Throwable) {
                val msg = e.localizedMessage ?: ""
                val className = e.javaClass.name
                if (msg.contains("cancel", ignoreCase = true) ||
                    msg.contains("16", ignoreCase = true) ||
                    className.contains("Cancellation", ignoreCase = true)
                ) {
                    Log.d("DataCashViewModel", "Google Sign-In canceled or dismissed by user")
                    _uiState.update { it.copy(isAuthLoading = false) }
                } else {
                    Log.e("DataCashViewModel", "signInWithGoogle error", e)
                    _uiState.update {
                        it.copy(
                            isAuthLoading = false,
                            authErrorMessage = if (msg.isNotBlank()) "Google Sign-In error: $msg" else "Google Sign-In failed. Please try again or use Email login."
                        )
                    }
                }
            }
        }
    }

    private fun completeSuccessfulSignIn(
        uid: String,
        userName: String,
        userEmail: String,
        photoUrl: String?
    ) {
        val appCtx = getApplication<Application>()
        val currentBalances = SessionManager.getSavedBalances(appCtx)
        val bal = if (currentBalances.balance > 0) currentBalances.balance else 1250.0
        val todays = if (currentBalances.todays > 0) currentBalances.todays else 450.0
        val total = if (currentBalances.total > 0) currentBalances.total else 5800.0
        val mbSold = if (currentBalances.mbSold > 0) currentBalances.mbSold else 18450.0
        val withdrawn = if (currentBalances.withdrawn > 0) currentBalances.withdrawn else 4200.0

        SessionManager.saveSession(
            context = appCtx,
            uid = uid,
            name = userName,
            email = userEmail,
            photoUrl = photoUrl,
            balance = bal,
            todays = todays,
            total = total,
            mbSold = mbSold,
            withdrawn = withdrawn
        )

        _uiState.update {
            it.copy(
                isAuthLoading = false,
                showAuthDialog = false,
                isUserLoggedIn = true,
                authUserUid = uid,
                selectedTab = NavigationTab.HOME,
                availableBalance = bal,
                todaysEarnings = todays,
                totalEarnings = total,
                totalMbSold = mbSold,
                totalWithdrawn = withdrawn,
                userProfile = UserProfile(
                    name = userName,
                    email = userEmail,
                    photoUrl = photoUrl
                ),
                cloudSyncStatus = "Google Sign-In Active & Firestore Connected",
                userNoticeMessage = null
            )
        }
        syncUserDataToFirestore()
    }

    fun signOutFirebase() {
        try {
            firebaseRepository.signOut()
        } catch (e: Throwable) {
            // ignore
        }
        val appCtx = getApplication<Application>()
        SessionManager.clearSession(appCtx)

        _uiState.update {
            it.copy(
                isUserLoggedIn = false,
                authUserUid = null,
                userProfile = UserProfile(name = "DataCash User", email = "user@datacash.pk"),
                cloudSyncStatus = "Logged Out",
                selectedTab = NavigationTab.HOME,
                showAuthDialog = false,
                userNoticeMessage = null
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
                photoUrl = state.userProfile.photoUrl,
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

    companion object {
        const val MAX_24H_MB_SELLING = 12000.0
        const val MAX_24H_WITHDRAWAL_PKR = 3500.0
        const val EARNING_RATE_PER_MB = 0.3
    }

    fun getMbSoldLast24Hours(): Double {
        val appCtx = getApplication<Application>()
        val local24h = SessionManager.getMbSoldLast24Hours(appCtx)
        val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        val history24h = _uiState.value.earningHistory
            .filter { it.createdMs >= cutoff }
            .sumOf { it.mbSold }
        return maxOf(local24h, history24h)
    }

    fun getWithdrawalsLast24Hours(): Double {
        val appCtx = getApplication<Application>()
        val local24h = SessionManager.getWithdrawalsLast24Hours(appCtx)
        val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        val history24h = _uiState.value.withdrawalHistory
            .filter { it.createdMs >= cutoff && it.status != WithdrawalStatus.REJECTED }
            .sumOf { it.requestedAmount }
        return maxOf(local24h, history24h)
    }

    private fun startMbSellingTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1000L) // tick every 1 second
                val state = _uiState.value
                if (state.isSellingActive) {
                    val rateMbPerSec = 14.0
                    val nextMbSold = state.currentWidgetMbSold + rateMbPerSec
                    val totalMb24h = getMbSoldLast24Hours() + nextMbSold
                    if (totalMb24h >= MAX_24H_MB_SELLING) {
                        val remainingCapacity = (MAX_24H_MB_SELLING - getMbSoldLast24Hours()).coerceAtLeast(0.0)
                        _uiState.update {
                            it.copy(
                                currentWidgetMbSold = remainingCapacity,
                                isSellingActive = false,
                                userNoticeMessage = "Daily limit reached (12,000 MBs / 24 hrs). You can sell more bandwidth tomorrow!"
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(currentWidgetMbSold = nextMbSold)
                        }
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
        val mbSold24h = getMbSoldLast24Hours()
        if (mbSold24h >= MAX_24H_MB_SELLING) {
            _uiState.update {
                it.copy(
                    isSellingActive = false,
                    userNoticeMessage = "Daily limit reached (12,000 MBs / 24 hrs). You can sell more bandwidth tomorrow!"
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

    fun dismissCelebration() {
        _uiState.update { it.copy(isCelebrationActive = false, celebrationMessage = "") }
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
        val newRecord = EarningRecord(mbSold = mbSold, pkrEarned = pkrEarned)

        // 1. Immediately update UI state in memory synchronously
        _uiState.update { state ->
            state.copy(
                isSellingActive = false,
                isCelebrationActive = true,
                celebrationMessage = "+Rs. %.2f Cash Added!".format(pkrEarned),
                userNoticeMessage = customMessage,
                availableBalance = state.availableBalance + pkrEarned,
                todaysEarnings = state.todaysEarnings + pkrEarned,
                totalEarnings = state.totalEarnings + pkrEarned,
                totalMbSold = state.totalMbSold + mbSold,
                earningHistory = listOf(newRecord) + state.earningHistory,
                currentWidgetMbSold = 0.0
            )
        }

        // 2. Persist locally and sync to cloud in background non-blocking
        val appCtx = getApplication<Application>()
        SessionManager.recordMbSold(appCtx, mbSold)
        val updatedState = _uiState.value
        val uid = updatedState.authUserUid

        viewModelScope.launch(Dispatchers.IO) {
            try {
                SessionManager.updateBalances(
                    context = appCtx,
                    balance = updatedState.availableBalance,
                    todays = updatedState.todaysEarnings,
                    total = updatedState.totalEarnings,
                    mbSold = updatedState.totalMbSold,
                    withdrawn = updatedState.totalWithdrawn
                )
                if (uid != null) {
                    firebaseRepository.recordEarningSale(uid, newRecord)
                    firebaseRepository.saveUserData(
                        uid = uid,
                        name = updatedState.userProfile.name,
                        email = updatedState.userProfile.email,
                        photoUrl = updatedState.userProfile.photoUrl,
                        availableBalance = updatedState.availableBalance,
                        todaysEarnings = updatedState.todaysEarnings,
                        totalEarnings = updatedState.totalEarnings,
                        totalMbSold = updatedState.totalMbSold,
                        totalWithdrawn = updatedState.totalWithdrawn
                    )
                }
            } catch (e: Throwable) {
                Log.w("DataCashViewModel", "Background sync after cash out: ${e.message}")
            }
        }

        // 3. Auto dismiss celebration overlay after 2.5 seconds
        viewModelScope.launch {
            delay(2500L)
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
        val trimmedHolder = accountHolder.trim()
        val trimmedNumber = accountNumber.trim()
        val amount = amountText.trim().toDoubleOrNull()

        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(userNoticeMessage = "Please enter a valid numeric withdrawal amount.") }
            return false
        }

        if (trimmedHolder.isBlank()) {
            _uiState.update { it.copy(userNoticeMessage = "Please enter Account Holder Name.") }
            return false
        }

        if (trimmedNumber.isBlank()) {
            _uiState.update { it.copy(userNoticeMessage = "Please enter Mobile Number or Bank IBAN / Account Number.") }
            return false
        }

        if (amount < 200.0) {
            _uiState.update { it.copy(userNoticeMessage = "Minimum withdrawal amount is PKR 200.") }
            return false
        }

        if (amount > MAX_24H_WITHDRAWAL_PKR) {
            _uiState.update {
                it.copy(
                    userNoticeMessage = "Daily withdrawal limit is Rs. 3,500. Remaining wallet balance can be withdrawn after 24 hours."
                )
            }
            return false
        }

        val pastWithdrawals24h = getWithdrawalsLast24Hours()
        if (pastWithdrawals24h + amount > MAX_24H_WITHDRAWAL_PKR) {
            val remaining = (MAX_24H_WITHDRAWAL_PKR - pastWithdrawals24h).coerceAtLeast(0.0)
            _uiState.update {
                it.copy(
                    userNoticeMessage = "Daily withdrawal limit is Rs. 3,500. You have already withdrawn PKR %.2f in the last 24 hours (Remaining daily limit: PKR %.2f).".format(pastWithdrawals24h, remaining)
                )
            }
            return false
        }

        val currentBal = _uiState.value.availableBalance
        if (amount > currentBal) {
            _uiState.update { it.copy(userNoticeMessage = "Insufficient balance! Your current available balance is PKR %.2f.".format(currentBal)) }
            return false
        }

        // All validations pass! Set as pending withdrawal record so the Confirmation Dialog displays
        val newRecord = WithdrawalRecord(
            paymentMethod = paymentMethod,
            accountHolder = trimmedHolder,
            accountNumber = trimmedNumber,
            requestedAmount = amount,
            adminFee = 50.0,
            status = WithdrawalStatus.PENDING_1_HR,
            timestamp = "Just Now"
        )

        _uiState.update { state ->
            state.copy(
                pendingWithdrawalRecord = newRecord,
                userNoticeMessage = null
            )
        }

        return true
    }

    fun cancelPendingWithdrawal() {
        _uiState.update { it.copy(pendingWithdrawalRecord = null) }
    }

    fun confirmPendingWithdrawal() {
        val record = _uiState.value.pendingWithdrawalRecord ?: return
        val amount = record.requestedAmount
        val appCtx = getApplication<Application>()

        // 1. Immediately update and deduct in-memory state
        _uiState.update { state ->
            state.copy(
                availableBalance = (state.availableBalance - amount).coerceAtLeast(0.0),
                totalWithdrawn = state.totalWithdrawn + amount,
                withdrawalHistory = listOf(record) + state.withdrawalHistory,
                pendingWithdrawalRecord = null,
                isCelebrationActive = true,
                celebrationMessage = "Withdrawal Confirmed!\nPKR %.2f to %s".format(amount, record.paymentMethod.title),
                userNoticeMessage = "Withdrawal Request Submitted Successfully!\n\n• Transaction ID: #${record.id}\n• Requested Amount: PKR %.2f\n• Flat Admin Fee: PKR 50.00\n• Net Receiving: PKR %.2f\n• Payout Channel: %s\n• Account: %s (%s)\n\nYour payout request has been registered in the system & Firestore DB. Funds will be transferred to your account within 1 hour.".format(
                    amount,
                    record.netAmount,
                    record.paymentMethod.title,
                    record.accountHolder,
                    record.accountNumber
                )
            )
        }

        // 2. Persist locally to SessionManager
        SessionManager.recordWithdrawalAmount(appCtx, amount)
        val s = _uiState.value
        SessionManager.updateBalances(
            context = appCtx,
            balance = s.availableBalance,
            todays = s.todaysEarnings,
            total = s.totalEarnings,
            mbSold = s.totalMbSold,
            withdrawn = s.totalWithdrawn
        )

        // 3. Submit payout request to Firestore DB asynchronously
        val uid = _uiState.value.authUserUid ?: SessionManager.getUserUid(appCtx) ?: "user-datacash-local"
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firebaseRepository.ensureFirebaseInitialized(appCtx)
                firebaseRepository.recordCashOutRequest(uid, record)
                syncUserDataToFirestore()
            } catch (e: Throwable) {
                Log.e("DataCashViewModel", "Firestore payout request error: ${e.message}")
            }
        }
    }

    fun clearUserNotice() {
        _uiState.update { it.copy(userNoticeMessage = null, pendingWithdrawalRecord = null) }
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
