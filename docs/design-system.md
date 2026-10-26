# Design System ActivaPlan

Font: Inter. Material 3 style. Frame mobile 390 × 844.

## Token warna
| Token | Terang | Gelap |
|---|---|---|
| bg | `#F7F8FA` | `#0E1117` |
| surface | `#FFFFFF` | `#171B24` |
| surfaceVar | `#EEF1F5` | `#1F2430` |
| border | `#E3E7EE` | `#2A3040` |
| text | `#12151C` | `#F2F4F8` |
| textMuted | `#5B6475` | `#9AA3B5` |
| primary | `#E8531A` | `#FF7F45` |
| onPrimary | `#FFFFFF` | `#1A0E06` |
| primarySoft | `#FFE9DF` | `#3A2417` |
| success / onSuccess / successSoft | `#1FA463` / `#FFFFFF` / `#E3F6EC` | `#3DD68C` / `#06210F` / `#12301F` |
| warning / warningSoft | `#F5A524` / `#FEF3DC` | `#FFC15A` / `#3A2D10` |
| error / onError / errorSoft | `#D92D20` / `#FFFFFF` / `#FDE8E6` | `#FF6B6F` / `#2B0A0B` / `#3A1716` |
| zoneBlue / Red / Yellow / Green | `#2F80ED` `#E5484D` `#F5C518` `#2FB171` | sama |
| board / boardHole | `#1B1F27` / `#4A5262` | `#0A0C10` / `#4A5262` |

Catatan: primary terang diubah dari `#FF6B2C` ke `#E8531A` agar kontras teks putih lebih baik. Di Figma: dua koleksi variabel `HW Light` dan `HW Dark` (paket Starter hanya 1 mode per koleksi), nama token identik.

## Zona board (selalu warna + label teks, jangan hanya warna)
Biru = Dada, Merah = Bahu, Kuning = Punggung, Hijau = Triceps. Warna zona tidak dipakai untuk status berhasil/gagal.

## Level
Mudah = success, Sedang = warning, Sulit = error. Selalu titik warna + teks.

## Tipografi
Judul layar 24 Bold, judul besar 26-30 Bold, judul kartu 15-16 Semi Bold, isi 14-15 Regular, label 11-13 Medium, angka timer 64 Bold.

## Bentuk dan spasi
Radius kartu 20, tombol 16, chip penuh. Padding layar 20. Target sentuh minimal 44-48. Bottom nav 84, status bar 44.

## Komponen (ada di Figma "Components & Alerts" dan layar)
Tombol (primary, secondary, soft, danger, disabled), chip, segmented control, kartu latihan (thumb board + badge level + badge zona), badge level, badge zona, stepper set/rep, toggle, ring progres, bottom nav 5 tab (Beranda, Jelajah, Rutinitas, Jadwal, Setelan), snackbar, dialog, bottom sheet, banner, empty state, FAB.
