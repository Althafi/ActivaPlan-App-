package com.activaplan.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class RoutineItem(
    val exerciseId: String,
    val sets: Int,
    val reps: Int? = null,
    val durationSeconds: Int? = null,
    val restSeconds: Int,
)

@Serializable
data class Routine(
    val id: String,
    val name: String,
    val items: List<RoutineItem>,
)

/** [dayOfWeek]: 1 = Senin .. 7 = Minggu. [timeMinutes]: menit sejak 00:00. */
@Serializable
data class ScheduleEntry(
    val id: String,
    val dayOfWeek: Int,
    val timeMinutes: Int,
    val routineId: String,
    val reminder: Boolean,
)

@Serializable
data class SessionLog(
    val date: String,
    val routineId: String,
    val status: SessionStatus,
    val completedSets: Int,
    val totalSets: Int,
)

@Serializable
data class Settings(
    val theme: ThemeMode,
    val remindersEnabled: Boolean,
    val defaultTimeMinutes: Int,
    val keepHistory: Boolean,
    val language: AppLanguage = AppLanguage.SYSTEM,
) {
    companion object {
        /** Nilai awal sebelum pengguna mengubah setelan; waktu default 16.30. */
        val DEFAULT = Settings(
            theme = ThemeMode.SYSTEM,
            remindersEnabled = true,
            defaultTimeMinutes = 16 * 60 + 30,
            keepHistory = true,
        )
    }
}

/** Seluruh data pengguna yang tersimpan, apa adanya (belum divalidasi terhadap katalog latihan). */
data class UserData(
    val routines: List<Routine> = emptyList(),
    val schedule: List<ScheduleEntry> = emptyList(),
    val sessionLogs: List<SessionLog> = emptyList(),
    val settings: Settings = Settings.DEFAULT,
)
