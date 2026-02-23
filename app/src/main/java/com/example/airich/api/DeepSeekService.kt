package com.example.airich.api

import android.content.Context
import com.example.airich.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Я настраиваю Retrofit для запросов к моему backend
object DeepSeekService {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_BACKEND_HOST = "backend_url_override"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Возвращает базовый URL: из настроек (IP в приложении) или из BuildConfig. */
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

    /** Сохранить IP/host сервера (например 192.168.0.105). Передать null — сбросить на значение по умолчанию. */
    fun setBackendHost(context: Context, host: String?) {
        prefs(context).edit()
            .putString(KEY_BACKEND_HOST, host?.trim()?.takeIf { it.isNotEmpty() })
            .commit() // commit() чтобы в release следующий запрос точно увидел новый IP
        cachedBaseUrl = null
        cachedApi = null
    }

    fun getBackendHost(context: Context): String? {
        return prefs(context).getString(KEY_BACKEND_HOST, null)?.trim()?.takeIf { it.isNotEmpty() }
    }

    /** Адрес по умолчанию из сборки (например 89.169.46.180). */
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

    /** API с учётом настроек (IP из приложения или из BuildConfig). */
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

    /** Проверяет, доступен ли сервер (GET /health). */
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
