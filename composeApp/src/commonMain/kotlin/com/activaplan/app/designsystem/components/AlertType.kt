package com.activaplan.app.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.activaplan.app.designsystem.ActivaPlanTheme

/** Jenis pesan; selalu dipasangkan dengan ikon dan teks, tidak hanya warna. */
enum class AlertType(val defaultIcon: ActivaIconKind) {
    SUCCESS(ActivaIconKind.CHECK),
    WARNING(ActivaIconKind.WARNING),
    ERROR(ActivaIconKind.WARNING),
    INFO(ActivaIconKind.INFO),
}

/** Latar lembut per jenis (successSoft, warningSoft, errorSoft, primarySoft). */
@Composable
fun alertSoftColor(type: AlertType): Color = with(ActivaPlanTheme.colors) {
    when (type) {
        AlertType.SUCCESS -> successSoft
        AlertType.WARNING -> warningSoft
        AlertType.ERROR -> errorSoft
        AlertType.INFO -> primarySoft
    }
}

/** Warna ikon: hijau untuk sukses, merah untuk gagal, warna teks untuk peringatan dan info. */
@Composable
fun alertIconColor(type: AlertType): Color = with(ActivaPlanTheme.colors) {
    when (type) {
        AlertType.SUCCESS -> success
        AlertType.ERROR -> error
        AlertType.WARNING, AlertType.INFO -> text
    }
}
