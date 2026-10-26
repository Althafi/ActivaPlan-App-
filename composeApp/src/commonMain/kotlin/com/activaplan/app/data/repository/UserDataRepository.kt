package com.activaplan.app.data.repository

import com.activaplan.app.data.local.UserDataStore
import com.activaplan.app.domain.model.Routine
import com.activaplan.app.domain.model.ScheduleEntry
import com.activaplan.app.domain.model.SessionLog
import com.activaplan.app.domain.model.Settings
import com.activaplan.app.domain.validation.UserDataValidation
import com.activaplan.app.domain.validation.UserDataValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/** Akses data pengguna (rutinitas, jadwal, riwayat sesi, setelan) di atas [UserDataStore]. */
class UserDataRepository(
    private val store: UserDataStore,
    private val exercises: ExerciseRepository,
) {
    val routines: Flow<List<Routine>> = store.data.map { it.routines }.distinctUntilChanged()
    val schedule: Flow<List<ScheduleEntry>> = store.data.map { it.schedule }.distinctUntilChanged()
    val sessionLogs: Flow<List<SessionLog>> = store.data.map { it.sessionLogs }.distinctUntilChanged()
    val settings: Flow<Settings> = store.data.map { it.settings }.distinctUntilChanged()

    /** Rutinitas dan jadwal yang valid terhadap katalog latihan; entri rusak diabaikan dan dilaporkan di `issues`. */
    val validated: Flow<UserDataValidation> = flow {
        val exerciseIds = exercises.catalog().exercises.map { it.id }.toSet()
        emitAll(store.data.map { UserDataValidator.validate(it.routines, it.schedule, exerciseIds) })
    }

    /** Menambah rutinitas baru, atau mengganti yang ber-ID sama. */
    suspend fun saveRoutine(routine: Routine) = store.update { d ->
        d.copy(routines = d.routines.upsert(routine) { it.id })
    }

    /** Menghapus rutinitas beserta jadwal yang menunjuk padanya. */
    suspend fun deleteRoutine(id: String) = store.update { d ->
        d.copy(
            routines = d.routines.filterNot { it.id == id },
            schedule = d.schedule.filterNot { it.routineId == id },
        )
    }

    suspend fun saveScheduleEntry(entry: ScheduleEntry) = store.update { d ->
        d.copy(schedule = d.schedule.upsert(entry) { it.id })
    }

    suspend fun deleteScheduleEntry(id: String) = store.update { d ->
        d.copy(schedule = d.schedule.filterNot { it.id == id })
    }

    /** Menambah log sesi; tidak melakukan apa pun jika `keepHistory` mati. Mengembalikan true bila log ditulis. */
    suspend fun appendSessionLog(log: SessionLog): Boolean {
        var written = false
        store.update { d ->
            written = d.settings.keepHistory
            if (written) d.copy(sessionLogs = d.sessionLogs + log) else d
        }
        return written
    }

    suspend fun clearSessionLogs() = store.update { it.copy(sessionLogs = emptyList()) }

    suspend fun updateSettings(transform: (Settings) -> Settings) = store.update { d ->
        d.copy(settings = transform(d.settings))
    }

    /** Menghapus rutinitas, jadwal, dan riwayat. Setelan dipertahankan. */
    suspend fun clearAllData() = store.update {
        it.copy(routines = emptyList(), schedule = emptyList(), sessionLogs = emptyList())
    }

    private inline fun <T> List<T>.upsert(item: T, id: (T) -> String): List<T> {
        val index = indexOfFirst { id(it) == id(item) }
        return if (index < 0) this + item else toMutableList().also { it[index] = item }
    }
}
