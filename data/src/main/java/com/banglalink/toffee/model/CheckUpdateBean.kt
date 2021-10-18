package com.banglalink.toffee.model

data class CheckUpdateBean(
    val updateAvailable: Int,
    val message: String,
    var messageTitle: String) {
}