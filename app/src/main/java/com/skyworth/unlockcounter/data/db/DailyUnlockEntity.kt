package com.skyworth.unlockcounter.data.db

import androidx.room.Entity

@Entity(
    tableName = "daily_unlocks",
    primaryKeys = ["userId", "date"]
)
data class DailyUnlockEntity(
    val userId: Long,
    val date: String,
    val count: Int,
    val lastUpdatedAt: Long
)
