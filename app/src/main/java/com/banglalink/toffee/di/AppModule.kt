package com.banglalink.toffee.di

import com.banglalink.toffee.BuildConfig
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