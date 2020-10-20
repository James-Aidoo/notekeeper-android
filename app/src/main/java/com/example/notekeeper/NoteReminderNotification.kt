package com.example.notekeeper

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * Helper class for showing and canceling note reminder
 * notifications.
 *
 *
 * This class makes heavy use of the [NotificationCompat.Builder] helper
 * class to create notifications in a backward-compatible way.
 */
object NoteReminderNotification {
    /**
     * The unique identifier for this type of notification.
     */
    private val NOTIFICATION_TAG = "NoteReminder"

    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     *
     *
     * TODO: Customize this method's arguments to present relevant content in
     * the notification.
     *
     *
     * TODO: Customize the contents of this method to tweak the behavior and
     * presentation of note reminder notifications. Make
     * sure to follow the
     * [
 * Notification design guidelines](https://developer.android.com/design/patterns/notifications.html) when doing so.
     *
     * @see .cancel
     */
    fun notify(
        context: Context,
        noteTitle: String,
        noteText: String,
        noteId: Int
    ) {
        val res = context.resources

        val noteActivityIntent = Intent(context, NoteActivity::class.java)
        noteActivityIntent.putExtra(NOTE_ID, noteId)

        val backupIntent = Intent(context, NoteBackupService::class.java)
        backupIntent.putExtra(EXTRA_COURSE_ID, NoteBackup.ALL_COURSES)

        // This image is used as the notification's large icon (thumbnail).
        // TODO: Remove this if your notification has no relevant thumbnail.
        val picture = BitmapFactory.decodeResource(res, R.drawable.logo)

        val builder = NotificationCompat.Builder(context)

            // Set appropriate defaults for the notification light, sound,
            // and vibration.
            .setDefaults(Notification.DEFAULT_ALL)

            // Set required fields, including the small icon, the
            // notification title, and text.
            .setSmallIcon(R.drawable.ic_stat_note_reminder)
            .setContentTitle("Review Note")
            .setContentText(noteText)

            // All fields below this line are optional.

            // Use a default priority (recognized on devices running Android
            // 4.1 or later)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            // Provide a large icon, shown with the notification in the
            // notification drawer on devices running Android 3.0 or later.
            .setLargeIcon(picture)

            // Set ticker text (preview) information for this notification.
            .setTicker("Review Note")

            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(noteText)
                .setBigContentTitle(noteTitle)
                .setSummaryText("Review Note"))

            // Show a number. This is useful when stacking notifications of
            // a single type.
//            .setNumber(number)

            // If this notification relates to a past or upcoming event, you
            // should set the relevant time information using the setWhen
            // method below. If this call is omitted, the notification's
            // timestamp will by set to the time at which it was shown.
            // TODO: Call setWhen if this notification relates to a past or
            // upcoming event. The sole argument to this method should be
            // the notification timestamp in milliseconds.
            //.setWhen(...)

            // Set the pending intent to be initiated when the user touches
            // the notification.
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    noteActivityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )

            .addAction(
                0,
                "View All Notes",
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )

            .addAction(
                0,
                "Backup Notes",
                PendingIntent.getService(
                    context,
                    0,
                    backupIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )

            // Automatically dismiss the notification when it is touched.
            .setAutoCancel(true)

        notify(context, builder.build())
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private fun notify(context: Context, notification: Notification) {
        val nm = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(NOTIFICATION_TAG, 0, notification)
        } else {
            nm.notify(NOTIFICATION_TAG.hashCode(), notification)
        }
    }

    /**
     * Cancels any notifications of this type previously shown using
     * [.notify].
     */
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    fun cancel(context: Context) {
        val nm = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(NOTIFICATION_TAG, 0)
        } else {
            nm.cancel(NOTIFICATION_TAG.hashCode())
        }
    }
}
