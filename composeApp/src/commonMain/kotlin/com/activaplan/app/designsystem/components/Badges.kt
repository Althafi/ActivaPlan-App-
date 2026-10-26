package com.activaplan.app.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.designsystem.ActivaShapes
import com.activaplan.app.domain.model.BoardZone
import com.activaplan.app.domain.model.Difficulty

@Composable
private fun Dot(color: Color) {
    Spacer(Modifier.size(8.dp).background(color, CircleShape))
}

@Composable
private fun BadgePill(dot: Color, label: String, modifier: Modifier) {
    val c = ActivaPlanTheme.colors
    Row(
        modifier.clip(ActivaShapes.chip).background(c.surfaceVar).padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Dot(dot)
        Spacer(Modifier.width(6.dp))
        Text(label, style = ActivaPlanTheme.type.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = c.text)
    }
}

/** Badge level: titik warna + teks (Mudah/Sedang/Sulit). */
@Composable
fun LevelBadge(difficulty: Difficulty, modifier: Modifier = Modifier) =
    BadgePill(ActivaPlanTheme.colors.level(difficulty), ActivaPlanTheme.strings.label(difficulty), modifier)

/** Badge zona board: titik warna zona + "Biru · Dada", tidak pernah hanya warna. */
@Composable
fun ZoneChip(zone: BoardZone, modifier: Modifier = Modifier) =
    BadgePill(ActivaPlanTheme.colors.zone(zone), ActivaPlanTheme.strings.zoneChip(zone), modifier)

/** Chip filter: terpilih = primary penuh, tidak = surface bergaris. Target sentuh 48 lewat inset interaktif. */
@Composable
fun ActivaChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = ActivaPlanTheme.colors
    Row(
        modifier.minimumInteractiveComponentSize()
            .defaultMinSize(minHeight = 36.dp)
            .clip(ActivaShapes.chip)
            .background(if (selected) c.primary else c.surface)
            .border(1.dp, if (selected) c.primary else c.border, ActivaShapes.chip)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = ActivaPlanTheme.type.label, color = if (selected) c.onPrimary else c.text)
    }
}
