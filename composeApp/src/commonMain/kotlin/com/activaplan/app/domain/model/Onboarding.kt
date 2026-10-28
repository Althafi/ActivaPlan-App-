package com.activaplan.app.domain.model

/**
 * Pilihan di layar Onboarding. [remindersOn] false berarti pengguna tidak meminta pengingat sekarang:
 * `remindersEnabled` dan `defaultTimeMinutes` dibiarkan seperti semula.
 */
data class OnboardingChoices(
    val startLevel: Difficulty,
    val ownedModes: Set<Mode>,
    val remindersOn: Boolean,
    val reminderTimeMinutes: Int,
) {
    /** Tombol utama aktif hanya bila minimal satu alat dipilih. */
    val isValid: Boolean get() = ownedModes.isNotEmpty() && reminderTimeMinutes in 0..1439

    companion object {
        /** Nilai awal layar Onboarding: level Mudah, hanya Tanpa Alat, pengingat mati pada jam default [defaultTimeMinutes]. */
        fun initial(defaultTimeMinutes: Int) = OnboardingChoices(
            startLevel = Difficulty.EASY,
            ownedModes = setOf(Mode.NO_EQUIPMENT),
            remindersOn = false,
            reminderTimeMinutes = defaultTimeMinutes,
        )
    }
}

/** Mode awal Jelajah: mode pertama yang dimiliki, urut Tanpa Alat, Push-up Board, Board + Tali. */
fun Settings.initialExploreMode(): Mode = Mode.entries.firstOrNull { it in ownedModes } ?: Mode.NO_EQUIPMENT
