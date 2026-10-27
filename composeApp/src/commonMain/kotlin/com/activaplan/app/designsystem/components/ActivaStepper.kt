package com.activaplan.app.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.domain.i18n.StringKey

/**
 * Stepper berlabel: "Set (−) 3 (+)". Tombol bulat 32 tampil kecil seperti di Figma, tetapi area sentuhnya 44;
 * [label] dibaca screen reader bersama nilai, mis. "Kurangi Set".
 */
@Composable
fun ActivaStepper(
    label: String,
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    canDecrease: Boolean = true,
    canIncrease: Boolean = true,
) {
    val c = ActivaPlanTheme.colors
    val strings = ActivaPlanTheme.strings
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, style = ActivaPlanTheme.type.label, color = c.textMuted)
        StepButton(ActivaIconKind.MINUS, strings.format(StringKey.BUILDER_DECREASE, label), canDecrease, onDecrease)
        Text(
            value.toString(),
            style = ActivaPlanTheme.type.cardTitle.copy(fontWeight = FontWeight.Bold),
            color = c.text,
            textAlign = TextAlign.Center,
            modifier = Modifier.defaultMinSize(minWidth = 28.dp),
        )
        StepButton(ActivaIconKind.PLUS, strings.format(StringKey.BUILDER_INCREASE, label), canIncrease, onIncrease)
    }
}

@Composable
private fun StepButton(icon: ActivaIconKind, description: String, enabled: Boolean, onClick: () -> Unit) {
    val c = ActivaPlanTheme.colors
    Box(
        Modifier.size(44.dp)
            .semantics { contentDescription = description }
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(32.dp).background(c.surfaceVar, CircleShape), contentAlignment = Alignment.Center) {
            ActivaIcon(icon, if (enabled) c.text else c.textMuted.copy(alpha = 0.5f), size = 16.dp)
        }
    }
}
