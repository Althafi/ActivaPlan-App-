# 01 Beranda (Home)

Figma: Home terang `6004:2`, gelap `6004:243`. Bergantung pada: 03, 04, 05.

## Tujuan
Menampilkan latihan terjadwal hari ini dan progres mingguan, satu ketukan untuk mulai.

## Isi layar
- Brand "ActivaPlan" (kecil, di atas sapaan; belum ada di Figma) + sapaan + tombol bel (menuju Jadwal).
- Kartu "Hari ini": jam, nama rutinitas, jumlah latihan, estimasi menit, tombol **Mulai Latihan** (membuka cek keselamatan jika ada latihan tali, lalu sesi).
- Streak minggu ini: 7 lingkaran (Sen-Min): centang = sesi COMPLETED, abu = tidak ada, garis oranye = hari ini.
- Rekomendasi: 2 template (kartu level + nama + jumlah latihan), ketuk membuka pratinjau lalu "Gunakan".

## Logika
- Rutinitas hari ini = `ScheduleEntry` dengan `dayOfWeek` hari ini, urut jam; tampilkan yang berikutnya (belum lewat), jika semua lewat tampilkan yang terakhir dengan label "Sudah lewat".
- Streak = jumlah hari terjadwal berturut-turut berstatus COMPLETED; hari tanpa jadwal tidak memutus.
- Tidak ada jadwal hari ini: kartu diganti empty state "Belum ada jadwal hari ini" + [Tambah jadwal].
- Rekomendasi: template yang levelnya sama atau satu tingkat di atas rutinitas terakhir selesai; default Mudah.

## Kriteria selesai
- Kartu hari ini benar untuk 0, 1, dan banyak jadwal.
- Streak benar di kasus: hari terlewat, hari tanpa jadwal, pergantian pekan.
- Tema terang/gelap sesuai Figma.
