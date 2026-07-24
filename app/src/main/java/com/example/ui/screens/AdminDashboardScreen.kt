package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ApiKeyEntity
import com.example.data.local.ApiLogEntity
import com.example.data.local.OtpLogEntity
import com.example.ui.ApiViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.NeonButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    viewModel: ApiViewModel,
    modifier: Modifier = Modifier
) {
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val adminPinInput by viewModel.adminPinInput.collectAsState()
    val adminPinError by viewModel.adminPinError.collectAsState()
    val isGlobalServiceActive by viewModel.isGlobalServiceActive.collectAsState()

    val otpLogs by viewModel.otpLogs.collectAsState(initial = emptyList())
    val apiKeys by viewModel.apiKeys.collectAsState(initial = emptyList())
    val recentLogs by viewModel.recentLogs.collectAsState(initial = emptyList())
    val totalRequests by viewModel.totalRequests.collectAsState(initial = 0L)

    var selectedAdminTab by remember { mutableIntStateOf(0) }
    var showCreateKeyDialog by remember { mutableStateOf(false) }

    if (!isAdminLoggedIn) {
        // Admin Security PIN Login View
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                glowColor = NeonPurple,
                cornerRadius = 24.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = NeonPurple.copy(alpha = 0.15f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Admin Lock",
                                tint = NeonPurple,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "ADMIN STUDIO ACCESS",
                        color = NeonPurple,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Enter Security PIN to unlock API Control Panel",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    GlassTextField(
                        value = adminPinInput,
                        onValueChange = { viewModel.setAdminPinInput(it) },
                        label = "Admin Security PIN (Default: 1234)",
                        keyboardType = KeyboardType.NumberPassword,
                        testTag = "admin_pin_input"
                    )

                    if (adminPinError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = adminPinError!!,
                            color = NeonRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    NeonButton(
                        text = "UNLOCK DASHBOARD",
                        onClick = { viewModel.loginAdmin() },
                        modifier = Modifier.fillMaxWidth(),
                        accentColor = NeonPurple,
                        testTag = "admin_login_button"
                    )
                }
            }
        }
    } else {
        // Logged-In Admin Control Center
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Admin Status Bar with Global Service Switch
            GlassCard(glowColor = if (isGlobalServiceActive) NeonGreen else NeonRed) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isGlobalServiceActive) NeonGreen else NeonRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isGlobalServiceActive) "API GATEWAY ONLINE" else "GATEWAY OFF",
                                color = if (isGlobalServiceActive) NeonGreen else NeonRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = if (isGlobalServiceActive) "Receiving REST requests" else "Requests blocked (503 Service Unavailable)",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = isGlobalServiceActive,
                            onCheckedChange = { viewModel.toggleGlobalService(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = NeonGreen,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = CyberSurface
                            ),
                            modifier = Modifier.testTag("global_service_switch")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { viewModel.logoutAdmin() }) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Logout",
                                tint = NeonRed
                            )
                        }
                    }
                }
            }

            // Stat Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total OTPs",
                    value = otpLogs.size.toString(),
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Active Keys",
                    value = apiKeys.count { it.isEnabled }.toString(),
                    color = NeonPurple,
                    modifier = Modifier.weight(1f)
                )
                val verifiedCount = otpLogs.count { it.status == "VERIFIED" }
                val successRate = if (otpLogs.isNotEmpty()) (verifiedCount * 100 / otpLogs.size) else 100
                StatCard(
                    title = "Success Rate",
                    value = "$successRate%",
                    color = NeonGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            // Sub-Navigation Tabs
            TabRow(
                selectedTabIndex = selectedAdminTab,
                containerColor = CyberSurface,
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedAdminTab]),
                        color = NeonCyan
                    )
                }
            ) {
                Tab(
                    selected = selectedAdminTab == 0,
                    onClick = { selectedAdminTab = 0 },
                    text = { Text("OTP LOGS", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedAdminTab == 1,
                    onClick = { selectedAdminTab = 1 },
                    text = { Text("API KEYS", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedAdminTab == 2,
                    onClick = { selectedAdminTab = 2 },
                    text = { Text("AUDIT LOGS", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            // Tab Contents
            Box(modifier = Modifier.weight(1f)) {
                when (selectedAdminTab) {
                    0 -> OtpLogsTab(otpLogs = otpLogs, onClearLogs = { viewModel.clearAllLogs() })
                    1 -> ApiKeysTab(
                        apiKeys = apiKeys,
                        onToggleKey = { id, active -> viewModel.toggleApiKey(id, active) },
                        onDeleteKey = { id -> viewModel.deleteApiKey(id) },
                        onCreateKeyClick = { showCreateKeyDialog = true }
                    )
                    2 -> AuditLogsTab(recentLogs = recentLogs)
                }
            }
        }
    }

    if (showCreateKeyDialog) {
        CreateApiKeyDialog(
            onDismiss = { showCreateKeyDialog = false },
            onCreate = { name, rateLimit ->
                viewModel.createApiKey(name, rateLimit)
                showCreateKeyDialog = false
            }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(glowColor = color, modifier = modifier) {
        Text(title, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun OtpLogsTab(
    otpLogs: List<OtpLogEntity>,
    onClearLogs: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("OTP Request Stream (${otpLogs.size})", color = TextPrimary, fontWeight = FontWeight.Bold)
            if (otpLogs.isNotEmpty()) {
                TextButton(onClick = onClearLogs) {
                    Text("CLEAR ALL", color = NeonRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (otpLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No OTP requests recorded yet.", color = TextMuted)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(otpLogs) { log ->
                    Surface(
                        color = CyberSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(log.mobileNumber, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("OTP: ", color = TextMuted, fontSize = 11.sp)
                                    Text(
                                        log.otpCode,
                                        color = NeonCyan,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "${dateFormatter.format(Date(log.requestedAt))} | ${log.responseTimeMs}ms",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            StatusBadge(status = log.status)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApiKeysTab(
    apiKeys: List<ApiKeyEntity>,
    onToggleKey: (String, Boolean) -> Unit,
    onDeleteKey: (String) -> Unit,
    onCreateKeyClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Registered API Keys (${apiKeys.size})", color = TextPrimary, fontWeight = FontWeight.Bold)
            Button(
                onClick = onCreateKeyClick,
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("NEW KEY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(apiKeys) { key ->
                Surface(
                    color = CyberSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(key.keyName, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = key.isEnabled,
                                    onCheckedChange = { onToggleKey(key.id, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Black,
                                        checkedTrackColor = NeonCyan
                                    )
                                )
                                IconButton(onClick = { onDeleteKey(key.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NeonRed)
                                }
                            }
                        }
                        Text(
                            text = key.apiKeySecret,
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rate Limit: ${key.rateLimitPerMin} req/min | Total Hits: ${key.totalRequests}",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogsTab(recentLogs: List<ApiLogEntity>) {
    val dateFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Real-Time Request Audit Logs", color = TextPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(recentLogs) { log ->
                val statusColor = when (log.statusCode) {
                    200 -> NeonGreen
                    401 -> NeonOrange
                    429 -> NeonRed
                    else -> NeonRed
                }

                Surface(
                    color = CyberSurface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${log.method} ${log.endpoint}",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${log.statusCode}",
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${log.responseTimeMs}ms",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateApiKeyDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Int) -> Unit
) {
    var keyName by remember { mutableStateOf("") }
    var rateLimit by remember { mutableStateOf("10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberSurface,
        title = { Text("Generate New API Key", color = NeonCyan) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = keyName,
                    onValueChange = { keyName = it },
                    label = { Text("App / Client Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = rateLimit,
                    onValueChange = { rateLimit = it },
                    label = { Text("Rate Limit (Req / Minute)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(keyName, rateLimit.toIntOrNull() ?: 10) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
            ) {
                Text("Generate Key")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
