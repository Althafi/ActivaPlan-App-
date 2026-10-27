package com.activaplan.app.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication

@Composable
actual fun KeepScreenOn(enabled: Boolean) {
    DisposableEffect(enabled) {
        if (enabled) UIApplication.sharedApplication.idleTimerDisabled = true
        onDispose { if (enabled) UIApplication.sharedApplication.idleTimerDisabled = false }
    }
}
