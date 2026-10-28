package com.activaplan.app.presentation

import androidx.compose.runtime.Composable

/**
 * True bila pengguna meminta gerak dikurangi: Android bila skala animator 0, iOS bila "Reduce Motion" menyala.
 * Splash dan ilustrasi Perkenalan menampilkan kondisi akhirnya tanpa bergerak.
 */
@Composable
expect fun rememberReduceMotion(): Boolean
