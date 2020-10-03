package com.banglalink.toffee.data.network.retrofit

import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.data.network.interceptor.AuthInterceptor
import com.banglalink.toffee.data.network.interceptor.GetTracker
import com.banglalink.toffee.util.unsafeLazy
import com.facebook.FacebookSdk.getCacheDir
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitApiClient {

    private val retrofit: Retrofit
    private val dbRetrofit: Retrofit

    init {
        //retrofit instance for api
        retrofit = Retrofit.Builder()
            .client(buildApiHttpClient())
//            .baseUrl("https://mapi.toffeelive.com/")
            .baseUrl("https://staging.toffee-cms.com/")
//            .baseUrl("https://dev.toffeelive.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        //retrofit instance for db stream
        dbRetrofit = Retrofit.Builder()
            .client(OkHttpClient.Builder().build())
            .baseUrl("https://real-db.toffeelive.com/")
//            .baseUrl("https://dev.toffeelive.com/")
            .build()
    }

    private fun buildApiHttpClient():OkHttpClient{
        val cacheSize = 25 * 1024 * 1024 // 25 MB

        val cache = Cache(getCacheDir(), cacheSize.toLong())

        val clientBuilder = OkHttpClient.Builder()

        clientBuilder.connectTimeout(15, TimeUnit.SECONDS)
        clientBuilder.readTimeout(30, TimeUnit.SECONDS)
        clientBuilder.retryOnConnectionFailure(false)
        clientBuilder.cache(cache)

        if (BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            clientBuilder.addInterceptor(interceptor)
        }
        clientBuilder.addInterceptor(AuthInterceptor(GetTracker()))
        return clientBuilder.build()

    }

    val authApi: AuthApi by unsafeLazy {
        retrofit.create(AuthApi::class.java)
    }

    val toffeeApi: ToffeeApi by unsafeLazy {
        retrofit.create(ToffeeApi::class.java)
    }

    val dbApi:DbApi by unsafeLazy {
        dbRetrofit.create(DbApi::class.java)
    }
}