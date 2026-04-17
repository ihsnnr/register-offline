package com.registeroffline.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.registeroffline.core.util.TokenManager
import com.registeroffline.domain.usecase.GetProfileUseCase
import com.registeroffline.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val fullName: String = "",
    val email: String = "",
    val loggedOut: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val tokenManager: TokenManager,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    init {
        // Load cached profile
        viewModelScope.launch {
            tokenManager.fullNameFlow.collect { name ->
                _state.update { it.copy(fullName = name) }
            }
        }
        viewModelScope.launch {
            tokenManager.emailFlow.collect { email ->
                _state.update { it.copy(email = email) }
            }
        }
        // Try to refresh from server
        viewModelScope.launch {
            val result = getProfileUseCase()
            result.getOrNull()?.let { profile ->
                _state.update { it.copy(fullName = profile.fullName, email = profile.email) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _state.update { it.copy(loggedOut = true) }
        }
    }
}
