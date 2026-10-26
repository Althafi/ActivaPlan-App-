# Rencana Eksekusi per Sesi (Claude Code)

Satu sesi = satu baris. Mulai sesi baru (`/clear`) tiap baris agar konteks kecil. Selalu baca file di kolom "Baca" saja, jangan seluruh folder.
Prompt awal tiap sesi: `Baca <file di kolom Baca>, kerjakan <tugas>, berhenti saat kriteria selesai terpenuhi.`

| # | Sesi | Baca | Tugas | Selesai jika |
|---|---|---|---|---|
| 0 | Setup proyek | README.md | Buat proyek KMP + Compose Multiplatform (androidMain, iosMain, commonMain), Koin, coroutines, kotlinx-serialization, DataStore; struktur paket data/domain/ui | Build Android dan iOS jalan, layar kosong tampil |
| 1 | Model data | docs/data-model.md | Buat enum, `Exercise`, `ExerciseImage`, `Template`, `Routine`, `ScheduleEntry`, `SessionLog`, `Settings` + validasi | Unit test serialisasi dan validasi lulus |
| 2 | Loader latihan | docs/data-model.md, data/exercises_general.json, data/exercises_pushup_board.json | Salin JSON (termasuk `i18n_exercises.json`) ke `composeResources/files`, buat `ExerciseRepository` (baca, filter mode/kategori/level, terjemahan lewat `ExerciseLocalizer`) | Test: 46 latihan terbaca, referensi progresi valid, `TranslationValidator` EN tanpa temuan |
| 3 | Penyimpanan | docs/data-model.md | `UserDataStore` (DataStore + JSON) untuk routines, schedule, session_logs, settings; repository | Data bertahan setelah aplikasi ditutup |
| 4 | Tema & komponen | docs/design-system.md, docs/figma-reference.md, docs/alerts-states.md | Token terang/gelap, tipografi Inter, komponen (kartu, tombol, badge, chip zona, snackbar, dialog) | Preview komponen terang dan gelap cocok dengan Figma |
| 5 | Navigasi | README.md, docs/figma-reference.md | Bottom nav 5 tab + NavHost, kerangka layar | Pindah tab tanpa crash |
| 6 | Fitur Beranda | features/01-home.md | Layar, ViewModel, use case | Kriteria di file terpenuhi |
| 7 | Fitur Jelajah & Detail | features/02-explore-detail.md | Layar, filter, detail latihan | Kriteria di file terpenuhi |
| 8 | Fitur Rutinitas | features/03-routines.md | Daftar, Builder, CRUD, template, estimasi menit | CRUD bertahan, estimasi benar |
| 9 | Fitur Sesi | features/05-session.md, docs/alerts-states.md | State machine sesi, timer, istirahat, gagal, selesai, log | Semua jalur menghasilkan log benar |
| 10 | Fitur Jadwal | features/04-schedule.md | Layar jadwal, sheet tambah/ubah, deteksi bentrok | Jadwal CRUD benar |
| 11 | Pengingat (platform) | features/04-schedule.md | expect/actual notifikasi Android + iOS, izin | Notifikasi muncul tepat waktu |
| 12 | Fitur Setelan | features/06-settings.md | Tema, pengingat, data, tentang | Tema berganti tanpa restart |
| 13 | Gambar latihan | docs/data-sources-licenses.md | Tambah aset gambar berlisensi jelas, isi `images`, placeholder bila kosong | Tiap gambar punya lisensi tercatat |
| 14 | QA & rilis | README.md, docs/alerts-states.md | Uji semua alert, rotasi, dark mode, ikon, versi, build rilis | Checklist alert lulus |

## Catatan
- Layar tanpa desain Figma (Daftar Rutinitas, Setelan, sheet Jadwal, Istirahat, Cek Keselamatan): desain dulu lewat Figma/Stitch, lalu kerjakan sesi 8, 9, 10, 12.
- Urutan 0-5 wajib berurutan; 6-12 bisa bertukar kecuali 11 setelah 10 dan 9 setelah 8.
