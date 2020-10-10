package com.banglalink.toffee.di

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.migration.DisableInstallInCheck

//@DisableInstallInCheck
@InstallIn(ActivityComponent::class)
@AssistedModule
@Module/*(includes = [AssistedInject_AssistedInjectModule::class])*/
interface ChannelParamsAssistedInjectModule {}