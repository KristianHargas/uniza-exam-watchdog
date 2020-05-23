package com.tinko.unizaexamwatchdog.network

import android.util.Log
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

data class AuthRes(val logged: Boolean)

interface AuthService {
    @FormUrlEncoded
    @POST("login.php")
    suspend fun login(
        @Field("meno") name: String,
        @Field("heslo") password: String
    ): Response<AuthRes>

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

fun getAuthService() = service