package com.activaplan.app.domain.i18n

import com.activaplan.app.domain.model.AppLanguage
import com.activaplan.app.domain.model.BoardZone
import com.activaplan.app.domain.model.Category
import com.activaplan.app.domain.model.Difficulty
import com.activaplan.app.domain.model.Exercise
import com.activaplan.app.domain.model.Mode
import com.activaplan.app.domain.model.SessionStatus
import com.activaplan.app.domain.model.ThemeMode

internal fun applyArgs(text: String, vararg args: Any): String =
    args.foldIndexed(text) { i, acc, arg -> acc.replace("{$i}", arg.toString()) }

/** Akses teks UI untuk satu [language]; sepenuhnya offline. */
class Strings(val language: Language) {

    operator fun get(key: StringKey): String = key.text(language)

    /** Mengganti `{0}`, `{1}`, ... dengan [args] sesuai urutan. */
    fun format(key: StringKey, vararg args: Any): String = applyArgs(get(key), *args)

    fun label(value: Difficulty): String = get(
        when (value) {
            Difficulty.EASY -> StringKey.DIFFICULTY_EASY
            Difficulty.MEDIUM -> StringKey.DIFFICULTY_MEDIUM
            Difficulty.HARD -> StringKey.DIFFICULTY_HARD
        },
    )

    fun label(value: BoardZone): String = get(
        when (value) {
            BoardZone.BLUE -> StringKey.ZONE_BLUE
            BoardZone.RED -> StringKey.ZONE_RED
            BoardZone.YELLOW -> StringKey.ZONE_YELLOW
            BoardZone.GREEN -> StringKey.ZONE_GREEN
        },
    )

    /** Label chip zona, mis. "Biru · Dada". */
    fun zoneChip(value: BoardZone): String = get(
        when (value) {
            BoardZone.BLUE -> StringKey.ZONE_COLOR_BLUE
            BoardZone.RED -> StringKey.ZONE_COLOR_RED
            BoardZone.YELLOW -> StringKey.ZONE_COLOR_YELLOW
            BoardZone.GREEN -> StringKey.ZONE_COLOR_GREEN
        },
    ) + " \u00b7 " + label(value)

    fun label(value: Category): String = get(
        when (value) {
            Category.CHEST -> StringKey.CATEGORY_CHEST
            Category.ABS -> StringKey.CATEGORY_ABS
            Category.ARMS -> StringKey.CATEGORY_ARMS
            Category.SHOULDERS -> StringKey.CATEGORY_SHOULDERS
            Category.BACK -> StringKey.CATEGORY_BACK
            Category.LEGS -> StringKey.CATEGORY_LEGS
            Category.FULL_BODY -> StringKey.CATEGORY_FULL_BODY
        },
    )

    fun label(value: Mode): String = get(
        when (value) {
            Mode.NO_EQUIPMENT -> StringKey.MODE_NO_EQUIPMENT
            Mode.PUSHUP_BOARD -> StringKey.MODE_PUSHUP_BOARD
            Mode.BOARD_ROPE -> StringKey.MODE_BOARD_ROPE
        },
    )

    fun label(value: SessionStatus): String = get(
        when (value) {
            SessionStatus.COMPLETED -> StringKey.SESSION_COMPLETED
            SessionStatus.PARTIAL -> StringKey.SESSION_PARTIAL
            SessionStatus.ABANDONED -> StringKey.SESSION_ABANDONED
        },
    )

    fun label(value: ThemeMode): String = get(
        when (value) {
            ThemeMode.LIGHT -> StringKey.THEME_LIGHT
            ThemeMode.DARK -> StringKey.THEME_DARK
            ThemeMode.SYSTEM -> StringKey.THEME_SYSTEM
        },
    )

    fun label(value: AppLanguage): String = get(
        when (value) {
            AppLanguage.SYSTEM -> StringKey.LANGUAGE_SYSTEM
            AppLanguage.ID -> StringKey.LANGUAGE_ID
            AppLanguage.EN -> StringKey.LANGUAGE_EN
        },
    )

    /** Ringkasan kartu Jelajah; latihan durasi memakai "dtk", bukan "rep". */
    fun summary(exercise: Exercise): String = if (exercise.reps != null) {
        format(StringKey.EXPLORE_SUMMARY_REPS, exercise.sets, exercise.reps, exercise.restSeconds)
    } else {
        format(StringKey.EXPLORE_SUMMARY_SECONDS, exercise.sets, exercise.durationSeconds ?: 0, exercise.restSeconds)
    }

    /** Satuan tile kerja di Detail: "rep" untuk latihan repetisi, "detik" untuk latihan durasi. */
    fun workUnit(exercise: Exercise): String =
        get(if (exercise.reps != null) StringKey.DETAIL_UNIT_REP else StringKey.DETAIL_UNIT_SECONDS)

    /** Nama hari tiga huruf ("Sen", "Min"); [dayOfWeek]: 1 = Senin .. 7 = Minggu. */
    fun dayShort(dayOfWeek: Int): String {
        require(dayOfWeek in 1..7) { "dayOfWeek harus 1..7, bukan $dayOfWeek" }
        return get(StringKey.entries.first { it.name == "DAY_SHORT_$dayOfWeek" })
    }

    /** [month]: 1 = Januari .. 12 = Desember. */
    fun monthName(month: Int): String {
        require(month in 1..12) { "month harus 1..12, bukan $month" }
        return get(StringKey.entries.first { it.name == "MONTH_$month" })
    }

    /** [dayOfWeek]: 1 = Senin .. 7 = Minggu. */
    fun dayName(dayOfWeek: Int): String {
        require(dayOfWeek in 1..7) { "dayOfWeek harus 1..7, bukan $dayOfWeek" }
        return get(StringKey.entries.first { it.name == "DAY_$dayOfWeek" })
    }
}
