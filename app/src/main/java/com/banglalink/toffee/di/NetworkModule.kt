package com.banglalink.toffee.di

import android.content.Context
import coil.disk.DiskCache
import com.banglalink.toffee.data.ToffeeConfig
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.google.android.exoplayer2.util.Util
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.Cache

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {
    
    const val isDebugMessageActive: Boolean = false
    
    private const val TOFFEE_BASE_URL = "https://mapi.toffeelive.com/"
//    private const val TOFFEE_BASE_URL = "https://staging.toffee-cms.com/"
//    private const val TOFFEE_BASE_URL = "https://ugc-staging.toffeelive.com/"
//    private const val TOFFEE_BASE_URL = "https://j1-staging.toffeelive.com/"
    
    @Provides
    @Singleton
    fun providesToffeeConfig(): ToffeeConfig {
        return ToffeeConfig(
            toffeeBaseUrl = TOFFEE_BASE_URL
        )
    }
    
    @Provides
    @ToffeeHeader
    fun providesToffeeHeader(@ApplicationContext context: Context, mPref: SessionPreference, cPref: CommonPreference): String {
        return Util.getUserAgent(context, "Toffee") +
                "/" +
                mPref.customerId +
                "/" +
                cPref.deviceId
    }
    
    @Provides
    @ApiHeader
    fun providesApiHeader(@ApplicationContext context: Context): String {
        return Util.getUserAgent(context, "Toffee")
    }
}