package com.banglalink.toffee.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.ui.upload.UploadObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class AppCoroutineScope

@InstallIn(ApplicationComponent::class)
@Module
object AppModule {

    @Provides
    fun providesSharedPreference(@ApplicationContext app: Context): SharedPreferences {
        return app.getSharedPreferences("IP_TV", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providesPreference(pref: SharedPreferences, @ApplicationContext ctx: Context): Preference {
        return Preference(pref, ctx)
    }

    @Provides
    @Singleton
    fun providesGlobalUploadObserver(app: Application, pref: Preference, repo: UploadInfoRepository): UploadObserver {
        return UploadObserver(app, pref, repo)
    }

    @Provides
    @Singleton
    @AppCoroutineScope
    fun providesApplicationCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }
}