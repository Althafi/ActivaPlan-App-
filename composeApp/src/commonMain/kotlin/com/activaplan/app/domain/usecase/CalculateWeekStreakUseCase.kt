package com.activaplan.app.domain.usecase

import com.activaplan.app.domain.model.ScheduleEntry
import com.activaplan.app.domain.model.SessionLog
import com.activaplan.app.domain.model.SessionStatus
import com.activaplan.app.domain.model.StreakDay
import com.activaplan.app.domain.model.WeekStreak
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.previousOrSame

/**
 * Lingkaran streak pekan ini (Senin-Minggu) dan hitungan streak.
 *
 * Streak = jumlah hari terjadwal berturut-turut yang punya sesi COMPLETED, dihitung mundur dari hari ini
 * dan boleh melewati batas pekan. Hari tanpa jadwal tidak memutus; hari terjadwal yang terlewat memutus.
 * Hari ini yang belum selesai tidak memutus, karena masih bisa dikerjakan.
 */
class CalculateWeekStreakUseCase {

    operator fun invoke(schedule: List<ScheduleEntry>, logs: List<SessionLog>, today: LocalDate): WeekStreak {
        val completedDates = logs.filter { it.status == SessionStatus.COMPLETED }.map { it.date }.toSet()

        val monday = today.previousOrSame(DayOfWeek.MONDAY)
        val days = (0..6).map { offset ->
            val date = monday.plus(offset, DateTimeUnit.DAY)
            StreakDay(date.dayOfWeek.isoDayNumber, date.toString() in completedDates, date == today)
        }
        return WeekStreak(days, streak(schedule.map { it.dayOfWeek }.toSet(), completedDates, today))
    }

    private fun streak(scheduledDays: Set<Int>, completedDates: Set<String>, today: LocalDate): Int {
        if (scheduledDays.isEmpty()) return 0
        var streak = 0
        var date = today
        // Berakhir: hari terjadwal tanpa log pasti ditemukan sebelum log paling awal.
        while (true) {
            if (date.dayOfWeek.isoDayNumber in scheduledDays) {
                if (date.toString() in completedDates) streak++ else if (date != today) break
            }
            date = date.minus(1, DateTimeUnit.DAY)
        }
        return streak
    }
}
