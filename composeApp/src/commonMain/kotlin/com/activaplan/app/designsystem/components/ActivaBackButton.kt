package com.activaplan.app.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.activaplan.app.designsystem.ActivaDimens
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.domain.i18n.StringKey

/** Tombol kembali bulat 48 dengan chevron, seperti di Detail dan Builder. */
@Composable
fun ActivaBackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val c = ActivaPlanTheme.colors
    val label = ActivaPlanTheme.strings[StringKey.DETAIL_BACK]
    Box(
        modifier.size(ActivaDimens.minTouchTarget)
            .background(c.surface, CircleShape)
            .border(1.dp, c.border, CircleShape)
            .semantics { contentDescription = label }
            .clickable(role = Role.Button, onClick = onBack),
        contentAlignment = Alignment.Center,
    ) {
        ActivaIcon(ActivaIconKind.CHEVRON_LEFT, c.text, size = 22.dp)
    }
}
