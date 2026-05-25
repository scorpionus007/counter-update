package com.skyworth.unlockcounter.ui.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.skyworth.unlockcounter.data.CloudSyncRepository
import com.skyworth.unlockcounter.ui.home.HistoryRow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CloudUserDetailUiState(
    val userName: String = "",
    val phone: String = "",
    val todayCount: Int = 0,
    val history: List<HistoryRow> = emptyList(),
    val totalUnlocks: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

class CloudUserDetailViewModel(
    private val cloudUserId: String,
    private val cloudSync: CloudSyncRepository
) : ViewModel() {

    private val displayFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault())
    private val _uiState = MutableStateFlow(CloudUserDetailUiState())
    val uiState: StateFlow<CloudUserDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val response = cloudSync.fetchCloudUserHistory(cloudUserId)
                val user = response.user
                val today = LocalDate.now().toString()
                val rows = response.history.map { day ->
                    val date = LocalDate.parse(day.date)
                    HistoryRow(
                        date = date,
                        label = date.format(displayFormatter),
                        count = day.unlockCount,
                        isToday = day.date == today
                    )
                }
                val todayCount = rows.firstOrNull { it.isToday }?.count ?: 0
                _uiState.value = CloudUserDetailUiState(
                    userName = user?.name ?: "Unknown",
                    phone = user?.phone ?: "",
                    todayCount = todayCount,
                    history = rows,
                    totalUnlocks = rows.sumOf { it.count },
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = CloudUserDetailUiState(
                    isLoading = false,
                    error = e.message ?: "Failed to load"
                )
            }
        }
    }

    class Factory(
        private val cloudUserId: String,
        private val cloudSync: CloudSyncRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CloudUserDetailViewModel::class.java)) {
                return CloudUserDetailViewModel(cloudUserId, cloudSync) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
