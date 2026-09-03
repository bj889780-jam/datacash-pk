package com.example

import com.example.data.PaymentMethod
import com.example.data.WithdrawalRecord
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun zeroAmountWithdrawalRecord_hasZeroFeeAndZeroNet() {
    val zeroRecord = WithdrawalRecord(
      paymentMethod = PaymentMethod.EASYPAISA,
      accountHolder = "Ali Khan",
      accountNumber = "03001234567",
      requestedAmount = 0.0
    )
    assertEquals(0.0, zeroRecord.adminFee, 0.001)
    assertEquals(0.0, zeroRecord.netAmount, 0.001)
  }

  @Test
  fun positiveAmountWithdrawalRecord_hasFlatFeeAndCorrectNet() {
    val record = WithdrawalRecord(
      paymentMethod = PaymentMethod.JAZZCASH,
      accountHolder = "Fatima Noor",
      accountNumber = "03219876543",
      requestedAmount = 1000.0
    )
    assertEquals(50.0, record.adminFee, 0.001)
    assertEquals(950.0, record.netAmount, 0.001)
  }

  @Test
  fun validatePakistaniPhoneNumberFormat() {
    val validPhone = "03001234567"
    val digitsOnly = validPhone.filter { it.isDigit() }
    assertTrue(digitsOnly.length == 11 && digitsOnly.startsWith("03"))

    val invalidShort = "030012345"
    assertFalse(invalidShort.filter { it.isDigit() }.length == 11)

    val invalidPrefix = "04001234567"
    assertFalse(invalidPrefix.startsWith("03"))
  }

  @Test
  fun validateAccountHolderMinLength() {
    val validName = "Ali"
    assertTrue(validName.trim().length in 3..15)

    val invalidName = "Al"
    assertFalse(invalidName.trim().length in 3..15)
  }

  @Test
  fun validateAccountHolderStrictEnglishAndLength() {
    val validNames = listOf("Bilal Iqbal", "Ali Khan", "John Doe", "Muhammad Ali")
    for (name in validNames) {
      val trimmed = name.trim()
      val isValid = trimmed.length in 3..15 && trimmed.all { (it in 'a'..'z') || (it in 'A'..'Z') || it == ' ' }
      assertTrue("Expected $name to be valid", isValid)
    }

    val invalidNames = listOf("Ali123", "Bilal@Jamali", "Al", "ThisNameIsFarTooLongForValidation", "علی خان")
    for (name in invalidNames) {
      val trimmed = name.trim()
      val isValid = trimmed.length in 3..15 && trimmed.all { (it in 'a'..'z') || (it in 'A'..'Z') || it == ' ' }
      assertFalse("Expected $name to be invalid", isValid)
    }
  }

  @Test
  fun validateUserProfileOwnerCheck() {
    val ownerProfile = com.example.data.UserProfile(email = "bj889780@gmail.com")
    assertTrue(ownerProfile.isOwner)

    val ownerProfileUpper = com.example.data.UserProfile(email = "BJ889780@GMAIL.COM")
    assertTrue(ownerProfileUpper.isOwner)

    val regularProfile = com.example.data.UserProfile(email = "regularuser@datacash.pk")
    assertFalse(regularProfile.isOwner)
  }
}
