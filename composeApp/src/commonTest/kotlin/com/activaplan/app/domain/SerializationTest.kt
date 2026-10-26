package com.activaplan.app.domain

import com.activaplan.app.domain.model.BoardZone
import com.activaplan.app.domain.model.Category
import com.activaplan.app.domain.model.Difficulty
import com.activaplan.app.domain.model.Exercise
import com.activaplan.app.domain.model.ImageLicense
import com.activaplan.app.domain.model.Mode
import com.activaplan.app.domain.model.Routine
import com.activaplan.app.domain.model.RoutineItem
import com.activaplan.app.domain.model.ScheduleEntry
import com.activaplan.app.domain.model.SessionLog
import com.activaplan.app.domain.model.SessionStatus
import com.activaplan.app.domain.model.Settings
import com.activaplan.app.domain.model.Template
import com.activaplan.app.domain.model.ThemeMode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SerializationTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private inline fun <reified T> roundTrip(value: T) =
        assertEquals(value, json.decodeFromString<T>(json.encodeToString(value)))

    @Test
    fun exerciseFromJsonFileShape() {
        val text = """
            {"id":"pb_chest_knee_pushup","name":"Push-up Dada dari Lutut","mode":"PUSHUP_BOARD",
             "category":"CHEST","difficulty":"EASY","boardZone":"BLUE",
             "primaryMuscles":["chest","triceps"],"sets":3,"reps":8,"durationSeconds":null,
             "restSeconds":60,"steps":["a"],"tips":["b"],"commonMistakes":["c"],
             "progressionId":"pb_chest_pushup","regressionId":null,"images":[]}
        """.trimIndent()
        val e = json.decodeFromString<Exercise>(text)
        assertEquals(Mode.PUSHUP_BOARD, e.mode)
        assertEquals(Category.CHEST, e.category)
        assertEquals(BoardZone.BLUE, e.boardZone)
        assertEquals(8, e.reps)
        assertEquals(null, e.durationSeconds)
        assertTrue(e.images.isEmpty())
    }

    @Test
    fun exerciseWithoutImagesFieldDefaultsToEmpty() {
        val text = """
            {"id":"x","name":"X","mode":"NO_EQUIPMENT","category":"ABS","difficulty":"HARD",
             "primaryMuscles":[],"sets":1,"durationSeconds":30,"restSeconds":10,
             "steps":[],"tips":[],"commonMistakes":[]}
        """.trimIndent()
        val e = json.decodeFromString<Exercise>(text)
        assertTrue(e.images.isEmpty())
        assertEquals(null, e.boardZone)
    }

    @Test
    fun exerciseImageLicenseUsesDocumentedNames() {
        val text = """
            {"id":"x","name":"X","mode":"NO_EQUIPMENT","category":"ABS","difficulty":"EASY",
             "primaryMuscles":[],"sets":1,"reps":5,"restSeconds":10,"steps":[],"tips":[],"commonMistakes":[],
             "images":[{"file":"a.webp","alt":"a","license":"CC-BY","author":"Z","sourceUrl":"https://x"},
                       {"file":"b.webp","alt":"b","license":"OWN"},
                       {"file":"c.webp","alt":"c","license":"CC0"}]}
        """.trimIndent()
        val images = json.decodeFromString<Exercise>(text).images
        assertEquals(listOf(ImageLicense.CC_BY, ImageLicense.OWN, ImageLicense.CC0), images.map { it.license })
        assertEquals(null, images[1].author)
        assertTrue(json.encodeToString(images[0]).contains("\"CC-BY\""))
    }

    @Test
    fun templateRoundTrip() = roundTrip(Template("t", "T", Difficulty.MEDIUM, listOf("a", "b")))

    @Test
    fun userDataRoundTrip() {
        roundTrip(Routine("r", "R", listOf(RoutineItem("a", 3, 10, null, 45), RoutineItem("b", 2, null, 30, 20))))
        roundTrip(ScheduleEntry("s", 1, 420, "r", true))
        roundTrip(SessionLog("2026-10-25", "r", SessionStatus.PARTIAL, 3, 6))
        roundTrip(Settings(ThemeMode.SYSTEM, true, 420, false))
    }

    @Test
    fun settingsEnumsSerializeAsNames() {
        val s = json.encodeToString(Settings(ThemeMode.DARK, false, 480, true))
        assertTrue(s.contains("\"DARK\""))
    }
}
