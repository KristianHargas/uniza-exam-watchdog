package com.tinko.unizaexamwatchdog.repository

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tinko.unizaexamwatchdog.domain.Term
import com.tinko.unizaexamwatchdog.network.AuthRes
import com.tinko.unizaexamwatchdog.network.AuthService
import com.tinko.unizaexamwatchdog.network.SESSION_COOKIE_NAME
import com.tinko.unizaexamwatchdog.network.getAuthService
import com.tinko.unizaexamwatchdog.preferences.UserPreferences
import com.tinko.unizaexamwatchdog.util.SingletonHolder
import retrofit2.Response

enum class AuthenticationState {
    UNAUTHENTICATED,
    AUTHENTICATING,
    AUTHENTICATED,
    INVALID_AUTHENTICATION,
    NETWORK_ERROR
}

class UserRepository private constructor(context: Context) {

    private val authService: AuthService by lazy { getAuthService() }
    private val preferences: UserPreferences = UserPreferences(context)

    private val _authState = MutableLiveData<AuthenticationState>()
    val authState : LiveData<AuthenticationState>
        get() = _authState

    private val _term = MutableLiveData<Term>()
    val term : LiveData<Term>
        get() = _term

    init {
        _authState.value = when(preferences.getUsername()) {
            null -> AuthenticationState.UNAUTHENTICATED
            else -> AuthenticationState.AUTHENTICATED
        }

        _term.value = preferences.getTerm()
    }

    fun saveTerm(term: Term) {
        preferences.saveTerm(term)
        _term.value = term
    }

    fun getSessionCookie(): String? = preferences.getSessionCookie()

    fun loginCancelled() {
        _authState.value = AuthenticationState.UNAUTHENTICATED
    }

    suspend fun refreshSessionFromApp(): Boolean {
        return UserRepository.refreshSession(preferences, authService)
    }

    suspend fun login(username: String, password: String) {
        try {
            _authState.value = AuthenticationState.AUTHENTICATING
            // calling authentication service
            val res: Response<AuthRes> = authService.login(username, password)

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
                    preferences.saveUserData(username, password, sessionCookie)

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

    companion object : SingletonHolder<UserRepository, Context>(::UserRepository) {

        suspend fun refreshSessionFromBackgroundAndGetCookie(context: Context): String? {
            val preferences = UserPreferences(context)
            val authService = getAuthService()

            return if (UserRepository.refreshSession(preferences, authService)) preferences.getSessionCookie() else null
        }

        suspend fun refreshSession(preferences: UserPreferences, authService: AuthService): Boolean {
            if (preferences.getSessionCookie() != null) {
                return try {
                    val res: Response<AuthRes> = authService.refresh(
                        preferences.getUsername()!!,
                        preferences.getPassword()!!,
                        preferences.getSessionCookie()!!
                    )

                    res.body()?.logged ?: false
                } catch (e: Throwable) {
                    false
                }
            }

            return false
        }
    }
}