# 05 Sesi Latihan

Figma: Session `6005:90` / `6005:351`; Failed sheet `6005:145` / `6005:406`; Complete `6005:221` / `6005:482`. Layar Istirahat dan Cek keselamatan belum didesain (lihat bawah).

## Alur (state machine)
`PreCheck` (hanya jika ada latihan tali) → `Exercise(i, set n)` → `Rest` → … → `Complete`. Cabang: `Abandoned` (hentikan), `FailedSheet` (set tidak selesai).

## Layar
- **Latihan**: tombol tutup, "Latihan 2 dari 5", jeda; 5 segmen progres; chip zona; nama; "Set 2 dari 3"; ring hitungan rep (atau hitung mundur untuk latihan durasi); kartu petunjuk (dari `steps`/`tips`); tombol **Selesai Set**, **Lewati**, **Jeda**.
- **Istirahat** (belum di Figma): hitung mundur `restSeconds`, latihan berikutnya, tombol +15 dtk dan Lewati istirahat.
- **Cek keselamatan** (belum di Figma): kartu `safetyNotes` + [Saya sudah cek].
- **Gagal (bottom sheet)**: muncul saat Lewati ditekan atau timer durasi habis tanpa selesai. [Coba lagi] [Turun ke <regresi>] [Lewati latihan ini].
- **Selesai**: ikon sukses, "Latihan Selesai!", tile latihan/set/menit, chip streak, [Selesai] [Lihat Riwayat].

## Aturan
- Layar tetap menyala; jeda menghentikan timer; kembali dari background melanjutkan.
- Haptic ringan saat set selesai.
- Tutup (X) → dialog "Hentikan latihan?"; Hentikan menyimpan `SessionLog(status=PARTIAL)` jika ≥ 1 set selesai, selain itu ABANDONED.
- Semua set selesai → `COMPLETED`. Log hanya ditulis jika `keepHistory=true`, tetapi streak tetap dihitung dari data sesi aktif.
- "Turun ke <regresi>" mengganti latihan saat ini dalam sesi saja (rutinitas asli tidak berubah); disembunyikan jika `regressionId=null`.

## Kriteria selesai
- Rotasi dan background tidak mengacaukan timer.
- Semua jalur (selesai, hentikan, gagal, turun level) menghasilkan log dan UI yang benar.
