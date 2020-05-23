package com.tinko.unizaexamwatchdog.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.work.WorkInfo
import com.tinko.unizaexamwatchdog.domain.Subject
import com.tinko.unizaexamwatchdog.domain.Term
import com.tinko.unizaexamwatchdog.repository.AuthenticationState
import com.tinko.unizaexamwatchdog.repository.ExamRepository
import com.tinko.unizaexamwatchdog.repository.SubjectRepository
import com.tinko.unizaexamwatchdog.repository.UserRepository
import com.tinko.unizaexamwatchdog.work.setupWatchdogWorker
import com.tinko.unizaexamwatchdog.work.cancelWatchdogWorker
import com.tinko.unizaexamwatchdog.work.getWatchdogWorkerInfo
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository = UserRepository.getInstance(application)
    private val subjectRepository : SubjectRepository by lazy { SubjectRepository.getInstance(application) }
    private val examsRepository: ExamRepository by lazy { ExamRepository.getInstance(application) }

    val userAuthState: LiveData<AuthenticationState> = userRepository.authState
    val userAuthenticated: LiveData<Boolean> = userRepository.authenticated
    val term: LiveData<Term> = userRepository.term
    val allSubjects: LiveData<List<Subject>> = subjectRepository.allSubjects

    // val workerState = WorkManager.getInstance(application).getWorkInfosForUniqueWorkLiveData("watchdog")
    val workerRunning: LiveData<Boolean> = Transformations.map(getWatchdogWorkerInfo(application)) { workerInfoList ->
        workerInfoList?.filterNotNull()?.any { workInfo ->
            workInfo.state != WorkInfo.State.CANCELLED
        } ?: false
    }

    fun logout() = viewModelScope.launch {
        stopWorker()
        examsRepository.deleteAllExams()
        subjectRepository.deleteAllSubjects()
        userRepository.logout()
    }

    fun loadSubjects() = viewModelScope.launch {
        subjectRepository.loadSubjects()
    }

    fun updateSubject(subject: Subject, watcherState: Boolean) = viewModelScope.launch {
        subjectRepository.updateSubjectWatchState(subject, watcherState)
    }

    fun startWorker() {
        setupWatchdogWorker(getApplication())
    }

    fun stopWorker() {
        cancelWatchdogWorker(getApplication())
    }

    fun termChanged(term: Term) = userRepository.saveSelectedTerm(term)

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct ViewModel!")
        }
    }
}