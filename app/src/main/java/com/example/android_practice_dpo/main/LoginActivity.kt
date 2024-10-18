package com.example.android_practice_dpo.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android_practice_dpo.databinding.ActivityLoginBinding
import com.example.android_practice_dpo.main.api.Repository
import com.example.android_practice_dpo.main.data.TokenData
import com.example.android_practice_dpo.main.data.ApplicationDataStoreManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

private val REQUIRED_PERMISSIONS =
    arrayOf(
        "public",
        "read_user",
        "write_user",
        "read_photos",
        "write_photos",
        "write_likes",
        "write_followers",
        "read_collections",
        "write_collections"
    )
private const val ACCESS_KEY = "YzLOa6JhN7sScSdchA7Fmctj2JsLp8yo3RQ6IbZukSY"
private const val SECRET_KEY = "6YUjreMyxn9Fi2ct5LvEwsCb_tygXPCat-7XhyqDGVY"
private const val CODE_QUERY = "code"
private const val ERROR = "error"
private const val ERROR_DESCRIPTION = "error_description"
private const val CODE = "code"
private const val ACCESS_TOKEN = "access_token"

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    @Inject lateinit var repository: Repository
    @Inject lateinit var sharedPreferences: SharedPreferences

    @Inject lateinit var applicationDataStoreManager: ApplicationDataStoreManager
    private var getSharedAuthorizationCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = intent
        if (sharedPreferences.contains(CODE)) authorize(intent)
        if (intent.data != null) authorize(intent)

        binding.loginButton.setOnClickListener {
            if (checkForInternet(this)) {
                authorize(intent)
            } else {
                Toast.makeText(
                    this,
                    "Please, check your internet connection and click login button again =)",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun authorize(intent: Intent) {
        if(sharedPreferences.contains(ACCESS_TOKEN)) {
            loginMainActivityIntent()
        } else {
            if (sharedPreferences.contains(CODE)) {
                getSharedAuthorizationCode = sharedPreferences.getString(CODE, null)
                Log.d("UNSPLASH_DEBUG", "Получаем SHARED$getSharedAuthorizationCode")
            }

            Log.d("UNSPLASH_DEBUG", "Получаем интент$intent")
            Log.d("UNSPLASH_DEBUG", (intent.action == Intent.ACTION_MAIN).toString())
            Log.d("UNSPLASH_DEBUG", (getSharedAuthorizationCode == null).toString())
            if (intent.action == Intent.ACTION_MAIN && getSharedAuthorizationCode == null) {
                val outcomingIntent = Intent(Intent.ACTION_VIEW, composeUrl())
                startActivity(outcomingIntent)
            } else {
                runOnUiThread {
                    binding.loginButton.visibility = View.INVISIBLE
                    binding.message.text = ""
                }
                CoroutineScope(Dispatchers.IO).launch {
                    var authorizationCode: String? = null
                    val data = intent.data
                    if (data != null && data.queryParameterNames.contains(CODE_QUERY)) {
                        authorizationCode = data.getQueryParameter(CODE_QUERY)
                    } else {
                        if (getSharedAuthorizationCode != null) authorizationCode =
                            getSharedAuthorizationCode
                    }
                    if (authorizationCode != null) {
                        applicationDataStoreManager.saveAuthorizationCode(
                            authorizationCode
                        )
                        sharedPreferences.edit().putString(CODE, authorizationCode).apply()
                        var accessTokenResponse: Response<TokenData>? = null
                        try {
                            accessTokenResponse = repository.getToken(
                                ACCESS_KEY, SECRET_KEY,
                                authorizationCode
                            )
                            Log.d("UNSPLASH_DEBUG", "repo$accessTokenResponse")
                        } catch (e: HttpException) {
                            Log.d("UNSPLASH_DEBUG", "Ошибка:" + e.localizedMessage)
                        }
                        var accessToken: String? = null
                        if (accessTokenResponse?.isSuccessful == true && accessTokenResponse.code() == 200) {
                            Log.d(
                                "UNSPLASH_DEBUG",
                                "LoginActivity ЗАПРОС УДАЧНЫЙ" + accessTokenResponse.isSuccessful.toString()
                            )
                            accessToken = accessTokenResponse.body()?.accessToken
                            sharedPreferences.edit().putString(ACCESS_TOKEN, accessToken).apply()
                            Log.d(
                                "UNSPLASH_DEBUG",
                                "LoginActivity получили ТОКЕН" + accessToken.toString()
                            )
                        } else {
                            val outcomingIntent = Intent(Intent.ACTION_VIEW, composeUrl())
                            startActivity(outcomingIntent)
                        }
                        loginMainActivityIntent()
                    } else {
                        val intentData = intent.data ?: return@launch
                        if (intentData.queryParameterNames.contains(ERROR)
                            && intentData.queryParameterNames.contains(ERROR_DESCRIPTION)
                        ) {
                            val error = intentData.getQueryParameter(ERROR)
                            val errorDescription = intentData.getQueryParameter(ERROR_DESCRIPTION)
                            binding.message.text = "$error\n$errorDescription"
                            Toast.makeText(
                                this@LoginActivity,
                                error.toString(),
                                Toast.LENGTH_LONG
                            )
                                .show()
                            Log.d(
                                "UNSPLASH_DEBUG",
                                error.toString() + errorDescription.toString()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loginMainActivityIntent() {
        val accessToken = sharedPreferences.getString(ACCESS_TOKEN, null)
        Log.d("UNSPLASH_DEBUG", "Получаем TOKEN из Shared   $accessToken")
        val loginIntent = Intent(this@LoginActivity, MainActivity::class.java)
        loginIntent.putExtra(ACCESS_TOKEN, accessToken)
        startActivity(loginIntent)
    }

    private fun composeUrl(): Uri = Uri.parse("https://unsplash.com/oauth/authorize")
        .buildUpon()
        .appendQueryParameter("client_id", ACCESS_KEY)
        .appendQueryParameter(
            "redirect_uri",
            "app://open.my.app"
        )
        .appendQueryParameter("response_type", CODE_QUERY)
        .appendQueryParameter("scope", REQUIRED_PERMISSIONS.joinToString(" "))
        .build()

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