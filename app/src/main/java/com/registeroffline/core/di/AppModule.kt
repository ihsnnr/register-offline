package com.registeroffline.core.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.registeroffline.core.util.Constants
import com.registeroffline.core.local.dao.MemberDao
import com.registeroffline.core.local.db.AppDatabase
import com.registeroffline.core.network.AuthInterceptor
import com.registeroffline.core.util.TokenManager
import com.registeroffline.data.remote.api.ApiService
import com.registeroffline.data.repository.AuthRepositoryImpl
import com.registeroffline.data.repository.MemberRepositoryImpl
import com.registeroffline.domain.repository.AuthRepository
import com.registeroffline.domain.repository.MemberRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val TAG = "API_CALL"

    private val verboseLoggingInterceptor = Interceptor { chain ->
        val request = chain.request()
        val startNs = System.nanoTime()

        // ── Log Request ──
        Log.d(TAG, "╔══════════════════ REQUEST ══════════════════")
        Log.d(TAG, "║ ${request.method} ${request.url}")
        Log.d(TAG, "║ ── Headers ──")
        request.headers.forEach { (name, value) ->
            // Redact token untuk keamanan log
            val logValue = if (name.equals("Authorization", true)) {
                "${value.take(15)}...[REDACTED]"
            } else value
            Log.d(TAG, "║   $name: $logValue")
        }

        val body = request.body
        if (body != null) {
            Log.d(TAG, "║ ── Body ──")
            Log.d(TAG, "║   Content-Type: ${body.contentType()}")
            Log.d(TAG, "║   Content-Length: ${body.contentLength()}")

            when (body) {
                is MultipartBody -> {
                    Log.d(TAG, "║   Multipart parts: ${body.parts.size}")
                    body.parts.forEachIndexed { idx, part ->
                        val disposition = part.headers?.get("Content-Disposition") ?: "no-disposition"
                        val partContentType = part.body.contentType()
                        val partSize = part.body.contentLength()
                        Log.d(TAG, "║   [$idx] $disposition")
                        Log.d(TAG, "║       content-type: $partContentType | size: $partSize bytes")

                        // Untuk text part (bukan file), log isi value-nya
                        if (partContentType == null || partContentType.type == "text") {
                            try {
                                val buffer = Buffer()
                                part.body.writeTo(buffer)
                                val textContent = buffer.readString(Charset.forName("UTF-8"))
                                if (textContent.length < 500) {
                                    Log.d(TAG, "║       value: $textContent")
                                } else {
                                    Log.d(TAG, "║       value: ${textContent.take(500)}... [truncated]")
                                }
                            } catch (_: Exception) {
                                Log.d(TAG, "║       value: [unable to read]")
                            }
                        }
                    }
                }
                else -> {
                    // Buffer body utk log content JSON/form biasa
                    try {
                        val buffer = Buffer()
                        body.writeTo(buffer)
                        val charset = body.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
                        val bodyString = buffer.readString(charset)
                        if (bodyString.length < 2000) {
                            Log.d(TAG, "║   $bodyString")
                        } else {
                            Log.d(TAG, "║   ${bodyString.take(2000)}... [truncated, total=${bodyString.length}]")
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "║   [unable to read body: ${e.message}]")
                    }
                }
            }
        } else {
            Log.d(TAG, "║ ── Body: (empty) ──")
        }
        Log.d(TAG, "╚═════════════════════════════════════════════")

        // ── Execute ──
        val response: Response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            Log.e(TAG, "╔══════════════════ NETWORK FAILURE ══════════════════")
            Log.e(TAG, "║ ${request.method} ${request.url}")
            Log.e(TAG, "║ Error: ${e.javaClass.simpleName}: ${e.message}")
            Log.e(TAG, "╚═════════════════════════════════════════════════════")
            throw e
        }

        val tookMs = (System.nanoTime() - startNs) / 1_000_000

        // ── Log Response ──
        Log.d(TAG, "╔══════════════════ RESPONSE ══════════════════")
        Log.d(TAG, "║ ${response.code} ${response.message} (${tookMs}ms)")
        Log.d(TAG, "║ ${request.method} ${request.url}")

        val responseBody = response.body
        if (responseBody != null) {
            // Peek body (jangan consume, biar bisa dipakai Retrofit)
            val source = responseBody.source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer.clone()
            val charset = responseBody.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
            val bodyString = buffer.readString(charset)

            Log.d(TAG, "║ ── Body ──")
            if (bodyString.length < 3000) {
                // Split supaya Logcat ga truncate
                bodyString.chunked(1000).forEach { Log.d(TAG, "║ $it") }
            } else {
                bodyString.take(3000).chunked(1000).forEach { Log.d(TAG, "║ $it") }
                Log.d(TAG, "║ ... [truncated, total=${bodyString.length}]")
            }
        }
        Log.d(TAG, "╚═════════════════════════════════════════════")

        response
    }

    // ── Network ──
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        // HttpLoggingInterceptor lvl BASIC sbg fallback
        val basicLogging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(verboseLoggingInterceptor) // Verbose custom logger
            .addInterceptor(basicLogging)              // Basic OkHttp logger sbg backup
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    // ── Database ──
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "register_offline.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideMemberDao(db: AppDatabase): MemberDao = db.memberDao()

    // ── Repositories ──
    @Provides
    @Singleton
    fun provideAuthRepository(api: ApiService, tokenManager: TokenManager): AuthRepository =
        AuthRepositoryImpl(api, tokenManager)

    @Provides
    @Singleton
    fun provideMemberRepository(
        memberDao: MemberDao,
        api: ApiService,
        @ApplicationContext context: Context,
    ): MemberRepository = MemberRepositoryImpl(memberDao, api, context)
}