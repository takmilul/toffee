package com.banglalink.toffee.di

import android.content.Context
import android.content.SharedPreferences
import com.banglalink.toffee.data.storage.COMMON_PREF_NAME
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.PREF_NAME_IP_TV
import com.banglalink.toffee.data.storage.SessionPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModuleLib {

    @Provides
    @com.banglalink.toffee.di.SessionPreference
    fun providesSessionSharedPreference(@ApplicationContext app: Context): SharedPreferences {
        return app.getSharedPreferences(PREF_NAME_IP_TV, Context.MODE_PRIVATE)
    }

    @Provides
    @com.banglalink.toffee.di.CommonPreference
    fun providesCommonSharedPreference(@ApplicationContext app: Context): SharedPreferences {
        return app.getSharedPreferences(COMMON_PREF_NAME, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providesPreference(@com.banglalink.toffee.di.SessionPreference pref: SharedPreferences, @ApplicationContext ctx: Context): SessionPreference {
        return SessionPreference(pref, ctx)
    }

    @Provides
    @Singleton
    @AppCoroutineScope
    fun providesApplicationCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }
    
    @Provides
    @Singleton
    fun provideCommonPreference(@com.banglalink.toffee.di.CommonPreference pref: SharedPreferences, @ApplicationContext ctx: Context): CommonPreference {
        return CommonPreference(pref, ctx)
    }
}