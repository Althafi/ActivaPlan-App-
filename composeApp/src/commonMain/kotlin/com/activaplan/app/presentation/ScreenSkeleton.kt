package com.activaplan.app.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.activaplan.app.designsystem.ActivaDimens
import com.activaplan.app.designsystem.ActivaPlanTheme

/** Kerangka layar tab: latar `bg` dan judul layar; isi fitur diisi di sesi fitur masing-masing. */
@Composable
fun ScreenSkeleton(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit = {}) {
    Column(
        modifier.fillMaxSize().background(ActivaPlanTheme.colors.bg).statusBarsPadding()
            .padding(horizontal = ActivaDimens.screenPadding, vertical = 16.dp),
    ) {
        Text(title, style = ActivaPlanTheme.type.screenTitle, color = ActivaPlanTheme.colors.text)
        content()
    }
}
