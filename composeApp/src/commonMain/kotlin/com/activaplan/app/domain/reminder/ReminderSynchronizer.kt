package com.activaplan.app.domain.reminder

import com.activaplan.app.data.repository.UserDataRepository
import com.activaplan.app.domain.i18n.Strings
import com.activaplan.app.domain.i18n.StringKey
import com.activaplan.app.domain.i18n.resolve
import com.activaplan.app.domain.model.Routine
import com.activaplan.app.domain.model.ScheduleEntry
import com.activaplan.app.domain.model.Settings
import com.activaplan.app.domain.time.formatClock
import com.activaplan.app.domain.usecase.EstimateRoutineMinutesUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Menyelaraskan alarm notifikasi dengan jadwal. Pengingat hanya dibuat untuk entri ber-`reminder` yang
 * rutinitasnya ada, selama `Settings.remindersEnabled` menyala dan izin notifikasi diberikan; selain itu
 * semua alarm dibatalkan. Setiap penyelarasan menjadwal ulang seluruhnya, jadi idempoten.
 */
class ReminderSynchronizer(
    private val userData: UserDataRepository,
    private val scheduler: ReminderScheduler,
    private val permission: NotificationPermission,
    private val estimate: EstimateRoutineMinutesUseCase = EstimateRoutineMinutesUseCase(),
) {
    private val lock = Mutex()

    /** Menyelaraskan sekali dari data tersimpan saat ini. */
    suspend fun sync() {
        reschedule(userData.routines.first(), userData.schedule.first(), userData.settings.first())
    }

    /** Menyelaraskan sekarang dan setiap kali jadwal, rutinitas, atau setelan berubah; berjalan sampai dibatalkan. */
    suspend fun keepInSync() {
        combine(userData.routines, userData.schedule, userData.settings) { r, s, st -> Triple(r, s, st) }
            .collectLatest { (routines, schedule, settings) ->
                try {
                    reschedule(routines, schedule, settings)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Kegagalan platform tidak boleh menghentikan pengamatan; penyelarasan berikutnya mencoba lagi.
                }
            }
    }

    private suspend fun reschedule(routines: List<Routine>, schedule: List<ScheduleEntry>, settings: Settings) = lock.withLock {
        val active = settings.remindersEnabled && permission.status() == PermissionState.GRANTED
        scheduler.replaceAll(if (active) build(routines, schedule, Strings(settings.language.resolve())) else emptyList())
    }

    private fun build(routines: List<Routine>, schedule: List<ScheduleEntry>, strings: Strings): List<Reminder> {
        val routineById = routines.filter { it.items.isNotEmpty() }.associateBy { it.id }
        return schedule
            .filter { it.reminder && it.dayOfWeek in 1..7 && it.timeMinutes in 0..1439 }
            .sortedWith(compareBy({ it.dayOfWeek }, { it.timeMinutes }, { it.id }))
            .mapNotNull { entry ->
                routineById[entry.routineId]?.let { routine ->
                    Reminder(
                        id = entry.id,
                        dayOfWeek = entry.dayOfWeek,
                        timeMinutes = entry.timeMinutes,
                        title = strings.format(StringKey.REMINDER_TITLE, formatClock(entry.timeMinutes)),
                        body = strings.format(StringKey.REMINDER_BODY, routine.name, estimate(routine)),
                    )
                }
            }
    }
}
