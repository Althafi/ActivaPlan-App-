package com.activaplan.app.domain

import com.activaplan.app.data.InMemoryPreferencesDataStore
import com.activaplan.app.data.local.UserDataStore
import com.activaplan.app.data.repository.ExerciseDataSource
import com.activaplan.app.data.repository.ExerciseFiles
import com.activaplan.app.data.repository.ExerciseRepository
import com.activaplan.app.data.repository.UserDataRepository
import com.activaplan.app.domain.i18n.Language
import com.activaplan.app.domain.i18n.Strings
import com.activaplan.app.domain.model.Difficulty
import com.activaplan.app.domain.model.DraftLoad
import com.activaplan.app.domain.model.Routine
import com.activaplan.app.domain.model.RoutineDraft
import com.activaplan.app.domain.model.RoutineItem
import com.activaplan.app.domain.model.RoutineRules
import com.activaplan.app.domain.model.SaveRoutineResult
import com.activaplan.app.domain.model.ScheduleEntry
import com.activaplan.app.domain.model.ScheduleSlot
import com.activaplan.app.domain.usecase.CreateRoutineFromTemplateUseCase
import com.activaplan.app.domain.usecase.DeleteRoutineUseCase
import com.activaplan.app.domain.usecase.EstimateRoutineMinutesUseCase
import com.activaplan.app.domain.usecase.GetTemplateDetailUseCase
import com.activaplan.app.domain.usecase.LoadRoutineDraftUseCase
import com.activaplan.app.domain.usecase.ObserveRoutinesUseCase
import com.activaplan.app.domain.usecase.SaveRoutineUseCase
import com.activaplan.app.presentation.routines.routineMeta
import com.activaplan.app.presentation.routines.scheduleText
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoutineUseCasesTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun exerciseJson(id: String, difficulty: String, reps: Int?, seconds: Int?, rest: Int = 60) = """
        {"id":"$id","name":"Latihan $id","mode":"NO_EQUIPMENT","category":"CHEST","difficulty":"$difficulty",
         "primaryMuscles":["chest"],"sets":3,"reps":$reps,"durationSeconds":$seconds,"restSeconds":$rest,
         "steps":[],"tips":[],"commonMistakes":[]}
    """.trimIndent()

    private val files = mapOf(
        ExerciseFiles.GENERAL to """{"schemaVersion":1,"exercises":[
            ${exerciseJson("a", "EASY", 10, null)},
            ${exerciseJson("b", "HARD", 8, null)},
            ${exerciseJson("t", "MEDIUM", null, 20, rest = 30)}],
            "templates":[
              {"id":"tpl_full","name":"Template Lengkap","difficulty":"EASY","items":["a","hilang","b","t"]},
              {"id":"tpl_kosong","name":"Template Kosong","difficulty":"EASY","items":["hilang"]},
              {"id":"tpl_panjang","name":"${"N".repeat(60)}","difficulty":"MEDIUM","items":["a"]}]}""",
        ExerciseFiles.PUSHUP_BOARD to """{"schemaVersion":1,"exercises":[],"templates":[]}""",
        ExerciseFiles.I18N to """{"schemaVersion":1,"baseLanguage":"id","muscles":[],"packs":{}}""",
    )

    private val exercises = ExerciseRepository(ExerciseDataSource { files.getValue(it) }, json)
    private val userData = UserDataRepository(UserDataStore(InMemoryPreferencesDataStore(), json), exercises)
    private val estimate = EstimateRoutineMinutesUseCase()

    private var idCounter = 0
    private val newId = { "routine_${++idCounter}" }

    private fun item(id: String = "a", sets: Int = 3, reps: Int? = 10, seconds: Int? = null, rest: Int = 60) =
        RoutineItem(id, sets, reps, seconds, rest)

    // region RoutineDraft

    private fun draftOf(count: Int) =
        (1..count).fold(RoutineDraft("r", "Nama")) { d, _ -> d.add(item())!! }

    @Test
    fun draftAcceptsUpToTheMaximumThenRejects() {
        val full = draftOf(RoutineRules.MAX_ITEMS)
        assertTrue(full.isFull)
        assertNull(full.add(item()))
        assertEquals(RoutineRules.MAX_ITEMS, full.items.size)
    }

    @Test
    fun sameExerciseCanBeAddedTwiceWithDistinctKeys() {
        val draft = draftOf(2)
        assertEquals(2, draft.items.map { it.key }.toSet().size)
        val removedFirst = draft.remove(draft.items.first().key)
        assertEquals(1, removedFirst.items.size)
        // Kunci tidak dipakai ulang setelah penghapusan.
        assertNotEquals(draft.items.first().key, removedFirst.add(item())!!.items.last().key)
    }

    @Test
    fun moveReordersAndIgnoresInvalidRequests() {
        val draft = listOf("a", "b", "t").fold(RoutineDraft("r", "Nama")) { d, id -> d.add(item(id))!! }
        fun ids(d: RoutineDraft) = d.items.map { it.item.exerciseId }

        assertEquals(listOf("b", "t", "a"), ids(draft.move(0, 2)))
        assertEquals(listOf("t", "a", "b"), ids(draft.move(2, 0)))
        assertEquals(ids(draft), ids(draft.move(1, 1)))
        assertEquals(ids(draft), ids(draft.move(-1, 1)))
        assertEquals(ids(draft), ids(draft.move(0, 3)))
    }

    @Test
    fun setsAndRepsStayInsideTheirRanges() {
        var draft = RoutineDraft("r", "Nama").add(item(sets = 3, reps = 10))!!
        val key = draft.items.single().key

        repeat(10) { draft = draft.adjustSets(key, 1) }
        assertEquals(RoutineRules.SETS.last, draft.items.single().item.sets)
        assertFalse(draft.items.single().canIncreaseSets)
        repeat(10) { draft = draft.adjustSets(key, -1) }
        assertEquals(RoutineRules.SETS.first, draft.items.single().item.sets)
        assertFalse(draft.items.single().canDecreaseSets)

        repeat(60) { draft = draft.adjustWork(key, 1) }
        assertEquals(RoutineRules.REPS.last, draft.items.single().item.reps)
        repeat(60) { draft = draft.adjustWork(key, -1) }
        assertEquals(RoutineRules.REPS.first, draft.items.single().item.reps)
    }

    @Test
    fun timedExercisesStepInFiveSecondsWithinRange() {
        var draft = RoutineDraft("r", "Nama").add(item("t", reps = null, seconds = 20))!!
        val key = draft.items.single().key

        draft = draft.adjustWork(key, 1)
        assertEquals(25, draft.items.single().item.durationSeconds)
        assertNull(draft.items.single().item.reps)

        repeat(100) { draft = draft.adjustWork(key, 1) }
        assertEquals(RoutineRules.SECONDS.last, draft.items.single().item.durationSeconds)
        repeat(100) { draft = draft.adjustWork(key, -1) }
        assertEquals(RoutineRules.SECONDS.first, draft.items.single().item.durationSeconds)
        assertFalse(draft.items.single().canDecreaseWork)
    }

    @Test
    fun nameRulesAndRoundTripToRoutine() {
        assertFalse(RoutineDraft("r", "   ").isNameValid)
        assertTrue(RoutineDraft("r", "x".repeat(RoutineRules.MAX_NAME_LENGTH)).isNameValid)
        assertFalse(RoutineDraft("r", "x".repeat(RoutineRules.MAX_NAME_LENGTH + 1)).isNameValid)

        val routine = Routine("r1", "Pagi", listOf(item("a"), item("t", reps = null, seconds = 20)))
        assertEquals(routine, RoutineDraft.from(routine).toRoutine())
        assertEquals("Pagi", RoutineDraft.from(routine).rename("  Pagi  ").toRoutine().name)
    }

    // endregion

    // region Estimate

    @Test
    fun estimateFollowsTheDocumentedFormula() {
        // 3 set × (10 rep × 4 dtk + 60 dtk) = 300 dtk = 5 menit.
        assertEquals(5, estimate(listOf(item())))
        // 3 set × (20 dtk + 30 dtk) = 150 dtk, dibulatkan ke 3 menit.
        assertEquals(3, estimate(listOf(item("t", reps = null, seconds = 20, rest = 30))))
        assertEquals(8, estimate(listOf(item(), item("t", reps = null, seconds = 20, rest = 30))))
        assertEquals(0, estimate(emptyList<RoutineItem>()))
        assertEquals(estimate(listOf(item())), estimate(Routine("r", "n", listOf(item()))))
    }

    @Test
    fun estimateIsNeverBelowOneMinuteForANonEmptyRoutine() {
        assertEquals(1, estimate(listOf(item(sets = 1, reps = 1, rest = 15))))
    }

    // endregion

    // region Load, save, delete

    @Test
    fun newRoutineStartsEmptyWithAFreshId() = runTest {
        val loaded = LoadRoutineDraftUseCase(userData, exercises, newId)(null) as DraftLoad.Loaded
        assertEquals("routine_1", loaded.draft.id)
        assertTrue(loaded.draft.items.isEmpty())
        assertEquals(loaded.draft.toRoutine(), loaded.baseline, "rutinitas baru yang belum diubah tidak dianggap berubah")
    }

    @Test
    fun existingRoutineLoadsAndSkipsMissingExercises() = runTest {
        userData.saveRoutine(Routine("r1", "Pagi", listOf(item("a"), item("hilang"), item("b"))))
        val load = LoadRoutineDraftUseCase(userData, exercises, newId)

        val loaded = load("r1") as DraftLoad.Loaded
        assertEquals(listOf("a", "b"), loaded.draft.items.map { it.item.exerciseId })
        assertEquals(1, loaded.skippedItems)
        assertEquals(DraftLoad.NotFound, load("tidak-ada"))
    }

    @Test
    fun saveRejectsInvalidNameOrItemCountAndPersistsValidDrafts() = runTest {
        val save = SaveRoutineUseCase(userData)
        val valid = draftOf(2)

        assertEquals(SaveRoutineResult.InvalidName, save(valid.rename("  ")))
        assertEquals(SaveRoutineResult.NoItems, save(RoutineDraft("r", "Nama")))
        assertEquals(emptyList(), userData.routines.first())

        assertEquals(SaveRoutineResult.Saved, save(valid.rename("  Pagi  ")))
        assertEquals(listOf(Routine("r", "Pagi", valid.items.map { it.item })), userData.routines.first())
    }

    @Test
    fun editingReplacesTheRoutineInsteadOfDuplicatingIt() = runTest {
        val save = SaveRoutineUseCase(userData)
        save(draftOf(1))
        save(draftOf(3).rename("Baru"))

        val saved = userData.routines.first().single()
        assertEquals("Baru", saved.name)
        assertEquals(3, saved.items.size)
    }

    @Test
    fun deletingARoutineAlsoDeletesItsSchedule() = runTest {
        userData.saveRoutine(Routine("r1", "A", listOf(item())))
        userData.saveRoutine(Routine("r2", "B", listOf(item())))
        userData.saveScheduleEntry(ScheduleEntry("s1", 1, 390, "r1", reminder = true))
        userData.saveScheduleEntry(ScheduleEntry("s2", 2, 390, "r2", reminder = false))

        DeleteRoutineUseCase(userData)("r1")

        assertEquals(listOf("r2"), userData.routines.first().map { it.id })
        assertEquals(listOf("s2"), userData.schedule.first().map { it.id })
    }

    // endregion

    // region Templates

    private val fromTemplate = CreateRoutineFromTemplateUseCase(userData, exercises, newId)

    @Test
    fun usingATemplateSavesACopyWithTheExercisesDefaults() = runTest {
        val copy = fromTemplate("tpl_full", Language.ID)!!

        assertEquals("Template Lengkap", copy.name)
        assertEquals(listOf("a", "b", "t"), copy.items.map { it.exerciseId }, "latihan yang hilang dilewati")
        assertEquals(item("t", sets = 3, reps = null, seconds = 20, rest = 30), copy.items.last())
        assertEquals(listOf(copy), userData.routines.first())
    }

    @Test
    fun templateCopiesAreIndependent() = runTest {
        val first = fromTemplate("tpl_full", Language.ID)!!
        val second = fromTemplate("tpl_full", Language.ID)!!
        assertNotEquals(first.id, second.id)

        val save = SaveRoutineUseCase(userData)
        val edited = RoutineDraft.from(first).rename("Ubahan").adjustSets(0, 2).remove(1)
        save(edited)

        val stored = userData.routines.first().associateBy { it.id }
        assertEquals("Ubahan", stored.getValue(first.id).name)
        assertEquals(2, stored.getValue(first.id).items.size)
        assertEquals(second, stored.getValue(second.id), "salinan lain tidak berubah")
        // Template di katalog tetap utuh: salinan baru berisi item awal lagi.
        assertEquals(3, fromTemplate("tpl_full", Language.ID)!!.items.size)
    }

    @Test
    fun unusableTemplatesGiveNothing() = runTest {
        assertNull(fromTemplate("tpl_kosong", Language.ID))
        assertNull(fromTemplate("tidak-ada", Language.ID))
        assertEquals(emptyList(), userData.routines.first())
    }

    @Test
    fun copiedTemplateNameIsCutToTheMaximumLength() = runTest {
        assertEquals(RoutineRules.MAX_NAME_LENGTH, fromTemplate("tpl_panjang", Language.ID)!!.name.length)
    }

    @Test
    fun templatePreviewListsKnownExercisesAndEstimate() = runTest {
        val detail = GetTemplateDetailUseCase(exercises, estimate)("tpl_full", Language.ID)!!

        assertEquals(listOf("a", "b", "t"), detail.exercises.map { it.id })
        assertEquals(Difficulty.EASY, detail.difficulty)
        // a: 3×(40+60)=300, b: 3×(32+60)=276, t: 3×(20+30)=150 → 726 dtk ≈ 12 menit.
        assertEquals(12, detail.estimatedMinutes)
        assertNull(GetTemplateDetailUseCase(exercises, estimate)("tpl_kosong", Language.ID))
    }

    // endregion

    // region ObserveRoutines

    @Test
    fun listSummarisesLevelEstimateAndSchedule() = runTest {
        userData.saveRoutine(Routine("r1", "Campur", listOf(item("a"), item("b"), item("t", reps = null, seconds = 20, rest = 30))))
        userData.saveRoutine(Routine("r2", "Kosong jadwal", listOf(item("a"))))
        userData.saveScheduleEntry(ScheduleEntry("s1", 7, 990, "r1", reminder = true))
        userData.saveScheduleEntry(ScheduleEntry("s2", 1, 390, "r1", reminder = false))

        val data = ObserveRoutinesUseCase(userData, exercises, estimate)().first()

        val mixed = data.routines.first { it.id == "r1" }
        assertEquals(Difficulty.HARD, mixed.level, "level = latihan tertinggi")
        assertEquals(3, mixed.exerciseCount)
        assertEquals(12, mixed.estimatedMinutes)
        assertEquals(listOf(ScheduleSlot(1, 390), ScheduleSlot(7, 990)), mixed.schedule)
        assertEquals(emptyList(), data.routines.first { it.id == "r2" }.schedule)
    }

    @Test
    fun listOnlyOffersTemplatesWithKnownExercises() = runTest {
        val data = ObserveRoutinesUseCase(userData, exercises, estimate)().first()

        assertEquals(listOf("tpl_full", "tpl_panjang"), data.templates.map { it.id })
        assertEquals(3, data.templates.first().exerciseCount)
    }

    @Test
    fun deletedRoutineDisappearsFromTheList() = runTest {
        userData.saveRoutine(Routine("r1", "A", listOf(item())))
        val observe = ObserveRoutinesUseCase(userData, exercises, estimate)
        assertEquals(1, observe().first().routines.size)

        DeleteRoutineUseCase(userData)("r1")
        assertEquals(0, observe().first().routines.size)
    }

    // endregion

    // region Text

    @Test
    fun cardTextFollowsTheSpec() {
        val id = Strings(Language.ID)
        assertEquals("5 latihan · ±42 mnt", id.routineMeta(5, 42))
        assertEquals("1 latihan · ±5 mnt", id.routineMeta(1, 5))
        assertEquals("Belum dijadwalkan", id.scheduleText(emptyList()))
        assertEquals("Min 16.30", id.scheduleText(listOf(ScheduleSlot(7, 990))))
        assertEquals("Sen 06.30 +1 lagi", id.scheduleText(listOf(ScheduleSlot(1, 390), ScheduleSlot(3, 390))))
        assertEquals("Sun 16.30", Strings(Language.EN).scheduleText(listOf(ScheduleSlot(7, 990))))
    }

    // endregion
}
