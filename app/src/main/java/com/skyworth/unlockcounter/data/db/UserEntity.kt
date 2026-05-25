package com.skyworth.unlockcounter.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val cloudUserId: String? = null,
    val cloudDeviceId: String? = null,
    val accessToken: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
