package com.example.android_practice_dpo.main.api

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "https://images.unsplash.com/"

interface FileDownloadApi {

    @GET("{path}")
    suspend fun downloadPhoto(@Path("path") path: String, @Query("query") query: String): Response<ResponseBody>



    companion object {
        private val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        private val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()
        val instance: FileDownloadApi by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .build()
                .create(FileDownloadApi::class.java)
        }
    }

}