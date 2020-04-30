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

enum class AuthenticationState {
    UNAUTHENTICATED,
    AUTHENTICATING,
    AUTHENTICATED,
    INVALID_AUTHENTICATION,
    NETWORK_ERROR
}

class UserRepository private constructor(application: Application) {
    private val authService: AuthService by lazy { getAuthService() }

    private val _authState = MutableLiveData<AuthenticationState>()
    val authState : LiveData<AuthenticationState>
        get() = _authState

    init {
        Log.i("UserRepository", "init")
        _authState.value = AuthenticationState.UNAUTHENTICATED
    }

    suspend fun login(name: String, password: String) {
        try {
            _authState.value = AuthenticationState.AUTHENTICATING
            // calling authentication service
            val res: Response<AuthRes> = authService.login(name, password)

            // Server always returns 200
            if (res.code() == 200) {
                val loggedSuccessful = res.body()?.logged ?: throw Exception("Missing response body!")
                // Correct credentials
                if (loggedSuccessful) {
                    // returns list of all cookies
                    val cookies = res.raw().headers("Set-Cookie")

                    var sessionCookieStr: String = cookies.firstOrNull {
                        it.contains(SESSION_COOKIE_NAME)
                    } ?: throw Exception("Missing session cookie!")

                    val start = sessionCookieStr.indexOf(SESSION_COOKIE_NAME)
                    // extracted cookie which will be saved along with name and password in encrypted shared prefs
                    val sessionCookie = sessionCookieStr.substring(start, sessionCookieStr.indexOf(';', start))

                    // save
                    Log.i("UserRepository", sessionCookie)

                    _authState.value = AuthenticationState.AUTHENTICATED
                } else {
                    _authState.value = AuthenticationState.INVALID_AUTHENTICATION
                }
            } else {
                // Should not happen, but what if..
                throw Exception("Server sent unexpected response code!")
            }
        } catch (e: Throwable) {
            Log.e("UserRepository", e.message)
            _authState.value = AuthenticationState.NETWORK_ERROR
        }
    }

    companion object : SingletonHolder<UserRepository, Application>(::UserRepository)
}