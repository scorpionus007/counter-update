package com.skyworth.unlockcounter.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, DailyUnlockEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyUnlockDao(): DailyUnlockDao
    abstract fun userDao(): UserDao
}
