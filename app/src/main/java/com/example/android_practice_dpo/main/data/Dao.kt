package com.example.android_practice_dpo.main.data

import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface PhotoDao {

    @Update
    suspend fun updatePhoto(photoEntity: PhotoEntity)

    @Query("SELECT * FROM photos")
    fun pagingSourcePhoto(): PagingSource<Int, PhotoEntity>

    @Upsert
    suspend fun upsertPhotos(photoEntity: List<PhotoEntity>)

    @Query("DELETE FROM photos")
    suspend fun clearDb()
}