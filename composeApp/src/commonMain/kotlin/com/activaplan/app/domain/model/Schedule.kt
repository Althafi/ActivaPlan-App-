package com.activaplan.app.domain.model

import kotlinx.datetime.LocalDate

/** Satu entri jadwal untuk layar Jadwal, lengkap dengan data rutinitasnya. */
data class ScheduleItem(
    val id: String,
    val dayOfWeek: Int,
    val timeMinutes: Int,
    val routineId: String,
    val routineName: String,
    val level: Difficulty?,
    val exerciseCount: Int,
    val estimatedMinutes: Int,
    val reminder: Boolean,
)

/** Satu hari di pemilih pekan; [hasSchedule] memberi titik oranye. */
data class ScheduleWeekDay(val date: LocalDate, val dayOfWeek: Int, val hasSchedule: Boolean, val isToday: Boolean)

/** Rutinitas yang bisa dipilih di sheet Tambah/Ubah. */
data class RoutineOption(
    val id: String,
    val name: String,
    val level: Difficulty?,
    val exerciseCount: Int,
    val estimatedMinutes: Int,
)

/**
 * Isi layar Jadwal. [items] urut hari lalu jam; [nextTodayId] adalah jadwal hari ini yang berikutnya
 * (atau yang terakhir bila semua sudah lewat), yang diberi garis oranye.
 */
data class ScheduleData(
    val today: LocalDate,
    val week: List<ScheduleWeekDay>,
    val items: List<ScheduleItem>,
    val nextTodayId: String?,
    val routines: List<RoutineOption>,
    val defaultTimeMinutes: Int,
)

/**
 * Isian sheet Tambah/Ubah. [editingId] null untuk jadwal baru. Satu entri dibuat per hari di [days];
 * saat mengubah, entri yang diubah dipakai ulang untuk salah satu hari.
 */
data class ScheduleDraft(
    val editingId: String?,
    val days: Set<Int>,
    val timeMinutes: Int,
    val routineId: String?,
    val reminder: Boolean,
) {
    val isComplete: Boolean get() = days.isNotEmpty() && routineId != null

    fun toggleDay(day: Int): ScheduleDraft = copy(days = if (day in days) days - day else days + day)

    companion object {
        fun from(item: ScheduleItem) = ScheduleDraft(
            editingId = item.id,
            days = setOf(item.dayOfWeek),
            timeMinutes = item.timeMinutes,
            routineId = item.routineId,
            reminder = item.reminder,
        )
    }
}

/** Cara menangani jadwal yang bentrok saat menyimpan. */
enum class ConflictPolicy {
    /** Berhenti dan lapor bila ada yang bentrok. */
    ASK,

    /** Hapus entri yang bentrok lalu simpan yang baru. */
    REPLACE,

    /** Simpan yang baru dan pertahankan entri lama. */
    KEEP_BOTH,
}

sealed interface SaveScheduleResult {
    /** [entries] adalah entri yang tersimpan, satu per hari. */
    data class Saved(val entries: List<ScheduleEntry>) : SaveScheduleResult

    /** Ada entri lain di hari yang sama yang waktunya bertumpuk; belum ada yang disimpan. */
    data class Conflict(val conflicts: List<ScheduleEntry>) : SaveScheduleResult

    data object NoDay : SaveScheduleResult
    data object NoRoutine : SaveScheduleResult
}
