package com.example.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserProfile(
    val name: String = "DataCash User",
    val email: String = "user@datacash.pk",
    val photoUrl: String? = null,
    val ownerTag: String = "Owner: Bilal Iqbal Jamali",
    val isVerified: Boolean = true
) {
    val avatarInitial: String
        get() = name.trim().firstOrNull()?.uppercase() ?: "D"
}

enum class NavigationTab(val title: String, val route: String) {
    HOME("Home", "home"),
    CASH_OUT("Cash Out", "cash_out"),
    MINE("Mine", "mine")
}

enum class PaymentMethod(val id: String, val title: String, val brandColorHex: Long, val badgeText: String) {
    EASYPAISA("easypaisa", "Easypaisa", 0xFF00A651, "Instant 24/7"),
    JAZZCASH("jazzcash", "JazzCash", 0xFFD32F2F, "Fast Transfer"),
    BANK_TRANSFER("bank_transfer", "Bank Transfer", 0xFF1976D2, "All PK Banks")
}

data class EarningRecord(
    val id: String = System.currentTimeMillis().toString(),
    val mbSold: Double,
    val pkrEarned: Double,
    val timestamp: String = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date()),
    val createdMs: Long = System.currentTimeMillis()
)

enum class WithdrawalStatus {
    PENDING_1_HR,
    APPROVED,
    COMPLETED,
    REJECTED
}

data class WithdrawalRecord(
    val id: String = "TXN-${System.currentTimeMillis().toString().takeLast(5)}${(100..999).random()}",
    val paymentMethod: PaymentMethod,
    val accountHolder: String,
    val accountNumber: String,
    val requestedAmount: Double,
    val adminFee: Double = 50.0,
    val netAmount: Double = (requestedAmount - adminFee).coerceAtLeast(0.0),
    val status: WithdrawalStatus = WithdrawalStatus.PENDING_1_HR,
    val timestamp: String = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date()),
    val userName: String = accountHolder,
    val bankName: String? = null,
    val createdMs: Long = System.currentTimeMillis()
) {
    val displayBankName: String?
        get() {
            if (!bankName.isNullOrBlank()) return bankName
            if (accountNumber.contains(" - ")) return accountNumber.substringBefore(" - ")
            if (paymentMethod == PaymentMethod.BANK_TRANSFER) return "Commercial Bank"
            return null
        }

    val displayAccountNumber: String
        get() {
            if (accountNumber.contains(" - ")) return accountNumber.substringAfter(" - ")
            return accountNumber
        }
}
