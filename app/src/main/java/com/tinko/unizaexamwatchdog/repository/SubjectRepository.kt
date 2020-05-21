package com.tinko.unizaexamwatchdog.repository

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.tinko.unizaexamwatchdog.database.MyRoomDatabase
import com.tinko.unizaexamwatchdog.database.SubjectDao
import com.tinko.unizaexamwatchdog.database.asDomainModel
import com.tinko.unizaexamwatchdog.domain.Subject
import com.tinko.unizaexamwatchdog.domain.asDatabaseModel
import com.tinko.unizaexamwatchdog.network.scrapeSubjects
import com.tinko.unizaexamwatchdog.util.SingletonHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubjectRepository private constructor(private val application: Application) {

    private val userRepository: UserRepository by lazy { UserRepository.getInstance(application) }
    private val subjectDao: SubjectDao by lazy { MyRoomDatabase.getDatabase(application).subjectDao() }

    val allSubjects: LiveData<List<Subject>> = Transformations.map(subjectDao.getAllSubjects()) {
        it.asDomainModel()
    }

    suspend fun updateSubject(subject: Subject, watcherState: Boolean) = withContext(Dispatchers.IO) {
        // only when there is a change update db
        if (subject.watched != watcherState) {
            subject.watched = watcherState
            subjectDao.updateSubject(subject.asDatabaseModel())
        }
    }

    suspend fun loadSubjects() = withContext(Dispatchers.IO) {
        if (userRepository.authState.value == AuthenticationState.AUTHENTICATED) {
            val subjectCount: Int = subjectDao.getSubjectCount()
            // db is empty, lets scrape the web
            if (subjectCount == 0) {
                // refresh session in case it expired
                if (userRepository.refreshSession()) {
                    val subjects: List<Subject> = scrapeSubjects(userRepository.getSessionCookie()!!)
                    subjectDao.insertAll(subjects.asDatabaseModel())
                }
            }
        }
    }

    companion object : SingletonHolder<SubjectRepository, Application>(::SubjectRepository)
}