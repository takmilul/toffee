package com.banglalink.toffee.di

import javax.inject.Qualifier

@Qualifier
annotation class EncryptedHttpClient

@Qualifier
annotation class CustomConnectionSpec

@Qualifier
annotation class SimpleHttpClient

@Qualifier
annotation class DnsHttpClient

@Qualifier
annotation class DefaultCache

@Qualifier
annotation class CoilCache

@Qualifier
annotation class DbRetrofit

@Qualifier
annotation class AppCoroutineScope

@Qualifier
annotation class SessionPreference

@Qualifier
annotation class CommonPreference

@Qualifier
annotation class ToffeeHeader

@Qualifier
annotation class ApiHeader

@Qualifier
annotation class CustomCookieManager

@Qualifier
annotation class ExternalApiRetrofit

@Qualifier
annotation class PlainHttpClient

@Qualifier
annotation class CoilHttpClient

@Qualifier
annotation class FirebaseInAppMessage

@Qualifier
annotation class ApplicationId

@Qualifier
annotation class AppVersionName

@Qualifier
annotation class AppVersionCode