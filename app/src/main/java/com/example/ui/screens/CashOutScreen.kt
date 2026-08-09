package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PaymentMethod
import com.example.ui.components.safePainterResource
import com.example.viewmodel.DataCashUiState
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ActiveYellow
import com.example.ui.theme.BentoBlue
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoEmeraldLight
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.StopRed

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CashOutScreen(
    uiState: DataCashUiState,
    onWithdrawSubmit: (PaymentMethod, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.EASYPAISA) }
    var selectedBank by remember { mutableStateOf("Meezan Bank") }
    var bankDropdownExpanded by remember { mutableStateOf(false) }
    var accountHolder by remember(uiState.userProfile.name) {
        mutableStateOf(uiState.userProfile.name.ifBlank { "DataCash User" })
    }
    var accountNumber by remember { mutableStateOf("03001234567") }
    var amountText by remember { mutableStateOf("500") }

    val pakistaniBanks = listOf(
        "Meezan Bank",
        "Habib Bank Limited (HBL)",
        "United Bank Limited (UBL)",
        "Allied Bank Limited (ABL)",
        "MCB Bank",
        "Bank Alfalah",
        "Faysal Bank",
        "Askari Bank",
        "Bank Al Habib",
        "Standard Chartered Pakistan",
        "JS Bank",
        "Soneri Bank",
        "National Bank of Pakistan (NBP)"
    )

    val amountDouble = amountText.toDoubleOrNull() ?: 0.0
    val adminFee = 50.0
    val netReceiving = (amountDouble - adminFee).coerceAtLeast(0.0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Available Balance Bento Card (Screen starts directly here)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(22.dp)
                    ),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = BentoEmerald,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AVAILABLE BALANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Text(
                        text = "PKR %.2f".format(uiState.availableBalance),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = BentoEmerald
                    )
                }
            }
        }

        // 2. Payment Methods Selection (EasyPaisa, JazzCash, Bank Transfer)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SELECT PAYMENT METHOD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PaymentMethod.values().forEach { method ->
                        val isSelected = selectedMethod == method
                        val brandColor = Color(method.brandColorHex)

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedMethod = method }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) brandColor else MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) brandColor.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(width = 56.dp, height = 40.dp)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) brandColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    shadowElevation = 2.dp
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when (method) {
                                            PaymentMethod.EASYPAISA -> {
                                                Image(
                                                    painter = safePainterResource(
                                                        id = R.drawable.ic_easypaisa_vector
                                                    ),
                                                    contentDescription = "EasyPaisa Official Logo",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                            PaymentMethod.JAZZCASH -> {
                                                Image(
                                                    painter = safePainterResource(
                                                        id = R.drawable.ic_jazzcash_vector
                                                    ),
                                                    contentDescription = "JazzCash Official Logo",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }
                                            PaymentMethod.BANK_TRANSFER -> {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(brandColor),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AccountBalance,
                                                        contentDescription = "Bank Transfer Logo",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = method.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Input Fields Bento Card: Account Holder Name, Mobile Number, Amount
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "WITHDRAWAL DETAILS (${selectedMethod.title.uppercase()})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )

                    if (selectedMethod == PaymentMethod.BANK_TRANSFER) {
                        ExposedDropdownMenuBox(
                            expanded = bankDropdownExpanded,
                            onExpandedChange = { bankDropdownExpanded = !bankDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedBank,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Bank Name") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = "Bank Icon"
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankDropdownExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )

                            ExposedDropdownMenu(
                                expanded = bankDropdownExpanded,
                                onDismissRequest = { bankDropdownExpanded = false }
                            ) {
                                pakistaniBanks.forEach { bank ->
                                    DropdownMenuItem(
                                        text = { Text(text = bank, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            selectedBank = bank
                                            bankDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = accountHolder,
                        onValueChange = { accountHolder = it },
                        label = { Text("Account Holder Name") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text(if (selectedMethod == PaymentMethod.BANK_TRANSFER) "IBAN / Account Number" else "Mobile Number") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    val isOverLimit = (amountText.toDoubleOrNull() ?: 0.0) > 15000.0

                    Column {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Withdrawal Amount (PKR)") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Receipt, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = isOverLimit,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                        if (isOverLimit) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Notice: The maximum withdrawal limit is PKR 15,000 per request.",
                                color = StopRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Max limit: PKR 15,000 per request (Unlimited MB sales allowed)",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Quick Presets Bento Pills
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(300, 500, 1000, 2000, 5000, 15000).forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (amountText == preset.toString()) ActiveYellow else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { amountText = preset.toString() }
                            ) {
                                Text(
                                    text = "PKR $preset",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (amountText == preset.toString()) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { 
                                val maxBal = uiState.availableBalance.toInt().coerceAtMost(15000)
                                amountText = if (maxBal > 0) maxBal.toString() else "300"
                            }
                        ) {
                            Text(
                                text = "Max (15k)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BentoEmerald,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Admin Tax / Fee Logic & Breakdown Bento Box
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(22.dp)
                    ),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "FEE & TAX BREAKDOWN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Requested Amount", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "PKR %.2f".format(amountDouble), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Flat Admin Fee", fontSize = 13.sp, color = StopRed)
                        Text(text = "- PKR %.2f".format(adminFee), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StopRed)
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Net Receiving Amount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BentoEmerald)
                        Text(text = "PKR %.2f".format(netReceiving), fontSize = 17.sp, fontWeight = FontWeight.Black, color = BentoEmerald)
                    }
                }
            }
        }

        // 5. Policy & Limits Bento Notice
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(22.dp)
                    ),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = AccentGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WITHDRAWAL POLICY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Minimum Withdrawal: PKR 300\n• Maximum Daily Limit: PKR 15,000\n• Processing Time: 1 Hour (Direct to EasyPaisa / JazzCash / Bank)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 6. Active Yellow "Withdraw Now" Bento Button
        item {
            Button(
                onClick = {
                    val finalAccountNo = if (selectedMethod == PaymentMethod.BANK_TRANSFER) {
                        "$selectedBank - $accountNumber"
                    } else {
                        accountNumber
                    }
                    onWithdrawSubmit(selectedMethod, accountHolder, finalAccountNo, amountText)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActiveYellow,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WITHDRAW NOW (PKR %.2f)".format(amountDouble),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}
