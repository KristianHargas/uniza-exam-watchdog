package com.tinko.unizaexamwatchdog.repository

import android.app.Application
import android.content.Context
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
import com.tinko.unizaexamwatchdog.util.SingletonHolder
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class ExamRepository private constructor(private val context: Context) {

    private val subjectDao: SubjectDao by lazy { MyRoomDatabase.getDatabase(context).subjectDao() }
    private val examDao: ExamDao by lazy { MyRoomDatabase.getDatabase(context).examDao() }

    suspend fun getExamsForSubject(subjectId: String): List<Exam> = withContext(Dispatchers.IO) {
        val exams = examDao.getExamsForSubject(subjectId)
        exams.asDomainModel()
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

                if (newExams.isNotEmpty()) subject else null
            }
        }

        try {
            // await until all jobs are completed
            val results: List<Subject?> = jobs.awaitAll()

            // send notifications
            results.filterNotNull().forEach {
                // notification -> new exams for given subject
                val notificationId: Int = it.id.substring(3, 6).toInt()
                showNotification(it.name, "Pridané nové termíny", context, notificationId)
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
    var builder = NotificationCompat.Builder(context, "WATCHDOG")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_MAX)

    with(NotificationManagerCompat.from(context)) {
        notify(id, builder.build())
    }
}