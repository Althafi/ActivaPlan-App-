package com.activaplan.app.domain

import com.activaplan.app.domain.i18n.ExerciseDictionary
import com.activaplan.app.domain.i18n.ExerciseLocalizer
import com.activaplan.app.domain.i18n.Language
import com.activaplan.app.domain.i18n.StringKey
import com.activaplan.app.domain.i18n.Strings
import com.activaplan.app.domain.i18n.languageFromTag
import com.activaplan.app.domain.i18n.applyArgs
import com.activaplan.app.domain.i18n.resolve
import com.activaplan.app.domain.model.AppLanguage
import com.activaplan.app.domain.model.Category
import com.activaplan.app.domain.model.Difficulty
import com.activaplan.app.domain.model.Exercise
import com.activaplan.app.domain.model.Mode
import com.activaplan.app.domain.model.SessionStatus
import com.activaplan.app.domain.model.Settings
import com.activaplan.app.domain.model.Template
import com.activaplan.app.domain.model.ThemeMode
import com.activaplan.app.domain.validation.TranslationValidator
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class I18nTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val dictionary = json.decodeFromString<ExerciseDictionary>(
        """
        {"schemaVersion":1,"baseLanguage":"id",
         "muscles":[{"terms":["dada","chest"],"names":{"id":"Dada","en":"Chest"}},
                    {"terms":["triceps"],"names":{"id":"Triceps","en":"Triceps"}}],
         "packs":{"en":{
           "exercises":{"a":{"name":"Wall Push-up","steps":["s1"],"tips":["t1"],"commonMistakes":["m1"]}},
           "templates":{"t":"Beginner"},
           "device":{"name":"Board","zones":{"BLUE":{"label":"Blue","target":"Chest"}},"safetyNotes":["n"]}}}}
        """.trimIndent(),
    )

    private fun ex(id: String, muscles: List<String> = listOf("chest", "Triceps")) = Exercise(
        id = id, name = "Push-up Dinding", mode = Mode.NO_EQUIPMENT, category = Category.CHEST,
        difficulty = Difficulty.EASY, primaryMuscles = muscles, sets = 3, reps = 10, restSeconds = 30,
        steps = listOf("langkah"), tips = listOf("tip"), commonMistakes = listOf("salah"),
    )

    private val template = Template("t", "Pemula", Difficulty.EASY, listOf("a"))
    private val localizer = ExerciseLocalizer(dictionary)

    @Test
    fun everyStringKeyHasBothLanguages() {
        StringKey.entries.forEach {
            assertTrue(it.id.isNotBlank(), "ID kosong: $it")
            assertTrue(it.en.isNotBlank(), "EN kosong: $it")
        }
    }

    @Test
    fun placeholdersMatchBetweenLanguages() {
        val regex = Regex("""\{\d+}""")
        StringKey.entries.forEach {
            assertEquals(
                regex.findAll(it.id).map { m -> m.value }.toSet(),
                regex.findAll(it.en).map { m -> m.value }.toSet(),
                "Placeholder berbeda: $it",
            )
        }
    }

    @Test
    fun stringsPickLanguage() {
        assertEquals("Mudah", Strings(Language.ID).label(Difficulty.EASY))
        assertEquals("Easy", Strings(Language.EN).label(Difficulty.EASY))
        assertEquals("Seluruh tubuh", Strings(Language.ID).label(Category.FULL_BODY))
        assertEquals("Board + Rope", Strings(Language.EN).label(Mode.BOARD_ROPE))
        assertEquals("Dihentikan", Strings(Language.ID).label(SessionStatus.ABANDONED))
        assertEquals("Dark", Strings(Language.EN).label(ThemeMode.DARK))
        assertEquals("Ikuti sistem", Strings(Language.ID).label(AppLanguage.SYSTEM))
    }

    @Test
    fun dayNamesCoverWeek() {
        assertEquals("Senin", Strings(Language.ID).dayName(1))
        assertEquals("Sunday", Strings(Language.EN).dayName(7))
        assertFailsWith<IllegalArgumentException> { Strings(Language.ID).dayName(0) }
        assertFailsWith<IllegalArgumentException> { Strings(Language.ID).dayName(8) }
    }

    @Test
    fun applyArgsReplacesPlaceholders() {
        assertEquals("3 dari 6 set", applyArgs("{0} dari {1} set", 3, 6))
        assertEquals("6 / 3", applyArgs("{1} / {0}", 3, 6))
        assertEquals("tanpa arg", applyArgs("tanpa arg"))
        assertEquals("Senin", Strings(Language.ID).format(StringKey.DAY_1))
    }

    @Test
    fun languageResolution() {
        assertEquals(Language.ID, languageFromTag("id"))
        assertEquals(Language.ID, languageFromTag("id-ID"))
        assertEquals(Language.ID, languageFromTag("in_ID"))
        assertEquals(Language.EN, languageFromTag("en-US"))
        assertEquals(Language.EN, languageFromTag("fr-FR"))
        assertEquals(Language.EN, languageFromTag(""))
        assertEquals(Language.ID, AppLanguage.SYSTEM.resolve("id-ID"))
        assertEquals(Language.EN, AppLanguage.SYSTEM.resolve("de"))
        assertEquals(Language.EN, AppLanguage.EN.resolve("id"))
        assertEquals(Language.ID, AppLanguage.ID.resolve("en"))
    }

    @Test
    fun englishOverlaysTextAndMuscles() {
        val e = localizer.localize(ex("a"), Language.EN)
        assertEquals("Wall Push-up", e.name)
        assertEquals(listOf("s1"), e.steps)
        assertEquals(listOf("t1"), e.tips)
        assertEquals(listOf("m1"), e.commonMistakes)
        assertEquals(listOf("Chest", "Triceps"), e.primaryMuscles)
        assertEquals("Beginner", localizer.localize(template, Language.EN).name)
        assertEquals("Blue", localizer.deviceText(Language.EN)?.zones?.get("BLUE")?.label)
    }

    @Test
    fun baseLanguageKeepsSourceTextButNormalizesMuscles() {
        val e = localizer.localize(ex("a"), Language.ID)
        assertEquals("Push-up Dinding", e.name)
        assertEquals(listOf("langkah"), e.steps)
        assertEquals(listOf("Dada", "Triceps"), e.primaryMuscles)
        assertEquals("Pemula", localizer.localize(template, Language.ID).name)
        assertEquals(null, localizer.deviceText(Language.ID))
    }

    @Test
    fun missingTranslationFallsBackToSource() {
        val e = localizer.localize(ex("unknown", listOf("Betis")), Language.EN)
        assertEquals("Push-up Dinding", e.name)
        assertEquals(listOf("langkah"), e.steps)
        assertEquals(listOf("Betis"), e.primaryMuscles)
    }

    @Test
    fun translationValidatorAcceptsCompletePack() {
        val issues = TranslationValidator.validate(listOf(ex("a")), listOf(template), dictionary, Language.EN)
        assertTrue(issues.isEmpty(), issues.toString())
    }

    @Test
    fun translationValidatorReportsGaps() {
        val issues = TranslationValidator.validate(
            exercises = listOf(ex("a", listOf("chest", "Betis")), ex("b"), ex("c").copy(steps = listOf("x", "y"))),
            templates = listOf(template, Template("t2", "Lain", Difficulty.HARD, emptyList())),
            dictionary = dictionary,
            language = Language.EN,
        )
        val bySubject = issues.groupBy { it.subjectId }
        assertEquals(1, bySubject.getValue("a").size)   // istilah otot "Betis"
        assertEquals(1, bySubject.getValue("b").size)   // tidak ada terjemahan
        assertEquals(1, bySubject.getValue("c").size)   // tidak ada terjemahan
        assertEquals(1, bySubject.getValue("t2").size)  // template tanpa terjemahan
    }

    @Test
    fun translationValidatorReportsOrphansAndMissingPack() {
        val orphan = TranslationValidator.validate(emptyList(), emptyList(), dictionary, Language.EN)
        assertEquals(listOf("a"), orphan.map { it.subjectId })
        val noPack = TranslationValidator.validate(emptyList(), emptyList(), dictionary.copy(packs = emptyMap()), Language.EN)
        assertEquals(1, noPack.size)
    }

    @Test
    fun settingsLanguageDefaultsToSystemForOldData() {
        val old = """{"theme":"DARK","remindersEnabled":true,"defaultTimeMinutes":420,"keepHistory":true}"""
        assertEquals(AppLanguage.SYSTEM, json.decodeFromString<Settings>(old).language)
        val s = Settings(ThemeMode.SYSTEM, true, 420, true, AppLanguage.EN)
        assertEquals(s, json.decodeFromString<Settings>(json.encodeToString(s)))
    }
}
