package com.banglalink.toffee.model

data class DBVersion(
    val chanelDbVersion: Int,
    val vodDbVersion: Int,
    val notificationDbVersion: Int,
    val catchupDbVersion: Int,
    val packageDbVersion: Int
)