package com.activaplan.app.designsystem

import activaplan.composeapp.generated.resources.Res
import activaplan.composeapp.generated.resources.inter_bold
import activaplan.composeapp.generated.resources.inter_medium
import activaplan.composeapp.generated.resources.inter_regular
import activaplan.composeapp.generated.resources.inter_semibold
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font

@Composable
fun interFontFamily(): FontFamily = FontFamily(
    Font(Res.font.inter_regular, FontWeight.Normal),
    Font(Res.font.inter_medium, FontWeight.Medium),
    Font(Res.font.inter_semibold, FontWeight.SemiBold),
    Font(Res.font.inter_bold, FontWeight.Bold),
)

/** Skala tipografi dari `docs/design-system.md` (Inter). */
@Immutable
data class ActivaTypography(
    val screenTitle: TextStyle,
    val largeTitle: TextStyle,
    val cardTitle: TextStyle,
    val body: TextStyle,
    val label: TextStyle,
    val labelSmall: TextStyle,
    val timer: TextStyle,
)

fun activaTypography(family: FontFamily): ActivaTypography = ActivaTypography(
    screenTitle = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    largeTitle = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    cardTitle = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    body = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    label = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = family, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp),
    timer = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 64.sp, lineHeight = 72.sp),
)

/** Memetakan skala ActivaPlan ke slot Material 3 agar komponen bawaan ikut Inter. */
fun ActivaTypography.toMaterial(): Typography = Typography(
    headlineMedium = screenTitle,
    headlineLarge = largeTitle,
    titleMedium = cardTitle,
    titleSmall = cardTitle.copy(fontSize = 15.sp),
    bodyLarge = body.copy(fontSize = 15.sp),
    bodyMedium = body,
    bodySmall = body.copy(fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = label,
    labelMedium = label.copy(fontSize = 12.sp),
    labelSmall = labelSmall,
    displayLarge = timer,
)
