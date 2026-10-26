package com.activaplan.app.presentation.explore

import androidx.compose.runtime.Composable
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.domain.i18n.StringKey
import com.activaplan.app.presentation.ScreenSkeleton

@Composable
fun ExploreScreen() = ScreenSkeleton(ActivaPlanTheme.strings[StringKey.NAV_EXPLORE])
