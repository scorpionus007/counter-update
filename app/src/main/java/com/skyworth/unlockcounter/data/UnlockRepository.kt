package com.skyworth.unlockcounter.data

import com.skyworth.unlockcounter.data.db.DailyUnlockDao
import com.skyworth.unlockcounter.data.db.DailyUnlockEntity
import com.skyworth.unlockcounter.domain.DailyUnlock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class UnlockRepository(
    private val dao: DailyUnlockDao,
    private val usageStats: UsageStatsDataSource,
    private val activeUserStore: ActiveUserStore,
    private val cloudSync: CloudSyncRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun observeHistory(userId: Long, limit: Int = 30): Flow<List<DailyUnlock>> =
        dao.observeForUser(userId, limit).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun refresh(): RefreshResult {
        val userId = activeUserStore.getActiveUserId()
            ?: throw IllegalStateException("No active user")

        val dates = datesToRefresh()
        val now = System.currentTimeMillis()

        for (date in dates) {
            val count = countForDate(date, now)
            dao.upsert(
                DailyUnlockEntity(
                    userId = userId,
                    date = date.format(dateFormatter),
                    count = count,
                    lastUpdatedAt = now
                )
            )
        }

        val today = LocalDate.now(zoneId)
        val todayEntity = dao.getByUserAndDate(userId, today.format(dateFormatter))
        cloudSync.syncLocalUser(userId) // best-effort; Result ignored if offline
        return RefreshResult(
            todayCount = todayEntity?.count ?: 0,
            daysRefreshed = dates.size
        )
    }

    suspend fun backfillIfNeeded(userId: Long) {
        if (dao.rowCountForUser(userId) == 0) {
            backfillLast7Days(userId)
        }
    }

    suspend fun backfillLast7Days(userId: Long) {
        val today = LocalDate.now(zoneId)
        val now = System.currentTimeMillis()

        for (daysAgo in 6 downTo 0) {
            val date = today.minusDays(daysAgo.toLong())
            val count = countForDate(date, now)
            dao.upsert(
                DailyUnlockEntity(
                    userId = userId,
                    date = date.format(dateFormatter),
                    count = count,
                    lastUpdatedAt = now
                )
            )
        }
    }

    private fun datesToRefresh(): List<LocalDate> {
        val today = LocalDate.now(zoneId)
        val now = Instant.now().atZone(zoneId)
        val minutesSinceMidnight = now.hour * 60 + now.minute

        return if (minutesSinceMidnight < 120) {
            listOf(today.minusDays(1), today)
        } else {
            listOf(today)
        }
    }

    private fun countForDate(date: LocalDate, nowMillis: Long): Int {
        val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val rangeEnd = minOf(endOfDay, nowMillis)
        return usageStats.countUnlocksBetween(startOfDay, rangeEnd)
    }

    private fun DailyUnlockEntity.toDomain(): DailyUnlock =
        DailyUnlock(
            date = LocalDate.parse(date, dateFormatter),
            count = count,
            lastUpdatedAt = Instant.ofEpochMilli(lastUpdatedAt)
        )

    data class RefreshResult(
        val todayCount: Int,
        val daysRefreshed: Int
    )
}
