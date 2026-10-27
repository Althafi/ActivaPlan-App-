package com.activaplan.app.presentation.navigation

import kotlinx.serialization.Serializable

/** Builder rutinitas; [routineId] null membuat rutinitas baru. */
@Serializable
data class RoutineBuilderRoute(val routineId: String? = null)

/** Jelajah dalam mode pilih; hasilnya dikirim ke Builder lewat [PICKED_EXERCISE_KEY]. */
@Serializable
data object ExercisePickerRoute

/** Pratinjau template; [id] adalah `Template.id`. */
@Serializable
data class TemplatePreviewRoute(val id: String)

/** Kunci `savedStateHandle` entri sebelumnya: ID latihan terpilih (String?) dari mode pilih. */
const val PICKED_EXERCISE_KEY = "picked_exercise"

/** Kunci `savedStateHandle` entri sebelumnya: true setelah Builder menyimpan, agar daftar menampilkan snackbar. */
const val ROUTINE_SAVED_KEY = "routine_saved"
