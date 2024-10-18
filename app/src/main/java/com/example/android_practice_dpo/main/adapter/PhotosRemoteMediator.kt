package com.example.android_practice_dpo.main.adapter

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.android_practice_dpo.main.api.Repository
import com.example.android_practice_dpo.main.utils.toPhotoEntity
import com.example.android_practice_dpo.main.data.PhotoDatabase
import com.example.android_practice_dpo.main.data.PhotoEntity
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PhotosRemoteMediator(
    private val repository: Repository,
    private val photosDao: PhotoDatabase,
) : RemoteMediator<Int, PhotoEntity>() {

    private var pageIndex = 1

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PhotoEntity>
    ): MediatorResult {

        pageIndex =
            getPageIndex(loadType) ?: return MediatorResult.Success(endOfPaginationReached = true)

        return try {
            val photos = repository.getPagedPhotos(page = pageIndex)
            photosDao.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    photosDao.photoDao().clearDb()
                }
                val photoEntity = photos.map { it.toPhotoEntity() }
                photosDao.photoDao().upsertPhotos(photoEntity)
            }
            MediatorResult.Success(endOfPaginationReached = photos.isEmpty())
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }

    private fun getPageIndex(loadType: LoadType): Int? {
        pageIndex = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return null
            LoadType.APPEND -> ++pageIndex
        }
        return pageIndex
    }
}