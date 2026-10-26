package com.activaplan.app.presentation.schedule

import androidx.compose.runtime.Composable
import com.activaplan.app.designsystem.ActivaPlanTheme
import com.activaplan.app.domain.i18n.StringKey
import com.activaplan.app.presentation.ScreenSkeleton

@Composable
fun ScheduleScreen() = ScreenSkeleton(ActivaPlanTheme.strings[StringKey.NAV_SCHEDULE])
