package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.components.safePainterResource
import com.example.ui.theme.BentoBlue
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoEmeraldLight
import com.example.ui.theme.StopRed
import com.google.firebase.FirebaseApp

enum class LoginScreenState {
    LANDING_MINIMAL,
    PASSWORD_SIGN_IN,
    SIGN_UP,
    FORGOT_PASSWORD
}

private enum class PendingAuthAction {
    GOOGLE,
    PASSWORD,
    SIGN_UP
}

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String? = null,
    onGoogleSignIn: () -> Unit,
    onEmailSignIn: (email: String, pass: String) -> Unit,
    onEmailSignUp: (email: String, pass: String, name: String) -> Unit,
    onResetPassword: (email: String) -> Unit = {},
    onClearMessages: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var screenState by remember { mutableStateOf(LoginScreenState.LANDING_MINIMAL) }
    var isAgreedToTerms by remember { mutableStateOf(false) }
    var showTermsValidationDialog by remember { mutableStateOf(false) }
    var showFullTermsDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<PendingAuthAction?>(null) }

    // Form inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    var resetSuccessNotification by remember { mutableStateOf<String?>(null) }

    val displayedError = localError ?: errorMessage
    val displayedSuccess = resetSuccessNotification ?: successMessage

    // Helper function to handle action with Terms check
    fun executeWithAgreementCheck(action: PendingAuthAction) {
        if (isAgreedToTerms) {
            when (action) {
                PendingAuthAction.GOOGLE -> {
                    localError = null
                    onGoogleSignIn()
                }
                PendingAuthAction.PASSWORD -> {
                    localError = null
                    onClearMessages()
                    screenState = LoginScreenState.PASSWORD_SIGN_IN
                }
                PendingAuthAction.SIGN_UP -> {
                    localError = null
                    onClearMessages()
                    screenState = LoginScreenState.SIGN_UP
                }
            }
        } else {
            pendingAction = action
            showTermsValidationDialog = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = screenState,
            transitionSpec = {
                if (targetState == LoginScreenState.LANDING_MINIMAL) {
                    (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                } else {
                    (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                }
            },
            label = "LoginScreenStateTransition"
        ) { targetScreen ->
            when (targetScreen) {
                LoginScreenState.LANDING_MINIMAL -> {
                    MinimalLandingView(
                        isLoading = isLoading,
                        isAgreed = isAgreedToTerms,
                        displayedError = displayedError,
                        displayedSuccess = displayedSuccess,
                        onToggleAgreement = { isAgreedToTerms = !isAgreedToTerms },
                        onContinueWithGoogle = { executeWithAgreementCheck(PendingAuthAction.GOOGLE) },
                        onContinueWithPassword = { executeWithAgreementCheck(PendingAuthAction.PASSWORD) },
                        onSignUp = { executeWithAgreementCheck(PendingAuthAction.SIGN_UP) },
                        onOpenTermsDialog = { showFullTermsDialog = true }
                    )
                }

                LoginScreenState.PASSWORD_SIGN_IN -> {
                    PasswordSignInView(
                        email = email,
                        password = password,
                        passwordVisible = passwordVisible,
                        isLoading = isLoading,
                        displayedError = displayedError,
                        displayedSuccess = displayedSuccess,
                        onEmailChange = {
                            email = it
                            localError = null
                        },
                        onPasswordChange = {
                            password = it
                            localError = null
                        },
                        onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                        onBack = {
                            screenState = LoginScreenState.LANDING_MINIMAL
                            localError = null
                            onClearMessages()
                        },
                        onForgotPassword = {
                            screenState = LoginScreenState.FORGOT_PASSWORD
                            localError = null
                            resetSuccessNotification = null
                            onClearMessages()
                        },
                        onSubmit = {
                            val trimmedEmail = email.trim()
                            val trimmedPass = password.trim()
                            if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
                                localError = "Please enter a valid email address."
                                return@PasswordSignInView
                            }
                            if (trimmedPass.isEmpty()) {
                                localError = "Please enter your password."
                                return@PasswordSignInView
                            }
                            localError = null
                            try {
                                if (FirebaseApp.getApps(context).isEmpty()) {
                                    FirebaseApp.initializeApp(context)
                                }
                            } catch (_: Throwable) {}
                            onEmailSignIn(trimmedEmail, trimmedPass)
                        },
                        onSwitchToSignUp = {
                            screenState = LoginScreenState.SIGN_UP
                            localError = null
                            onClearMessages()
                        }
                    )
                }

                LoginScreenState.SIGN_UP -> {
                    SignUpView(
                        name = name,
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword,
                        passwordVisible = passwordVisible,
                        confirmPasswordVisible = confirmPasswordVisible,
                        isLoading = isLoading,
                        displayedError = displayedError,
                        onNameChange = {
                            name = it
                            localError = null
                        },
                        onEmailChange = {
                            email = it
                            localError = null
                        },
                        onPasswordChange = {
                            password = it
                            localError = null
                        },
                        onConfirmPasswordChange = {
                            confirmPassword = it
                            localError = null
                        },
                        onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                        onToggleConfirmPasswordVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
                        onBack = {
                            screenState = LoginScreenState.LANDING_MINIMAL
                            localError = null
                            onClearMessages()
                        },
                        onSubmit = {
                            val trimmedName = name.trim()
                            val trimmedEmail = email.trim()
                            val trimmedPass = password.trim()
                            val trimmedConfirm = confirmPassword.trim()

                            if (trimmedName.isEmpty()) {
                                localError = "Please enter your full name."
                                return@SignUpView
                            }
                            if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
                                localError = "Please enter a valid email address."
                                return@SignUpView
                            }
                            if (trimmedPass.length < 6) {
                                localError = "Password must be at least 6 characters long."
                                return@SignUpView
                            }
                            if (trimmedPass != trimmedConfirm) {
                                localError = "Passwords do not match."
                                return@SignUpView
                            }

                            localError = null
                            try {
                                if (FirebaseApp.getApps(context).isEmpty()) {
                                    FirebaseApp.initializeApp(context)
                                }
                            } catch (_: Throwable) {}
                            onEmailSignUp(trimmedEmail, trimmedPass, trimmedName)
                        },
                        onSwitchToSignIn = {
                            screenState = LoginScreenState.PASSWORD_SIGN_IN
                            localError = null
                            onClearMessages()
                        }
                    )
                }

                LoginScreenState.FORGOT_PASSWORD -> {
                    ForgotPasswordView(
                        email = email,
                        isLoading = isLoading,
                        displayedError = displayedError,
                        displayedSuccess = displayedSuccess,
                        onEmailChange = {
                            email = it
                            localError = null
                            resetSuccessNotification = null
                        },
                        onBack = {
                            screenState = LoginScreenState.PASSWORD_SIGN_IN
                            localError = null
                            resetSuccessNotification = null
                            onClearMessages()
                        },
                        onSubmit = {
                            val trimmed = email.trim()
                            if (trimmed.isEmpty() || !trimmed.contains("@") || !trimmed.contains(".")) {
                                localError = "Please enter a valid email address."
                                return@ForgotPasswordView
                            }
                            localError = null
                            onResetPassword(trimmed)
                        }
                    )
                }
            }
        }

        // Terms of Use & Privacy Policy Validation Dialog (Prompted when user taps action before agreeing)
        if (showTermsValidationDialog) {
            TermsValidationDialog(
                onDismiss = {
                    showTermsValidationDialog = false
                    pendingAction = null
                },
                onAgreeAndContinue = {
                    isAgreedToTerms = true
                    showTermsValidationDialog = false
                    val actionToExecute = pendingAction
                    pendingAction = null
                    if (actionToExecute != null) {
                        when (actionToExecute) {
                            PendingAuthAction.GOOGLE -> {
                                localError = null
                                onGoogleSignIn()
                            }
                            PendingAuthAction.PASSWORD -> {
                                localError = null
                                onClearMessages()
                                screenState = LoginScreenState.PASSWORD_SIGN_IN
                            }
                            PendingAuthAction.SIGN_UP -> {
                                localError = null
                                onClearMessages()
                                screenState = LoginScreenState.SIGN_UP
                            }
                        }
                    }
                },
                onViewFullTerms = {
                    showTermsValidationDialog = false
                    showFullTermsDialog = true
                }
            )
        }

        // Full Terms & Privacy Policy Dialog
        if (showFullTermsDialog) {
            FullTermsAndPrivacyDialog(
                onDismiss = { showFullTermsDialog = false },
                onAccept = {
                    isAgreedToTerms = true
                    showFullTermsDialog = false
                }
            )
        }
    }
}

