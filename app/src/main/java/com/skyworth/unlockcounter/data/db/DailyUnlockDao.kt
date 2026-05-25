package com.skyworth.unlockcounter.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyUnlockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyUnlockEntity)

    @Query("SELECT * FROM daily_unlocks WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    fun observeForUser(userId: Long, limit: Int = 30): Flow<List<DailyUnlockEntity>>

    @Query("SELECT * FROM daily_unlocks WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getByUserAndDate(userId: Long, date: String): DailyUnlockEntity?

    @Query("SELECT COUNT(*) FROM daily_unlocks WHERE userId = :userId")
    suspend fun rowCountForUser(userId: Long): Int

    @Query("SELECT * FROM daily_unlocks WHERE userId = :userId ORDER BY date DESC")
    suspend fun getAllForUser(userId: Long): List<DailyUnlockEntity>

    @Query(
        """
        SELECT COALESCE(SUM(count), 0) FROM daily_unlocks
        WHERE userId = :userId AND date >= :fromDate
        """
    )
    suspend fun sumSince(userId: Long, fromDate: String): Int

    @Query(
        """
        SELECT COALESCE(SUM(count), 0) FROM daily_unlocks
        WHERE userId = :userId
        """
    )
    suspend fun totalForUser(userId: Long): Int
}
