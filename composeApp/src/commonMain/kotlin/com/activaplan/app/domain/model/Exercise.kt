package com.activaplan.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseImage(
    val file: String,
    val alt: String,
    val license: ImageLicense,
    val author: String? = null,
    val sourceUrl: String? = null,
)

@Serializable
data class Exercise(
    val id: String,
    val name: String,
    val mode: Mode,
    val category: Category,
    val difficulty: Difficulty,
    val boardZone: BoardZone? = null,
    val primaryMuscles: List<String>,
    val sets: Int,
    val reps: Int? = null,
    val durationSeconds: Int? = null,
    val restSeconds: Int,
    val steps: List<String>,
    val tips: List<String>,
    val commonMistakes: List<String>,
    val progressionId: String? = null,
    val regressionId: String? = null,
    val images: List<ExerciseImage> = emptyList(),
)

@Serializable
data class Template(
    val id: String,
    val name: String,
    val difficulty: Difficulty,
    val items: List<String>,
)
