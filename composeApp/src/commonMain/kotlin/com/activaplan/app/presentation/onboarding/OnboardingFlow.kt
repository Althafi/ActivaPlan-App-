package com.activaplan.app.presentation.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.activaplan.app.presentation.PlatformBackHandler

/**
 * Alur pengguna baru: Perkenalan 1-3 lalu Onboarding. Ditampilkan oleh `App` selama `Settings.onboardingDone` false,
 * di luar NavHost, supaya Beranda tidak sempat tampil dan graf navigasi utama (dengan Beranda sebagai dasar tab)
 * baru dibuat setelah selesai. Tidak ada yang disimpan sebelum tombol akhir Onboarding ditekan.
 *
 * Kembali: halaman 2-3 menggeser satu halaman, Onboarding kembali ke halaman 3, halaman 1 menutup aplikasi.
 */
@Composable
fun OnboardingFlow(modifier: Modifier = Modifier) {
    // Dipegang di sini agar halaman terakhir yang dilihat tetap ada saat pengguna kembali dari Onboarding.
    val pagerState = rememberIntroPagerState()
    var inSetup by rememberSaveable { mutableStateOf(false) }

    PlatformBackHandler(enabled = inSetup) { inSetup = false }

    if (inSetup) {
        OnboardingScreen(onBack = { inSetup = false }, modifier = modifier)
    } else {
        IntroScreen(onSkip = { inSetup = true }, onFinish = { inSetup = true }, modifier = modifier, pagerState = pagerState)
    }
}

/**
 * "Lihat pengantar lagi" di Setelan: hanya tiga halaman Perkenalan, tanpa Onboarding dan tanpa mengubah setelan apa pun.
 * Lewati dan Mulai sama-sama menutup layar ini.
 */
@Composable
fun IntroReplayScreen(onClose: () -> Unit, modifier: Modifier = Modifier) {
    IntroScreen(onSkip = onClose, onFinish = onClose, modifier = modifier)
}
