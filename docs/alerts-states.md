# Alert, Dialog, dan State

Aturan: ikon + teks + warna (jangan hanya warna). Snackbar tampil 4 detik. Semua pesan terbaca screen reader.

| Situasi | Tipe | Komponen | Pesan (ID) |
|---|---|---|---|
| Set selesai | Sukses | Snackbar | "Set 2 dari 3 selesai. Istirahat 60 detik." |
| Semua latihan selesai | Sukses | Layar "Latihan Selesai!" | ringkasan latihan, set, menit, streak |
| Jadwal tersimpan | Sukses | Snackbar | "Jadwal Senin 06.30 tersimpan." |
| Rutinitas tersimpan | Sukses | Snackbar | "Rutinitas tersimpan." |
| Hentikan sesi lebih awal | Peringatan | Dialog | "Hentikan latihan? Progres sesi ini akan tersimpan sebagian." [Lanjut] [Hentikan] |
| Set tidak selesai / Lewati | Gagal | Bottom sheet | "Set ini belum selesai. Tidak apa-apa, ..." [Coba lagi] [Turun ke <regresi>] [Lewati latihan ini] |
| Jadwal bentrok | Peringatan | Snackbar/dialog | "Sudah ada rutinitas jam ini. Ganti atau tambah?" |
| Izin notifikasi ditolak | Gagal | Banner di Jadwal + baris di Setelan | "Pengingat tidak aktif. Aktifkan izin notifikasi." [Buka Pengaturan] |
| Cek keselamatan mode tali | Info | Kartu sebelum sesi | "Periksa tali dari retak dan pastikan papan stabil." [Saya sudah cek] |
| Hapus rutinitas | Peringatan | Dialog destruktif | "Hapus rutinitas ini? Rutinitas dan jadwal terkait akan dihapus." [Batal] [Hapus] |
| Hapus semua data | Peringatan | Dialog destruktif | "Hapus semua data? Rutinitas, jadwal, dan riwayat hilang dan tidak dapat dikembalikan." |
| Gagal muat JSON latihan | Gagal | Empty state | "Data latihan tidak dapat dimuat." [Muat ulang] |
| Gagal simpan DataStore | Gagal | Snackbar + aksi | "Gagal menyimpan. Coba lagi." [Coba lagi] |
| Rutinitas rujuk latihan hilang | Peringatan | Snackbar | "Satu latihan tidak ditemukan dan dilewati." |
| Belum ada jadwal / rutinitas / riwayat | Kosong | Empty state | "Belum ada jadwal." [Tambah jadwal] |
