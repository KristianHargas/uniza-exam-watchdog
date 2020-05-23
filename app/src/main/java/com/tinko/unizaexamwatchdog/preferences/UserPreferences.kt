package com.tinko.unizaexamwatchdog.preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.tinko.unizaexamwatchdog.domain.Term
import com.tinko.unizaexamwatchdog.domain.WINTER_TERM_STRING
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * This class holds user data and stores them in encrypted form using [EncryptedSharedPreferences].
 *
 * @constructor
 * Constructs new preferences object.
 *
 * @param context context.
 */
class UserPreferences(context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences = EncryptedSharedPreferences.create(
        FILE_NAME,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * This method saves user credentials.
     *
     * @param username username of the user.
     * @param password password of the user.
     * @param sessionId session identifier of the user.
     */
    fun saveCredentials(username: String, password: String, sessionId: String) {
        sharedPreferences.edit()
            .putString(USERNAME_KEY, username)
            .putString(PASSWORD_KEY, password)
            .putString(SESSION_COOKIE_KEY, sessionId)
            .apply()
    }

    /**
     * This method saves term (summer/winter) selected by the user.
     *
     * @param term selected term.
     */
    fun saveSelectedTerm(term: Term) {
        sharedPreferences.edit()
            .putString(TERM_KEY, term.toString())
            .apply()
    }

    /**
     * This method clears all data from preferences.
     */
    suspend fun clear() = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .putString(USERNAME_KEY, null)
            .putString(PASSWORD_KEY, null)
            .putString(SESSION_COOKIE_KEY, null)
            .putString(TERM_KEY, null)
            .apply()
    }

    /**
     * Method used to get username of the logged user.
     *
     * @return username of the the logged user.
     */
    fun getUsername(): String? = sharedPreferences.getString(USERNAME_KEY, null)

    /**
     * Method used to get password of the logged user.
     *
     * @return password of the logged user.
     */
    fun getPassword(): String? = sharedPreferences.getString(PASSWORD_KEY, null)

    /**
     * Method used to get session identifier cookie of the logged user.
     *
     * @return session cookie of the logged user.
     */
    fun getSessionCookie(): String? = sharedPreferences.getString(SESSION_COOKIE_KEY, null)

    /**
     * Method used to get term selected by the logged user.
     *
     * @return selected term of the logged user.
     */
    fun getTerm(): Term = if (sharedPreferences.getString(
            TERM_KEY,
            WINTER_TERM_STRING
        )!! == WINTER_TERM_STRING
    ) Term.WINTER else Term.SUMMER

    companion object {
        private const val FILE_NAME = "user_preferences"
        private const val USERNAME_KEY = "username"
        private const val PASSWORD_KEY = "password"
        private const val SESSION_COOKIE_KEY = "session_cookie"
        private const val TERM_KEY = "term"
    }
}