package com.example.android_practice_dpo.main.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.room.Room
import androidx.work.WorkManager
import com.example.android_practice_dpo.main.adapter.PhotosRemoteMediator
import com.example.android_practice_dpo.main.api.Repository
import com.example.android_practice_dpo.main.api.UnsplashApi
import com.example.android_practice_dpo.main.api.UnsplashAuthorizationApi
import com.example.android_practice_dpo.main.data.AccessToken
import com.example.android_practice_dpo.main.data.ApplicationDataStoreManager
import com.example.android_practice_dpo.main.data.PhotoDatabase
import com.example.android_practice_dpo.main.data.PhotoEntity
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

private const val SETTINGS = "settings"
private const val API_URL = "https://api.unsplash.com/"
private const val AUTHORIZATION_URL = "https://unsplash.com/"
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun providesMoshi(): Moshi {
        return Moshi.Builder().addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    fun providesInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    fun providesOkHttpClient(interceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor(interceptor).build()
    }

    @Provides
    @Singleton
    fun providesUnsplashApi(moshi: Moshi, httpClient: OkHttpClient): UnsplashApi {
        return Retrofit.Builder()
            .baseUrl(API_URL)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(UnsplashApi::class.java)
    }

    @Provides
    @Singleton
    fun providesUnsplashAuthorizationApi(moshi: Moshi, httpClient: OkHttpClient): UnsplashAuthorizationApi {
        return Retrofit.Builder()
            .baseUrl(AUTHORIZATION_URL)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(UnsplashAuthorizationApi::class.java)
    }

    @Provides
    @Singleton
    fun providesContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun providesDataBase(@ApplicationContext context: Context): PhotoDatabase {
        return Room.databaseBuilder(
            context,
            PhotoDatabase::class.java,
            "photoDb.db"
        )
            .build()
    }

    @OptIn(ExperimentalPagingApi::class)
    @Provides
    fun providesPagerFactory(
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


    @Provides
    @Singleton
    fun providesSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(SETTINGS, MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providesToken(): AccessToken = AccessToken(null)

    @Provides
    @Singleton
    fun providesDataStoreManager(@ApplicationContext context: Context): ApplicationDataStoreManager {
        return ApplicationDataStoreManager(context)
    }

    @Provides
    @Singleton
    fun providesWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

}