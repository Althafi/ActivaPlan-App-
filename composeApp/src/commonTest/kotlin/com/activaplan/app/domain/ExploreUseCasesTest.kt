package com.activaplan.app.domain

import com.activaplan.app.data.InMemoryPreferencesDataStore
import com.activaplan.app.data.local.UserDataStore
import com.activaplan.app.data.repository.ExerciseDataSource
import com.activaplan.app.data.repository.ExerciseFiles
import com.activaplan.app.data.repository.ExerciseRepository
import com.activaplan.app.data.repository.UserDataRepository
import com.activaplan.app.domain.i18n.Language
import com.activaplan.app.domain.i18n.Strings
import com.activaplan.app.domain.model.AddToRoutineResult
import com.activaplan.app.domain.model.BoardLayout
import com.activaplan.app.domain.model.BoardZone
import com.activaplan.app.domain.model.Category
import com.activaplan.app.domain.model.Difficulty
import com.activaplan.app.domain.model.Exercise
import com.activaplan.app.domain.model.ExploreQuery
import com.activaplan.app.domain.model.Mode
import com.activaplan.app.domain.model.Routine
import com.activaplan.app.domain.model.RoutineItem
import com.activaplan.app.domain.model.RoutineRules
import com.activaplan.app.domain.usecase.AddExerciseToRoutineUseCase
import com.activaplan.app.domain.usecase.FilterExercisesUseCase
import com.activaplan.app.domain.usecase.GetExerciseDetailUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExploreUseCasesTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun ex(
        id: String,
        name: String,
        mode: Mode = Mode.PUSHUP_BOARD,
        category: Category = Category.CHEST,
        difficulty: Difficulty = Difficulty.EASY,
        muscles: List<String> = listOf("Dada"),
        reps: Int? = 10,
        durationSeconds: Int? = null,
        progressionId: String? = null,
        regressionId: String? = null,
    ) = Exercise(
        id = id, name = name, mode = mode, category = category, difficulty = difficulty,
        boardZone = if (mode == Mode.PUSHUP_BOARD) BoardZone.BLUE else null, primaryMuscles = muscles,
        sets = 3, reps = reps, durationSeconds = durationSeconds, restSeconds = 60,
        steps = listOf("s"), tips = listOf("t"), commonMistakes = listOf("m"),
        progressionId = progressionId, regressionId = regressionId,
    )

    // region FilterExercisesUseCase

    private val filter = FilterExercisesUseCase()

    private val catalog = listOf(
        ex("chest_easy", "Push-up Dada dari Lutut"),
        ex("chest_hard", "Push-up Dada Kaki Ditinggikan", difficulty = Difficulty.HARD),
        ex("abs_easy", "Plank Papan", category = Category.ABS, muscles = listOf("Perut", "Bahu")),
        ex("rope_arms", "Curl Biseps Tali", mode = Mode.BOARD_ROPE, category = Category.ARMS, muscles = listOf("Biseps")),
        ex("ne_chest", "Push-up Dinding", mode = Mode.NO_EQUIPMENT),
    )

    private fun ids(query: ExploreQuery) = filter(catalog, query).map { it.id }

    @Test
    fun modeSelectsOnlyExercisesOfThatMode() {
        assertEquals(listOf("chest_easy", "chest_hard", "abs_easy"), ids(ExploreQuery(Mode.PUSHUP_BOARD)))
        assertEquals(listOf("rope_arms"), ids(ExploreQuery(Mode.BOARD_ROPE)))
        assertEquals(listOf("ne_chest"), ids(ExploreQuery(Mode.NO_EQUIPMENT)))
    }

    @Test
    fun categoryAndDifficultyAreCombinedWithAnd() {
        val board = ExploreQuery(Mode.PUSHUP_BOARD)
        assertEquals(listOf("chest_easy", "chest_hard"), ids(board.copy(category = Category.CHEST)))
        assertEquals(listOf("chest_hard"), ids(board.copy(difficulty = Difficulty.HARD)))
        assertEquals(listOf("chest_hard"), ids(board.copy(category = Category.CHEST, difficulty = Difficulty.HARD)))
        assertEquals(emptyList(), ids(board.copy(category = Category.ABS, difficulty = Difficulty.HARD)))
    }

    @Test
    fun textSearchesNameAndPrimaryMusclesIgnoringCaseAndWhitespace() {
        val board = ExploreQuery(Mode.PUSHUP_BOARD)
        assertEquals(listOf("chest_easy", "chest_hard"), ids(board.copy(text = "DADA")))
        assertEquals(listOf("chest_easy"), ids(board.copy(text = "  LUTUT ")))
        assertEquals(listOf("abs_easy"), ids(board.copy(text = "perut")))
        assertEquals(listOf("abs_easy"), ids(board.copy(text = "bahu")))
        assertEquals(listOf("chest_easy", "chest_hard", "abs_easy"), ids(board.copy(text = "   ")))
    }

    @Test
    fun textIsCombinedWithOtherFilters() {
        val query = ExploreQuery(Mode.PUSHUP_BOARD, text = "dada", difficulty = Difficulty.HARD)
        assertEquals(listOf("chest_hard"), ids(query))
        assertEquals(emptyList(), ids(query.copy(text = "perut")))
    }

    // endregion

    // region BoardLayout

    @Test
    fun everyZoneHighlightsFourHolesInsideTheBoard() {
        BoardZone.entries.forEach { zone ->
            val holes = BoardLayout.highlightedHoles(zone)
            assertEquals(4, holes.size, "$zone")
            assertTrue(holes.all { it.row in 0 until BoardLayout.ROWS && it.column in 0 until BoardLayout.COLUMNS }, "$zone")
        }
    }

    @Test
    fun zonesAreVisuallyDistinct() {
        val patterns = BoardZone.entries.map { BoardLayout.highlightedHoles(it) }
        assertEquals(patterns.size, patterns.toSet().size)
    }

    @Test
    fun blueZoneMatchesFigma() {
        val blue = BoardLayout.highlightedHoles(BoardZone.BLUE).map { it.row to it.column }.toSet()
        assertEquals(setOf(0 to 0, 0 to 5, 1 to 2, 1 to 3), blue)
    }

    @Test
    fun compactThumbnailKeepsRowsAndFoldsColumnPairs() {
        val blue = BoardLayout.compactHighlightedHoles(BoardZone.BLUE).map { it.row to it.column }.toSet()
        assertEquals(setOf(0 to 0, 0 to 2, 1 to 1), blue)
        BoardZone.entries.forEach { zone ->
            assertTrue(BoardLayout.compactHighlightedHoles(zone).all { it.column in 0 until BoardLayout.COMPACT_COLUMNS })
        }
    }

    // endregion

    // region GetExerciseDetailUseCase

    private fun exerciseJson(e: Exercise) = """
        {"id":"${e.id}","name":"${e.name}","mode":"${e.mode}","category":"${e.category}","difficulty":"${e.difficulty}",
         "boardZone":${e.boardZone?.let { "\"$it\"" }},"primaryMuscles":["chest"],"sets":3,"reps":${e.reps},
         "durationSeconds":${e.durationSeconds},"restSeconds":60,"steps":["s"],"tips":["t"],"commonMistakes":["m"],
         "progressionId":${e.progressionId?.let { "\"$it\"" }},"regressionId":${e.regressionId?.let { "\"$it\"" }}}
    """.trimIndent()

    private val sourceExercises = listOf(
        ex("a", "A", progressionId = "b"),
        ex("b", "B", regressionId = "a", progressionId = "hilang"),
        ex("rope", "Tali", mode = Mode.BOARD_ROPE, reps = null, durationSeconds = 20),
    )

    private val files = mapOf(
        ExerciseFiles.GENERAL to """{"schemaVersion":1,"exercises":[],"templates":[]}""",
        ExerciseFiles.PUSHUP_BOARD to """{"schemaVersion":1,"device":{"safetyNotes":["Papan datar","Periksa tali"]},
            "exercises":[${sourceExercises.joinToString(",") { exerciseJson(it) }}],"templates":[]}""",
        ExerciseFiles.I18N to """{"schemaVersion":1,"baseLanguage":"id","muscles":[],
            "packs":{"en":{"device":{"name":"Board","zones":{},"safetyNotes":["Flat board"]}}}}""",
    )

    private val exercises = ExerciseRepository(ExerciseDataSource { files.getValue(it) }, json)
    private val getDetail = GetExerciseDetailUseCase(exercises)

    @Test
    fun levelNeighboursAreOnlyKeptWhenTheyExistInTheCatalog() = runTest {
        val a = getDetail("a", Language.ID)!!
        assertNull(a.regressionId)
        assertEquals("b", a.progressionId)

        val b = getDetail("b", Language.ID)!!
        assertEquals("a", b.regressionId)
        assertNull(b.progressionId, "progressionId ke ID yang tidak ada harus dibuang")
    }

    @Test
    fun unknownExerciseGivesNull() = runTest {
        assertNull(getDetail("tidak-ada", Language.ID))
    }

    @Test
    fun safetyNotesOnlyForRopeMode() = runTest {
        assertEquals(emptyList(), getDetail("a", Language.ID)!!.safetyNotes)
        assertEquals(listOf("Papan datar", "Periksa tali"), getDetail("rope", Language.ID)!!.safetyNotes)
        assertEquals(listOf("Flat board"), getDetail("rope", Language.EN)!!.safetyNotes)
    }

    // endregion

    // region AddExerciseToRoutineUseCase

    private val userData = UserDataRepository(UserDataStore(InMemoryPreferencesDataStore(), json), exercises)
    private val add = AddExerciseToRoutineUseCase(userData, newId = { "routine_baru" })

    private fun routine(id: String, itemCount: Int) =
        Routine(id, "Rutinitas $id", List(itemCount) { RoutineItem("a", 3, 10, null, 60) })

    @Test
    fun addingToExistingRoutineAppendsItemWithExerciseDefaults() = runTest {
        userData.saveRoutine(routine("r1", itemCount = 1))
        val plank = sourceExercises.last()

        val result = add.toExisting("r1", plank)

        assertEquals(AddToRoutineResult.Added("Rutinitas r1"), result)
        val saved = userData.routines.first().single()
        assertEquals(2, saved.items.size)
        assertEquals(RoutineItem("rope", sets = 3, reps = null, durationSeconds = 20, restSeconds = 60), saved.items.last())
    }

    @Test
    fun fullRoutineIsRejectedAndLeftUnchanged() = runTest {
        userData.saveRoutine(routine("r1", itemCount = RoutineRules.MAX_ITEMS))

        assertEquals(AddToRoutineResult.RoutineFull, add.toExisting("r1", sourceExercises.first()))
        assertEquals(RoutineRules.MAX_ITEMS, userData.routines.first().single().items.size)
    }

    @Test
    fun missingRoutineIsReported() = runTest {
        assertEquals(AddToRoutineResult.RoutineNotFound, add.toExisting("hilang", sourceExercises.first()))
        assertEquals(emptyList(), userData.routines.first())
    }

    @Test
    fun newRoutineIsCreatedWithTrimmedNameAndTheExercise() = runTest {
        val result = add.toNew("  Pagi Sehat  ", sourceExercises.first())

        assertEquals(AddToRoutineResult.Added("Pagi Sehat"), result)
        val saved = userData.routines.first().single()
        assertEquals("routine_baru", saved.id)
        assertEquals("Pagi Sehat", saved.name)
        assertEquals(listOf("a"), saved.items.map { it.exerciseId })
        assertEquals(10, saved.items.single().reps)
    }

    @Test
    fun invalidNamesDoNotCreateARoutine() = runTest {
        assertEquals(AddToRoutineResult.InvalidName, add.toNew("   ", sourceExercises.first()))
        assertEquals(AddToRoutineResult.InvalidName, add.toNew("x".repeat(RoutineRules.MAX_NAME_LENGTH + 1), sourceExercises.first()))
        assertEquals(emptyList(), userData.routines.first())
        assertEquals(AddToRoutineResult.Added("x".repeat(RoutineRules.MAX_NAME_LENGTH)), add.toNew("x".repeat(RoutineRules.MAX_NAME_LENGTH), sourceExercises.first()))
    }

    // endregion

    // region Strings

    @Test
    fun repExercisesShowRepAndDurationExercisesShowSeconds() {
        val id = Strings(Language.ID)
        val rep = ex("r", "R", reps = 8)
        val timed = ex("t", "T", reps = null, durationSeconds = 25)

        assertEquals("3 set × 8 rep · istirahat 60 dtk", id.summary(rep))
        assertEquals("3 set × 25 dtk · istirahat 60 dtk", id.summary(timed))
        assertEquals("rep", id.workUnit(rep))
        assertEquals("detik", id.workUnit(timed))

        val en = Strings(Language.EN)
        assertEquals("seconds", en.workUnit(timed))
        assertNotEquals(id.summary(timed), en.summary(timed))
    }

    // endregion
}
