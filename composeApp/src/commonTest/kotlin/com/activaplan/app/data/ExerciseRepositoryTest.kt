package com.activaplan.app.data

import com.activaplan.app.data.repository.ExerciseDataSource
import com.activaplan.app.data.repository.ExerciseFilter
import com.activaplan.app.data.repository.ExerciseFiles
import com.activaplan.app.data.repository.ExerciseRepository
import com.activaplan.app.domain.i18n.Language
import com.activaplan.app.domain.model.Category
import com.activaplan.app.domain.model.Difficulty
import com.activaplan.app.domain.model.Mode
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExerciseRepositoryTest {
    private fun exerciseJson(id: String, mode: String, category: String, difficulty: String, zone: String = "null") = """
        {"id":"$id","name":"Nama $id","mode":"$mode","category":"$category","difficulty":"$difficulty",
         "boardZone":$zone,"primaryMuscles":["chest"],"sets":3,"reps":10,"durationSeconds":null,"restSeconds":30,
         "steps":["langkah"],"tips":["tip"],"commonMistakes":["salah"],"progressionId":null,"regressionId":null,"images":[]}
    """.trimIndent()

    private val files = mapOf(
        ExerciseFiles.GENERAL to """
            {"schemaVersion":1,"exercises":[
              ${exerciseJson("a", "NO_EQUIPMENT", "CHEST", "EASY")},
              ${exerciseJson("b", "NO_EQUIPMENT", "ABS", "HARD")}],
             "templates":[{"id":"t","name":"Pemula","difficulty":"EASY","items":["a"]}]}
        """.trimIndent(),
        ExerciseFiles.PUSHUP_BOARD to """
            {"schemaVersion":1,"device":{"name":"x"},"exercises":[
              ${exerciseJson("c", "PUSHUP_BOARD", "CHEST", "EASY", "\"BLUE\"")}],"templates":[]}
        """.trimIndent(),
        ExerciseFiles.I18N to """
            {"schemaVersion":1,"baseLanguage":"id",
             "muscles":[{"terms":["chest"],"names":{"id":"Dada","en":"Chest"}}],
             "packs":{"en":{"exercises":{"a":{"name":"Wall","steps":["s"],"tips":["t"],"commonMistakes":["m"]}},
                            "templates":{"t":"Beginner"}}}}
        """.trimIndent(),
    )

    private var reads = 0
    private val repository = ExerciseRepository(
        source = ExerciseDataSource { name -> reads++; files.getValue(name) },
        json = Json { ignoreUnknownKeys = true },
    )

    @Test
    fun mergesAllFiles() = runTest {
        assertEquals(listOf("a", "b", "c"), repository.exercises(language = Language.ID).map { it.id })
        assertEquals(listOf("t"), repository.templates(Language.ID).map { it.id })
    }

    @Test
    fun filtersByModeCategoryAndDifficulty() = runTest {
        assertEquals(listOf("c"), idsOf(ExerciseFilter(mode = Mode.PUSHUP_BOARD)))
        assertEquals(listOf("a", "c"), idsOf(ExerciseFilter(category = Category.CHEST)))
        assertEquals(listOf("b"), idsOf(ExerciseFilter(difficulty = Difficulty.HARD)))
        assertEquals(listOf("c"), idsOf(ExerciseFilter(Mode.PUSHUP_BOARD, Category.CHEST, Difficulty.EASY)))
        assertEquals(emptyList(), idsOf(ExerciseFilter(Mode.BOARD_ROPE)))
    }

    private suspend fun idsOf(f: ExerciseFilter) = repository.exercises(f, Language.ID).map { it.id }

    @Test
    fun translatesAndFallsBackToSource() = runTest {
        val en = repository.exercises(language = Language.EN).associateBy { it.id }
        assertEquals("Wall", en.getValue("a").name)
        assertEquals(listOf("Chest"), en.getValue("a").primaryMuscles)
        assertEquals("Nama b", en.getValue("b").name)
        assertEquals("Nama a", repository.exercise("a", Language.ID)?.name)
        assertEquals("Beginner", repository.templates(Language.EN).single().name)
        assertEquals("Pemula", repository.templates(Language.ID).single().name)
    }

    @Test
    fun findsByIdOrNull() = runTest {
        assertEquals("c", repository.exercise("c", Language.ID)?.id)
        assertNull(repository.exercise("zzz", Language.ID))
    }

    @Test
    fun readsEachFileOnce() = runTest {
        repository.exercises(language = Language.ID)
        repository.exercises(language = Language.EN)
        repository.templates(Language.EN)
        assertEquals(3, reads)
    }
}
