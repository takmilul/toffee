package com.banglalink.toffee.di

import android.content.Context
import androidx.media3.common.util.Util
import com.banglalink.toffee.data.ToffeeConfig
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
object NetworkModule {
    
    const val isDebugMessageActive: Boolean = false
    
//    private const val TOFFEE_BASE_URL = "https://mapi.toffeelive.com/"          // production server
    private const val TOFFEE_BASE_URL = "https://j1-staging.toffeelive.com/"  // staging server
    
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