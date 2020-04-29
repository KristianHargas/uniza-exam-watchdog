package com.tinko.unizaexamwatchdog.repository

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tinko.unizaexamwatchdog.network.AuthRes
import com.tinko.unizaexamwatchdog.network.AuthService
import com.tinko.unizaexamwatchdog.network.SESSION_COOKIE_NAME
import com.tinko.unizaexamwatchdog.network.getAuthService
import com.tinko.unizaexamwatchdog.util.SingletonHolder
import retrofit2.Response

class UserRepository private constructor(application: Application) {
    enum class AuthState {
        UNAUTHENTICATED,
        AUTHENTICATING,
        AUTHENTICATED,
        INVALID_AUTHENTICATION,
        NETWORK_ERROR
    }

    private val authService: AuthService = getAuthService()

    private val _authState = MutableLiveData<AuthState>()
    val authState : LiveData<AuthState>
        get() = _authState

    init {
        Log.i("UserRepository", "init")
        _authState.value = AuthState.UNAUTHENTICATED
    }

    suspend fun login(name: String, password: String) {
        try {
            _authState.value = AuthState.AUTHENTICATING
            val res: Response<AuthRes> = authService.login(name, password)

            // Server always returns 200
            if (res.code() == 200) {
                val loggedSuccessful = res.body()?.logged ?: throw Exception("Missing response body!")

                // Correct credentials
                if (loggedSuccessful) {
                    // returns string of all cookies
                    val cookies = res.raw().header("Set-Cookie") ?: ""

                    if (cookies.contains(SESSION_COOKIE_NAME)) {
                        val start = cookies.indexOf(SESSION_COOKIE_NAME)
                        val sessionCookie = cookies.subSequence(start, cookies.indexOf(';', start))

                        Log.i("UserRepository", cookies)
                        Log.i("UserRepository", sessionCookie.toString())
                    } else {
                        throw Exception("Missing session cookie!")
                    }

                    _authState.value = AuthState.AUTHENTICATED
                } else {
                    _authState.value = AuthState.INVALID_AUTHENTICATION
                }
            } else {
                // Should not happen, but what if..
                throw Exception("Server sent unexpected response code!")
            }
        } catch (e: Throwable) {
            Log.e("UserRepository", e.message)
            _authState.value = AuthState.NETWORK_ERROR
        }
    }

    companion object : SingletonHolder<UserRepository, Application>(::UserRepository)
}