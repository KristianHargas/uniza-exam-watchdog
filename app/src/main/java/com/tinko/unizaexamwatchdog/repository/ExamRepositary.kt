package com.tinko.unizaexamwatchdog.repository

import android.app.Application
import android.util.Log
import com.tinko.unizaexamwatchdog.database.MyRoomDatabase
import com.tinko.unizaexamwatchdog.database.SubjectDao
import com.tinko.unizaexamwatchdog.database.asDomainModel
import com.tinko.unizaexamwatchdog.domain.Subject
import com.tinko.unizaexamwatchdog.network.scrapeExams
import com.tinko.unizaexamwatchdog.util.SingletonHolder
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class ExamRepository private constructor(private val application: Application) {

    private val userRepository: UserRepository by lazy { UserRepository.getInstance(application) }
    private val subjectDao: SubjectDao by lazy { MyRoomDatabase.getDatabase(application).subjectDao() }

    suspend fun loadExams() = withContext(Dispatchers.IO) {
        val time = measureTimeMillis {
            if (userRepository.refreshSession()) {
                val watchedSubjects: List<Subject> = subjectDao.getWatchedSubjects().asDomainModel()

                val jobs: List<Deferred<Boolean>> = watchedSubjects.map {subject ->
                    async {
                        Log.e("ExamRepository", "Checking subject: ${subject.name}")
                        val exams = scrapeExams(subject, userRepository.getSessionCookie()!!)
                        Log.i("ExamRepository", exams.toString())

                        true
                    }
                }

                try {
                    Log.e("ExamRepository", "Before awaitAll")
                    val results: List<Boolean> = jobs.awaitAll()
                    Log.e("ExamRepository", "After awaitAll")
                } catch (e: Throwable) {
                    Log.e("ExamRepository", "Exception throwed")
                }
            }
        }
        Log.e("ExamRepository", "Total time: ${time.toString()} ms")
    }

    companion object : SingletonHolder<ExamRepository, Application>(::ExamRepository)
}