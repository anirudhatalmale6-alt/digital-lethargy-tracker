package com.dld.tracker.data.repository

import com.dld.tracker.data.local.dao.*
import com.dld.tracker.data.local.entity.*
import kotlinx.coroutines.flow.Flow

class UsageRepository(
    private val usageEventDao: UsageEventDao,
    private val interactionEventDao: InteractionEventDao,
    private val sessionDao: SessionDao,
    private val selfReportDao: SelfReportDao,
    private val reactionTestDao: ReactionTestDao
) {
    // Usage Events
    suspend fun insertUsageEvent(event: UsageEventEntity) = usageEventDao.insert(event)
    fun getRecentUsageEvents(limit: Int = 100) = usageEventDao.getRecent(limit)
    suspend fun getUsageEventsByRange(start: Long, end: Long) = usageEventDao.getByTimeRange(start, end)
    fun getUsageEventCount() = usageEventDao.getTotalCount()
    suspend fun getTotalDwellSince(sinceMs: Long) = usageEventDao.getTotalDwellSince(sinceMs)
    suspend fun getDistinctAppsSince(sinceMs: Long) = usageEventDao.getDistinctAppsSince(sinceMs)

    // Interaction Events
    suspend fun insertInteractionEvent(event: InteractionEventEntity) = interactionEventDao.insert(event)
    fun getRecentInteractions(limit: Int = 100) = interactionEventDao.getRecent(limit)
    suspend fun getInteractionsByRange(start: Long, end: Long) = interactionEventDao.getByTimeRange(start, end)
    fun getInteractionCount() = interactionEventDao.getTotalCount()
    suspend fun getInteractionCountByType(type: String, sinceMs: Long) = interactionEventDao.getCountByTypeSince(type, sinceMs)

    // Sessions
    suspend fun insertSession(session: SessionEntity) = sessionDao.insert(session)
    suspend fun updateSession(session: SessionEntity) = sessionDao.update(session)
    suspend fun getActiveSession() = sessionDao.getActiveSession()
    fun getRecentSessions(limit: Int = 50) = sessionDao.getRecent(limit)
    suspend fun getSessionsByRange(start: Long, end: Long) = sessionDao.getByTimeRange(start, end)
    fun getSessionCount() = sessionDao.getTotalCount()
    suspend fun getTotalSessionDuration(sinceMs: Long) = sessionDao.getTotalDurationSince(sinceMs)

    // Self Reports
    suspend fun insertSelfReport(report: SelfReportEntity) = selfReportDao.insert(report)
    fun getRecentSelfReports(limit: Int = 50) = selfReportDao.getRecent(limit)
    suspend fun getSelfReportsByRange(start: Long, end: Long) = selfReportDao.getByTimeRange(start, end)

    // Reaction Tests
    suspend fun insertReactionTest(test: ReactionTestEntity) = reactionTestDao.insert(test)
    fun getRecentReactionTests(limit: Int = 50) = reactionTestDao.getRecent(limit)
    suspend fun getAvgReactionTime(sinceMs: Long) = reactionTestDao.getAvgReactionTimeSince(sinceMs)
    suspend fun getReactionTestsByRange(start: Long, end: Long) = reactionTestDao.getByTimeRange(start, end)

    // Clear all data
    suspend fun clearAllData() {
        usageEventDao.deleteAll()
        interactionEventDao.deleteAll()
        sessionDao.deleteAll()
        selfReportDao.deleteAll()
        reactionTestDao.deleteAll()
    }
}
