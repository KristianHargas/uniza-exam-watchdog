package com.tinko.unizaexamwatchdog.work

import android.content.Context
import androidx.work.*
import com.tinko.unizaexamwatchdog.domain.Subject
import com.tinko.unizaexamwatchdog.repository.ExamDiscoveryListener
import com.tinko.unizaexamwatchdog.repository.ExamRepository
import com.tinko.unizaexamwatchdog.util.showWatchdogNotification
import java.util.concurrent.TimeUnit

private const val WATCHDOG_WORKER_NAME = "watchdog"

class WatchdogWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val examDiscoveryListener: ExamDiscoveryListener = object : ExamDiscoveryListener {
            override fun newExamsDiscovered(subject: Subject, newExamsCount: Int) {
                showWatchdogNotification(context, subject, newExamsCount)
            }
        }

        return when(ExamRepository.getInstance(context).checkExams(examDiscoveryListener)) {
            true -> Result.success()
            else -> Result.failure()
        }
    }
}

fun setupWatchdogWorker(applicationContext: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    val request = PeriodicWorkRequestBuilder<WatchdogWorker>(15, TimeUnit.MINUTES)
        .setInitialDelay(30, TimeUnit.SECONDS)
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
        WATCHDOG_WORKER_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}

fun cancelWatchdogWorker(applicationContext: Context) {
    WorkManager.getInstance(applicationContext).cancelUniqueWork(WATCHDOG_WORKER_NAME)
}

fun getWatchdogWorkerInfo(context: Context) =
    WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(WATCHDOG_WORKER_NAME)