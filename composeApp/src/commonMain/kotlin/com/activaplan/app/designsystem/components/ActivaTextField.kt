package com.activaplan.app.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.activaplan.app.designsystem.ActivaDimens
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.designsystem.ActivaShapes

/**
 * Kolom teks satu baris: latar `surfaceVar`, radius 16, [hint] saat kosong. Tombol hapus muncul bila ada isi dan [clearLabel] diberikan.
 * [isError] memberi garis `error`.
 */
@Composable
fun ActivaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    leadingIcon: ActivaIconKind? = null,
    clearLabel: String? = null,
    isError: Boolean = false,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
) {
    val c = ActivaPlanTheme.colors
    Surface(
        color = c.surfaceVar,
        shape = ActivaShapes.button,
        border = if (isError) BorderStroke(1.5.dp, c.error) else null,
        modifier = modifier.fillMaxWidth(),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = ActivaPlanTheme.type.body.copy(fontSize = 15.sp, color = c.text),
            cursorBrush = SolidColor(c.primary),
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            keyboardActions = KeyboardActions(onAny = { onImeAction() }),
            decorationBox = { inner ->
                Row(
                    Modifier.heightIn(min = ActivaDimens.minTouchTarget).padding(start = 14.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (leadingIcon != null) ActivaIcon(leadingIcon, c.textMuted, size = 20.dp)
                    Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) Text(hint, style = ActivaPlanTheme.type.body.copy(fontSize = 15.sp), color = c.textMuted)
                        inner()
                    }
                    if (clearLabel != null && value.isNotEmpty()) {
                        Box(
                            Modifier.size(ActivaDimens.minTouchTarget)
                                .semantics { contentDescription = clearLabel }
                                .clickable(role = Role.Button) { onValueChange("") },
                            contentAlignment = Alignment.Center,
                        ) {
                            ActivaIcon(ActivaIconKind.CLOSE, c.textMuted, size = 18.dp)
                        }
                    }
                }
            },
        )
    }
}
