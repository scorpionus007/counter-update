package com.skyworth.unlockcounter.data

import com.skyworth.unlockcounter.data.db.UserDao
import com.skyworth.unlockcounter.data.db.UserEntity
import com.skyworth.unlockcounter.data.db.DailyUnlockDao
import com.skyworth.unlockcounter.data.remote.CloudUserSummary
import com.skyworth.unlockcounter.domain.UserProfile
import com.skyworth.unlockcounter.domain.UserSummary
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class UserRepository(
    private val userDao: UserDao,
    private val unlockDao: DailyUnlockDao,
    private val activeUserStore: ActiveUserStore,
    private val cloudSync: CloudSyncRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun hasUsers(): Boolean = userDao.count() > 0

    suspend fun getActiveUserId(): Long? = activeUserStore.getActiveUserId()

    suspend fun getActiveUser(): UserProfile? {
        val id = activeUserStore.getActiveUserId() ?: return null
        return userDao.getById(id)?.toDomain()
    }

    suspend fun createUser(name: String, phone: String): Long {
        val trimmedName = name.trim()
        val trimmedPhone = phone.trim()
        require(trimmedName.length >= 2) { "Name too short" }
        require(trimmedPhone.matches(Regex("^[6-9]\\d{9}$"))) { "Invalid phone number" }

        val cloud = cloudSync.registerUser(trimmedName, trimmedPhone)
        val localId = userDao.insert(
            UserEntity(
                name = cloud.name,
                phone = cloud.phone,
                cloudUserId = cloud.cloudUserId,
                cloudDeviceId = cloud.cloudDeviceId,
                accessToken = cloud.accessToken
            )
        )
        activeUserStore.setActiveUserId(localId)
        return localId
    }

    suspend fun setActiveUser(userId: Long) {
        val user = userDao.getById(userId) ?: throw IllegalArgumentException("User not found")
        activeUserStore.setActiveUserId(user.id)
    }

    fun observeUserSummaries(): Flow<List<UserSummary>> = flow {
        userDao.observeAll().collect { users ->
            emit(buildSummaries(users))
        }
    }

    suspend fun fetchCloudUsers(): Result<List<CloudUserSummary>> =
        cloudSync.fetchAllCloudUsers()

    suspend fun getUserProfile(userId: Long): UserProfile? =
        userDao.getById(userId)?.toDomain()

    private suspend fun buildSummaries(users: List<UserEntity>): List<UserSummary> {
        val today = LocalDate.now(zoneId).format(dateFormatter)
        val weekAgo = LocalDate.now(zoneId).minusDays(6).format(dateFormatter)
        val activeId = activeUserStore.getActiveUserId()
        return users.map { user ->
            val todayEntity = unlockDao.getByUserAndDate(user.id, today)
            UserSummary(
                profile = user.toDomain(),
                todayCount = todayEntity?.count ?: 0,
                last7DaysTotal = unlockDao.sumSince(user.id, weekAgo),
                allTimeTotal = unlockDao.totalForUser(user.id),
                isActive = user.id == activeId
            )
        }
    }

    private fun UserEntity.toDomain(): UserProfile =
        UserProfile(
            id = id,
            name = name,
            phone = phone,
            cloudUserId = cloudUserId,
            createdAt = Instant.ofEpochMilli(createdAt)
        )
}
