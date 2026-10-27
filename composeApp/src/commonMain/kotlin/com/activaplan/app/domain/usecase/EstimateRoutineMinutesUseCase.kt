package com.activaplan.app.domain.usecase

import com.activaplan.app.domain.model.Routine
import com.activaplan.app.domain.model.RoutineItem

/** Estimasi menit rutinitas: Σ set × (kerja + istirahat) / 60, kerja = rep × 4 detik atau `durationSeconds`. */
class EstimateRoutineMinutesUseCase {

    operator fun invoke(routine: Routine): Int = invoke(routine.items)

    operator fun invoke(items: List<RoutineItem>): Int {
        val seconds = items.sumOf { it.sets * (workSeconds(it) + it.restSeconds) }
        return if (seconds <= 0) 0 else maxOf(1, (seconds + 30) / 60)
    }

    private fun workSeconds(item: RoutineItem): Int = item.durationSeconds ?: ((item.reps ?: 0) * SECONDS_PER_REP)

    companion object {
        const val SECONDS_PER_REP = 4
    }
}
