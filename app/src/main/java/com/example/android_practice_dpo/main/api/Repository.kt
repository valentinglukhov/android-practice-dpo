package com.example.android_practice_dpo.main.api

import android.util.Log
import com.example.android_practice_dpo.main.data.AccessToken
import com.example.android_practice_dpo.main.data.LikedPhoto
import com.example.android_practice_dpo.main.data.Photo
import com.example.android_practice_dpo.main.data.PhotoCollection
import com.example.android_practice_dpo.main.data.PhotoDatabase
import com.example.android_practice_dpo.main.data.PhotoDescription
import com.example.android_practice_dpo.main.data.SearchResults
import com.example.android_practice_dpo.main.data.TokenData
import com.example.android_practice_dpo.main.data.UnsplashUser
import com.example.android_practice_dpo.main.utils.token
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import javax.inject.Inject
import javax.inject.Singleton

private const val AUTHORIZATION_URL = "https://unsplash.com/"
private const val API_URL = "https://api.unsplash.com/"
private const val REDIRECT_URI = "app://open.my.app"
private const val GRANT_TYPE = "authorization_code"

@Singleton
class Repository @Inject constructor(
    private var accessToken: AccessToken?
) {
    @Inject
    lateinit var photoDataBase: PhotoDatabase
    fun refreshToken(token: AccessToken?) {
        accessToken = token
    }

    init {
        Log.d("UNSPLASH_DEBUG", "repository init")
    }

    suspend fun searchPhotos(
        query: String,
        page: Int,
    ): Response<SearchResults> {
        val header = accessToken!!.token()
        Log.d("UNSPLASH_DEBUG", "repository Search $header")
        return unsplashGetData.searchPhoto(query = query, page = page, perPage = 40, accessToken = header)
    }

    suspend fun downloadPhotoCounter(
        id: String,
    ): Response<ResponseBody> {
        val header = "Bearer ${accessToken?.value}"
        Log.d("UNSPLASH_DEBUG", "repository like $header")
        return unsplashGetData.downloadPhotoCounter(id = id, accessToken = header)
    }

    suspend fun unlikePhoto(
        id: String,
    ): Response<LikedPhoto> {
        val header = "Bearer ${accessToken?.value}"
        Log.d("UNSPLASH_DEBUG", "repository unlike $header")
        return unsplashGetData.unlikePhoto(id = id, accessToken = header)
    }

    suspend fun likePhoto(
        id: String,
    ): Response<LikedPhoto> {
        val header = "Bearer ${accessToken?.value}"
        Log.d("UNSPLASH_DEBUG", "repository like $header")
        return unsplashGetData.likePhoto(id = id, accessToken = header)
    }

    suspend fun getUserInfo(): Response<UnsplashUser> {
        val header = "Bearer ${accessToken?.value}"
        Log.d("UNSPLASH_DEBUG", "repository UserInfo $header")
        return unsplashGetData.getUserInfo(accessToken = header)
    }

    suspend fun getPhotoDescription(
        id: String,
    ): Response<PhotoDescription> {
        val header = "Bearer ${accessToken?.value}"
        Log.d("UNSPLASH_DEBUG", "repository Description $header")
        return unsplashGetData.getPhotoDescription(id = id, accessToken = header)
    }

    suspend fun getLikedPhotos(
        page: Int,
        username: String,
    ): Response<List<Photo>> {
        val header = "Bearer ${accessToken?.value}"
        Log.d("UNSPLASH_DEBUG", "repository Liked Photos $header")
        return unsplashGetData.getLikedPhotos(username = username, page = page, accessToken = header)
    }

    suspend fun getPagedPhotos(
        page: Int,
    ): List<Photo> {
        val header = "Bearer ${accessToken?.value}"
        Log.d("UNSPLASH_DEBUG", "repository PagedPhotos $header")
        return unsplashGetData.getPagedPhotos(page = page, perPage = 40, accessToken = header)
    }

    suspend fun getPagedCollections(
        page: Int,
    ): Response<List<PhotoCollection>> {
        val header = "Bearer ${accessToken?.value}"
        Log.d("UNSPLASH_DEBUG", "repository PagedCollection $header")
        return unsplashGetData.getPagedCollections(
            page = page,
            perPage = 40,
            accessToken = header
        )
    }

    suspend fun getPagedCollectionsPhoto(
        id: String?,
        page: Int,
    ): Response<List<Photo>> {
        val header = "Bearer ${accessToken?.value}"
        Log.d("UNSPLASH_DEBUG", "repository PagedCollectionsPhoto $header")
        return unsplashGetData.getPagedCollectionsPhoto(
            id = id,
            page = page,
            perPage = 40,
            accessToken = header
        )
    }

    suspend fun getToken(
        clientId: String,
        clientSecret: String,
        code: String,
    ): Response<TokenData> {
        return unsplashAuthorization.getAuthorizationToken(
            clientId,
            clientSecret,
            REDIRECT_URI,
            code,
            GRANT_TYPE
        )
    }

    private val moshi =
        Moshi.Builder().addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()

    private val interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val httpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()


    private val unsplashAuthorization = Retrofit.Builder()
        .baseUrl(AUTHORIZATION_URL)
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(UnsplashAuthorization::class.java)

    private val unsplashGetData = Retrofit.Builder()
        .baseUrl(API_URL)
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(UnsplashApi::class.java)
}

interface UnsplashAuthorization {
    @POST("oauth/token")
    suspend fun getAuthorizationToken(
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
        @Query("redirect_uri") redirectUri: String,
        @Query("code") code: String,
        @Query("grant_type") grantType: String,
    ): Response<TokenData>
}

interface UnsplashApi {
    @GET("photos")
    suspend fun getPagedPhotos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 10,
        @Query("order_by") orderBy: String = "latest",
        @Header("Authorization") accessToken: String
    ): List<Photo>

    @GET("users/{username}/likes")
    suspend fun getLikedPhotos(
        @Path("username") username: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 40,
        @Header("Authorization") accessToken: String
    ): Response<List<Photo>>

    @GET ("search/photos")
    suspend fun searchPhoto(
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 40,
        @Header("Authorization") accessToken: String
    ): Response<SearchResults>

    @DELETE("photos/{id}/like")
    suspend fun unlikePhoto(
        @Path("id") id: String,
        @Header("Authorization") accessToken: String
    ): Response<LikedPhoto>

    @POST("photos/{id}/like")
    suspend fun likePhoto(
        @Path("id") id: String,
        @Header("Authorization") accessToken: String
    ): Response<LikedPhoto>

    @GET("photos/{id}/download")
    suspend fun downloadPhotoCounter(
        @Path("id") id: String,
        @Header("Authorization") accessToken: String
    ): Response<ResponseBody>

    @GET("me")
    suspend fun getUserInfo(
        @Header("Authorization") accessToken: String
    ): Response<UnsplashUser>

    @GET("photos/{id}")
    suspend fun getPhotoDescription(
        @Path("id") id: String,
        @Header("Authorization") accessToken: String
    ): Response<PhotoDescription>

    @GET("collections")
    suspend fun getPagedCollections(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 10,
        @Header("Authorization") accessToken: String
    ): Response<List<PhotoCollection>>

    @GET("collections/{id}/photos")
    suspend fun getPagedCollectionsPhoto(
        @Path("id") id: String?,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 10,
        @Header("Authorization") accessToken: String
    ): Response<List<Photo>>
}


