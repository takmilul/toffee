package com.banglalink.toffee.di

import android.app.Application
import com.banglalink.toffee.apiservice.UploadConfirmation
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.ui.upload.UploadObserver
import com.banglalink.toffee.ui.upload.UploadStateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    fun providesUploadStateManager(mPref: SessionPreference, app: Application, repo: UploadInfoRepository, uploadConfApi: UploadConfirmation): UploadStateManager {
        return UploadStateManager(app, mPref, repo, uploadConfApi)
    }

    @Provides
    @Singleton
    fun providesGlobalUploadObserver(app: Application, manager: UploadStateManager, cacheManager: CacheManager): UploadObserver {
        return UploadObserver(app, manager, cacheManager)
    }
}