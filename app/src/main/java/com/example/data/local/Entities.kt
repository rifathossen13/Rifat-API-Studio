package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "otp_logs")
data class OtpLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mobileNumber: String,
    val otpCode: String,
    val status: String, // PENDING, VERIFIED, EXPIRED, FAILED
    val requestedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long,
    val channel: String = "REST_API",
    val responseTimeMs: Long = 45
)

@Entity(tableName = "api_keys")
data class ApiKeyEntity(
    @PrimaryKey val id: String,
    val keyName: String,
    val apiKeySecret: String,
    val rateLimitPerMin: Int = 10,
    val isEnabled: Boolean = true,
    val totalRequests: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "api_logs")
data class ApiLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val endpoint: String,
    val method: String,
    val statusCode: Int,
    val apiKey: String,
    val responseTimeMs: Long,
    val clientIp: String = "127.0.0.1",
    val timestamp: Long = System.currentTimeMillis()
)
