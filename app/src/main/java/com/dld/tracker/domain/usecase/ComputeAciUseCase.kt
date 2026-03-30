package com.dld.tracker.domain.usecase

import com.dld.tracker.data.repository.UsageRepository
import com.dld.tracker.domain.engine.AciEngine

class ComputeAciUseCase(private val repository: UsageRepository) {

    suspend fun compute(windowMinutes: Int = 30): AciEngine.AciResult {
        val now = System.currentTimeMillis()
        val windowMs = windowMinutes * 60_000L
        val sinceMs = now - windowMs

        val usageEvents = repository.getUsageEventsByRange(sinceMs, now)
        val dwellTimes = usageEvents.map { it.dwellMs }
        val transitionCount = usageEvents.size

        val tapCount = repository.getInteractionCountByType("tap", sinceMs)
        val scrollCount = repository.getInteractionCountByType("scroll", sinceMs)
        val interactionCount = tapCount + scrollCount

        return AciEngine.compute(
            AciEngine.AciInput(
                dwellTimes = dwellTimes,
                transitionCount = transitionCount,
                interactionCount = interactionCount,
                windowDurationMs = windowMs
            )
        )
    }
}
