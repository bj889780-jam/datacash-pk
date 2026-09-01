package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.data.UserProfile

object SessionManager {
    private const val PREFS_NAME = "datacash_pk_session_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_UID = "user_uid"
    private const val KEY_NAME = "user_name"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_PHOTO_URL = "user_photo_url"
    private const val KEY_BALANCE = "available_balance"
    private const val KEY_TODAYS = "todays_earnings"
    private const val KEY_TOTAL = "total_earnings"
    private const val KEY_MB_SOLD = "total_mb_sold"
    private const val KEY_WITHDRAWN = "total_withdrawn"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(
        context: Context,
        uid: String,
        name: String,
        email: String,
        photoUrl: String? = null,
        balance: Double = 0.0,
        todays: Double = 0.0,
        total: Double = 0.0,
        mbSold: Double = 0.0,
        withdrawn: Double = 0.0
    ) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_UID, uid)
            putString(KEY_NAME, name)
            putString(KEY_EMAIL, email)
            putString(KEY_PHOTO_URL, photoUrl ?: "")
            putFloat(KEY_BALANCE, balance.toFloat())
            putFloat(KEY_TODAYS, todays.toFloat())
            putFloat(KEY_TOTAL, total.toFloat())
            putFloat(KEY_MB_SOLD, mbSold.toFloat())
            putFloat(KEY_WITHDRAWN, withdrawn.toFloat())
            apply()
        }
    }

    fun isUserLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserUid(context: Context): String? {
        return getPrefs(context).getString(KEY_UID, null)
    }

    fun getUserProfile(context: Context): UserProfile {
        val prefs = getPrefs(context)
        val name = prefs.getString(KEY_NAME, "DataCash User") ?: "DataCash User"
        val email = prefs.getString(KEY_EMAIL, "user@datacash.pk") ?: "user@datacash.pk"
        val photoUrl = prefs.getString(KEY_PHOTO_URL, null)?.ifEmpty { null }
        return UserProfile(
            name = name,
            email = email,
            photoUrl = photoUrl
        )
    }

    fun getSavedBalances(context: Context): SavedBalances {
        val prefs = getPrefs(context)
        return SavedBalances(
            balance = prefs.getFloat(KEY_BALANCE, 0.0f).toDouble(),
            todays = prefs.getFloat(KEY_TODAYS, 0.0f).toDouble(),
            total = prefs.getFloat(KEY_TOTAL, 0.0f).toDouble(),
            mbSold = prefs.getFloat(KEY_MB_SOLD, 0.0f).toDouble(),
            withdrawn = prefs.getFloat(KEY_WITHDRAWN, 0.0f).toDouble()
        )
    }

    fun updateBalances(
        context: Context,
        balance: Double,
        todays: Double,
        total: Double,
        mbSold: Double,
        withdrawn: Double
    ) {
        getPrefs(context).edit().apply {
            putFloat(KEY_BALANCE, balance.toFloat())
            putFloat(KEY_TODAYS, todays.toFloat())
            putFloat(KEY_TOTAL, total.toFloat())
            putFloat(KEY_MB_SOLD, mbSold.toFloat())
            putFloat(KEY_WITHDRAWN, withdrawn.toFloat())
            apply()
        }
    }

    private const val KEY_MB_SOLD_HISTORY_24H = "mb_sold_history_24h"
    private const val KEY_WITHDRAWALS_HISTORY_24H = "withdrawals_history_24h"

    fun recordMbSold(context: Context, mb: Double) {
        if (mb <= 0) return
        val prefs = getPrefs(context)
        val now = System.currentTimeMillis()
        val currentStr = prefs.getString(KEY_MB_SOLD_HISTORY_24H, "") ?: ""
        val cutoff = now - 24 * 60 * 60 * 1000L
        val validEntries = currentStr.split(";")
            .mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    val ts = parts[0].toLongOrNull() ?: 0L
                    val amount = parts[1].toDoubleOrNull() ?: 0.0
                    if (ts >= cutoff) "$ts:$amount" else null
                } else null
            }
            .toMutableList()
        validEntries.add("$now:$mb")
        prefs.edit().putString(KEY_MB_SOLD_HISTORY_24H, validEntries.joinToString(";")).apply()
    }

    fun getMbSoldLast24Hours(context: Context): Double {
        val prefs = getPrefs(context)
        val now = System.currentTimeMillis()
        val currentStr = prefs.getString(KEY_MB_SOLD_HISTORY_24H, "") ?: ""
        val cutoff = now - 24 * 60 * 60 * 1000L
        var total = 0.0
        val remaining = mutableListOf<String>()
        currentStr.split(";").forEach { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val ts = parts[0].toLongOrNull() ?: 0L
                val amount = parts[1].toDoubleOrNull() ?: 0.0
                if (ts >= cutoff) {
                    total += amount
                    remaining.add(entry)
                }
            }
        }
        if (remaining.size != currentStr.split(";").size) {
            prefs.edit().putString(KEY_MB_SOLD_HISTORY_24H, remaining.joinToString(";")).apply()
        }
        return total
    }

    fun recordWithdrawalAmount(context: Context, pkr: Double) {
        if (pkr <= 0) return
        val prefs = getPrefs(context)
        val now = System.currentTimeMillis()
        val currentStr = prefs.getString(KEY_WITHDRAWALS_HISTORY_24H, "") ?: ""
        val cutoff = now - 24 * 60 * 60 * 1000L
        val validEntries = currentStr.split(";")
            .mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    val ts = parts[0].toLongOrNull() ?: 0L
                    val amount = parts[1].toDoubleOrNull() ?: 0.0
                    if (ts >= cutoff) "$ts:$amount" else null
                } else null
            }
            .toMutableList()
        validEntries.add("$now:$pkr")
        prefs.edit().putString(KEY_WITHDRAWALS_HISTORY_24H, validEntries.joinToString(";")).apply()
    }

    fun getWithdrawalsLast24Hours(context: Context): Double {
        val prefs = getPrefs(context)
        val now = System.currentTimeMillis()
        val currentStr = prefs.getString(KEY_WITHDRAWALS_HISTORY_24H, "") ?: ""
        val cutoff = now - 24 * 60 * 60 * 1000L
        var total = 0.0
        val remaining = mutableListOf<String>()
        currentStr.split(";").forEach { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val ts = parts[0].toLongOrNull() ?: 0L
                val amount = parts[1].toDoubleOrNull() ?: 0.0
                if (ts >= cutoff) {
                    total += amount
                    remaining.add(entry)
                }
            }
        }
        if (remaining.size != currentStr.split(";").size) {
            prefs.edit().putString(KEY_WITHDRAWALS_HISTORY_24H, remaining.joinToString(";")).apply()
        }
        return total
    }

    fun clearSession(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    data class SavedBalances(
        val balance: Double,
        val todays: Double,
        val total: Double,
        val mbSold: Double,
        val withdrawn: Double
    )
}
