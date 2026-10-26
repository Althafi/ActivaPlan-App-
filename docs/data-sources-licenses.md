# Sumber Data & Lisensi

Bukan nasihat hukum. Prinsip: **data teks dan gambar dipisah**, dan hanya aset yang hak ciptanya jelas yang dipakai.

| Aset | Sumber | Status |
|---|---|---|
| Teks latihan (`exercises_*.json`) | Ditulis sendiri untuk ActivaPlan | Milik Anda, bebas dipakai |
| Gambar latihan | Belum ada; diisi lewat field `images` | Lihat aturan di bawah |
| Font Inter | SIL OFL 1.1 | Aman untuk app komersial, sertakan lisensi |
| Foto poster/produk push-up board | Materi penjual | **Jangan dipakai** di app |

## Dataset publik yang diperiksa
- **free-exercise-db / wrkout/exercises.json**: Unlicense untuk data, tetapi sumber dan hak gambar tidak didokumentasikan. **Tidak dipakai.**
- **RepDB**: gratis dengan atribusi, melarang republish dataset dan penggunaan untuk AI generatif. Tidak dibundel.
- **wger**: kode AGPL, data CC per latihan (risiko share-alike). Tidak dibundel.

## Aturan field `images`
`images: [{ "file": "pb_chest_pushup_1.webp", "alt": "...", "license": "OWN|CC0|CC-BY", "author": "...", "sourceUrl": "..." }]`
- Boleh kosong; UI memakai ikon zona/kategori sebagai placeholder.
- Hanya isi jika `license` jelas (karya sendiri, foto sendiri, atau ilustrasi CC0/berlisensi beli).
- Gambar disimpan sebagai file di `composeResources/drawable`, bukan di JSON.
- Pustaka sumber aman jika ingin mencari: Pexels/Pixabay (periksa lisensi per gambar), atau ilustrasi dari desainer dengan kontrak tertulis.
