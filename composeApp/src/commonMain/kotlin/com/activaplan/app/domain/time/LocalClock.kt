package com.activaplan.app.domain.time

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/** Tanggal dan jam lokal saat ini; diganti di test. */
fun interface LocalClock {
    fun now(): LocalDateTime
}

object SystemLocalClock : LocalClock {
    override fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
}

/** Menit sejak 00:00, sama dengan `ScheduleEntry.timeMinutes`. */
val LocalDateTime.minutesOfDay: Int get() = hour * 60 + minute

/** Jam seperti di Figma: 990 menjadi "16.30". */
fun formatClock(minutesOfDay: Int): String =
    "${(minutesOfDay / 60).toString().padStart(2, '0')}.${(minutesOfDay % 60).toString().padStart(2, '0')}"
