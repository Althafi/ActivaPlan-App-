package com.activaplan.app.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.activaplan.app.designsystem.ActivaDimens
import com.activaplan.app.designsystem.ActivaPlanTheme

/** Bottom sheet modal: permukaan `surface`, sudut atas 24, terbuka penuh sesuai isi. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivaBottomSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val c = ActivaPlanTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface,
        contentColor = c.text,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = c.border) },
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = ActivaDimens.screenPadding).padding(bottom = 24.dp), content = content)
    }
}
