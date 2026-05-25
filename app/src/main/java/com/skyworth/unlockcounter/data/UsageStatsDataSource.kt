package com.skyworth.unlockcounter.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

class UsageStatsDataSource(context: Context) {

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun countUnlocksBetween(fromMillis: Long, toMillis: Long): Int {
        if (toMillis <= fromMillis) return 0

        val events = usageStatsManager.queryEvents(fromMillis, toMillis)
        var count = 0
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.KEYGUARD_HIDDEN) {
                count++
            }
        }
        return count
    }
}
