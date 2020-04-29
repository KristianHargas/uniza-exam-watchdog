package com.tinko.unizaexamwatchdog.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.tinko.unizaexamwatchdog.repository.UserRepository
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepo : UserRepository = UserRepository.getInstance(application)

    val name = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val authenticated: LiveData<UserRepository.AuthState> = userRepo.authState

    init {
        name.value = ""
        password.value = ""

        Log.i("LoginViewModel", "init")
    }

    fun authenticate() {
        val enteredName: String = name.value ?: ""
        val enteredPassword: String = password.value ?: ""

        viewModelScope.launch {
            userRepo.login(enteredName, enteredPassword)
        }
    }

    override fun onCleared() {
        super.onCleared()

        Log.i("LoginViewModel", "onCleared")
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(application) as T
            }
            throw IllegalArgumentException("Unable to construct ViewModel!")
        }
    }
}