package com.activaplan.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.activaplan.app.domain.model.Routine
import com.activaplan.app.domain.model.ScheduleEntry
import com.activaplan.app.domain.model.SessionLog
import com.activaplan.app.domain.model.Settings
import com.activaplan.app.domain.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Menyimpan data pengguna di DataStore sebagai JSON, satu kunci per koleksi:
 * `routines`, `schedule`, `session_logs`, `settings`.
 *
 * Nilai yang tidak terbaca (JSON rusak) dianggap kosong/default agar aplikasi tetap terbuka.
 * Kegagalan tulis dilempar ke pemanggil supaya UI dapat menampilkan "Gagal menyimpan".
 */
class UserDataStore(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
) {
    val data: Flow<UserData> = dataStore.data.map(::read)

    /** Membaca, mengubah, dan menulis kembali dalam satu transaksi; hanya kunci yang berubah ditulis. */
    suspend fun update(transform: (UserData) -> UserData) {
        dataStore.edit { prefs ->
            val before = read(prefs)
            val after = transform(before)
            if (after.routines != before.routines) prefs[ROUTINES] = json.encodeToString(after.routines)
            if (after.schedule != before.schedule) prefs[SCHEDULE] = json.encodeToString(after.schedule)
            if (after.sessionLogs != before.sessionLogs) prefs[SESSION_LOGS] = json.encodeToString(after.sessionLogs)
            if (after.settings != before.settings) prefs[SETTINGS] = json.encodeToString(after.settings)
        }
    }

    private fun read(prefs: Preferences) = UserData(
        routines = decode(prefs[ROUTINES], emptyList<Routine>()),
        schedule = decode(prefs[SCHEDULE], emptyList<ScheduleEntry>()),
        sessionLogs = decode(prefs[SESSION_LOGS], emptyList<SessionLog>()),
        settings = decode(prefs[SETTINGS], Settings.DEFAULT),
    )

    private inline fun <reified T> decode(raw: String?, fallback: T): T {
        if (raw == null) return fallback
        return try {
            json.decodeFromString<T>(raw)
        } catch (_: SerializationException) {
            fallback
        } catch (_: IllegalArgumentException) {
            fallback
        }
    }

    private companion object {
        val ROUTINES = stringPreferencesKey("routines")
        val SCHEDULE = stringPreferencesKey("schedule")
        val SESSION_LOGS = stringPreferencesKey("session_logs")
        val SETTINGS = stringPreferencesKey("settings")
    }
}
