package com.activaplan.app.domain.validation

import com.activaplan.app.domain.i18n.ExerciseDictionary
import com.activaplan.app.domain.i18n.Language
import com.activaplan.app.domain.model.Exercise
import com.activaplan.app.domain.model.Mode
import com.activaplan.app.domain.model.Routine
import com.activaplan.app.domain.model.ScheduleEntry
import com.activaplan.app.domain.model.Template

/** Satu temuan validasi; [subjectId] adalah ID entitas yang bermasalah. */
data class ValidationIssue(val subjectId: String, val message: String)

object ExerciseCatalogValidator {

    fun validate(exercises: List<Exercise>, templates: List<Template>): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        val ids = exercises.map { it.id }.toSet()

        exercises.groupBy { it.id }.filterValues { it.size > 1 }.keys.forEach {
            issues += ValidationIssue(it, "ID latihan duplikat")
        }

        exercises.forEach { e ->
            if (e.mode == Mode.PUSHUP_BOARD && e.boardZone == null) {
                issues += ValidationIssue(e.id, "Mode PUSHUP_BOARD wajib boardZone")
            }
            if (e.mode != Mode.PUSHUP_BOARD && e.boardZone != null) {
                issues += ValidationIssue(e.id, "boardZone harus null untuk mode ${e.mode}")
            }
            if ((e.reps == null) == (e.durationSeconds == null)) {
                issues += ValidationIssue(e.id, "Tepat satu dari reps atau durationSeconds harus terisi")
            }
            e.progressionId?.takeIf { it !in ids }?.let {
                issues += ValidationIssue(e.id, "progressionId '$it' tidak ada")
            }
            e.regressionId?.takeIf { it !in ids }?.let {
                issues += ValidationIssue(e.id, "regressionId '$it' tidak ada")
            }
        }

        templates.forEach { t ->
            t.items.filter { it !in ids }.forEach {
                issues += ValidationIssue(t.id, "Item template '$it' tidak ada di exercises")
            }
        }
        return issues
    }
}

/** Hasil validasi data pengguna: entri valid dipertahankan, yang rusak diabaikan dan dilaporkan di [issues]. */
data class UserDataValidation(
    val routines: List<Routine>,
    val schedule: List<ScheduleEntry>,
    val issues: List<ValidationIssue>,
) {
    val hasBrokenData: Boolean get() = issues.isNotEmpty()
}

object UserDataValidator {

    fun validate(
        routines: List<Routine>,
        schedule: List<ScheduleEntry>,
        exerciseIds: Set<String>,
    ): UserDataValidation {
        val issues = mutableListOf<ValidationIssue>()

        val validRoutines = routines.map { routine ->
            val (kept, dropped) = routine.items.partition { it.exerciseId in exerciseIds }
            dropped.forEach {
                issues += ValidationIssue(routine.id, "Latihan '${it.exerciseId}' tidak ada, item diabaikan")
            }
            routine.copy(items = kept)
        }
        val routineIds = validRoutines.map { it.id }.toSet()

        val validSchedule = schedule.filter { entry ->
            val problem = when {
                entry.routineId !in routineIds -> "Rutinitas '${entry.routineId}' tidak ada, jadwal diabaikan"
                entry.dayOfWeek !in 1..7 -> "dayOfWeek ${entry.dayOfWeek} di luar 1..7, jadwal diabaikan"
                entry.timeMinutes !in 0..1439 -> "timeMinutes ${entry.timeMinutes} di luar 0..1439, jadwal diabaikan"
                else -> null
            }
            problem?.let { issues += ValidationIssue(entry.id, it) }
            problem == null
        }
        return UserDataValidation(validRoutines, validSchedule, issues)
    }
}

object TranslationValidator {

    /** Memeriksa [dictionary] untuk [language] terhadap data sumber: tiap latihan dan template harus punya terjemahan. */
    fun validate(
        exercises: List<Exercise>,
        templates: List<Template>,
        dictionary: ExerciseDictionary,
        language: Language,
    ): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        val muscleTerms = dictionary.muscles.flatMap { it.terms }.toSet()
        val pack = dictionary.packs[language.code]
            ?: return listOf(ValidationIssue(language.code, "Pack bahasa '${language.code}' tidak ada"))

        exercises.forEach { e ->
            val t = pack.exercises[e.id]
            if (t == null) {
                issues += ValidationIssue(e.id, "Terjemahan '${language.code}' tidak ada")
                return@forEach
            }
            if (t.name.isBlank()) issues += ValidationIssue(e.id, "Nama terjemahan kosong")
            if (t.steps.size != e.steps.size) issues += ValidationIssue(e.id, "Jumlah steps berbeda dari sumber")
            if (t.tips.size != e.tips.size) issues += ValidationIssue(e.id, "Jumlah tips berbeda dari sumber")
            if (t.commonMistakes.size != e.commonMistakes.size) {
                issues += ValidationIssue(e.id, "Jumlah commonMistakes berbeda dari sumber")
            }
            e.primaryMuscles.filter { it.lowercase() !in muscleTerms }.forEach {
                issues += ValidationIssue(e.id, "Istilah otot '$it' tidak ada di kamus")
            }
        }
        templates.filter { it.id !in pack.templates }.forEach {
            issues += ValidationIssue(it.id, "Terjemahan nama template '${language.code}' tidak ada")
        }
        val exerciseIds = exercises.map { it.id }.toSet()
        pack.exercises.keys.filter { it !in exerciseIds }.forEach {
            issues += ValidationIssue(it, "Terjemahan yatim: latihan tidak ada di sumber")
        }
        return issues
    }
}
