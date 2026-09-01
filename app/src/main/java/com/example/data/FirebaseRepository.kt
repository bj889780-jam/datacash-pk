package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class FirebaseRepository(private val context: Context? = null) {

    init {
        ensureFirebaseInitialized()
    }

    fun ensureFirebaseInitialized(ctx: Context? = null): Boolean {
        return try {
            val targetContext = ctx ?: context
            val apps = if (targetContext != null) FirebaseApp.getApps(targetContext) else emptyList()
            if (apps.isNotEmpty()) {
                return true
            }
            if (targetContext != null) {
                try {
                    FirebaseApp.initializeApp(targetContext)
                } catch (e: Throwable) {
                    val options = com.google.firebase.FirebaseOptions.Builder()
                        .setApplicationId("1:123456789012:android:1234567890abcdef")
                        .setApiKey("AIzaSyDummyApiKeyDataCashPKApp")
                        .setProjectId("datacash-pk-app")
                        .build()
                    FirebaseApp.initializeApp(targetContext, options)
                }
                return FirebaseApp.getApps(targetContext).isNotEmpty()
            }
            try {
                FirebaseApp.getInstance()
                true
            } catch (e: Throwable) {
                false
            }
        } catch (e: Throwable) {
            Log.w("FirebaseRepository", "FirebaseApp initialization attempt: ${e.message}")
            false
        }
    }

    private val isFirebaseAvailable: Boolean
        get() = try {
            ensureFirebaseInitialized()
        } catch (e: Throwable) {
            false
        }

    private val auth: FirebaseAuth?
        get() = try {
            ensureFirebaseInitialized()
            if (isFirebaseAvailable) FirebaseAuth.getInstance() else null
        } catch (e: Throwable) {
            Log.w("FirebaseRepository", "FirebaseAuth.getInstance failed: ${e.message}")
            null
        }

    private val db: FirebaseFirestore?
        get() = try {
            ensureFirebaseInitialized()
            if (isFirebaseAvailable) FirebaseFirestore.getInstance() else null
        } catch (e: Throwable) {
            Log.w("FirebaseRepository", "FirebaseFirestore.getInstance failed: ${e.message}")
            null
        }

    fun getCurrentUser(): FirebaseUser? {
        return try {
            auth?.currentUser
        } catch (e: Throwable) {
            null
        }
    }

    fun addAuthStateListener(listener: (FirebaseUser?) -> Unit) {
        try {
            auth?.addAuthStateListener { firebaseAuth ->
                listener(firebaseAuth.currentUser)
            }
        } catch (e: Throwable) {
            Log.w("FirebaseRepository", "addAuthStateListener failed: ${e.message}")
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, name: String, ctx: Context? = null): Result<FirebaseUser> {
        ensureFirebaseInitialized(ctx)
        val authInstance = auth
        if (authInstance == null) {
            return Result.failure(
                Exception("Registration service unavailable. Please check connection.")
            )
        }
        return try {
            val authResult = withTimeoutOrNull(8000L) {
                authInstance.createUserWithEmailAndPassword(email.trim(), pass).await()
            }
            val user = authResult?.user ?: authInstance.currentUser
            if (user != null) {
                // Initialize user document in Firestore asynchronously with timeout
                val initialData = hashMapOf(
                    "uid" to user.uid,
                    "name" to name.ifBlank { "DataCash User" },
                    "email" to (user.email ?: email),
                    "availableBalance" to 1250.0,
                    "todaysEarnings" to 450.0,
                    "totalEarnings" to 5800.0,
                    "totalMbSold" to 18450.0,
                    "totalWithdrawn" to 4200.0,
                    "createdAt" to System.currentTimeMillis()
                )
                try {
                    withTimeoutOrNull(2000L) {
                        db?.collection("users")?.document(user.uid)?.set(initialData)?.await()
                    }
                } catch (e: Throwable) {
                    Log.w("FirebaseRepository", "Firestore initial doc creation error: ${e.message}")
                }
                Result.success(user)
            } else {
                Result.failure(Exception("Could not create account. Please try again."))
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "SignUp error", e)
            val msg = e.localizedMessage ?: ""
            val friendlyMsg = when {
                msg.contains("already in use", ignoreCase = true) ->
                    "An account with this email already exists. Please switch to Sign In."
                msg.contains("badly formatted", ignoreCase = true) || msg.contains("invalid email", ignoreCase = true) ->
                    "Please enter a valid email address."
                msg.contains("at least 6 characters", ignoreCase = true) || msg.contains("weak password", ignoreCase = true) ->
                    "Password must be at least 6 characters long."
                else -> msg.ifBlank { "Registration failed. Please try again." }
            }
            Result.failure(Exception(friendlyMsg))
        }
    }

    suspend fun signInWithEmail(email: String, pass: String, ctx: Context? = null): Result<FirebaseUser> {
        ensureFirebaseInitialized(ctx)
        val authInstance = auth
        if (authInstance == null) {
            return Result.failure(
                Exception("Authentication service unavailable. Please check connection.")
            )
        }
        return try {
            val authResult = withTimeoutOrNull(8000L) {
                authInstance.signInWithEmailAndPassword(email.trim(), pass).await()
            }
            val user = authResult?.user ?: authInstance.currentUser
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Sign in timed out. Please check your credentials and network."))
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "SignIn error", e)
            val msg = e.localizedMessage ?: ""
            val friendlyMsg = when {
                msg.contains("no user record", ignoreCase = true) || msg.contains("invalid credential", ignoreCase = true) || msg.contains("wrong password", ignoreCase = true) ->
                    "Invalid email or password. Please check your credentials."
                msg.contains("badly formatted", ignoreCase = true) ->
                    "Please enter a valid email address."
                else -> msg.ifBlank { "Sign in failed. Please try again." }
            }
            Result.failure(Exception(friendlyMsg))
        }
    }

    suspend fun sendPasswordResetEmail(email: String, ctx: Context? = null): Result<Unit> {
        ensureFirebaseInitialized(ctx)
        val authInstance = auth
        if (authInstance == null) {
            return Result.failure(Exception("Authentication service unavailable. Please check connection."))
        }
        return try {
            val taskResult = withTimeoutOrNull(8000L) {
                authInstance.sendPasswordResetEmail(email.trim()).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Password reset error", e)
            val msg = e.localizedMessage ?: ""
            val friendlyMsg = when {
                msg.contains("no user record", ignoreCase = true) || msg.contains("user-not-found", ignoreCase = true) ->
                    "No registered account found with this email address."
                msg.contains("badly formatted", ignoreCase = true) || msg.contains("invalid email", ignoreCase = true) ->
                    "Please enter a valid email address."
                else -> msg.ifBlank { "Could not send password reset email. Please try again." }
            }
            Result.failure(Exception(friendlyMsg))
        }
    }

    suspend fun signInWithGoogleCredential(idToken: String, ctx: Context? = null): Result<FirebaseUser> {
        ensureFirebaseInitialized(ctx)
        val authInstance = auth
        if (authInstance == null) {
            return Result.failure(Exception("Authentication service unavailable. Please check connection."))
        }
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = withTimeoutOrNull(10000L) {
                authInstance.signInWithCredential(credential).await()
            }
            val user = authResult?.user ?: authInstance.currentUser
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Google Sign-In failed to return user profile."))
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Google signInWithCredential error", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            ensureFirebaseInitialized()
            auth?.signOut()
        } catch (e: Throwable) {
            Log.e("FirebaseRepository", "SignOut error", e)
        }
    }

    suspend fun fetchUserData(uid: String): Map<String, Any>? {
        val dbInstance = db ?: return null
        return try {
            withTimeoutOrNull(1500L) {
                val snapshot = dbInstance.collection("users").document(uid).get().await()
                snapshot.data
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "FetchUserData error", e)
            null
        }
    }

    suspend fun saveUserData(
        uid: String,
        name: String,
        email: String,
        photoUrl: String? = null,
        availableBalance: Double,
        todaysEarnings: Double,
        totalEarnings: Double,
        totalMbSold: Double,
        totalWithdrawn: Double
    ) {
        val dbInstance = db ?: return
        try {
            withTimeoutOrNull(1500L) {
                val data = hashMapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "photoUrl" to (photoUrl ?: ""),
                    "availableBalance" to availableBalance,
                    "todaysEarnings" to todaysEarnings,
                    "totalEarnings" to totalEarnings,
                    "totalMbSold" to totalMbSold,
                    "totalWithdrawn" to totalWithdrawn,
                    "updatedAt" to System.currentTimeMillis()
                )
                dbInstance.collection("users").document(uid).set(data).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "SaveUserData error", e)
        }
    }

    suspend fun recordCashOutRequest(
        userId: String,
        record: WithdrawalRecord
    ): Boolean {
        val dbInstance = db ?: return false
        return try {
            val data = hashMapOf(
                "id" to record.id,
                "userId" to userId,
                "paymentMethod" to record.paymentMethod.name,
                "accountHolder" to record.accountHolder,
                "accountNumber" to record.accountNumber,
                "requestedAmount" to record.requestedAmount,
                "adminFee" to record.adminFee,
                "netAmount" to record.netAmount,
                "status" to record.status.name,
                "timestamp" to record.timestamp,
                "createdMs" to System.currentTimeMillis()
            )
            dbInstance.collection("withdrawals").document(record.id).set(data).await()
            dbInstance.collection("users").document(userId).collection("withdrawals").document(record.id).set(data).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "RecordCashOut error", e)
            false
        }
    }

    suspend fun recordEarningSale(
        userId: String,
        record: EarningRecord
    ): Boolean {
        val dbInstance = db ?: return false
        return try {
            val data = hashMapOf(
                "id" to record.id,
                "userId" to userId,
                "mbSold" to record.mbSold,
                "pkrEarned" to record.pkrEarned,
                "timestamp" to record.timestamp,
                "createdMs" to System.currentTimeMillis()
            )
            dbInstance.collection("users").document(userId).collection("earnings").document(record.id).set(data).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "RecordEarning error", e)
            false
        }
    }

    suspend fun updateWithdrawalStatus(recordId: String, newStatus: WithdrawalStatus): Boolean {
        val dbInstance = db ?: return false
        return try {
            dbInstance.collection("withdrawals").document(recordId).update("status", newStatus.name).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "UpdateWithdrawalStatus error", e)
            false
        }
    }

    suspend fun getMbSoldLast24Hours(userId: String): Double {
        val dbInstance = db ?: return 0.0
        return try {
            val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
            val snapshot = dbInstance.collection("users")
                .document(userId)
                .collection("earnings")
                .whereGreaterThanOrEqualTo("createdMs", cutoff)
                .get()
                .await()
            var total = 0.0
            for (doc in snapshot.documents) {
                val mb = doc.getDouble("mbSold") ?: 0.0
                total += mb
            }
            total
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "getMbSoldLast24Hours error", e)
            0.0
        }
    }

    suspend fun getWithdrawalsLast24Hours(userId: String): Double {
        val dbInstance = db ?: return 0.0
        return try {
            val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
            val snapshot = dbInstance.collection("users")
                .document(userId)
                .collection("withdrawals")
                .whereGreaterThanOrEqualTo("createdMs", cutoff)
                .get()
                .await()
            var total = 0.0
            for (doc in snapshot.documents) {
                val statusStr = doc.getString("status") ?: ""
                if (statusStr != WithdrawalStatus.REJECTED.name) {
                    val amount = doc.getDouble("requestedAmount") ?: 0.0
                    total += amount
                }
            }
            total
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "getWithdrawalsLast24Hours error", e)
            0.0
        }
    }
}
