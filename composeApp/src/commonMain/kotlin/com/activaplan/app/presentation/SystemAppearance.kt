package com.activaplan.app.presentation

import androidx.compose.runtime.Composable
import com.activaplan.app.domain.model.ThemeMode

/**
 * Menyelaraskan tampilan bilah sistem (ikon status bar) dengan tema aplikasi, karena pilihan [theme] bisa
 * berbeda dari mode gelap perangkat. [darkTheme] adalah hasil akhir tema yang dipakai aplikasi.
 */
@Composable
expect fun ApplySystemAppearance(theme: ThemeMode, darkTheme: Boolean)
