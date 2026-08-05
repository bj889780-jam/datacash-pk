package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
            if (targetContext != null) {
                if (FirebaseApp.getApps(targetContext).isEmpty()) {
                    FirebaseApp.initializeApp(targetContext)
                }
                !FirebaseApp.getApps(targetContext).isEmpty()
            } else {
                try {
                    FirebaseApp.getInstance()
                    true
                } catch (e: Throwable) {
                    false
                }
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
}
