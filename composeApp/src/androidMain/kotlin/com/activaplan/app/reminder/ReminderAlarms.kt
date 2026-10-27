package com.activaplan.app.reminder

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.activaplan.app.domain.i18n.StringKey
import com.activaplan.app.domain.i18n.Strings
import com.activaplan.app.domain.i18n.languageFromTag
import com.activaplan.app.domain.i18n.systemLanguageTag
import com.activaplan.app.domain.reminder.Reminder
import com.activaplan.app.domain.reminder.nextOccurrence
import com.activaplan.app.domain.time.SystemLocalClock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/** Alarm dan notifikasi pengingat di Android. Tiap pengingat satu alarm sekali bunyi yang dipasang ulang saat berbunyi. */
internal object ReminderAlarms {
    private const val ACTION_REMIND = "com.activaplan.app.action.REMIND"
    private const val EXTRA_ID = "id"
    private const val EXTRA_DAY = "day"
    private const val EXTRA_TIME = "time"
    private const val EXTRA_TITLE = "title"
    private const val EXTRA_BODY = "body"
    const val CHANNEL_ID = "workout_reminders"

    private fun alarmManager(context: Context) = context.getSystemService(AlarmManager::class.java)

    private fun baseIntent(context: Context, id: String) = Intent(context, ReminderAlarmReceiver::class.java)
        .setAction(ACTION_REMIND)
        .setData(Uri.parse("activaplan://reminder/$id"))

    fun arm(context: Context, reminder: Reminder, now: LocalDateTime = SystemLocalClock.now()) {
        val intent = baseIntent(context, reminder.id)
            .putExtra(EXTRA_ID, reminder.id)
            .putExtra(EXTRA_DAY, reminder.dayOfWeek)
            .putExtra(EXTRA_TIME, reminder.timeMinutes)
            .putExtra(EXTRA_TITLE, reminder.title)
            .putExtra(EXTRA_BODY, reminder.body)
        val pending = PendingIntent.getBroadcast(
            context, reminder.id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val triggerAt = nextOccurrence(reminder.dayOfWeek, reminder.timeMinutes, now)
            .toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val manager = alarmManager(context)
        // Alarm tepat waktu butuh izin khusus (Android 12+); tanpanya jatuh ke alarm hemat daya yang bisa bergeser.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        } else {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    fun cancel(context: Context, id: String) {
        val pending = PendingIntent.getBroadcast(
            context, id.hashCode(), baseIntent(context, id), PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarmManager(context).cancel(pending)
        pending.cancel()
    }

    fun reminderFrom(intent: Intent): Reminder? {
        val id = intent.getStringExtra(EXTRA_ID) ?: return null
        return Reminder(
            id = id,
            dayOfWeek = intent.getIntExtra(EXTRA_DAY, 0).takeIf { it in 1..7 } ?: return null,
            timeMinutes = intent.getIntExtra(EXTRA_TIME, -1).takeIf { it in 0..1439 } ?: return null,
            title = intent.getStringExtra(EXTRA_TITLE).orEmpty(),
            body = intent.getStringExtra(EXTRA_BODY).orEmpty(),
        )
    }

    fun ensureChannel(context: Context) {
        val name = Strings(languageFromTag(systemLanguageTag()))[StringKey.REMINDER_CHANNEL]
        val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH)
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun show(context: Context, reminder: Reminder) {
        ensureChannel(context)
        val open = context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(context.applicationInfo.icon)
            .setContentTitle(reminder.title)
            .setContentText(reminder.body)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(reminder.id.hashCode(), notification)
    }
}
