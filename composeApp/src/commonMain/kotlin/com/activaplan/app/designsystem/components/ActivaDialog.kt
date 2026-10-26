package com.activaplan.app.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.activaplan.app.designsystem.ActivaPlanTheme

/** Dialog konfirmasi: aksi aman di kiri (secondary), aksi konfirmasi di kanan (merah, bisa berikon). */
@Composable
fun ActivaDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    dismissLabel: String,
    onDismiss: () -> Unit,
    confirmIcon: ActivaIconKind? = null,
) {
    Dialog(onDismissRequest = onDismiss) {
        ActivaDialogCard(title, message, confirmLabel, onConfirm, dismissLabel, onDismiss, confirmIcon = confirmIcon)
    }
}

/** Isi dialog tanpa jendela, dipakai juga oleh galeri komponen. */
@Composable
fun ActivaDialogCard(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    dismissLabel: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmIcon: ActivaIconKind? = null,
) {
    val c = ActivaPlanTheme.colors
    Surface(
        color = c.surface,
        contentColor = c.text,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        modifier = modifier,
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(title, style = ActivaPlanTheme.type.cardTitle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold), color = c.text)
            Spacer(Modifier.height(6.dp))
            Text(message, style = ActivaPlanTheme.type.body.copy(fontSize = 13.sp), color = c.textMuted)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActivaButton(dismissLabel, onDismiss, Modifier.weight(1f), ButtonStyle.SECONDARY, compact = true)
                ActivaButton(confirmLabel, onConfirm, Modifier.weight(1f), ButtonStyle.DANGER, icon = confirmIcon, compact = true)
            }
        }
    }
}
