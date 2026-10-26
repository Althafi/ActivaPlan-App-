package com.activaplan.app.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.activaplan.app.designsystem.ActivaPlanTheme

data class ActivaBottomNavItem(val label: String, val icon: ActivaIconKind, val selected: Boolean, val onClick: () -> Unit)

/** Bottom nav ala Figma: permukaan `surface`, garis atas `border`, tab aktif berwarna `primary`. */
@Composable
fun ActivaBottomNav(items: List<ActivaBottomNavItem>, modifier: Modifier = Modifier) {
    val c = ActivaPlanTheme.colors
    Column(modifier.fillMaxWidth().background(c.surface)) {
        HorizontalDivider(color = c.border)
        Row(
            Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            items.forEach { item ->
                val tint = if (item.selected) c.primary else c.textMuted
                Column(
                    Modifier.weight(1f).height(56.dp)
                        .selectable(selected = item.selected, role = Role.Tab, onClick = item.onClick),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    ActivaIcon(item.icon, tint, size = 24.dp)
                    Text(
                        item.label,
                        style = ActivaPlanTheme.type.labelSmall.copy(
                            fontWeight = if (item.selected) FontWeight.SemiBold else FontWeight.Medium,
                        ),
                        color = tint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        Box(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
    }
}
