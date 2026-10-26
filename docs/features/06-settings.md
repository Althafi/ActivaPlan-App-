# 06 Setelan (menu Setelan)

Figma: **belum ada** (layar Setelan belum didesain). Spesifikasi layout di bawah, mengikuti `design-system.md`.

## Layout (390 × 844, tab Setelan aktif)
Judul "Setelan", lalu grup kartu (radius 20, baris tinggi 52, pembatas 1px):
1. **TAMPILAN**: Tema, segmented control Terang | Gelap | Ikuti sistem.
2. **PENGINGAT**: Pengingat latihan (toggle) · Waktu default (nilai "16.30" + chevron) · Izin notifikasi (badge "Ditolak" merah atau "Diizinkan" + chevron, membuka pengaturan sistem).
3. **DATA**: Simpan riwayat sesi (toggle) · Hapus semua data (teks dan ikon error).
4. **TENTANG**: ActivaPlan, versi; atribusi sumber data latihan (sumber data latihan ditulis sendiri; lisensi pihak ketiga).

## Aturan
- Tema disimpan di `settings.theme`; `SYSTEM` mengikuti mode gelap perangkat; perubahan berlaku langsung.
- Matikan "Pengingat latihan" membatalkan semua alarm; nyalakan menjadwal ulang.
- "Waktu default" hanya mengisi nilai awal saat membuat jadwal baru.
- "Hapus semua data" → dialog destruktif; sukses mengosongkan `routines`, `schedule`, `session_logs` dan kembali ke Beranda dengan snackbar.
- Mematikan "Simpan riwayat sesi" tidak menghapus log lama (tawarkan opsi hapus terpisah).

## Kriteria selesai
- Tema berganti tanpa restart.
- Hapus data tidak menyentuh file JSON latihan bawaan.
