package com.skyworth.unlockcounter.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.skyworth.unlockcounter.data.UnlockRepository
import com.skyworth.unlockcounter.data.UserRepository
import com.skyworth.unlockcounter.domain.DailyUnlock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HomeUiState(
    val userName: String = "",
    val todayCount: Int = 0,
    val lastUpdatedAt: Instant? = null,
    val history: List<HistoryRow> = emptyList(),
    val isRefreshing: Boolean = false,
    val isLoading: Boolean = true,
    val snackbarMessage: String? = null
)

data class HistoryRow(
    val date: LocalDate,
    val label: String,
    val count: Int,
    val isToday: Boolean
)

class HomeViewModel(
    private val repository: UnlockRepository,
    private val userRepository: UserRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : ViewModel() {

    private val displayFormatter =
        DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var previousTodayCount = 0
    private var initialLoadDone = false

    init {
        viewModelScope.launch {
            val user = userRepository.getActiveUser()
            _uiState.value = _uiState.value.copy(userName = user?.name ?: "")

            val userId = user?.id
            if (userId != null) {
                repository.backfillIfNeeded(userId)
                repository.observeHistory(userId, 30).collect { history ->
                    updateUiFromHistory(history)
                }
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
        refresh(silent = true)
    }

    fun refresh(silent: Boolean = false) {
        if (_uiState.value.isRefreshing) return
        viewModelScope.launch {
            if (userRepository.getActiveUserId() == null) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            try {
                val result = repository.refresh()
                val delta = result.todayCount - previousTodayCount
                previousTodayCount = result.todayCount
                initialLoadDone = true

                val snackbar = when {
                    !silent && delta > 0 -> "Updated: +$delta unlocks"
                    !silent && delta < 0 -> "Updated: $delta unlocks"
                    !silent -> "Count is up to date"
                    else -> _uiState.value.snackbarMessage
                }

                _uiState.value = _uiState.value.copy(
                    snackbarMessage = snackbar,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "Refresh failed: ${e.message}",
                    isLoading = false
                )
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun onResume() {
        viewModelScope.launch {
            val user = userRepository.getActiveUser()
            _uiState.value = _uiState.value.copy(userName = user?.name ?: "")
        }
        refresh(silent = true)
    }

    private fun updateUiFromHistory(history: List<DailyUnlock>) {
        val today = LocalDate.now(zoneId)
        val todayEntry = history.firstOrNull { it.date == today }
        val todayCount = todayEntry?.count ?: 0
        if (!initialLoadDone && todayCount > 0) {
            previousTodayCount = todayCount
        }

        val rows = history.map { entry ->
            HistoryRow(
                date = entry.date,
                label = entry.date.format(displayFormatter),
                count = entry.count,
                isToday = entry.date == today
            )
        }

        _uiState.value = _uiState.value.copy(
            todayCount = todayCount,
            lastUpdatedAt = todayEntry?.lastUpdatedAt,
            history = rows.filter { !it.isToday },
            isLoading = !initialLoadDone && history.isEmpty() && !_uiState.value.isRefreshing
        )
    }

    class Factory(
        private val repository: UnlockRepository,
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(repository, userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

fun formatUpdatedAgo(instant: Instant?): String {
    if (instant == null) return "Not updated yet"
    val duration = java.time.Duration.between(instant, Instant.now())
    val minutes = duration.toMinutes()
    return when {
        minutes < 1 -> "Updated just now"
        minutes < 60 -> "Updated $minutes min ago"
        duration.toHours() < 24 -> "Updated ${duration.toHours()} hr ago"
        else -> "Updated ${duration.toDays()} day ago"
    }
}
