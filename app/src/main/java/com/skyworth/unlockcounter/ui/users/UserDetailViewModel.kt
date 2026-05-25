package com.skyworth.unlockcounter.ui.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.skyworth.unlockcounter.data.UnlockRepository
import com.skyworth.unlockcounter.data.UserRepository
import com.skyworth.unlockcounter.domain.DailyUnlock
import com.skyworth.unlockcounter.ui.home.HistoryRow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class UserDetailUiState(
    val userName: String = "",
    val todayCount: Int = 0,
    val history: List<HistoryRow> = emptyList(),
    val totalUnlocks: Int = 0,
    val isLoading: Boolean = true
)

class UserDetailViewModel(
    private val userId: Long,
    private val userRepository: UserRepository,
    private val unlockRepository: UnlockRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : ViewModel() {

    private val displayFormatter =
        DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())

    private val _uiState = MutableStateFlow(UserDetailUiState())
    val uiState: StateFlow<UserDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = userRepository.getUserProfile(userId)
            if (profile == null) {
                _uiState.value = UserDetailUiState(isLoading = false)
                return@launch
            }
            unlockRepository.observeHistory(userId, 30).collect { history ->
                updateState(profile.name, history)
            }
        }
    }

    private fun updateState(userName: String, history: List<DailyUnlock>) {
        val today = LocalDate.now(zoneId)
        val todayEntry = history.firstOrNull { it.date == today }
        val rows = history.map { entry ->
            HistoryRow(
                date = entry.date,
                label = entry.date.format(displayFormatter),
                count = entry.count,
                isToday = entry.date == today
            )
        }
        _uiState.value = UserDetailUiState(
            userName = userName,
            todayCount = todayEntry?.count ?: 0,
            history = rows,
            totalUnlocks = history.sumOf { it.count },
            isLoading = false
        )
    }

    class Factory(
        private val userId: Long,
        private val userRepository: UserRepository,
        private val unlockRepository: UnlockRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserDetailViewModel::class.java)) {
                return UserDetailViewModel(userId, userRepository, unlockRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
