package com.example.android_practice_dpo.main.data

import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object ApplicationDataSerializer : Serializer<ApplicationData> {
    override val defaultValue: ApplicationData
        get() = ApplicationData()

    override suspend fun readFrom(input: InputStream): ApplicationData {
        return try {
            Json.decodeFromString(
                deserializer = ApplicationData.serializer(),
                string = input.readBytes().toString()
            )
        } catch (e: SerializationException) {
            ApplicationData()
        }
    }

    override suspend fun writeTo(applicationData: ApplicationData, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(
                    serializer = ApplicationData.serializer(),
                    value = applicationData
                ).encodeToByteArray()
            )
        }
    }
}