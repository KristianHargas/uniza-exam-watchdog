package com.tinko.unizaexamwatchdog.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.tinko.unizaexamwatchdog.R
import com.tinko.unizaexamwatchdog.util.createWatchdogNotificationChannel

/**
 * This class represents main entry point of the application.
 *
 * This application follows one activity approach with multiple fragments handled by navigation component.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // create the notification channel
        createWatchdogNotificationChannel(applicationContext)

        val navController = findNavController(R.id.nav_host_fragment)
        // top level destinations
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mainScreenFragment,
                R.id.loginFragment
            )
        )

        // set custom toolbar as action bar
        setSupportActionBar(findViewById(R.id.top_app_bar))
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        // when the user clicks arrow up icon in child fragment, let the navigation controller handle that
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp()
                || super.onSupportNavigateUp()
    }
}
