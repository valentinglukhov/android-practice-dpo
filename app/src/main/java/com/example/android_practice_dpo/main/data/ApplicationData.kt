package com.example.android_practice_dpo.main.data

@kotlinx.serialization.Serializable
data class ApplicationData(
    val access_key: String = "YzLOa6JhN7sScSdchA7Fmctj2JsLp8yo3RQ6IbZukSY",
    val secret_key: String = "6YUjreMyxn9Fi2ct5LvEwsCb_tygXPCat-7XhyqDGVY",
    val authorizationCode: String? = null,
    val access_token: String? = null,
    val token_type: String? = null,
    val refresh_token: String? = null,
    val scope: String? = null,
    val created_at: Long? = null,
) {
}