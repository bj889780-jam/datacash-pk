package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.viewmodel.DataCashViewModel
import com.example.utils.SessionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DataCash PK", appName)
  }

  @Test
  fun `verify 24h MB selling and withdrawal limit constants`() {
    assertEquals(12000.0, DataCashViewModel.MAX_24H_MB_SELLING, 0.001)
    assertEquals(3500.0, DataCashViewModel.MAX_24H_WITHDRAWAL_PKR, 0.001)
    assertEquals(0.3, DataCashViewModel.EARNING_RATE_PER_MB, 0.001)
    // 12,000 MBs = 3,600 PKR
    val totalEarningsForDailyMax = DataCashViewModel.MAX_24H_MB_SELLING * DataCashViewModel.EARNING_RATE_PER_MB
    assertEquals(3600.0, totalEarningsForDailyMax, 0.001)
  }

  @Test
  fun `verify session manager 24h rolling recording`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    SessionManager.clearSession(context)
    SessionManager.recordMbSold(context, 1000.0)
    SessionManager.recordMbSold(context, 2000.0)
    val mbSold = SessionManager.getMbSoldLast24Hours(context)
    assertEquals(3000.0, mbSold, 0.001)

    SessionManager.recordWithdrawalAmount(context, 500.0)
    SessionManager.recordWithdrawalAmount(context, 1000.0)
    val withdrawn = SessionManager.getWithdrawalsLast24Hours(context)
    assertEquals(1500.0, withdrawn, 0.001)
  }
}

