package com.skyworth.unlockcounter.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val PERMISSION = "permission"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val USERS = "users"
    const val USER_DETAIL = "user_detail/{userId}"
    const val CLOUD_USER_DETAIL = "cloud_user/{cloudUserId}"

    fun userDetail(userId: Long) = "user_detail/$userId"
    fun cloudUserDetail(cloudUserId: String) = "cloud_user/$cloudUserId"
}
