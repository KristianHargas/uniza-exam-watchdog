package com.tinko.unizaexamwatchdog.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.tinko.unizaexamwatchdog.domain.Subject
import com.tinko.unizaexamwatchdog.domain.Term
import com.tinko.unizaexamwatchdog.repository.AuthenticationState
import com.tinko.unizaexamwatchdog.repository.ExamRepository
import com.tinko.unizaexamwatchdog.repository.SubjectRepository
import com.tinko.unizaexamwatchdog.repository.UserRepository
import com.tinko.unizaexamwatchdog.work.setupWorker
import com.tinko.unizaexamwatchdog.work.stopWorker
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository = UserRepository.getInstance(application)
    private val subjectRepository : SubjectRepository by lazy { SubjectRepository.getInstance(application) }
    private val examsRepository: ExamRepository by lazy { ExamRepository.getInstance(application) }

    val authenticated: LiveData<AuthenticationState> = userRepository.authState
    val term: LiveData<Term> = userRepository.term
    val allSubjects: LiveData<List<Subject>> = subjectRepository.allSubjects

    val workerState = WorkManager.getInstance(application).getWorkInfosForUniqueWorkLiveData("watchdog")
    val workerRunning: LiveData<Boolean> = Transformations.map(workerState) { workerInfoList ->
        workerInfoList?.filterNotNull()?.any {workInfo ->
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
        subjectRepository.updateSubject(subject, watcherState)
    }

    fun startWorker() {
        setupWorker(getApplication())
    }

    fun stopWorker() {
        stopWorker(getApplication())
    }

    fun termChanged(term: Term) = userRepository.saveTerm(term)

    override fun onCleared() {
        super.onCleared()

        Log.i(TAG, "onCleared")
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct ViewModel!")
        }
    }

    companion object {
        const val TAG = "MainViewModel"
    }
}