package com.activaplan.app.domain.usecase

import com.activaplan.app.data.repository.UserDataRepository
import com.activaplan.app.domain.model.Settings
import com.activaplan.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

class ObserveSettingsUseCase(private val userData: UserDataRepository) {
    operator fun invoke(): Flow<Settings> = userData.settings
}

class SetThemeUseCase(private val userData: UserDataRepository) {
    suspend operator fun invoke(theme: ThemeMode) = userData.updateSettings { it.copy(theme = theme) }
}

/** Menyalakan atau mematikan semua pengingat; alarm diselaraskan oleh `ReminderSynchronizer` yang mengamati setelan. */
class SetRemindersEnabledUseCase(private val userData: UserDataRepository) {
    suspend operator fun invoke(enabled: Boolean) = userData.updateSettings { it.copy(remindersEnabled = enabled) }
}

/** Nilai awal jam saat membuat jadwal baru; jadwal yang sudah ada tidak berubah. */
class SetDefaultTimeUseCase(private val userData: UserDataRepository) {
    suspend operator fun invoke(timeMinutes: Int) {
        require(timeMinutes in 0..1439) { "timeMinutes di luar 0..1439: $timeMinutes" }
        userData.updateSettings { it.copy(defaultTimeMinutes = timeMinutes) }
    }
}

/** Mematikan tidak menghapus log lama; penghapusan lewat [ClearSessionHistoryUseCase]. */
class SetKeepHistoryUseCase(private val userData: UserDataRepository) {
    suspend operator fun invoke(keep: Boolean) = userData.updateSettings { it.copy(keepHistory = keep) }
}

class ClearSessionHistoryUseCase(private val userData: UserDataRepository) {
    suspend operator fun invoke() = userData.clearSessionLogs()
}

/** Mengosongkan rutinitas, jadwal, dan riwayat sesi. Setelan dan katalog latihan bawaan tidak tersentuh. */
class ClearAllDataUseCase(private val userData: UserDataRepository) {
    suspend operator fun invoke() = userData.clearAllData()
}
