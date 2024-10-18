package com.example.android_practice_dpo.main.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchResults(
    @Json(name = "results") val results: List<Photo>
)

@JsonClass(generateAdapter = true)
data class TokenData(
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

data class Url(
    @Json(name = "raw") val raw: String,
    @Json(name = "full") val full: String,
    @Json(name = "regular") val regular: String,
    @Json(name = "small") val small: String
)

data class Links(
    @Json(name = "download") val download: String,
    @Json(name = "download_location") val downloadLocation: String,
)

data class User(
    @Json(name = "name") val name: String,
    @Json(name = "profile_image") val profileImage: ProfileImage
)

data class ProfileImage(
    @Json(name = "medium") val medium: String
)

@JsonClass(generateAdapter = true)
data class PhotoDescription(
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

data class UserDescription(
    @Json(name = "id") val id: String,
    @Json(name = "username") val username: String,
    @Json(name = "name") val name: String,
    @Json(name = "location") val location: String? = null,
)

data class Tag(
    @Json(name = "title") val title: String
)

data class Location(
    @Json(name = "city") val city: String? = "no data",
    @Json(name = "country") val country: String? = "no data",
    @Json(name = "position") val position: Position? = null
)

data class Position(
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null
)

data class Exif(
    @Json(name = "make") val make: String? = null,
    @Json(name = "model") val model: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "exposure_time") val exposureTime: String? = null,
    @Json(name = "aperture") val aperture: String? = null,
    @Json(name = "focal_length") val focalLength: String? = null,
    @Json(name = "iso") val iso: Int? = null,
)

@JsonClass(generateAdapter = true)
data class PhotoCollection(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "user") val user: User,
    @Json(name = "cover_photo") val coverPhoto: CoverPhoto,
)

data class CoverPhoto(
    @Json(name = "urls") val urls: Url,
    @Json(name = "likes") val likes: Int,
)

@JsonClass(generateAdapter = true)
data class UnsplashUser(
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

data class LikedPhoto(
    @Json(name = "photo") val photo: Info
)

data class Info(
    @Json(name = "id") val id: String,
    @Json(name = "likes") val likes: Int,
    @Json(name = "liked_by_user") val likedByUser: Boolean,
)