package com.activaplan.app.presentation

import androidx.compose.runtime.Composable

/**
 * Mengembalikan fungsi yang menampilkan dialog izin notifikasi sistem. [onResult] dipanggil setelah pengguna
 * menjawab, atau langsung bila platform tidak butuh dialog; status terbaru dibaca lewat `NotificationPermission`.
 */
@Composable
expect fun rememberNotificationPermissionRequester(onResult: () -> Unit): () -> Unit
