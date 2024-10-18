package com.example.android_practice_dpo.main.api

import com.example.android_practice_dpo.main.data.TokenData
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

interface UnsplashAuthorizationApi {
    @POST("oauth/token")
    suspend fun getAuthorizationToken(
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
        @Query("redirect_uri") redirectUri: String,
        @Query("code") code: String,
        @Query("grant_type") grantType: String,
    ): Response<TokenData>
}