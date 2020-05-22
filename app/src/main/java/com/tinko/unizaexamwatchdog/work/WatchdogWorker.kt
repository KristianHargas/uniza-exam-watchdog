package com.tinko.unizaexamwatchdog.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tinko.unizaexamwatchdog.repository.ExamRepository
import com.tinko.unizaexamwatchdog.repository.showNotification

class WatchdogWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return when(ExamRepository.getInstance(context).checkExams()) {
            true -> Result.success()
            else -> Result.failure()
        }
    }
}