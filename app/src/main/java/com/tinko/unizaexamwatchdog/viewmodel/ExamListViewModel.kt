package com.tinko.unizaexamwatchdog.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.tinko.unizaexamwatchdog.domain.Exam
import com.tinko.unizaexamwatchdog.repository.ExamRepository
import kotlinx.coroutines.launch

/**
 * This view model is used by [ExamListFragment].
 *
 * @constructor
 * Constructs a new view model.
 *
 * @param application application.
 */
class ExamListViewModel(application: Application) : AndroidViewModel(application) {

    private val examRepository: ExamRepository by lazy { ExamRepository.getInstance(application) }

    private val _exams = MutableLiveData<List<Exam>>(emptyList())
    val exams: LiveData<List<Exam>>
        get() = _exams

    /**
     * This method loads all of the exams for given subject.
     *
     * @param subjectId id of the subject.
     */
    fun loadExams(subjectId: String) = viewModelScope.launch {
        val exams = examRepository.getExamsForSubject(subjectId)
        _exams.value = exams
    }

    /**
     * Factory class to construct new [MainViewModel].
     *
     * @property application application
     */
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExamListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ExamListViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct ViewModel!")
        }
    }
}