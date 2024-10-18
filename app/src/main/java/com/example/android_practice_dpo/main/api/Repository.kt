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
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

private const val REDIRECT_URI = "app://open.my.app"
private const val GRANT_TYPE = "authorization_code"

@Singleton
class Repository @Inject constructor(
    private var accessToken: AccessToken?,
    private val unsplashApi: UnsplashApi,
    private val unsplashAuthorizationApi: UnsplashAuthorizationApi
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
        return unsplashApi.searchPhoto(query = query, page = page, perPage = 40, accessToken = header)
    }

    suspend fun downloadPhotoCounter(
        id: String,
    ): Response<ResponseBody> {
        val header = accessToken!!.token()
        Log.d("UNSPLASH_DEBUG", "repository like $header")
        return unsplashApi.downloadPhotoCounter(id = id, accessToken = header)
    }

    suspend fun unlikePhoto(
        id: String,
    ): Response<LikedPhoto> {
        val header = accessToken!!.token()
        Log.d("UNSPLASH_DEBUG", "repository unlike $header")
        return unsplashApi.unlikePhoto(id = id, accessToken = header)
    }

    suspend fun likePhoto(
        id: String,
    ): Response<LikedPhoto> {
        val header = accessToken!!.token()
        Log.d("UNSPLASH_DEBUG", "repository like $header")
        return unsplashApi.likePhoto(id = id, accessToken = header)
    }

    suspend fun getUserInfo(): Response<UnsplashUser> {
        val header = accessToken!!.token()
        Log.d("UNSPLASH_DEBUG", "repository UserInfo $header")
        return unsplashApi.getUserInfo(accessToken = header)
    }

    suspend fun getPhotoDescription(
        id: String,
    ): Response<PhotoDescription> {
        val header = accessToken!!.token()
        Log.d("UNSPLASH_DEBUG", "repository Description $header")
        return unsplashApi.getPhotoDescription(id = id, accessToken = header)
    }

    suspend fun getLikedPhotos(
        page: Int,
        username: String,
    ): Response<List<Photo>> {
        val header = accessToken!!.token()
        Log.d("UNSPLASH_DEBUG", "repository Liked Photos $header")
        return unsplashApi.getLikedPhotos(username = username, page = page, accessToken = header)
    }

    suspend fun getPagedPhotos(
        page: Int,
    ): List<Photo> {
        val header = accessToken!!.token()
        Log.d("UNSPLASH_DEBUG", "repository PagedPhotos $header")
        return unsplashApi.getPagedPhotos(page = page, perPage = 40, accessToken = header)
    }

    suspend fun getPagedCollections(
        page: Int,
    ): Response<List<PhotoCollection>> {
        val header = accessToken!!.token()
        Log.d("UNSPLASH_DEBUG", "repository PagedCollection $header")
        return unsplashApi.getPagedCollections(
            page = page,
            perPage = 40,
            accessToken = header
        )
    }

    suspend fun getPagedCollectionsPhoto(
        id: String?,
        page: Int,
    ): Response<List<Photo>> {
        val header = accessToken!!.token()
        Log.d("UNSPLASH_DEBUG", "repository PagedCollectionsPhoto $header")
        return unsplashApi.getPagedCollectionsPhoto(
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
        return unsplashAuthorizationApi.getAuthorizationToken(
            clientId,
            clientSecret,
            REDIRECT_URI,
            code,
            GRANT_TYPE
        )
    }
}


