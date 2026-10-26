# Data Model & Penyimpanan

## Enum
```kotlin
enum class Difficulty { EASY, MEDIUM, HARD }
enum class Category { CHEST, ABS, ARMS, SHOULDERS, BACK, LEGS, FULL_BODY }
enum class Mode { NO_EQUIPMENT, PUSHUP_BOARD, BOARD_ROPE }
enum class BoardZone { BLUE, RED, YELLOW, GREEN }   // Dada, Bahu, Punggung, Triceps
enum class SessionStatus { COMPLETED, PARTIAL, ABANDONED }
```

## Latihan (read-only, dari JSON)
```kotlin
@Serializable data class Exercise(
  val id: String, val name: String, val mode: Mode,
  val category: Category, val difficulty: Difficulty,
  val boardZone: BoardZone? = null,
  val primaryMuscles: List<String>,
  val sets: Int, val reps: Int? = null, val durationSeconds: Int? = null,
  val restSeconds: Int,
  val steps: List<String>, val tips: List<String>, val commonMistakes: List<String>,
  val progressionId: String? = null, val regressionId: String? = null)
@Serializable data class Template(val id: String, val name: String, val difficulty: Difficulty, val items: List<String>)
```
File `exercises_pushup_board.json`: `schemaVersion`, `device.zones`, `device.safetyNotes`, `exercises[23]`, `templates[4]`.

## Data pengguna (DataStore, JSON)
```kotlin
@Serializable data class Routine(val id: String, val name: String, val items: List<RoutineItem>)
@Serializable data class RoutineItem(val exerciseId: String, val sets: Int, val reps: Int?, val durationSeconds: Int?, val restSeconds: Int)
@Serializable data class ScheduleEntry(val id: String, val dayOfWeek: Int /*1=Sen..7=Min*/, val timeMinutes: Int, val routineId: String, val reminder: Boolean)
@Serializable data class SessionLog(val date: String, val routineId: String, val status: SessionStatus, val completedSets: Int, val totalSets: Int)
@Serializable data class Settings(val theme: ThemeMode, val remindersEnabled: Boolean, val defaultTimeMinutes: Int, val keepHistory: Boolean, val language: AppLanguage = AppLanguage.SYSTEM)
enum class ThemeMode { LIGHT, DARK, SYSTEM }
enum class AppLanguage { SYSTEM, ID, EN }
```
Kunci DataStore: `routines`, `schedule`, `session_logs`, `settings`.

## Aturan validasi (unit test wajib)
- ID latihan unik; `progressionId`/`regressionId` menunjuk ID yang ada.
- Setiap `templates[].items` ada di `exercises`.
- Mode `PUSHUP_BOARD` wajib `boardZone`; mode lain `boardZone = null`.
- Tepat satu dari `reps` atau `durationSeconds` terisi.
- Routine/Schedule yang menunjuk ID tidak ada diabaikan dan dilaporkan sebagai data rusak (lihat alerts-states.md).


## ExerciseImage
`ExerciseImage(file, alt, license: OWN|CC0|CC-BY, author?, sourceUrl?)`; `Exercise.images: List<ExerciseImage>` (boleh kosong). Lihat `data-sources-licenses.md`.

## Multi-bahasa (ID/EN, offline)
- Pilihan pengguna: `Settings.language` (`SYSTEM` mengikuti bahasa perangkat; bahasa selain Indonesia memakai Inggris). Data lama tanpa field ini terbaca sebagai `SYSTEM`.
- Teks UI dan label enum: `StringKey` (kode, kedua bahasa wajib ada, jadi terjemahan hilang = error kompilasi) diakses lewat `Strings(language)`. Parameter ditulis `{0}`, `{1}`.
- Konten latihan: `data/i18n_exercises.json`. Data sumber (JSON latihan) tetap berbahasa Indonesia. Pack bahasa lain (`packs.en`) berisi overlay per ID untuk `name`, `steps`, `tips`, `commonMistakes`, nama template, dan teks perangkat (zona, catatan keselamatan). Teks yang tidak ada di kamus memakai teks sumber.
- Konvensi data sumber: `category`/`mode`/`difficulty`/`boardZone` memakai nama enum; `primaryMuscles` berisi token Inggris huruf kecil (mis. `chest`, `lower back`) yang jadi kunci di `muscles` pada kamus, dan teksnya ditampilkan lewat `ExerciseLocalizer.muscle`; `templates[].items` berupa daftar ID latihan (string).
- Validasi `TranslationValidator` (unit test wajib untuk data asli di sesi 2): tiap latihan dan template punya terjemahan, jumlah `steps`/`tips`/`commonMistakes` sama dengan sumber, semua istilah otot ada di kamus, tidak ada terjemahan yatim.
- Menambah bahasa: tambah entri `Language`, kolom di `StringKey`, dan pack baru di JSON.
