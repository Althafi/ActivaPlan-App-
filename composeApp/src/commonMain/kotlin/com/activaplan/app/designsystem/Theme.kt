package com.activaplan.app.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import com.activaplan.app.domain.i18n.Language
import com.activaplan.app.domain.i18n.Strings

private fun ActivaColors.toMaterial() = if (isDark) {
    darkColorScheme(
        primary = primary, onPrimary = onPrimary, primaryContainer = primarySoft, onPrimaryContainer = primary,
        secondary = textMuted, onSecondary = bg, background = bg, onBackground = text,
        surface = surface, onSurface = text, surfaceVariant = surfaceVar, onSurfaceVariant = textMuted,
        outline = border, outlineVariant = border, error = error, onError = onError, errorContainer = errorSoft,
        onErrorContainer = error,
    )
} else {
    lightColorScheme(
        primary = primary, onPrimary = onPrimary, primaryContainer = primarySoft, onPrimaryContainer = primary,
        secondary = textMuted, onSecondary = bg, background = bg, onBackground = text,
        surface = surface, onSurface = text, surfaceVariant = surfaceVar, onSurfaceVariant = textMuted,
        outline = border, outlineVariant = border, error = error, onError = onError, errorContainer = errorSoft,
        onErrorContainer = error,
    )
}

internal val LocalActivaTypography = staticCompositionLocalOf { activaTypography(FontFamily.Default) }
internal val LocalStrings = staticCompositionLocalOf { Strings(Language.ID) }

/** Akses token ActivaPlan: `ActivaPlanTheme.colors.primary`. */
object ActivaPlanTheme {
    val colors: ActivaColors
        @Composable @ReadOnlyComposable get() = LocalActivaColors.current
    val type: ActivaTypography
        @Composable @ReadOnlyComposable get() = LocalActivaTypography.current
    val strings: Strings
        @Composable @ReadOnlyComposable get() = LocalStrings.current
}

@Composable
fun ActivaPlanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    language: Language = Language.ID,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkActivaColors else LightActivaColors
    val type = activaTypography(interFontFamily())
    val strings = remember(language) { Strings(language) }
    CompositionLocalProvider(
        LocalActivaColors provides colors,
        LocalActivaTypography provides type,
        LocalStrings provides strings,
    ) {
        MaterialTheme(colorScheme = colors.toMaterial(), typography = type.toMaterial(), content = content)
    }
}
