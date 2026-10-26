package com.activaplan.app

import androidx.compose.ui.window.ComposeUIViewController
import com.activaplan.app.di.initKoin

fun MainViewController() = ComposeUIViewController(configure = { initKoin() }) { App() }
