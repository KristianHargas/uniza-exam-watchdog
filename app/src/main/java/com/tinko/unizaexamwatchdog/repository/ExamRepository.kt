package com.tinko.unizaexamwatchdog.repository

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tinko.unizaexamwatchdog.R
import com.tinko.unizaexamwatchdog.database.ExamDao
import com.tinko.unizaexamwatchdog.database.MyRoomDatabase
import com.tinko.unizaexamwatchdog.database.SubjectDao
import com.tinko.unizaexamwatchdog.database.asDomainModel
import com.tinko.unizaexamwatchdog.domain.Exam
import com.tinko.unizaexamwatchdog.domain.Subject
import com.tinko.unizaexamwatchdog.domain.asDatabaseModel
import com.tinko.unizaexamwatchdog.network.scrapeExams
import com.tinko.unizaexamwatchdog.ui.MainActivity
import com.tinko.unizaexamwatchdog.util.SingletonHolder
import kotlinx.coroutines.*
import java.util.*
import kotlin.system.measureTimeMillis

class ExamRepository private constructor(private val context: Context) {

    private val subjectDao: SubjectDao by lazy { MyRoomDatabase.getDatabase(context).subjectDao() }
    private val examDao: ExamDao by lazy { MyRoomDatabase.getDatabase(context).examDao() }

    suspend fun getExamsForSubject(subjectId: String): List<Exam> = withContext(Dispatchers.IO) {
        val exams = examDao.getExamsForSubject(subjectId)
        exams.asDomainModel()
    }

    suspend fun deleteAllExams() {
        examDao.deleteAllExams()
    }

    suspend fun checkExams(): Boolean = withContext(Dispatchers.IO) {
        // get all of the watched subjects
        val watchedSubjects: List<Subject> = subjectDao.getWatchedSubjects().asDomainModel()
        if (watchedSubjects.isEmpty()) return@withContext true
        // refresh user session
        val sessionCookie: String = UserRepository.refreshSessionFromBackgroundAndGetCookie(context) ?: return@withContext false

        // parallel jobs to check newly added exams on the web for every watched subject
        val jobs: List<Deferred<Subject?>> = watchedSubjects.map {subject ->
            async {
                val databaseExams: List<Exam> = examDao.getExamsForSubject(subject.id).asDomainModel()
                val webExams: List<Exam> = scrapeExams(subject, sessionCookie)

                // find out newly added exams on the web
                val newExams: List<Exam> = webExams.filter {
                    !databaseExams.contains(it)
                }

                if (newExams.isNotEmpty())
                    examDao.insertAll(newExams.asDatabaseModel())

                // subject checked
                subjectDao.updateLastCheck(subject.id, Calendar.getInstance().time)

                if (newExams.isNotEmpty()) subject else null
            }
        }

        try {
            // await until all jobs are completed
            val results: List<Subject?> = jobs.awaitAll()

            // send notifications
            results.filterNotNull().forEach {
                // notification -> new exams for given subject
                val notificationId: Int = it.id.hashCode()
                showNotification(it.name, "Objavené nové skúškové termíny", context, notificationId)
            }
            true
        } catch (e: Throwable) {
            // error during parsing
            showNotification("Ajajaj", "Chyba pri checkovaní termínov, radšej prever všetky termíny", context, 10)
            false
        }

        false
    }

    companion object : SingletonHolder<ExamRepository, Context>(::ExamRepository)
}

fun showNotification(title: String, message: String, context: Context, id: Int) {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

    var builder = NotificationCompat.Builder(context, "WATCHDOG")
        .setSmallIcon(R.drawable.ic_watchdog_on)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setContentIntent(pendingIntent)
    //.setAutoCancel

    with(NotificationManagerCompat.from(context)) {
        notify(id, builder.build())
    }
}