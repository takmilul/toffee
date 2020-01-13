package com.banglalink.toffee.data.network.retrofit

import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.data.network.interceptor.AuthInterceptor
import com.banglalink.toffee.util.unsafeLazy
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitApiClient {

    private val retrofit: Retrofit
    init {
        val clientBuilder = OkHttpClient.Builder()
        clientBuilder.connectTimeout(15, TimeUnit.SECONDS)
        clientBuilder.readTimeout(15, TimeUnit.SECONDS)
        clientBuilder.retryOnConnectionFailure(false)
        if (BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            clientBuilder.addInterceptor(interceptor)
        }
        clientBuilder.addInterceptor(AuthInterceptor())

        val client = clientBuilder.build()

        retrofit = Retrofit.Builder()
            .client(client)
//            .baseUrl("https://mapi.toffeelive.com/")
            .baseUrl("https://mapirnd.toffeelive.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by unsafeLazy {
        retrofit.create(AuthApi::class.java)
    }

    val toffeeApi: ToffeeApi by unsafeLazy {
        retrofit.create(ToffeeApi::class.java)
    }
}