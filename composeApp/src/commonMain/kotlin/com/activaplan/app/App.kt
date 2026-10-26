package com.activaplan.app

import androidx.compose.runtime.Composable
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.presentation.MainScaffold

@Composable
fun App() {
    ActivaPlanTheme {
        MainScaffold()
    }
}
