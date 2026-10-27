package com.activaplan.app.domain.schedule

import com.activaplan.app.domain.model.ScheduleEntry
import kotlin.math.abs

/**
 * Bentrok jadwal: dua entri di hari yang sama bentrok bila jam mulainya sama, atau selisihnya kurang dari
 * estimasi durasi rutinitas entri yang lebih awal (latihan pertama belum selesai saat yang kedua dimulai).
 */
object ScheduleConflicts {

    /** [minutesOf] mengembalikan estimasi menit rutinitas; 0 bila rutinitas tidak dikenal. */
    fun overlaps(a: ScheduleEntry, b: ScheduleEntry, minutesOf: (routineId: String) -> Int): Boolean {
        if (a.dayOfWeek != b.dayOfWeek) return false
        val gap = abs(a.timeMinutes - b.timeMinutes)
        if (gap == 0) return true
        val first = if (a.timeMinutes < b.timeMinutes) a else b
        return gap < minutesOf(first.routineId)
    }

    /** Entri di [existing] yang bentrok dengan salah satu [candidates]. */
    fun find(
        candidates: List<ScheduleEntry>,
        existing: List<ScheduleEntry>,
        minutesOf: (routineId: String) -> Int,
    ): List<ScheduleEntry> =
        existing.filter { other -> candidates.any { overlaps(it, other, minutesOf) } }
}
