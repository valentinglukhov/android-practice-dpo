package com.example.android_practice_dpo.main.data

@kotlinx.serialization.Serializable
data class ApplicationData(
    val accessKey: String = "YzLOa6JhN7sScSdchA7Fmctj2JsLp8yo3RQ6IbZukSY",
    val secretKey: String = "6YUjreMyxn9Fi2ct5LvEwsCb_tygXPCat-7XhyqDGVY",
    val authorizationCode: String? = null,
    val accessToken: String? = null,
    val tokenType: String? = null,
    val refreshToken: String? = null,
    val scope: String? = null,
    val createdAt: Long? = null,
)