package com.tinko.unizaexamwatchdog.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.work.WorkInfo
import com.tinko.unizaexamwatchdog.domain.Subject
import com.tinko.unizaexamwatchdog.domain.Term
import com.tinko.unizaexamwatchdog.repository.ExamRepository
import com.tinko.unizaexamwatchdog.repository.SubjectRepository
import com.tinko.unizaexamwatchdog.repository.UserRepository
import com.tinko.unizaexamwatchdog.work.setupWatchdogWorker
import com.tinko.unizaexamwatchdog.work.cancelWatchdogWorker
import com.tinko.unizaexamwatchdog.work.getWatchdogWorkerInfo
import kotlinx.coroutines.launch

/**
 * This view model is used by [MainScreenFragment].
 *
 * @constructor
 * Constructs a new view model.
 *
 * @param application application.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository = UserRepository.getInstance(application)
    private val subjectRepository: SubjectRepository by lazy {
        SubjectRepository.getInstance(
            application
        )
    }
    private val examsRepository: ExamRepository by lazy { ExamRepository.getInstance(application) }

    val userAuthenticated: LiveData<Boolean> = userRepository.authenticated
    val term: LiveData<Term> = userRepository.term
    val allSubjects: LiveData<List<Subject>> = subjectRepository.allSubjects

    val workerRunning: LiveData<Boolean> =
        Transformations.map(getWatchdogWorkerInfo(application)) { workerInfoList ->
            // check if there is some worker info and if it is not canceled
            workerInfoList?.filterNotNull()?.any { workInfo ->
                workInfo.state != WorkInfo.State.CANCELLED
            } ?: false
        }

    /**
     * This method logs out the user.
     */
    fun logout() = viewModelScope.launch {
        cancelWorker()
        examsRepository.deleteAllExams()
        subjectRepository.deleteAllSubjects()
        userRepository.logout()
    }

    /**
     * This method loads user's subjects.
     */
    fun loadSubjects() = viewModelScope.launch {
        subjectRepository.loadSubjects()
    }

    /**
     * This method is used to update subject's watch state.
     *
     * @param subject subject which watch state was updated.
     * @param watcherState new watch state.
     */
    fun updateSubjectWatchState(subject: Subject, watcherState: Boolean) = viewModelScope.launch {
        subjectRepository.updateSubjectWatchState(subject, watcherState)
    }

    /**
     * This method starts watchdog work manager.
     *
     */
    fun startWorker() {
        setupWatchdogWorker(getApplication())
    }

    /**
     * This method cancels watchdog work manager.
     */
    fun cancelWorker() {
        cancelWatchdogWorker(getApplication())
    }

    /**
     * This method is called when user selectes new term (winter/summer).
     *
     * @param term newly selected term.
     */
    fun termChanged(term: Term) = userRepository.saveSelectedTerm(term)

    /**
     * Factory class to construct new [MainViewModel].
     *
     * @property application application
     */
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