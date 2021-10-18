package com.banglalink.toffee.di

import android.content.Context
import coil.util.CoilUtils
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
}