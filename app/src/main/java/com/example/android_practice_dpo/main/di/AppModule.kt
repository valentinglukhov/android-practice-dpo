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
import com.example.android_practice_dpo.main.data.AccessToken
import com.example.android_practice_dpo.main.data.ApplicationDataStoreManager
import com.example.android_practice_dpo.main.data.PhotoDatabase
import com.example.android_practice_dpo.main.data.PhotoEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

private const val SETTINGS = "settings"
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun providesContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
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
    fun providesSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(SETTINGS, MODE_PRIVATE)
    }

    @Provides
    fun providesToken(): AccessToken = AccessToken(null)

    @Provides
    fun providesDataStoreManager(@ApplicationContext context: Context): ApplicationDataStoreManager {
        return ApplicationDataStoreManager(context)
    }

    @Provides
    fun providesWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

}