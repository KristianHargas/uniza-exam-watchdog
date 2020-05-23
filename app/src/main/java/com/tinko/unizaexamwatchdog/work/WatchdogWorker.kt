package com.tinko.unizaexamwatchdog.work

import android.content.Context
import androidx.work.*
import com.tinko.unizaexamwatchdog.domain.Subject
import com.tinko.unizaexamwatchdog.repository.ExamDiscoveryListener
import com.tinko.unizaexamwatchdog.repository.ExamRepository
import com.tinko.unizaexamwatchdog.util.showWatchdogNotification
import java.util.concurrent.TimeUnit

private const val WATCHDOG_WORKER_NAME = "watchdog"

/**
 * Periodic unique work manager which checks for newly added exams for every watched subject.
 *
 * @property context application context.
 * @constructor
 * Constructs new work manager.
 *
 * @param params work manager parameters.
 */
class WatchdogWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // listener for newly added exams basically shows notification to the user
        val examDiscoveryListener: ExamDiscoveryListener = object : ExamDiscoveryListener {
            override fun newExamsDiscovered(subject: Subject, newExamsCount: Int) {
                showWatchdogNotification(context, subject, newExamsCount)
            }
        }

        // check watched subjects and return result
        return when (ExamRepository.getInstance(context).checkExams(examDiscoveryListener)) {
            true -> Result.success()
            else -> Result.failure()
        }
    }
}

/**
 * This function starts watchdog work manager.
 *
 * Work manager is firstly executed after 30 seconds after startup and then periodically every 15 minutes.
 *
 * @param applicationContext application context
 */
fun setupWatchdogWorker(applicationContext: Context) {
    // work manager is executed only when all of the following conditions are met.
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    // configuring timing
    val request = PeriodicWorkRequestBuilder<WatchdogWorker>(15, TimeUnit.MINUTES)
        .setInitialDelay(30, TimeUnit.SECONDS)
        .setConstraints(constraints)
        .build()

    // start work manager
    WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
        WATCHDOG_WORKER_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}

/**
 * This method is used for canceling enqueued watchdog work manager.
 *
 * @param applicationContext application context.
 */
fun cancelWatchdogWorker(applicationContext: Context) {
    WorkManager.getInstance(applicationContext).cancelUniqueWork(WATCHDOG_WORKER_NAME)
}

/**
 * This method is used for getting information about running watchdog work manager.
 *
 * @param context context.
 */
fun getWatchdogWorkerInfo(context: Context) =
    WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(WATCHDOG_WORKER_NAME)