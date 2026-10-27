package com.activaplan.app.domain.usecase

import com.activaplan.app.domain.model.Mode
import com.activaplan.app.domain.model.Routine
import com.activaplan.app.domain.model.ScheduleEntry
import com.activaplan.app.domain.model.TodayWorkout
import com.activaplan.app.domain.time.minutesOfDay
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.isoDayNumber

/**
 * Jadwal hari ini yang ditampilkan di Beranda: yang berikutnya (belum lewat), atau yang terakhir bila semua sudah lewat.
 * Jadwal yang rutinitasnya tidak ada atau tanpa latihan dilewati karena tidak bisa dimulai. Null bila tidak ada jadwal hari ini.
 */
class GetTodayWorkoutUseCase(
    private val estimateMinutes: EstimateRoutineMinutesUseCase = EstimateRoutineMinutesUseCase(),
) {

    operator fun invoke(
        schedule: List<ScheduleEntry>,
        routines: List<Routine>,
        exerciseModes: Map<String, Mode>,
        now: LocalDateTime,
    ): TodayWorkout? {
        val routineById = routines.filter { it.items.isNotEmpty() }.associateBy { it.id }
        val today = schedule
            .filter { it.dayOfWeek == now.dayOfWeek.isoDayNumber }
            .sortedWith(compareBy({ it.timeMinutes }, { it.id }))
            .mapNotNull { entry -> routineById[entry.routineId]?.let { entry to it } }
        if (today.isEmpty()) return null

        val upcoming = today.firstOrNull { (entry, _) -> entry.timeMinutes >= now.minutesOfDay }
        val (entry, routine) = upcoming ?: today.last()
        return TodayWorkout(
            routineId = routine.id,
            routineName = routine.name,
            timeMinutes = entry.timeMinutes,
            exerciseCount = routine.items.size,
            estimatedMinutes = estimateMinutes(routine),
            passed = upcoming == null,
            requiresSafetyCheck = routine.items.any { exerciseModes[it.exerciseId] == Mode.BOARD_ROPE },
        )
    }
}
