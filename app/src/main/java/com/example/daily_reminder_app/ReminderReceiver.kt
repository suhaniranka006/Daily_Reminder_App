/**
 * BROADCAST RECEIVER: Handles incoming broadcast messages and shows notifications
 * PURPOSE: Receives alarm triggers and displays reminder notifications to user
 * USAGE: Registered in AndroidManifest.xml to receive system and custom broadcasts
 */
package com.example.daily_reminder_app

// ANDROID FRAMEWORK IMPORTS
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * REMINDER RECEIVER CLASS: Extends BroadcastReceiver to handle broadcast intents
 * WHY: To receive and process alarm triggers from AlarmManager
 * HOW: Registered in manifest and triggered by PendingIntent from AlarmManager
 */
class ReminderReceiver : BroadcastReceiver() {

    /**
     * onReceive(): Called by Android system when a broadcast intent is received
     * @param context The application context (can be null in some cases)
     * @param intent The intent containing data and action information (can be null)
     * WHY: This is the entry point for all broadcast messages sent to this receiver
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        /**
         * EXTRACT MESSAGE: Get reminder message from intent extras
         * WHY: Intent may contain custom data sent from the alarm triggering code
         * HOW: Use safe null handling with elvis operator for default message
         */
        val message = intent?.getStringExtra("msg") ?: "Reminder!"

        /**
         * NULL SAFETY CHECK: Ensure context is not null before proceeding
         * WHY: Context is needed for notification system access, could be null in rare cases
         */
        if (context != null) {
            showNotification(context, message)
        }
    }

    /**
     * SHOW NOTIFICATION: Creates and displays a system notification
     * @param context The application context required for system services
     * @param message The reminder message to display in notification
     * WHY: To visually alert the user about the reminder
     * HOW: Uses NotificationCompat for backward compatibility
     */
    private fun showNotification(context: Context, message: String) {
        // UNIQUE CHANNEL ID: Identifier for this notification type
        // WHY: Android 8.0+ requires notifications to be categorized into channels
        val channelId = "reminder_channel"

        /**
         * NOTIFICATION MANAGER: System service for managing notifications
         * WHY: Needed to create channels and display notifications
         * HOW: Get system service using context
         */
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        /**
         * NOTIFICATION CHANNEL SETUP: Required for Android 8.0 (Oreo) and above
         * WHY: Android 8.0 introduced notification channels for better user control
         * HOW: Check SDK version and create channel only for compatible devices
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            /**
             * CREATE NOTIFICATION CHANNEL: Defines behavior and appearance for this notification type
             * @param channelId Unique identifier for the channel
             * @param name User-visible name shown in device settings
             * @param importance Importance level affecting sound and visibility
             */
            val channel = NotificationChannel(
                channelId,
                "Reminder Channel",
                NotificationManager.IMPORTANCE_HIGH  // Makes sound and appears as heads-up
            ).apply {
                description = "Channel for daily reminder notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        /**
         * NOTIFICATION BUILDER: Constructs the notification using builder pattern
         * WHY: NotificationCompat provides backward compatibility with older Android versions
         * HOW: Chain setter methods to configure notification properties
         */
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Reminder")  // Main title of the notification
            .setContentText(message)      // The actual reminder message from intent
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // Must be a valid drawable resource
            .setPriority(NotificationCompat.PRIORITY_HIGH)    // Important for pre-Oreo devices
            .setAutoCancel(true)  // Notification dismisses when user taps it
            .build()  // Finalize and build the notification object

        /**
         * DISPLAY NOTIFICATION: Show the notification to user
         * @param id Unique ID for this notification (same ID will update existing notification)
         * @param notification The built notification object to display
         * WHY: To actually make the notification appear in status bar
         */
        notificationManager.notify(1, notification)  // ID=1 for this reminder notification
    }
}