package com.tinko.unizaexamwatchdog.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.tinko.unizaexamwatchdog.database.MyRoomDatabase
import com.tinko.unizaexamwatchdog.database.dao.SubjectDao
import com.tinko.unizaexamwatchdog.database.entity.asDomainModel
import com.tinko.unizaexamwatchdog.domain.Subject
import com.tinko.unizaexamwatchdog.domain.asDatabaseModel
import com.tinko.unizaexamwatchdog.network.scrapeSubjects
import com.tinko.unizaexamwatchdog.util.SingletonHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository class which manages all data and actions regarding subjects.
 *
 * Creates a layer of abstraction for view model objects.
 *
 * @property context application context.
 */
class SubjectRepository private constructor(private val context: Context) {

    private val userRepository: UserRepository by lazy { UserRepository.getInstance(context) }
    private val subjectDao: SubjectDao by lazy { MyRoomDatabase.getDatabase(context).subjectDao() }

    val allSubjects: LiveData<List<Subject>> = Transformations.map(subjectDao.getAllSubjects()) {
        it.asDomainModel()
    }

    /**
     * This method updates watch state of the subject so whether this subject is checked by the watchdog or not.
     *
     * @param subject subject which is updated.
     * @param watchedState new watch state.
     */
    suspend fun updateSubjectWatchState(subject: Subject, watchedState: Boolean) =
        withContext(Dispatchers.IO) {
            subject.watched = watchedState
            subjectDao.updateSubject(subject.asDatabaseModel())
        }

    /**
     * This method loads data regarding subjects from the web if the local database is empty.
     */
    suspend fun loadSubjects() = withContext(Dispatchers.IO) {
        val subjectCount: Int = subjectDao.getSubjectCount()
        // db is empty, lets scrape the web
        if (subjectCount == 0) {
            // refresh session in case it expired
            if (userRepository.refreshSessionFromApp()) {
                val subjects: List<Subject> = scrapeSubjects(userRepository.getSessionCookie()!!)
                subjectDao.insertAll(subjects.asDatabaseModel())
            }
        }
    }

    /**
     * Method deletes all subjects from the database.
     */
    suspend fun deleteAllSubjects() {
        subjectDao.deleteAllSubjects()
    }

    companion object : SingletonHolder<SubjectRepository, Context>(::SubjectRepository)
}