package com.activaplan.app.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.designsystem.ActivaShapes

enum class ButtonStyle { PRIMARY, SECONDARY, SOFT, DANGER }

/**
 * Tombol radius 16. Tinggi 52, atau 46 bila [compact] (dalam dialog); keduanya di atas target sentuh 44.
 * Status nonaktif memakai surfaceVar + textMuted.
 */
@Composable
fun ActivaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.PRIMARY,
    enabled: Boolean = true,
    icon: ActivaIconKind? = null,
    compact: Boolean = false,
) {
    val c = ActivaPlanTheme.colors
    val container = when {
        !enabled -> c.surfaceVar
        style == ButtonStyle.PRIMARY -> c.primary
        style == ButtonStyle.SECONDARY -> c.surface
        style == ButtonStyle.SOFT -> c.primarySoft
        else -> c.error
    }
    val content = when {
        !enabled -> c.textMuted
        style == ButtonStyle.PRIMARY -> c.onPrimary
        style == ButtonStyle.SECONDARY -> c.text
        style == ButtonStyle.SOFT -> c.primary
        else -> c.onError
    }
    val border = if (style == ButtonStyle.SECONDARY && enabled) BorderStroke(1.dp, c.border) else null
    Surface(
        color = container,
        contentColor = content,
        shape = ActivaShapes.button,
        border = border,
        modifier = modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick),
    ) {
        Row(
            Modifier.defaultMinSize(minHeight = if (compact) 46.dp else 52.dp).padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                ActivaIcon(icon, content, size = 18.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = ActivaPlanTheme.type.cardTitle.copy(fontSize = 15.sp), textAlign = TextAlign.Center)
        }
    }
}
