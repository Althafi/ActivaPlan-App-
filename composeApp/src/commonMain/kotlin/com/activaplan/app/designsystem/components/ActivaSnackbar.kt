package com.activaplan.app.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.designsystem.ActivaShapes

/** Isi snackbar; [SnackbarDuration.Short] di Material 3 berarti 4 detik, sesuai `alerts-states.md`. */
data class ActivaSnackbarVisuals(
    override val message: String,
    val type: AlertType = AlertType.SUCCESS,
    override val actionLabel: String? = null,
    val icon: ActivaIconKind = type.defaultIcon,
) : SnackbarVisuals {
    override val duration: SnackbarDuration = SnackbarDuration.Short
    override val withDismissAction: Boolean = false
}

/** Tampilkan pesan; mengembalikan true bila tombol aksi ditekan. */
suspend fun SnackbarHostState.showActiva(
    message: String,
    type: AlertType = AlertType.SUCCESS,
    actionLabel: String? = null,
    icon: ActivaIconKind = type.defaultIcon,
): Boolean = showSnackbar(ActivaSnackbarVisuals(message, type, actionLabel, icon)) == SnackbarResult.ActionPerformed

@Composable
fun ActivaSnackbarHost(state: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(state, modifier.padding(horizontal = 20.dp)) { data ->
        val visuals = data.visuals as? ActivaSnackbarVisuals
        ActivaSnackbar(
            message = data.visuals.message,
            type = visuals?.type ?: AlertType.INFO,
            actionLabel = data.visuals.actionLabel,
            onAction = { data.performAction() },
            icon = visuals?.icon ?: (visuals?.type ?: AlertType.INFO).defaultIcon,
        )
    }
}

/** Snackbar: latar lembut sesuai jenis, ikon garis, teks, dan aksi berupa teks tebal. */
@Composable
fun ActivaSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    type: AlertType = AlertType.SUCCESS,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
    icon: ActivaIconKind = type.defaultIcon,
) {
    val c = ActivaPlanTheme.colors
    Surface(
        color = alertSoftColor(type),
        contentColor = c.text,
        shape = ActivaShapes.button,
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            ActivaIcon(icon, alertIconColor(type), size = 20.dp)
            Spacer(Modifier.width(12.dp))
            Text(
                message,
                style = ActivaPlanTheme.type.body.copy(fontWeight = FontWeight.Medium),
                color = c.text,
                modifier = Modifier.weight(1f),
            )
            if (actionLabel != null) {
                Spacer(Modifier.width(12.dp))
                Text(
                    actionLabel,
                    style = ActivaPlanTheme.type.label.copy(fontWeight = FontWeight.Bold),
                    color = c.text,
                    modifier = Modifier.minimumInteractiveComponentSize().clickable(role = Role.Button, onClick = onAction),
                )
            }
        }
    }
}
