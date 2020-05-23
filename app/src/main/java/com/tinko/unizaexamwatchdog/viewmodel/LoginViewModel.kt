package com.tinko.unizaexamwatchdog.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.tinko.unizaexamwatchdog.repository.AuthenticationState
import com.tinko.unizaexamwatchdog.repository.UserRepository
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

/**
 * This view model is used by [LoginFragment].
 *
 * @constructor
 * Constructs a new view model.
 *
 * @param application application.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository = UserRepository.getInstance(application)

    // two-way data binding with text inputs
    val username = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    val authState: LiveData<AuthenticationState> = userRepository.authState
    val authenticating: LiveData<Boolean> = Transformations.map(authState) {
        it == AuthenticationState.AUTHENTICATING
    }

    init {
        username.value = ""
        password.value = ""
    }

    /**
     * This method is called in reaction to login button click.
     */
    fun authenticate() {
        val enteredName: String = username.value ?: ""
        val enteredPassword: String = password.value ?: ""

        // authenticate user
        viewModelScope.launch {
            userRepository.login(enteredName, enteredPassword)
        }
    }

    /**
     * This method is called when user refuses to authenticate.
     */
    fun loginCancelled() {
        userRepository.loginCancelled()
    }

    /**
     * Factory class to construct new [MainViewModel].
     *
     * @property application application
     */
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