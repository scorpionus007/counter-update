package com.skyworth.unlockcounter.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.skyworth.unlockcounter.UnlockApp
import com.skyworth.unlockcounter.util.UsageAccessChecker

class UnlockSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (!UsageAccessChecker.isGranted(applicationContext)) {
            return Result.success()
        }
        val app = applicationContext as UnlockApp
        return try {
            app.repository.refresh()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "unlock_sync"
    }
}
