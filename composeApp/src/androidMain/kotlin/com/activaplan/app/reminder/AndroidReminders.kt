package com.activaplan.app.reminder

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.activaplan.app.domain.reminder.NotificationPermission
import com.activaplan.app.domain.reminder.PermissionState
import com.activaplan.app.domain.reminder.Reminder
import com.activaplan.app.domain.reminder.ReminderScheduler

class AndroidNotificationPermission(private val context: Context) : NotificationPermission {

    override suspend fun status(): PermissionState {
        val manager = context.getSystemService(NotificationManager::class.java)
        return when {
            manager.areNotificationsEnabled() -> PermissionState.GRANTED
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !ReminderPrefs.wasAsked(context) -> PermissionState.UNDETERMINED
            else -> PermissionState.DENIED
        }
    }

    override fun openSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

class AndroidReminderScheduler(private val context: Context) : ReminderScheduler {

    override suspend fun replaceAll(reminders: List<Reminder>) {
        ReminderPrefs.scheduledIds(context).forEach { ReminderAlarms.cancel(context, it) }
        ReminderAlarms.ensureChannel(context)
        reminders.forEach { ReminderAlarms.arm(context, it) }
        ReminderPrefs.saveScheduledIds(context, reminders.mapTo(mutableSetOf()) { it.id })
    }
}
