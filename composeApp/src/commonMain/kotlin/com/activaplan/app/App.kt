package com.activaplan.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.activaplan.app.data.repository.UserDataRepository
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.designsystem.isDark
import com.activaplan.app.domain.reminder.ReminderSynchronizer
import com.activaplan.app.presentation.ApplySystemAppearance
import com.activaplan.app.presentation.MainScaffold
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun App() {
    ReminderSyncEffect()
    // Tema mengikuti `Settings.theme` secara langsung; sebelum setelan terbaca tidak ada yang digambar agar tidak berkedip.
    val settings by koinInject<UserDataRepository>().settings.collectAsState(initial = null)
    val theme = settings?.theme ?: return
    val darkTheme = theme.isDark(isSystemInDarkTheme())
    ApplySystemAppearance(theme, darkTheme)
    ActivaPlanTheme(darkTheme = darkTheme) {
        MainScaffold()
    }
}

/**
 * Menjaga alarm pengingat sesuai jadwal selama aplikasi hidup, dan memasang ulang saat aplikasi dibuka kembali
 * (izin bisa berubah di pengaturan; di iOS jendela notifikasi tertunda ikut diperbarui).
 */
@Composable
private fun ReminderSyncEffect() {
    val synchronizer = koinInject<ReminderSynchronizer>()
    val scope = rememberCoroutineScope()
    LaunchedEffect(synchronizer) { synchronizer.keepInSync() }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        scope.launch {
            try {
                synchronizer.sync()
            } catch (_: Exception) {
                // Penyelarasan berikutnya mencoba lagi.
            }
        }
    }
}
