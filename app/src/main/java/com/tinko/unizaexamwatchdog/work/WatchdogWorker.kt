package com.tinko.unizaexamwatchdog.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.*
import com.tinko.unizaexamwatchdog.repository.ExamRepository
import com.tinko.unizaexamwatchdog.repository.showNotification
import java.util.concurrent.TimeUnit

class WatchdogWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return when(ExamRepository.getInstance(context).checkExams()) {
            true -> Result.success()
            else -> Result.failure()
        }
    }
}

fun setupWorker(applicationContext: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    val request = PeriodicWorkRequestBuilder<WatchdogWorker>(15, TimeUnit.MINUTES)
        .setInitialDelay(30, TimeUnit.SECONDS)
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
        "watchdog",
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}

fun stopWorker(applicationContext: Context) {
    WorkManager.getInstance(applicationContext).cancelUniqueWork("watchdog")
}