package com.banglalink.toffee.di

import android.app.Application
import android.content.Context
import com.banglalink.toffee.data.ToffeeConfig
import com.banglalink.toffee.data.network.interceptor.AuthInterceptor
import com.banglalink.toffee.data.network.interceptor.GetTracker
import com.banglalink.toffee.data.network.interceptor.IGetMethodTracker
import com.banglalink.toffee.data.network.interceptor.ToffeeDns
import com.banglalink.toffee.data.network.retrofit.AuthApi
import com.banglalink.toffee.data.network.retrofit.DbApi
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.lib.BuildConfig
import com.banglalink.toffee.receiver.ConnectionWatcher
import com.banglalink.toffee.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.Cache
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.*
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModuleLib {

    @Provides
    @Singleton
    @EncryptedHttpClient
    fun providesEncryptedHttpClient(@DefaultCache cache: Cache, toffeeDns: ToffeeDns, authInterceptor: AuthInterceptor): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder().apply {
            connectTimeout(15, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            retryOnConnectionFailure(false)
            if (BuildConfig.DEBUG && Log.SHOULD_LOG) {
                addInterceptor(HttpLoggingInterceptor().also {
                    it.level = HttpLoggingInterceptor.Level.BODY
                })
            }
            addInterceptor(authInterceptor)
            dns(toffeeDns)
            cache(cache)
        }
        return clientBuilder.build()
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
    fun providesRetrofit(@EncryptedHttpClient httpClient: OkHttpClient, toffeeConfig: ToffeeConfig): Retrofit {
        return Retrofit.Builder()
            .baseUrl(toffeeConfig.toffeeBaseUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providesToffeeApi(retrofit: Retrofit): ToffeeApi {
        return retrofit.create(ToffeeApi::class.java)
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
    @SimpleHttpClient
    fun providesSimpleHttpClient(@DefaultCache cache: Cache): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .cache(cache)
            .build()
    }

    @Provides
    @Singleton
    @DnsHttpClient
    fun providesDnsHttpClient(@SimpleHttpClient simpleHttpClient: OkHttpClient, toffeeDns: ToffeeDns): OkHttpClient {
        return simpleHttpClient.newBuilder()
            .dns(toffeeDns)
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
}