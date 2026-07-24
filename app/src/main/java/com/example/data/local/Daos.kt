package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OtpDao {
    @Query("SELECT * FROM otp_logs ORDER BY requestedAt DESC")
    fun getAllOtpLogs(): Flow<List<OtpLogEntity>>

    @Query("SELECT * FROM otp_logs WHERE mobileNumber = :mobile ORDER BY requestedAt DESC LIMIT 1")
    suspend fun getLatestOtpForMobile(mobile: String): OtpLogEntity?

    @Query("SELECT COUNT(*) FROM otp_logs WHERE mobileNumber = :mobile AND requestedAt >= :sinceTimestamp")
    suspend fun getRequestCountSince(mobile: String, sinceTimestamp: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOtpLog(otpLog: OtpLogEntity): Long

    @Update
    suspend fun updateOtpLog(otpLog: OtpLogEntity)

    @Query("UPDATE otp_logs SET status = :status WHERE id = :id")
    suspend fun updateOtpStatus(id: Long, status: String)

    @Query("DELETE FROM otp_logs")
    suspend fun clearAllOtpLogs()
}

@Dao
interface ApiKeyDao {
    @Query("SELECT * FROM api_keys ORDER BY createdAt DESC")
    fun getAllApiKeys(): Flow<List<ApiKeyEntity>>

    @Query("SELECT * FROM api_keys WHERE apiKeySecret = :keySecret LIMIT 1")
    suspend fun getApiKeyBySecret(keySecret: String): ApiKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiKey(apiKey: ApiKeyEntity)

    @Update
    suspend fun updateApiKey(apiKey: ApiKeyEntity)

    @Query("UPDATE api_keys SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun toggleApiKeyStatus(id: String, isEnabled: Boolean)

    @Query("DELETE FROM api_keys WHERE id = :id")
    suspend fun deleteApiKey(id: String)
}

@Dao
interface ApiLogDao {
    @Query("SELECT * FROM api_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentApiLogs(): Flow<List<ApiLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiLog(log: ApiLogEntity)

    @Query("DELETE FROM api_logs")
    suspend fun clearApiLogs()

    @Query("SELECT COUNT(*) FROM api_logs")
    fun getTotalApiRequestsCount(): Flow<Long>
}
