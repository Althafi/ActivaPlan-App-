package com.activaplan.app.domain.usecase

import com.activaplan.app.domain.model.Exercise
import com.activaplan.app.domain.model.ExploreQuery

/**
 * Filter Jelajah: mode, kategori, level, dan teks digabung AND.
 * Teks dicari (tanpa membedakan huruf besar/kecil) di nama dan `primaryMuscles`, yang sudah dalam bahasa tampilan.
 */
class FilterExercisesUseCase {

    operator fun invoke(exercises: List<Exercise>, query: ExploreQuery): List<Exercise> {
        val text = query.text.trim()
        return exercises.filter { e ->
            e.mode == query.mode &&
                (query.category == null || e.category == query.category) &&
                (query.difficulty == null || e.difficulty == query.difficulty) &&
                (text.isEmpty() || e.matches(text))
        }
    }

    private fun Exercise.matches(text: String): Boolean =
        name.contains(text, ignoreCase = true) || primaryMuscles.any { it.contains(text, ignoreCase = true) }
}
