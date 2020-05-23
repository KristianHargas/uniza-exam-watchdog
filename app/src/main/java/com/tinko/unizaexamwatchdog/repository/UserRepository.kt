package com.tinko.unizaexamwatchdog.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.tinko.unizaexamwatchdog.domain.Term
import com.tinko.unizaexamwatchdog.network.AuthRes
import com.tinko.unizaexamwatchdog.network.AuthService
import com.tinko.unizaexamwatchdog.network.SESSION_COOKIE_NAME
import com.tinko.unizaexamwatchdog.network.getAuthService
import com.tinko.unizaexamwatchdog.preferences.UserPreferences
import com.tinko.unizaexamwatchdog.util.SingletonHolder
import retrofit2.Response

/**
 * Enum holding information regarding authentication state.
 *
 */
enum class AuthenticationState {
    UNAUTHENTICATED,
    AUTHENTICATING,
    AUTHENTICATED,
    INVALID_AUTHENTICATION,
    NETWORK_ERROR
}

/**
 * Repository class which manages all data and actions regarding user.
 *
 * Creates a layer of abstraction for view model objects.
 */
class UserRepository private constructor(context: Context) {

    private val authService: AuthService by lazy { getAuthService() }
    private val preferences: UserPreferences = UserPreferences(context)

    private val _authState = MutableLiveData<AuthenticationState>()
    val authState: LiveData<AuthenticationState>
        get() = _authState

    val authenticated: LiveData<Boolean> = Transformations.map(authState) {
        it == AuthenticationState.AUTHENTICATED
    }

    private val _term = MutableLiveData<Term>()
    val term: LiveData<Term>
        get() = _term

    init {
        // initialization based on saved data in preferences
        _authState.value = when (preferences.getUsername()) {
            null -> AuthenticationState.UNAUTHENTICATED
            else -> AuthenticationState.AUTHENTICATED
        }

        _term.value = preferences.getTerm()
    }

    /**
     * Method which stores newly selected term (summer/winter) by the user.
     *
     * @param term selected term.
     */
    fun saveSelectedTerm(term: Term) {
        preferences.saveSelectedTerm(term)
        _term.value = term
    }

    /**
     * Method used to get stored session cookie of the authenticated user.
     *
     * @return session cookie or null if no user is authenticated.
     */
    fun getSessionCookie(): String? = preferences.getSessionCookie()

    /**
     * Method used to refresh user's session.
     *
     * Should be called from running application, may save some performance.
     *
     * @return true if the session was successfully refreshed, false otherwise.
     */
    suspend fun refreshSessionFromApp(): Boolean {
        return refreshSession(preferences, authService)
    }

    /**
     * Method which logs out authenticated user.
     */
    suspend fun logout() {
        preferences.clear()
        _authState.value = AuthenticationState.UNAUTHENTICATED
    }

    /**
     * Called when the user cancels login process.
     */
    fun loginCancelled() {
        _authState.value = AuthenticationState.UNAUTHENTICATED
    }

    /**
     * This method is used to authenticate new user.
     *
     * @param username username of the user.
     * @param password password of the user.
     */
    suspend fun login(username: String, password: String) {
        try {
            _authState.value = AuthenticationState.AUTHENTICATING
            // calling authentication service
            val res: Response<AuthRes> = authService.login(username, password)

            // server always returns 200
            if (res.code() == 200) {
                val loggedSuccessful =
                    res.body()?.logged ?: throw Exception("Missing response body!")

                // correct credentials
                if (loggedSuccessful) {
                    // get list of all cookies
                    val cookies = res.raw().headers("Set-Cookie")

                    // we are interested only in session id cookie
                    var sessionCookieStr: String = cookies.firstOrNull {
                        it.contains(SESSION_COOKIE_NAME)
                    } ?: throw Exception("Missing session cookie!")

                    val start = sessionCookieStr.indexOf(SESSION_COOKIE_NAME)
                    // extracted cookie which will be saved along with name and password
                    val sessionCookie =
                        sessionCookieStr.substring(start, sessionCookieStr.indexOf(';', start))

                    // save the credentials
                    preferences.saveCredentials(username, password, sessionCookie)

                    _authState.value = AuthenticationState.AUTHENTICATED
                } else {
                    _authState.value = AuthenticationState.INVALID_AUTHENTICATION
                }
            } else {
                // should not happen, but what if..
                throw Exception("Server sent unexpected response code!")
            }
        } catch (e: Throwable) {
            _authState.value = AuthenticationState.NETWORK_ERROR
        }
    }

    companion object : SingletonHolder<UserRepository, Context>(::UserRepository) {

        /**
         * Static method used to refresh user's session from background, so even when the application is not running.
         *
         * @param context application context.
         * @return true if the session was successfully refreshed, false otherwise.
         */
        suspend fun refreshSessionFromBackgroundAndGetCookie(context: Context): String? {
            val preferences = UserPreferences(context)
            val authService = getAuthService()

            return if (refreshSession(
                    preferences,
                    authService
                )
            ) preferences.getSessionCookie() else null
        }

        /**
         * Helper method which tries to refresh users's session.
         *
         * @param preferences user preferences.
         * @param authService authentication service.
         * @return true if the session was successfully refreshed, false otherwise.
         */
        private suspend fun refreshSession(
            preferences: UserPreferences,
            authService: AuthService
        ): Boolean {
            // if the user is authenticated
            if (preferences.getSessionCookie() != null) {
                return try {
                    // try to refresh session
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