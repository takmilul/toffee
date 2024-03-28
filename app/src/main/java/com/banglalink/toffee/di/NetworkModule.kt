package com.banglalink.toffee.di

import android.content.Context
import androidx.media3.common.util.Util
import com.banglalink.toffee.Constants
import com.banglalink.toffee.data.Config
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
    
    const val IS_DEBUG_MESSAGE_ACTIVE: Boolean = false
    
    private val BASE_URL: String = Constants.STAGING_URL
//    private val BASE_URL: String = Constants.PROD_URL
    
//    init {
//        System.loadLibrary("native-lib")
//    }
    
//    private external fun getUrl(): String
    
    @Provides
    @Singleton
    fun providesConfig(): Config {
        return Config(
//            url = getUrl()
            url = BASE_URL
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