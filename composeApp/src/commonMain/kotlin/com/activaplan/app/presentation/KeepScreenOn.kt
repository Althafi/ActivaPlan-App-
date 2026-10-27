package com.activaplan.app.presentation

import androidx.compose.runtime.Composable

/** Menjaga layar tetap menyala selama [enabled] dan komposabel ini ada di komposisi; dikembalikan saat keluar. */
@Composable
expect fun KeepScreenOn(enabled: Boolean = true)
