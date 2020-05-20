package com.tinko.unizaexamwatchdog.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.tinko.unizaexamwatchdog.domain.Subject
import com.tinko.unizaexamwatchdog.domain.Term
import com.tinko.unizaexamwatchdog.repository.AuthenticationState
import com.tinko.unizaexamwatchdog.repository.SubjectRepository
import com.tinko.unizaexamwatchdog.repository.UserRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository = UserRepository.getInstance(application)
    private val subjectRepository : SubjectRepository by lazy { SubjectRepository.getInstance(application) }

    val authenticated: LiveData<AuthenticationState> = userRepository.authState
    val term: LiveData<Term> = userRepository.term
    val allSubjects: LiveData<List<Subject>> = subjectRepository.allSubjects

    fun loadSubjects() =  viewModelScope.launch {
        subjectRepository.loadSubjects()
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