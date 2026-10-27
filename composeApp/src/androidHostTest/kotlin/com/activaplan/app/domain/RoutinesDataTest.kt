package com.activaplan.app.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.activaplan.app.data.InMemoryPreferencesDataStore
import com.activaplan.app.data.local.UserDataStore
import com.activaplan.app.data.repository.ExerciseDataSource
import com.activaplan.app.data.repository.ExerciseRepository
import com.activaplan.app.data.repository.UserDataRepository
import com.activaplan.app.domain.i18n.Language
import com.activaplan.app.domain.model.DraftLoad
import com.activaplan.app.domain.model.Routine
import com.activaplan.app.domain.model.RoutineDraft
import com.activaplan.app.domain.model.RoutineItem
import com.activaplan.app.domain.model.RoutineRules
import com.activaplan.app.domain.model.ScheduleEntry
import com.activaplan.app.domain.usecase.CreateRoutineFromTemplateUseCase
import com.activaplan.app.domain.usecase.DeleteRoutineUseCase
import com.activaplan.app.domain.usecase.EstimateRoutineMinutesUseCase
import com.activaplan.app.domain.usecase.LoadRoutineDraftUseCase
import com.activaplan.app.domain.usecase.ObserveRoutinesUseCase
import com.activaplan.app.domain.usecase.SaveRoutineUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import java.io.File
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Kriteria selesai Rutinitas, diuji terhadap salinan JSON di `composeResources/files`. */
class RoutinesDataTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val resources = File("src/commonMain/composeResources/files")
    private val catalogSource = ExerciseDataSource { name -> File(resources, name).readText() }
    private val estimate = EstimateRoutineMinutesUseCase()

    private class Session(file: File, json: Json, source: ExerciseDataSource) {
        private val job = Job()
        private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
            scope = CoroutineScope(Dispatchers.IO + job),
            produceFile = { file.absolutePath.toPath() },
        )
        val exercises = ExerciseRepository(source, json)
        val userData = UserDataRepository(UserDataStore(dataStore, json), exercises)

        suspend fun close() = job.cancelAndJoin()
    }

    @Test
    fun routineCrudSurvivesClosingAndReopening() = runBlocking {
        val file = File.createTempFile("activaplan-routines", ".preferences_pb").also { it.delete(); it.deleteOnExit() }
        var counter = 0
        val newId = { "routine_${++counter}" }

        val first = Session(file, json, catalogSource)
        val templateCopy = CreateRoutineFromTemplateUseCase(first.userData, first.exercises, newId)("tpl_board_beginner_full", Language.ID)!!
        val save = SaveRoutineUseCase(first.userData)
        val created = RoutineDraft("routine_baru", "Buatan sendiri")
            .add(RoutineItem("pb_chest_pushup", 3, 10, null, 60))!!
        save(created)
        // Ubah salinan template: ganti nama dan kurangi satu set pada item pertama.
        save(RoutineDraft.from(templateCopy).rename("Salinan Ubahan").adjustSets(0, -1))
        val doomed = Routine("hapus_saya", "Akan dihapus", listOf(RoutineItem("pb_chest_pushup", 3, 10, null, 60)))
        first.userData.saveRoutine(doomed)
        first.userData.saveScheduleEntry(ScheduleEntry("s1", 7, 990, "hapus_saya", reminder = true))
        DeleteRoutineUseCase(first.userData)("hapus_saya")
        first.close()

        val second = Session(file, json, catalogSource)
        try {
            val routines = second.userData.routines.first()
            assertEquals(setOf(templateCopy.id, "routine_baru"), routines.map { it.id }.toSet())
            val edited = routines.first { it.id == templateCopy.id }
            assertEquals("Salinan Ubahan", edited.name)
            assertEquals(templateCopy.items.first().sets - 1, edited.items.first().sets)
            assertEquals(emptyList(), second.userData.schedule.first(), "jadwal rutinitas yang dihapus ikut hilang")

            val reloaded = LoadRoutineDraftUseCase(second.userData, second.exercises)("routine_baru") as DraftLoad.Loaded
            assertEquals(created.toRoutine(), reloaded.draft.toRoutine())
            val summaries = ObserveRoutinesUseCase(second.userData, second.exercises)().first().routines
            assertEquals(2, summaries.size)
        } finally {
            second.close()
        }
    }

    @Test
    fun allTemplatesAreOfferedAndEachCopyIsIndependent() = runTest {
        val exercises = ExerciseRepository(catalogSource, json)
        val templates = exercises.templates(Language.ID)
        val offered = ObserveRoutinesUseCase(
            UserDataRepository(UserDataStore(InMemoryPreferencesDataStore(), json), exercises),
            exercises,
        )().first().templates
        assertEquals(templates.map { it.id }, offered.map { it.id }, "semua template punya latihan yang dikenal")

        val userData = UserDataRepository(UserDataStore(InMemoryPreferencesDataStore(), json), exercises)
        var counter = 0
        val copy = CreateRoutineFromTemplateUseCase(userData, exercises) { "routine_${++counter}" }
        templates.forEach { template ->
            val a = copy(template.id, Language.ID)!!
            val b = copy(template.id, Language.ID)!!
            assertTrue(a.id != b.id, template.id)
            assertEquals(template.items, a.items.map { it.exerciseId }, template.id)
            assertEquals(a.items, b.items, template.id)
        }
    }

    @Test
    fun catalogDefaultsFitTheBuilderRanges() = runTest {
        ExerciseRepository(catalogSource, json).catalog().exercises.forEach { e ->
            assertTrue(e.sets in RoutineRules.SETS, "${e.id} sets")
            e.reps?.let { assertTrue(it in RoutineRules.REPS, "${e.id} reps") }
            e.durationSeconds?.let {
                assertTrue(it in RoutineRules.SECONDS && it % RoutineRules.SECONDS_STEP == 0, "${e.id} detik")
            }
            assertTrue(e.restSeconds in RoutineRules.REST_SECONDS, "${e.id} istirahat")
        }
    }

    /**
     * Sesi nyata menurut `05-session.md`: tiap set = kerja lalu istirahat, kecuali istirahat setelah set terakhir
     * latihan terakhir. Estimasi harus dalam 15% dari itu untuk setiap template.
     */
    @Test
    fun estimateStaysWithinFifteenPercentOfASimulatedSession() = runTest {
        val exercises = ExerciseRepository(catalogSource, json)
        val byId = exercises.catalog().exercises.associateBy { it.id }
        exercises.templates(Language.ID).forEach { template ->
            val items = template.items.map { byId.getValue(it) }.map {
                RoutineItem(it.id, it.sets, it.reps, it.durationSeconds, it.restSeconds)
            }
            val sessionSeconds = items.sumOf { it.sets * (work(it) + it.restSeconds) } - items.last().restSeconds
            val estimated = estimate(items)
            val actual = sessionSeconds / 60.0
            val difference = abs(estimated - actual) / actual
            assertTrue(difference < 0.15, "${template.id}: estimasi $estimated mnt vs sesi %.1f mnt (%.0f%%)".format(actual, difference * 100))
        }
    }

    private fun work(item: RoutineItem) = item.durationSeconds ?: ((item.reps ?: 0) * EstimateRoutineMinutesUseCase.SECONDS_PER_REP)
}
