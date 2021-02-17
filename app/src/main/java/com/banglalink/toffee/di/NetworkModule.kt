package com.banglalink.toffee.di

import android.app.Application
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.data.network.interceptor.AuthInterceptor
import com.banglalink.toffee.data.network.interceptor.GetTracker
import com.banglalink.toffee.data.network.retrofit.AuthApi
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.network.retrofit.DbApi
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.receiver.ConnectionWatcher
import com.facebook.FacebookSdk
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class EncryptedHttpClient

@Qualifier
annotation class DefaultCache

@Qualifier
annotation class DbRetrofit

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @EncryptedHttpClient
    @Provides
    fun providesEncryptedHttpClient(@DefaultCache cache: Cache): OkHttpClient {
//        val cacheSize = 10 * 1024 * 1024 // 10 MB
//        val cache = Cache(FacebookSdk.getCacheDir(), cacheSize.toLong())

        val clientBuilder = OkHttpClient.Builder().apply {
            connectTimeout(15, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            retryOnConnectionFailure(false)
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().also {
                    it.level = HttpLoggingInterceptor.Level.BODY
                })
            }
            cache(cache)
            addInterceptor(AuthInterceptor(GetTracker()))
        }
        return clientBuilder.build()
    }

    @Provides
    @Singleton
    @DefaultCache
    fun getCacheIterator(): Cache{
        val cacheSize = 10 * 1024 * 1024 // 10 MB
        return Cache(FacebookSdk.getCacheDir(), cacheSize.toLong())
    }
    
    @Provides
    @Singleton
    fun providesRetrofit(@EncryptedHttpClient httpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(httpClient)
//            .baseUrl("https://mapi.toffeelive.com/")
            .baseUrl("https://ugc-staging.toffeelive.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providesToffeeApi(retrofit: Retrofit): ToffeeApi {
        return retrofit.create(ToffeeApi::class.java)
//        return RetrofitApiClient.toffeeApi
    }

    @DbRetrofit
    @Singleton
    @Provides
    fun providesDbRetrofit(): Retrofit {
        return Retrofit.Builder()
            .client(OkHttpClient.Builder().build())
            .baseUrl("https://real-db.toffeelive.com/")
//            .baseUrl("https://dev.toffeelive.com/")
            .build()
    }

    @Provides
    @Singleton
    fun providesDbApi(@DbRetrofit dbRetrofit: Retrofit): DbApi {
        return dbRetrofit.create(DbApi::class.java)
//        return RetrofitApiClient.toffeeApi
    }

    @Provides
    @Singleton
    fun providesAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
//        return RetrofitApiClient.authApi
    }

    @ExperimentalCoroutinesApi
    @Singleton
    @Provides
    fun providesConnectionWatcher(app: Application): ConnectionWatcher {
        return ConnectionWatcher(app)
    }
    
    @Provides
    @Singleton
    fun providesCacheManager(@DefaultCache retrofitCache: Cache): CacheManager{
        return CacheManager(retrofitCache)
    }
}