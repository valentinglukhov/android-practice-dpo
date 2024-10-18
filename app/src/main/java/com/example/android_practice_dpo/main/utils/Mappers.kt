package com.example.android_practice_dpo.main.utils

import com.example.android_practice_dpo.main.data.AccessToken
import com.example.android_practice_dpo.main.data.Exif
import com.example.android_practice_dpo.main.data.Links
import com.example.android_practice_dpo.main.data.Location
import com.example.android_practice_dpo.main.data.Photo
import com.example.android_practice_dpo.main.data.PhotoDescription
import com.example.android_practice_dpo.main.data.PhotoEntity
import com.example.android_practice_dpo.main.data.Position
import com.example.android_practice_dpo.main.data.ProfileImage
import com.example.android_practice_dpo.main.data.UnsplashUser
import com.example.android_practice_dpo.main.data.Url
import com.example.android_practice_dpo.main.data.User
import com.example.android_practice_dpo.main.data.UserDescription

fun AccessToken.token(): String {
    return "Bearer ${this.value}"
}

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