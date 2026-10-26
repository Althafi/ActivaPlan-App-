package com.activaplan.app.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.activaplan.app.designsystem.components.ActivaButton
import com.activaplan.app.designsystem.components.ActivaCard
import com.activaplan.app.designsystem.components.ActivaChip
import com.activaplan.app.designsystem.components.ActivaDialogCard
import com.activaplan.app.designsystem.components.ActivaIconKind
import com.activaplan.app.designsystem.components.ActivaSnackbar
import com.activaplan.app.designsystem.components.AlertType
import com.activaplan.app.designsystem.components.ButtonStyle
import com.activaplan.app.designsystem.components.LevelBadge
import com.activaplan.app.designsystem.components.ZoneChip
import com.activaplan.app.domain.i18n.StringKey
import com.activaplan.app.domain.model.BoardZone
import com.activaplan.app.domain.model.Difficulty
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            title.uppercase(),
            style = ActivaPlanTheme.type.labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp),
            color = ActivaPlanTheme.colors.textMuted,
        )
        content()
    }
}

/** Galeri komponen sesi 4, dibandingkan dengan `ActivaPlanFigma/Components & Alerts`. */
@Composable
fun ComponentGallery(modifier: Modifier = Modifier) {
    val c = ActivaPlanTheme.colors
    val s = ActivaPlanTheme.strings
    var mode by remember { mutableStateOf(1) }
    Column(
        modifier.fillMaxSize().background(c.bg).statusBarsPadding().verticalScroll(rememberScrollState())
            .padding(ActivaDimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Text("Komponen & Status", style = ActivaPlanTheme.type.screenTitle, color = c.text)

        Section("Snackbar / Alert") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ActivaSnackbar(s.format(StringKey.ALERT_SET_DONE, 2, 3, 60), Modifier.fillMaxWidth())
                ActivaSnackbar(
                    s.format(StringKey.ALERT_SCHEDULE_SAVED, s.dayName(1), "06.30"), Modifier.fillMaxWidth(),
                    icon = ActivaIconKind.CALENDAR,
                )
                ActivaSnackbar(
                    s[StringKey.ALERT_SAVE_FAILED], Modifier.fillMaxWidth(), AlertType.ERROR,
                    actionLabel = s[StringKey.ACTION_RETRY],
                )
                ActivaSnackbar(s[StringKey.ALERT_SCHEDULE_CONFLICT], Modifier.fillMaxWidth(), AlertType.WARNING)
                ActivaSnackbar(s[StringKey.ALERT_ROPE_CHECK], Modifier.fillMaxWidth(), AlertType.INFO)
            }
        }

        Section("Dialog") {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ActivaDialogCard(
                    title = s[StringKey.ALERT_STOP_TITLE], message = s[StringKey.ALERT_STOP_BODY],
                    confirmLabel = s[StringKey.ACTION_STOP], onConfirm = {},
                    dismissLabel = s[StringKey.ACTION_CONTINUE], onDismiss = {},
                )
                ActivaDialogCard(
                    title = s[StringKey.ALERT_DELETE_ROUTINE_TITLE], message = s[StringKey.ALERT_DELETE_ROUTINE_BODY],
                    confirmLabel = s[StringKey.ACTION_DELETE], onConfirm = {},
                    dismissLabel = s[StringKey.ACTION_CANCEL], onDismiss = {},
                    confirmIcon = ActivaIconKind.TRASH,
                )
            }
        }

        Section("Badge level & zona board") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Difficulty.entries.forEach { LevelBadge(it) }
                BoardZone.entries.forEach { ZoneChip(it) }
            }
        }

        Section("Chip filter") {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(StringKey.MODE_NO_EQUIPMENT, StringKey.MODE_PUSHUP_BOARD, StringKey.MODE_BOARD_ROPE)
                    .forEachIndexed { i, key -> ActivaChip(s[key], mode == i, { mode = i }) }
            }
        }

        Section("Kartu") {
            ActivaCard(Modifier.fillMaxWidth()) {
                Text("Push-up Dada Standar", style = ActivaPlanTheme.type.cardTitle, color = c.text)
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LevelBadge(Difficulty.MEDIUM)
                    ZoneChip(BoardZone.BLUE)
                }
                Text(
                    "3 set × 10 rep · istirahat 60 dtk",
                    style = ActivaPlanTheme.type.body.copy(fontSize = 13.sp), color = c.textMuted,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        Section("Tombol") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ActivaButton("Mulai Latihan", {}, Modifier.fillMaxWidth(), ButtonStyle.PRIMARY, icon = ActivaIconKind.CHECK)
                ActivaButton("Lewati", {}, Modifier.fillMaxWidth(), ButtonStyle.SECONDARY, icon = ActivaIconKind.CLOSE)
                ActivaButton("Tambah jadwal", {}, Modifier.fillMaxWidth(), ButtonStyle.SOFT, icon = ActivaIconKind.PLUS)
                ActivaButton("Hapus", {}, Modifier.fillMaxWidth(), ButtonStyle.DANGER, icon = ActivaIconKind.TRASH)
                ActivaButton("Tidak tersedia", {}, Modifier.fillMaxWidth(), enabled = false)
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 2000, showBackground = true)
@Composable
private fun ComponentGalleryLightPreview() = ActivaPlanTheme(darkTheme = false) { ComponentGallery() }

@Preview(widthDp = 390, heightDp = 2000, showBackground = true)
@Composable
private fun ComponentGalleryDarkPreview() = ActivaPlanTheme(darkTheme = true) { ComponentGallery() }
