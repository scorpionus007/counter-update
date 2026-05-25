package com.skyworth.unlockcounter.data

import android.content.Context
import android.provider.Settings
import com.skyworth.unlockcounter.BuildConfig
import com.skyworth.unlockcounter.data.db.DailyUnlockDao
import com.skyworth.unlockcounter.data.db.UserDao
import com.skyworth.unlockcounter.data.remote.ApiClient
import com.skyworth.unlockcounter.data.remote.CloudUserSummary
import com.skyworth.unlockcounter.data.remote.DailySummaryPayload
import com.skyworth.unlockcounter.data.remote.RegisterRequest
import com.skyworth.unlockcounter.data.remote.SyncUnlocksRequest

class CloudSyncRepository(
    private val context: Context,
    private val userDao: UserDao,
    private val unlockDao: DailyUnlockDao
) {

    private val api = ApiClient.service

    fun androidDeviceId(): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown-device"

    suspend fun registerUser(name: String, phone: String): RegisterResult {
        val response = api.register(
            RegisterRequest(
                name = name.trim(),
                phone = phone.trim(),
                androidDeviceId = androidDeviceId()
            )
        )
        return RegisterResult(
            cloudUserId = response.userId,
            cloudDeviceId = response.deviceId,
            accessToken = response.accessToken,
            name = response.name,
            phone = response.phone
        )
    }

    suspend fun syncLocalUser(localUserId: Long): Result<Int> {
        val user = userDao.getById(localUserId) ?: return Result.failure(IllegalStateException("User not found"))
        val token = user.accessToken ?: return Result.failure(IllegalStateException("Not registered with cloud"))

        val rows = unlockDao.getAllForUser(localUserId)
        return try {
            val payload = rows.map {
                DailySummaryPayload(
                    date = it.date,
                    unlockCount = it.count
                )
            }
            val response = api.syncUnlocks(
                authorization = "Bearer $token",
                body = SyncUnlocksRequest(dailySummaries = payload)
            )
            Result.success(response.synced)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchAllCloudUsers(): Result<List<CloudUserSummary>> = try {
        Result.success(api.listAllUsers(BuildConfig.ADMIN_API_KEY))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun fetchCloudUserHistory(cloudUserId: String) =
        api.getUserHistory(BuildConfig.ADMIN_API_KEY, cloudUserId)

    data class RegisterResult(
        val cloudUserId: String,
        val cloudDeviceId: String,
        val accessToken: String,
        val name: String,
        val phone: String
    )
}
