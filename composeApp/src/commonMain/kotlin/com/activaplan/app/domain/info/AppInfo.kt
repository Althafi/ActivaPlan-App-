package com.activaplan.app.domain.info

/** Keterangan aplikasi yang dibaca dari platform. Implementasi per platform disediakan lewat `platformModule()`. */
interface AppInfo {
    /** Nama versi yang tampil di Setelan, mis. "0.1.0". */
    val versionName: String
}
