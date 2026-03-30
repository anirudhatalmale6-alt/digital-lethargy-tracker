package com.dld.tracker.domain.engine

/**
 * App Cognitive Inertia (ACI) Engine
 *
 * Computes a 0-10 score measuring "digital lethargy" based on:
 * - Dwell concentration: how long users stay stuck on single apps
 * - Transition frequency: how rarely they switch between apps
 * - Interaction density: how passively they consume (low taps/scrolls)
 *
 * High ACI = high lethargy (stuck, passive, low engagement)
 * Low ACI = active, purposeful usage
 */
object AciEngine {

    data class AciInput(
        val dwellTimes: List<Long>,      // per-app dwell durations in analysis window (ms)
        val transitionCount: Int,         // number of app switches in window
        val interactionCount: Int,        // total taps + scrolls in window
        val windowDurationMs: Long        // total analysis window size (ms)
    )

    data class AciResult(
        val score: Double,               // 0-10 scale
        val level: String,               // "Low", "Moderate", "High", "Very High"
        val dwellConcentration: Double,
        val transitionRate: Double,
        val interactionDensity: Double
    )

    fun compute(input: AciInput): AciResult {
        if (input.windowDurationMs == 0L || input.dwellTimes.isEmpty()) {
            return AciResult(0.0, "Insufficient Data", 0.0, 0.0, 0.0)
        }

        val windowMinutes = input.windowDurationMs / 60_000.0

        // Component 1: Dwell Concentration (0-4 points)
        // High value = user stuck on few apps for long periods
        val avgDwell = input.dwellTimes.average().coerceAtLeast(1.0)
        val maxDwell = input.dwellTimes.maxOrNull()?.toDouble() ?: 1.0
        val dwellConcentration = (maxDwell / avgDwell).coerceIn(1.0, 10.0)
        val dwellScore = ((dwellConcentration - 1.0) / 9.0) * 4.0

        // Component 2: Transition Rate (0-3.5 points)
        // Low transitions per minute = high inertia
        val transitionRate = input.transitionCount.toDouble() / windowMinutes.coerceAtLeast(0.1)
        val transitionScore = if (transitionRate >= 5.0) 0.0
        else ((5.0 - transitionRate) / 5.0) * 3.5

        // Component 3: Interaction Density (0-2.5 points)
        // Low interactions per minute = passive consumption
        val interactionDensity = input.interactionCount.toDouble() / windowMinutes.coerceAtLeast(0.1)
        val interactionScore = if (interactionDensity >= 20.0) 0.0
        else ((20.0 - interactionDensity) / 20.0) * 2.5

        val rawScore = dwellScore + transitionScore + interactionScore
        val score = rawScore.coerceIn(0.0, 10.0)

        val level = when {
            score < 2.5 -> "Low"
            score < 5.0 -> "Moderate"
            score < 7.5 -> "High"
            else -> "Very High"
        }

        return AciResult(
            score = Math.round(score * 100.0) / 100.0,
            level = level,
            dwellConcentration = Math.round(dwellConcentration * 100.0) / 100.0,
            transitionRate = Math.round(transitionRate * 100.0) / 100.0,
            interactionDensity = Math.round(interactionDensity * 100.0) / 100.0
        )
    }
}
