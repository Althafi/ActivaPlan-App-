package com.activaplan.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class Difficulty { EASY, MEDIUM, HARD }

enum class Category { CHEST, ABS, ARMS, SHOULDERS, BACK, LEGS, FULL_BODY }

enum class Mode { NO_EQUIPMENT, PUSHUP_BOARD, BOARD_ROPE }

/** Dada, Bahu, Punggung, Triceps. */
enum class BoardZone { BLUE, RED, YELLOW, GREEN }

enum class SessionStatus { COMPLETED, PARTIAL, ABANDONED }

enum class ThemeMode { LIGHT, DARK, SYSTEM }

/** Pilihan bahasa pengguna; [SYSTEM] mengikuti bahasa perangkat. */
enum class AppLanguage { SYSTEM, ID, EN }

@Serializable
enum class ImageLicense {
    @SerialName("OWN") OWN,
    @SerialName("CC0") CC0,
    @SerialName("CC-BY") CC_BY,
}
