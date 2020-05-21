package com.tinko.unizaexamwatchdog.repository

import android.app.Application
import android.util.Log
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

class ExamRepository private constructor(private val application: Application) {

    private val userRepository: UserRepository by lazy { UserRepository.getInstance(application) }
    private val subjectDao: SubjectDao by lazy { MyRoomDatabase.getDatabase(application).subjectDao() }
    private val examDao: ExamDao by lazy { MyRoomDatabase.getDatabase(application).examDao() }

    suspend fun loadExams() = withContext(Dispatchers.IO) {
        if (userRepository.refreshSession()) {
            val watchedSubjects: List<Subject> = subjectDao.getWatchedSubjects().asDomainModel()

            val jobs: List<Deferred<Subject?>> = watchedSubjects.map {subject ->
                async {
                    val databaseExams: List<Exam> = examDao.getExamsForSubject(subject.id).asDomainModel()
                    val webExams: List<Exam> = scrapeExams(subject, userRepository.getSessionCookie()!!)

                    // find out newly added exams on the web
                    val newExams: List<Exam> = webExams.filter {
                        !databaseExams.contains(it)
                    }

                    if (newExams.isNotEmpty())
                        examDao.insertAll(newExams.asDatabaseModel())

                    Log.i("ExamRepository", newExams.toString())

                    if (newExams.isNotEmpty()) subject else null
                }
            }

            try {
                Log.e("ExamRepository", "Before awaitAll")
                val results: List<Subject?> = jobs.awaitAll()
                results.filterNotNull().forEach {
                    Log.e("ExamRepository", "Update at subject: ${it.name}")
                }
                Log.e("ExamRepository", "After awaitAll")
            } catch (e: Throwable) {
                Log.e("ExamRepository", "Exception throwed")
            }
        }
    }

    companion object : SingletonHolder<ExamRepository, Application>(::ExamRepository)
}