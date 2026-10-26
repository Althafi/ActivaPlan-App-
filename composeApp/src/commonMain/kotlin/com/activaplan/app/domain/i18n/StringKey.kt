package com.activaplan.app.domain.i18n

/**
 * Kamus teks UI. Setiap entri wajib memuat kedua bahasa, jadi terjemahan yang
 * hilang menjadi error kompilasi. Parameter ditulis `{0}`, `{1}`, dst.
 */
enum class StringKey(val id: String, val en: String) {
    DIFFICULTY_EASY("Mudah", "Easy"),
    DIFFICULTY_MEDIUM("Sedang", "Medium"),
    DIFFICULTY_HARD("Sulit", "Hard"),

    CATEGORY_CHEST("Dada", "Chest"),
    CATEGORY_ABS("Perut", "Abs"),
    CATEGORY_ARMS("Lengan", "Arms"),
    CATEGORY_SHOULDERS("Bahu", "Shoulders"),
    CATEGORY_BACK("Punggung", "Back"),
    CATEGORY_LEGS("Kaki", "Legs"),
    CATEGORY_FULL_BODY("Seluruh tubuh", "Full body"),

    MODE_NO_EQUIPMENT("Tanpa Alat", "No Equipment"),
    MODE_PUSHUP_BOARD("Push-up Board", "Push-up Board"),
    MODE_BOARD_ROPE("Board + Tali", "Board + Rope"),

    SESSION_COMPLETED("Selesai", "Completed"),
    SESSION_PARTIAL("Sebagian", "Partial"),
    SESSION_ABANDONED("Dihentikan", "Abandoned"),

    THEME_LIGHT("Terang", "Light"),
    THEME_DARK("Gelap", "Dark"),
    THEME_SYSTEM("Ikuti sistem", "System"),

    LANGUAGE_SYSTEM("Ikuti sistem", "System"),
    LANGUAGE_ID("Bahasa Indonesia", "Bahasa Indonesia"),
    LANGUAGE_EN("English", "English"),

    ZONE_BLUE("Dada", "Chest"),
    ZONE_RED("Bahu", "Shoulders"),
    ZONE_YELLOW("Punggung", "Back"),
    ZONE_GREEN("Triceps", "Triceps"),

    ZONE_COLOR_BLUE("Biru", "Blue"),
    ZONE_COLOR_RED("Merah", "Red"),
    ZONE_COLOR_YELLOW("Kuning", "Yellow"),
    ZONE_COLOR_GREEN("Hijau", "Green"),

    NAV_HOME("Beranda", "Home"),
    NAV_EXPLORE("Jelajah", "Explore"),
    NAV_ROUTINES("Rutinitas", "Routines"),
    NAV_SCHEDULE("Jadwal", "Schedule"),
    NAV_SETTINGS("Setelan", "Settings"),

    ACTION_CONTINUE("Lanjut", "Continue"),
    ACTION_STOP("Hentikan", "Stop"),
    ACTION_CANCEL("Batal", "Cancel"),
    ACTION_DELETE("Hapus", "Delete"),
    ACTION_RETRY("Coba lagi", "Try again"),

    ALERT_SET_DONE("Set {0} dari {1} selesai. Istirahat {2} detik.", "Set {0} of {1} done. Rest {2} seconds."),
    ALERT_SCHEDULE_SAVED("Jadwal {0} {1} tersimpan.", "{0} {1} schedule saved."),
    ALERT_SCHEDULE_CONFLICT(
        "Sudah ada rutinitas jam ini. Ganti atau tambah?",
        "A routine already exists at this time. Replace or add?",
    ),
    ALERT_ROPE_CHECK(
        "Periksa tali dari retak dan pastikan papan stabil sebelum mulai.",
        "Check the rope for cracks and make sure the board is stable before starting.",
    ),
    ALERT_SAVE_FAILED("Gagal menyimpan. Coba lagi.", "Could not save. Try again."),
    ALERT_STOP_TITLE("Hentikan latihan?", "Stop workout?"),
    ALERT_STOP_BODY(
        "Progres sesi ini akan tersimpan sebagian.",
        "Progress of this session will be partially saved.",
    ),
    ALERT_DELETE_ROUTINE_TITLE("Hapus rutinitas ini?", "Delete this routine?"),
    ALERT_DELETE_ROUTINE_BODY(
        "Rutinitas dan jadwal terkait akan dihapus dari perangkat ini.",
        "The routine and its schedules will be deleted from this device.",
    ),

    DAY_1("Senin", "Monday"),
    DAY_2("Selasa", "Tuesday"),
    DAY_3("Rabu", "Wednesday"),
    DAY_4("Kamis", "Thursday"),
    DAY_5("Jumat", "Friday"),
    DAY_6("Sabtu", "Saturday"),
    DAY_7("Minggu", "Sunday"),
    ;

    fun text(language: Language): String = when (language) {
        Language.ID -> id
        Language.EN -> en
    }
}
