package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.ApiViewModel
import com.example.ui.OtpUiState
import com.example.ui.components.CountdownBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.NeonButton
import com.example.ui.components.OtpPinInput
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun UserOtpScreen(
    viewModel: ApiViewModel,
    modifier: Modifier = Modifier
) {
    val mobileInput by viewModel.mobileInput.collectAsState()
    val otpInput by viewModel.otpInput.collectAsState()
    val selectedApiKeySecret by viewModel.selectedApiKeySecret.collectAsState()
    val otpUiState by viewModel.otpUiState.collectAsState()
    val countdown by viewModel.remainingCountdown.collectAsState()
    val apiKeys by viewModel.apiKeys.collectAsState(initial = emptyList())

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    var keyDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Studio Header Banner
        GlassCard(
            glowColor = NeonCyan,
            cornerRadius = 20.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RIFAT API STUDIO",
                        color = NeonCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Secure OTP Verification Portal & REST Gateway",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = NeonCyan.copy(alpha = 0.15f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Active API Key Selector Widget
        GlassCard(glowColor = NeonPurple) {
            Text(
                text = "AUTHENTICATION KEY SYSTEM",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberSurface)
                        .clickable { keyDropdownExpanded = true }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Key",
                            tint = NeonMagenta,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val currentKey = apiKeys.find { it.apiKeySecret == selectedApiKeySecret }
                        Text(
                            text = currentKey?.keyName ?: "Select API Key",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "CHANGE ▼",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                DropdownMenu(
                    expanded = keyDropdownExpanded,
                    onDismissRequest = { keyDropdownExpanded = false },
                    modifier = Modifier.background(CyberSurface)
                ) {
                    apiKeys.forEach { key ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(key.keyName, color = TextPrimary, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Limit: ${key.rateLimitPerMin} req/min | Secret: ${key.apiKeySecret.take(12)}...",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            },
                            onClick = {
                                viewModel.setSelectedApiKeySecret(key.apiKeySecret)
                                keyDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // OTP Request Form Card
        GlassCard(glowColor = NeonCyan) {
            Text(
                text = "REQUEST OTP CODE",
                color = NeonCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            GlassTextField(
                value = mobileInput,
                onValueChange = { viewModel.setMobileInput(it) },
                label = "Mobile Number (with Country Code)",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = NeonCyan
                    )
                },
                keyboardType = KeyboardType.Phone,
                testTag = "mobile_input_field"
            )

            Spacer(modifier = Modifier.height(14.dp))

            NeonButton(
                text = "SEND OTP REQUEST",
                onClick = { viewModel.requestOtp() },
                isLoading = otpUiState is OtpUiState.Loading,
                modifier = Modifier.fillMaxWidth(),
                accentColor = NeonCyan,
                testTag = "send_otp_button"
            )
        }

        // OTP Verification View & Status
        AnimatedVisibility(
            visible = otpUiState is OtpUiState.OtpSent || otpUiState is OtpUiState.Verified || otpUiState is OtpUiState.Error,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            when (val state = otpUiState) {
                is OtpUiState.OtpSent -> {
                    GlassCard(glowColor = NeonMagenta) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ENTER 6-DIGIT OTP",
                                color = NeonMagenta,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            CountdownBadge(remainingSeconds = countdown)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Test/Demo Display of generated OTP for convenient verification
                        Surface(
                            color = NeonMagenta.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Generated OTP Code: ${state.otpCodeDisplay}",
                                    color = NeonMagenta,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "TAP TO COPY",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    modifier = Modifier.clickable {
                                        clipboardManager.setText(AnnotatedString(state.otpCodeDisplay))
                                        viewModel.setOtpInput(state.otpCodeDisplay)
                                        Toast.makeText(context, "OTP Code Copied!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OtpPinInput(
                            otpValue = otpInput,
                            onValueChange = { viewModel.setOtpInput(it) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            NeonButton(
                                text = "VERIFY OTP",
                                onClick = { viewModel.verifyOtp() },
                                modifier = Modifier.weight(1f),
                                accentColor = NeonGreen,
                                testTag = "verify_otp_button"
                            )

                            IconButton(
                                onClick = { viewModel.requestOtp() },
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CyberSurface)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Resend",
                                    tint = NeonCyan
                                )
                            }
                        }
                    }
                }

                is OtpUiState.Verified -> {
                    GlassCard(glowColor = NeonGreen) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = NeonGreen,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "OTP VERIFICATION SUCCESSFUL",
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Secure JWT Token generated & active.",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = CyberSurface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("JWT AUTHORIZATION TOKEN:", color = TextMuted, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = state.token,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(state.token))
                                            Toast.makeText(context, "Token Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy Auth Token", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        NeonButton(
                            text = "NEW VERIFICATION",
                            onClick = { viewModel.resetOtpState() },
                            modifier = Modifier.fillMaxWidth(),
                            accentColor = NeonCyan,
                            testTag = "reset_verification_button"
                        )
                    }
                }

                is OtpUiState.Error -> {
                    GlassCard(glowColor = NeonRed) {
                        Text(
                            text = "VERIFICATION ERROR (${state.errorCode})",
                            color = NeonRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.message,
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NeonButton(
                            text = "RETRY",
                            onClick = { viewModel.resetOtpState() },
                            modifier = Modifier.fillMaxWidth(),
                            accentColor = NeonOrange,
                            testTag = "retry_button"
                        )
                    }
                }

                else -> {}
            }
        }

        // Security Features Grid
        GlassCard(glowColor = GlassBorder) {
            Text(
                text = "GATEWAY SECURITY ARCHITECTURE",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            SecurityFeatureItem(
                icon = Icons.Default.Lock,
                title = "AES-256 OTP Encryption",
                subtitle = "Cryptographically secure random digit generation"
            )
            SecurityFeatureItem(
                icon = Icons.Default.Speed,
                title = "Adaptive Rate Limiting",
                subtitle = "Enforces max requests per minute per mobile number"
            )
            SecurityFeatureItem(
                icon = Icons.Default.Security,
                title = "120s TTL Expiration Window",
                subtitle = "Automatic invalidation upon timer expiration"
            )
        }
    }
}

@Composable
fun SecurityFeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = CyberSurface,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextSecondary, fontSize = 10.sp)
        }
    }
}
