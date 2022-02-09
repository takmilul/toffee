package com.banglalink.toffee.di

import com.banglalink.toffee.data.storage.SessionPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import javax.inject.Qualifier

@Qualifier
annotation class TestSessionPreference

@Module
//@TestInstallIn(
//    components = [SingletonComponent::class],
//    replaces = [AppModule::class]
//)
@InstallIn(SingletonComponent::class)
object TestAppModule {
    @Provides
    @TestSessionPreference
    fun providesSessionPreference(): SessionPreference {
        val pref: SessionPreference = mockk()
//        val tempSlot = slot<String>()
//        every { pref.getDBVersionByApiName(any()) } answers { 5 }
        return pref
    }
}