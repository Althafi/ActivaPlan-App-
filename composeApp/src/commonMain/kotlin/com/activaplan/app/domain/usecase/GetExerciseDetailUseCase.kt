package com.activaplan.app.domain.usecase

import com.activaplan.app.data.repository.ExerciseRepository
import com.activaplan.app.domain.i18n.Language
import com.activaplan.app.domain.model.ExerciseDetail
import com.activaplan.app.domain.model.Mode

/** Memuat detail latihan; null bila [id] tidak ada di katalog. Melempar bila katalog gagal dimuat. */
class GetExerciseDetailUseCase(private val exercises: ExerciseRepository) {

    suspend operator fun invoke(id: String, language: Language): ExerciseDetail? {
        val exercise = exercises.exercise(id, language) ?: return null
        val known = exercises.catalog().exercises.mapTo(mutableSetOf()) { it.id }
        return ExerciseDetail(
            exercise = exercise,
            regressionId = exercise.regressionId?.takeIf { it in known },
            progressionId = exercise.progressionId?.takeIf { it in known },
            safetyNotes = if (exercise.mode == Mode.BOARD_ROPE) exercises.safetyNotes(language) else emptyList(),
        )
    }
}
