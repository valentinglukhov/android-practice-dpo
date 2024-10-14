package com.example.android_practice_dpo.main.api

import android.content.Context
import android.util.Log
import com.example.android_practice_dpo.main.App
import com.example.android_practice_dpo.main.data.PhotoEntity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
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
    context: Context,
    private var accessToken: String? = null
) {
    fun refreshToken(token: String?) {
        accessToken = token
    }

    val photoDataBase = (context.applicationContext as App).photoDatabase

    init {
        Log.d("UNSPLASH_DEBUG", "repository init")
    }

    suspend fun searchPhotos(
        query: String,
        page: Int,
    ): Response<SearchResults> {
        val header = "Bearer $accessToken"
        Log.d("UNSPLASH_DEBUG", "repository Search $header")
        return unsplashGetData.searchPhoto(query = query, page = page, perPage = 40, accessToken = header)
    }

    suspend fun downloadPhotoCounter(
        id: String,
    ): Response<ResponseBody> {
        val header = "Bearer $accessToken"
        Log.d("UNSPLASH_DEBUG", "repository like $header")
        return unsplashGetData.downloadPhotoCounter(id = id, accessToken = header)
    }

    suspend fun unlikePhoto(
        id: String,
    ): Response<LikedPhoto> {
        val header = "Bearer $accessToken"
        Log.d("UNSPLASH_DEBUG", "repository unlike $header")
        return unsplashGetData.unlikePhoto(id = id, accessToken = header)
    }

    suspend fun likePhoto(
        id: String,
    ): Response<LikedPhoto> {
        val header = "Bearer $accessToken"
        Log.d("UNSPLASH_DEBUG", "repository like $header")
        return unsplashGetData.likePhoto(id = id, accessToken = header)
    }

    suspend fun getUserInfo(): Response<UnsplashUser> {
        val header = "Bearer $accessToken"
        Log.d("UNSPLASH_DEBUG", "repository UserInfo $header")
        return unsplashGetData.getUserInfo(accessToken = header)
    }

    suspend fun getPhotoDescription(
        id: String,
    ): Response<PhotoDescription> {
        val header = "Bearer $accessToken"
        Log.d("UNSPLASH_DEBUG", "repository Description $header")
        return unsplashGetData.getPhotoDescription(id = id, accessToken = header)
    }

    suspend fun getLikedPhotos(
        page: Int,
        username: String,
    ): Response<List<Photo>> {
        val header = "Bearer $accessToken"
        Log.d("UNSPLASH_DEBUG", "repository Liked Photos $header")
        return unsplashGetData.getLikedPhotos(username = username, page = page, accessToken = header)
    }

    suspend fun getPagedPhotos(
        page: Int,
    ): List<Photo> {
        val header = "Bearer $accessToken"
        Log.d("UNSPLASH_DEBUG", "repository PagedPhotos $header")
        return unsplashGetData.getPagedPhotos(page = page, perPage = 40, accessToken = header)
    }

    suspend fun getPagedCollections(
        page: Int,
    ): Response<List<PhotoCollection>> {
        val header = "Bearer $accessToken"
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
        val header = "Bearer $accessToken"
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

@JsonClass(generateAdapter = true)
class SearchResults(
    @Json(name = "results") val results: List<Photo>
)

@JsonClass(generateAdapter = true)
class TokenData(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "refresh_token") val refreshToken: String,
    @Json(name = "scope") val scope: String,
    @Json(name = "created_at") val createdAt: Long
)

@JsonClass(generateAdapter = true)
data class Photo(
    @Json(name = "id") val id: String,
    @Json(name = "description") val description: String? = "",
    @Json(name = "urls") val urls: Url,
    @Json(name = "links") val links: Links,
    @Json(name = "likes") var likes: Int,
    @Json(name = "liked_by_user") val likedByUser: Boolean,
    @Json(name = "user") val user: User,
)

class Url(
    @Json(name = "raw") val raw: String,
    @Json(name = "full") val full: String,
    @Json(name = "regular") val regular: String,
    @Json(name = "small") val small: String
)

class Links(
    @Json(name = "download") val download: String,
    @Json(name = "download_location") val downloadLocation: String,
)

class User(
    @Json(name = "name") val name: String,
    @Json(name = "profile_image") val profileImage: ProfileImage
)

class ProfileImage(
    @Json(name = "medium") val medium: String
)

@JsonClass(generateAdapter = true)
class PhotoDescription(
    @Json(name = "id") val id: String,
    @Json(name = "exif") val exif: Exif,
    @Json(name = "location") val location: Location,
    @Json(name = "tags") val tags: List<Tag>,
    @Json(name = "user") val user: UserDescription,
    @Json(name = "downloads") val downloads: Int,
    @Json(name = "likes") val likes: Int,
    @Json(name = "liked_by_user") val likedByUser: Boolean,
    @Json(name = "links") val links: Links,
    @Json(name = "urls") val urls: Url
)

class UserDescription(
    @Json(name = "id") val id: String,
    @Json(name = "username") val username: String,
    @Json(name = "name") val name: String,
    @Json(name = "location") val location: String? = null,
)

class Tag(
    @Json(name = "title") val title: String
)

class Location(
    @Json(name = "city") val city: String? = "no data",
    @Json(name = "country") val country: String? = "no data",
    @Json(name = "position") val position: Position? = null
)

class Position(
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null
)

class Exif(
    @Json(name = "make") val make: String? = null,
    @Json(name = "model") val model: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "exposure_time") val exposureTime: String? = null,
    @Json(name = "aperture") val aperture: String? = null,
    @Json(name = "focal_length") val focalLength: String? = null,
    @Json(name = "iso") val iso: Int? = null,
)

