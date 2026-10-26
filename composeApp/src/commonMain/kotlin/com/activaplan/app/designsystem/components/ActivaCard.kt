package com.activaplan.app.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.designsystem.ActivaShapes

/** Kartu radius 20: surface dengan garis border; bisa diklik bila [onClick] diberikan. */
@Composable
fun ActivaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = ActivaPlanTheme.colors
    val base = modifier
    Surface(
        color = c.surface,
        contentColor = c.text,
        shape = ActivaShapes.card,
        border = BorderStroke(1.dp, c.border),
        modifier = if (onClick != null) base.clickable(role = Role.Button, onClick = onClick) else base,
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}
