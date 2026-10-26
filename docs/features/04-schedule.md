# 04 Jadwal & Pengingat

Figma: Schedule terang `6008:2`, gelap `6008:389`. Layar Tambah/Ubah Jadwal belum didesain (bottom sheet sederhana, lihat bawah).

## Layar Jadwal
- Banner merah jika izin notifikasi ditolak ([Buka Pengaturan]).
- Pemilih pekan 7 hari (Sen 19 … Min 25); titik oranye = ada jadwal; hari terpilih berwarna primary.
- Daftar jadwal hari terpilih: jam, estimasi menit, nama rutinitas, badge level, jumlah latihan, toggle pengingat. Entri hari ini diberi garis oranye.
- FAB tambah jadwal.

## Tambah/Ubah Jadwal (bottom sheet)
Hari (multi-pilih chip), jam (time picker), rutinitas (daftar), toggle pengingat, [Simpan] [Hapus].

## Aturan
- Jadwal berulang mingguan berdasarkan `dayOfWeek` (1=Senin).
- Bentrok jika dua entri di hari sama memiliki jam mulai berbeda < estimasi durasi rutinitas pertama → dialog "Ganti atau tambah?".
- Pengingat: jadwalkan notifikasi lokal pada jam tersebut; mati jika izin ditolak atau `remindersEnabled=false`.
- Setiap perubahan jadwal menjadwal ulang seluruh alarm secara idempoten.
- Reboot Android: pulihkan alarm (receiver BOOT_COMPLETED).
- Entri yang menunjuk rutinitas terhapus dibersihkan.

## Platform
Android `AlarmManager`/WorkManager + izin `POST_NOTIFICATIONS` (API 33+). iOS `UNUserNotificationCenter` dengan trigger kalender mingguan (batas 64 notifikasi tertunda: jadwalkan hanya 7 hari ke depan dan perbarui saat aplikasi dibuka).

## Kriteria selesai
- Notifikasi muncul tepat waktu setelah aplikasi ditutup dan setelah reboot (Android).
- Izin ditolak menampilkan banner, toggle pengingat tetap bisa disimpan tetapi tidak berbunyi.
