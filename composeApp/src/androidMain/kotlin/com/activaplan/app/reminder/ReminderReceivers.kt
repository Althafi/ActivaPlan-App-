package com.activaplan.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.activaplan.app.domain.reminder.ReminderSynchronizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform

/** Berbunyi di jam pengingat: pasang alarm pekan depan lebih dulu agar tetap berjalan walau notifikasi gagal, lalu tampilkan. */
class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminder = ReminderAlarms.reminderFrom(intent) ?: return
        try {
            ReminderAlarms.arm(context, reminder)
        } finally {
            ReminderAlarms.show(context, reminder)
        }
    }
}

/** Memulihkan seluruh alarm setelah reboot, pembaruan aplikasi, perubahan zona waktu, atau perubahan izin alarm tepat waktu. */
class ReminderRestoreReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                KoinPlatform.getKoin().get<ReminderSynchronizer>().sync()
            } catch (_: Exception) {
                // Dicoba lagi saat aplikasi dibuka.
            } finally {
                pending.finish()
            }
        }
    }
}
