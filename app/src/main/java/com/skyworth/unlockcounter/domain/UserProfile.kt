package com.skyworth.unlockcounter.domain

import java.time.Instant

data class UserProfile(
    val id: Long,
    val name: String,
    val phone: String,
    val cloudUserId: String? = null,
    val createdAt: Instant
)

data class UserSummary(
    val profile: UserProfile,
    val todayCount: Int,
    val last7DaysTotal: Int,
    val allTimeTotal: Int,
    val isActive: Boolean
)
