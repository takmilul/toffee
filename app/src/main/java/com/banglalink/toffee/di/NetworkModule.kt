package com.banglalink.toffee.di

import android.content.Context
import android.os.Build
import coil.util.CoilUtils
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.BuildConfig
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