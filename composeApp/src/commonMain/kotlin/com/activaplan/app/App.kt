package com.activaplan.app

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.activaplan.app.data.repository.UserDataRepository
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.designsystem.isDark
import com.activaplan.app.domain.i18n.resolve
import com.activaplan.app.domain.model.AppLanguage
import com.activaplan.app.domain.reminder.ReminderSynchronizer
import com.activaplan.app.presentation.ApplySystemAppearance
import com.activaplan.app.presentation.MainScaffold
import com.activaplan.app.presentation.onboarding.OnboardingFlow
import com.activaplan.app.presentation.splash.SplashScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun App() {
    ReminderSyncEffect()
    val settings by koinInject<UserDataRepository>().settings.collectAsState(initial = null)
    val current = settings
    // Splash tampil tiap cold start; rememberSaveable menjaganya agar tidak diulang saat layar diputar.
    var splashDone by rememberSaveable { mutableStateOf(false) }
    val systemDark = isSystemInDarkTheme()
    // Sebelum setelan terbaca (splash) tema dan bahasa mengikuti sistem; sesudahnya mengikuti `Settings`.
    val darkTheme = current?.theme?.isDark(systemDark) ?: systemDark
    if (current != null) ApplySystemAppearance(current.theme, darkTheme)
    ActivaPlanTheme(darkTheme = darkTheme, language = (current?.language ?: AppLanguage.SYSTEM).resolve()) {
        Crossfade(targetState = splashDone && current != null, animationSpec = tween(SPLASH_EXIT_MS)) { showApp ->
            if (showApp && current != null) {
                // Satu-satunya tempat yang memutuskan tujuan awal: pengguna baru ke Perkenalan/Onboarding, selain itu ke Beranda.
                if (current.onboardingDone) MainScaffold() else OnboardingFlow()
            } else {
                SplashScreen(ready = current != null, onFinished = { splashDone = true })
            }
        }
    }
}

private const val SPLASH_EXIT_MS = 250

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
