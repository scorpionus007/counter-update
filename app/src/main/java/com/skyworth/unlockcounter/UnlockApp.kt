package com.skyworth.unlockcounter

import android.app.Application
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.skyworth.unlockcounter.data.ActiveUserStore
import com.skyworth.unlockcounter.data.CloudSyncRepository
import com.skyworth.unlockcounter.data.UnlockRepository
import com.skyworth.unlockcounter.data.UsageStatsDataSource
import com.skyworth.unlockcounter.data.UserRepository
import com.skyworth.unlockcounter.data.db.AppDatabase
import com.skyworth.unlockcounter.work.UnlockSyncWorker
import java.util.concurrent.TimeUnit

class UnlockApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: UnlockRepository
        private set

    lateinit var userRepository: UserRepository
        private set

    lateinit var activeUserStore: ActiveUserStore
        private set

    lateinit var cloudSync: CloudSyncRepository
        private set

    override fun onCreate() {
        super.onCreate()
        activeUserStore = ActiveUserStore(applicationContext)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "unlock_counter.db"
        )
            .fallbackToDestructiveMigration()
            .build()

        val usageStats = UsageStatsDataSource(applicationContext)
        cloudSync = CloudSyncRepository(
            context = applicationContext,
            userDao = database.userDao(),
            unlockDao = database.dailyUnlockDao()
        )
        repository = UnlockRepository(
            dao = database.dailyUnlockDao(),
            usageStats = usageStats,
            activeUserStore = activeUserStore,
            cloudSync = cloudSync
        )
        userRepository = UserRepository(
            userDao = database.userDao(),
            unlockDao = database.dailyUnlockDao(),
            activeUserStore = activeUserStore,
            cloudSync = cloudSync
        )

        schedulePeriodicSync()
    }

    private fun schedulePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<UnlockSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.NONE)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            UnlockSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
