package com.example.android_practice_dpo.main.api

import com.example.android_practice_dpo.main.data.LikedPhoto
import com.example.android_practice_dpo.main.data.Photo
import com.example.android_practice_dpo.main.data.PhotoCollection
import com.example.android_practice_dpo.main.data.PhotoDescription
import com.example.android_practice_dpo.main.data.SearchResults
import com.example.android_practice_dpo.main.data.UnsplashUser
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("search/photos")
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