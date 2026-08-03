package com.example.data

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val isFirebaseAvailable: Boolean
        get() = try {
            FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()
        } catch (e: Exception) {
            false
        }

    private val auth: FirebaseAuth?
        get() = try {
            if (isFirebaseAvailable) FirebaseAuth.getInstance() else null
        } catch (e: Exception) {
            null
        }

    private val db: FirebaseFirestore?
        get() = try {
            if (isFirebaseAvailable) FirebaseFirestore.getInstance() else null
        } catch (e: Exception) {
            null
        }

    fun getCurrentUser(): FirebaseUser? {
        return auth?.currentUser
    }

    suspend fun signUpWithEmail(email: String, pass: String, name: String): Result<FirebaseUser> {
        val authInstance = auth ?: return Result.failure(
            IllegalStateException("Firebase Auth is not configured. Please ensure google-services.json is present in the project.")
        )
        return try {
            val authResult = authInstance.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user
            if (user != null) {
                // Initialize user document in Firestore
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
                db?.collection("users")?.document(user.uid)?.set(initialData)?.await()
            }
            if (user != null) Result.success(user) else Result.failure(Exception("Failed to get user after creation."))
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "SignUp error", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> {
        val authInstance = auth ?: return Result.failure(
            IllegalStateException("Firebase Auth is not configured. Please ensure google-services.json is present in the project.")
        )
        return try {
            val authResult = authInstance.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user
            if (user != null) Result.success(user) else Result.failure(Exception("User not found."))
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "SignIn error", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth?.signOut()
    }

    suspend fun fetchUserData(uid: String): Map<String, Any>? {
        val dbInstance = db ?: return null
        return try {
            val snapshot = dbInstance.collection("users").document(uid).get().await()
            snapshot.data
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
