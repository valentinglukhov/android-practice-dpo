package com.example.android_practice_dpo.main.adapter

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.android_practice_dpo.main.data.Photo
import com.example.android_practice_dpo.main.api.Repository
import retrofit2.HttpException

class PhotoCollectionPagingSource(
    private val repository: Repository,
    private val id: String?
) : PagingSource<Int, Photo>() {

    override fun getRefreshKey(state: PagingState<Int, Photo>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null
        return anchorPage.prevKey?.plus(1) ?: anchorPage.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
        val pageNumber = params.key ?: 1

        return try {
            val response = repository.getPagedCollectionsPhoto(id = id, page = pageNumber)
            if (response.isSuccessful) {
                val collections = response.body()
                val nextPageNumber = if (collections?.isEmpty()!!) null else pageNumber + 1
                val prevPageNumber = if (pageNumber > 1) pageNumber - 1 else null
                LoadResult.Page(collections, prevPageNumber, nextPageNumber)
            } else {
                LoadResult.Error(HttpException(response))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}