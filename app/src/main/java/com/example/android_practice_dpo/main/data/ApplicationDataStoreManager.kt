package com.example.android_practice_dpo.main.data

import android.content.Context
import androidx.datastore.dataStore

private const val APPLICATION_DATA = "application_data.json"
private val Context.applicationDataStore by dataStore(APPLICATION_DATA, ApplicationDataSerializer)

class ApplicationDataStoreManager(val context: Context) {

    suspend fun saveAuthorizationCode(authorisationCode: String) {
        context.applicationDataStore.updateData { currentApplicationData ->
            currentApplicationData.copy(authorizationCode = authorisationCode)
        }
    }

    suspend fun saveAccessToken(accessToken: String) {
        context.applicationDataStore.updateData { currentApplicationData ->
            currentApplicationData.copy(accessToken = accessToken)
        }
    }

    fun getApplicationData() = context.applicationDataStore.data
}