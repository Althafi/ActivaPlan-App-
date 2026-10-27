package com.activaplan.app.presentation.navigation

import kotlinx.serialization.Serializable

/** Rute detail latihan; [id] adalah `Exercise.id`. */
@Serializable
data class ExerciseDetailRoute(val id: String)
