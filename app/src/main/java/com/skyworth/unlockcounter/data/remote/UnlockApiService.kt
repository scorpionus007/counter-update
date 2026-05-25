package com.skyworth.unlockcounter.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface UnlockApiService {

    @POST("v1/auth/register")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse

    @POST("v1/unlocks/sync")
    suspend fun syncUnlocks(
        @Header("Authorization") authorization: String,
        @Body body: SyncUnlocksRequest
    ): SyncUnlocksResponse

    @GET("v1/admin/users")
    suspend fun listAllUsers(
        @Header("X-Admin-Key") adminKey: String
    ): List<CloudUserSummary>

    @GET("v1/admin/users/{userId}/history")
    suspend fun getUserHistory(
        @Header("X-Admin-Key") adminKey: String,
        @retrofit2.http.Path("userId") userId: String
    ): CloudUserHistoryResponse
}
