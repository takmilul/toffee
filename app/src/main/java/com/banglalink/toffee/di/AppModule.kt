package com.banglalink.toffee.di

import android.app.Application
import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.apiservice.UploadConfirmation
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.ui.upload.UploadObserver
import com.banglalink.toffee.ui.upload.UploadStateManager
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
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
    @FirebaseInAppMessage
    fun providesFirebaseInAppMessaging(): FirebaseInAppMessaging {
        return FirebaseInAppMessaging.getInstance()
    }
    
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
    
    @Provides
    @Singleton
    @ApplicationId
    fun providesApplicationId(): String {
        return BuildConfig.APPLICATION_ID
    }
    
    @Provides
    @Singleton
    @AppVersionName
    fun providesAppVersionName(): String {
        return BuildConfig.VERSION_NAME
    }
    
    @Provides
    @Singleton
    @AppVersionCode
    fun providesAppVersionCode(): Int {
        return BuildConfig.VERSION_CODE
    }
}