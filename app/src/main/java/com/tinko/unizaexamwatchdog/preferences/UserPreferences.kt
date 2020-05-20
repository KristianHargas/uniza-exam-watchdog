package com.tinko.unizaexamwatchdog.preferences

import android.app.Application
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.tinko.unizaexamwatchdog.domain.Term
import com.tinko.unizaexamwatchdog.domain.WINTER_TERM_STRING

class UserPreferences(application: Application) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences = EncryptedSharedPreferences.create(
        FILE_NAME,
        masterKeyAlias,
        application,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveUserData(username: String, password: String, sessionId: String) {
        sharedPreferences.edit()
            .putString(USERNAME_KEY, username)
            .putString(PASSWORD_KEY, password)
            .putString(SESSION_COOKIE_KEY, sessionId)
            .apply()
    }

    fun saveTerm(term: Term) {
        sharedPreferences.edit()
            .putString(TERM_KEY, term.toString())
            .apply()
    }

    fun getUsername(): String? = sharedPreferences.getString(USERNAME_KEY, null)
    fun getPassword(): String? = sharedPreferences.getString(PASSWORD_KEY, null)
    fun getSessionCookie(): String? = sharedPreferences.getString(SESSION_COOKIE_KEY, null)
    fun getTerm(): Term = if (sharedPreferences.getString(TERM_KEY, WINTER_TERM_STRING)!! == WINTER_TERM_STRING) Term.WINTER else Term.SUMMER

    companion object {
        private const val FILE_NAME = "user_preferences"
        private const val USERNAME_KEY = "username"
        private const val PASSWORD_KEY = "password"
        private const val SESSION_COOKIE_KEY = "session_cookie"
        private const val TERM_KEY = "term"
    }
}