package com.banglalink.toffee.di

import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.data.network.interceptor.AuthInterceptor
import com.banglalink.toffee.data.network.interceptor.GetTracker
import com.banglalink.toffee.data.network.retrofit.AuthApi
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.facebook.FacebookSdk
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
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

@InstallIn(ApplicationComponent::class)
@Module
object NetworkModule {

    @EncryptedHttpClient
    @Provides
    fun providesEncryptedHttpClient(): OkHttpClient {
        val cacheSize = 10 * 1024 * 1024 // 10 MB
        val cache = Cache(FacebookSdk.getCacheDir(), cacheSize.toLong())

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
    fun providesRetrofit(@EncryptedHttpClient httpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(httpClient)
//            .baseUrl("https://mapi.toffeelive.com/")
            .baseUrl("https://staging.toffee-cms.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providesToffeeApi(retrofit: Retrofit): ToffeeApi {
//        return retrofit.create(ToffeeApi::class.java)
        return RetrofitApiClient.toffeeApi
    }

    @Provides
    @Singleton
    fun providesAuthApi(retrofit: Retrofit): AuthApi {
//        return retrofit.create(AuthApi::class.java)
        return RetrofitApiClient.authApi
    }
}