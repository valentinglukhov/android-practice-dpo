package com.example.android_practice_dpo.main.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "rawImageUrl") val rawImageUrl: String,
    @ColumnInfo(name = "fullImageUrl") val fullImageUrl: String,
    @ColumnInfo(name = "regularImageUrl") val regularImageUrl: String,
    @ColumnInfo(name = "smallImageUrl") val smallImageUrl: String,
    @ColumnInfo(name = "downloadLink") val downloadLink: String,
    @ColumnInfo(name = "downloadLink_api") val downloadLink_api: String,
    @ColumnInfo(name = "likes") val likes: Int,
    @ColumnInfo(name = "liked_by_user") val likedByUser: Boolean,
    @ColumnInfo(name = "userName") val userName: String,
    @ColumnInfo(name = "userProfileImage") val userProfileImage: String
)
