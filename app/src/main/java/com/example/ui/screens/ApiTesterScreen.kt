package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ApiViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.components.NeonButton
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun ApiTesterScreen(
    viewModel: ApiViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val selectedApiKeySecret by viewModel.selectedApiKeySecret.collectAsState()
    val mobileInput by viewModel.mobileInput.collectAsState()

    var testEndpoint by remember { mutableStateOf("/api/v1/otp/request") }
    var jsonRequestBody by remember {
        mutableStateOf(
            """{
  "mobile": "$mobileInput",
  "api_key": "$selectedApiKeySecret"
}"""
        )
    }

    var jsonResponseOutput by remember { mutableStateOf("// Click EXECUTE REST REQUEST to view response output") }
    var responseCode by remember { mutableStateOf("200 OK") }
    var isExecuting by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        GlassCard(glowColor = NeonCyan) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Terminal",
                    tint = NeonCyan,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "REST API CONSOLE & TESTER",
                        color = NeonCyan,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Interactive API Endpoint Testing & Payload Inspection",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Endpoint Selector Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EndpointChip(
                label = "POST /otp/request",
                isSelected = testEndpoint == "/api/v1/otp/request",
                onClick = {
                    testEndpoint = "/api/v1/otp/request"
                    jsonRequestBody = """{
  "mobile": "$mobileInput",
  "api_key": "$selectedApiKeySecret"
}"""
                },
                modifier = Modifier.weight(1f)
            )

            EndpointChip(
                label = "POST /otp/verify",
                isSelected = testEndpoint == "/api/v1/otp/verify",
                onClick = {
                    testEndpoint = "/api/v1/otp/verify"
                    jsonRequestBody = """{
  "mobile": "$mobileInput",
  "code": "123456"
}"""
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Request Body Editor
        GlassCard(glowColor = NeonPurple) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("JSON REQUEST PAYLOAD", color = NeonPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(testEndpoint, color = NeonCyan, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))

            GlassTextField(
                value = jsonRequestBody,
                onValueChange = { jsonRequestBody = it },
                label = "JSON Body",
                testTag = "api_json_body_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            NeonButton(
                text = "EXECUTE REST REQUEST",
                onClick = {
                    coroutineScope.launch {
                        isExecuting = true
                        jsonResponseOutput = "Executing request via API Studio Gateway..."
                        kotlinx.coroutines.delay(400)

                        if (testEndpoint == "/api/v1/otp/request") {
                            val res = viewModel.repository.requestOtp(mobileInput, selectedApiKeySecret)
                            when (res) {
                                is com.example.data.repository.OtpRequestResult.Success -> {
                                    responseCode = "200 OK"
                                    jsonResponseOutput = """{
  "status": "success",
  "otp_id": ${res.otpId},
  "mobile": "${res.mobile}",
  "otp_code_display": "${res.otpCodeDisplay}",
  "expires_in_seconds": ${res.expiresInSeconds},
  "message": "${res.message}"
}"""
                                }
                                is com.example.data.repository.OtpRequestResult.Error -> {
                                    responseCode = "ERROR (${res.errorCode})"
                                    jsonResponseOutput = """{
  "status": "error",
  "error_code": "${res.errorCode}",
  "message": "${res.message}"
}"""
                                }
                            }
                        } else {
                            val res = viewModel.repository.verifyOtp(mobileInput, "123456")
                            when (res) {
                                is com.example.data.repository.OtpVerifyResult.Success -> {
                                    responseCode = "200 OK"
                                    jsonResponseOutput = """{
  "status": "verified",
  "authorization_token": "${res.token}",
  "verified_at": ${res.verifiedAt},
  "message": "${res.message}"
}"""
                                }
                                is com.example.data.repository.OtpVerifyResult.Error -> {
                                    responseCode = "ERROR (${res.errorCode})"
                                    jsonResponseOutput = """{
  "status": "error",
  "error_code": "${res.errorCode}",
  "message": "${res.message}"
}"""
                                }
                            }
                        }
                        isExecuting = false
                    }
                },
                isLoading = isExecuting,
                modifier = Modifier.fillMaxWidth(),
                accentColor = NeonGreen,
                testTag = "execute_api_test_button"
            )
        }

        // Response Output Viewer
        GlassCard(glowColor = NeonGreen) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("RESPONSE OUTPUT", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "[$responseCode]",
                        color = if (responseCode.contains("200")) NeonGreen else com.example.ui.theme.NeonRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(jsonResponseOutput))
                        Toast.makeText(context, "Response JSON copied!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Response",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = CyberSurface,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = jsonResponseOutput,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Python Flask Backend Integration Instructions
        GlassCard(glowColor = NeonMagenta) {
            Text("PYTHON FLASK BACKEND ARCHITECTURE", color = NeonMagenta, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Rifat API Studio contains a complete standalone Python Flask backend service in `/backend/app.py`. " +
                        "Deploy it to PythonAnywhere, Render, or Railway to handle production REST traffic.",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun EndpointChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else CyberSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) NeonCyan else GlassBorder
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) NeonCyan else TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
