package com.registeroffline.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.registeroffline.domain.usecase.IsLoggedInUseCase
import com.registeroffline.domain.usecase.LoginUseCase
import com.registeroffline.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false,
    val registerSuccess: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    isLoggedInUseCase: IsLoggedInUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    val isLoggedIn: StateFlow<Boolean?> = isLoggedInUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = "Email dan password wajib diisi") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = loginUseCase(email, password)
            _state.update {
                if (result.isSuccess) it.copy(isLoading = false, loginSuccess = true)
                else it.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun register(email: String, password: String, fullName: String) {
        if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
            _state.update { it.copy(error = "Semua field wajib diisi") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = registerUseCase(email, password, fullName)
            _state.update {
                if (result.isSuccess) it.copy(isLoading = false, registerSuccess = result.getOrNull())
                else it.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun clearError() { _state.update { it.copy(error = null) } }
    fun clearRegisterSuccess() { _state.update { it.copy(registerSuccess = null) } }
}
