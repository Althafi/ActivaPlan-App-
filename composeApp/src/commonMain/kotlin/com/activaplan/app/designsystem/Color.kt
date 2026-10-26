package com.activaplan.app.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.activaplan.app.domain.model.BoardZone
import com.activaplan.app.domain.model.Difficulty

/** Token warna ActivaPlan; nama dan nilai mengikuti `docs/design-system.md`. */
@Immutable
data class ActivaColors(
    val bg: Color,
    val surface: Color,
    val surfaceVar: Color,
    val border: Color,
    val text: Color,
    val textMuted: Color,
    val primary: Color,
    val onPrimary: Color,
    val primarySoft: Color,
    val success: Color,
    val onSuccess: Color,
    val successSoft: Color,
    val warning: Color,
    val warningSoft: Color,
    val error: Color,
    val onError: Color,
    val errorSoft: Color,
    val zoneBlue: Color,
    val zoneRed: Color,
    val zoneYellow: Color,
    val zoneGreen: Color,
    val board: Color,
    val boardHole: Color,
    val isDark: Boolean,
) {
    fun zone(zone: BoardZone): Color = when (zone) {
        BoardZone.BLUE -> zoneBlue
        BoardZone.RED -> zoneRed
        BoardZone.YELLOW -> zoneYellow
        BoardZone.GREEN -> zoneGreen
    }

    /** Mudah = success, Sedang = warning, Sulit = error. */
    fun level(difficulty: Difficulty): Color = when (difficulty) {
        Difficulty.EASY -> success
        Difficulty.MEDIUM -> warning
        Difficulty.HARD -> error
    }

    fun levelSoft(difficulty: Difficulty): Color = when (difficulty) {
        Difficulty.EASY -> successSoft
        Difficulty.MEDIUM -> warningSoft
        Difficulty.HARD -> errorSoft
    }
}

private val ZoneBlue = Color(0xFF2F80ED)
private val ZoneRed = Color(0xFFE5484D)
private val ZoneYellow = Color(0xFFF5C518)
private val ZoneGreen = Color(0xFF2FB171)
private val BoardHole = Color(0xFF4A5262)

val LightActivaColors = ActivaColors(
    bg = Color(0xFFF7F8FA),
    surface = Color(0xFFFFFFFF),
    surfaceVar = Color(0xFFEEF1F5),
    border = Color(0xFFE3E7EE),
    text = Color(0xFF12151C),
    textMuted = Color(0xFF5B6475),
    primary = Color(0xFFE8531A),
    onPrimary = Color(0xFFFFFFFF),
    primarySoft = Color(0xFFFFE9DF),
    success = Color(0xFF1FA463),
    onSuccess = Color(0xFFFFFFFF),
    successSoft = Color(0xFFE3F6EC),
    warning = Color(0xFFF5A524),
    warningSoft = Color(0xFFFEF3DC),
    error = Color(0xFFD92D20),
    onError = Color(0xFFFFFFFF),
    errorSoft = Color(0xFFFDE8E6),
    zoneBlue = ZoneBlue,
    zoneRed = ZoneRed,
    zoneYellow = ZoneYellow,
    zoneGreen = ZoneGreen,
    board = Color(0xFF1B1F27),
    boardHole = BoardHole,
    isDark = false,
)

val DarkActivaColors = ActivaColors(
    bg = Color(0xFF0E1117),
    surface = Color(0xFF171B24),
    surfaceVar = Color(0xFF1F2430),
    border = Color(0xFF2A3040),
    text = Color(0xFFF2F4F8),
    textMuted = Color(0xFF9AA3B5),
    primary = Color(0xFFFF7F45),
    onPrimary = Color(0xFF1A0E06),
    primarySoft = Color(0xFF3A2417),
    success = Color(0xFF3DD68C),
    onSuccess = Color(0xFF06210F),
    successSoft = Color(0xFF12301F),
    warning = Color(0xFFFFC15A),
    warningSoft = Color(0xFF3A2D10),
    error = Color(0xFFFF6B6F),
    onError = Color(0xFF2B0A0B),
    errorSoft = Color(0xFF3A1716),
    zoneBlue = ZoneBlue,
    zoneRed = ZoneRed,
    zoneYellow = ZoneYellow,
    zoneGreen = ZoneGreen,
    board = Color(0xFF0A0C10),
    boardHole = BoardHole,
    isDark = true,
)

internal val LocalActivaColors = staticCompositionLocalOf { LightActivaColors }
