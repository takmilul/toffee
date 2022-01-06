package com.banglalink.toffee.di

import android.content.Context
import android.os.Build
import coil.util.CoilUtils
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.data.ToffeeConfig
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.google.android.exoplayer2.ExoPlayerLibraryInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

//    private const val TOFFEE_BASE_URL = "https://mapi.toffeelive.com/"
//    const val TOFFEE_BASE_URL = "https://staging.toffee-cms.com/"
//    const val TOFFEE_BASE_URL = "https://ugc-staging.toffeelive.com/"
    const val TOFFEE_BASE_URL = "https://j1-staging.toffeelive.com/"

    @Provides
    @Singleton
    fun providesToffeeConfig(): ToffeeConfig {
        return ToffeeConfig(
            toffeeBaseUrl = TOFFEE_BASE_URL
        )
    }

    @Provides
    @Singleton
    @CoilCache
    fun getCoilCache(@ApplicationContext ctx: Context): Cache {
        return CoilUtils.createDefaultCache(ctx)
    }

    @Provides
    @ToffeeHeader
    fun providesToffeeHeader(mPref: SessionPreference, cPref: CommonPreference): String {
        return "Toffee" +
                "/" +
                BuildConfig.VERSION_NAME +
                " (Linux;Android " +
                Build.VERSION.RELEASE +
                ") " +
                ExoPlayerLibraryInfo.VERSION_SLASHY +
                "/" +
                mPref.customerId +
                "/" +
                cPref.deviceId
    }
}