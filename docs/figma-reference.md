# Referensi Desain (Figma)

Sumber kebenaran tampilan: **https://www.figma.com/design/aWwYXscnhoUgmuczlIC9Sz/ActivaPlan-APP?node-id=0-1**
File key: `aWwYXscnhoUgmuczlIC9Sz`. Halaman utama: `6003:4`.

## Aturan untuk AI/pengembang
1. **Sebelum membuat atau mengubah tampilan, baca desain di link Figma di atas** (lewat Figma MCP: `get_metadata`, `get_screenshot`, `get_design_context`) dan ikuti gayanya. Jangan membuat gaya baru dari deskripsi teks.
2. Token warna, tipografi, radius, dan jarak diambil dari variable collection `HW Light` dan `HW Dark` (nama token sama, lihat `design-system.md`).
3. Setiap layar harus ada versi terang dan gelap.
4. Jika teks di spec berbeda dengan Figma, **Figma yang dipakai**, lalu perbarui spec.

## Peta layar
| Layar | Terang | Gelap | Spec |
|---|---|---|---|
| Beranda | 6004:2 | 6004:243 | features/01-home.md |
| Jelajah | 6004:118 | 6004:359 | features/02-explore-detail.md |
| Detail Latihan | 6005:2 | 6005:263 | features/02-explore-detail.md |
| Sesi Latihan | 6005:90 | 6005:351 | features/05-session.md |
| Sesi Gagal (sheet) | 6005:145 | 6005:406 | features/05-session.md |
| Sesi Selesai | 6005:221 | 6005:482 | features/05-session.md |
| Jadwal | 6008:2 | 6008:389 | features/04-schedule.md |
| Pembuat Rutinitas | 6008:105 | 6008:492 | features/03-routines.md |
| Komponen & Alert | 6008:261 | 6008:648 | alerts-states.md |

## Layar yang belum didesain
Daftar Rutinitas, Setelan, Tambah/Ubah Jadwal (sheet), Istirahat, Cek Keselamatan. Isi layar dan aturannya ada di spec fitur masing-masing (`features/03-routines.md`, `06-settings.md`, `04-schedule.md`, `05-session.md`). Saat membuatnya (Stitch atau Figma), cocokkan dengan layar yang sudah ada: kartu radius 20, tombol radius 16, font Inter, ikon garis, bottom nav yang sama.
