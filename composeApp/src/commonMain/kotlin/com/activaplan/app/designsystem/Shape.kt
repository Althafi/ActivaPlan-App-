package com.activaplan.app.designsystem

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Bentuk dan spasi dari `docs/design-system.md`: kartu 20, tombol 16, chip penuh, padding layar 20. */
object ActivaShapes {
    val card = RoundedCornerShape(20.dp)
    val button = RoundedCornerShape(16.dp)
    val chip = CircleShape
}

object ActivaDimens {
    val screenPadding = 20.dp
    val minTouchTarget = 48.dp
}
