package com.activaplan.app.domain.usecase

import com.activaplan.app.domain.model.Difficulty
import com.activaplan.app.domain.model.Exercise
import com.activaplan.app.domain.model.Routine
import com.activaplan.app.domain.model.SessionLog
import com.activaplan.app.domain.model.SessionStatus
import com.activaplan.app.domain.model.Template
import com.activaplan.app.domain.model.TemplateRecommendation

/**
 * Rekomendasi template: levelnya sama atau satu tingkat di atas rutinitas terakhir yang selesai
 * (level rutinitas = level tertinggi latihannya; default Mudah). Maksimal [LIMIT], dipilih bergantian
 * antara level yang sama dan level di atasnya supaya ada pilihan lanjut dan pilihan aman.
 */
class RecommendTemplatesUseCase {

    operator fun invoke(
        templates: List<Template>,
        routines: List<Routine>,
        logs: List<SessionLog>,
        exercises: Map<String, Exercise>,
    ): List<TemplateRecommendation> {
        val base = lastCompletedLevel(routines, logs, exercises) ?: Difficulty.EASY
        val next = Difficulty.entries.getOrNull(base.ordinal + 1)

        val same = templates.filter { it.difficulty == base }
        val above = templates.filter { it.difficulty == next }
        val picked = buildList {
            for (i in 0 until maxOf(same.size, above.size)) {
                same.getOrNull(i)?.let { add(it) }
                above.getOrNull(i)?.let { add(it) }
            }
        }.take(LIMIT)

        return picked.map { t ->
            val modes = t.items.mapNotNull { exercises[it]?.mode }
            TemplateRecommendation(
                id = t.id,
                name = t.name,
                difficulty = t.difficulty,
                exerciseCount = t.items.size,
                primaryMode = modes.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key,
            )
        }
    }

    /** Log COMPLETED terbaru yang rutinitasnya masih ada; urutan sama tanggal mengikuti urutan log. */
    private fun lastCompletedLevel(
        routines: List<Routine>,
        logs: List<SessionLog>,
        exercises: Map<String, Exercise>,
    ): Difficulty? {
        val routineById = routines.associateBy { it.id }
        val routine = logs.withIndex()
            .filter { it.value.status == SessionStatus.COMPLETED }
            .sortedWith(compareBy({ it.value.date }, { it.index }))
            .asReversed()
            .firstNotNullOfOrNull { routineById[it.value.routineId] }
        return routine?.items?.mapNotNull { exercises[it.exerciseId]?.difficulty }?.maxOrNull()
    }

    companion object {
        const val LIMIT = 2
    }
}
