package com.tinko.unizaexamwatchdog.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

const val HEADER_COOKIE_NAME = "Cookie"
const val SESSION_COOKIE_NAME = "PHPSESSID"
private const val AUTH_SERVICE_URL = "https://vzdelavanie.uniza.sk/vzdelavanie/"

/**
 * Data class holding authentication response from the server.
 *
 * @property logged whether the user was successfully logged in or not.
 */
data class AuthRes(val logged: Boolean)

/**
 * Interface implemented by Retrofit 2 which manages authentication tasks.
 */
interface AuthService {
    /**
     * Method used for authentication.
     *
     * @param name username of the user.
     * @param password password of the user.
     * @return parsed response from the server.
     */
    @FormUrlEncoded
    @POST("login.php")
    suspend fun login(
        @Field("meno") name: String,
        @Field("heslo") password: String
    ): Response<AuthRes>

    /**
     * Method used for refreshing expired session.
     *
     * @param name username of the user.
     * @param password password of the user.
     * @param sessionCookie session cookie of the user which will be refreshed by the server.
     * @return parsed response from the server.
     */
    @FormUrlEncoded
    @POST("login.php")
    suspend fun refresh(
        @Field("meno") name: String,
        @Field("heslo") password: String,
        @Header(HEADER_COOKIE_NAME) sessionCookie: String
    ): Response<AuthRes>
}

private val service: AuthService by lazy {
    val retrofit = Retrofit.Builder()
        .baseUrl(AUTH_SERVICE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    retrofit.create(AuthService::class.java)
}

/**
 * Method used to get authentication service object.
 */
fun getAuthService() = service