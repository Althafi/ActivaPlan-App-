# ActivaPlan

Aplikasi personal training mandiri (Kotlin Multiplatform + Compose Multiplatform). Atur jadwal, pilih latihan per otot dan level (mudah/sedang/sulit), tiga mode: **Tanpa Alat**, **Push-up Board**, **Board + Tali**. Tanpa backend, tanpa database server.

## Stack
| Kebutuhan | Keputusan |
|---|---|
| UI | Compose Multiplatform (`commonMain`) |
| DI | **Koin** (Hilt hanya Android; pakai Hilt hanya jika UI dipisah per platform) |
| Async | kotlinx-coroutines + StateFlow |
| Penyimpanan | DataStore KMP + kotlinx-serialization JSON (bukan database) |
| Data latihan | JSON dibundel: `exercises_general.json` (dari free-exercise-db, disaring bodyweight) + `exercises_pushup_board.json` |
| Navigasi | Navigation Compose Multiplatform |
| Reminder | expect/actual: WorkManager/AlarmManager (Android), UNUserNotificationCenter (iOS) |

Arsitektur: Clean Architecture + MVVM. Alur: `Screen → ViewModel(StateFlow) → UseCase → Repository → (JSON asset | DataStore)`.

```
composeApp/commonMain/{data, domain, presentation, di, designsystem}
composeApp/androidMain   notifikasi, DataStore factory
composeApp/iosMain       notifikasi, DataStore factory
iosApp/                  host SwiftUI
```

## Peta dokumen (baca hanya yang sedang dikerjakan)
| File | Isi |
|---|---|
| `docs/data-model.md` | Model, kunci penyimpanan, aturan validasi JSON |
| `docs/design-system.md` | Token warna terang/gelap, tipografi, komponen |
| `docs/alerts-states.md` | Semua alert, dialog, empty/error state |
| `docs/features/01-home.md` | Beranda |
| `docs/features/02-explore-detail.md` | Jelajah + Detail Latihan |
| `docs/features/03-routines.md` | Daftar & pembuat Rutinitas |
| `docs/features/04-schedule.md` | Jadwal + pengingat |
| `docs/features/05-session.md` | Sesi latihan, istirahat, selesai, gagal |
| `docs/features/06-settings.md` | Setelan |

## Urutan pengerjaan
1. Setup KMP + Koin + serialization + tema (design-system.md)
2. data-model.md + loader JSON + unit test
3. 02 Explore/Detail → 03 Routines → 04 Schedule → 05 Session → 01 Home → 06 Settings
4. alerts-states.md diterapkan bersamaan per fitur
5. Uji aksesibilitas (kontras, label zona), uji Android lalu iOS

## Status desain Figma
File: `APP` (key `aWwYXscnhoUgmuczlIC9Sz`), halaman ber-ID `6003:4` (nama halaman masih "Home Workout Planner", rencana diganti "ActivaPlan"). Tema terang di baris atas, gelap di baris bawah.
- Sudah ada (terang + gelap): Home, Explore, Exercise Detail, Workout Session, Session Failed sheet, Session Complete, Schedule, Routine Builder, Components & Alerts.
- **Belum ada**: Routine List (menu Rutinitas) dan Settings (menu Setelan). Spesifikasi layout ada di `03-routines.md` dan `06-settings.md`. Penambahan brand "ActivaPlan" di header Home juga tertunda.
- Penyebab: batas panggilan Figma MCP paket Starter tercapai.
