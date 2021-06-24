package com.banglalink.toffee.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
//@TestInstallIn(
//    components = [SingletonComponent::class],
//    replaces = [NetworkModule::class]
//)
@InstallIn(SingletonComponent::class)
object TestNetworkModule {

}