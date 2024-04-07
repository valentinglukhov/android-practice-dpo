package com.example.android_practice_dpo.main.adapter

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.android_practice_dpo.main.api.Photo
import com.example.android_practice_dpo.main.api.Repository
import retrofit2.HttpException

class SearchPhotosPagingSource(
    private val repository: Repository,
    private val query: String,
) : PagingSource<Int, Photo>() {

    init {
        Log.d("UNSPLASH_DEBUG", "SearchPaging class init")
    }

    override fun getRefreshKey(state: PagingState<Int, Photo>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null
        return anchorPage.prevKey?.plus(1) ?: anchorPage.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
        val pageNumber = params.key ?: 1

        return try {
            Log.d("UNSPLASH_DEBUG", "try catch Paging")
            val response = repository.searchPhotos(query = query, page = pageNumber)
            if (response.isSuccessful) {
                val searchResults = response.body()
                val nextPageNumber = if (searchResults!!.results.isEmpty()) null else pageNumber + 1
                val prevPageNumber = if (pageNumber > 1) pageNumber - 1 else null
                LoadResult.Page(searchResults.results, prevPageNumber, nextPageNumber)
            } else {
                LoadResult.Error(HttpException(response))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}