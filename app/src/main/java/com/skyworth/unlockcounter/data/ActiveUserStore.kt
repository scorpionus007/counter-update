package com.skyworth.unlockcounter.data

import android.content.Context

class ActiveUserStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getActiveUserId(): Long? {
        val id = prefs.getLong(KEY_ACTIVE_USER_ID, NO_USER)
        return if (id == NO_USER) null else id
    }

    fun setActiveUserId(userId: Long) {
        prefs.edit().putLong(KEY_ACTIVE_USER_ID, userId).apply()
    }

    fun clearActiveUser() {
        prefs.edit().remove(KEY_ACTIVE_USER_ID).apply()
    }

    companion object {
        private const val PREFS_NAME = "unlock_counter_prefs"
        private const val KEY_ACTIVE_USER_ID = "active_user_id"
        private const val NO_USER = -1L
    }
}
