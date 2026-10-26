# 02 Jelajah & Detail Latihan

Figma: Explore `6004:118` / `6004:359`; Detail `6005:2` / `6005:263`. Data: `data-model.md`.

## Jelajah
- Judul, kolom cari, segmented control mode (Tanpa Alat | Push-up Board | Board + Tali), chip kategori (Semua, Dada, Perut, Lengan, Bahu, Punggung, Kaki), chip level (Semua level, Mudah, Sedang, Sulit).
- Kartu: thumbnail board (titik zona disorot), nama, badge level, badge zona (warna + label), "3 set × 8 rep · istirahat 60 dtk".
- Filter digabung AND. Cari di nama dan `primaryMuscles`. Kosong → empty state "Tidak ada latihan cocok" + [Reset filter].
- Tab Tanpa Alat memakai `exercises_general.json` (disaring bodyweight); dua tab lain memakai `exercises_pushup_board.json` dengan `mode` yang sesuai. Kategori FULL_BODY muncul di chip "Semua" dan saat dipilih "Full Body" (tambahkan chip jika dibutuhkan).

## Detail
- Hero: diagram board (2×6 lubang) dengan lubang zona disorot + chip "Zona Biru · Dada"; tombol kembali.
- Judul, badge level + zona, 3 tile (set, rep atau detik, istirahat), Langkah (bernomor), Tips, Kesalahan umum.
- Tombol **Turun level** (ke `regressionId`) dan **Naik level** (ke `progressionId`); nonaktif jika null.
- **Tambah ke Rutinitas**: pilih rutinitas yang ada atau buat baru; bawa nilai default set/rep/istirahat.
- Mode tali: tampilkan kartu info keselamatan singkat dari `device.safetyNotes`.

## Kriteria selesai
- 23 latihan board tampil dan terfilter benar; navigasi naik/turun level tidak menghasilkan ID tak dikenal.
- Zona di diagram sesuai `boardZone` (Biru/Merah/Kuning/Hijau).
- Latihan durasi (tanpa rep) menampilkan "detik", bukan "rep".
