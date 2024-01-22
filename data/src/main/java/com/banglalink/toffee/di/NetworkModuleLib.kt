package com.banglalink.toffee.di

import android.app.Application
import android.content.Context
import coil.disk.DiskCache
import com.banglalink.toffee.data.Config
import com.banglalink.toffee.data.network.interceptor.AuthInterceptor
import com.banglalink.toffee.data.network.interceptor.CoilInterceptor
import com.banglalink.toffee.data.network.interceptor.GetTracker
import com.banglalink.toffee.data.network.interceptor.IGetMethodTracker
import com.banglalink.toffee.data.network.interceptor.PlainInterceptor
import com.banglalink.toffee.data.network.interceptor.ToffeeDns
import com.banglalink.toffee.data.network.retrofit.AuthApi
import com.banglalink.toffee.data.network.retrofit.DbApi
import com.banglalink.toffee.data.network.retrofit.ExternalApi
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.lib.BuildConfig
import com.banglalink.toffee.receiver.ConnectionWatcher
import com.banglalink.toffee.util.CustomCookieJar
import com.banglalink.toffee.util.Log
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.TlsVersion.*
import okhttp3.dnsoverhttps.DnsOverHttps
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.net.CookieManager
import java.util.concurrent.*
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModuleLib {
    
    @Provides
    @Singleton
    @EncryptedHttpClient
    fun providesEncryptedHttpClient(@DefaultCache cache: Cache, cookieJar: CustomCookieJar, toffeeDns: ToffeeDns, authInterceptor:
    AuthInterceptor, mPref:com.banglalink.toffee.data.storage.SessionPreference): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder().apply {
            connectTimeout(mPref.internalTimeOut.toLong(), TimeUnit.SECONDS)
            readTimeout(mPref.internalTimeOut.toLong(), TimeUnit.SECONDS)
            retryOnConnectionFailure(true)
            if (BuildConfig.DEBUG && Log.SHOULD_LOG) {
                addInterceptor(HttpLoggingInterceptor().also {
                    it.level = HttpLoggingInterceptor.Level.BODY
                })
            }
            addInterceptor(authInterceptor)
            dns(toffeeDns)
            cache(cache)
//            cookieJar(cookieJar)
        }
        return clientBuilder.build()
    }
    
    @Provides
    @Singleton
    @PlainHttpClient
    fun providesPlainHttpClient(@DefaultCache cache: Cache, toffeeDns: ToffeeDns, mPref:com.banglalink.toffee.data.storage.SessionPreference, plainInterceptor: PlainInterceptor): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder().apply {
            connectTimeout(mPref.internalTimeOut.toLong(), TimeUnit.SECONDS)
            readTimeout(mPref.internalTimeOut.toLong(), TimeUnit.SECONDS)
            retryOnConnectionFailure(true)
            if (BuildConfig.DEBUG && Log.SHOULD_LOG) {
                addInterceptor(HttpLoggingInterceptor().also {
                    it.level = HttpLoggingInterceptor.Level.BODY
                })
            }
            addInterceptor(plainInterceptor)
            dns(toffeeDns)
            cache(cache)
        }
        return clientBuilder.build()
    }
    
    @Provides
    @Singleton
    @CoilHttpClient
    fun providesCoilHttpClient(coilInterceptor: CoilInterceptor, toffeeDns: ToffeeDns): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder().apply {
            connectTimeout(15, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            retryOnConnectionFailure(true)
            addInterceptor(coilInterceptor)
            dns(toffeeDns)
        }
        return clientBuilder.build()
    }
    
    @Provides
    @Singleton
    fun providesCustomCookieJar(): CustomCookieJar {
        return CustomCookieJar()
    }
    
    @Provides
    @Singleton
    @CustomCookieManager
    fun providesCookieManager(): CookieManager {
        return CookieManager()
    }
    
    @Provides
    @Singleton
    @CoilCache
    fun getCoilCache(@ApplicationContext ctx: Context): DiskCache {
        return DiskCache.Builder()
            .directory(ctx.cacheDir.resolve("image_cache"))
            .maxSizeBytes(250 * 1024 * 1024) // 250MB
            .build()
    }
    
    @Provides
    @Singleton
    @DefaultCache
    fun getCacheIterator(@ApplicationContext ctx: Context): Cache {
        val cacheSize = 25 * 1024 * 1024 // 25 MB
        return Cache(ctx.cacheDir, cacheSize.toLong())
    }
    
    @Provides
    @Singleton
    fun providesRetrofit(@EncryptedHttpClient httpClient: OkHttpClient, config: Config, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(config.url)
            .client(httpClient)
            .addConverterFactory(json.asConverterFactory("text/plain".toMediaType()))
            .build()
    }
    
    @Provides
    @Singleton
    @ExternalApiRetrofit
    fun providesExternalApiRetrofit(@PlainHttpClient httpClient: OkHttpClient, config: Config, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(config.url)
            .client(httpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    
    @Provides
    @Singleton
    fun providesToffeeApi(retrofit: Retrofit): ToffeeApi {
        return retrofit.create(ToffeeApi::class.java)
    }
    
    @Provides
    @Singleton
    fun providesExternalApi(@ExternalApiRetrofit retrofit: Retrofit): ExternalApi {
        return retrofit.create(ExternalApi::class.java)
    }

    @Provides
    @Singleton
    fun providesToffeeDns(@SimpleHttpClient httpClient: OkHttpClient): DnsOverHttps {
        return DnsOverHttps.Builder()
            .url("https://dns.google/dns-query".toHttpUrl())
            .client(httpClient)
            .build()
    }

    @Provides
    @Singleton
    @CustomConnectionSpec
    fun getConnectionSpec(): ConnectionSpec {
        return ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
            .tlsVersions(TLS_1_3, TLS_1_2, TLS_1_1, TLS_1_0)
            .cipherSuites(
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA
            )
            .build()
    }
    
    @Provides
    @Singleton
    @SimpleHttpClient
    fun providesSimpleHttpClient(@DefaultCache cache: Cache, mPref:com.banglalink.toffee.data.storage.SessionPreference): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(if(mPref.internalTimeOut==0) 10 else mPref.internalTimeOut.toLong(), TimeUnit.SECONDS)
            .readTimeout(if(mPref.internalTimeOut==0) 20 else mPref.internalTimeOut.toLong() * 2, TimeUnit.SECONDS)
            .cache(cache)
            .build()
    }

    @Provides
    @Singleton
    @DnsHttpClient
    fun providesDnsHttpClient(@SimpleHttpClient simpleHttpClient: OkHttpClient, toffeeDns: ToffeeDns, cookieJar: CustomCookieJar): OkHttpClient {
        return simpleHttpClient.newBuilder()
            .dns(toffeeDns)
//            .cookieJar(cookieJar)
            .build()
    }

    @DbRetrofit
    @Singleton
    @Provides
    fun providesDbRetrofit(@SimpleHttpClient httpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://real-db.toffeelive.com/")
            .client(httpClient)
            .build()
    }

    @Provides
    @Singleton
    fun providesDbApi(@DbRetrofit dbRetrofit: Retrofit): DbApi {
        return dbRetrofit.create(DbApi::class.java)
    }

    @Provides
    @Singleton
    fun providesAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    fun providesConnectionWatcher(app: Application): ConnectionWatcher {
        return ConnectionWatcher(app)
    }

    @Provides
    @Singleton
    fun providesGetTracker(): IGetMethodTracker {
        return GetTracker()
    }
    
    @Provides
    @Singleton
    fun providesJsonWithConfig(): Json {
        return Json {
            encodeDefaults = false
            ignoreUnknownKeys = true
        }
    }
}