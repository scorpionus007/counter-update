package com.skyworth.unlockcounter.ui.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.skyworth.unlockcounter.data.UnlockRepository
import com.skyworth.unlockcounter.data.UserRepository
import com.skyworth.unlockcounter.data.remote.CloudUserSummary
import com.skyworth.unlockcounter.domain.UserSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UsersUiState(
    val cloudUsers: List<CloudUserSummary> = emptyList(),
    val localUsers: List<UserSummary> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val message: String? = null,
    val cloudError: String? = null
)

class UsersViewModel(
    private val userRepository: UserRepository,
    private val unlockRepository: UnlockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsersUiState())
    val uiState: StateFlow<UsersUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.observeUserSummaries().collect { local ->
                _uiState.value = _uiState.value.copy(localUsers = local)
            }
        }
        refreshCloud()
    }

    fun refreshCloud() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, cloudError = null)
            userRepository.fetchCloudUsers()
                .onSuccess { cloud ->
                    _uiState.value = _uiState.value.copy(
                        cloudUsers = cloud,
                        isLoading = false,
                        isRefreshing = false,
                        message = if (cloud.isNotEmpty()) "Loaded ${cloud.size} testers from cloud" else null
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        cloudError = e.message ?: "Could not load cloud users"
                    )
                }
        }
    }

    fun switchToUser(userId: Long, onSwitched: () -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.setActiveUser(userId)
                unlockRepository.refresh()
                onSwitched()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(message = "Switch failed: ${e.message}")
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    class Factory(
        private val userRepository: UserRepository,
        private val unlockRepository: UnlockRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UsersViewModel::class.java)) {
                return UsersViewModel(userRepository, unlockRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
