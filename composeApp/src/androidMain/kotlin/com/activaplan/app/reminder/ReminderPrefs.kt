package com.activaplan.app.reminder

import android.content.Context
import android.content.SharedPreferences

/** Catatan kecil pengingat: ID alarm yang sedang aktif dan apakah izin notifikasi pernah ditanyakan. */
internal object ReminderPrefs {
    private const val FILE = "activaplan_reminders"
    private const val KEY_IDS = "scheduled_ids"
    private const val KEY_ASKED = "permission_asked"

    private fun prefs(context: Context): SharedPreferences = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun scheduledIds(context: Context): Set<String> = prefs(context).getStringSet(KEY_IDS, emptySet()).orEmpty().toSet()

    fun saveScheduledIds(context: Context, ids: Set<String>) {
        prefs(context).edit().putStringSet(KEY_IDS, ids).apply()
    }

    /** Android tidak membedakan "belum ditanya" dan "ditolak permanen", jadi pertanyaan dicatat sendiri. */
    fun wasAsked(context: Context): Boolean = prefs(context).getBoolean(KEY_ASKED, false)

    fun markAsked(context: Context) {
        prefs(context).edit().putBoolean(KEY_ASKED, true).apply()
    }
}
