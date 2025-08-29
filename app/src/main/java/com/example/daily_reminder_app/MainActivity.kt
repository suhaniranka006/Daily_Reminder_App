package com.example.daily_reminder_app

// ✅ Required imports for AlarmManager, Notifications, TimePicker, etc.
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    // 🔹 UI Button for setting reminder
    private lateinit var btnSetReminder: Button

    // 🔹 AlarmManager for scheduling reminders
    private lateinit var alarmManager: AlarmManager

    // 🔹 PendingIntent that will trigger the ReminderReceiver
    private lateinit var pendingIntent: PendingIntent

    // 🔹 Calendar object to hold selected reminder time
    private var calendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI initialization
        btnSetReminder = findViewById(R.id.btnSetReminder)

        // System service initialization (AlarmManager)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // ✅ Notification channel create karo (Oreo 8.0+ ke liye required)
        createNotificationChannel()

        // ✅ Android 13+ ke liye runtime notification permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // Agar permission allow nahi h to user se request karo
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // 🔹 Button click → time picker show karega
        btnSetReminder.setOnClickListener {
            showTimePicker()
        }
    }

    // ✅ Function: User ko ek Time Picker show karna
    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)  // 24-hour format picker
            .setHour(12) // Default hour
            .setMinute(0) // Default minute
            .setTitleText("Select Reminder Time") // Title for picker
            .build()

        // Dialog show karo
        picker.show(supportFragmentManager, "reminderPicker")

        // ✅ User ne time select kar liya → Calendar me set karo
        picker.addOnPositiveButtonClickListener {
            calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, picker.hour)
                set(Calendar.MINUTE, picker.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Reminder schedule karo
            setReminder(calendar.timeInMillis)
        }
    }

    // ✅ Function: AlarmManager se reminder set karna
    private fun setReminder(timeInMillis: Long) {
        // ReminderReceiver ko trigger karne ke liye Intent
        val intent = Intent(this, ReminderReceiver::class.java)

        // 🔹 OOP Polymorphism: Alag alag Reminder types
        val reminder: Reminder = MeetingReminder() // Ya MedicineReminder()
        intent.putExtra("msg", reminder.getReminderMessage())

        // PendingIntent jo system ko batata hai ki kaunse receiver ko call karna hai
        pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // ✅ Alarm schedule with exact time
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP, // Device wake karega
            timeInMillis, // Selected time
            pendingIntent
        )

        // Confirmation toast
        Toast.makeText(this, "Reminder Set!", Toast.LENGTH_SHORT).show()
    }

    // ✅ Function: Oreo+ ke liye Notification Channel create karna
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel", // Unique channel ID
                "Reminder Notifications", // Channel name (Settings me show hoga)
                NotificationManager.IMPORTANCE_HIGH // Importance level
            ).apply {
                description = "Channel for Reminder notifications"
            }

            // Channel ko system manager me register karo
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