// -------------------------------------------------------------
// 1. MINIMAL DEEPSEEK-STYLE LANDING VIEW
// -------------------------------------------------------------
@Composable
private fun MinimalLandingView(
    isLoading: Boolean,
    isAgreed: Boolean,
    displayedError: String?,
    displayedSuccess: String?,
    onToggleAgreement: () -> Unit,
    onContinueWithGoogle: () -> Unit,
    onContinueWithPassword: () -> Unit,
    onSignUp: () -> Unit,
    onOpenTermsDialog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP SECTION: Logo, App Name, Tagline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
        ) {
            // App Logo with subtle rounded container
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, BentoEmerald.copy(alpha = 0.35f)),
                modifier = Modifier
                    .size(88.dp)
                    .testTag("app_logo_landing")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Image(
                        painter = safePainterResource(id = R.drawable.datacash_dc_wifi_icon_1786121930105),
                        contentDescription = "DataCash PK Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "DataCash PK",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.6).sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Monetize unused mobile bandwidth securely",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Optional Notification Banners
            if (displayedSuccess != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BentoEmeraldLight.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, BentoEmerald.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = BentoEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = displayedSuccess,
                            fontSize = 12.sp,
                            color = BentoEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (displayedError != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StopRed.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, StopRed.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = StopRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = displayedError,
                            fontSize = 12.sp,
                            color = StopRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // MIDDLE SECTION: 3 CLEAR PILL-SHAPED ACTION BUTTONS (DEEPSEEK STYLE)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Button 1: Continue with Google (Pill shape, Google G-logo)
            Button(
                onClick = onContinueWithGoogle,
                enabled = !isLoading,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp, pressedElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_continue_google")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = safePainterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google Logo",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Google",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.2).sp
                    )
                }
            }

            // Button 2: Continue with Password (Pill shape, Lock icon)
            Button(
                onClick = onContinueWithPassword,
                enabled = !isLoading,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_continue_password")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Password",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.2).sp
                    )
                }
            }

            // Button 3: Sign Up (Pill shape, Emerald highlight)
            Button(
                onClick = onSignUp,
                enabled = !isLoading,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BentoEmerald,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_sign_up")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Sign Up",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign Up",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    )
                }
            }
        }

        // BOTTOM SECTION: Terms of Service & Privacy Policy Agreement Radio/Checkbox
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleAgreement
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Radio/Checkbox toggle indicator
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (isAgreed) BentoEmerald else Color.Transparent
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (isAgreed) BentoEmerald else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                            shape = CircleShape
                        )
                        .testTag("terms_checkbox"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAgreed) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Agreed",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                val agreementText = buildAnnotatedString {
                    append("I agree to ")
                    withStyle(style = SpanStyle(color = BentoEmerald, fontWeight = FontWeight.Bold)) {
                        append("Terms of Service")
                    }
                    append(" and ")
                    withStyle(style = SpanStyle(color = BentoEmerald, fontWeight = FontWeight.Bold)) {
                        append("Privacy Policy")
                    }
                }

                Text(
                    text = agreementText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOpenTermsDialog
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Protected by Firebase 256-bit Cloud Security",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

// -------------------------------------------------------------
// 2. TERMS & CONDITIONS VALIDATION DIALOG
// -------------------------------------------------------------
@Composable
private fun TermsValidationDialog(
    onDismiss: () -> Unit,
    onAgreeAndContinue: () -> Unit,
    onViewFullTerms: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("terms_validation_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Surface(
                    shape = CircleShape,
                    color = BentoEmeraldLight.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, BentoEmerald.copy(alpha = 0.4f)),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = BentoEmerald,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Terms of Use & Privacy Policy",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Before proceeding, please review and agree to the DataCash PK service agreement and privacy protections:",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Highlights Summary List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TermsFeatureRow(
                        icon = Icons.Default.Security,
                        title = "Zero Identity Tracking",
                        description = "Personal files, photos, browsing history, and calls are never accessed."
                    )
                    TermsFeatureRow(
                        icon = Icons.Default.Speed,
                        title = "Safe Idle Bandwidth Only",
                        description = "Only unused network bandwidth is shared for verified data queries."
                    )
                    TermsFeatureRow(
                        icon = Icons.Default.MonetizationOn,
                        title = "Guaranteed Payouts",
                        description = "Cash out directly to JazzCash, EasyPaisa, or Pakistani Bank accounts."
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onViewFullTerms,
                    modifier = Modifier.testTag("btn_view_full_terms")
                ) {
                    Text(
                        text = "Read Full Terms & Policy Details",
                        fontSize = 12.sp,
                        color = BentoEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_terms_cancel"),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onAgreeAndContinue,
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoEmerald,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_terms_agree")
                    ) {
                        Text(
                            text = "Agree",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TermsFeatureRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BentoEmerald,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
    }
}

// -------------------------------------------------------------
// 3. FULL TERMS OF SERVICE & PRIVACY POLICY DIALOG
// -------------------------------------------------------------
@Composable
private fun FullTermsAndPrivacyDialog(
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("full_terms_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Policy,
                        contentDescription = null,
                        tint = BentoEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Terms & Privacy Policy",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1. DataCash PK Service Overview",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoEmerald
                    )
                    Text(
                        text = "DataCash PK enables registered users in Pakistan to monetize unused mobile data bandwidth. Network queries sent through your connection are strictly for distributed web indexing, public price comparison, and speed tests.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "2. User Privacy Guarantee",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoEmerald
                    )
                    Text(
                        text = "We guarantee 100% privacy protection. The app never accesses, reads, logs, or transmits personal media, contacts, SMS messages, credentials, or private browsing history.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "3. Earnings & Payout Processing",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoEmerald
                    )
                    Text(
                        text = "Earnings accumulate in Pakistani Rupees (PKR) based on shared Megabytes (MB). Users may initiate withdrawals via JazzCash, EasyPaisa, or Pakistani Bank accounts at any time with minimum threshold compliance.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "4. Account Security & Control",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoEmerald
                    )
                    Text(
                        text = "You retain complete control. You may pause bandwidth sharing instantly at any moment or request full account deletion. Authentication data is secured via Google Cloud & Firebase 256-bit encryption.",
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Close",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoEmerald,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_accept_full_terms")
                    ) {
                        Text(
                            text = "Accept Terms",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. PASSWORD SIGN-IN SUB-VIEW
// -------------------------------------------------------------
@Composable
private fun PasswordSignInView(
    email: String,
    password: String,
    passwordVisible: Boolean,
    isLoading: Boolean,
    displayedError: String?,
    displayedSuccess: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onBack: () -> Unit,
    onForgotPassword: () -> Unit,
    onSubmit: () -> Unit,
    onSwitchToSignUp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("btn_back_to_landing")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BentoEmerald
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sign In with Password",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Error Banner
            if (displayedError != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StopRed.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, StopRed.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = StopRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = displayedError,
                            fontSize = 12.sp,
                            color = StopRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Input Fields Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = { Text("Email Address") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = BentoEmerald)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_signin_email")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = BentoEmerald)
                        },
                        trailingIcon = {
                            IconButton(onClick = onTogglePasswordVisibility) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_signin_password")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onForgotPassword,
                            modifier = Modifier.testTag("btn_forgot_password")
                        ) {
                            Text(
                                text = "Forgot Password?",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoEmerald
                            )
                        }
                    }

                    // Sign In Pill Button
                    Button(
                        onClick = onSubmit,
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoEmerald,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_submit_signin")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Sign In",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Bottom switch to Sign Up
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account?",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(
                onClick = onSwitchToSignUp,
                modifier = Modifier.testTag("btn_switch_signup")
            ) {
                Text(
                    text = "Sign Up",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoEmerald
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 5. SIGN-UP SUB-VIEW
// -------------------------------------------------------------
@Composable
private fun SignUpView(
    name: String,
    email: String,
    password: String,
    confirmPassword: String,
    passwordVisible: Boolean,
    confirmPasswordVisible: Boolean,
    isLoading: Boolean,
    displayedError: String?,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
    onSwitchToSignIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("btn_signup_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BentoEmerald
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Create Account",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Error Banner
            if (displayedError != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StopRed.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, StopRed.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = StopRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = displayedError,
                            fontSize = 12.sp,
                            color = StopRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Registration Fields Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = onNameChange,
                        label = { Text("Full Name") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = BentoEmerald)
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_signup_name")
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = { Text("Email Address") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = BentoEmerald)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_signup_email")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = BentoEmerald)
                        },
                        trailingIcon = {
                            IconButton(onClick = onTogglePasswordVisibility) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_signup_password")
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = { Text("Confirm Password") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = BentoEmerald)
                        },
                        trailingIcon = {
                            IconButton(onClick = onToggleConfirmPasswordVisibility) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle confirm password visibility"
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_signup_confirm_password")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Create Account Pill Button
                    Button(
                        onClick = onSubmit,
                        enabled = !isLoading && name.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoEmerald,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_submit_signup")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Create Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Bottom switch to Sign In
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account?",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(
                onClick = onSwitchToSignIn,
                modifier = Modifier.testTag("btn_switch_signin")
            ) {
                Text(
                    text = "Sign In",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoEmerald
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 6. FORGOT PASSWORD SUB-VIEW
// -------------------------------------------------------------
@Composable
private fun ForgotPasswordView(
    email: String,
    isLoading: Boolean,
    displayedError: String?,
    displayedSuccess: String?,
    onEmailChange: (String) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("btn_forgot_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Sign In",
                        tint = BentoEmerald
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Reset Password",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Success Banner
            if (displayedSuccess != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BentoEmeraldLight.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, BentoEmerald.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = BentoEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = displayedSuccess,
                            fontSize = 12.sp,
                            color = BentoEmerald,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Error Banner
            if (displayedError != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StopRed.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, StopRed.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = StopRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = displayedError,
                            fontSize = 12.sp,
                            color = StopRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Enter your registered email address and we'll send you a password reset link via Firebase Auth.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = { Text("Email Address") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = BentoEmerald)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_forgot_email")
                    )

                    Button(
                        onClick = onSubmit,
                        enabled = !isLoading && email.isNotBlank(),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoEmerald,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_send_reset")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Send Reset Link",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Return button at bottom
        TextButton(
            onClick = onBack,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .testTag("btn_return_signin")
        ) {
            Text(
                text = "Return to Sign In",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = BentoEmerald
            )
        }
    }
}
