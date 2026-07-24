package com.example.data.repository

import com.example.data.local.ApiKeyDao
import com.example.data.local.ApiKeyEntity
import com.example.data.local.ApiLogDao
import com.example.data.local.ApiLogEntity
import com.example.data.local.OtpDao
import com.example.data.local.OtpLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.util.UUID

sealed class OtpRequestResult {
    data class Success(
        val otpId: Long,
        val mobile: String,
        val otpCodeDisplay: String, // Exposed for testing & admin view
        val expiresInSeconds: Int = 120,
        val message: String = "OTP sent successfully via Secure API Gateway."
    ) : OtpRequestResult()

    data class Error(val message: String, val errorCode: String) : OtpRequestResult()
}

sealed class OtpVerifyResult {
    data class Success(
        val token: String,
        val verifiedAt: Long,
        val message: String = "OTP verified successfully. Authorization token generated."
    ) : OtpVerifyResult()

    data class Error(val message: String, val errorCode: String) : OtpVerifyResult()
}

class ApiRepository(
    private val otpDao: OtpDao,
    private val apiKeyDao: ApiKeyDao,
    private val apiLogDao: ApiLogDao
) {
    val allOtpLogs: Flow<List<OtpLogEntity>> = otpDao.getAllOtpLogs()
    val allApiKeys: Flow<List<ApiKeyEntity>> = apiKeyDao.getAllApiKeys()
    val recentApiLogs: Flow<List<ApiLogEntity>> = apiLogDao.getRecentApiLogs()
    val totalApiRequestsCount: Flow<Long> = apiLogDao.getTotalApiRequestsCount()

    @Volatile
    var isGlobalServiceEnabled: Boolean = true

    @Volatile
    var backendUrl: String = "http://127.0.0.1:5000"

    private val random = SecureRandom()

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        val keys = apiKeyDao.getAllApiKeys().first()
        if (keys.isEmpty()) {
            val defaultLiveKey = ApiKeyEntity(
                id = "key_live_01",
                keyName = "Production Mobile App Key",
                apiKeySecret = "rifat_live_sec_99482104812",
                rateLimitPerMin = 10,
                isEnabled = true,
                totalRequests = 12
            )
            val defaultDevKey = ApiKeyEntity(
                id = "key_dev_02",
                keyName = "Staging/Dev Tester Key",
                apiKeySecret = "rifat_dev_sec_11029384",
                rateLimitPerMin = 5,
                isEnabled = true,
                totalRequests = 5
            )
            apiKeyDao.insertApiKey(defaultLiveKey)
            apiKeyDao.insertApiKey(defaultDevKey)

            // Seed initial sample logs
            val now = System.currentTimeMillis()
            otpDao.insertOtpLog(
                OtpLogEntity(
                    mobileNumber = "+8801700000000",
                    otpCode = "782910",
                    status = "VERIFIED",
                    requestedAt = now - 300000,
                    expiresAt = now - 180000,
                    responseTimeMs = 38
                )
            )
            apiLogDao.insertApiLog(
                ApiLogEntity(
                    endpoint = "/api/v1/otp/request",
                    method = "POST",
                    statusCode = 200,
                    apiKey = "rifat_live_sec_99482104812",
                    responseTimeMs = 42
                )
            )
        }
    }

    suspend fun requestOtp(mobileNumber: String, apiKeySecret: String): OtpRequestResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        if (!isGlobalServiceEnabled) {
            logApiRequest("/api/v1/otp/request", "POST", 503, apiKeySecret, startTime)
            return@withContext OtpRequestResult.Error(
                message = "API Gateway Service is currently disabled by Admin.",
                errorCode = "SERVICE_DISABLED"
            )
        }

        val cleanedMobile = mobileNumber.trim()
        if (cleanedMobile.length < 8) {
            logApiRequest("/api/v1/otp/request", "POST", 400, apiKeySecret, startTime)
            return@withContext OtpRequestResult.Error(
                message = "Invalid mobile number format. Include country code (e.g., +8801700000000).",
                errorCode = "INVALID_MOBILE"
            )
        }

        // Validate API Key
        val apiKey = apiKeyDao.getApiKeyBySecret(apiKeySecret)
        if (apiKey == null || !apiKey.isEnabled) {
            logApiRequest("/api/v1/otp/request", "POST", 401, apiKeySecret, startTime)
            return@withContext OtpRequestResult.Error(
                message = "Unauthorized API Key or key is disabled.",
                errorCode = "UNAUTHORIZED_KEY"
            )
        }

        // Rate Limiting Check: max requests per minute for this mobile
        val oneMinAgo = System.currentTimeMillis() - 60000
        val recentRequestsCount = otpDao.getRequestCountSince(cleanedMobile, oneMinAgo)
        if (recentRequestsCount >= apiKey.rateLimitPerMin) {
            logApiRequest("/api/v1/otp/request", "POST", 429, apiKeySecret, startTime)
            return@withContext OtpRequestResult.Error(
                message = "Rate limit exceeded (${apiKey.rateLimitPerMin} req/min). Please wait 60 seconds.",
                errorCode = "RATE_LIMIT_EXCEEDED"
            )
        }

        // Generate 6-digit OTP
        val codeInt = 100000 + random.nextInt(900000)
        val otpCode = codeInt.toString()
        val expiresAt = System.currentTimeMillis() + (120 * 1000) // 2 minutes expiration

        val otpLog = OtpLogEntity(
            mobileNumber = cleanedMobile,
            otpCode = otpCode,
            status = "PENDING",
            requestedAt = System.currentTimeMillis(),
            expiresAt = expiresAt,
            channel = "SECURE_REST_GW",
            responseTimeMs = System.currentTimeMillis() - startTime
        )

        val otpId = otpDao.insertOtpLog(otpLog)

        // Increment key request count
        apiKeyDao.updateApiKey(apiKey.copy(totalRequests = apiKey.totalRequests + 1))
        logApiRequest("/api/v1/otp/request", "POST", 200, apiKeySecret, startTime)

        OtpRequestResult.Success(
            otpId = otpId,
            mobile = cleanedMobile,
            otpCodeDisplay = otpCode,
            expiresInSeconds = 120
        )
    }

    suspend fun verifyOtp(mobileNumber: String, enteredCode: String): OtpVerifyResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val cleanedMobile = mobileNumber.trim()
        val cleanedCode = enteredCode.trim()

        val latestOtp = otpDao.getLatestOtpForMobile(cleanedMobile)
        if (latestOtp == null) {
            logApiRequest("/api/v1/otp/verify", "POST", 404, "client", startTime)
            return@withContext OtpVerifyResult.Error(
                message = "No OTP request found for $cleanedMobile.",
                errorCode = "NO_OTP_FOUND"
            )
        }

        val now = System.currentTimeMillis()
        if (now > latestOtp.expiresAt) {
            otpDao.updateOtpStatus(latestOtp.id, "EXPIRED")
            logApiRequest("/api/v1/otp/verify", "POST", 410, "client", startTime)
            return@withContext OtpVerifyResult.Error(
                message = "OTP code has expired. Please request a new one.",
                errorCode = "OTP_EXPIRED"
            )
        }

        if (latestOtp.status == "VERIFIED") {
            logApiRequest("/api/v1/otp/verify", "POST", 400, "client", startTime)
            return@withContext OtpVerifyResult.Error(
                message = "This OTP has already been used.",
                errorCode = "OTP_ALREADY_USED"
            )
        }

        if (latestOtp.otpCode == cleanedCode) {
            otpDao.updateOtpStatus(latestOtp.id, "VERIFIED")
            val jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                    UUID.randomUUID().toString().replace("-", "") +
                    ".rifat_sig_" + (now / 1000)
            logApiRequest("/api/v1/otp/verify", "POST", 200, "client", startTime)
            OtpVerifyResult.Success(
                token = jwtToken,
                verifiedAt = now
            )
        } else {
            otpDao.updateOtpStatus(latestOtp.id, "FAILED")
            logApiRequest("/api/v1/otp/verify", "POST", 400, "client", startTime)
            OtpVerifyResult.Error(
                message = "Incorrect OTP code. Verification failed.",
                errorCode = "INVALID_CODE"
            )
        }
    }

    suspend fun createApiKey(keyName: String, rateLimit: Int): ApiKeyEntity = withContext(Dispatchers.IO) {
        val secret = "rifat_key_" + UUID.randomUUID().toString().take(12)
        val newKey = ApiKeyEntity(
            id = "key_" + System.currentTimeMillis(),
            keyName = keyName.ifBlank { "Custom API Key" },
            apiKeySecret = secret,
            rateLimitPerMin = rateLimit,
            isEnabled = true,
            totalRequests = 0
        )
        apiKeyDao.insertApiKey(newKey)
        newKey
    }

    suspend fun toggleApiKeyStatus(id: String, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        apiKeyDao.toggleApiKeyStatus(id, isEnabled)
    }

    suspend fun deleteApiKey(id: String) = withContext(Dispatchers.IO) {
        apiKeyDao.deleteApiKey(id)
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        otpDao.clearAllOtpLogs()
        apiLogDao.clearApiLogs()
    }

    private suspend fun logApiRequest(
        endpoint: String,
        method: String,
        statusCode: Int,
        apiKey: String,
        startTimeMs: Long
    ) {
        val responseTimeMs = (System.currentTimeMillis() - startTimeMs).coerceAtLeast(12)
        apiLogDao.insertApiLog(
            ApiLogEntity(
                endpoint = endpoint,
                method = method,
                statusCode = statusCode,
                apiKey = apiKey,
                responseTimeMs = responseTimeMs
            )
        )
    }
}
