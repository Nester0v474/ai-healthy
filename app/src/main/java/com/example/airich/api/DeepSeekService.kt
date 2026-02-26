package com.example.airich.api

import android.content.Context
import com.example.airich.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object DeepSeekService {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_BACKEND_HOST = "backend_url_override"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getBaseUrl(context: Context): String {
        val prefs = prefs(context)
        val override = prefs.getString(KEY_BACKEND_HOST, null)?.trim()
        return if (!override.isNullOrEmpty()) {
            val withScheme = if (override.startsWith("http")) override else "http://$override"
            val noTrail = withScheme.trimEnd('/')
            val withPort = if (noTrail.contains(":3000")) noTrail else "$noTrail:3000"
            "$withPort/"
        } else {
            BuildConfig.BACKEND_URL.let { url ->
                if (url.endsWith("/")) url else "$url/"
            }
        }
    }

    fun setBackendHost(context: Context, host: String?) {
        prefs(context).edit()
            .putString(KEY_BACKEND_HOST, host?.trim()?.takeIf { it.isNotEmpty() })
            .commit()
        cachedBaseUrl = null
        cachedApi = null
    }

    fun getBackendHost(context: Context): String? {
        return prefs(context).getString(KEY_BACKEND_HOST, null)?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun getDefaultHost(): String {
        val url = BuildConfig.BACKEND_URL.trimEnd('/')
        return when {
            url.contains("://") -> url.substringAfter("://").substringBefore("/").substringBefore(":")
            else -> url
        }
    }

    private var cachedBaseUrl: String? = null
    private var cachedApi: DeepSeekApi? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun buildRetrofit(baseUrl: String): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getApi(context: Context): DeepSeekApi {
        val url = getBaseUrl(context)
        if (url != cachedBaseUrl) {
            cachedBaseUrl = url
            cachedApi = buildRetrofit(url).create(DeepSeekApi::class.java)
        }
        return cachedApi!!
    }

    fun clearCache() {
        cachedBaseUrl = null
        cachedApi = null
    }

    fun checkServerReachable(context: Context): Boolean {
        return try {
            val url = getBaseUrl(context) + "health"
            val client = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (_: Exception) { false }
    }
}
