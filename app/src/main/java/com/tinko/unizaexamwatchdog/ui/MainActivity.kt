package com.tinko.unizaexamwatchdog.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.work.*
import com.tinko.unizaexamwatchdog.R
import com.tinko.unizaexamwatchdog.repository.showNotification
import com.tinko.unizaexamwatchdog.work.WatchdogWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private fun setupWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<WatchdogWorker>(20, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInitialDelay(5, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "watchdog",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Watchdog name"
            val descriptionText = "Watchdog desc"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("WATCHDOG", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupWorker()
        createNotificationChannel()
        // showNotification("Appka zapnutá", "Testíčková notifikácia...", applicationContext)

        val navController = findNavController(R.id.nav_host_fragment)
        // top level destinations
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.mainScreenFragment,
            R.id.loginFragment
        ))

        setSupportActionBar(findViewById(R.id.top_app_bar))
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp()
                || super.onSupportNavigateUp()
    }
}
