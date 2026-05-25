package com.skyworth.unlockcounter.data.remote

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val name: String,
    val phone: String,
  @SerializedName("androidDeviceId") val androidDeviceId: String
)

data class RegisterResponse(
    @SerializedName("userId") val userId: String,
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("accessToken") val accessToken: String,
    val name: String,
    val phone: String
)

data class DailySummaryPayload(
    val date: String,
    @SerializedName("unlockCount") val unlockCount: Int,
    @SerializedName("payableAmountP") val payableAmountP: Int = 0,
    val capped: Boolean = false
)

data class SyncUnlocksRequest(
    @SerializedName("dailySummaries") val dailySummaries: List<DailySummaryPayload>
)

data class SyncUnlocksResponse(
    val synced: Int,
    val events: Int
)

data class CloudUserSummary(
    @SerializedName("userId") val userId: String,
    val name: String,
    val phone: String,
    @SerializedName("kycStatus") val kycStatus: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("todayCount") val todayCount: Int,
    @SerializedName("last7DaysTotal") val last7DaysTotal: Int,
    @SerializedName("totalEvents") val totalEvents: Int,
    @SerializedName("recentDays") val recentDays: List<CloudDaySummary>
)

data class CloudDaySummary(
    val date: String,
    @SerializedName("unlockCount") val unlockCount: Int
)

data class CloudUserHistoryResponse(
    val user: CloudUserInfo?,
    val history: List<CloudHistoryDay>
)

data class CloudUserInfo(
    val id: String,
    val name: String,
    val phone: String
)

data class CloudHistoryDay(
    val date: String,
    @SerializedName("unlockCount") val unlockCount: Int,
    @SerializedName("payableAmountP") val payableAmountP: Int
)
