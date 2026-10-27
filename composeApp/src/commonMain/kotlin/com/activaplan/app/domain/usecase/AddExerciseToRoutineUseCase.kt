package com.activaplan.app.domain.usecase

import com.activaplan.app.data.repository.UserDataRepository
import com.activaplan.app.domain.model.AddToRoutineResult
import com.activaplan.app.domain.model.Exercise
import com.activaplan.app.domain.model.Routine
import com.activaplan.app.domain.model.RoutineItem
import com.activaplan.app.domain.model.RoutineRules
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

internal fun randomRoutineId(): String = "routine_${Random.nextLong().toULong().toString(36)}"

/** Nilai awal item rutinitas: set, rep (atau detik), dan istirahat bawaan latihan. */
internal fun Exercise.toRoutineItem() = RoutineItem(
    exerciseId = id,
    sets = sets,
    reps = reps,
    durationSeconds = durationSeconds,
    restSeconds = restSeconds,
)

/** Menambah latihan ke rutinitas dengan nilai default set, rep (atau detik), dan istirahat dari latihan itu. */
class AddExerciseToRoutineUseCase(
    private val userData: UserDataRepository,
    private val newId: () -> String = ::randomRoutineId,
) {

    /** Rutinitas milik pengguna, untuk daftar pilihan. */
    val routines: Flow<List<Routine>> get() = userData.routines

    suspend fun toExisting(routineId: String, exercise: Exercise): AddToRoutineResult =
        userData.appendRoutineItem(routineId, exercise.toRoutineItem(), RoutineRules.MAX_ITEMS)

    /** Membuat rutinitas baru bernama [name] (1-40 karakter setelah dirapikan) berisi [exercise]. */
    suspend fun toNew(name: String, exercise: Exercise): AddToRoutineResult {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed.length > RoutineRules.MAX_NAME_LENGTH) return AddToRoutineResult.InvalidName
        userData.saveRoutine(Routine(newId(), trimmed, listOf(exercise.toRoutineItem())))
        return AddToRoutineResult.Added(trimmed)
    }
}
