package com.activaplan.app.presentation

import androidx.compose.runtime.Composable

/**
 * Menangkap tombol/gestur Kembali sistem selama [enabled]; saat aktif, [onBack] menggantikan perilaku bawaan.
 * Di iOS belum menangkap apa pun, jadi tombol kembali di layar yang harus dijaga sendiri.
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
