package com.dld.tracker.ui.screen.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dld.tracker.DLTApplication
import com.dld.tracker.domain.engine.AciEngine
import com.dld.tracker.domain.usecase.ComputeAciUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardState(
    val isTracking: Boolean = false,
    val aciResult: AciEngine.AciResult = AciEngine.AciResult(0.0, "No Data", 0.0, 0.0, 0.0),
    val totalEvents: Int = 0,
    val totalInteractions: Int = 0,
    val totalSessions: Int = 0,
    val todayApps: Int = 0,
    val todayDwellMinutes: Long = 0
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val app = DLTApplication.instance
    private val repository = app.usageRepository
    private val computeAci = ComputeAciUseCase(repository)

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadData()
        observeCounts()
    }

    private fun observeCounts() {
        viewModelScope.launch {
            repository.getUsageEventCount().collect { count ->
                _state.update { it.copy(totalEvents = count) }
            }
        }
        viewModelScope.launch {
            repository.getInteractionCount().collect { count ->
                _state.update { it.copy(totalInteractions = count) }
            }
        }
        viewModelScope.launch {
            repository.getSessionCount().collect { count ->
                _state.update { it.copy(totalSessions = count) }
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            val isTracking = app.securePrefs.isTrackingEnabled()
            val aciResult = computeAci.compute(30)

            val todayStart = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            val apps = repository.getDistinctAppsSince(todayStart)
            val dwellMs = repository.getTotalDwellSince(todayStart) ?: 0L

            _state.update {
                it.copy(
                    isTracking = isTracking,
                    aciResult = aciResult,
                    todayApps = apps.size,
                    todayDwellMinutes = dwellMs / 60_000
                )
            }
        }
    }

    fun toggleTracking() {
        val newState = !_state.value.isTracking
        app.securePrefs.setTrackingEnabled(newState)

        if (newState) {
            com.dld.tracker.service.UsagePollingService.start(getApplication())
        } else {
            com.dld.tracker.service.UsagePollingService.stop(getApplication())
        }

        _state.update { it.copy(isTracking = newState) }
    }

    fun refreshAci() {
        loadData()
    }
}