@JsonClass(generateAdapter = true)
class PhotoCollection(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "user") val user: User,
    @Json(name = "cover_photo") val coverPhoto: CoverPhoto,
)

class CoverPhoto(
    @Json(name = "urls") val urls: Url,
    @Json(name = "likes") val likes: Int,
)

@JsonClass(generateAdapter = true)
class UnsplashUser(
    @Json(name = "id") val id: String,
    @Json(name = "username") val username: String,
    @Json(name = "first_name") val firstName: String? = null,
    @Json(name = "last_name") val lastName: String? = null,
    @Json(name = "location") val location: String? = null,
    @Json(name = "total_likes") val totalLikes: Int? = null,
    @Json(name = "total_photos") val totalPhotos: Int? = null,
    @Json(name = "total_collections") val totalCollections: Int? = null,
    @Json(name = "downloads") val downloads: Int? = null,
)

class LikedPhoto(
    @Json(name = "photo") val photo: Info
)
class Info(
    @Json(name = "id") val id: String,
    @Json(name = "likes") val likes: Int,
    @Json(name = "liked_by_user") val likedByUser: Boolean,
)

fun UnsplashUser.toNonNull(): UnsplashUser {
    return UnsplashUser(
        id = id,
        username = username,
        firstName = firstName ?: "no data",
        lastName = lastName ?: "no data",
        location = location ?: "no data",
        totalLikes = totalLikes ?: 0,
        totalPhotos = totalPhotos ?: 0,
        totalCollections = totalCollections ?: 0,
        downloads = downloads ?: 0
    )
}


fun PhotoDescription.toNonNull(): PhotoDescription {
    return PhotoDescription(
        id = id,
        exif = Exif(
            make = exif.make ?: "no data",
            model = exif.model ?: "no data",
            name = exif.name ?: "no data",
            exposureTime = exif.exposureTime ?: "no data",
            aperture = exif.aperture ?: "no data",
            focalLength = exif.focalLength ?: "no data",
            iso = exif.iso
        ),
        location = Location(
            city = location.city ?: "no data",
            country = location.country ?: "no data",
            position = Position(
                latitude = location.position?.latitude ?: 0.0,
                longitude = location.position?.longitude ?: 0.0
            )
        ),
        tags = tags,
        user = UserDescription(user.id, user.username, user.name, location = user.location),
        downloads = downloads,
        likes = likes,
        likedByUser = likedByUser,
        links = Links(links.download, links.downloadLocation),
        urls = Url(raw = urls.raw, full = urls.full, regular = urls.regular, small = urls.small)
    )
}


fun Photo.toPhotoEntity(): PhotoEntity {
    return PhotoEntity(
        id = id,
        description = description,
        rawImageUrl = urls.raw,
        fullImageUrl = urls.full,
        regularImageUrl = urls.regular,
        smallImageUrl = urls.small,
        downloadLink = links.download,
        downloadLinkApi = links.downloadLocation,
        likes = likes,
        likedByUser = likedByUser,
        userName = user.name,
        userProfileImage = user.profileImage.medium
    )
}

fun PhotoEntity.toPhoto(): Photo {
    return Photo(
        id = id,
        description = description,
        urls = Url(rawImageUrl, fullImageUrl, regularImageUrl, smallImageUrl),
        links = Links(downloadLink, downloadLinkApi),
        likes = likes,
        likedByUser = likedByUser,
        user = User(
            userName,
            ProfileImage(userProfileImage)
        )
    )
}

