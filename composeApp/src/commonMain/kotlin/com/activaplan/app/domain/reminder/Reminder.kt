package com.activaplan.app.domain.reminder

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus

/** Satu pengingat mingguan: tiap [dayOfWeek] (1 = Senin .. 7 = Minggu) pada [timeMinutes] (menit sejak 00:00). */
data class Reminder(
    val id: String,
    val dayOfWeek: Int,
    val timeMinutes: Int,
    val title: String,
    val body: String,
)

enum class PermissionState {
    GRANTED,

    /** Ditolak, atau notifikasi dimatikan di pengaturan sistem; hanya bisa diperbaiki lewat pengaturan. */
    DENIED,

    /** Belum pernah ditanyakan. */
    UNDETERMINED,
}

/** Izin notifikasi perangkat. Implementasi per platform disediakan lewat `platformModule()`. */
interface NotificationPermission {
    /** Status saat ini; dibaca ulang setiap dipanggil karena pengguna bisa mengubahnya di pengaturan. */
    suspend fun status(): PermissionState

    /** Membuka halaman pengaturan notifikasi aplikasi. */
    fun openSettings()
}

/** Penjadwal notifikasi lokal per platform. */
interface ReminderScheduler {
    /**
     * Membuat seluruh pengingat terjadwal persis sama dengan [reminders]: yang tidak ada di daftar dibatalkan,
     * yang ada dijadwalkan ulang. Aman dipanggil berulang dengan daftar yang sama.
     */
    suspend fun replaceAll(reminders: List<Reminder>)
}

/** Waktu terdekat setelah [now] untuk pengingat mingguan; hari yang sama tetapi jam sudah lewat jatuh pekan depan. */
fun nextOccurrence(dayOfWeek: Int, timeMinutes: Int, now: LocalDateTime): LocalDateTime {
    val daysAhead = (dayOfWeek - now.date.dayOfWeek.isoDayNumber + 7) % 7
    val candidate = now.date.plus(daysAhead, DateTimeUnit.DAY).atTime(timeMinutes / 60, timeMinutes % 60)
    return if (candidate > now) candidate else now.date.plus(daysAhead + 7, DateTimeUnit.DAY).atTime(timeMinutes / 60, timeMinutes % 60)
}

/** Paling banyak [limit] pengingat yang akan berbunyi paling dekat setelah [now], untuk platform dengan batas notifikasi tertunda. */
fun nearestReminders(reminders: List<Reminder>, now: LocalDateTime, limit: Int): List<Reminder> =
    reminders.sortedBy { nextOccurrence(it.dayOfWeek, it.timeMinutes, now) }.take(limit)
