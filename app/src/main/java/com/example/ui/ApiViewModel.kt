package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.ApiRepository
import com.example.data.repository.OtpRequestResult
import com.example.data.repository.OtpVerifyResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OtpUiState {
    object Idle : OtpUiState()
    object Loading : OtpUiState()
    data class OtpSent(
        val mobile: String,
        val otpCodeDisplay: String,
        val expiresInSeconds: Int = 120
    ) : OtpUiState()
    data class Verified(val token: String, val verifiedAt: Long) : OtpUiState()
    data class Error(val message: String, val errorCode: String) : OtpUiState()
}

class ApiViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = ApiRepository(
        database.otpDao(),
        database.apiKeyDao(),
        database.apiLogDao()
    )

    val otpLogs = repository.allOtpLogs
    val apiKeys = repository.allApiKeys
    val recentLogs = repository.recentApiLogs
    val totalRequests = repository.totalApiRequestsCount

    private val _mobileInput = MutableStateFlow("+8801700000000")
    val mobileInput: StateFlow<String> = _mobileInput.asStateFlow()

    private val _otpInput = MutableStateFlow("")
    val otpInput: StateFlow<String> = _otpInput.asStateFlow()

    private val _selectedApiKeySecret = MutableStateFlow("rifat_live_sec_99482104812")
    val selectedApiKeySecret: StateFlow<String> = _selectedApiKeySecret.asStateFlow()

    private val _otpUiState = MutableStateFlow<OtpUiState>(OtpUiState.Idle)
    val otpUiState: StateFlow<OtpUiState> = _otpUiState.asStateFlow()

    private val _remainingCountdown = MutableStateFlow(120)
    val remainingCountdown: StateFlow<Int> = _remainingCountdown.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _adminPinInput = MutableStateFlow("")
    val adminPinInput: StateFlow<String> = _adminPinInput.asStateFlow()

    private val _adminPinError = MutableStateFlow<String?>(null)
    val adminPinError: StateFlow<String?> = _adminPinError.asStateFlow()

    private val _isGlobalServiceActive = MutableStateFlow(true)
    val isGlobalServiceActive: StateFlow<Boolean> = _isGlobalServiceActive.asStateFlow()

    private val _selectedTab = MutableStateFlow("USER_OTP") // USER_OTP, ADMIN_DASHBOARD, API_TESTER, BACKEND_CODE
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private var countdownJob: Job? = null

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun setMobileInput(value: String) {
        _mobileInput.value = value
    }

    fun setOtpInput(value: String) {
        if (value.length <= 6 && value.all { it.isDigit() }) {
            _otpInput.value = value
        }
    }

    fun setSelectedApiKeySecret(secret: String) {
        _selectedApiKeySecret.value = secret
    }

    fun setSelectedTab(tab: String) {
        _selectedTab.value = tab
    }

    fun setAdminPinInput(value: String) {
        _adminPinInput.value = value
        _adminPinError.value = null
    }

    fun requestOtp() {
        viewModelScope.launch {
            _otpUiState.value = OtpUiState.Loading
            _otpInput.value = ""
            val result = repository.requestOtp(_mobileInput.value, _selectedApiKeySecret.value)
            when (result) {
                is OtpRequestResult.Success -> {
                    _otpUiState.value = OtpUiState.OtpSent(
                        mobile = result.mobile,
                        otpCodeDisplay = result.otpCodeDisplay,
                        expiresInSeconds = result.expiresInSeconds
                    )
                    startCountdown(result.expiresInSeconds)
                }
                is OtpRequestResult.Error -> {
                    _otpUiState.value = OtpUiState.Error(result.message, result.errorCode)
                }
            }
        }
    }

    fun verifyOtp() {
        viewModelScope.launch {
            if (_otpInput.value.length < 6) {
                _otpUiState.value = OtpUiState.Error("Please enter complete 6-digit OTP code.", "INCOMPLETE_CODE")
                return@launch
            }
            _otpUiState.value = OtpUiState.Loading
            val result = repository.verifyOtp(_mobileInput.value, _otpInput.value)
            when (result) {
                is OtpVerifyResult.Success -> {
                    countdownJob?.cancel()
                    _otpUiState.value = OtpUiState.Verified(result.token, result.verifiedAt)
                }
                is OtpVerifyResult.Error -> {
                    _otpUiState.value = OtpUiState.Error(result.message, result.errorCode)
                }
            }
        }
    }

    fun resetOtpState() {
        countdownJob?.cancel()
        _otpUiState.value = OtpUiState.Idle
        _otpInput.value = ""
    }

    private fun startCountdown(seconds: Int) {
        countdownJob?.cancel()
        _remainingCountdown.value = seconds
        countdownJob = viewModelScope.launch {
            while (_remainingCountdown.value > 0) {
                delay(1000)
                _remainingCountdown.value -= 1
            }
        }
    }

    fun loginAdmin() {
        if (_adminPinInput.value == "1234" || _adminPinInput.value == "7860") {
            _isAdminLoggedIn.value = true
            _adminPinError.value = null
        } else {
            _adminPinError.value = "Invalid Admin PIN code. (Default PIN: 1234)"
        }
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        _adminPinInput.value = ""
    }

    fun createApiKey(name: String, rateLimit: Int) {
        viewModelScope.launch {
            repository.createApiKey(name, rateLimit)
        }
    }

    fun toggleApiKey(id: String, active: Boolean) {
        viewModelScope.launch {
            repository.toggleApiKeyStatus(id, active)
        }
    }

    fun deleteApiKey(id: String) {
        viewModelScope.launch {
            repository.deleteApiKey(id)
        }
    }

    fun toggleGlobalService(active: Boolean) {
        _isGlobalServiceActive.value = active
        repository.isGlobalServiceEnabled = active
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }
}
