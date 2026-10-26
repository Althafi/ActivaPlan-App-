# 03 Rutinitas (menu Rutinitas)

Figma: **Routine Builder** terang `6008:105`, gelap `6008:492`. **Routine List: belum ada di Figma** (spesifikasi layout di bawah).

## Routine List (tab Rutinitas di bottom nav)
- Header "Rutinitas" + tombol tambah (lingkaran oranye, membuka Builder kosong).
- Segmented control: **Rutinitasku (n)** | **Template (4)**.
- Kartu rutinitas: nama, badge level (level tertinggi item), "5 latihan · 42 mnt", baris jadwal ("Min 16.30", atau "Belum dijadwalkan"), tombol **Mulai** (primary kecil) dan **Ubah** (secondary kecil), menu hapus.
- Bagian "Template siap pakai": kartu dengan nama, level, jumlah latihan, tombol **Gunakan** (menyalin template menjadi rutinitas milik pengguna lalu membuka Builder).
- Kosong: empty state "Belum ada rutinitas" + [Buat rutinitas] / [Pakai template].

## Routine Builder
- Top bar: kembali, "Buat Rutinitas".
- Kolom nama (wajib, 1-40 karakter).
- Daftar item: pegangan geser, titik zona, nama, stepper Set dan Rep (atau Detik untuk latihan durasi), hapus lewat geser.
- Tombol **Tambah latihan** → membuka Jelajah dalam mode pilih.
- Footer: total latihan + estimasi menit, tombol **Simpan Rutinitas**.

## Aturan
- Rentang: set 1-6, rep 1-50, detik 5-180 (langkah 5), istirahat 15-180 dtk.
- Minimal 1 latihan, maksimal 12.
- Estimasi menit = Σ set × (kerja + istirahat) / 60, kerja = rep × 4 dtk atau `durationSeconds`; ditampilkan "±".
- Hapus rutinitas ikut menghapus `ScheduleEntry` terkait (dialog konfirmasi, lihat alerts-states.md).
- Keluar Builder dengan perubahan belum disimpan → dialog "Buang perubahan?".

## Kriteria selesai
- CRUD rutinitas tersimpan dan bertahan setelah aplikasi ditutup.
- "Gunakan" template menghasilkan salinan independen.
- Estimasi menit konsisten dengan sesi nyata (selisih < 15%).
