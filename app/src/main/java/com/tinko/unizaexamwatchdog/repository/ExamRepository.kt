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

/**
 * Listener interface which is invoked when new exams are detected for given subject.
 */
interface ExamDiscoveryListener {
    /**
     * This method is called when new exams are discovered.
     *
     * @param subject subject which exams were added.
     * @param newExamsCount count of the added exams.
     */
    fun newExamsDiscovered(subject: Subject, newExamsCount: Int)
}

/**
 * Repository class which manages all data and actions regarding exams.
 *
 * Creates a layer of abstraction for view model objects.
 *
 * @property context application context.
 */
class ExamRepository private constructor(private val context: Context) {

    private val subjectDao: SubjectDao by lazy { MyRoomDatabase.getDatabase(context).subjectDao() }
    private val examDao: ExamDao by lazy { MyRoomDatabase.getDatabase(context).examDao() }

    /**
     * This method is used to get all exams found so far for given subject.
     *
     * This call queries the local database and not the web.
     *
     * @param subjectId id of the subject which exams should be retrieved.
     * @return list of exams for given subject.
     */
    suspend fun getExamsForSubject(subjectId: String): List<Exam> {
        return examDao.getExamsForSubject(subjectId).asDomainModel()
    }

    /**
     * This method deletes all exams from local database.
     */
    suspend fun deleteAllExams() {
        examDao.deleteAllExams()
    }

    /**
     * This method checks the web and local database for newly added exams for all watched subjects.
     *
     * Should be called from background service or work manager periodically.
     *
     * @param listener listener object to call when there are new exams found.
     * @return true if the operation was successful, false otherwise.
     */
    suspend fun checkExams(listener: ExamDiscoveryListener): Boolean = withContext(Dispatchers.IO) {
        // get all of the watched subjects
        val watchedSubjects: List<Subject> = subjectDao.getWatchedSubjects().asDomainModel()
        // there is nothing to check
        if (watchedSubjects.isEmpty()) return@withContext true
        // refresh user session
        val sessionCookie: String = UserRepository.refreshSessionFromBackgroundAndGetCookie(context)
            ?: return@withContext false

        // parallel jobs each to check newly added exams on the web for every watched subject
        val jobs: List<Deferred<Subject?>> = watchedSubjects.map { subject ->
            async {
                // get locally stored exams for the current subject
                val databaseExams: List<Exam> =
                    examDao.getExamsForSubject(subject.id).asDomainModel()
                // get all exams from the web for the current subject
                val webExams: List<Exam> = scrapeExams(subject, sessionCookie)

                // filter newly added exams
                val newExams: List<Exam> = webExams.filter {
                    !databaseExams.contains(it)
                }

                // subject checked, update timestamp
                subjectDao.updateLastCheck(subject.id, Calendar.getInstance().time)

                // broadcast notification and save newly found exams to database
                if (newExams.isNotEmpty()) {
                    listener.newExamsDiscovered(subject, newExams.size)
                    examDao.insertAll(newExams.asDatabaseModel())
                }

                // if there are newly found exams for the current subject,
                // return subject as the result of this job, null otherwise
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
