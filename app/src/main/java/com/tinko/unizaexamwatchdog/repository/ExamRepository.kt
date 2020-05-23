package com.tinko.unizaexamwatchdog.repository

import android.content.Context
import com.tinko.unizaexamwatchdog.database.dao.ExamDao
import com.tinko.unizaexamwatchdog.database.MyRoomDatabase
import com.tinko.unizaexamwatchdog.database.dao.SubjectDao
import com.tinko.unizaexamwatchdog.database.entity.asDomainModel
import com.tinko.unizaexamwatchdog.domain.Exam
import com.tinko.unizaexamwatchdog.domain.Subject
import com.tinko.unizaexamwatchdog.domain.asDatabaseModel
import com.tinko.unizaexamwatchdog.network.scrapeExams
import com.tinko.unizaexamwatchdog.util.SingletonHolder
import kotlinx.coroutines.*
import java.util.*

interface ExamDiscoveryListener {
    fun newExamsDiscovered(subject: Subject, newExamsCount: Int)
}

class ExamRepository private constructor(private val context: Context) {

    private val subjectDao: SubjectDao by lazy { MyRoomDatabase.getDatabase(context).subjectDao() }
    private val examDao: ExamDao by lazy { MyRoomDatabase.getDatabase(context).examDao() }

    suspend fun getExamsForSubject(subjectId: String): List<Exam> {
        return examDao.getExamsForSubject(subjectId).asDomainModel()
    }

    suspend fun deleteAllExams() {
        examDao.deleteAllExams()
    }

    suspend fun checkExams(listener: ExamDiscoveryListener): Boolean = withContext(Dispatchers.IO) {
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

                // subject checked
                subjectDao.updateLastCheck(subject.id, Calendar.getInstance().time)

                // broadcast notification
                if (newExams.isNotEmpty()) {
                    listener.newExamsDiscovered(subject, newExams.size)
                    examDao.insertAll(newExams.asDatabaseModel())
                }

                if (newExams.isNotEmpty()) subject else null
            }
        }

        try {
            // await until all jobs are completed
            jobs.awaitAll()
            true
        } catch (e: Throwable) {
            false
        }
    }

    companion object : SingletonHolder<ExamRepository, Context>(::ExamRepository)
}
