package com.activaplan.app.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.designsystem.ActivaShapes

/** Segmented control: trek `surfaceVar`, segmen terpilih berupa kartu `surface` bergaris dengan teks tebal. */
@Composable
fun ActivaSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = ActivaPlanTheme.colors
    Surface(color = c.surfaceVar, shape = ActivaShapes.button, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                Surface(
                    color = if (selected) c.surface else c.surfaceVar,
                    shape = ActivaShapes.button,
                    border = if (selected) BorderStroke(1.dp, c.border) else null,
                    modifier = Modifier.weight(1f).selectable(selected = selected, role = Role.Tab) { onSelect(index) },
                ) {
                    Row(
                        Modifier.defaultMinSize(minHeight = 40.dp).padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            label,
                            style = ActivaPlanTheme.type.label.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
                            color = if (selected) c.text else c.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}
