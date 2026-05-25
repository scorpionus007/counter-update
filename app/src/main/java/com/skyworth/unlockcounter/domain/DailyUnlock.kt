package com.skyworth.unlockcounter.domain

import java.time.Instant
import java.time.LocalDate

data class DailyUnlock(
    val date: LocalDate,
    val count: Int,
    val lastUpdatedAt: Instant
)
