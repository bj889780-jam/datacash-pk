package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    onWithdrawSubmit: (PaymentMethod, String, String, String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.EASYPAISA) }
    var selectedBank by remember { mutableStateOf("Meezan Bank") }
    var bankDropdownExpanded by remember { mutableStateOf(false) }
    var accountHolder by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }

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
    val adminFee = if (amountDouble > 0.0) 50.0 else 0.0
    val netReceiving = if (amountDouble > 0.0) (amountDouble - adminFee).coerceAtLeast(0.0) else 0.0

    val isMobileMethod = selectedMethod == PaymentMethod.EASYPAISA || selectedMethod == PaymentMethod.JAZZCASH

    // Strict Account Holder Name validation (3-15 English letters and spaces only)
    val trimmedHolder = accountHolder.trim()
    val isHolderLengthValid = trimmedHolder.length in 3..15
    val isHolderCharsValid = trimmedHolder.isNotEmpty() && trimmedHolder.all { (it in 'a'..'z') || (it in 'A'..'Z') || it == ' ' }
    val isHolderValid = isHolderLengthValid && isHolderCharsValid
    val isHolderError = accountHolder.isNotEmpty() && !isHolderValid

    // Strict Mobile Number validation (11-digit Pakistani format 03XXXXXXXXX)
    val cleanDigits = accountNumber.filter { it.isDigit() }
    val isNumberValid = if (isMobileMethod) {
        cleanDigits.length == 11 && cleanDigits.startsWith("03") && accountNumber.all { it.isDigit() }
    } else {
        accountNumber.trim().length >= 8
    }
    val isNumberError = accountNumber.isNotEmpty() && !isNumberValid

    val isAmountValid = amountDouble in 200.0..3500.0 && amountDouble <= uiState.availableBalance
    val isFormValid = isHolderValid && isNumberValid && isAmountValid

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

        // 2. Payment Methods Selection (Easypaisa, JazzCash, Bank Transfer)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                    color = if (isSelected) brandColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(18.dp)
                                ),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) brandColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp, horizontal = 6.dp)
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(end = 2.dp)
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(brandColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // High contrast, rounded white container so Easypaisa & JazzCash logos are crystal clear in BOTH Light and Dark modes
                                    Surface(
                                        modifier = Modifier.size(54.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.2.dp, if (isSelected) brandColor.copy(alpha = 0.6f) else Color(0xFFE2E8F0)),
                                        shadowElevation = if (isSelected) 3.dp else 1.5.dp
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(5.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            when (method) {
                                                PaymentMethod.EASYPAISA -> {
                                                    Image(
                                                        painter = safePainterResource(
                                                            id = R.drawable.easypaisa
                                                        ),
                                                        contentDescription = "Easypaisa",
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(2.dp),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                }
                                                PaymentMethod.JAZZCASH -> {
                                                    Image(
                                                        painter = safePainterResource(
                                                            id = R.drawable.jazzcash
                                                        ),
                                                        contentDescription = "JazzCash",
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(2.dp),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                }
                                                PaymentMethod.BANK_TRANSFER -> {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .clip(CircleShape)
                                                            .background(brandColor.copy(alpha = 0.12f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.AccountBalance,
                                                            contentDescription = "Bank Transfer",
                                                            tint = brandColor,
                                                            modifier = Modifier.size(26.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Name directly below each option/logo
                                    Text(
                                        text = method.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) brandColor else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "WITHDRAWAL DETAILS (${selectedMethod.title.uppercase()})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )

                        // Small provider indicator in card header
                        Surface(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                when (selectedMethod) {
                                    PaymentMethod.EASYPAISA -> {
                                        Image(
                                            painter = safePainterResource(id = R.drawable.easypaisa),
                                            contentDescription = "EasyPaisa",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    PaymentMethod.JAZZCASH -> {
                                        Image(
                                            painter = safePainterResource(id = R.drawable.jazzcash),
                                            contentDescription = "JazzCash",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    PaymentMethod.BANK_TRANSFER -> {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalance,
                                            contentDescription = "Bank",
                                            tint = Color(selectedMethod.brandColorHex),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

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
                        onValueChange = { input ->
                            // Instantly block or strip any digits (0-9), special symbols, or non-English characters, max 15 chars
                            val filtered = input.filter { (it in 'a'..'z') || (it in 'A'..'Z') || it == ' ' }.take(15)
                            accountHolder = filtered
                        },
                        label = { Text("Account Holder Name") },
                        placeholder = { Text("Enter account title (3-15 letters)") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                        isError = isHolderError,
                        supportingText = {
                            if (isHolderError) {
                                Text(
                                    text = "Name must be 3-15 English letters only.",
                                    color = StopRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else if (isHolderValid) {
                                Text(
                                    text = "Valid title: $trimmedHolder (${trimmedHolder.length}/15)",
                                    color = BentoEmerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(
                                    text = "Name must be 3-15 English letters only.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("account_holder_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { input ->
                            if (isMobileMethod) {
                                // Strictly allow only numeric digits (0-9) and cap at 11 digits
                                val digitsOnly = input.filter { it.isDigit() }.take(11)
                                accountNumber = digitsOnly
                            } else {
                                accountNumber = input
                            }
                        },
                        label = { Text(if (selectedMethod == PaymentMethod.BANK_TRANSFER) "IBAN / Account Number" else "Mobile Number") },
                        placeholder = { Text(if (selectedMethod == PaymentMethod.BANK_TRANSFER) "PK00XXXX0000000000000000" else "03XXXXXXXXX") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = if (isMobileMethod) KeyboardType.Number else KeyboardType.Text),
                        isError = isNumberError,
                        supportingText = {
                            if (isNumberError) {
                                Text(
                                    text = if (isMobileMethod) "Enter valid 11-digit Pakistani phone number (03XXXXXXXXX)" else "Must contain at least 8 characters",
                                    color = StopRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else if (isMobileMethod && isNumberValid) {
                                Text(
                                    text = "Valid Pakistani number (${cleanDigits.length}/11)",
                                    color = BentoEmerald,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                if (isMobileMethod) {
                                    Text(
                                        text = "Format: 03XXXXXXXXX (11 digits)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                } else {
                                    Text(
                                        text = "Enter account number or IBAN (min 8 chars)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("account_number_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    val isOverLimit = (amountText.toDoubleOrNull() ?: 0.0) > 3500.0

                    Column {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Withdrawal Amount (PKR)") },
                            placeholder = { Text("Enter amount in PKR") },
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
                                text = "Notice: Daily withdrawal limit is Rs. 3,500. Remaining wallet balance can be withdrawn after 24 hours.",
                                color = StopRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Max limit: PKR 3,500 per 24 hours",
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
                        listOf(200, 500, 1000, 1500, 2500, 3500).forEach { preset ->
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
                                val maxBal = uiState.availableBalance.toInt().coerceAtMost(3500)
                                amountText = if (maxBal > 0) maxBal.toString() else "200"
                            }
                        ) {
                            Text(
                                text = "Max (3.5k)",
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
                        Text(text = "Flat Admin Fee", fontSize = 13.sp, color = if (adminFee > 0) StopRed else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (adminFee > 0) "- PKR %.2f".format(adminFee) else "PKR 0.00",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (adminFee > 0) StopRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                        text = "• Minimum Withdrawal: PKR 200\n• Maximum Daily Limit: PKR 3,500 per 24 hours\n• Processing Time: 1 Hour (Direct to EasyPaisa / JazzCash / Bank)\n• 24-Hour Selling Cap: 12,000 MBs (Max Rs. 3,600 / 24 hrs)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 6. Active Yellow "Withdraw Now" Bento Button (Strictly gated by Form Validation)
        item {
            Button(
                onClick = {
                    if (!isFormValid) return@Button
                    val finalAccountNo = if (selectedMethod == PaymentMethod.BANK_TRANSFER) {
                        "$selectedBank - $accountNumber"
                    } else {
                        accountNumber
                    }
                    val isSuccess = onWithdrawSubmit(selectedMethod, accountHolder, finalAccountNo, amountText)
                    if (isSuccess) {
                        amountText = ""
                    }
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActiveYellow,
                    contentColor = Color.Black,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("withdraw_now_btn")
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFormValid) "WITHDRAW NOW (PKR %.2f)".format(amountDouble) else "WITHDRAW NOW",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}
