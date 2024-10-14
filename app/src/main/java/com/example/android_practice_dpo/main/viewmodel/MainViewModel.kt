package com.example.android_practice_dpo.main.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.android_practice_dpo.main.adapter.*
import com.example.android_practice_dpo.main.api.*
import com.example.android_practice_dpo.main.data.PhotoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    pager: Pager<Int, PhotoEntity>
) : ViewModel() {

    private val _errorMessageFlow = MutableStateFlow<String?>(null)
    val errorMessageFlow = _errorMessageFlow.asStateFlow()

    private val _photoDescriptionFlow = MutableStateFlow<PhotoDescription?>(null)
    val photoDescriptionFlow = _photoDescriptionFlow.asStateFlow()

    private val _userInfoFlow = MutableStateFlow<UnsplashUser?>(null)
    val userInfoFlow = _userInfoFlow.asStateFlow()

    private val _errorUserFlow = MutableStateFlow<String?>(null)
    val errorUserFlow = _errorUserFlow.asStateFlow()

    private val _likePhotoFlow = MutableStateFlow<Boolean?>(null)

    val photosPagingFlow = pager
        .flow
        .map { pagingData ->
            pagingData.map { it.toPhoto() }
        }
        .cachedIn(viewModelScope)

    val photoCollectionFlow: Flow<PagingData<PhotoCollection>> =
        Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = { CollectionsPagingSource(repository) })
            .flow
            .cachedIn(viewModelScope)

    var collectionsPhotoPagedSource: Flow<PagingData<Photo>>? = null

    fun refreshToken(token: String?) {
        repository.refreshToken(token)
    }

    fun getPagedCollectionsPhoto(id: String?) {
        collectionsPhotoPagedSource = Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = { PhotoCollectionPagingSource(repository = repository, id = id) })
            .flow
            .cachedIn(viewModelScope)
    }

    suspend fun downloadPhotoCounter(photoId: String) {
        repository.downloadPhotoCounter(photoId)
    }

    var searchPhotoPagedSource: Flow<PagingData<Photo>>? = null

    fun searchPhotosPagedSource(query: String) {
        Log.d("UNSPLASH_DEBUG", "viewmodel launched search $query")
        searchPhotoPagedSource = Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = { SearchPhotosPagingSource(repository = repository, query = query) })
            .flow
            .cachedIn(viewModelScope)
    }

    var likedPhotoPagedSource: Flow<PagingData<Photo>>? = null

    fun getLikedPhotoPagedSource(username: String) {
        likedPhotoPagedSource = Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = { LikedPhotosPagingSource(repository = repository, username = username) })
            .flow
            .cachedIn(viewModelScope)
    }

    fun clearCollectionsPhotoPagedSource() {
        collectionsPhotoPagedSource = null
    }

    fun clearState() {
        _photoDescriptionFlow.value = null
        _errorMessageFlow.value = null
        _errorUserFlow.value = null
    }

    suspend fun likeUnlikePhoto(photo: Photo, context: Context): Boolean {
        var isSuccess = false
        viewModelScope.launch(Dispatchers.IO) {
            if (checkForInternet(context)) {
                try {
                    if (photo.likedByUser) {
                        val response = repository.unlikePhoto(photo.id)
                        Log.d("UNSPLASH_DEBUG", "Viewmodel ${response.isSuccessful}")
                        if (response.isSuccessful && response.code() == 200) {
                            val updatedPhoto =
                                photo.copy(likedByUser = response.body()?.photo?.likedByUser!!, likes = response.body()?.photo?.likes!!)
                            repository.photoDataBase.photoDao()
                                .updatePhoto(updatedPhoto.toPhotoEntity())
                            Log.d(
                                "UNSPLASH_DEBUG",
                                "Viewmodel ${response.isSuccessful}______${response.code()}____${response.body()?.photo?.likedByUser}"
                            )
                            _likePhotoFlow.value = response.body()?.photo?.likedByUser
                            isSuccess = true
                        }
                    } else {
                        val response = repository.likePhoto(photo.id)
                        Log.d("UNSPLASH_DEBUG", "Viewmodel ${response.isSuccessful}")
                        if (response.isSuccessful && response.code() == 201) {
                            val updatedPhoto =
                                photo.copy(likedByUser = response.body()?.photo?.likedByUser!!, likes = response.body()?.photo?.likes!!)
                            repository.photoDataBase.photoDao()
                                .updatePhoto(updatedPhoto.toPhotoEntity())
                            Log.d(
                                "UNSPLASH_DEBUG",
                                "Viewmodel ${response.isSuccessful}______${response.code()}____${response.body()?.photo?.likedByUser}"
                            )
                            _likePhotoFlow.value = response.body()?.photo?.likedByUser
                            isSuccess = true
                        }
                    }
                } catch (e: Exception) {
                    clearState()
                    Log.d("UNSPLASH_DEBUG", "Viewmodel" + e.localizedMessage)
                    _errorMessageFlow.value = e.localizedMessage
                }
            } else {
                clearState()
                _errorMessageFlow.value = "Please, check your internet connection"
            }
        }.join()
        Log.d("UNSPLASH_DEBUG", "Viewmodel return$isSuccess")
        return isSuccess
    }

    fun getUserInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getUserInfo()
                Log.d("UNSPLASH_DEBUG", "Viewmodel ${response.isSuccessful}")
                if (response.isSuccessful) {
                    _userInfoFlow.value = response.body()
                }
            } catch (e: Exception) {
                Log.d("UNSPLASH_DEBUG", "Viewmodel" + e.localizedMessage)
                clearState()
                _errorUserFlow.value = e.localizedMessage
            }
        }
    }

    fun getPhotoDescription(
        id: String
    ) {
        Log.d("UNSPLASH_DEBUG", id)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repository.getPhotoDescription(id)
                Log.d("UNSPLASH_DEBUG", "Viewmodel ${response.isSuccessful}")
                if (response.isSuccessful) {
                    Log.d("UNSPLASH_DEBUG", "Viewmodel" + response.body()!!.user.name)
                    _photoDescriptionFlow.value = response.body()
                }
            } catch (e: Exception) {
                Log.d("UNSPLASH_DEBUG", "Viewmodel" + e.localizedMessage)
                clearState()
                _errorMessageFlow.value = e.localizedMessage
            }
        }
    }

    private fun checkForInternet(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }
}

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory @Inject constructor(private val repository: Repository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MainViewModel(repository, pagerFactory(repository = repository)) as T
}

@OptIn(ExperimentalPagingApi::class)
fun pagerFactory(
    repository: Repository
): Pager<Int, PhotoEntity> {
    return Pager(
        config = PagingConfig(pageSize = 10),
        pagingSourceFactory = { repository.photoDataBase.photoDao().pagingSourcePhoto() },
        remoteMediator = PhotosRemoteMediator(
            repository = repository,
            photosDao = repository.photoDataBase
        )
    )
}